package tfcthermaldeposits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.HotWaterBlock;
import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.common.TDTags;
import tfcthermaldeposits.common.blocks.InverseBooleanProperty;
import tfcthermaldeposits.common.blocks.TDBlockStateProperties;
import tfcthermaldeposits.common.blocks.TDBlocks;
import tfcthermaldeposits.common.blocks.rock.Mineral;
import tfcthermaldeposits.common.blocks.rock.MineralSheetBlock;
import tfcthermaldeposits.common.items.TDItems;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

@Mixin(HotWaterBlock.class)
public abstract class HotWaterBlockMixin extends LiquidBlock
{
    @Unique private static final IntegerProperty MINERAL_AMOUNT = TDBlockStateProperties.MINERAL_AMOUNT;
    @Unique private static final BooleanProperty NATURAL = TDBlockStateProperties.NATURAL;
    @Unique private static final InverseBooleanProperty NATURAL_INVERSE = TDBlockStateProperties.NATURAL_INVERSE;

    public HotWaterBlockMixin(FlowingFluid fluid, Properties properties)
    {
        super(fluid, properties);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void inject$init(CallbackInfo ci)
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(MINERAL_AMOUNT, 0).setValue(NATURAL, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        boolean notPlayer = context.getPlayer() == null;
        return this.defaultBlockState().setValue(NATURAL, notPlayer).setValue(MINERAL_AMOUNT, notPlayer ? Mth.clamp(context.getLevel().getRandom().nextInt(100), 50, 100) : 0);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        if (level.getFluidState(currentPos).getValue(NATURAL_INVERSE))
        {
            state.setValue(NATURAL, true);
        }
        if (level.getFluidState(currentPos).getValue(MINERAL_AMOUNT) > 0)
        {
            state.setValue(MINERAL_AMOUNT, level.getFluidState(currentPos).getValue(MINERAL_AMOUNT));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (level.getFluidState(pos).hasProperty(NATURAL_INVERSE))
        {
            if (level.getFluidState(pos).getValue(NATURAL_INVERSE))
            {
                level.setBlockAndUpdate(pos, state.setValue(NATURAL, true));
            }
        }

        boolean hasProperties = state.hasProperty(NATURAL) && state.hasProperty(MINERAL_AMOUNT);
        if (hasProperties)
        {
            boolean validDimension = level.dimension() == Level.OVERWORLD || TDConfig.COMMON.toggleOtherDimensions.get();
            FluidState fluidState = level.getFluidState(pos);

            boolean isNatural = (hasProperties && state.getValue(NATURAL)) || TDConfig.COMMON.toggleDepositsFromAllLavaOrSpringWater.get();

            if (validDimension && hasProperties && isNatural && fluidState.isSource())
            {
                if (TDConfig.COMMON.shouldHotSpringsDepositMinerals.get() && state.getValue(MINERAL_AMOUNT) > 0)
                {
                    if (random.nextInt(TDConfig.COMMON.mineralGenFrequencyHotSpring.get()) == 0)
                    {
                        Mineral mineral = TDHelpers.waterMineral(level, pos, random, TDHelpers.rockTypeMantle(level, pos).displayCategory());

                        if (mineral != null)
                        {
                            ItemStack itemMineral = new ItemStack(TDItems.MINERALS.get(mineral).get().asItem());

                            BlockPos genPos = pos.offset(random.nextInt(7) - 3, random.nextInt(7) - 3, random.nextInt(7) - 3);
                            BlockState stateAt = level.getBlockState(genPos);

                            Direction face = Direction.getRandom(random);
                            Direction sheetFace = face.getOpposite();

                            BooleanProperty directionProperty = DirectionPropertyBlock.getProperty(face);

                            BlockPos adjacentPos = genPos.relative(face);
                            BlockState adjacentState = level.getBlockState(adjacentPos);

                            if (Helpers.isBlock(stateAt, TDBlocks.MINERAL_SHEET.get()))
                            {
                                if (!stateAt.getValue(directionProperty) && adjacentState.isFaceSturdy(level, adjacentPos, sheetFace))
                                {
                                    MineralSheetBlock.addSheet(level, genPos, stateAt, face, itemMineral);
                                    TDHelpers.changeMineralContent(level, state, pos, -1);
                                }
                            }
                            else if (stateAt.canBeReplaced() && !stateAt.liquid() && !(stateAt.getMapColor(level, pos) == MapColor.FIRE || stateAt.getMapColor(level, pos) == MapColor.WATER))
                            {
                                if (adjacentState.isFaceSturdy(level, adjacentPos, sheetFace))
                                {
                                    BlockState placingState = TDBlocks.MINERAL_SHEET.get().defaultBlockState().setValue(directionProperty, true);
                                    MineralSheetBlock.addSheet(level, genPos, placingState, face, itemMineral);
                                    TDHelpers.changeMineralContent(level, state, pos, -1);
                                }
                            }
                        }
                    }
                }
                if (TDConfig.COMMON.naturalReplenishment.get() && state.getValue(MINERAL_AMOUNT) < 100 && random.nextInt((int) Math.ceil((Mth.abs(TDConfig.COMMON.mineralGenFrequencyHotSpring.get() * 6) * TDHelpers.replenishmentFactor(level, pos)))) == 0)
                {
                    TDHelpers.changeMineralContent(level, state, pos, level.getRandom().nextInt(3));
                }
            }
        }
    }

    @Inject(method = "entityInside", at = @At("TAIL"))
    private void inject$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci)
    {
        if (state.hasProperty(NATURAL) && state.hasProperty(MINERAL_AMOUNT))
        {
            if (entity instanceof ItemEntity itemEntity)
            {
                if (Helpers.isItem(itemEntity.getItem(), TDTags.Items.REPLENISHES_LAVA))
                {
                    itemEntity.setInvulnerable(true);
                    /*
                    * Spawn particles to create a visual effect and give feedback
                    * Higher mineral content, the more bubbles/particles and sounds
                    */
                    if (level.isEmptyBlock(pos.above()))
                    {
                        RandomSource random = level.getRandom();
                        for (int j = 0; j < 1 + itemEntity.getItem().getCount() + Math.round(state.getValue(MINERAL_AMOUNT) * 0.25F); j++)
                        {
                            double d0 = pos.getX() + 0.5D + (random.nextGaussian() * 0.15D);
                            double d1 = pos.getY() + 1.0D;
                            double d2 = pos.getZ() + 0.5D + (random.nextGaussian() * 0.15D);
                            ParticleOptions particle = random.nextInt() == 0 ? ParticleTypes.BUBBLE_POP : ParticleTypes.SPLASH;
                            level.addAlwaysVisibleParticle(particle, d0, d1, d2, 0.0D, 0.01D + random.nextDouble() * 0.01D, 0.0D);
                        }
                        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.6F + random.nextFloat() * 0.2F, false);
                    }
                    if (level instanceof ServerLevel serverLevel)
                    {
                        int mineralAmount = state.getValue(MINERAL_AMOUNT);
                        if (mineralAmount < 100)
                        {
                            int stackSize = itemEntity.getItem().getCount();
                            int neededAmount = 100 - mineralAmount;
                            int amountToConsume = stackSize <= neededAmount ? stackSize : neededAmount;

                            TDHelpers.changeMineralContent(serverLevel, state, pos, amountToConsume);
                            itemEntity.getItem().shrink(amountToConsume);
                        }
                        serverLevel.setBlockAndUpdate(pos, state.setValue(NATURAL, true));
                    }
                    itemEntity.setInvulnerable(false);
                }
            }
            else if (entity instanceof ThrowableItemProjectile throwableObject)
            {
                if (level.isEmptyBlock(pos.above()))
                {
                    RandomSource random = level.getRandom();
                    for (int j = 0; j < 6 + Math.round(state.getValue(MINERAL_AMOUNT) * 0.25F); j++)
                    {
                        double d0 = pos.getX() + 0.5D + (random.nextGaussian() * 0.15D);
                        double d1 = pos.getY() + 1.0D;
                        double d2 = pos.getZ() + 0.5D + (random.nextGaussian() * 0.15D);
                        ParticleOptions particle = random.nextInt() == 0 ? ParticleTypes.BUBBLE_POP : ParticleTypes.SPLASH;
                        level.addAlwaysVisibleParticle(particle, d0, d1, d2, 0.0D, 0.01D + random.nextDouble() * 0.05D, 0.0D);
                    }
                    level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.3F + random.nextFloat() * 0.3F, 0.6F + random.nextFloat() * 0.2F, false);
                }
                if (Helpers.isItem(throwableObject.getItem(), TDTags.Items.REPLENISHES_LAVA) && level instanceof ServerLevel serverLevel)
                {
                    int mineralAmount = state.getValue(MINERAL_AMOUNT);
                    if (mineralAmount < 100)
                    {
                        int neededAmount = 100 - mineralAmount;
                        int amountToConsume = 1 <= neededAmount ? 1 : neededAmount;

                        TDHelpers.changeMineralContent(serverLevel, state, pos, amountToConsume);
                        throwableObject.getItem().shrink(amountToConsume);
                    }
                    serverLevel.setBlockAndUpdate(pos, state.setValue(NATURAL, true));
                }
            }
        }
    }
}
