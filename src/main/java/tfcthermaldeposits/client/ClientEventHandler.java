package tfcthermaldeposits.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import tfcthermaldeposits.client.particle.PyroclasticBombParticle;
import tfcthermaldeposits.client.particle.PyroclasticBombSeedParticle;
import tfcthermaldeposits.client.particle.TDParticles;
import tfcthermaldeposits.client.render.blockentity.MineralSheetBlockEntityRenderer;
import tfcthermaldeposits.client.render.entity.PyroclasticBombRenderer;
import tfcthermaldeposits.common.TDCreativeTabs;
import tfcthermaldeposits.common.blockentities.TDBlockEntities;
import tfcthermaldeposits.common.entities.TDEntities;
import tfcthermaldeposits.config.TDConfig;

public class ClientEventHandler
{
    public static void init()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;

        bus.addListener(ClientEventHandler::clientSetup);
        bus.addListener(ClientEventHandler::registerEntityRenderers);
        bus.addListener(ClientEventHandler::registerParticleFactories);
        bus.addListener(TDCreativeTabs::onBuildCreativeTab);
        eventBus.addListener(ClientEventHandler::setupCameraAngles);
    }

    public static void setupCameraAngles(ComputeCameraAngles event)
    {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
        Level level = player.level();

		if (TDConfig.COMMON.toggleVolcanoShake.get() && level.isClientSide() && player != null && player.getPersistentData().getInt("shakeTime") >= 1)
        {
			float shakeAmount = 1f * player.getPersistentData().getFloat("shakeFactor");
			float pitchAmount = shakeAmount * 0.33f;
			event.setYaw(event.getYaw() + (float) shakeAmount * (float) Math.cos((Math.random() * 5f + 1f) * 3d * ((float) player.getPersistentData().getInt("shakeTime")) / 20f));
			event.setPitch(event.getPitch() + (float) pitchAmount * (float) Math.cos((Math.random() * 3f + 1f) * 3d * ((float) player.getPersistentData().getInt("shakeTime")) / 20f));
			event.setRoll(event.getRoll() + (float) shakeAmount * (float) Math.cos((Math.random() * 4f + 1f) * 3d * ((float) player.getPersistentData().getInt("shakeTime")) / 20f));
		}
	}

    @SuppressWarnings("deprecation")
    public static void clientSetup(FMLClientSetupEvent event)
    {
        // Render Types
        final RenderType cutout = RenderType.cutout();

        //ItemBlockRenderTypes.setRenderLayer(TDBlocks.MINERAL_SHEET.get(), cutout);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        // Entities
        event.registerEntityRenderer(TDEntities.PYROCLASTIC_BOMB.get(), PyroclasticBombRenderer::new);

        // Block Entities
        event.registerBlockEntityRenderer(TDBlockEntities.MINERAL_SHEET.get(), ctx -> new MineralSheetBlockEntityRenderer());
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(TDParticles.PYROCLASTIC_BOMB.get(), PyroclasticBombParticle.Provider::new);
        event.registerSpecial(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), new PyroclasticBombSeedParticle.Provider());
    }
}
