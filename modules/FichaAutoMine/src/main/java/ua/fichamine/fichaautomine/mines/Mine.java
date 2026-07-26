package ua.fichamine.fichaautomine.mines;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichaautomine.models.BlockChance;
import ua.fichamine.fichaautomine.models.Rarity;

public abstract class Mine {
   protected final JavaPlugin plugin;
   protected int taskId = -1;
   protected long nextUpdate;
   protected long updateInterval;
   protected String currentType;
   protected String nextType;
   protected String currentDisplayName;
   protected String nextDisplayName;
   protected Rarity currentRarity;
   protected Rarity nextRarity;

   public Mine(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
      this.selectNextTypeAndRarity();
      this.currentType = this.nextType;
      this.currentDisplayName = this.nextDisplayName;
      this.currentRarity = this.nextRarity;
      this.selectNextTypeAndRarity();
      if (this.currentRarity != null) {
         this.fillArea(this.currentRarity);
      }
   }

   public abstract void startTimer();

   public abstract void updateMine();

   public abstract void cancelTimer();

   public abstract void forceUpdate(String var1, boolean var2);

   public abstract void forceUpdate(String var1, String var2, boolean var3);

   public abstract void setNextUpdateTimer(int var1);

   public abstract String getConfigPath();

   protected abstract void fillArea(Rarity var1);

   protected abstract void selectNextTypeAndRarity();

   public void resetTimer() {
      this.cancelTimer();
      this.nextUpdate = System.currentTimeMillis() + this.updateInterval;
      this.taskId = this.plugin
         .getServer()
         .getScheduler()
         .scheduleSyncRepeatingTask(this.plugin, this::updateMine, this.updateInterval / 50L, this.updateInterval / 50L);
   }

   public long getRemainingTime() {
      return Math.max(0L, this.nextUpdate - System.currentTimeMillis());
   }

   public String getCurrentDisplayName() {
      return this.currentDisplayName;
   }

   public String getNextDisplayName() {
      return this.nextDisplayName;
   }

   protected Rarity selectRarity(String rarityKey) {
      ConfigurationSection raritiesSection = this.plugin.getConfig().getConfigurationSection(this.getConfigPath() + ".rarities");
      if (raritiesSection != null && raritiesSection.contains(rarityKey)) {
         ConfigurationSection raritySection = raritiesSection.getConfigurationSection(rarityKey);
         if (raritySection != null) {
            return new Rarity(
               rarityKey, raritySection.getString("displayname"), raritySection.getInt("chance"), this.parseBlocks(raritySection.getStringList("blocks"))
            );
         }
      }

      return null;
   }

   protected List<BlockChance> parseBlocks(List<String> blocksList) {
      List<BlockChance> blocks = new ArrayList<>();

      for (String blockStr : blocksList) {
         String[] parts = blockStr.split(":");
         if (parts.length == 2) {
            try {
               blocks.add(new BlockChance(parts[0], Double.parseDouble(parts[1])));
            } catch (NumberFormatException var7) {
            }
         }
      }

      return blocks;
   }
}
