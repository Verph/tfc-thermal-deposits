package tfcthermaldeposits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.mixin.accessor.FlowingFluidAccessor;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.ForgeEventFactory;

import tfcthermaldeposits.common.TDTags;
import tfcthermaldeposits.config.TDConfig;

@Mixin(FluidHelpers.class)
public abstract class FluidHelpersMixin
{
    @Inject(method = "getNewFluidWithMixing", at = @At("HEAD"), cancellable = true, remap = false)
    private static void inject$getNewFluidWithMixing(FlowingFluid self, Level level, BlockPos pos, BlockState blockStateIn, boolean canConvertToSource, int dropOff, CallbackInfoReturnable<FluidState> cir)
    {
        //boolean isWater = self.getFluidType().equals(Fluids.WATER.getFluidType()) || self.getFluidType().equals(TFCFluids.SALT_WATER.getSource().getFluidType()) || self.getFluidType().equals(TFCFluids.SPRING_WATER.getSource().getFluidType()) || self.getFluidType().equals(TFCFluids.RIVER_WATER.get().getFluidType());
        if (Helpers.isFluid(self, TDTags.Fluids.FINITE_FLUIDS) && TDConfig.COMMON.toggleFiniteWater.get())
        {
            int maxAdjacentFluidAmount = 0;
            FlowingFluid maxAdjacentFluid = self;
            Object2IntArrayMap<FlowingFluid> adjacentSourceBlocksByFluid = new Object2IntArrayMap<>(2);

            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                BlockPos offsetPos = pos.relative(direction);
                BlockState offsetState = level.getBlockState(offsetPos);
                FluidState offsetFluid = offsetState.getFluidState();

                if (offsetFluid.getType() instanceof FlowingFluid && ((FlowingFluidAccessor) self).invoke$canPassThroughWall(direction, level, pos, blockStateIn, offsetPos, offsetState))
                {
                    if (offsetFluid.isSource() && ForgeEventFactory.canCreateFluidSource(level, offsetPos, offsetState, canConvertToSource))
                    {
                        adjacentSourceBlocksByFluid.mergeInt((FlowingFluid) offsetFluid.getType(), 1, Integer::sum);
                    }
                    if (offsetFluid.getAmount() > maxAdjacentFluidAmount || (offsetFluid.getAmount() == maxAdjacentFluidAmount && self.isSame(offsetFluid.getType())))
                    {
                        maxAdjacentFluidAmount = offsetFluid.getAmount();
                        maxAdjacentFluid = (FlowingFluid) offsetFluid.getType();
                    }
                }
            }

            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            FluidState aboveFluid = aboveState.getFluidState();
            if (!aboveFluid.isEmpty() && aboveFluid.getType() instanceof FlowingFluid && ((FlowingFluidAccessor) self).invoke$canPassThroughWall(Direction.UP, level, pos, blockStateIn, abovePos, aboveState))
            {
                cir.setReturnValue(((FlowingFluid) aboveFluid.getType()).getFlowing(8, true));
            }
            else
            {
                int selfFluidAmount = maxAdjacentFluidAmount - dropOff;
                if (selfFluidAmount <= 0)
                {
                    cir.setReturnValue(Fluids.EMPTY.defaultFluidState());
                }
                if (selfFluidAmount >= 1)
                {
                    cir.setReturnValue(maxAdjacentFluid.getFlowing(selfFluidAmount, false));
                }
            }
        }
        else
        {
            cir.getReturnValue();
        }
    }
}
