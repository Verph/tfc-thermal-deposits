package tfcthermaldeposits.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import tfcthermaldeposits.common.blocks.TDBlocks;
import tfcthermaldeposits.common.entities.PyroclasticBomb;

public class PyroclasticBombRenderer extends EntityRenderer<PyroclasticBomb>
{
    private final BlockRenderDispatcher blockRenderer;

    public PyroclasticBombRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PyroclasticBomb entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        int i = entity.getFuse();
        if ((float)i - partialTicks + 1.0F < 10.0F)
        {
            float f = 1.0F - ((float)i - partialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float f1 = 1.0F + f * 0.3F;
            poseStack.scale(f1, f1, f1);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, TDBlocks.PYROCLASTIC_BOMB.get().defaultBlockState(), poseStack, buffer, packedLight, false);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
        * Returns the location of an entity's texture.
        */
    public ResourceLocation getTextureLocation(PyroclasticBomb entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
