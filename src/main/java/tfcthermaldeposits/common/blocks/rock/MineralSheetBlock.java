package tfcthermaldeposits.common.blocks.rock;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.common.blockentities.TDBlockEntities;

public class MineralSheetBlock extends ExtendedBlock implements EntityBlockExtension, DirectionPropertyBlock, IFluidLoggable
{
    public static final FluidProperty ALL_WATER_AND_LAVA = FluidProperty.create("fluid", Stream.of(Fluids.EMPTY, Fluids.WATER, TFCFluids.SALT_WATER, TFCFluids.SPRING_WATER, TFCFluids.RIVER_WATER, Fluids.LAVA));
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");

    public static final Map<BooleanProperty, VoxelShape> SHAPES = new ImmutableMap.Builder<BooleanProperty, VoxelShape>()
        .put(NORTH, box(0, 0, 0, 16, 16, 1))
        .put(SOUTH, box(0, 0, 15, 16, 16, 16))
        .put(EAST, box(15, 0, 0, 16, 16, 16))
        .put(WEST, box(0, 0, 0, 1, 16, 16))
        .put(UP, box(0, 15, 0, 16, 16, 16))
        .put(DOWN, box(0, 0, 0, 16, 1, 16))
        .build();

    public static final FluidProperty FLUID = ALL_WATER_AND_LAVA;
    private final Map<BlockState, VoxelShape> shapeCache;

    public MineralSheetBlock(ExtendedProperties properties)
    {
        super(properties);

        registerDefaultState(DirectionPropertyBlock.setAllDirections(getStateDefinition().any(), false)
            .setValue(FACING, Direction.NORTH)
            .setValue(MIRROR, false)
        );
        shapeCache = DirectionPropertyBlock.makeShapeCache(getStateDefinition(), SHAPES::get);
    }

    public static void removeSheet(Level level, BlockPos pos, BlockState state, Direction face, @Nullable Player player, boolean doDrops)
    {
        final BlockState newState = state.setValue(DirectionPropertyBlock.getProperty(face), false);

        level.playSound(null, pos, SoundEvents.CALCITE_BREAK, SoundSource.BLOCKS, 0.7f, 0.9f + 0.2f * level.getRandom().nextFloat());
        if (doDrops && (player == null || !player.isCreative()))
        {
            level.getBlockEntity(pos, TDBlockEntities.MINERAL_SHEET.get()).ifPresent(pile ->
            {
                final ItemStack stack = pile.removeSheet(face);
                popResourceFromFace(level, pos, face, stack);
            });
        }

        if (isEmpty(newState))
        {
            level.destroyBlock(pos, false);
        }
        else
        {
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        }
    }

    public static void addSheet(LevelAccessor level, BlockPos pos, BlockState state, Direction face, ItemStack stack)
    {
        final BlockState newState = state.setValue(DirectionPropertyBlock.getProperty(face), true);

        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        level.getBlockEntity(pos, TDBlockEntities.MINERAL_SHEET.get()).ifPresent(pile -> pile.addSheet(face, stack));

        final SoundType placementSound = state.getSoundType(level, pos, null);
        level.playSound(null, pos, state.getSoundType(level, pos, null).getPlaceSound(), SoundSource.BLOCKS, (placementSound.getVolume() + 1.0f) / 2.0f, placementSound.getPitch() * 0.8f);
    }

    /**
     * @return The targeted face, if we can find one, or the first non-empty face, if we can find one, or {@code null}, if the block is empty.
     */
    @Nullable
    public static Direction getTargetedFace(Level level, BlockState state, Player player)
    {
        final BlockHitResult result = Helpers.rayTracePlayer(level, player, ClipContext.Fluid.NONE);
        if (result.getType() == HitResult.Type.BLOCK)
        {
            final Vec3 hit = result.getLocation();
            @Nullable Direction firstDirection = null;
            for (Map.Entry<BooleanProperty, VoxelShape> entry : SHAPES.entrySet())
            {
                final BooleanProperty property = entry.getKey();
                if (state.getValue(property))
                {
                    if (firstDirection == null)
                    {
                        firstDirection = DirectionPropertyBlock.getDirection(property);
                    }
                    if (entry.getValue().bounds().move(result.getBlockPos()).inflate(0.01d).contains(hit))
                    {
                        return DirectionPropertyBlock.getDirection(property);
                    }
                }
            }
            return firstDirection;
        }
        return null;
    }

