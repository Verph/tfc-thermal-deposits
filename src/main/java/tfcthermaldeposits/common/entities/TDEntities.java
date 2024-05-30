package tfcthermaldeposits.common.entities;

import java.util.Locale;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.common.entities.misc.TFCFallingBlockEntity;

import static tfcthermaldeposits.TFCThermalDeposits.MOD_ID;

@SuppressWarnings("unused")
public class TDEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);

    public static final RegistryObject<EntityType<PyroclasticBomb>> PYROCLASTIC_BOMB = register("pyroclastic_bomb", EntityType.Builder.<PyroclasticBomb>of(PyroclasticBomb::new, MobCategory.MISC).fireImmune().sized(0.99F, 0.99F).clientTrackingRange(32).updateInterval(1));

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder)
    {
        return register(name, builder, true);
    }

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder, boolean serialize)
    {
        final String id = name.toLowerCase(Locale.ROOT);
        return ENTITIES.register(id, () -> {
            if (!serialize) builder.noSave();
            return builder.build(MOD_ID + ":" + id);
        });
    }
}
