package tfcthermaldeposits.config;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import net.dries007.tfc.config.ConfigBuilder;
import net.dries007.tfc.util.Helpers;

public class TDConfig
{
    public static final TDCommonConfig COMMON = register(ModConfig.Type.COMMON, TDCommonConfig::new, "common").getKey();

    public static void init() {}

    private static <C> Pair<C, ForgeConfigSpec> register(ModConfig.Type type, Function<ConfigBuilder, C> factory, String prefix)
    {
        final Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> factory.apply(new ConfigBuilder(builder, prefix)));
        if (!Helpers.BOOTSTRAP_ENVIRONMENT) ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair;
    }
}
