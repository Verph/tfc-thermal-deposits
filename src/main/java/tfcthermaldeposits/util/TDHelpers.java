package tfcthermaldeposits.util;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockDisplayCategory;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.settings.RockSettings;

import tfcthermaldeposits.common.blocks.TDBlockStateProperties;
import tfcthermaldeposits.common.blocks.rock.Mineral;
import tfcthermaldeposits.config.TDConfig;

import static tfcthermaldeposits.TFCThermalDeposits.MOD_ID;
import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

public class TDHelpers
{
    public static final IntegerProperty MINERAL_AMOUNT = TDBlockStateProperties.MINERAL_AMOUNT;
    public static final RegistryRock DEFAULT_ROCK = Rock.ANDESITE;

    /**
     * Default {@link ResourceLocation}, except with a TFC namespace
     */
    public static ResourceLocation identifier(String name)
    {
        return new ResourceLocation(MOD_ID, name);
    }

    public static ModelLayerLocation modelIdentifier(String name, String part)
    {
        return new ModelLayerLocation(identifier(name), part);
    }

    public static ModelLayerLocation modelIdentifier(String name)
    {
        return modelIdentifier(name, "main");
    }

    public static boolean isBiome(Biome biome, TagKey<Biome> tag)
    {
        return Helpers.checkTag(ForgeRegistries.BIOMES, biome, tag);
    }

