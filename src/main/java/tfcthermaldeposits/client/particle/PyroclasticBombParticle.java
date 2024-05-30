package tfcthermaldeposits.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class PyroclasticBombParticle extends TextureSheetParticle
{
    public final SpriteSet sprites;

    public PyroclasticBombParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pQuadSizeMultiplier, SpriteSet pSprites)
    {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.lifetime = 6 + this.random.nextInt(4);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.quadSize = 2.0F * (1.0F - (float)pQuadSizeMultiplier * 0.5F);
        this.sprites = pSprites;
        this.setSpriteFromAge(pSprites);
    }

    @Override
    public int getLightColor(float pPartialTick)
    {
        return 15728880;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
        else
        {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        public final SpriteSet sprites;

        public Provider(SpriteSet pSprites)
        {
            this.sprites = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
        {
            return new PyroclasticBombParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
        }
    }
}
