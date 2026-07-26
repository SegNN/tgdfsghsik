package ru.kotikstasika.holyliteend.end.managers.bossbar;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.managers.EndEvent;
import ru.kotikstasika.holyliteend.utils.ColorUtils;

public class EndBossBar {
   private final HolyLiteEnd plugin;
   private BossBar endBossBar;
   private BukkitTask updateTask;
   private BukkitTask countdownTask;

   public EndBossBar(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public void startEndBossBar() {
      if (this.endBossBar != null) {
         this.endBossBar.removeAll();
      }

      long totalDuration = 5400L;
      long hours = totalDuration / 3600L;
      long minutes = totalDuration % 3600L / 60L;
      String timeText = String.format("Энд-мир будет закрыт через &#DFB000%dч. %dмин. 0сек", hours, minutes);
      this.endBossBar = Bukkit.createBossBar(ColorUtils.translateColors(timeText), BarColor.GREEN, BarStyle.SOLID, new BarFlag[0]);
      this.endBossBar.setProgress(1.0);

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.endBossBar.addPlayer(player);
      }

      this.startUpdateTask();
   }

   private void startUpdateTask() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
      }

      this.updateTask = (new BukkitRunnable() {
         public void run() {
            if (!EndEvent.isEventActive()) {
               EndBossBar.this.stopEndBossBar();
            } else {
               long remainingTime = EndBossBar.this.plugin.getEndEventManager().getRemainingTime();
               if (remainingTime <= 0L) {
                  EndBossBar.this.teleportAllPlayers();
                  EndBossBar.this.stopEndBossBar();
                  EndBossBar.this.plugin.getEndEventManager().startEvent();
               } else {
                  EndBossBar.this.updateBossBar(remainingTime);
                  if (remainingTime <= 10L && EndBossBar.this.countdownTask == null) {
                     EndBossBar.this.startCountdown();
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 10L, 10L);
   }

   public void updateBossBar(long remainingSeconds) {
      if (this.endBossBar != null) {
         long totalDuration = 5400L;
         long hours = remainingSeconds / 3600L;
         long minutes = remainingSeconds % 3600L / 60L;
         long seconds = remainingSeconds % 60L;
         String timeText = String.format("Энд-мир будет закрыт через &#DFB000%dч. %dмин. %dсек", hours, minutes, seconds);
         this.endBossBar.setTitle(ColorUtils.translateColors(timeText));
         double progress = Math.max(0.0, Math.min(1.0, (double)remainingSeconds / totalDuration));
         this.endBossBar.setProgress(progress);
         if (remainingSeconds > 3600L) {
            this.endBossBar.setColor(BarColor.GREEN);
         } else if (remainingSeconds > 1800L) {
            this.endBossBar.setColor(BarColor.YELLOW);
         } else {
            this.endBossBar.setColor(BarColor.RED);
         }
      }
   }

   private void startCountdown() {
      if (this.countdownTask != null) {
         this.countdownTask.cancel();
      }

      this.countdownTask = (new BukkitRunnable() {
            public void run() {
               long remainingTime = EndBossBar.this.plugin.getEndEventManager().getRemainingTime();

               for (Player player : Bukkit.getOnlinePlayers()) {
                  if (remainingTime == 1L) {
                     return;
                  }

                  player.sendMessage(
                     ColorUtils.translateColors("&x&f&b&6&f&0&0▶ &fСобытие «Захват Энда» завершено. Энд-мир был &x&f&b&6&f&0&0закрыт до следующего воскресенье")
                  );
                  String message = ColorUtils.translateColors("&#DFB000▶ &fДо закрытия энда осталось &#DFB000" + remainingTime + " секунд..");
                  player.sendTitle("", message, 0, 20, 0);
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
   }

   private void teleportAllPlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (player.getWorld().getName().equals("world_the_end")) {
            player.teleport(new Location(Bukkit.getWorld("spawn"), 0.0, 70.0, 0.0));
         }
      }
   }

   public void stopEndBossBar() {
      if (this.endBossBar != null) {
         this.endBossBar.removeAll();
         this.endBossBar = null;
      }

      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }

      if (this.countdownTask != null) {
         this.countdownTask.cancel();
         this.countdownTask = null;
      }
   }

   @Generated
   public BossBar getEndBossBar() {
      return this.endBossBar;
   }
}
