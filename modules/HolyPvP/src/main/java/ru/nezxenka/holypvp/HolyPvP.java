package ru.nezxenka.holypvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HolyPvP extends JavaPlugin {
   public HashMap<Player, Pvp> pvpHashMap = new HashMap<>();
   public DatabaseManager databaseManager;
   public ConfigManager configManager;

   public HolyPvP() {
   }

   @Override
   public void onEnable() {
      this.getServer().getPluginManager().registerEvents(new PvpListener(this), this);
      this.getCommand("pvp").setExecutor(new HolyPvP.PvpCommand());
      this.getCommand("pvp").setTabCompleter(new HolyPvP.PvpCommand());
      this.databaseManager = new DatabaseManager(this);
      this.configManager = new ConfigManager(this);
   }

   @Override
   public void onDisable() {
      try {
         for (PlayerProfile profile : this.databaseManager.getAllPlayerProfiles().get()) {
            if (profile.getServer().equalsIgnoreCase(this.configManager.getServerName())) {
               this.databaseManager.deletePlayerProfile(profile.getNickname());
            }
         }

         for (Pvp pvp : this.pvpHashMap.values()) {
            pvp.stopForce();
         }
      } catch (Exception var4) {
      }
   }

   public CompletableFuture<Boolean> hasPlayerProfile(String playerName) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            PlayerProfile profile = this.databaseManager.getPlayerProfile(playerName).get();
            return profile != null;
         } catch (Exception var31) {
            this.getLogger().severe("Ошибка при проверке профиля игрока " + playerName + ": " + var31.getMessage());
            return false;
         }
      });
   }

   public void sendWorldBorder(Player player, Location center, int radius) {
      try {
         WorldBorder worldBorder = Bukkit.createWorldBorder();
         worldBorder.setCenter(center.getX(), center.getZ());
         worldBorder.setSize(radius * 2);
         worldBorder.setWarningDistance(5);
         worldBorder.setWarningTime(15);
         player.setWorldBorder(worldBorder);
      } catch (Exception var101) {
         var101.printStackTrace();
      }
   }

   /**
    * On 1.16.5 the border was a raw PacketPlayOutWorldBorder, so the client threw it away by itself on
    * teleport/relog and it never had to be cleared. A per-player API border persists until removed, so
    * the fight-end paths clear it explicitly to keep the observable behaviour identical.
    */
   public void clearWorldBorder(Player player) {
      try {
         player.setWorldBorder(null);
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   private static class PvpCommand implements CommandExecutor, TabExecutor {
      private PvpCommand() {
      }

      @Override
      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

         if (sender instanceof Player) {
            HolyPvP plugin = JavaPlugin.getPlugin(HolyPvP.class);
            if (plugin.pvpHashMap.containsKey((Player)sender)) {
               sender.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', plugin.getConfig().getString("messages.deny.already", "&x&f&b&0&0&0&0▶ &fВы уже находитесь в &6PvP.")
                  )
               );
               return true;
            }

            if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
               new PvpList(plugin, (Player)sender);
               return true;
            }

            new PvpMenu(plugin, (Player)sender);
         } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&0&0&0▶ &fТолько &6игроки &fмогут использовать эту команду."));
         }

         return true;
      }

      @Nullable
      @Override
      public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

         List<String> list = new ArrayList<>();
         if (args.length == 1) {
            list.add("list");
         }

         return list;
      }

   }
}