    public static RegistryRock rockTypeIgneous(ServerLevel level, BlockPos pos)
    {
        if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
        {
            RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, pos).getRockData().getRock(pos);
            if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
            {
                return convertToIgneous(rock);
            }
            else
            {
                return convertToIgneous(rockLoopCheck(localRock));
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockTypeIgneous(LevelAccessor reader, BlockPos pos)
    {
        if (reader instanceof ServerLevel level)
        {
            if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
            {
                RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, pos).getRockData().getRock(pos);
                if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
                {
                    return convertToIgneous(rock);
                }
                else
                {
                    return convertToIgneous(rockLoopCheck(localRock));
                }
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockType(ServerLevel level, BlockPos pos)
    {
        if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
        {
            RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, pos).getRockData().getRock(pos);
            if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
            {
                return rock;
            }
            else
            {
                return rockLoopCheck(localRock);
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockType(LevelAccessor reader, BlockPos pos)
    {
        if (reader instanceof ServerLevel level)
        {
            if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
            {
                RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, pos).getRockData().getRock(pos);
                if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
                {
                    return rock;
                }
                else
                {
                    return rockLoopCheck(localRock);
                }
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockTypeMantle(ServerLevel level, BlockPos pos)
    {
        if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
        {
            int minY = level.getMinBuildHeight() + 8;
            BlockPos lowestPos = new BlockPos(pos.getX(), minY, pos.getZ());
            RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, lowestPos).getRockData().getRock(lowestPos);
            if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
            {
                return rock;
            }
            else
            {
                return rockLoopCheck(localRock);
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockTypeMantle(LevelAccessor reader, BlockPos pos)
    {
        if (reader instanceof ServerLevel level)
        {
            if (level.getChunkSource().getGenerator() instanceof TFCChunkGenerator chunkGen)
            {
                int minY = level.getMinBuildHeight() + 8;
                BlockPos lowestPos = new BlockPos(pos.getX(), minY, pos.getZ());
                RockSettings localRock = chunkGen.chunkDataProvider().get(level).get(level, lowestPos).getRockData().getRock(lowestPos);
                if (localRock.raw() instanceof RegistryRock rock && localRock.raw().equals(rock.getBlock(Rock.BlockType.RAW).get()))
                {
                    return rock;
                }
                else
                {
                    return rockLoopCheck(localRock);
                }
            }
        }
        return DEFAULT_ROCK;
    }

    public static RegistryRock rockLoopCheck(RockSettings localRock)
    {
        for (Rock rock : Rock.values())
        {
            if (localRock.raw() == TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.RAW).get())
            {
                return rock;
            }
        }
        return DEFAULT_ROCK;
    }

    public static double replenishmentFactor(Level level, BlockPos pos)
    {
        switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
        {
            case FELSIC_IGNEOUS_EXTRUSIVE:
            case FELSIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorFelsic.get();
            case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
            case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorIntermediate.get();
            case MAFIC_IGNEOUS_EXTRUSIVE:
            case MAFIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorMafic.get();
            case METAMORPHIC:
                return TDConfig.COMMON.replenishmentFactorMetamorphic.get();
            case SEDIMENTARY:
                return TDConfig.COMMON.replenishmentFactorSedimentary.get();
            default:
                return 1D;
        }
    }

    public static double replenishmentFactor(ServerLevel level, BlockPos pos)
    {
        switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
        {
            case FELSIC_IGNEOUS_EXTRUSIVE:
            case FELSIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorFelsic.get();
            case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
            case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorIntermediate.get();
            case MAFIC_IGNEOUS_EXTRUSIVE:
            case MAFIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.replenishmentFactorMafic.get();
            case METAMORPHIC:
                return TDConfig.COMMON.replenishmentFactorMetamorphic.get();
            case SEDIMENTARY:
                return TDConfig.COMMON.replenishmentFactorSedimentary.get();
            default:
                return 1D;
        }
    }

    public static void changeMineralContent(ServerLevel level, BlockState state, BlockPos pos, int amount)
	{
        level.setBlockAndUpdate(pos, state.setValue(MINERAL_AMOUNT, Mth.clamp(state.getValue(MINERAL_AMOUNT) + amount, 0, 100)));
	}

    public static Noise2D wideNise(long seed, int minHeight, int maxHeight)
    {
        return new OpenSimplex2D(seed).octaves(2).spread(0.05f).scaled(SEA_LEVEL_Y + minHeight, SEA_LEVEL_Y + maxHeight);
    }

    public static Mineral waterMineral(ServerLevel level, BlockPos pos, RandomSource random, RockDisplayCategory category)
	{
        if (wideNise(level.getSeed() + 10, 63, 140).noise(pos.getX(), pos.getZ()) > 100D)
        {
            switch (category)
            {
                case MAFIC_IGNEOUS_INTRUSIVE:
                case MAFIC_IGNEOUS_EXTRUSIVE:
                    return Mineral.SALMIAK;
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                    return Mineral.SALTPETER;
                case FELSIC_IGNEOUS_INTRUSIVE:
                case FELSIC_IGNEOUS_EXTRUSIVE:
                    return Mineral.CALCITE;
                default:
                    return Mineral.SALT;
            }
        }
        return null;
	}

    public static Mineral lavaMineral(ServerLevel level, BlockPos pos, RandomSource random, RockDisplayCategory category)
	{
        double variantNoiseValue = wideNise(level.getSeed() + 10, 63, 140).noise(pos.getX(), pos.getZ());

        if (variantNoiseValue >= 110D)
        {
            switch (category)
            {
                case FELSIC_IGNEOUS_INTRUSIVE:
                case FELSIC_IGNEOUS_EXTRUSIVE:
                    return Mineral.SMITHSONITE;
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                    return Mineral.GREIGITE;
                case MAFIC_IGNEOUS_INTRUSIVE:
                case MAFIC_IGNEOUS_EXTRUSIVE:
                    return Mineral.MAGNESITE;
                case METAMORPHIC:
                    return Mineral.BASTNASITE;
                default:
                    return Mineral.BRIMSTONE;
            }
        }
        else if (variantNoiseValue <= 90D)
        {
            switch (category)
            {
                case FELSIC_IGNEOUS_INTRUSIVE:
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                    return Mineral.ALABANDITE;
                case MAFIC_IGNEOUS_INTRUSIVE:
                case METAMORPHIC:
                    return Mineral.GREIGITE;
                case MAFIC_IGNEOUS_EXTRUSIVE:
                    return Mineral.ZABUYELITE;
                case FELSIC_IGNEOUS_EXTRUSIVE:
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                    return Mineral.SPHEROCOBALTITE;
                default:
                    return Mineral.BRIMSTONE;
            }
        }
        else
        {
            switch (category)
            {
                case MAFIC_IGNEOUS_INTRUSIVE:
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                case FELSIC_IGNEOUS_INTRUSIVE:
                    return Mineral.APATITE;
                default:
                    return Mineral.BRIMSTONE;
            }
        }
	}

    public static void spawnSmoke(Level level, BlockPos pos, FluidState state, RandomSource random, RockDisplayCategory category)
    {
        int smokeLevel = 3;
        switch (category)
        {
            case FELSIC_IGNEOUS_INTRUSIVE:
            case FELSIC_IGNEOUS_EXTRUSIVE:
                smokeLevel = 4;
            case INTERMEDIATE_IGNEOUS_INTRUSIVE:
            case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                smokeLevel = 3;
            case MAFIC_IGNEOUS_INTRUSIVE:
            case MAFIC_IGNEOUS_EXTRUSIVE:
                smokeLevel = 0;
            default:
                smokeLevel = 2;
        }

        final double x = pos.getX() + 0.5D;
        final double y = pos.getY() + 1.0D;
        final double z = pos.getZ() + 0.5D;
        final int smoke = level.canSeeSky(pos) ? smokeLevel + 1 : smokeLevel + 0;

        level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);

        for (int i = 0; i < 1 + random.nextInt(3); i++)
        {
            level.addAlwaysVisibleParticle(TFCParticles.SMOKES.get(Mth.clamp(smoke, 0, 4)).get(), x + Helpers.triangle(random) * 0.5f, y + random.nextDouble(), z + Helpers.triangle(random) * 0.5f, 0, 0.07D, 0);
        }
        for (int i = 0; i < random.nextInt(4 + smoke); i++)
        {
            level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, x + Helpers.triangle(random) * 0.5f, y + random.nextDouble(), z + Helpers.triangle(random) * 0.5f, 0, 0.005D, 0);
        }
        if (random.nextInt(6 - smoke) <= 1)
        {
            level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, x + Helpers.triangle(random) * 0.5f, y + random.nextDouble(), z + Helpers.triangle(random) * 0.5f, 0, 0.005D, 0);
        }
    }

    public static int getSlopeFindDistance(LevelAccessor level, BlockPos pos)
    {
        if (!level.dimensionType().ultraWarm() && pos != null)
        {
            switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
            {
                case FELSIC_IGNEOUS_EXTRUSIVE:
                case FELSIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.slopeDistanceFelsic.get();
                case METAMORPHIC:
                    return TDConfig.COMMON.slopeDistanceMetamorphic.get();
                case SEDIMENTARY:
                    return TDConfig.COMMON.slopeDistanceSedimentary.get();
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.slopeDistanceIntermediate.get();
                case MAFIC_IGNEOUS_EXTRUSIVE:
                case MAFIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.slopeDistanceMafic.get();
                default:
                    return 2;
            }
        }
        return level.dimensionType().ultraWarm() ? 4 : 2;
    }

    public static int getDropOff(LevelAccessor level, BlockPos pos)
    {
        if (!level.dimensionType().ultraWarm() && pos != null)
        {
            switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
            {
                case FELSIC_IGNEOUS_EXTRUSIVE:
                case FELSIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.dropOffFelsic.get();
                case METAMORPHIC:
                    return TDConfig.COMMON.dropOffMetamorphic.get();
                case SEDIMENTARY:
                    return TDConfig.COMMON.dropOffSedimentary.get();
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.dropOffIntermediate.get();
                case MAFIC_IGNEOUS_EXTRUSIVE:
                case MAFIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.dropOffMafic.get();
                default:
                    return 2;
            }
        }
        return level.dimensionType().ultraWarm() ? 1 : 2;
    }

    public static int getTickDelay(LevelAccessor level, BlockPos pos)
    {
        if (!level.dimensionType().ultraWarm() && pos != null)
        {
            switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
            {
                case FELSIC_IGNEOUS_EXTRUSIVE:
                case FELSIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.tickDelayFelsic.get();
                case METAMORPHIC:
                    return TDConfig.COMMON.tickDelayMetamorphic.get();
                case SEDIMENTARY:
                    return TDConfig.COMMON.tickDelaySedimentary.get();
                case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
                case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.tickDelayIntermediate.get();
                case MAFIC_IGNEOUS_EXTRUSIVE:
                case MAFIC_IGNEOUS_INTRUSIVE:
                    return TDConfig.COMMON.tickDelayMafic.get();
                default:
                    return 30;
            }
        }
        return level.dimensionType().ultraWarm() ? 10 : 30;
    }

    public static RegistryRock convertToIgneous(RegistryRock rock)
    {
        if (rock.displayCategory() == RockDisplayCategory.SEDIMENTARY)
        {
            if (rock == Rock.SHALE || rock == Rock.CLAYSTONE)
            {
                return Rock.SLATE;
            }
            else if (rock == Rock.LIMESTONE || rock == Rock.DOLOMITE || rock == Rock.CHALK)
            {
                return Rock.MARBLE;
            }
            else if (rock == Rock.CONGLOMERATE)
            {
                return Rock.GNEISS;
            }
            else
            {
                return Rock.QUARTZITE;
            }
        }
        return rock;
    }

    public static double getIntensityFactor(LevelAccessor level, BlockPos pos)
    {
        switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
        {
            case MAFIC_IGNEOUS_EXTRUSIVE:
            case MAFIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.maficVolcanoIntensityFactor.get();
            case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
            case INTERMEDIATE_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.intermediateVolcanoIntensityFactor.get();
            case FELSIC_IGNEOUS_EXTRUSIVE:
            case FELSIC_IGNEOUS_INTRUSIVE:
                return TDConfig.COMMON.felsicVolcanoIntensityFactor.get();
            case METAMORPHIC:
                return TDConfig.COMMON.metamorphicVolcanoIntensityFactor.get();
            case SEDIMENTARY:
                return TDConfig.COMMON.sedimentaryVolcanoIntensityFactor.get();
            default:
                return 1.0D;
        }
    }

    public static double volcanicRumbleNoise(Level level, boolean frequency)
    {
        // Max is probably around 250 or higher
        ICalendar calendar = Calendars.get(level);

        float spread = frequency ? 0.00001f : 0.000001f;
        double noiseValue = new OpenSimplex2D(calendar.getTotalYears())
            .add(new OpenSimplex2D(calendar.getTotalYears() + 1L)
                .octaves(1)
                .spread(spread)
                .ridged()
                .map(x -> 2f * -(x >= 0f ? x * x * x : Math.pow(x, 50f)))
                .scaled(-1f, 0f, -1f, 2f)
                .terraces(15)
                .scaled(SEA_LEVEL_Y, -SEA_LEVEL_Y)
            )
            .map(x -> x < SEA_LEVEL_Y ? SEA_LEVEL_Y - 0.3f * (SEA_LEVEL_Y - x) : x)
            .noise(calendar.getCalendarDayOfMonth(), calendar.getCalendarTicks());
        return convertExponential(noiseValue, frequency);
    }

    public static double convertExponential(double value, boolean frequency)
    {
        if (frequency)
        {
            return Mth.clamp((float) (Math.pow(1.022f, value + 4f) - 3f), 0f, 250f);
        }
        else
        {
            return Mth.clamp((float) (Math.pow(1.2f, value - 220f) - 0.25f), 0f, 250f);
        }
    }

    public static RandomSource seededRandom(Level level)
    {
        return RandomSource.create(Calendars.get(level).getCalendarDayTime());
    }

    public static RandomSource seededRandom(BlockPos pos)
    {
        return RandomSource.create(pos.getX() * pos.getZ());
    }

    public static boolean isIgneous(LevelAccessor level, BlockPos pos)
    {
        switch (TDHelpers.rockTypeMantle(level, pos).displayCategory())
        {
            case MAFIC_IGNEOUS_EXTRUSIVE:
            case MAFIC_IGNEOUS_INTRUSIVE:
            case INTERMEDIATE_IGNEOUS_EXTRUSIVE:
            case INTERMEDIATE_IGNEOUS_INTRUSIVE:
            case FELSIC_IGNEOUS_EXTRUSIVE:
            case FELSIC_IGNEOUS_INTRUSIVE:
                return true;
            default:
                return false;
        }
    }
}
