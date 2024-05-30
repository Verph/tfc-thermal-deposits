package tfcthermaldeposits.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import tfcthermaldeposits.client.ClientAccess;

public class ClientboundFloatNBTPacket
{
	public final String name;
	public final float tag;
	public final int entityId;

	public ClientboundFloatNBTPacket(String name, float tag, int entityId)
    {
		this.name = name;
		this.tag = tag;
		this.entityId = entityId;
	}

	public ClientboundFloatNBTPacket(FriendlyByteBuf buffer)
    {
		name = buffer.readUtf();
		tag = buffer.readFloat();
		entityId = buffer.readInt();
	}

	public void encode(FriendlyByteBuf buffer)
    {
		buffer.writeUtf(name);
		buffer.writeFloat(tag);
		buffer.writeInt(entityId);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
    {
		ctx.get().enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAccess.setEntityFloatTag(name, tag, entityId));
		});
		ctx.get().setPacketHandled(true);
	}
}