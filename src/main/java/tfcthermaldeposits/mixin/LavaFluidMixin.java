package tfcthermaldeposits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;

import net.dries007.tfc.common.blocks.rock.RockDisplayCategory;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.biome.VolcanoNoise;

import tfcthermaldeposits.client.particle.TDParticles;
import tfcthermaldeposits.common.blocks.TDBlockStateProperties;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin extends FlowingFluid
{
    @Inject(method = "animateTick", at = @At("TAIL"))
    private void inject$animateTick(Level level, BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci)
    {
        if (TDConfig.COMMON.shouldLavaSmoke.get())
        {
            RandomSource seededRandom = TDHelpers.seededRandom(pos);
            BlockState blockState = level.getBlockState(pos);
            double timeNoise = TDHelpers.volcanicRumbleNoise(level, level.dimension() == Level.NETHER ? true : TDConfig.COMMON.highTremorFrequency.get());
            if (level.isEmptyBlock(pos.above()) && blockState.hasProperty(TDBlockStateProperties.MINERAL_AMOUNT) && (blockState.getValue(TDBlockStateProperties.MINERAL_AMOUNT) > 0 || TDConfig.COMMON.toggleDepositsFromAllLavaOrSpringWater.get()))
            {
                if (random.nextInt(Mth.clamp(TDConfig.COMMON.mineralGenFrequencyLava.get() * 2, 16, 48)) <= timeNoise * 0.05f)
                {
                    double volcanoNoise = 1;
                    RockDisplayCategory category = RockDisplayCategory.METAMORPHIC;
                    if (level instanceof ServerLevel serverLevel)
                    {
                        category = TDHelpers.rockTypeMantle(serverLevel, pos).displayCategory();
                        if (TFCBiomes.hasExtension(serverLevel, serverLevel.getBiome(pos).value()))
                        {
                            volcanoNoise = new VolcanoNoise(serverLevel.getSeed()).calculateEasing(pos.getX(), pos.getZ(), TFCBiomes.getExtension(serverLevel, serverLevel.getBiome(pos).value()).getVolcanoRarity());
                        }
                    }
                    TDHelpers.spawnSmoke(level, pos, state, random, category);

                    for (int j = 0; j < Math.floor(timeNoise * 0.1F); j++)
                    {
                        double d0 = pos.getX() + 0.5D + (random.nextGaussian() * 0.15D);
                        double d1 = pos.getY() + 1.0D;
                        double d2 = pos.getZ() + 0.5D + (random.nextGaussian() * 0.15D);
                        level.addAlwaysVisibleParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.001D + random.nextDouble() * 0.001D, 0.0D);
                    }
                    level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.6F + random.nextFloat() * 0.2F, false);

                    double rockTypeFactor = TDHelpers.getIntensityFactor(level, pos);
                    double extraFactor = TDHelpers.replenishmentFactor(level, pos) * 3.5f;

                    if (volcanoNoise > 0.9D && timeNoise >= 155D + (seededRandom.nextInt(8) * rockTypeFactor) && seededRandom.nextDouble() * (timeNoise * 0.015f) * extraFactor >= rockTypeFactor && random.nextInt(10) == 0)
                    {
                        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
                        level.addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), pos.getX(), pos.getY(), pos.getZ(), 1.0D, 0.0D, 0.0D);
                    }
                }
            }
            if (level.dimension() == Level.NETHER && state.isSource() && TDConfig.COMMON.togglePyroclasticBombs.get() && TDConfig.COMMON.toggleNetherTremor.get() && timeNoise >= 170D + seededRandom.nextInt(50) && TDHelpers.wideNise(Calendars.get(level).getTotalYears(), 63, 160).noise(pos.getX(), pos.getZ()) > 120D)
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
                                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.AMBIENT, 1F + random.nextFloat(), 0.8F + random.nextFloat() * 0.3F, true);
                                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 1F + random.nextFloat(), 1F + random.nextFloat() * 0.3F, true);
                                    primaryBombSpawned = true;
                                }
                                if (!extraBombsSpawned)
                                {
                                    for (int j = 0; j < 9 + seededRandom.nextInt(16); j++)
                                    {
                                        double x = pos.getX() - 0.5D + random.nextGaussian() * 2.0D;
                                        double y = pos.getY() - 0.9D + random.nextGaussian() * 0.2D;
                                        double z = pos.getZ() - 0.5D + random.nextGaussian() * 2.0D;
                                        level.playLocalSound(x, y, z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
                                        level.addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), x, y, z, 1.0D, 0.0D, 0.0D);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Overwrite(remap = false)
    public int getSpreadDelay(Level level, BlockPos pos, FluidState currentState, FluidState newState)
    {
        int i = TDHelpers.getTickDelay(level, pos);
        if (!currentState.isEmpty() && !newState.isEmpty() && !currentState.getValue(FALLING) && !newState.getValue(FALLING) && newState.getHeight(level, pos) > currentState.getHeight(level, pos) && level.getRandom().nextInt(4) != 0)
        {
            i *= 4;
        }
        return i;
    }
}
