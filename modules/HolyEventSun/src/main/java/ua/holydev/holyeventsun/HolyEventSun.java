package ua.holydev.holyeventsun;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.holydev.holyeventsun.command.LootCommand;
import ua.holydev.holyeventsun.config.Config;
import ua.holydev.holyeventsun.core.SunCore;
import ua.holydev.holyeventsun.listener.PlayerMoveListener;
import ua.holydev.holyeventsun.manager.CustomLootManager;
import ua.holydev.holyeventsun.manager.HologramManager;
import ua.holydev.holyeventsun.manager.PhaseManager;
import ua.holydev.holyeventsun.manager.RewardManager;
import ua.holydev.holyeventsun.task.AnchorAnimationTask;
import ua.holydev.holyeventsun.task.LootDropTask;
import ua.holydev.holyeventsun.task.RewardTask;

public final class HolyEventSun extends JavaPlugin {
   private static HolyEventSun instance;
   private Economy economy;
   private Config cfg;
   private PhaseManager phaseManager;
   private RewardManager rewardManager;
   private HologramManager hologramManager;
   private CustomLootManager customLootManager;
   private SunCore sunCore;

   public HolyEventSun() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      if (!this.setupEconomy()) {
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.cfg = new Config(this);
         this.customLootManager = new CustomLootManager(this);
         this.phaseManager = new PhaseManager(this);
         this.rewardManager = new RewardManager(this);
         this.hologramManager = new HologramManager(this);
         this.sunCore = new SunCore(this);
         (new BukkitRunnable() {
            private int attempts = 0;

            public void run() {
               this.attempts++;
               Location loc = HolyEventSun.this.cfg.getCoreLocation();
               if (loc != null && loc.getWorld() != null) {
                  HolyEventSun.this.sunCore.initialize();
                  new RewardTask(HolyEventSun.this).start();
                  new LootDropTask(HolyEventSun.this).start();
                  new AnchorAnimationTask(HolyEventSun.this).start();
                  this.cancel();
               } else {
                  if (this.attempts >= 30) {
                     this.cancel();
                  }
               }
            }
         }).runTaskTimer(this, 20L, 20L);
         this.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
         LootCommand lootCommand = new LootCommand(this);
         this.getCommand("holysun").setExecutor(lootCommand);
         this.getCommand("holysun").setTabCompleter(lootCommand);
      }
   }

   public void onDisable() {
      if (this.sunCore != null) {
         this.sunCore.shutdown();
      }

      instance = null;
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy)rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   public static HolyEventSun getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public Config getCfg() {
      return this.cfg;
   }

   public PhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   public RewardManager getRewardManager() {
      return this.rewardManager;
   }

   public HologramManager getHologramManager() {
      return this.hologramManager;
   }

   public CustomLootManager getCustomLootManager() {
      return this.customLootManager;
   }

   public SunCore getSunCore() {
      return this.sunCore;
   }
}
