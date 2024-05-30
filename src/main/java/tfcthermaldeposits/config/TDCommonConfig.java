package tfcthermaldeposits.config;

import net.minecraftforge.common.ForgeConfigSpec;

import net.dries007.tfc.config.ConfigBuilder;

public class TDCommonConfig
{
    // General
    public final ForgeConfigSpec.BooleanValue shouldLavaDepositMinerals;
    public final ForgeConfigSpec.BooleanValue shouldHotSpringsDepositMinerals;
    public final ForgeConfigSpec.IntValue mineralGenFrequencyLava;
    public final ForgeConfigSpec.IntValue mineralGenFrequencyHotSpring;
    public final ForgeConfigSpec.BooleanValue shouldLavaSmoke;
    public final ForgeConfigSpec.BooleanValue toggleOtherDimensions;
    public final ForgeConfigSpec.BooleanValue naturalReplenishment;
    public final ForgeConfigSpec.BooleanValue toggleDepositsFromAllLavaOrSpringWater;
    public final ForgeConfigSpec.BooleanValue togglePyroclasticBombs;
    public final ForgeConfigSpec.BooleanValue togglePyroclasticBombsExplode;
    public final ForgeConfigSpec.DoubleValue pyroclasticBombExplosionRadius;
    public final ForgeConfigSpec.BooleanValue togglePyroclasticBombScoria;
    public final ForgeConfigSpec.BooleanValue togglePyroclasticBombOre;
    public final ForgeConfigSpec.IntValue pyroclasticBombOreChance;
    public final ForgeConfigSpec.BooleanValue toggleVolcanoShake;
    public final ForgeConfigSpec.BooleanValue toggleNetherTremor;
    public final ForgeConfigSpec.BooleanValue highTremorFrequency;
    public final ForgeConfigSpec.DoubleValue earthquakeSoundAmplifier;

    public final ForgeConfigSpec.DoubleValue replenishmentFactorMafic;
    public final ForgeConfigSpec.DoubleValue replenishmentFactorIntermediate;
    public final ForgeConfigSpec.DoubleValue replenishmentFactorFelsic;
    public final ForgeConfigSpec.DoubleValue replenishmentFactorMetamorphic;
    public final ForgeConfigSpec.DoubleValue replenishmentFactorSedimentary;

    public final ForgeConfigSpec.DoubleValue maficVolcanoIntensityFactor;
    public final ForgeConfigSpec.DoubleValue intermediateVolcanoIntensityFactor;
    public final ForgeConfigSpec.DoubleValue felsicVolcanoIntensityFactor;
    public final ForgeConfigSpec.DoubleValue metamorphicVolcanoIntensityFactor;
    public final ForgeConfigSpec.DoubleValue sedimentaryVolcanoIntensityFactor;

    public final ForgeConfigSpec.IntValue slopeDistanceFelsic;
    public final ForgeConfigSpec.IntValue slopeDistanceSedimentary;
    public final ForgeConfigSpec.IntValue slopeDistanceMetamorphic;
    public final ForgeConfigSpec.IntValue slopeDistanceIntermediate;
    public final ForgeConfigSpec.IntValue slopeDistanceMafic;

    public final ForgeConfigSpec.IntValue dropOffFelsic;
    public final ForgeConfigSpec.IntValue dropOffSedimentary;
    public final ForgeConfigSpec.IntValue dropOffMetamorphic;
    public final ForgeConfigSpec.IntValue dropOffIntermediate;
    public final ForgeConfigSpec.IntValue dropOffMafic;

    public final ForgeConfigSpec.IntValue tickDelayFelsic;
    public final ForgeConfigSpec.IntValue tickDelaySedimentary;
    public final ForgeConfigSpec.IntValue tickDelayMetamorphic;
    public final ForgeConfigSpec.IntValue tickDelayIntermediate;
    public final ForgeConfigSpec.IntValue tickDelayMafic;

    public final ForgeConfigSpec.BooleanValue toggleDebug;
    public final ForgeConfigSpec.BooleanValue toggleFiniteWater;