    public static VoxelShape getShapeForSingleFace(Direction direction)
    {
        return SHAPES.get(DirectionPropertyBlock.getProperty(direction));
    }

    public static int countSheets(BlockState state, Predicate<Direction> onlyTheseDirections)
    {
        int count = 0;
        for (Direction direction : Helpers.DIRECTIONS)
        {
            if (onlyTheseDirections.test(direction) && state.getValue(DirectionPropertyBlock.getProperty(direction)))
            {
                count++;
            }
        }
        return count;
    }

    public static boolean isEmpty(BlockState state)
    {
        for (BooleanProperty property : PROPERTIES)
        {
            if (state.getValue(property))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos)
    {
        if (!neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite()))
        {
            level.scheduleTick(currentPos, this, 0);
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        for (Direction direction : Helpers.DIRECTIONS)
        {
            if (state.getValue(DirectionPropertyBlock.getProperty(direction)))
            {
                final BlockPos adjacentPos = pos.relative(direction);
                final BlockState adjacentState = level.getBlockState(adjacentPos);
                if (!adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite()))
                {
                    // Neighbor state is not sturdy, so pop off
                    removeSheet(level, pos, state, direction, null, true);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        for (Direction direction : Helpers.DIRECTIONS)
        {
            if (state.getValue(DirectionPropertyBlock.getProperty(direction)))
            {
                final BlockPos adjacentPos = pos.relative(direction);
                final BlockState adjacentState = level.getBlockState(adjacentPos);
                if (!adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player)
    {
        if (level instanceof Level realLevel)
        {
            final Direction targetFace = getTargetedFace(realLevel, state, player);
            if (targetFace != null)
            {
                return level.getBlockEntity(pos, TDBlockEntities.MINERAL_SHEET.get())
                    .map(pile -> pile.getSheet(targetFace))
                    .orElse(ItemStack.EMPTY);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Destroys the block, including setting it to air. Called on both sides, and regardless of if a player has the correct tool to drop the block.
     * We have to manually check the harvest check here to see if we should drop anything.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        final boolean canActuallyHarvest = state.canHarvestBlock(level, pos, player);
        final Direction targetFace = getTargetedFace(level, state, player);

        playerWillDestroy(level, pos, state, player);

        if (targetFace == null)
        {
            level.destroyBlock(pos, false);
        }
        else
        {
            removeSheet(level, pos, state, targetFace, player, canActuallyHarvest);
        }

        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(PROPERTIES).add(FACING).add(MIRROR).add(getFluidProperty()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapeCache.get(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return DirectionPropertyBlock.rotate(state, rot).setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        if (mirror == Mirror.NONE)
        {
            return state; // don't flip MIRROR bit
        }

        return DirectionPropertyBlock.mirror(state, mirror).setValue(FACING, mirror.mirror(state.getValue(FACING))).cycle(MIRROR);
    }

    public static boolean canPlace(Level level, BlockPos pos, BlockState stateToPlace)
    {
        return stateToPlace.canSurvive(level, pos);
    }

    @Override
    public FluidProperty getFluidProperty()
    {
        return FLUID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state)
    {
        return IFluidLoggable.super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)
    {
        if (fluid instanceof FlowingFluid && !getFluidProperty().canContain(fluid))
        {
            return true;
        }
        return IFluidLoggable.super.canPlaceLiquid(level, pos, state, fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn)
    {
        if (fluidStateIn.getType() instanceof FlowingFluid && !getFluidProperty().canContain(fluidStateIn.getType()))
        {
            for (Direction direction : Helpers.DIRECTIONS)
            {
                if (direction != null)
                {
                    ItemStack item = level.getBlockEntity(pos, TDBlockEntities.MINERAL_SHEET.get())
                        .map(pile -> pile.getSheet(direction))
                        .orElse(ItemStack.EMPTY);
                    if (item != null && level instanceof Level realLevel)
                    {
                        realLevel.addFreshEntity(new ItemEntity(realLevel, pos.getX(), pos.getY(), pos.getZ(), item));
                    }
                }
            }
            level.destroyBlock(pos, false);
            level.setBlock(pos, fluidStateIn.createLegacyBlock(), 2);
            return true;
        }
        return IFluidLoggable.super.placeLiquid(level, pos, state, fluidStateIn);
    }
}
