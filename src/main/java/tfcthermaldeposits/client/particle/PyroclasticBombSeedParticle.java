package tfcthermaldeposits.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;

public class PyroclasticBombSeedParticle extends NoRenderParticle
{
    private int life;
    private final int lifeTime = 8;

    PyroclasticBombSeedParticle(ClientLevel pLevel, double pX, double pY, double pZ)
    {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void tick()
    {
        for (int i = 0; i < 6; ++i)
        {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            this.level.addParticle(TDParticles.PYROCLASTIC_BOMB.get(), d0, d1, d2, (double)((float)this.life / (float)this.lifeTime), 0.0D, 0.0D);
        }

        ++this.life;
        if (this.life == this.lifeTime)
        {
            this.remove();
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
        {
            return new PyroclasticBombSeedParticle(pLevel, pX, pY, pZ);
        }
    }
}
