package tfcthermaldeposits;

import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.biome.VolcanoNoise;

import tfcthermaldeposits.client.TDSounds;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

import static tfcthermaldeposits.TFCThermalDeposits.*;

public class TDForgeEventHandler
{
    public static WorldGenLevel worldLevel;

    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        final IEventBus loader = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(TDForgeEventHandler::playerLivingTick);
        bus.addListener(TDForgeEventHandler::onFluidPlaceBlock);
        bus.addListener(TDForgeEventHandler::onWorldTick);
        bus.addListener(TDForgeEventHandler::onPlayerTick);
        //loader.addListener(TDForgeEventHandler::onPackFinder);
    }

    public static void onPackFinder(AddPackFindersEvent event)
    {
        try
        {
            if (event.getPackType() == PackType.CLIENT_RESOURCES)
            {
                final IModFile modFile = ModList.get().getModFileById(MOD_ID).getFile();
                final Path resourcePath = modFile.getFilePath();
                try (PathPackResources pack = new PathPackResources(modFile.getFileName() + ":overload", true, resourcePath){

                    private final IModFile file = ModList.get().getModFileById(MOD_ID).getFile();

                    @NotNull
                    @Override
                    protected Path resolve(String @NotNull ... paths)
                    {
                        return file.findResource(paths);
                    }
                })
                {
                    final PackMetadataSection metadata = pack.getMetadataSection(PackMetadataSection.TYPE);
                    if (metadata != null)
                    {
                        LOGGER.info("Injecting TFC Thermal Deposits override pack");
                        event.addRepositorySource(consumer ->
                            consumer.accept(Pack.readMetaAndCreate("tfcthermaldeposits_data", Component.literal("TFC Thermal Deposits Resources"), true, id -> pack, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN))
                        );
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

	public static void playerLivingTick(TickEvent.PlayerTickEvent event)
    {
		if (event.phase == TickEvent.Phase.END)
        {
			Player player = event.player;
			if (player.getPersistentData().getInt("shakeTime") > 0)
            {
				player.getPersistentData().putInt("shakeTime", player.getPersistentData().getInt("shakeTime") - 1);
			}
			if (player.getPersistentData().getInt("shakeCooldown") > 0)
            {
				player.getPersistentData().putInt("shakeCooldown", player.getPersistentData().getInt("shakeCooldown") - 1);
			}
		}
	}

    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event)
    {
        BlockState state = event.getNewState();
        RegistryRock rock = TDHelpers.rockTypeIgneous(event.getLevel(), event.getPos());

        if (Helpers.isBlock(state, Blocks.STONE))
        {
            event.setNewState(rock.getBlock(Rock.BlockType.HARDENED).get().defaultBlockState());
        }
        else if (Helpers.isBlock(state, Blocks.COBBLESTONE))
        {
            event.setNewState(rock.getBlock(Rock.BlockType.COBBLE).get().defaultBlockState());
        }
        else if (Helpers.isBlock(state, Blocks.BASALT))
        {
            event.setNewState(TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.HARDENED).get().defaultBlockState());
        }
    }

    public static void onWorldTick(TickEvent.LevelTickEvent event)
    {
        if (event.level instanceof WorldGenLevel level)
        {
            worldLevel = level;
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        Level level = player.level();
        if (player == null || level == null || worldLevel == null) return;

        if (player.getPersistentData().getFloat("shakeCooldown") <= 0)
        {
            BlockPos pos = player.blockPosition();
            RandomSource random = TDHelpers.seededRandom(pos); // To synchronize with other volcano stuff
            double rockTypeFactor = TDHelpers.getIntensityFactor(level, pos);
            double timeNoise = TDHelpers.volcanicRumbleNoise(level, level.dimension() == Level.NETHER ? true : TDConfig.COMMON.highTremorFrequency.get());
            final float shouldShake = TDConfig.COMMON.toggleVolcanoShake.get() ? 1f : 0f;
            if (level.dimension() == Level.OVERWORLD)
            {
                if (timeNoise >= 90D + (random.nextInt(8) * rockTypeFactor) && random.nextInt((int) Math.round(rockTypeFactor + 1)) == 0)
                {
                    Biome biome = level.getBiome(pos).value();
                    if (biome != null)
                    {
                        if (TFCBiomes.hasExtension(level, biome))
                        {
                            double volcanoNoise = new VolcanoNoise(worldLevel.getSeed()).calculateEasing(pos.getX(), pos.getZ(), TFCBiomes.getExtension(worldLevel, biome).getVolcanoRarity());
                            if (volcanoNoise > 0D)
                            {
                                long shakeTime = Math.round(Mth.clamp(volcanoNoise * 2.5f, 0f, 1.5f) * 400f);
                                float shakeFactor = (float) (Mth.clamp(volcanoNoise * 2.25f, 0f, 1.25f) * (timeNoise * 0.0066f)); // Further away or time => weaker shake/rumble

                                player.getPersistentData().putInt("shakeTime", (int) (shakeTime * shouldShake));
                                player.getPersistentData().putFloat("shakeFactor", shakeFactor * shouldShake);

                                float soundAmplifier = TDConfig.COMMON.earthquakeSoundAmplifier.get().floatValue();
                                if (player.isUnderWater())
                                {
                                    player.playSound(TDSounds.TREMOR_UNDERWATER.get(), (soundAmplifier + random.nextFloat() * 2f) * shakeFactor, 1f + random.nextFloat() * 0.5f);
                                    player.getPersistentData().putFloat("shakeCooldown", 400);
                                }
                                else if (player.blockPosition().getY() < level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() - 10)
                                {
                                    player.playSound(TDSounds.TREMOR.get(), (soundAmplifier + random.nextFloat() * 2f) * shakeFactor, 1f + random.nextFloat() * 0.5f);
                                    player.getPersistentData().putFloat("shakeCooldown", 280);
                                }
                                else
                                {
                                    player.playSound(TDSounds.EARTHQUAKE.get(), (soundAmplifier + random.nextFloat() * 2f) * shakeFactor, 1f + random.nextFloat() * 0.5f);
                                    player.getPersistentData().putFloat("shakeCooldown", 480);
                                }
                            }
                        }
                    }
                }
            }
            else if (level.dimension() == Level.NETHER && TDConfig.COMMON.toggleNetherTremor.get())
            {
                if (timeNoise >= 125D + random.nextInt(32))
                {
                    long shakeTime = Math.round(Math.pow((timeNoise * 0.01f), 1.3f) * 200f);
                    float shakeFactor = (float) (Math.pow((timeNoise * 0.005f), 1.25f));

                    player.getPersistentData().putInt("shakeTime", (int) (shakeTime * shouldShake));
                    player.getPersistentData().putFloat("shakeFactor", shakeFactor * shouldShake);

                    boolean bool = random.nextBoolean();
                    SoundEvent sound = bool ? TDSounds.EARTHQUAKE.get() : TDSounds.TREMOR.get();
                    int cooldown = bool ? 480 : 280;
                    player.playSound(sound, (5f + random.nextFloat() * 2f) * shakeFactor, 1f + random.nextFloat() * 0.5f);
                    player.getPersistentData().putFloat("shakeCooldown", cooldown);
                }
            }
            if (TDConfig.COMMON.toggleDebug.get())
            {
                TFCThermalDeposits.LOGGER.info("Time noise is " + timeNoise);
            }
        }
    }
}
