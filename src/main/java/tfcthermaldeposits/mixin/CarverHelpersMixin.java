package tfcthermaldeposits.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.dries007.tfc.world.carver.CarverHelpers;

import tfcthermaldeposits.common.blocks.TDBlockStateProperties;

@Mixin(CarverHelpers.class)
public abstract class CarverHelpersMixin
{
    @Overwrite(remap = false)
    @Nullable
    public static <C extends CarverConfiguration> BlockState getCarveState(CarvingContext context, C config, BlockPos pos, Aquifer aquifer)
    {
        if (pos.getY() <= config.lavaLevel.resolveY(context))
        {
            FluidState lavaFluid = Fluids.LAVA.defaultFluidState();
            lavaFluid.setValue(TDBlockStateProperties.NATURAL_INVERSE, true).setValue(TDBlockStateProperties.MINERAL_AMOUNT, 100);

            BlockState lava = lavaFluid.createLegacyBlock();
            lava.setValue(TDBlockStateProperties.NATURAL, true).setValue(TDBlockStateProperties.MINERAL_AMOUNT, 100);

            return lava;
        }
        else
        {
            final BlockState carveState = aquifer.computeSubstance(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()), 0);
            if (carveState == null)
            {
                return isDebugEnabled(config) ? config.debugSettings.getBarrierState() : null;
            }
            return isDebugEnabled(config) ? getDebugState(config, carveState) : carveState;
        }
    }

    @Shadow
    public static BlockState getDebugState(CarverConfiguration config, BlockState state)
    {
        return state;
    }

    @Shadow
    public static boolean isDebugEnabled(CarverConfiguration config)
    {
        return config.debugSettings.isDebugMode();
    }
}