    TDCommonConfig(ConfigBuilder builder)
    {
        builder.push("General");
        shouldLavaDepositMinerals = builder.comment("Should lava deposit minerals onto the surrounding blocks?").define("shouldLavaDepositMinerals", true);
        shouldHotSpringsDepositMinerals = builder.comment("Should hot springs deposit minerals onto the surrounding blocks?").define("shouldHotSpringsDepositMinerals", true);
        mineralGenFrequencyLava = builder.comment("How often minerals can generate around lava. This also affects natural replenishment. Lower value => more often.").define("mineralGenFrequencyLava", 128, 0, Integer.MAX_VALUE);
        mineralGenFrequencyHotSpring = builder.comment("How often minerals can generate around hot springs. This also affects natural replenishment. Lower value => more often.").define("mineralGenFrequencyHotSpring", 64, 0, Integer.MAX_VALUE);
        shouldLavaSmoke = builder.comment("Toggle smoke particles released by lava? True = smokes.").define("shouldLavaSmoke", true);
        toggleOtherDimensions = builder.comment("Toggle whether or not lava and hot springs should deposit minerals in other dimensions than the overworld? True = not limited to the overworld.").define("toggleOtherDimensions", false);
        naturalReplenishment = builder.comment("Toggle whether or not lava and hot spring water should replenish their mineral \"storage\" naturally. True = replenishes.").define("naturalReplenishment", true);
        toggleDepositsFromAllLavaOrSpringWater = builder.comment("Toggle whether or not lava and hot spring water should deposit minerals, regardless of them being natural or placed. True = all generated or placed deposits minerals.").define("toggleDepositsFromAllLavaOrSpringWater", false);
        earthquakeSoundAmplifier = builder.comment("Sound amplifier level for earthquakes and tremors. Default = 4.0.").define("earthquakeSoundAmplifier", 4.0D, 0, Double.MAX_VALUE);

        togglePyroclasticBombs = builder.comment("Should volcanoes launch pyroclastic bombs?").define("togglePyroclasticBombs", true);
        togglePyroclasticBombsExplode = builder.comment("Should pyroclastic bombs destroy blocks?").define("togglePyroclasticBombsExplode", false);
        pyroclasticBombExplosionRadius = builder.comment("How big should the explosion radius of pyroclastic bombs be? Default = 4.0.").define("pyroclasticBombExplosionRadius", 4.0D, 0, Double.MAX_VALUE);
        togglePyroclasticBombScoria = builder.comment("Should pyroclastic bombs generate scoria around its zone of impact?").define("togglePyroclasticBombScoria", true);
        togglePyroclasticBombOre = builder.comment("Should pyroclastic bombs have a chance of spawning with ore?").define("togglePyroclasticBombOre", true);
        pyroclasticBombOreChance = builder.comment("How big of a chance should pyroclastic bombs have to contain a random ore? Higher value => lower chance.").define("pyroclasticBombOreChance", 30, 1, Integer.MAX_VALUE);
        toggleVolcanoShake = builder.comment("Should volcanos shake the camera during high activity events?").define("toggleVolcanoShake", true);
        toggleNetherTremor = builder.comment("Should Nether have the occasional tremors and earthquakes?").define("toggleNetherTremor", true);
        highTremorFrequency = builder.comment("Should tremors happen more frequent? True => more frequent.").define("highTremorFrequency", false);

        replenishmentFactorMafic = builder.comment("How fast shall minerals replenish for the lava or hot spring water in mafic rock environment? Lower = faster.").define("replenishmentFactorMafic", 0.25D, Double.MIN_VALUE, Double.MAX_VALUE);
        replenishmentFactorIntermediate = builder.comment("How fast shall minerals replenish for the lava or hot spring water in intermediate rock environment? Lower = faster.").define("replenishmentFactorIntermediate", 0.42D, Double.MIN_VALUE, Double.MAX_VALUE);
        replenishmentFactorFelsic = builder.comment("How fast shall minerals replenish for the lava or hot spring water in felsic rock environment? Lower = faster.").define("replenishmentFactorFelsic", 0.66D, Double.MIN_VALUE, Double.MAX_VALUE);
        replenishmentFactorMetamorphic = builder.comment("How fast shall minerals replenish for the lava or hot spring water in metamorphic rock environment? Lower = faster.").define("replenishmentFactorMetamorphic", 0.5D, Double.MIN_VALUE, Double.MAX_VALUE);
        replenishmentFactorSedimentary = builder.comment("How fast shall minerals replenish for the lava or hot spring water in sedimentary rock environment? Lower = faster.").define("replenishmentFactorSedimentary", 1D, Double.MIN_VALUE, Double.MAX_VALUE);

        maficVolcanoIntensityFactor = builder.comment("Factor for how rare pyroclastic bombs will happen in rocks of the mafic type. Lower = greater intensity.").define("maficVolcanoIntensityFactor", 2.0D, 0D, 2000D);
        intermediateVolcanoIntensityFactor = builder.comment("Factor for how rare pyroclastic bombs will happen in rocks of the intermediate type. Lower = greater intensity.").define("intermediateVolcanoIntensityFactor", 1.5D, 0D, 2000D);
        felsicVolcanoIntensityFactor = builder.comment("Factor for how rare pyroclastic bombs will happen in rocks of the felsic type. Lower = greater intensity.").define("felsicVolcanoIntensityFactor", 0.6D, 0D, 2000D);
        metamorphicVolcanoIntensityFactor = builder.comment("Factor for how rare pyroclastic bombs will happen in rocks of the metamorphic type. Lower = greater intensity.").define("metamorphicVolcanoIntensityFactor", 1.0D, 0D, 2000D);
        sedimentaryVolcanoIntensityFactor = builder.comment("Factor for how rare pyroclastic bombs will happen in rocks of the sedimentary type. Lower = greater intensity.").define("sedimentaryVolcanoIntensityFactor", 0.9D, 0D, 2000D);

        builder.comment("Felsic rocks contain more silica compounds than mafic, thus \"felsic lava\" has a higher viscosity, flow speed and distance -- like honey.", "");
        builder.comment("Standard slope distance for lava is 2, whilst 4 in the Nether.");
        slopeDistanceFelsic = builder.comment("Slope distance for lava in felsic rocks.").define("slopeDistanceFelsic", 4, 0, Integer.MAX_VALUE);
        slopeDistanceSedimentary = builder.comment("Slope distance for lava in sedimentary rocks.").define("slopeDistanceSedimentary", 5, 0, Integer.MAX_VALUE);
        slopeDistanceMetamorphic = builder.comment("Slope distance for lava in metamorphic rocks.").define("slopeDistanceMetamorphic", 5, 0, Integer.MAX_VALUE);
        slopeDistanceIntermediate = builder.comment("Slope distance for lava in intermediate rocks.").define("slopeDistanceIntermediate", 6, 0, Integer.MAX_VALUE);
        slopeDistanceMafic = builder.comment("Slope distance for lava in mafic rocks.").define("slopeDistanceMafic", 7, 0, Integer.MAX_VALUE);

        builder.comment("Standard drop off for lava is 2, whilst 1 in the Nether.");
        dropOffFelsic = builder.comment("Drop off for lava in felsic rocks.").define("dropOffFelsic", 3, 0, Integer.MAX_VALUE);
        dropOffSedimentary = builder.comment("Drop off for lava in sedimentary rocks.").define("dropOffSedimentary", 2, 0, Integer.MAX_VALUE);
        dropOffMetamorphic = builder.comment("Drop off for lava in metamorphic rocks.").define("dropOffMetamorphic", 2, 0, Integer.MAX_VALUE);
        dropOffIntermediate = builder.comment("Drop off for lava in intermediate rocks.").define("dropOffIntermediate", 1, 0, Integer.MAX_VALUE);
        dropOffMafic = builder.comment("Drop off for lava in mafic rocks.").define("dropOffMafic", 1, 0, Integer.MAX_VALUE);

        builder.comment("Standard tick delay for lava is 30, whilst 10 in the Nether.");
        tickDelayFelsic = builder.comment("Tick delay for lava in felsic rocks.").define("tickDelayFelsic", 40, 0, Integer.MAX_VALUE);
        tickDelaySedimentary = builder.comment("Tick delay for lava in sedimentary rocks.").define("tickDelaySedimentary", 25, 0, Integer.MAX_VALUE);
        tickDelayMetamorphic = builder.comment("Tick delay for lava in metamorphic rocks.").define("tickDelayMetamorphic", 25, 0, Integer.MAX_VALUE);
        tickDelayIntermediate = builder.comment("Tick delay for lava in intermediate rocks.").define("tickDelayIntermediate", 10, 0, Integer.MAX_VALUE);
        tickDelayMafic = builder.comment("Tick delay for lava in mafic rocks.").define("tickDelayMafic", 5, 0, Integer.MAX_VALUE);

        toggleDebug = builder.comment("Enable debug messaging in the log?").define("toggleDebug", false);
        toggleFiniteWater = builder.comment("Toggle finite water for salt-, fresh-, river- and spring water? Setting this to true will prevent water from creating source blocks when spreading.").define("toggleFiniteWater", false);

        builder.pop();
    }
}
