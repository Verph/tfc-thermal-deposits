package tfcthermaldeposits.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.ForgeEventFactory;

import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.common.TDTags;
import tfcthermaldeposits.common.blocks.InverseBooleanProperty;
import tfcthermaldeposits.common.blocks.TDBlockStateProperties;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin extends Fluid
{
    @Unique private static final IntegerProperty MINERAL_AMOUNT = TDBlockStateProperties.MINERAL_AMOUNT;
    @Unique private static final BooleanProperty NATURAL = TDBlockStateProperties.NATURAL;
    @Unique private static final InverseBooleanProperty NATURAL_INVERSE = TDBlockStateProperties.NATURAL_INVERSE;

    @Inject(method = "createFluidStateDefinition", at = @At("TAIL"))
    protected void inject$createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder, CallbackInfo ci)
    {
        builder.add(NATURAL_INVERSE, MINERAL_AMOUNT);
    }

    @Inject(method = "getSlopeDistance", at = @At("HEAD"), cancellable = true)
    protected void inject$getSlopeDistance(LevelReader level, BlockPos spreadPos, int distance, Direction sourceDirection, BlockState currentSpreadState, BlockPos sourcePos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap waterHoleCache, CallbackInfoReturnable<Integer> cir)
    {
        if (this.getFluidType().equals(Fluids.LAVA.getFluidType()))
        {
            int i = 1000;

            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (direction != sourceDirection)
                {
                    BlockPos blockpos = spreadPos.relative(direction);
                    short short1 = getCacheKey(sourcePos, blockpos);
                    Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(short1, (map) -> {
                        BlockState blockstate1 = level.getBlockState(blockpos);
                        return Pair.of(blockstate1, blockstate1.getFluidState());
                    });
                    BlockState blockstate = pair.getFirst();
                    FluidState fluidstate = pair.getSecond();
                    if (this.canPassThrough(level, this.getFlowing(), spreadPos, currentSpreadState, direction, blockpos, blockstate, fluidstate))
                    {
                        boolean flag = waterHoleCache.computeIfAbsent(short1, (map) -> {
                            BlockPos blockpos1 = blockpos.below();
                            BlockState blockstate1 = level.getBlockState(blockpos1);
                            return this.isWaterHole(level, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                        });
                        if (flag)
                        {
                            cir.setReturnValue(distance);
                        }
                        if (level instanceof Level actualLevel && distance < TDHelpers.getSlopeFindDistance(actualLevel, sourcePos))
                        {
                            int j = this.getSlopeDistance(level, blockpos, distance + 1, direction.getOpposite(), blockstate, sourcePos, stateCache, waterHoleCache);
                            if (j < i)
                            {
                                i = j;
                            }
                        }
                    }
                }
            }
            cir.setReturnValue(i);
        }
        cir.getReturnValue();
    }

    @Inject(method = "spreadToSides", at = @At("HEAD"), cancellable = true)
    private void inject$spreadToSides(Level level, BlockPos pos, FluidState fluidState, BlockState blockState, CallbackInfo ci)
    {
        if (this.getFluidType().equals(Fluids.LAVA.getFluidType()))
        {
            int i = fluidState.getAmount() - TDHelpers.getDropOff(level, pos);
            if (fluidState.getValue(FlowingFluid.FALLING))
            {
                i = 7;
            }
            if (i > 0)
            {
                Map<Direction, FluidState> map = this.getSpread(level, pos, blockState);
                for (Map.Entry<Direction, FluidState> entry : map.entrySet())
                {
                    Direction direction = entry.getKey();
                    FluidState fluidstate = entry.getValue();
                    BlockPos blockpos = pos.relative(direction);
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (this.canSpreadTo(level, pos, blockState, direction, blockpos, blockstate, level.getFluidState(blockpos), fluidstate.getType()))
                    {
                        this.spreadTo(level, blockpos, blockstate, direction, fluidstate);
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "getNewLiquid", at = @At("HEAD"), cancellable = true)
    protected void inject$getNewLiquid(Level level, BlockPos pos, BlockState blockState, CallbackInfoReturnable<FluidState> cir)
    {
        boolean isLava = this.getFluidType().equals(Fluids.LAVA.getFluidType());
        //boolean isWater = Helpers.isFluid(this, TFCTags.Fluids.ANY_WATER) || this.getFluidType().equals(Fluids.WATER.getFluidType()) || this.getFluidType().equals(TFCFluids.SALT_WATER.getSource().getFluidType()) || this.getFluidType().equals(TFCFluids.SPRING_WATER.getSource().getFluidType()) || this.getFluidType().equals(TFCFluids.RIVER_WATER.get().getFluidType());
        if (isLava || (Helpers.isFluid(this, TDTags.Fluids.FINITE_FLUIDS) && TDConfig.COMMON.toggleFiniteWater.get()))
        {
            int i = 0;
            int j = 0;

            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                BlockPos blockpos = pos.relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                FluidState fluidstate = blockstate.getFluidState();
                if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, level, pos, blockState, blockpos, blockstate))
                {
                    if (fluidstate.isSource() && ForgeEventFactory.canCreateFluidSource(level, blockpos, blockstate, fluidstate.canConvertToSource(level, blockpos)))
                    {
                        ++j;
                    }
                    i = Math.max(i, fluidstate.getAmount());
                }
            }

            if (j >= 2)
            {
                BlockState blockstate1 = level.getBlockState(pos.below());
                FluidState fluidstate1 = blockstate1.getFluidState();
                if (blockstate1.isSolid() || this.isSourceBlockOfThisType(fluidstate1))
                {
                    cir.setReturnValue(((FlowingFluid) (Object) this).getSource(false));
                }
            }

            BlockPos blockpos1 = pos.above();
            BlockState blockstate2 = level.getBlockState(blockpos1);
            FluidState fluidstate2 = blockstate2.getFluidState();
            if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, level, pos, blockState, blockpos1, blockstate2))
            {
                cir.setReturnValue(((FlowingFluid)(Object) this).getFlowing(8, true));
            }
            else
            {
                int k = i - (isLava ? TDHelpers.getDropOff(level, pos) : this.getDropOff(level));
                cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : ((FlowingFluid)(Object) this).getFlowing(k, false));
            }
            cir.cancel();
        }
        cir.getReturnValue();
    }

    /*
     * Ugh.
     */
    @Shadow
    protected int getSlopeDistance(LevelReader level, BlockPos spreadPos, int distance, Direction sourceDirection, BlockState currentSpreadState, BlockPos sourcePos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap waterHoleCache)
    {
        return 1000;
    }

    @Shadow
    private static short getCacheKey(BlockPos sourcePos, BlockPos spreadPos)
    {
        return 1;
    }

    @Shadow
    private boolean canPassThrough(BlockGetter level, Fluid pFluid, BlockPos pos, BlockState pState, Direction direction, BlockPos spreadPos, BlockState pSpreadState, FluidState fluidState)
    {
        return true;
    }

    @Shadow
    private boolean isWaterHole(BlockGetter level, Fluid pFluid, BlockPos pos, BlockState pState, BlockPos spreadPos, BlockState pSpreadState)
    {
        return false;
    }

    @Shadow
    private boolean isSourceBlockOfThisType(FluidState state)
    {
        return false;
    }

    @Shadow
    private boolean canPassThroughWall(Direction direction, BlockGetter level, BlockPos pos, BlockState state, BlockPos spreadPos, BlockState spreadState)
    {
        return false;
    }

    @Shadow
    public abstract Fluid getSource();

    @Shadow
    public FluidState getSource(boolean falling)
    {
        return this.getSource().defaultFluidState();
    }

    @Shadow
    public abstract Fluid getFlowing();

    @Shadow
    public FluidState getFlowing(int level, boolean falling)
    {
        return this.getFlowing().defaultFluidState();
    }

    @Shadow
    protected abstract int getSlopeFindDistance(LevelReader level);

    @Shadow
    protected Map<Direction, FluidState> getSpread(Level level, BlockPos pos, BlockState state)
    {
        return Maps.newEnumMap(Direction.class);
    }

    @Shadow
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState) {}

    @Shadow
    protected abstract int getDropOff(LevelReader level);

    @Shadow
    protected boolean canSpreadTo(BlockGetter level, BlockPos pFromPos, BlockState pFromBlockState, Direction pDirection, BlockPos pToPos, BlockState pToBlockState, FluidState pToFluidState, Fluid pFluid)
    {
        return false;
    }
}