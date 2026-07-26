package org.dimasik.expirytime;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Expirytime extends JavaPlugin {
   private LuckPerms luckPerms;

   public Expirytime() {
   }

   public void onEnable() {
      RegisteredServiceProvider<LuckPerms> provider = this.getServer().getServicesManager().getRegistration(LuckPerms.class);
      if (provider != null) {
         this.luckPerms = (LuckPerms)provider.getProvider();
      }

      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new Expirytime.ExpireExpansion(this).register();
      } else {
         this.getLogger().warning("PlaceholderAPI не найден! Плагин не будет работать.");
      }
   }

   public void onDisable() {
   }

   public LuckPerms getLuckPerms() {
      return this.luckPerms;
   }

   private class ExpireExpansion extends PlaceholderExpansion {
      private final Expirytime plugin;

      public ExpireExpansion(Expirytime plugin) {
         this.plugin = plugin;
      }

      public String getIdentifier() {
         return "expire";
      }

      public String getAuthor() {
         return "Dimasik";
      }

      public String getVersion() {
         return "1.0";
      }

      public String onPlaceholderRequest(Player player, String identifier) {
         if (player == null) {
            return "";
         } else if (identifier.equals("donate")) {
            User user = this.plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
            if (user == null) {
               return "Ошибка";
            } else {
               Optional<InheritanceNode> primaryGroupNode = user.getNodes()
                  .stream()
                  .filter(node -> node instanceof InheritanceNode)
                  .map(node -> (InheritanceNode)node)
                  .filter(node -> node.getGroupName().equalsIgnoreCase(user.getPrimaryGroup()))
                  .findFirst();
               if (primaryGroupNode.isPresent()) {
                  Instant expiry = primaryGroupNode.get().getExpiry();
                  if (expiry == null) {
                     return "∞";
                  } else {
                     Duration duration = Duration.between(Instant.now(), expiry);
                     long days = duration.toDays();
                     long hours = duration.toHours() % 24L;
                     long minutes = duration.toMinutes() % 60L;
                     return days >= 1L ? days + 1L + "д" : String.format("%dч %dм", hours, minutes);
                  }
               } else {
                  return "Ошибка";
               }
            }
         } else {
            return null;
         }
      }
   }
}
