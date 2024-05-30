package tfcthermaldeposits.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.util.TDHelpers;

public class TDTags
{
    public static class Items
    {
        public static final TagKey<Item> MINERAL = create("mineral");
        public static final TagKey<Item> REPLENISHES_HOT_SPRING_WATER = create("replenishes_hot_spring_water");
        public static final TagKey<Item> REPLENISHES_LAVA = create("replenishes_lava");
        public static final TagKey<Item> WET = create("wet");

        private static TagKey<Item> create(String id)
        {
            return TagKey.create(Registries.ITEM, TDHelpers.identifier(id));
        }
    }

    public static class Fluids
    {
        public static final TagKey<Fluid> FINITE_FLUIDS = create("finite_fluids");

        private static TagKey<Fluid> create(String id)
        {
            return TagKey.create(Registries.FLUID, TDHelpers.identifier(id));
        }
    }

    public static class TFCBiomes
    {
        public static final TagKey<Biome> IS_RIVER = create("is_river");
        public static final TagKey<Biome> IS_LAKE = create("is_lake");
        public static final TagKey<Biome> IS_VOLCANIC = create("is_volcanic");

        private static TagKey<Biome> create(String id)
        {
            return TagKey.create(Registries.BIOME, Helpers.identifier(id));
        }
    }
}
