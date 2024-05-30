package tfcthermaldeposits.common.blocks;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TDBlockStateProperties
{
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
    public static final InverseBooleanProperty NATURAL_INVERSE = InverseBooleanProperty.create("natural");
    public static final IntegerProperty MINERAL_AMOUNT = IntegerProperty.create("mineral_content", 0, 100);
    public static final BooleanProperty ITEM_INSIDE = BooleanProperty.create("item_inside");
}
