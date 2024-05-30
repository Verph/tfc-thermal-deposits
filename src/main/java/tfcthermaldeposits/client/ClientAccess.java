package tfcthermaldeposits.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ClientAccess
{
	public static void setEntityStringTag(String name, String tag, int id)
    {
		Minecraft minecraft = Minecraft.getInstance();
		Entity ent = minecraft.level.getEntity(id);
		if (ent != null)
        {
			ent.getPersistentData().putString(name, tag);
		}
	}

	public static void setEntityBooleanTag(String name, boolean tag, int id)
    {
		Minecraft minecraft = Minecraft.getInstance();
		Entity ent = minecraft.level.getEntity(id);
		if (ent != null)
        {
			ent.getPersistentData().putBoolean(name, tag);
		}
	}

	public static void setEntityIntTag(String name, int tag, int id)
    {
		Minecraft minecraft = Minecraft.getInstance();
		Entity ent = minecraft.level.getEntity(id);
		if (ent != null)
        {
			ent.getPersistentData().putInt(name, tag);
		}
	}

	@SuppressWarnings("resource")
	public static void updateEntityIntNBT(String nbt, int value)
    {
		Player player = Minecraft.getInstance().player;
		if (player != null)
        {
			player.getPersistentData().putInt(nbt, value);
		}
	}

	public static void setEntityFloatTag(String name, float tag, int id)
    {
		Minecraft minecraft = Minecraft.getInstance();
		Entity ent = minecraft.level.getEntity(id);
		if (ent != null)
        {
			ent.getPersistentData().putFloat(name, tag);
		}
	}

	@SuppressWarnings("resource")
	public static void updateEntityFloatNBT(String nbt, float value)
    {
		Player player = Minecraft.getInstance().player;
		if (player != null)
        {
			player.getPersistentData().putFloat(nbt, value);
		}
	}
}
