package tfcthermaldeposits;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import tfcthermaldeposits.util.TDInteractionManager;
import tfcthermaldeposits.client.ClientEventHandler;
import tfcthermaldeposits.client.TDSounds;
import tfcthermaldeposits.client.particle.TDParticles;
import tfcthermaldeposits.common.blockentities.TDBlockEntities;
import tfcthermaldeposits.common.blocks.TDBlocks;
import tfcthermaldeposits.common.entities.TDEntities;
import tfcthermaldeposits.common.items.TDItems;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.network.PacketHandler;

@Mod(TFCThermalDeposits.MOD_ID)
public class TFCThermalDeposits
{
    public static final String MOD_ID = "tfcthermaldeposits";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCThermalDeposits()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);

        TDConfig.init();
        TDBlocks.BLOCKS.register(bus);
        TDItems.ITEMS.register(bus);
        TDSounds.SOUNDS.register(bus);
        TDBlockEntities.BLOCK_ENTITIES.register(bus);
        TDEntities.ENTITIES.register(bus);
        TDParticles.PARTICLE_TYPES.register(bus);

        TDForgeEventHandler.init();

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ClientEventHandler.init();
        }
    }

    public void setup(FMLCommonSetupEvent event)
    {
        LOGGER.info("TFC Thermal Deposits Common Setup");

        event.enqueueWork(() -> {
            TDInteractionManager.init();
            PacketHandler.register();
        });
    }
}