package ru.nezxenka.holypvp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R3.WorldBorder;
import net.minecraft.server.v1_16_R3.WorldServer;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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
         CraftPlayer craftPlayer = (CraftPlayer)player;
         EntityPlayer nmsPlayer = craftPlayer.getHandle();
         WorldBorder worldBorder = new WorldBorder();
         WorldServer worldServer = ((CraftWorld)player.getWorld()).getHandle();
         Field worldField = WorldBorder.class.getDeclaredField("world");
         worldField.setAccessible(true);
         worldField.set(worldBorder, worldServer);
         worldBorder.setCenter(center.getX(), center.getZ());
         worldBorder.setSize(radius * 2);
         PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(worldBorder, EnumWorldBorderAction.INITIALIZE);
         nmsPlayer.playerConnection.sendPacket(packet);
      } catch (Exception var101) {
         var101.printStackTrace();
      }
   }

   private static class PvpCommand implements CommandExecutor, TabExecutor {
      private PvpCommand() {
      }

      @Override
      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (sender == null) {
            $$$reportNull$$$1(0);
         }

         if (command == null) {
            $$$reportNull$$$1(1);
         }

         if (label == null) {
            $$$reportNull$$$1(2);
         }

         if (args == null) {
            $$$reportNull$$$1(3);
         }

         if (sender == null) {
            $$$reportNull$$$0(0);
         }

         if (command == null) {
            $$$reportNull$$$0(1);
         }

         if (label == null) {
            $$$reportNull$$$0(2);
         }

         if (args == null) {
            $$$reportNull$$$0(3);
         }

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
         if (sender == null) {
            $$$reportNull$$$1(4);
         }

         if (command == null) {
            $$$reportNull$$$1(5);
         }

         if (alias == null) {
            $$$reportNull$$$1(6);
         }

         if (args == null) {
            $$$reportNull$$$1(7);
         }

         if (sender == null) {
            $$$reportNull$$$0(4);
         }

         if (command == null) {
            $$$reportNull$$$0(5);
         }

         if (alias == null) {
            $$$reportNull$$$0(6);
         }

         if (args == null) {
            $$$reportNull$$$0(7);
         }

         List<String> list = new ArrayList<>();
         if (args.length == 1) {
            list.add("list");
         }

         return list;
      }

      private static void $$$reportNull$$$0(int var0) {
         Object[] var10001 = new Object[3];
         switch (var0) {
            case 0:
            case 4:
            default:
               var10001[0] = "sender";
               break;
            case 1:
            case 5:
               var10001[0] = "command";
               break;
            case 2:
               var10001[0] = "label";
               break;
            case 3:
            case 7:
               var10001[0] = "args";
               break;
            case 6:
               var10001[0] = "alias";
         }

         var10001[1] = "ru/nezxenka/holypvp/HolyPvP$PvpCommand";
         switch (var0) {
            case 0:
            case 1:
            case 2:
            case 3:
            default:
               var10001[2] = "onCommand";
               break;
            case 4:
            case 5:
            case 6:
            case 7:
               var10001[2] = "onTabComplete";
         }

         throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", var10001));
      }
   }
}
