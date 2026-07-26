package ua.holydev.holyeventsun.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ua.holydev.holyeventsun.HolyEventSun;

public class Config {
   private final HolyEventSun plugin;

   public Config(HolyEventSun plugin) {
      this.plugin = plugin;
   }

   public Location getCoreLocation() {
      String worldName = this.plugin.getConfig().getString("core.world", "spawn");
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         return null;
      } else {
         double x = this.plugin.getConfig().getDouble("core.x", -121.0);
         double y = this.plugin.getConfig().getDouble("core.y", 48.0);
         double z = this.plugin.getConfig().getDouble("core.z", -28.0);
         return new Location(world, x, y, z);
      }
   }

   public int getRadius() {
      return this.plugin.getConfig().getInt("core.radius", 12);
   }

   public int getRewardIntervalTicks() {
      return this.plugin.getConfig().getInt("rewards.interval-seconds", 5) * 20;
   }

   public double getMoneyMin() {
      return this.plugin.getConfig().getDouble("rewards.money-min", 8500.0);
   }

   public double getMoneyMax() {
      return this.plugin.getConfig().getDouble("rewards.money-max", 9700.0);
   }

   public int getExpMin() {
      return this.plugin.getConfig().getInt("rewards.exp-min", 20);
   }

   public int getExpMax() {
      return this.plugin.getConfig().getInt("rewards.exp-max", 40);
   }

   public String getMoneyMessage() {
      return this.plugin.getConfig().getString("messages.money-received", "&6▶ &fВаш баланс пополнен на &6%amount% ¤");
   }

   public int getPhaseChangeTicks() {
      return this.plugin.getConfig().getInt("phase.change-minutes", 40) * 60 * 20;
   }

   public int getLootDropIntervalTicks() {
      return this.plugin.getConfig().getInt("loot.drop-interval-seconds", 16) * 20;
   }

   public String getLootSource() {
      return this.plugin.getConfig().getString("loot.source", "custom");
   }

   public int getLootLevel() {
      return this.plugin.getConfig().getInt("loot.lootmanager.level", 1);
   }

   public int getLootCount() {
      return this.plugin.getConfig().getInt("loot.lootmanager.count", 1);
   }

   public String getLootCategory() {
      return this.plugin.getConfig().getString("loot.lootmanager.category", "");
   }

   public int getCustomLootCount() {
      return this.plugin.getConfig().getInt("loot.custom.count", 1);
   }

   public String getEnterMessage() {
      return this.plugin
         .getConfig()
         .getString("messages.enter", "&b▶ &fВы вошли на территорию &6Ядра Солнца&f. Находитесь в радиусе &b%radius% блоков&f, чтобы получать %reward%&f.");
   }

   public String getHologramTitle() {
      return this.plugin.getConfig().getString("hologram.title", "&6Ядро Солнца");
   }

   public String getHologramPhaseLine() {
      return this.plugin.getConfig().getString("hologram.phase-line", "&fСейчас выдаёт: %phase%");
   }

   public String getHologramTimerLine() {
      return this.plugin.getConfig().getString("hologram.timer-line", "&fЛут через &x&0&0&D&0&F&8%time% сек.");
   }

   public double getHologramOffset() {
      return this.plugin.getConfig().getDouble("hologram.y-offset", 1.5);
   }
}
