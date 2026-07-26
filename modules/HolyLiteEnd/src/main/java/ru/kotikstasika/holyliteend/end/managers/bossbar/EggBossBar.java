package ru.kotikstasika.holyliteend.end.managers.bossbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.utils.ColorUtils;

public class EggBossBar {
   private final HolyLiteEnd plugin;
   private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
   private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();

   public EggBossBar(HolyLiteEnd plugin) {
      this.plugin = plugin;
      this.startPeriodicCheck();
   }

   public void checkPlayerForEgg(Player player) {
      boolean hasEgg = false;
      int eggValue = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && EggItem.isDragonEgg(item)) {
            hasEgg = true;
            eggValue = EggItem.getEggValue(item);
            break;
         }
      }

      if (hasEgg) {
         this.showBossBar(player, eggValue);
      } else {
         this.hideBossBar(player);
      }
   }

   private void showBossBar(final Player player, int eggValue) {
      final UUID playerId = player.getUniqueId();
      if (this.playerBossBars.containsKey(playerId)) {
         BossBar bossBar = this.playerBossBars.get(playerId);
         bossBar.setTitle(
            ColorUtils.translateColors(
               "&x&9&F&0&0&F&FО&x&9&D&0&0&F&Aс&x&9&B&0&1&F&6т&x&9&9&0&1&F&1о&x&9&7&0&2&E&Dр&x&9&5&0&2&E&8о&x&9&3&0&2&E&4ж&x&9&1&0&3&D&Fн&x&8&F&0&3&D&Bо&x&8&D&0&3&D&6! &x&6&A&2&0&A&FЯ&x&7&4&2&3&B&Fй&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа!"
            )
         );
         bossBar.setProgress(1.0);
      } else {
         BossBar bossBar = Bukkit.createBossBar(
            ColorUtils.translateColors(
               "&x&9&F&0&0&F&FО&x&9&D&0&0&F&Aс&x&9&B&0&1&F&6т&x&9&9&0&1&F&1о&x&9&7&0&2&E&Dр&x&9&5&0&2&E&8о&x&9&3&0&2&E&4ж&x&9&1&0&3&D&Fн&x&8&F&0&3&D&Bо&x&8&D&0&3&D&6! &x&6&A&2&0&A&FЯ&x&7&4&2&3&B&Fй&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа!"
            ),
            BarColor.PURPLE,
            BarStyle.SOLID,
            new BarFlag[0]
         );
         bossBar.setProgress(1.0);
         bossBar.addPlayer(player);
         this.playerBossBars.put(playerId, bossBar);
         BukkitTask updateTask = (new BukkitRunnable() {
               public void run() {
                  if (!player.isOnline()) {
                     EggBossBar.this.hideBossBar(player);
                  } else {
                     boolean hasEgg = false;

                     for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && EggItem.isDragonEgg(item)) {
                           hasEgg = true;
                           break;
                        }
                     }

                     if (hasEgg) {
                        BossBar currentBossBar = EggBossBar.this.playerBossBars.get(playerId);
                        if (currentBossBar != null) {
                           currentBossBar.setTitle(
                              ColorUtils.translateColors(
                                 "&x&9&F&0&0&F&FО&x&9&D&0&0&F&Aс&x&9&B&0&1&F&6т&x&9&9&0&1&F&1о&x&9&7&0&2&E&Dр&x&9&5&0&2&E&8о&x&9&3&0&2&E&4ж&x&9&1&0&3&D&Fн&x&8&F&0&3&D&Bо&x&8&D&0&3&D&6! &x&6&A&2&0&A&FЯ&x&7&4&2&3&B&Fй&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа!"
                              )
                           );
                           currentBossBar.setProgress(1.0);
                        }
                     } else {
                        EggBossBar.this.hideBossBar(player);
                     }
                  }
               }
            })
            .runTaskTimer(this.plugin, 100L, 100L);
         this.updateTasks.put(playerId, updateTask);
      }
   }

   public void hideBossBar(Player player) {
      UUID playerId = player.getUniqueId();
      BossBar bossBar = this.playerBossBars.remove(playerId);
      if (bossBar != null) {
         bossBar.removeAll();
      }

      BukkitTask updateTask = this.updateTasks.remove(playerId);
      if (updateTask != null) {
         updateTask.cancel();
      }
   }

   public void onPlayerQuit(Player player) {
      this.hideBossBar(player);
   }

   public void shutdown() {
      for (BossBar bossBar : this.playerBossBars.values()) {
         bossBar.removeAll();
      }

      this.playerBossBars.clear();

      for (BukkitTask task : this.updateTasks.values()) {
         task.cancel();
      }

      this.updateTasks.clear();
   }

   private void startPeriodicCheck() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : EggBossBar.this.plugin.getServer().getOnlinePlayers()) {
               EggBossBar.this.checkPlayerForEgg(player);
            }
         }
      }).runTaskTimer(this.plugin, 1L, 3L);
   }
}
