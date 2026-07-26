package ua.fichadev.fichacases.particle;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

public record WrappedParticle(Particle particle, int count, double offX, double offY, double offZ, double speed, int red, int green, int blue) {
   public Object getData() {
      return this.particle.getDataType() == DustOptions.class ? new DustOptions(Color.fromRGB(this.red, this.green, this.blue), 1.0F) : null;
   }
}
