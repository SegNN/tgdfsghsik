package ru.kotikstasika.kotikdang.dang.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.DangType;
import ru.kotikstasika.kotikdang.util.LocationAndSpawnUtil;
import ru.kotikstasika.kotikdang.util.WorldEditUtils;

public class DangNoRg {
   private final KotikDang plugin;
   private final LootAPI lootAPI;
   private final WorldEditUtils worldEditUtils;
   private final Random random = new Random();
   private static final int SEARCH_RADIUS = 30;

   public DangNoRg(KotikDang plugin) {
      this.plugin = plugin;
      this.lootAPI = new LootAPI();
      this.worldEditUtils = new WorldEditUtils();
   }

   public boolean spawnNoRgDang(Location location, boolean spawnAtPlayer, String dangName) {
      ConfigurationSection noRgSection = this.plugin.getConfig().getConfigurationSection("dangs.no_rg");
      if (noRgSection != null && noRgSection.contains(dangName)) {
         ConfigurationSection dangConfig = noRgSection.getConfigurationSection(dangName);
         String schemName = dangConfig.getString("schematic");
         int lootLevel = dangConfig.getInt("lootlvl", 1);
         if (spawnAtPlayer) {
            Location spawnLoc = location.clone();
            spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc.getBlockX(), spawnLoc.getBlockZ()) + 1);
            this.pasteAndSetupDang(spawnLoc, schemName, lootLevel);
         } else {
            CompletableFuture<Location> locationFuture = LocationAndSpawnUtil.findSuitableLocation(location.getWorld(), DangType.NO_RG, 50);
            locationFuture.thenAccept(spawnLoc -> {
               if (spawnLoc != null) {
                  (new BukkitRunnable() {
                     public void run() {
                        DangNoRg.this.pasteAndSetupDang(spawnLoc, schemName, lootLevel);
                     }
                  }).runTask(this.plugin);
               }
            });
         }

         return spawnAtPlayer;
      } else {
         this.plugin.getLogger().warning("NoRg данж '" + dangName + "' не найден в конфиге!");
         return spawnAtPlayer;
      }
   }

   private void pasteAndSetupDang(Location spawnLoc, String schemName, int lootLevel) {
      if (this.worldEditUtils.pasteSchematic(spawnLoc, schemName)) {
         List<Location> chests = this.scanChests(spawnLoc);
         this.fillChests(chests, lootLevel);
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removePistonBlocks(spawnLoc, 40), 10L);
      }
   }

   private boolean isBiomeValid(Location loc, List<String> requiredBiomes) {
      if (requiredBiomes.contains("ALL")) {
         return true;
      } else {
         World world = loc.getWorld();
         Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

         for (String biomeName : requiredBiomes) {
            if (biome.toString().contains(biomeName)) {
               return true;
            }
         }

         return false;
      }
   }

   private void removePistonBlocks(Location center, int radius) {
      World world = center.getWorld();

      for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
         for (int y = 0; y <= 256; y++) {
            for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
               Block block = world.getBlockAt(x, y, z);
               if (block.getType() == Material.PISTON_HEAD || block.getType().toString().contains("PISTON")) {
                  block.setType(Material.AIR);
               }
            }
         }
      }
   }

   private List<Location> scanChests(Location center) {
      World world = center.getWorld();
      List<Location> chests = new ArrayList<>();

      for (int x = center.getBlockX() - 30; x <= center.getBlockX() + 30; x++) {
         for (int y = center.getBlockY() - 10; y <= center.getBlockY() + 20; y++) {
            for (int z = center.getBlockZ() - 30; z <= center.getBlockZ() + 30; z++) {
               Block block = world.getBlockAt(x, y, z);
               if (block.getType() == Material.CHEST) {
                  chests.add(block.getLocation());
               }
            }
         }
      }

      return chests;
   }

   private void fillChests(List<Location> chests, int lootLevel) {
      for (Location loc : chests) {
         Block block = loc.getBlock();
         if (block.getState() instanceof Chest) {
            Chest chest = (Chest)block.getState();
            this.fillChest(chest, lootLevel);
         }
      }
   }

   private void fillChest(Chest chest, int lootLevel) {
      chest.getInventory().clear();
      int itemCount = 5 + this.random.nextInt(10);

      for (ItemStack item : this.lootAPI.getRandomItems(itemCount, lootLevel)) {
         if (item != null) {
            int slot;
            do {
               slot = this.random.nextInt(chest.getInventory().getSize());
            } while (chest.getInventory().getItem(slot) != null);

            chest.getInventory().setItem(slot, item);
         }
      }
   }
}
