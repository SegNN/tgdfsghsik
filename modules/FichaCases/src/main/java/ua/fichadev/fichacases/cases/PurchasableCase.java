package ua.fichadev.fichacases.cases;

import java.util.List;
import java.util.function.Function;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jdbi.v3.core.async.JdbiExecutor;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.animation.Animation;
import ua.fichadev.fichacases.lucks.Luck;
import ua.fichadev.fichacases.menu.Menu;
import ua.fichadev.fichacases.misc.ColorUtil;
import ua.fichadev.fichacases.misc.Messages;
import ua.fichadev.fichacases.particle.WrappedParticle;
import ua.fichadev.fichacases.reward.Reward;

public class PurchasableCase extends GameCase {
   private final int expPrice;
   private final int sapphirePrice;
   private final String notEnoughExpMessage;
   private final String notEnoughSapphireMessage;

   public PurchasableCase(
      FichaCases plugin,
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
      Messages messages,
      int expPrice,
      int sapphirePrice,
      String notEnoughExpMessage,
      String notEnoughSapphireMessage
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
      this.expPrice = expPrice;
      this.sapphirePrice = sapphirePrice;
      this.notEnoughExpMessage = notEnoughExpMessage;
      this.notEnoughSapphireMessage = notEnoughSapphireMessage;
   }

   public void openWithPurchase(Player player, ClickType clickType, Luck luck) {
      if (clickType.isLeftClick()) {
         if (this.plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            try {
               PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();
               if (api.look(player.getUniqueId()) >= this.sapphirePrice) {
                  api.take(player.getUniqueId(), this.sapphirePrice);
                  this.opening = player;
                  this.openingLuck = luck;
                  removeAll(this);
                  this.setTitleVisible(false);
                  this.plugin.addPlayer(player, this);
                  this.getAnimation().animate(this);
               } else {
                  player.sendMessage(ColorUtil.parse(this.notEnoughSapphireMessage.replace("%price%", String.valueOf(this.sapphirePrice))));
               }
            } catch (Exception var5) {
               player.sendMessage(ColorUtil.parse("&cОшибка при работе с PlayerPoints!"));
               this.plugin.getLogger().warning("Ошибка PlayerPoints API: " + var5.getMessage());
            }
         } else {
            player.sendMessage(ColorUtil.parse("&cПлагин PlayerPoints не найден!"));
         }
      } else if (clickType.isRightClick()) {
         int currentLevel = player.getLevel();
         if (currentLevel >= this.expPrice) {
            this.plugin.getLogger().info("Игрок " + player.getName() + " покупает кейс за " + this.expPrice + " уровней. Текущий уровень: " + currentLevel);
            player.setLevel(currentLevel - this.expPrice);
            this.opening = player;
            this.openingLuck = luck;
            removeAll(this);
            this.setTitleVisible(false);
            this.plugin.addPlayer(player, this);
            this.getAnimation().animate(this);
         } else {
            player.sendMessage(ColorUtil.parse(this.notEnoughExpMessage.replace("%price%", String.valueOf(this.expPrice))));
         }
      }
   }

   @Override
   public void open(Player player, Luck luck) {
      player.sendMessage(ColorUtil.parse("&cЭтот кейс требует покупки!"));
   }

   @Override
   public void onOpenEnd() {
      this.opening = null;
      this.openingLuck = null;
      this.setTitleVisible(true);
   }

   public int getExpPrice() {
      return this.expPrice;
   }

   public int getSapphirePrice() {
      return this.sapphirePrice;
   }
}
