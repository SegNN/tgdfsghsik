package ua.fichamine.fichamineskills.event;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichamineskills.bonus.BonusSystem;
import ua.fichamine.fichamineskills.config.ConfigManager;
import ua.fichamine.fichamineskills.database.DatabaseManager;
import ua.fichamine.fichamineskills.util.Logger;

public class BlockBreakListener implements Listener {
   private final JavaPlugin plugin;
   private final DatabaseManager databaseManager;
   private final ConfigManager configManager;
   private final Random random = new Random();
   private final ConcurrentHashMap<Material, Double> pointsCache = new ConcurrentHashMap<>();

   public BlockBreakListener(JavaPlugin plugin, DatabaseManager databaseManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
      this.configManager = configManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      try {
         Player player = event.getPlayer();
         Material blockType = event.getBlock().getType();
         if (!this.isBlockInProtectedRegion(event.getBlock().getLocation())) {
            return;
         }

         double basePoints = this.pointsCache.computeIfAbsent(blockType, k -> this.configManager.getPointsForBlock(k));
         if (basePoints <= 0.0) {
            return;
         }

         this.processBlockBreakAsync(player, blockType, basePoints);
      } catch (Exception var61) {
         Logger.severe("Ошибка при обработке добычи блока: " + var61.getMessage());
      }
   }

   private void processBlockBreakAsync(Player player, Material blockType, double basePoints) {
      CompletableFuture<Double> currentPointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
      currentPointsFuture.thenAccept(currentPoints -> {
         try {
            if (currentPoints == null) {
               currentPoints = 0.0;
            }

            double finalPoints = this.calculateFinalPoints(basePoints, currentPoints, player, blockType);
            double newPoints = currentPoints + finalPoints;
            CompletableFuture<Void> addPointsFuture = this.databaseManager.addPointsAsync(player.getUniqueId(), player.getName(), finalPoints);
            this.databaseManager.addTotalBlocksAsync(player.getUniqueId(), player.getName(), 1L);
            double oldPoints = currentPoints;
            addPointsFuture.thenAccept(v -> BonusSystem.checkForNewUnlocks(player, oldPoints, newPoints, this.databaseManager)).exceptionally(ex -> null);
         } catch (Exception var13) {
            Logger.severe("Ошибка при обработке очков для " + player.getName() + ": " + var13.getMessage());
         }
      }).exceptionally(e -> {
         Logger.severe("Ошибка при получении очков для " + player.getName() + ": " + e.getMessage());
         return null;
      });
   }

   private double calculateFinalPoints(double basePoints, double playerPoints, Player player, Material blockType) {
      double totalMultiplier = 1.0;
      double questExpBonus = BonusSystem.getBonusMultiplier(playerPoints, "quest_exp");
      if (questExpBonus > 0.0) {
         totalMultiplier += questExpBonus / 100.0;
      }

      double emeraldizationBonus = BonusSystem.getBonusMultiplier(playerPoints, "emeraldization");
      if (emeraldizationBonus > 0.0 && this.random.nextDouble() < emeraldizationBonus / 100.0 && blockType == Material.DIAMOND_ORE) {
         totalMultiplier += 0.5;
      }

      double luckBonus = BonusSystem.getBonusMultiplier(playerPoints, "luck");
      if (luckBonus > 0.0) {
         totalMultiplier += luckBonus / 100.0;
      }

      double rageBonus = BonusSystem.getBonusMultiplier(playerPoints, "rage");
      if (rageBonus > 0.0 && this.random.nextDouble() < rageBonus) {
         totalMultiplier *= 2.0;
      }

      return basePoints * totalMultiplier;
   }

   private boolean isBlockInProtectedRegion(Location location) {
      try {
         if (this.plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return true;
         } else if (location.getWorld() == null) {
            return false;
         } else if ("spawn".equalsIgnoreCase(location.getWorld().getName())) {
            return true;
         } else {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
               return false;
            } else {
               ProtectedRegion mineRegion = regionManager.getRegion("mine");
               ProtectedRegion mine2Region = regionManager.getRegion("mine2");
               if (mineRegion == null && mine2Region == null) {
                  return false;
               } else {
                  com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
                  boolean isInMine = mineRegion != null && mineRegion.contains(wgLocation.toVector().toBlockPoint());
                  boolean isInMine2 = mine2Region != null && mine2Region.contains(wgLocation.toVector().toBlockPoint());
                  return isInMine || isInMine2;
               }
            }
         }
      } catch (Exception var81) {
         return true;
      }
   }
}
