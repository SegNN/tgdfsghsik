package org.dimasik.eventzamok;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import org.jetbrains.annotations.NotNull;

public final class EventZamok extends JavaPlugin implements Listener {
   public HashMap<Location, ZamokShulker> shulkers = new HashMap<>();
   private boolean goldRushActive = false;
   private BukkitRunnable spawnTask;
   private BukkitRunnable goldRushTask;
   private File dataFile;
   private YamlConfiguration dataConfig;

   public EventZamok() {
   }

   public void onEnable() {
      this.dataFile = new File(this.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         this.saveResource("data.yml", false);
      }

      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.getCommand("zerofortress").setExecutor(new EventZamok.GruzCommand());
      this.loadShulkers();
      this.startSpawnTask();
   }

   public void onDisable() {
      if (this.spawnTask != null) {
         this.spawnTask.cancel();
      }

      if (this.goldRushTask != null) {
         this.goldRushTask.cancel();
      }

      this.saveShulkers();
      this.shulkers.values().forEach(ZamokShulker::remove);
   }

   private void saveShulkers() {
      this.dataConfig.set("locations", null);
      int i = 0;

      for (Location loc : this.shulkers.keySet()) {
         String path = "locations." + loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
         this.dataConfig.set(path, loc.serialize());
         i++;
      }

      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var5) {
         this.getLogger().severe("Не удалось сохранить данные шалкеров!");
         var5.printStackTrace();
      }
   }

   private void loadShulkers() {
      if (this.dataConfig.getConfigurationSection("locations") != null) {
         for (String key : this.dataConfig.getConfigurationSection("locations").getKeys(false)) {
            Location loc = Location.deserialize(this.dataConfig.getConfigurationSection("locations." + key).getValues(false));
            if (loc.getWorld() == null) {
               World world = Bukkit.getWorld(loc.getWorld().getName());
               if (world == null) {
                  this.getLogger().warning("Мир " + loc.getWorld().getName() + " не найден для шалкера!");
                  continue;
               }

               loc.setWorld(world);
            }

            ZamokShulker.ShulkerType type = this.getRandomType();
            ZamokShulker shulker = new ZamokShulker(loc, type);
            this.shulkers.put(loc, shulker);
         }
      }
   }

   private void startSpawnTask() {
      if (this.spawnTask != null) {
         this.spawnTask.cancel();
      }

      this.spawnTask = new BukkitRunnable() {
         public void run() {
            if (!EventZamok.this.goldRushActive) {
               EventZamok.this.spawnRandomShulkers();
            }
         }
      };
      this.spawnTask.runTaskTimer(this, 0L, 6000L);
   }

   private void spawnRandomShulkers() {
      if (!this.goldRushActive) {
         int count = new Random().nextInt(4) + 6;

         for (int i = 0; i < count; i++) {
            this.spawnRandomShulker();
         }
      }
   }

   private void spawnRandomShulker() {
      if (!this.shulkers.isEmpty()) {
         List<Location> locations = new ArrayList<>(this.shulkers.keySet());
         Location randomLocation = locations.get(new Random().nextInt(locations.size()));
         this.respawnShulker(randomLocation);
      }
   }

   private ZamokShulker.ShulkerType getRandomType() {
      int random = new Random().nextInt(100);
      if (random < 5) {
         return ZamokShulker.ShulkerType.LEGENDARY;
      } else {
         return random < 20 ? ZamokShulker.ShulkerType.EPIC : ZamokShulker.ShulkerType.DEFAULT;
      }
   }

   private void startGoldRush() {
      this.shulkers.values().forEach(ZamokShulker::remove);
      this.saveShulkers();
      Bukkit.broadcastMessage(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&E&4&C&F&0&0◀&x&E&6&D&1&0&0&m &x&E&7&D&2&0&0&m &x&E&9&D&4&0&0&m &x&E&B&D&5&0&0&m &x&E&C&D&7&0&0&m &x&E&E&D&8&0&0&m &x&F&0&D&A&0&0&m &x&F&2&D&C&0&0&m &x&F&3&D&D&0&0&m &x&F&5&D&F&0&0&m &x&F&7&E&0&0&0&m &x&F&8&E&2&0&0&m &x&F&A&E&3&0&0&m &x&F&C&E&5&0&0&m &x&F&D&E&6&0&0&m &x&F&F&E&8&0&0&m &x&F&D&E&6&0&0&m &x&F&C&E&5&0&0&m &x&F&A&E&3&0&0&m &x&F&8&E&2&0&0&m &x&F&7&E&0&0&0&m &x&F&5&D&F&0&0&m &x&F&3&D&D&0&0&m &x&F&2&D&C&0&0&m &x&F&0&D&A&0&0&m &x&E&E&D&8&0&0&m &x&E&C&D&7&0&0&m &x&E&B&D&5&0&0&m &x&E&9&D&4&0&0&m &x&E&7&D&2&0&0&m &x&E&6&D&1&0&0&m &x&E&4&C&F&0&0▶"
         )
      );
      Bukkit.broadcastMessage(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&E&4&C&F&0&0&n▍&f   &x&E&4&C&F&0&0&lЗ&x&E&7&D&2&0&0&lо&x&E&B&D&5&0&0&lл&x&E&E&D&8&0&0&lо&x&F&2&D&C&0&0&lт&x&F&5&D&F&0&0&lа&x&F&8&E&2&0&0&lя&x&F&C&E&5&0&0&l &x&F&F&E&8&0&0&lл&x&F&C&E&5&0&0&lи&x&F&8&E&2&0&0&lх&x&F&5&D&F&0&0&lо&x&F&2&D&C&0&0&lр&x&E&E&D&8&0&0&lа&x&E&B&D&5&0&0&lд&x&E&7&D&2&0&0&lк&x&E&4&C&F&0&0&lа"
         )
      );
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&E&4&C&F&0&0&n▍&f"));
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&E&4&C&F&0&0▍&f Сейчас проходит на &x&F&8&E&2&0&0замке."));
      Bukkit.broadcastMessage(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&E&4&C&F&0&0◀&x&E&6&D&1&0&0&m &x&E&7&D&2&0&0&m &x&E&9&D&4&0&0&m &x&E&B&D&5&0&0&m &x&E&C&D&7&0&0&m &x&E&E&D&8&0&0&m &x&F&0&D&A&0&0&m &x&F&2&D&C&0&0&m &x&F&3&D&D&0&0&m &x&F&5&D&F&0&0&m &x&F&7&E&0&0&0&m &x&F&8&E&2&0&0&m &x&F&A&E&3&0&0&m &x&F&C&E&5&0&0&m &x&F&D&E&6&0&0&m &x&F&F&E&8&0&0&m &x&F&D&E&6&0&0&m &x&F&C&E&5&0&0&m &x&F&A&E&3&0&0&m &x&F&8&E&2&0&0&m &x&F&7&E&0&0&0&m &x&F&5&D&F&0&0&m &x&F&3&D&D&0&0&m &x&F&2&D&C&0&0&m &x&F&0&D&A&0&0&m &x&E&E&D&8&0&0&m &x&E&C&D&7&0&0&m &x&E&B&D&5&0&0&m &x&E&9&D&4&0&0&m &x&E&7&D&2&0&0&m &x&E&6&D&1&0&0&m &x&E&4&C&F&0&0▶"
         )
      );
      String displayName = "&x&E&4&C&F&0&0З&x&E&7&D&2&0&0о&x&E&B&D&5&0&0л&x&E&E&D&8&0&0о&x&F&2&D&C&0&0т&x&F&5&D&F&0&0а&x&F&8&E&2&0&0я&x&F&C&E&5&0&0 &x&F&F&E&8&0&0л&x&F&C&E&5&0&0и&x&F&8&E&2&0&0х&x&F&5&D&F&0&0о&x&F&2&D&C&0&0р&x&E&E&D&8&0&0а&x&E&B&D&5&0&0д&x&E&7&D&2&0&0к&x&E&4&C&F&0&0а";
      String color = "&x&E&4&C&F&0&0";
      EventManager.getInstance().events.put("goldrush", new Event(displayName, color + "▶ " + displayName + " &fпроходит на координатах " + color + "0 0"));
      this.goldRushActive = true;
      int duration = new Random().nextInt(6000) + 12000;
      if (this.goldRushTask != null) {
         this.goldRushTask.cancel();
      }

      if (this.spawnTask != null) {
         this.spawnTask.cancel();
         this.spawnTask = null;
      }

      this.goldRushTask = new BukkitRunnable() {
         public void run() {
            EventZamok.this.spawnGoldRushShulkers();
         }
      };
      this.goldRushTask.runTaskTimer(this, 0L, 1800L);
      (new BukkitRunnable() {
         public void run() {
            EventZamok.this.endGoldRush();
         }
      }).runTaskLater(this, duration);
   }

   private void spawnGoldRushShulkers() {
      if (!this.shulkers.isEmpty()) {
         List<Location> locations = new ArrayList<>(this.shulkers.keySet());
         Collections.shuffle(locations);
         int count = locations.size();
         boolean redSpawned = false;

         for (int i = 0; i < count; i++) {
            Location loc = locations.get(i);
            this.shulkers.get(loc).remove();
            ZamokShulker.ShulkerType newType;
            if (!redSpawned && new Random().nextBoolean()) {
               redSpawned = true;
               newType = ZamokShulker.ShulkerType.GOLD_RUSH_RED;
            } else {
               newType = ZamokShulker.ShulkerType.GOLD_RUSH_YELLOW;
            }

            ZamokShulker newShulker = new ZamokShulker(loc, newType);
            this.shulkers.put(loc, newShulker);
            newShulker.spawn();
         }

         this.saveShulkers();
      }
   }

   private void endGoldRush() {
      this.goldRushActive = false;
      if (this.goldRushTask != null) {
         this.goldRushTask.cancel();
         this.goldRushTask = null;
      }

      this.shulkers.values().forEach(ZamokShulker::remove);
      this.saveShulkers();
      EventManager.getInstance().events.remove("goldrush");
      this.startSpawnTask();
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Location loc = event.getBlock().getLocation();
      if (this.shulkers.containsKey(loc)) {
         event.setCancelled(true);
         ZamokShulker shulker = this.shulkers.get(loc);
         shulker.hit();
         Player player = event.getPlayer();
         int remaining = shulker.getRequiredHits() - shulker.getHits();
         player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&x&F&C&2&2&2&2▶ &fУ шалкера осталось &b" + remaining + " прочности"));
         if (shulker.isBroken()) {
            shulker.dropItems();
            shulker.remove();
            this.saveShulkers();
         }
      }
   }

   private void respawnShulker(Location loc) {
      this.shulkers.get(loc).remove();
      ZamokShulker.ShulkerType newType;
      if (this.goldRushActive) {
         newType = new Random().nextBoolean() ? ZamokShulker.ShulkerType.GOLD_RUSH_RED : ZamokShulker.ShulkerType.GOLD_RUSH_YELLOW;
      } else {
         newType = this.getRandomType();
      }

      ZamokShulker newShulker = new ZamokShulker(loc, newType);
      this.shulkers.put(loc, newShulker);
      newShulker.spawn();
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block block = event.getClickedBlock();
         if (block != null) {
            Location loc = block.getLocation();
            if (this.shulkers.containsKey(loc)) {
               ZamokShulker shulker = this.shulkers.get(loc);
               Player player = event.getPlayer();
               int left = shulker.getRequiredHits() - shulker.getHits();
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', "&b▶ &fЧтобы получить лут из шалкера, необходимо его &bсломать еще " + left + " раз")
               );
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (this.shulkers.containsKey(event.getBlockAgainst().getLocation())) {
         event.setCancelled(true);
      }
   }

   public class GruzCommand implements CommandExecutor {
      public GruzCommand() {
      }

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Использование: /zerofortress [zamok/goldrush]");
            return true;
         } else {
            String var5 = args[0].toLowerCase();
            switch (var5) {
               case "zamok":
                  Player player = (Player)sender;
                  this.handleZamokCommand(player, args);
                  break;
               case "goldrush":
                  this.handleGoldRushCommand(sender, args);
                  break;
               default:
                  sender.sendMessage(ChatColor.GOLD + "Использование: /zerofortress [zamok/goldrush]");
            }

            return true;
         }
      }

      private void handleZamokCommand(Player player, String[] args) {
         if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "Использование: /zerofortress zamok [add/remove/spawn/despawn]");
         } else {
            String var3 = args[1].toLowerCase();
            switch (var3) {
               case "add":
                  Block targetBlock = player.getTargetBlockExact(10);
                  if (targetBlock == null) {
                     player.sendMessage(ChatColor.RED + "Вы не смотрите на блок!");
                     return;
                  }

                  Location loc = targetBlock.getLocation();
                  if (EventZamok.this.shulkers.containsKey(loc)) {
                     player.sendMessage(ChatColor.RED + "Шалкер уже существует в этом месте!");
                     return;
                  }

                  ZamokShulker.ShulkerType type = EventZamok.this.getRandomType();
                  ZamokShulker shulker = new ZamokShulker(loc, type);
                  EventZamok.this.shulkers.put(loc, shulker);
                  EventZamok.this.saveShulkers();
                  player.sendMessage(ChatColor.GREEN + "Шалкер " + type.name() + " добавлен!");
                  break;
               case "remove":
                  Block targetBlock1 = player.getTargetBlockExact(10);
                  if (targetBlock1 == null) {
                     player.sendMessage(ChatColor.RED + "Вы не смотрите на блок!");
                     return;
                  }

                  Location loc1 = targetBlock1.getLocation();
                  if (!EventZamok.this.shulkers.containsKey(loc1)) {
                     player.sendMessage(ChatColor.RED + "Шалкер не найден в этом месте!");
                     return;
                  }

                  EventZamok.this.shulkers.get(loc1).remove();
                  EventZamok.this.shulkers.remove(loc1);
                  EventZamok.this.saveShulkers();
                  player.sendMessage(ChatColor.GREEN + "Шалкер удален!");
                  break;
               case "spawn":
                  EventZamok.this.spawnRandomShulkers();
                  player.sendMessage(ChatColor.GREEN + "Случайные шалкеры заспавнены!");
                  break;
               case "despawn":
                  EventZamok.this.shulkers.values().forEach(ZamokShulker::remove);
                  EventZamok.this.saveShulkers();
                  player.sendMessage(ChatColor.GREEN + "Все шалкеры удалены!");
                  break;
               default:
                  player.sendMessage(ChatColor.GOLD + "Использование: /zerofortress zamok [add/remove/spawn/despawn]");
            }
         }
      }

      private void handleGoldRushCommand(CommandSender sender, String[] args) {
         if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + "Использование: /zerofortress goldrush [start/stop]");
         } else {
            String var3 = args[1].toLowerCase();
            switch (var3) {
               case "start":
                  if (EventZamok.this.goldRushActive) {
                     sender.sendMessage(ChatColor.RED + "Золотая лихорадка уже активна!");
                     return;
                  }

                  EventZamok.this.startGoldRush();
                  break;
               case "stop":
                  if (!EventZamok.this.goldRushActive) {
                     sender.sendMessage(ChatColor.RED + "Золотая лихорадка не активна!");
                     return;
                  }

                  EventZamok.this.endGoldRush();
                  break;
               default:
                  sender.sendMessage(ChatColor.GOLD + "Использование: /zerofortress goldrush [start/stop]");
            }
         }
      }
   }
}
