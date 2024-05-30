package tfcthermaldeposits.common;

import java.util.function.Supplier;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.dries007.tfc.util.SelfTests;

import tfcthermaldeposits.common.blocks.rock.Mineral;
import tfcthermaldeposits.common.items.TDItems;

@SuppressWarnings("unused")
public class TDCreativeTabs
{
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent out)
    {
        if (out.getTab() == TFCCreativeTabs.ORES.tab().get())
        {
            for (Mineral mineral : Mineral.values())
            {
                accept(out, TDItems.MINERALS.get(mineral));
                if (!mineral.hasTFCEquivalent())
                {
                    accept(out, TDItems.MINERAL_POWDER.get(mineral));
                }
            }
        }
    }

    private static void accept(CreativeModeTab.Output out, DecorationBlockRegistryObject decoration)
    {
        out.accept(decoration.stair().get());
        out.accept(decoration.slab().get());
        out.accept(decoration.wall().get());
    }

    public static <T extends ItemLike, R extends Supplier<T>> void accept(CreativeModeTab.Output out, R reg)
    {
        if (reg.get().asItem() == Items.AIR)
        {
            SelfTests.reportExternalError();
            return;
        }
        out.accept(reg.get());
    }
}
