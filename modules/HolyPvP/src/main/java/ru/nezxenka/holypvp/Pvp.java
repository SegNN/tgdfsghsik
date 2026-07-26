package ru.nezxenka.holypvp;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Pvp {
   private final HolyPvP plugin;
   public final Player player1;
   public final Player player2;
   public final Boolean death;
   private final World world;
   private World deathWorld;
   public BukkitTask teleportTask;
   private BukkitTask pvpTask;
   public Location center;
   public Boolean canPvp;

   public Pvp(HolyPvP plugin, Player player1, Player player2, Boolean death) {
      this.plugin = plugin;
      this.player1 = player1;
      this.player2 = player2;
      this.death = death;
      plugin.databaseManager.deletePlayerProfile(player1.getName());
      plugin.databaseManager.deletePlayerProfile(player2.getName());
      this.world = Bukkit.getWorld(plugin.getConfig().getString("settings.settings.worlds.pvp", "world"));
      if (death) {
         this.deathWorld = Bukkit.getWorld(plugin.getConfig().getString("settings.settings.worlds.smertka", "deadly_fight"));
         if (this.deathWorld == null) {
            Bukkit.createWorld(new WorldCreator(plugin.getConfig().getString("settings.settings.worlds.smertka", "deadly_fight")));
            this.deathWorld = Bukkit.getWorld(plugin.getConfig().getString("settings.settings.worlds.smertka", "deadly_fight"));
         }
      }

      this.start();
   }

   private void startTimer() {
      (new BukkitRunnable() {
         int remain = 6;

         @Override
         public void run() {
            this.remain--;
            if (Pvp.this.player1 == null || Pvp.this.player2 == null) {
               this.cancel();
            }

            if (this.remain == 0) {
               this.cancel();
               Pvp.this.canPvp = true;
               Pvp.this.player1.clearTitle();
               Pvp.this.player2.clearTitle();
               Pvp.this.player2.playSound(Pvp.this.player1.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
               Pvp.this.player2.playSound(Pvp.this.player2.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
            } else {
               Pvp.this.player1.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + this.remain), "", 5, 30, 0);
               Pvp.this.player1.playSound(Pvp.this.player1.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
               Pvp.this.player2.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + this.remain), "", 5, 30, 0);
               Pvp.this.player2.playSound(Pvp.this.player2.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private void start() {
      this.canPvp = false;
      this.startTimer();
      if (this.death) {
         int x = new Random().nextInt(20001) - 10000;
         int z = new Random().nextInt(20001) - 10000;
         int y = this.deathWorld.getHighestBlockYAt(x, z);

         for (List biomes = this.plugin.getConfig().getStringList("settings.biomes.smertka");
            !biomes.contains(this.deathWorld.getBiome(x, y, z).name());
            y = this.deathWorld.getHighestBlockYAt(x, z)
         ) {
            x = new Random().nextInt(20001) - 10000;
            z = new Random().nextInt(20001) - 10000;
         }

         Location location1 = new Location(this.deathWorld, x, y + 1, z);
         int x1 = new Random().nextInt(21) - 10;
         int z1 = new Random().nextInt(21) - 10;
         int y1 = this.deathWorld.getHighestBlockYAt(x + x1, z + z1);
         Location location2 = new Location(this.deathWorld, x + x1, y1 + 1, z + z1);
         Vector direction1 = location2.toVector().subtract(location1.toVector()).normalize();
         location1.setDirection(direction1);
         Vector direction2 = location1.toVector().subtract(location2.toVector()).normalize();
         location2.setDirection(direction2);
         double centerX = (location1.getX() + location2.getX()) / 2.0;
         double centerY = (location1.getY() + location2.getY()) / 2.0;
         double centerZ = (location1.getZ() + location2.getZ()) / 2.0;
         this.center = new Location(this.deathWorld, (int)centerX, (int)centerY, (int)centerZ);
         this.player1.teleport(location1);
         this.player2.teleport(location2);
         this.plugin.sendWorldBorder(this.player1, this.center, 50);
         this.plugin.sendWorldBorder(this.player2, this.center, 50);
         this.pvpTask = (new BukkitRunnable() {
            @Override
            public void run() {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + Pvp.this.player1.getName() + " " + Pvp.this.player2.getName());
            }
         }).runTaskTimer(this.plugin, 0L, 300L);
      } else {
         int x = new Random().nextInt(1800) - 900;
         int z = new Random().nextInt(1800) - 900;
         int y = this.world.getHighestBlockYAt(x, z);

         for (List biomes = this.plugin.getConfig().getStringList("settings.biomes.pvp");
            !biomes.contains(this.world.getBiome(x, y, z).name());
            y = this.world.getHighestBlockYAt(x, z)
         ) {
            x = new Random().nextInt(1800) - 900;
            z = new Random().nextInt(1800) - 900;
         }

         Location location1 = new Location(this.world, x, y + 1, z);
         int x1 = new Random().nextInt(21) - 10;
         int z1 = new Random().nextInt(21) - 10;
         int y1 = this.world.getHighestBlockYAt(x + x1, z + z1);
         Location location2 = new Location(this.world, x + x1, y1 + 1, z + z1);
         Vector direction1 = location2.toVector().subtract(location1.toVector()).normalize();
         location1.setDirection(direction1);
         Vector direction2 = location1.toVector().subtract(location2.toVector()).normalize();
         location2.setDirection(direction2);
         this.player1.teleport(location1);
         this.player2.teleport(location2);
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " " + this.player2.getName());
      }
   }

   public void stopForce() {
      if (this.pvpTask != null && !this.pvpTask.isCancelled()) {
         this.pvpTask.cancel();
      }

      if (this.death) {
         this.plugin.clearWorldBorder(this.player1);
         this.plugin.clearWorldBorder(this.player2);
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " 0");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player2.getName() + " 0");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + this.player1.getName() + " -s");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + this.player2.getName() + " -s");
      } else {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " 0");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player2.getName() + " 0");
      }
   }

   public void onDeath(Player looser) {
      if (this.pvpTask != null && !this.pvpTask.isCancelled()) {
         this.pvpTask.cancel();
      }

      this.plugin.pvpHashMap.remove(this.player1);
      this.plugin.pvpHashMap.remove(this.player2);
      this.plugin.clearWorldBorder(this.player1);
      this.plugin.clearWorldBorder(this.player2);
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " 0");
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player2.getName() + " 0");
      final BossBar bossBar = Bukkit.createBossBar(
         ChatColor.translateAlternateColorCodes('&', "&x&0&0&8&F&B&9Телепортация через: 60 сек."), BarColor.BLUE, BarStyle.SOLID
      );
      if (looser == this.player1) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast " + this.translateFinish(this.player2.getName(), looser.getName()));
         this.plugin.pvpHashMap.remove(this.player1);
         if (this.death) {
            this.player2
               .sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     this.plugin
                        .getConfig()
                        .getString(
                           "messages.left-time", "&x&f&b&0&0&0&0▶ &fВы победили в &dсмертельной схватке&f. У вас есть &660 секунд&f, чтобы забрать ресурсы."
                        )
                  )
               );
            bossBar.addPlayer(this.player2);
            this.teleportTask = (new BukkitRunnable() {
                  int time = 0;
                  World prevWorld = Pvp.this.player2.getWorld();

                  @Override
                  public void run() {
                     if (this.time >= 60) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + Pvp.this.player2.getName() + " -s");
                        Pvp.this.plugin.pvpHashMap.remove(Pvp.this.player2);
                        bossBar.removeAll();
                        this.cancel();
                     } else if (this.prevWorld != Pvp.this.player2.getWorld()) {
                        Pvp.this.plugin.pvpHashMap.remove(Pvp.this.player2);
                        bossBar.removeAll();
                        this.cancel();
                     } else {
                        this.prevWorld = Pvp.this.player2.getWorld();
                        bossBar.setTitle(
                           ChatColor.translateAlternateColorCodes(
                              '&', "&x&0&0&8&F&B&9Победа! &fВы покинете арену через &x&0&0&8&F&B&9" + (60 - this.time) + " &fсекунд."
                           )
                        );
                        bossBar.setProgress(Math.max(Math.min((60.0 - this.time) / 60.0, 0.0), 1.0));
                        this.time++;
                     }
                  }
               })
               .runTaskTimer(this.plugin, 0L, 20L);
         }
      } else {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast " + this.translateFinish(this.player1.getName(), looser.getName()));
         this.plugin.pvpHashMap.remove(this.player2);
         if (this.death) {
            this.player1
               .sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     this.plugin
                        .getConfig()
                        .getString(
                           "messages.left-time", "&x&f&b&0&0&0&0▶ &fВы победили в &dсмертельной схватке&f. У вас есть &660 секунд&f, чтобы забрать ресурсы."
                        )
                  )
               );
            bossBar.addPlayer(this.player1);
            this.teleportTask = (new BukkitRunnable() {
                  int time = 0;
                  World prevWorld = Pvp.this.player1.getWorld();

                  @Override
                  public void run() {
                     if (this.time >= 60) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + Pvp.this.player1.getName() + " -s");
                        Pvp.this.plugin.pvpHashMap.remove(Pvp.this.player1);
                        bossBar.removeAll();
                        this.cancel();
                     } else if (this.prevWorld != Pvp.this.player1.getWorld()) {
                        Pvp.this.plugin.pvpHashMap.remove(Pvp.this.player1);
                        bossBar.removeAll();
                        this.cancel();
                     } else {
                        this.prevWorld = Pvp.this.player1.getWorld();
                        bossBar.setTitle(
                           ChatColor.translateAlternateColorCodes(
                              '&', "&x&0&0&8&F&B&9Победа! &fВы покинете арену через &x&0&0&8&F&B&9" + (60 - this.time) + " &fсекунд."
                           )
                        );
                        bossBar.setProgress(Math.max(Math.min((60.0 - this.time) / 60.0, 0.0), 1.0));
                        this.time++;
                     }
                  }
               })
               .runTaskTimer(this.plugin, 0L, 20L);
         }
      }
   }

   public String translateFinish(String winner, String looser) {
      List<String> list = this.plugin.getConfig().getStringList("messages.finish");
      String random = list.get(new Random().nextInt(list.size()));

      while (random.contains("%winner%")) {
         random = random.replace("%winner%", winner);
      }

      while (random.contains("%looser%")) {
         random = random.replace("%looser%", looser);
      }

      return random;
   }

   public void stopTp(Player player) {
      this.plugin.pvpHashMap.remove(this.player1);
      this.plugin.pvpHashMap.remove(this.player2);
      if (player == this.player1) {
         if (this.pvpTask != null && !this.pvpTask.isCancelled()) {
            this.pvpTask.cancel();
         }

         if (this.death) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player2.getName() + " 0");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + this.player2.getName() + " -s");
            this.player2
               .sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&0&0&8&F&B&9▶ &fВы были телепортированы на &x&0&0&8&F&B&9спавн&f, т.к. соперник &x&0&0&8&F&B&9покинул арену&f."
                  )
               );
         } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " 0");
         }
      } else {
         if (this.pvpTask != null && !this.pvpTask.isCancelled()) {
            this.pvpTask.cancel();
         }

         if (this.death) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player1.getName() + " 0");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + this.player1.getName() + " -s");
            this.player1
               .sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&0&0&8&F&B&9▶ &fВы были телепортированы на &x&0&0&8&F&B&9спавн&f, т.к. соперник &x&0&0&8&F&B&9покинул арену&f."
                  )
               );
         } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + this.player2.getName() + " 0");
         }
      }
   }
}
