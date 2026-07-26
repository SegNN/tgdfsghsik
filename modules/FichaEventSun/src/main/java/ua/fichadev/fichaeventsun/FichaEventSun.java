package ua.fichadev.fichaeventsun;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichaeventsun.command.LootCommand;
import ua.fichadev.fichaeventsun.config.Config;
import ua.fichadev.fichaeventsun.core.SunCore;
import ua.fichadev.fichaeventsun.listener.PlayerMoveListener;
import ua.fichadev.fichaeventsun.manager.CustomLootManager;
import ua.fichadev.fichaeventsun.manager.HologramManager;
import ua.fichadev.fichaeventsun.manager.PhaseManager;
import ua.fichadev.fichaeventsun.manager.RewardManager;
import ua.fichadev.fichaeventsun.task.AnchorAnimationTask;
import ua.fichadev.fichaeventsun.task.LootDropTask;
import ua.fichadev.fichaeventsun.task.RewardTask;

public final class FichaEventSun extends JavaPlugin {
   private static FichaEventSun instance;
   private Economy economy;
   private Config cfg;
   private PhaseManager phaseManager;
   private RewardManager rewardManager;
   private HologramManager hologramManager;
   private CustomLootManager customLootManager;
   private SunCore sunCore;

   public FichaEventSun() {
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
               Location loc = FichaEventSun.this.cfg.getCoreLocation();
               if (loc != null && loc.getWorld() != null) {
                  FichaEventSun.this.sunCore.initialize();
                  new RewardTask(FichaEventSun.this).start();
                  new LootDropTask(FichaEventSun.this).start();
                  new AnchorAnimationTask(FichaEventSun.this).start();
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
         this.getCommand("fichasun").setExecutor(lootCommand);
         this.getCommand("fichasun").setTabCompleter(lootCommand);
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

   public static FichaEventSun getInstance() {
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
