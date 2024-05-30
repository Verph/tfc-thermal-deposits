package tfcthermaldeposits.client;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import tfcthermaldeposits.util.TDHelpers;

import static tfcthermaldeposits.TFCThermalDeposits.*;

public class TDSounds
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    public static final RegistryObject<SoundEvent> TREMOR = create("tremor");
    public static final RegistryObject<SoundEvent> TREMOR_UNDERWATER = create("tremor_underwater");
    public static final RegistryObject<SoundEvent> EARTHQUAKE = create("earthquake");

    private static RegistryObject<SoundEvent> create(String name)
    {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TDHelpers.identifier(name)));
    }

    private static Optional<Supplier<SoundEvent>> createOptional(String name, boolean present)
    {
        return Optional.ofNullable(present ? create(name) : null);
    }
}
