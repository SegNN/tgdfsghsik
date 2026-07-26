package ua.fichatime.fichapromocode.command;

import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ua.fichatime.fichapromocode.manager.ConfigManager;

public class CommandVisibilityHandler implements Listener {
   private final ConfigManager configManager;

   public CommandVisibilityHandler(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @EventHandler
   public void onCommandSend(PlayerCommandSendEvent event) {
      Map<String, Map<String, Object>> promocodes = this.configManager.getPromocodes();
      if (promocodes != null) {
         for (String promoCode : promocodes.keySet()) {
            boolean showInCommands = this.configManager.isShowInCommands(promoCode);
            if (!showInCommands) {
               event.getCommands().remove(promoCode);
            }
         }
      }
   }
}
