package tfcthermaldeposits.client.render.blockentity;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.util.Helpers;

import tfcthermaldeposits.common.blockentities.MineralSheetBlockEntity;
import tfcthermaldeposits.common.blocks.rock.Mineral;
import tfcthermaldeposits.common.blocks.rock.MineralSheetBlock;
import tfcthermaldeposits.util.TDHelpers;

public class MineralSheetBlockEntityRenderer implements BlockEntityRenderer<MineralSheetBlockEntity>
{
    @Override
    public void render(MineralSheetBlockEntity pile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        final BlockState state = pile.getBlockState();

        if (state.getBlock() instanceof DirectionPropertyBlock)
        {
            final Function<ResourceLocation, TextureAtlasSprite> textureAtlas = Minecraft.getInstance().getTextureAtlas(RenderHelpers.BLOCKS_ATLAS);
            final VertexConsumer builder = buffer.getBuffer(RenderType.cutout());

            for (Direction direction : Helpers.DIRECTIONS)
            {
                if (state.getValue(DirectionPropertyBlock.getProperty(direction)))
                {
                    final RandomSource random = random(pile.getBlockPos().getX(), pile.getBlockPos().getY(), pile.getBlockPos().getZ(), direction.ordinal());

                    Mineral mineral = pile.getOrCacheMineral(direction);
                    final String mineralName = pile.mineralNameRandom(mineral, random);

                    final ResourceLocation textureId = TDHelpers.identifier(mineralName);
                    final TextureAtlasSprite sprite = textureAtlas.apply(textureId);

                    renderSheet(poseStack, sprite, builder, direction, packedLight, packedOverlay);
                }
            }
        }
    }

    @Override
    public int getViewDistance()
    {
        return 128;
    }

    private void renderSheet(PoseStack poseStack, TextureAtlasSprite sprite, VertexConsumer buffer, Direction direction, int packedLight, int packedOverlay)
    {
        RenderHelpers.renderTexturedCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, MineralSheetBlock.getShapeForSingleFace(direction).bounds());
    }

    public RandomSource random(int posX, int posY, int posZ, int direction)
    {
        long seed = (posX + direction) * (posY + direction) * (posZ + direction);
        return RandomSource.create(seed).forkPositional().at(posX + direction, posY + direction, posZ + direction);
    }
}
