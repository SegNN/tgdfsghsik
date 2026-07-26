package org.dimasik.holyfreeze;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Freeze {
   public Player checker;
   public Player suspect;
   public final HolyFreeze plugin;
   public Location suspectLocation;
   public BukkitTask messageTask;
   public BukkitTask titleTask;
   public BukkitTask teleportTask;
   public Location checkLocation;
   public Integer locIndex = null;
   public String checkerName;
   public String suspectName;
   public boolean teleport;

   public Freeze(HolyFreeze plugin, Player checker, Player suspect, boolean teleport) {
      this.plugin = plugin;
      this.checker = checker;
      this.suspect = suspect;
      this.checkerName = checker.getName();
      this.suspectName = suspect.getName();
      this.teleport = teleport;
      this.start();
   }

   public void start() {
      if (HolyFreeze.enableBlockJoin) {
         HolyFreeze.database.addPlayer(this.suspectName);
      }

      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.suspect.getName() + " 0");
      this.suspectLocation = this.suspect.getLocation();
      if (this.teleport) {
         World spawn = Bukkit.getWorld(HolyFreeze.config.getString("location.world", "spawn"));
         this.checkLocation = new Location(
            spawn,
            HolyFreeze.config.getDouble("location.x"),
            HolyFreeze.config.getDouble("location.y"),
            HolyFreeze.config.getDouble("location.z"),
            (float)HolyFreeze.config.getDouble("location.yaw"),
            (float)HolyFreeze.config.getDouble("location.pitch")
         );
         this.suspect.teleport(this.checkLocation);
      }

      this.messageTask = (new BukkitRunnable() {
            public void run() {
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&x&E&D&2&4&2&9▶ &fВы подозреваетесь в &x&E&D&2&4&2&9использовании читов, &fпоэтому были заморожены!"
                     )
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&6 Чтобы разморозить себя и не получить бан, &fу Вас есть &67 мин., &fчтобы отправить свой логин &x&E&D&2&4&2&9&nAnyDesk&f в чат &7(Кликабельно: &x&E&D&2&4&2&9www.anydesk.com/ru&7) &fи пройти проверку."
                     )
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9 В случае отказа/выхода/игнора &f- &x&E&D&2&4&2&9блокировка аккаунта")
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            }
         })
         .runTaskTimer(this.plugin, 0L, 600L);
      this.titleTask = (new BukkitRunnable() {
            public void run() {
               Freeze.this.suspect
                  .sendTitle(
                     ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9ПРОВЕРКА НА ЧИТЫ"),
                     ChatColor.translateAlternateColorCodes(
                        '&', "&x&E&D&2&4&2&9Следите за чатом и не выходите из игры! &fВсе инструкции &6отправлены &fвам в &eчат&f!"
                     )
                  );
            }
         })
         .runTaskTimer(this.plugin, 0L, 60L);
   }

   public void updatePlayer() {
      this.suspect = Bukkit.getPlayer(this.suspectName);
      if (this.messageTask != null) {
         this.messageTask.cancel();
      }

      if (this.titleTask != null) {
         this.titleTask.cancel();
      }

      this.messageTask = (new BukkitRunnable() {
            public void run() {
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&x&E&D&2&4&2&9▶ &fВы подозреваетесь в &x&E&D&2&4&2&9использовании читов, &fпоэтому были заморожены!"
                     )
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&6 Чтобы разморозить себя и не получить бан, &fу Вас есть &67 мин., &fчтобы отправить свой логин &x&E&D&2&4&2&9&nAnyDesk&f в чат &7(Кликабельно: &x&E&D&2&4&2&9www.anydesk.com/ru&7) &fи пройти проверку."
                     )
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Freeze.this.suspect
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9 В случае отказа/выхода/игнора &f- &x&E&D&2&4&2&9блокировка аккаунта")
                  );
               Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            }
         })
         .runTaskTimer(this.plugin, 0L, 600L);
      this.titleTask = (new BukkitRunnable() {
            public void run() {
               Freeze.this.suspect
                  .sendTitle(
                     ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9ПРОВЕРКА НА ЧИТЫ"),
                     ChatColor.translateAlternateColorCodes(
                        '&', "&x&E&D&2&4&2&9Следите за чатом и не выходите из игры! &fВсе инструкции &6отправлены &fвам в &eчат&f!"
                     )
                  );
            }
         })
         .runTaskTimer(this.plugin, 0L, 60L);
   }

   public void stop() {
      if (HolyFreeze.enableBlockJoin) {
         HolyFreeze.database.removePlayer(this.suspectName);
      }

      if (this.messageTask != null) {
         this.messageTask.cancel();
      }

      if (this.titleTask != null) {
         this.titleTask.cancel();
      }

      if (this.teleport) {
         this.teleportTask = (new BukkitRunnable() {
               int seconds = 11;

               public void run() {
                  this.seconds--;
                  if (Freeze.this.suspect == null) {
                     this.cancel();
                  } else if (this.seconds <= 0) {
                     Freeze.this.suspect.teleport(Freeze.this.suspectLocation);
                     Freeze.this.suspect.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a▶ &fТелепортация..."));
                     this.cancel();
                  } else if (Freeze.this.suspect.getLocation().getWorld() == Freeze.this.checkLocation.getWorld()
                     && !(Math.abs(Freeze.this.suspect.getLocation().getX() - Freeze.this.checkLocation.getX()) > 2.5)
                     && !(Math.abs(Freeze.this.suspect.getLocation().getY() - Freeze.this.checkLocation.getY()) > 2.5)
                     && !(Math.abs(Freeze.this.suspect.getLocation().getZ() - Freeze.this.checkLocation.getZ()) > 2.5)) {
                     Freeze.this.suspect
                        .sendTitle(
                           ChatColor.translateAlternateColorCodes('&', "Вы &aразморожены&f!"),
                           ChatColor.translateAlternateColorCodes(
                              '&', "&fТелепортация обратно через: &b" + this.seconds + "&f. Покиньте область проверки для &x&E&D&2&4&2&9отмены&f."
                           )
                        );
                  } else {
                     Freeze.this.suspect
                        .sendMessage(
                           ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9▶ &fВы покинули область, телепортация &x&E&D&2&4&2&9отменена&f!")
                        );
                     this.cancel();
                  }
               }
            })
            .runTaskTimer(this.plugin, 0L, 20L);
      }
   }

   public void stopKick(Boolean msg) {
      if (Bukkit.getPlayer(this.checkerName) != null) {
         this.checker = Bukkit.getPlayer(this.checkerName);
         if (msg) {
            this.checker
               .sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&E&D&2&4&2&9▶ &fПроверяемый покинул &x&E&D&2&4&2&9игру&f. Его нужно &x&E&D&2&4&2&9заблокировать&f."
                  )
               );
         }
      }

      if (this.messageTask != null) {
         this.messageTask.cancel();
      }

      if (this.titleTask != null) {
         this.titleTask.cancel();
      }
   }
}
