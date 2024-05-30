package tfcthermaldeposits.common.blocks.rock;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import tfcthermaldeposits.common.items.TDItems;

public enum Mineral
{
    SALMIAK, // Ammonium Chloride
    ZABUYELITE, // Lithium
    CALCITE(true), // Calcium Carbonate
    MAGNESITE, // Magnesium Carbonate
    BRIMSTONE(true), // Sulphur
    SPHEROCOBALTITE, // Cobalt Carbonate
    ALABANDITE, // Manganese Sulfide
    SMITHSONITE, // Zinc Carbonate
    GREIGITE, // Iron Sulfide
    APATITE, // Phosphate mineral
    BASTNASITE, // Fluor Carbonate
    SALTPETER(true), // Kalium Nitrate
    SALT(true);

    public final boolean hasTFCEquivalent;

    Mineral(boolean hasTFCEquivalent)
    {
        this.hasTFCEquivalent = hasTFCEquivalent;
    }

    Mineral()
    {
        this.hasTFCEquivalent = false;
    }

    public boolean hasTFCEquivalent()
    {
        return hasTFCEquivalent;
    }

    @Nullable
    public static Mineral getMineral(ItemStack stack)
    {
        if (stack != null)
        {
            for (Mineral mineral : Mineral.values())
            {
                if (stack.getItem().equals(TDItems.MINERALS.get(mineral).get()))
                {
                    return mineral;
                }
            }
        }
        return Mineral.BRIMSTONE;
    }
}
