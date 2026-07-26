package org.impldev.holycore.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.impldev.holycore.HolyCore;

public class RegionCommandExtension implements Listener {
   private final HolyCore plugin;
   private final Map<String, List<String>> regionCommands = new HashMap<>();
   private final Set<String> playerRegionTracker = new HashSet<>();

   public RegionCommandExtension(HolyCore plugin) {
      this.plugin = plugin;
      this.loadRegions();
   }

   public void loadRegions() {
      this.regionCommands.clear();
      ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("region-commands");
      if (section != null) {
         for (String regionId : section.getKeys(false)) {
            ConfigurationSection regionSection = section.getConfigurationSection(regionId);
            if (regionSection != null) {
               boolean enabled = regionSection.getBoolean("enable", true);
               if (enabled) {
                  String regionName = regionSection.getString("region", "");
                  if (!regionName.isEmpty()) {
                     List<String> commands = regionSection.getStringList("commands");
                     if (!commands.isEmpty()) {
                        this.regionCommands.put(regionName.toLowerCase(), commands);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getFrom().getBlockX() != event.getTo().getBlockX()
         || event.getFrom().getBlockY() != event.getTo().getBlockY()
         || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
         if (!this.regionCommands.isEmpty()) {
            Player player = event.getPlayer();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(event.getTo()));

            for (ProtectedRegion region : regions) {
               String regionName = region.getId().toLowerCase();
               if (this.regionCommands.containsKey(regionName)) {
                  String trackingKey = player.getUniqueId().toString() + ":" + regionName;
                  if (!this.playerRegionTracker.contains(trackingKey)) {
                     this.playerRegionTracker.add(trackingKey);

                     for (String command : this.regionCommands.get(regionName)) {
                        String processedCommand = command.replace("%player%", player.getName()).replace("%uuid%", player.getUniqueId().toString());
                        if (processedCommand.startsWith("console:")) {
                           String consoleCommand = processedCommand.substring("console:".length()).trim();
                           Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                        } else {
                           player.performCommand(processedCommand);
                        }
                     }
                  }
               }
            }

            for (String trackedRegion : new HashSet<>(this.playerRegionTracker)) {
               if (trackedRegion.startsWith(player.getUniqueId().toString() + ":")) {
                  String regionName = trackedRegion.split(":")[1];
                  boolean stillInRegion = false;

                  for (ProtectedRegion regionx : regions) {
                     if (regionx.getId().toLowerCase().equals(regionName)) {
                        stillInRegion = true;
                        break;
                     }
                  }

                  if (!stillInRegion) {
                     this.playerRegionTracker.remove(trackedRegion);
                  }
               }
            }
         }
      }
   }
}
