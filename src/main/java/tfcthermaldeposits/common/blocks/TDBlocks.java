package tfcthermaldeposits.common.blocks;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GravelBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.OreDeposit;
import net.dries007.tfc.common.blocks.OreDepositBlock;
import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.TFCMagmaBlock;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.registry.RegistrationHelpers;

import tfcthermaldeposits.common.blockentities.TDBlockEntities;
import tfcthermaldeposits.common.blocks.rock.MineralSheetBlock;
import tfcthermaldeposits.common.items.TDItems;

import static tfcthermaldeposits.TFCThermalDeposits.MOD_ID;

@SuppressWarnings("unused")
public final class TDBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);

    public static final RegistryObject<Block> MINERAL_SHEET = register("mineral/mineral_sheet", () -> new MineralSheetBlock(ExtendedProperties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BANJO).strength(0.5F, 4F).sound(SoundType.POINTED_DRIPSTONE).noCollission().noOcclusion().blockEntity(TDBlockEntities.MINERAL_SHEET)));
    public static final RegistryObject<Block> PYROCLASTIC_BOMB = register("pyroclastic_bomb", () -> new Block(Properties.of().mapColor(MapColor.NETHER).strength(3.0F).sound(TFCSounds.CHARCOAL).instrument(NoteBlockInstrument.BANJO)));

    public static boolean always(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    public static boolean never(BlockState state, BlockGetter level, BlockPos pos)
    {
        return false;
    }

    public static boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type)
    {
        return false;
    }

    public static ToIntFunction<BlockState> lavaLoggedBlockEmission()
    {
        // This is resolved only at registration time, so we can't use the fast check (.getFluid() == Fluids.LAVA) and we have to use the slow check instead
        return state -> state.getValue(TFCBlockStateProperties.WATER_AND_LAVA).is(((IFluidLoggable) state.getBlock()).getFluidProperty().keyFor(Fluids.LAVA)) ? 15 : 0;
    }

    public static ToIntFunction<BlockState> litBlockEmission(int lightValue)
    {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> blockSupplier)
    {
        return register(name, blockSupplier, (Function<T, ? extends BlockItem>) null);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        return register(name, blockSupplier, block -> new BlockItem(block, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, Item.Properties blockItemProperties)
    {
        return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        return RegistrationHelpers.registerBlock(TDBlocks.BLOCKS, TDItems.ITEMS, name, blockSupplier, blockItemFactory);
    }
}
