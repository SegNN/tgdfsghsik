package ua.holydev.holycases.cases;

import java.util.List;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jdbi.v3.core.async.JdbiExecutor;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.animation.Animation;
import ua.holydev.holycases.lucks.Luck;
import ua.holydev.holycases.menu.Menu;
import ua.holydev.holycases.misc.Messages;
import ua.holydev.holycases.particle.WrappedParticle;
import ua.holydev.holycases.reward.Reward;

public class ConfigurableCase extends GameCase {
   public ConfigurableCase(
      HolyCases plugin,
      JdbiExecutor executor,
      String name,
      String titleAboveCase,
      double titleAboveCaseYOffset,
      boolean isLuckWork,
      Location location,
      BlockFace direction,
      Animation animation,
      Material blockMaterial,
      List<WrappedParticle> wrappedParticles,
      List<Reward> rewards,
      Function<Player, Menu> menuCreator,
      List<String> prizeMessage,
      Messages messages
   ) {
      super(
         plugin,
         executor,
         name,
         titleAboveCase,
         titleAboveCaseYOffset,
         isLuckWork,
         location,
         direction,
         animation,
         blockMaterial,
         wrappedParticles,
         rewards,
         menuCreator,
         prizeMessage,
         messages
      );
   }

   @Override
   public void open(Player player, Luck luck) {
      this.opening = player;
      this.openingLuck = luck;
      removeAll(this);
      this.setTitleVisible(false);
      this.plugin.addPlayer(player, this);
      this.getAnimation().animate(this);
   }

   @Override
   public void onOpenEnd() {
      this.opening = null;
      this.openingLuck = null;
      this.setTitleVisible(true);
   }
}
