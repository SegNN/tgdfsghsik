package ua.holymine.holycommandwhitelist;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holymine.holycommandwhitelist.utils.ColorUtils;

public final class HolyCommandWhitelist extends JavaPlugin {
   private static HolyCommandWhitelist instance;
   private Map<String, String> groupMessages = new HashMap<>();
   private Map<String, List<String>> commandGroups = new HashMap<>();
   private Map<String, List<String>> subcommandGroups = new HashMap<>();
   private Map<Player, String> lastCommandMap = new HashMap<>();
   private Plugin cwlPlugin;
   private ProtocolManager protocolManager;
   private PacketAdapter sendAdapter;
   private PacketAdapter receiveAdapter;

   public HolyCommandWhitelist() {
   }

   public void onEnable() {
      instance = this;
      this.cwlPlugin = Bukkit.getPluginManager().getPlugin("CommandWhitelist");
      if (this.cwlPlugin == null) {
         this.getLogger().severe("CommandWhitelist не найден");
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         this.protocolManager = ProtocolLibrary.getProtocolManager();
         this.saveDefaultConfig();
         this.loadConfig();
         this.loadCommandWhitelistConfig(this.cwlPlugin);
         this.receiveAdapter = new PacketAdapter(this, ListenerPriority.MONITOR, Client.CHAT) {
            public void onPacketReceiving(PacketEvent event) {
               if (event.getPlayer() != null) {
                  Player player = event.getPlayer();
                  String message = (String)event.getPacket().getStrings().read(0);
                  if (message.startsWith("/")) {
                     String command = message.substring(1).split(" ")[0].toLowerCase();
                     HolyCommandWhitelist.this.lastCommandMap.put(player, command);
                  }
               }
            }
         };
         this.sendAdapter = new PacketAdapter(this, ListenerPriority.LOWEST, Server.CHAT) {
            public void onPacketSending(PacketEvent event) {
               if (event.getPlayer() != null) {
                  Player player = event.getPlayer();

                  try {
                     String lastCommand = HolyCommandWhitelist.this.lastCommandMap.get(player);
                     if (lastCommand != null) {
                        String requiredGroup = HolyCommandWhitelist.this.findRequiredGroupForCommand(lastCommand, player);
                        if (requiredGroup != null) {
                           String customMessage = HolyCommandWhitelist.this.getGroupRequiredMessage(requiredGroup);
                           if (customMessage != null) {
                              event.setCancelled(true);
                              HolyCommandWhitelist.this.lastCommandMap.remove(player);
                              Bukkit.getScheduler().runTask(HolyCommandWhitelist.this, () -> player.sendMessage(customMessage));
                           }
                        } else {
                           HolyCommandWhitelist.this.lastCommandMap.remove(player);
                        }
                     }
                  } catch (Exception var6) {
                  }
               }
            }
         };
         this.protocolManager.addPacketListener(this.receiveAdapter);
         this.protocolManager.addPacketListener(this.sendAdapter);
      }
   }

   public void onDisable() {
      if (this.protocolManager != null) {
         if (this.receiveAdapter != null) {
            this.protocolManager.removePacketListener(this.receiveAdapter);
         }

         if (this.sendAdapter != null) {
            this.protocolManager.removePacketListener(this.sendAdapter);
         }
      }

      this.lastCommandMap.clear();
   }

   public void loadConfig() {
      this.reloadConfig();
      this.groupMessages.clear();
      ConfigurationSection groupMessagesSection = this.getConfig().getConfigurationSection("group_messages");
      if (groupMessagesSection != null) {
         for (String group : groupMessagesSection.getKeys(false)) {
            this.groupMessages.put(group, this.getConfig().getString("group_messages." + group));
         }
      }
   }

   private void loadCommandWhitelistConfig(Plugin cwlPlugin) {
      this.commandGroups.clear();
      this.subcommandGroups.clear();
      ConfigurationSection groupsSection = cwlPlugin.getConfig().getConfigurationSection("groups");
      if (groupsSection != null) {
         for (String group : groupsSection.getKeys(false)) {
            List<String> commands = cwlPlugin.getConfig().getStringList("groups." + group + ".commands");
            List<String> subcommands = cwlPlugin.getConfig().getStringList("groups." + group + ".subcommands");
            this.commandGroups.put(group, commands);
            this.subcommandGroups.put(group, subcommands);
         }
      }
   }

   private String findRequiredGroupForCommand(String command, Player player) {
      for (Entry<String, List<String>> entry : this.commandGroups.entrySet()) {
         String group = entry.getKey();
         List<String> commands = entry.getValue();
         if (commands.contains(command) && !player.hasPermission("commandwhitelist." + group) && !player.hasPermission("commandwhitelist.*")) {
            return group;
         }
      }

      return null;
   }

   private String getGroupRequiredMessage(String group) {
      return this.groupMessages.containsKey(group) ? ColorUtils.translateColors(this.groupMessages.get(group)) : null;
   }

   public static HolyCommandWhitelist getInstance() {
      return instance;
   }
}
