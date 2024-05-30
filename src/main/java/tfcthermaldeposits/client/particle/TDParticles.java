package tfcthermaldeposits.client.particle;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static tfcthermaldeposits.TFCThermalDeposits.*;

public class TDParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID);

    public static final RegistryObject<SimpleParticleType> PYROCLASTIC_BOMB_EMITTER = register("pyroclastic_bomb_emitter", true);
    public static final RegistryObject<SimpleParticleType> PYROCLASTIC_BOMB = register("pyroclastic_bomb", true);

    @SuppressWarnings("deprecation")
    private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(String name, ParticleOptions.Deserializer<T> deserializer, final Function<ParticleType<T>, Codec<T>> codec)
    {
        return PARTICLE_TYPES.register(name, () -> new ParticleType<>(false, deserializer){
            @Override
            public Codec<T> codec()
            {
                return codec.apply(this);
            }
        });
    }

    private static RegistryObject<SimpleParticleType> register(String name, boolean overrideLimiter)
    {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(overrideLimiter));
    }

    private static RegistryObject<SimpleParticleType> register(String name)
    {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
}
