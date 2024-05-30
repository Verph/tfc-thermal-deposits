package tfcthermaldeposits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidStack;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockDisplayCategory;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.items.FluidContainerItem;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.biome.VolcanoNoise;

import tfcthermaldeposits.client.particle.TDParticles;
import tfcthermaldeposits.common.TDTags;
import tfcthermaldeposits.common.blocks.InverseBooleanProperty;
import tfcthermaldeposits.common.blocks.TDBlockStateProperties;
import tfcthermaldeposits.common.blocks.TDBlocks;
import tfcthermaldeposits.common.blocks.rock.Mineral;
import tfcthermaldeposits.common.blocks.rock.MineralSheetBlock;
import tfcthermaldeposits.common.entities.PyroclasticBomb;
import tfcthermaldeposits.common.items.TDItems;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin extends Block
{
    @Unique private static final IntegerProperty MINERAL_AMOUNT = TDBlockStateProperties.MINERAL_AMOUNT;
    @Unique private static final BooleanProperty NATURAL = TDBlockStateProperties.NATURAL;
    @Unique private static final InverseBooleanProperty NATURAL_INVERSE = TDBlockStateProperties.NATURAL_INVERSE;

    public LiquidBlockMixin(FlowingFluid fluid, Properties properties)
    {
        super(properties);
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

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    protected void inject$createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci)
    {
        builder.add(NATURAL, MINERAL_AMOUNT);
    }

    @Inject(method = "updateShape", at = @At("TAIL"))
    public void inject$updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir)
    {
        FluidState fluidState = level.getFluidState(currentPos);
        if (fluidState.hasProperty(NATURAL_INVERSE) && fluidState.hasProperty(MINERAL_AMOUNT))
        {
            if (fluidState.getValue(NATURAL_INVERSE))
            {
                state.setValue(NATURAL, true);
            }
            if (fluidState.getValue(MINERAL_AMOUNT) > 0)
            {
                state.setValue(MINERAL_AMOUNT, fluidState.getValue(MINERAL_AMOUNT));
            }
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    public void inject$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci)
    {
        if (level.getFluidState(pos).hasProperty(NATURAL_INVERSE))
        {
            if (level.getFluidState(pos).getValue(NATURAL_INVERSE))
            {
                level.setBlockAndUpdate(pos, state.setValue(NATURAL, true));
            }
        }

        boolean validDimension = level.dimension() == Level.OVERWORLD || TDConfig.COMMON.toggleOtherDimensions.get();
        FluidState fluidState = level.getFluidState(pos);

        boolean hasProperties = state.hasProperty(NATURAL) && state.hasProperty(MINERAL_AMOUNT);
        if (hasProperties)
        {
            double timeNoise = TDHelpers.volcanicRumbleNoise(level, level.dimension() == Level.NETHER ? true : TDConfig.COMMON.highTremorFrequency.get());
            boolean isNatural = (hasProperties && state.getValue(NATURAL)) || TDConfig.COMMON.toggleDepositsFromAllLavaOrSpringWater.get();
            boolean isLava = fluidState.getType() == Fluids.LAVA.getSource() || state.getBlock() == Blocks.LAVA;

            if (validDimension && isNatural && isLava && fluidState.isSource())
            {
                RockDisplayCategory category = TDHelpers.rockTypeMantle(level, pos).displayCategory();
                if (TDConfig.COMMON.shouldLavaDepositMinerals.get() && state.getValue(MINERAL_AMOUNT) > 0)
                {
                    if (random.nextInt(TDConfig.COMMON.mineralGenFrequencyLava.get()) == 0)
                    {
                        Mineral mineral = TDHelpers.lavaMineral(level, pos, random, category);

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
                if (TDConfig.COMMON.naturalReplenishment.get() && state.getValue(MINERAL_AMOUNT) < 100 && random.nextInt((int) Math.ceil((Mth.abs(TDConfig.COMMON.mineralGenFrequencyLava.get() * 6) * TDHelpers.replenishmentFactor(level, pos)))) == 0)
                {
                    TDHelpers.changeMineralContent(level, state, pos, level.getRandom().nextInt(3));
                }

                // Launch pyroclastic bomb(s)!
                RandomSource seededRandom = TDHelpers.seededRandom(pos); // To synchronize with other volcano stuff
                double rockTypeFactor = TDHelpers.getIntensityFactor(level, pos);
                Level.ExplosionInteraction shouldDestroy = TDConfig.COMMON.togglePyroclasticBombsExplode.get() ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;

                if (TDConfig.COMMON.togglePyroclasticBombs.get() && timeNoise >= 155D + (seededRandom.nextInt(8) * rockTypeFactor))
                {
                    double volcanoNoise = new VolcanoNoise(level.getSeed()).calculateEasing(pos.getX(), pos.getZ(), TFCBiomes.getExtension(level, level.getBiome(pos).value()).getVolcanoRarity());

                    if (volcanoNoise > 0.9D)
                    {
                        boolean acceptableLocation = false;
                        boolean primaryBombSpawned = false;
                        boolean extraBombsSpawned = false;
                        double extraFactor = TDHelpers.replenishmentFactor(level, pos) * 3.5f;

                        for (int i = 0; i < 1 + random.nextInt(8) * extraFactor; i++)
                        {
                            if (seededRandom.nextDouble() * (timeNoise * 0.015f) * extraFactor >= rockTypeFactor) // Mafic => few, felsic/sedimentary => more
                            {
                                for (Direction direction : Direction.Plane.HORIZONTAL)
                                {
                                    for (int distance = 0; distance <= 2; distance++)
                                    {
                                        acceptableLocation = (level.getFluidState(pos.relative(direction, distance)).getType() instanceof LavaFluid || level.getBlockState(pos.relative(direction, distance)).getBlock() == Blocks.LAVA) && level.isEmptyBlock(pos.relative(direction, distance).above());
                                    }
                                }
                                if (acceptableLocation)
                                {
                                    RegistryRock scoria = TDHelpers.rockTypeMantle(level, pos);
                                    if (!primaryBombSpawned && pos.getY() >= level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() - 16)
                                    {
                                        PyroclasticBomb primaryBomb = new PyroclasticBomb(level, pos.above().getX() + 0.5D, pos.above().getY(), pos.above().getZ() + 0.5D, (LivingEntity) null, ParticleTypes.LARGE_SMOKE, ParticleTypes.EXPLOSION_EMITTER, TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), shouldDestroy, true, true, false, scoria);
                                        primaryBomb.setFuse(3000);
                                        level.addFreshEntity(primaryBomb);
                                        level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.AMBIENT, 1F + random.nextFloat(), 0.8F + random.nextFloat() * 0.3F, true);
                                        level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 1F + random.nextFloat(), 1F + random.nextFloat() * 0.3F, true);
                                        primaryBombSpawned = true;
                                    }
                                    if (!extraBombsSpawned)
                                    {
                                        for (int j = 0; j < 9 + random.nextInt(16); j++)
                                        {
                                            double x = pos.getX() - 0.5D + random.nextGaussian() * 2.0D;
                                            double y = pos.getY() - 0.9D + random.nextGaussian() * 0.2D;
                                            double z = pos.getZ() - 0.5D + random.nextGaussian() * 2.0D;
                                            PyroclasticBomb extraBombs = new PyroclasticBomb(level, x, y, z, (LivingEntity) null, ParticleTypes.LARGE_SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), shouldDestroy, true, false, false, scoria);
                                            extraBombs.setFuse(0);
                                            level.addFreshEntity(extraBombs);
                                            extraBombsSpawned = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (level.dimension() == Level.NETHER && isLava && fluidState.isSource() && TDConfig.COMMON.togglePyroclasticBombs.get() && TDConfig.COMMON.toggleNetherTremor.get())
            {
                Level.ExplosionInteraction shouldDestroy = TDConfig.COMMON.togglePyroclasticBombsExplode.get() ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;

                RandomSource seededRandom = TDHelpers.seededRandom(pos);
                if (timeNoise >= 170D + seededRandom.nextInt(50) && TDHelpers.wideNise(Calendars.get(level).getTotalYears(), 63, 160).noise(pos.getX(), pos.getZ()) > 120D)
                {
                    boolean acceptableLocation = false;
                    boolean primaryBombSpawned = false;
                    boolean extraBombsSpawned = false;

                    if (seededRandom.nextInt(400) == 0)
                    {
                        for (int i = 0; i < 1 + seededRandom.nextInt(3); i++)
                        {
                            if (seededRandom.nextInt(500) <= timeNoise * 0.5f)
                            {
                                for (Direction direction : Direction.Plane.HORIZONTAL)
                                {
                                    for (int distance = 0; distance <= 6; distance++)
                                    {
                                        acceptableLocation = (level.getFluidState(pos.relative(direction, distance)).getType() instanceof LavaFluid || level.getBlockState(pos.relative(direction, distance)).getBlock() == Blocks.LAVA) && level.isEmptyBlock(pos.relative(direction, distance).above());
                                    }
                                }
                                if (acceptableLocation)
                                {
                                    if (!primaryBombSpawned)
                                    {
                                        PyroclasticBomb primaryBomb = new PyroclasticBomb(level, pos.above().getX() + 0.5D, pos.above().getY(), pos.above().getZ() + 0.5D, (LivingEntity) null, ParticleTypes.LARGE_SMOKE, ParticleTypes.EXPLOSION_EMITTER, TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), shouldDestroy, true, true, false, Rock.BASALT);
                                        primaryBomb.setFuse(3000);
                                        level.addFreshEntity(primaryBomb);
                                        level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.AMBIENT, 1F + random.nextFloat(), 0.8F + random.nextFloat() * 0.3F, true);
                                        level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 1F + random.nextFloat(), 1F + random.nextFloat() * 0.3F, true);
                                        primaryBombSpawned = true;
                                    }
                                    if (!extraBombsSpawned)
                                    {
                                        for (int j = 0; j < 9 + seededRandom.nextInt(16); j++)
                                        {
                                            double x = pos.getX() - 0.5D + random.nextGaussian() * 2.0D;
                                            double y = pos.getY() - 0.9D + random.nextGaussian() * 0.2D;
                                            double z = pos.getZ() - 0.5D + random.nextGaussian() * 2.0D;
                                            PyroclasticBomb extraBombs = new PyroclasticBomb(level, x, y, z, (LivingEntity) null, ParticleTypes.LARGE_SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), shouldDestroy, true, false, false, Rock.BASALT);
                                            extraBombs.setFuse(0);
                                            level.addFreshEntity(extraBombs);
                                            extraBombsSpawned = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        super.entityInside(state, level, pos, entity);

        boolean isLava = level.getFluidState(pos).getType() == Fluids.LAVA.getSource() || state.getBlock() == Blocks.LAVA;
        if (isLava && state.hasProperty(MINERAL_AMOUNT) && state.hasProperty(NATURAL))
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
                            level.addAlwaysVisibleParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.001D + random.nextDouble() * 0.001D, 0.0D);
                        }
                        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.6F + random.nextFloat() * 0.2F, false);
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
                if (TDConfig.COMMON.togglePyroclasticBombs.get())
                {
                    if (itemEntity.getItem().getItem() instanceof FluidContainerItem)
                    {
                        itemEntity.getCapability(Capabilities.FLUID_ITEM).ifPresent((cap) -> {
                            FluidStack fluid = cap.getFluidInTank(cap.getTanks());
                            if (!fluid.isEmpty() && (Helpers.isFluid(fluid.getFluid(), TFCTags.Fluids.ANY_WATER) || Helpers.isFluid(fluid.getFluid(), TFCTags.Fluids.WATER_LIKE)))
                            {
                                spawnExtraBombs(level, pos);
                                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
                                level.addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), pos.getX(), pos.getY(), pos.getZ(), 1.0D, 0.0D, 0.0D);
                            }
                        });
                    }
                    else if (Helpers.isItem(itemEntity.getItem(), TDTags.Items.WET))
                    {
                        spawnExtraBombs(level, pos);
                        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
                        level.addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), pos.getX(), pos.getY(), pos.getZ(), 1.0D, 0.0D, 0.0D);
                    }
                }
            }
            else if (entity instanceof ThrowableItemProjectile throwableObject)
            {
                if (TDConfig.COMMON.togglePyroclasticBombs.get() && level.isEmptyBlock(pos.above()) && throwableObject instanceof Snowball && level.getRandom().nextInt(6) == 0)
                {
                    spawnExtraBombs(level, pos);
                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
                    level.addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), pos.getX(), pos.getY(), pos.getZ(), 1.0D, 0.0D, 0.0D);
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
                else
                {
                    if (level.isEmptyBlock(pos.above()))
                    {
                        RandomSource random = level.getRandom();
                        for (int j = 0; j < 6 + Math.round(state.getValue(MINERAL_AMOUNT) * 0.25F); j++)
                        {
                            double d0 = pos.getX() + 0.5D + (random.nextGaussian() * 0.15D);
                            double d1 = pos.getY() + 1.0D;
                            double d2 = pos.getZ() + 0.5D + (random.nextGaussian() * 0.15D);
                            level.addAlwaysVisibleParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.001D + random.nextDouble() * 0.001D, 0.0D);
                        }
                        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.3F + random.nextFloat() * 0.3F, 0.6F + random.nextFloat() * 0.2F, false);
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

    @Unique
    private void spawnExtraBombs(Level level, BlockPos pos)
    {
        RandomSource random = level.getRandom();
        boolean primaryBombSpawned = false;
        boolean extraBombsSpawned = false;

        for (int i = 0; i < 1 + random.nextInt(3); i++)
        {
            RegistryRock scoria = level.dimension() == Level.NETHER ? Rock.BASALT : TDHelpers.rockTypeMantle(level, pos);
            boolean heightPrerequisite = level.dimension() == Level.NETHER ? true : pos.getY() >= level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() - 16;
            if (!primaryBombSpawned && heightPrerequisite)
            {
                PyroclasticBomb primaryBomb = new PyroclasticBomb(level, (double) (pos.above().getX() + 0.5D), (double) pos.above().getY(), (double) (pos.above().getZ() + 0.5D), (LivingEntity) null, ParticleTypes.LARGE_SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), Level.ExplosionInteraction.NONE, true, true, false, scoria);
                primaryBomb.setFuse(3000);
                level.addFreshEntity(primaryBomb);
                level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.AMBIENT, 1F + random.nextFloat(), 0.8F + random.nextFloat() * 0.3F, true);
                level.playLocalSound(primaryBomb.getX(), primaryBomb.getY(), primaryBomb.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 1F + random.nextFloat(), 1F + random.nextFloat() * 0.3F, true);
                primaryBombSpawned = true;
            }
            if (!extraBombsSpawned)
            {
                for (int j = 0; j < 3 + random.nextInt(6); j++)
                {
                    double x = pos.getX() - 0.5D + random.nextGaussian() * 2.0D;
                    double y = pos.getY() - 0.9D + random.nextGaussian() * 0.2D;
                    double z = pos.getZ() - 0.5D + random.nextGaussian() * 2.0D;
                    PyroclasticBomb extraBombs = new PyroclasticBomb(level, x, y, z, (LivingEntity) null, ParticleTypes.LARGE_SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), TDConfig.COMMON.pyroclasticBombExplosionRadius.get(), Level.ExplosionInteraction.NONE, true, false, false, scoria);
                    extraBombs.setFuse(0);
                    level.addFreshEntity(extraBombs);
                    extraBombsSpawned = true;
                }
            }
        }
    }
}
