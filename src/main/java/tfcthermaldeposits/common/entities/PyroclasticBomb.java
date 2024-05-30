package tfcthermaldeposits.common.entities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryRock;

import tfcthermaldeposits.client.particle.TDParticles;
import tfcthermaldeposits.config.TDConfig;
import tfcthermaldeposits.util.TDHelpers;

public class PyroclasticBomb extends PrimedTnt
{
    public static final BlockState DEFAULT_BLOCK = TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.COBBLE).get().defaultBlockState();
    @Nullable private LivingEntity owner;
    public SimpleParticleType particleType;
    public SimpleParticleType particleTypeExplosion;
    public Level.ExplosionInteraction explosionInteraction;
    public double explosionRadius;
    public boolean shouldSpreadFire;
    public boolean shouldSpawnScoria;
    public boolean shouldSpawnVanillaExplosionParticles;
    public RegistryRock rock;

    public PyroclasticBomb(EntityType<? extends PrimedTnt> entityType, Level level)
    {
        this(entityType, level, ParticleTypes.SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), 4.0f, Level.ExplosionInteraction.NONE, true, false, true, Rock.BASALT);
    }

    public PyroclasticBomb(EntityType<? extends PrimedTnt> entityType, Level level, SimpleParticleType particleType, SimpleParticleType particleTypeExplosion, double explosionRadius, Level.ExplosionInteraction explosionInteraction, boolean shouldSpreadFire, boolean shouldSpawnScoria, boolean shouldSpawnVanillaExplosionParticles, RegistryRock rock)
    {
        super(entityType, level);
        this.particleType = particleType;
        this.particleTypeExplosion = particleTypeExplosion;
        this.explosionRadius = explosionRadius;
        this.explosionInteraction = explosionInteraction;
        this.shouldSpreadFire = shouldSpreadFire;
        this.shouldSpawnScoria = shouldSpawnScoria;
        this.shouldSpawnVanillaExplosionParticles = shouldSpawnVanillaExplosionParticles;
        this.rock = rock;
    }

    public PyroclasticBomb(Level level, double x, double y, double z, @Nullable LivingEntity owner)
    {
        this(level, x, y, z, owner, ParticleTypes.LARGE_SMOKE, TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), 4.0f, Level.ExplosionInteraction.NONE, true, false, true, Rock.BASALT);
    }

    public PyroclasticBomb(Level level, double x, double y, double z, @Nullable LivingEntity owner, SimpleParticleType particleType, SimpleParticleType particleTypeExplosion, double explosionRadius, Level.ExplosionInteraction explosionInteraction, boolean shouldSpreadFire, boolean shouldSpawnScoria, boolean shouldSpawnVanillaExplosionParticles, RegistryRock rock)
    {
        this(TDEntities.PYROCLASTIC_BOMB.get(), level, particleType, particleTypeExplosion, explosionRadius, explosionInteraction, shouldSpreadFire, shouldSpawnScoria, shouldSpawnVanillaExplosionParticles, rock);
        this.setPos(x, y, z);
        double d0 = level.random.nextDouble() * (Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.owner = owner;
        this.particleType = particleType;
        this.particleTypeExplosion = particleTypeExplosion;
        this.explosionRadius = explosionRadius;
        this.explosionInteraction = explosionInteraction;
        this.shouldSpreadFire = shouldSpreadFire;
        this.shouldSpawnScoria = shouldSpawnScoria;
        this.shouldSpawnVanillaExplosionParticles = shouldSpawnVanillaExplosionParticles;
        this.rock = rock;
    }

    public SimpleParticleType getParticleType()
    {
        return particleType;
    }

    public SimpleParticleType getExplosionParticles()
    {
        return particleTypeExplosion;
    }

    public Level.ExplosionInteraction getExplosionInteraction()
    {
        return explosionInteraction;
    }

    public double getExplosionRadius()
    {
        return explosionRadius;
    }

    public boolean shouldSpreadFire()
    {
        return shouldSpreadFire;
    }

    public boolean shouldSpawnScoria()
    {
        return shouldSpawnScoria;
    }

    public boolean shouldSpawnVanillaExplosionParticles()
    {
        return shouldSpawnVanillaExplosionParticles;
    }

    public RegistryRock getRock()
    {
        return rock;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.ALL;
    }

    @Override
    public void tick()
    {
        if (!this.isNoGravity())
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround())
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0 || this.onGround() || this.isColliding(this.blockPosition(), this.level().getBlockState(this.blockPosition())))
        {
            this.level().playLocalSound(this.position().x, this.position().y, this.position().z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2F) * 0.7F, false);
            this.level().addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), this.position().x, this.position().y, this.position().z, 1.0D, 0.0D, 0.0D);

            if (!this.level().isClientSide)
            {
                this.explode();
                if (TDConfig.COMMON.togglePyroclasticBombScoria.get() && shouldSpawnScoria() && this.level().getBlockState(this.blockPosition().below()).isSolid())
                {
                    this.placeBlocks(this.level(), this.blockPosition(), this.level().getRandom());
                }
            }

            this.level().playLocalSound(this.position().x, this.position().y, this.position().z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 4.0F, (0.4F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2F) * 0.7F, false);
            this.level().addParticle(TDParticles.PYROCLASTIC_BOMB_EMITTER.get(), this.position().x, this.position().y, this.position().z, 1.0D, 0.0D, 0.0D);

            this.discard();
        }
        else
        {
            if (this.level().getBlockState(this.blockPosition()).getMapColor(this.level(), this.blockPosition()) == MapColor.WATER)
            {
                this.updateInWaterStateAndDoFluidPushing();
            }
            for (int j = 0; j < 2 + random.nextInt(10); j++)
            {
                this.level().addParticle(getParticleType(), this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void explode()
    {
        boolean fire = shouldSpreadFire() && shouldSpawnScoria();
        this.level().explode(this, null, null, this.getX(), this.getY(), this.getZ(), (float) getExplosionRadius(), fire, getExplosionInteraction(), shouldSpawnVanillaExplosionParticles());
    }

    // Copy of TFC's "BabyBoulderFeature"
    public void placeBlocks(Level level, BlockPos pos, RandomSource random)
    {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (random.nextFloat() < 0.5f)
        {
            // small sphere-like boulder
            setBlockCheck(level, pos);
            for (Direction dir : Helpers.DIRECTIONS)
            {
                if (random.nextFloat() < 0.8f)
                {
                    cursor.setWithOffset(pos, dir);
                    setBlockCheck(level, cursor);
                }
            }
        }
        else if (random.nextFloat() < 0.5f)
        {
            // short column of rock with an optional second column next to it
            cursor.setWithOffset(pos, 0, -2, 0);
            final int height = 4 + random.nextInt(3);
            for (int i = 0; i < height; i++)
            {
                setBlockCheck(level, cursor);
                cursor.move(0, 1, 0);
            }
            cursor.setWithOffset(pos, 0, -2, 0);
            cursor.move(Direction.Plane.HORIZONTAL.getRandomDirection(random));
            for (int i = 0; i < height - 2; i++)
            {
                setBlockCheck(level, cursor);
                cursor.move(0, 1, 0);
            }
        }
        else
        {
            // a single block with a single block underneath
            cursor.set(pos);
            setBlockCheck(level, cursor);
            cursor.move(0, -1, 0);
            setBlockCheck(level, cursor);
        }
    }

    public void setBlockCheck(Level level, BlockPos pos)
    {
        if (level.isEmptyBlock(pos) || (level.getBlockState(pos).canBeReplaced() && !(level.getFluidState(pos).getType() == Fluids.LAVA || level.getBlockState(pos).getBlock() == Blocks.LAVA)))
        {
            level.setBlockAndUpdate(pos, getRandomRock());
        }
    }

    public BlockState getRandomRock()
    {
        RegistryRock rock = this.getRock();
        BlockState scoria = TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.COBBLE).get().defaultBlockState();
        if (TDConfig.COMMON.togglePyroclasticBombOre.get() && random.nextInt(TDConfig.COMMON.pyroclasticBombOreChance.get()) == 0)
        {
            Ore[] ores = Ore.values();
            Ore.Grade[] grades = Ore.Grade.values();
            Ore ore = ores[random.nextInt(ores.length)];
            Ore.Grade grade = grades[random.nextInt(grades.length)];
            if (ore.isGraded())
            {
                scoria = TFCBlocks.GRADED_ORES.get(rock).get(ore).get(grade).get().defaultBlockState();
            }
            if (TDHelpers.isIgneous(this.level(), this.blockPosition()) && random.nextInt(10) == 0)
            {
                scoria = Blocks.OBSIDIAN.defaultBlockState();
                if (random.nextInt(10) == 0)
                {
                    scoria = Blocks.CRYING_OBSIDIAN.defaultBlockState();
                }
            }
        }
        if (this.level().dimension() == Level.NETHER && random.nextInt(10) == 0 && (rock.category() == RockCategory.IGNEOUS_EXTRUSIVE || rock.category() == RockCategory.IGNEOUS_INTRUSIVE))
        {
            scoria = TFCBlocks.MAGMA_BLOCKS.get(rock).get().defaultBlockState();
        }
        return scoria;
    }
}
