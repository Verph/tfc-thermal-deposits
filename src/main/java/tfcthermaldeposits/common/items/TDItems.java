package tfcthermaldeposits.common.items;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.common.blocks.rock.Mineral;

import static tfcthermaldeposits.TFCThermalDeposits.MOD_ID;

@SuppressWarnings("unused")
public final class TDItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final Map<Mineral, RegistryObject<Item>> MINERALS = Helpers.mapOfKeys(Mineral.class, mineral ->
        register("mineral/" + mineral.name())
    );

    public static final Map<Mineral, RegistryObject<Item>> MINERAL_POWDER = Helpers.mapOfKeys(Mineral.class, mineral -> !mineral.hasTFCEquivalent(), mineral ->
        register("mineral/powder/" + mineral.name())
    );

    private static RegistryObject<Item> register(String name)
    {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item)
    {
        return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
    }
}
