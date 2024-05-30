package tfcthermaldeposits.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static tfcthermaldeposits.TFCThermalDeposits.*;

import java.util.Optional;

public class PacketHandler
{
	public static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public PacketHandler() {}

	public static void register()
    {
		int index = 0;
		CHANNEL.registerMessage(index++, ClientboundStringNBTPacket.class, ClientboundStringNBTPacket::encode, ClientboundStringNBTPacket::new, ClientboundStringNBTPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(index++, ClientboundIntNBTPacket.class, ClientboundIntNBTPacket::encode, ClientboundIntNBTPacket::new, ClientboundIntNBTPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(index++, ClientboundFloatNBTPacket.class, ClientboundFloatNBTPacket::encode, ClientboundFloatNBTPacket::new, ClientboundFloatNBTPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
