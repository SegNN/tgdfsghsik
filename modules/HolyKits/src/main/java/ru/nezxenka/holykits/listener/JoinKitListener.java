package ru.nezxenka.holykits.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.nezxenka.holykits.HolyKits;
import ru.nezxenka.holykits.managers.Kit;

public class JoinKitListener implements Listener {
   private final HolyKits plugin;

   public JoinKitListener(HolyKits plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (!player.hasPlayedBefore()) {
         Kit kit = this.plugin.getKitManager().getKit("start");
         if (kit != null) {
            kit.applyToInventory(player.getInventory());
            player.updateInventory();
            if (kit.getCooldown() > 0L) {
               this.plugin.getDatabase().setCooldown(player.getUniqueId(), "start", System.currentTimeMillis() + kit.getCooldown() * 1000L);
            }
         }
      }
   }
}
