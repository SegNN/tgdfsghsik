package ua.fichamine.fichaautomine.mines;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichaautomine.models.Rarity;

public class EndNetherMine extends Mine {
   public EndNetherMine(JavaPlugin plugin) {
      super(plugin);
   }

   @Override
   public void startTimer() {
      this.selectNextTypeAndRarity();
      this.updateMine();
      this.updateInterval = this.plugin.getConfig().getInt(this.getConfigPath() + ".update-time", 300) * 1000L;
      this.nextUpdate = System.currentTimeMillis() + this.updateInterval;
      this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateMine, this.updateInterval / 50L, this.updateInterval / 50L);
   }

   @Override
   public void cancelTimer() {
      if (this.taskId != -1) {
         Bukkit.getScheduler().cancelTask(this.taskId);
         this.taskId = -1;
      }
   }

   @Override
   public void forceUpdate(String rarity, boolean selectNext) {
      this.currentType = "end";
      String path = this.getConfigPath() + ".end.rarities." + rarity;
      this.currentRarity = this.selectRarityDirect(path);
      if (this.currentRarity != null) {
         this.currentDisplayName = this.currentRarity.getDisplayName();
         this.fillArea(this.currentRarity);
      }

      if (selectNext) {
         this.selectNextTypeAndRarity();
      }
   }

   @Override
   public void forceUpdate(String type, String rarity, boolean selectNext) {
      String rarityPath = type.equals("nether") ? "nether" : "end";
      String fullPath = this.getConfigPath() + "." + rarityPath + ".rarities." + rarity;
      this.currentRarity = this.selectRarityDirect(fullPath);
      if (this.currentRarity == null) {
         fullPath = this.getConfigPath() + "." + rarityPath + ".rarities.default";
         this.currentRarity = this.selectRarityDirect(fullPath);
      }

      if (this.currentRarity != null) {
         this.currentType = type;
         this.currentDisplayName = this.currentRarity.getDisplayName();
         this.fillArea(this.currentRarity);
      }

      if (selectNext) {
         this.selectNextTypeAndRarity();
      }
   }

   @Override
   public void setNextUpdateTimer(int seconds) {
      this.nextUpdate = System.currentTimeMillis() + seconds * 1000L;
      Bukkit.getScheduler().cancelTask(this.taskId);
      this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateMine, seconds * 20L, this.updateInterval / 50L);
   }

   @Override
   public String getConfigPath() {
      return "endnether";
   }

   @Override
   protected void selectNextTypeAndRarity() {
      String type = ThreadLocalRandom.current().nextBoolean() ? "end" : "nether";
      this.nextType = type;
      this.nextRarity = this.selectRandomRarity(type);
      this.nextDisplayName = this.nextRarity != null ? this.nextRarity.getDisplayName() : (type.equals("end") ? "Энд - Обычная" : "Ад - Обычная");
   }

   private Rarity selectRarityDirect(String fullPath) {
      ConfigurationSection raritySection = this.plugin.getConfig().getConfigurationSection(fullPath);
      if (raritySection != null) {
         String key = fullPath.substring(fullPath.lastIndexOf(46) + 1);
         return new Rarity(key, raritySection.getString("displayname"), raritySection.getInt("chance"), this.parseBlocks(raritySection.getStringList("blocks")));
      } else {
         return null;
      }
   }

   private Rarity selectRandomRarity(String type) {
      String path = this.getConfigPath() + "." + type + ".rarities";
      ConfigurationSection raritiesSection = this.plugin.getConfig().getConfigurationSection(path);
      if (raritiesSection == null) {
         return null;
      } else {
         List<Rarity> rarities = new ArrayList<>();

         for (String rarityKey : raritiesSection.getKeys(false)) {
            ConfigurationSection raritySection = raritiesSection.getConfigurationSection(rarityKey);
            if (raritySection != null) {
               rarities.add(
                  new Rarity(
                     rarityKey, raritySection.getString("displayname"), raritySection.getInt("chance"), this.parseBlocks(raritySection.getStringList("blocks"))
                  )
               );
            }
         }

         if (rarities.isEmpty()) {
            return null;
         } else {
            int totalChance = rarities.stream().mapToInt(Rarity::getChance).sum();
            int random = ThreadLocalRandom.current().nextInt(totalChance) + 1;
            int cumulativeChance = 0;

            for (Rarity rarity : rarities) {
               cumulativeChance += rarity.getChance();
               if (random <= cumulativeChance) {
                  return rarity;
               }
            }

            return rarities.get(0);
         }
      }
   }

   @Override
   public void updateMine() {
      this.currentType = this.nextType;
      this.currentDisplayName = this.nextDisplayName;
      this.currentRarity = this.nextRarity;
      this.selectNextTypeAndRarity();
      if (this.currentRarity != null) {
         this.fillArea(this.currentRarity);
      }

      this.nextUpdate = System.currentTimeMillis() + this.updateInterval;
   }

   @Override
   protected void fillArea(Rarity rarity) {
      ConfigurationSection pos1 = this.plugin.getConfig().getConfigurationSection(this.getConfigPath() + ".pos1");
      ConfigurationSection pos2 = this.plugin.getConfig().getConfigurationSection(this.getConfigPath() + ".pos2");
      if (pos1 != null && pos2 != null) {
         World world = Bukkit.getWorld(pos1.getString("world"));
         if (world != null) {
            int minX = Math.min(pos1.getInt("x"), pos2.getInt("x"));
            int minY = Math.min(pos1.getInt("y"), pos2.getInt("y"));
            int minZ = Math.min(pos1.getInt("z"), pos2.getInt("z"));
            int maxX = Math.max(pos1.getInt("x"), pos2.getInt("x"));
            int maxY = Math.max(pos1.getInt("y"), pos2.getInt("y"));
            int maxZ = Math.max(pos1.getInt("z"), pos2.getInt("z"));
            List<EndNetherMine.BlockData> blockUpdates = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
               for (int y = minY; y <= maxY; y++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     Material material = rarity.getRandomBlock();
                     if (material != null) {
                        blockUpdates.add(new EndNetherMine.BlockData(x, y, z, material));
                     }
                  }
               }
            }

            this.applyBlockUpdatesAsync(world, blockUpdates);
         }
      }
   }

   private void applyBlockUpdatesAsync(World world, List<EndNetherMine.BlockData> blockUpdates) {
      int blocksPerTick = 500;
      int totalBlocks = blockUpdates.size();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         int i = 0;

         while (i < totalBlocks) {
            int end = Math.min(i + blocksPerTick, totalBlocks);
            List<EndNetherMine.BlockData> batch = blockUpdates.subList(i, end);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               for (EndNetherMine.BlockData data : batch) {
                  Block block = world.getBlockAt(data.x, data.y, data.z);
                  block.setType(data.material, false);
               }
            });

            try {
               Thread.sleep(50L);
            } catch (InterruptedException var10) {
               var10.printStackTrace();
            }

            i += blocksPerTick;
         }
      });
   }

   @Override
   public void initialize() {
      this.nextType = ThreadLocalRandom.current().nextBoolean() ? "end" : "nether";
      this.nextRarity = this.selectRandomRarity(this.nextType);
      this.currentType = this.nextType;
      this.currentDisplayName = this.nextRarity != null ? this.nextRarity.getDisplayName() : (this.nextType.equals("end") ? "Энд - Обычная" : "Ад - Обычная");
      this.currentRarity = this.nextRarity;
      this.selectNextTypeAndRarity();
      if (this.currentRarity != null) {
         this.fillArea(this.currentRarity);
      } else {
         String defaultPath = this.getConfigPath() + "." + this.currentType + ".rarities.default";
         Rarity defaultRarity = this.selectRarityDirect(defaultPath);
         if (defaultRarity != null) {
            this.fillArea(defaultRarity);
         }
      }
   }

   private static class BlockData {
      final int x;
      final int y;
      final int z;
      final Material material;

      BlockData(int x, int y, int z, Material material) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.material = material;
      }
   }
}
