package org.dimasik.eventposilka;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import org.dimasik.lootmanager.LootAPI;

public class PosilkaManager implements Listener {
   private final EventPosilka plugin;
   public EditSession editSession;
   public Location posilkaLocation = null;
   public PosilkaManager.PosilkaType posilkaType;
   public Hologram posilkaHologram;
   public int posilkaTime = 180;
   public int breaks = 1;
   public BukkitTask posilkaTask;
   public PosilkaManager.PosilkaState state = PosilkaManager.PosilkaState.NOT_SPAWNED;
   public String displayName = "";
   public String color = "";

   public PosilkaManager(EventPosilka plugin) {
      this.plugin = plugin;
   }

   public void start(PosilkaManager.PosilkaType posilkaType) {
      if (this.state != PosilkaManager.PosilkaState.NOT_SPAWNED) {
         this.plugin.getLogger().info("Посылка уже создана!");
      } else {
         this.posilkaType = posilkaType;
         World world = Bukkit.getWorld("world");
         if (world == null) {
            this.plugin.getLogger().warning("Мир world не найден!");
         } else {
            Random random = new Random();
            int x = random.nextInt(5001) - 2500;
            int z = random.nextInt(5001) - 2500;
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x, y, z);
            loc.add(0.5, 8.5, 0.5);
            this.posilkaLocation = loc;
            File schematicFile;
            if (posilkaType == PosilkaManager.PosilkaType.DEFAULT) {
               schematicFile = new File("plugins/WorldEdit/schematics/posilka_default.schem");
            } else if (posilkaType == PosilkaManager.PosilkaType.EPIC) {
               schematicFile = new File("plugins/WorldEdit/schematics/posilka_epic.schem");
            } else {
               schematicFile = new File("plugins/WorldEdit/schematics/posilka_legendary.schem");
            }

            if (!schematicFile.exists()) {
               this.plugin.getLogger().warning("Схематика посылки не найдена!");
            } else {
               this.state = PosilkaManager.PosilkaState.WAITING;
               this.createRegion(loc);
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&2&4&2&4◀&x&F&F&2&3&2&3&m &x&F&F&2&2&2&2&m &x&F&F&2&0&2&0&m &x&F&F&1&F&1&F&m &x&F&F&1&E&1&E&m &x&F&F&1&D&1&D&m &x&F&F&1&B&1&B&m &x&F&F&1&A&1&A&m &x&F&F&1&9&1&9&m &x&F&F&1&8&1&8&m &x&F&F&1&6&1&6&m &x&F&F&1&5&1&5&m &x&F&F&1&4&1&4&m &x&F&F&1&3&1&3&m &x&F&F&1&1&1&1&m &x&F&F&1&0&1&0&m &x&F&F&1&1&1&1&m &x&F&F&1&3&1&3&m &x&F&F&1&4&1&4&m &x&F&F&1&5&1&5&m &x&F&F&1&6&1&6&m &x&F&F&1&8&1&8&m &x&F&F&1&9&1&9&m &x&F&F&1&A&1&A&m &x&F&F&1&B&1&B&m &x&F&F&1&D&1&D&m &x&F&F&1&E&1&E&m &x&F&F&1&F&1&F&m &x&F&F&2&0&2&0&m &x&F&F&2&2&2&2&m &x&F&F&2&3&2&3&m &x&F&F&2&4&2&4▶"
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&2&4&2&4&n▍&f   &x&F&F&2&4&2&4&lП&x&F&F&1&D&1&D&lо&x&F&F&1&7&1&7&lс&x&F&F&1&0&1&0&lы&x&F&F&1&7&1&7&lл&x&F&F&1&D&1&D&lк&x&F&F&2&4&2&4&lа"
                  )
               );
               if (posilkaType == PosilkaManager.PosilkaType.DEFAULT) {
                  Bukkit.broadcastMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&F&F&2&4&2&4&n▍&f Редкость: &x&5&9&C&8&F&F&lО&x&4&B&C&1&F&F&lб&x&3&D&B&A&F&F&lы&x&2&F&B&4&F&F&lч&x&2&0&A&D&F&F&lн&x&1&2&A&6&F&F&lа&x&0&4&9&F&F&F&lя"
                     )
                  );
               } else if (posilkaType == PosilkaManager.PosilkaType.EPIC) {
                  Bukkit.broadcastMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&F&F&2&4&2&4&n▍&f Редкость: &x&F&F&6&5&1&8&lР&x&E&E&5&F&1&3&lе&x&D&C&5&9&0&E&lд&x&C&B&5&3&0&A&lк&x&B&9&4&D&0&5&lа&x&A&8&4&7&0&0&lя"
                     )
                  );
               } else {
                  Bukkit.broadcastMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&F&F&2&4&2&4&n▍&f Редкость: &x&F&F&1&8&1&8&lЛ&x&F&E&1&6&1&A&lе&x&F&C&1&3&1&C&lг&x&F&B&1&1&1&E&lе&x&F&9&0&E&2&0&lн&x&F&8&0&C&2&3&lд&x&F&6&0&A&2&5&lа&x&F&5&0&7&2&7&lр&x&F&3&0&5&2&9&lн&x&F&2&0&2&2&B&lа&x&F&0&0&0&2&D&lя"
                     )
                  );
               }

               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&2&4&2&4&n▍&f Появилась на координатах"));
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&2&4&2&4▍&f X: &x&F&F&1&0&1&0"
                        + loc.getBlockX()
                        + " &fY: &x&F&F&1&0&1&0"
                        + loc.getBlockY()
                        + " &fZ: &x&F&F&1&0&1&0"
                        + loc.getBlockZ()
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&2&4&2&4◀&x&F&F&2&3&2&3&m &x&F&F&2&2&2&2&m &x&F&F&2&0&2&0&m &x&F&F&1&F&1&F&m &x&F&F&1&E&1&E&m &x&F&F&1&D&1&D&m &x&F&F&1&B&1&B&m &x&F&F&1&A&1&A&m &x&F&F&1&9&1&9&m &x&F&F&1&8&1&8&m &x&F&F&1&6&1&6&m &x&F&F&1&5&1&5&m &x&F&F&1&4&1&4&m &x&F&F&1&3&1&3&m &x&F&F&1&1&1&1&m &x&F&F&1&0&1&0&m &x&F&F&1&1&1&1&m &x&F&F&1&3&1&3&m &x&F&F&1&4&1&4&m &x&F&F&1&5&1&5&m &x&F&F&1&6&1&6&m &x&F&F&1&8&1&8&m &x&F&F&1&9&1&9&m &x&F&F&1&A&1&A&m &x&F&F&1&B&1&B&m &x&F&F&1&D&1&D&m &x&F&F&1&E&1&E&m &x&F&F&1&F&1&F&m &x&F&F&2&0&2&0&m &x&F&F&2&2&2&2&m &x&F&F&2&3&2&3&m &x&F&F&2&4&2&4▶"
                  )
               );
               this.displayName = "&x&F&F&2&4&2&4П&x&F&F&1&D&1&Dо&x&F&F&1&7&1&7с&x&F&F&1&0&1&0ы&x&F&F&1&7&1&7л&x&F&F&1&D&1&Dк&x&F&F&2&4&2&4а";
               this.color = "&x&F&F&2&4&2&4";
               ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
               if (format == null) {
                  this.plugin.getLogger().warning("Не удалось определить формат схематики!");
               } else {
                  try {
                     ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

                     try {
                        Clipboard clipboard = reader.read();
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1);
                        BlockVector3 to = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        Operation operation = holder.createPaste(editSession).to(to).ignoreAirBlocks(true).build();
                        Operations.complete(operation);
                        editSession.flushSession();
                        this.editSession = editSession;
                     } catch (Throwable var17) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var16) {
                              var17.addSuppressed(var16);
                           }
                        }

                        throw var17;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                  } catch (Exception var18) {
                     this.plugin.getLogger().warning("Ошибка при вставке схематики: " + var18.getMessage());
                     return;
                  }

                  this.createHologram();
                  this.updateTimer(180);
               }
            }
         }
      }
   }

   public void updateTimer(final int newTime) {
      if (this.posilkaTask != null && !this.posilkaTask.isCancelled()) {
         this.posilkaTask.cancel();
      }

      this.posilkaTask = (new BukkitRunnable() {
         int timer = newTime;

         public void run() {
            PosilkaManager.this.posilkaTime = this.timer;
            if (this.timer <= 0) {
               PosilkaManager.this.activatePosilka();
               this.cancel();
            } else {
               this.timer--;
               PosilkaManager.this.updateHologram();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void activatePosilka() {
      this.state = PosilkaManager.PosilkaState.BREAKABLE;
      this.breaks = new Random().nextInt(750) + 750;
      this.updateBreakHologram();
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.posilkaLocation != null) {
         Location location = event.getBlock().getLocation();
         if (location.getWorld() == this.posilkaLocation.getWorld()
            && location.getBlockX() == this.posilkaLocation.getBlockX()
            && location.getBlockY() == this.posilkaLocation.getBlockY()
            && location.getBlockZ() == this.posilkaLocation.getBlockZ()) {
            event.setCancelled(true);
            if (this.state == PosilkaManager.PosilkaState.BREAKABLE) {
               this.handleBreak(event.getPlayer());
            }
         }
      }
   }

   public void handlePosilkaOpen() {
      this.state = PosilkaManager.PosilkaState.OPENED;
      DHAPI.removeHologram(this.posilkaHologram.getName());
      this.posilkaHologram = null;
      int level = 1;
      if (this.posilkaType == PosilkaManager.PosilkaType.EPIC) {
         level = 2;
      } else if (this.posilkaType == PosilkaManager.PosilkaType.LEGENDARY) {
         level = 3;
      }

      final List<ItemStack> itemStacks = new LootAPI().getRandomItems(new Random().nextInt(30) + 30, level);
      this.posilkaTask = (new BukkitRunnable() {
         public void run() {
            if (itemStacks.isEmpty()) {
               PosilkaManager.this.startRemovingPosilka();
               this.cancel();
            } else {
               Location location = PosilkaManager.this.posilkaLocation.clone().add(-0.5, 0.5, -0.5);
               double x = new Random().nextDouble() - 0.5;
               double y = new Random().nextDouble() + 0.25;
               double z = new Random().nextDouble() - 0.5;
               location.getWorld().dropItemNaturally(location, itemStacks.remove(0)).setVelocity(new Vector(x, y, z));
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
   }

   public void startRemovingPosilka() {
      this.state = PosilkaManager.PosilkaState.REMOVING;
      this.posilkaTask = (new BukkitRunnable() {
         int timeLeft = 180;

         public void run() {
            if (this.timeLeft <= 0) {
               PosilkaManager.this.removePosilka();
               this.cancel();
            } else {
               this.timeLeft--;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void removePosilka() {
      if (this.state == PosilkaManager.PosilkaState.NOT_SPAWNED) {
         this.plugin.getLogger().info("Посылка не создана!");
      } else {
         this.state = PosilkaManager.PosilkaState.NOT_SPAWNED;
         if (this.editSession != null) {
            this.editSession.undo(this.editSession);
         }

         if (this.posilkaLocation != null) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(this.posilkaLocation.getWorld()));
            if (regionManager != null) {
               regionManager.removeRegion("posilka_event");
            }
         }

         if (this.posilkaHologram != null) {
            DHAPI.removeHologram(this.posilkaHologram.getName());
         }

         if (this.posilkaTask != null && !this.posilkaTask.isCancelled()) {
            this.posilkaTask.cancel();
            this.posilkaTask = null;
         }

         EventManager.getInstance().events.remove("posilka");
         this.posilkaLocation = null;
      }
   }

   public void handleBreak(Player player) {
      this.breaks--;
      if (this.breaks <= 0) {
         this.handlePosilkaOpen();
      } else {
         this.updateBreakHologram();
         int random = new Random().nextInt(100);
         if (random == 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " " + (new Random().nextInt(2) + 1));
         } else if (random <= 15) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + (new Random().nextInt(2000) + 1500));
         } else if (random <= 30) {
            player.giveExp(new Random().nextInt(25) + 10);
         } else if (random <= 45) {
            this.spawnEffect(player);
         } else if (random <= 50) {
            this.posilkaLocation.createExplosion(4.0F, false, false);
         }
      }
   }

   public void createHologram() {
      Hologram hologram = DHAPI.createHologram(
         "posilka_" + this.posilkaLocation.getBlockX() + "_" + this.posilkaLocation.getBlockY() + "_" + this.posilkaLocation.getBlockZ(),
         this.posilkaLocation.clone().add(0.0, 1.5, 0.0)
      );
      DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
      DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aдоступна &fчерез " + this.color + "3 мин., 0 сек."));
      this.posilkaHologram = hologram;
      EventManager.getInstance()
         .events
         .put(
            "posilka",
            new Event(
               this.displayName,
               this.color
                  + "▶ "
                  + this.displayName
                  + " &fна координатах "
                  + this.color
                  + this.posilkaLocation.getBlockX()
                  + " "
                  + this.posilkaLocation.getBlockY()
                  + " "
                  + this.posilkaLocation.getBlockZ()
                  + " &fбудет активирована через "
                  + this.color
                  + 3
                  + " мин., "
                  + 0
                  + " сек."
            )
         );
   }

   public void updateHologram() {
      int min = this.posilkaTime / 60;
      int sec = this.posilkaTime % 60;
      DHAPI.setHologramLine(
         this.posilkaHologram, 1, ChatColor.translateAlternateColorCodes('&', "Будет &aдоступна &fчерез " + this.color + min + " мин., " + sec + " сек.")
      );
      EventManager.getInstance()
         .events
         .put(
            "posilka",
            new Event(
               this.displayName,
               this.color
                  + "▶ "
                  + this.displayName
                  + " &fна координатах "
                  + this.color
                  + this.posilkaLocation.getBlockX()
                  + " "
                  + this.posilkaLocation.getBlockY()
                  + " "
                  + this.posilkaLocation.getBlockZ()
                  + " &fбудет активирована через "
                  + this.color
                  + min
                  + " мин., "
                  + sec
                  + " сек."
            )
         );
   }

   public void updateBreakHologram() {
      DHAPI.setHologramLine(this.posilkaHologram, 1, ChatColor.translateAlternateColorCodes('&', "Осталось " + this.color + this.breaks + " &fраз."));
      EventManager.getInstance()
         .events
         .put(
            "posilka",
            new Event(
               this.displayName,
               this.color
                  + "▶ "
                  + this.displayName
                  + " &fна координатах "
                  + this.color
                  + this.posilkaLocation.getBlockX()
                  + " "
                  + this.posilkaLocation.getBlockY()
                  + " "
                  + this.posilkaLocation.getBlockZ()
                  + "."
            )
         );
   }

   private void createRegion(Location loc) {
      loc = loc.clone();
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager != null) {
         loc.add(loc.getX() < 0.0 ? 1.0 : 0.0, 0.0, loc.getZ() < 0.0 ? 1.0 : 0.0);
         BlockVector3 pos1 = BlockVector3.at(loc.getX() + 40.0, 0.0, loc.getZ() + 40.0);
         BlockVector3 pos2 = BlockVector3.at(loc.getX() - 40.0, 255.0, loc.getZ() - 40.0);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion("posilka_event", pos1, pos2);
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         region.setFlag(Flags.USE, State.DENY);
         region.setFlag(Flags.PVP, State.ALLOW);
         region.setFlag(flag, State.DENY);
         region.setFlag(Flags.CHEST_ACCESS, State.DENY);
         region.setFlag(Flags.TNT, State.DENY);
         region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
         region.setFlag(Flags.FIRE_SPREAD, State.DENY);
         region.setFlag(Flags.LAVA_FIRE, State.DENY);
         regionManager.addRegion(region);
      }
   }

   private void spawnEffect(Player player) {
      int damage = new Random().nextInt(6);
      PotionEffectType potionEffectType = null;
      switch (damage) {
         case 0:
            potionEffectType = PotionEffectType.STRENGTH;
            break;
         case 1:
            potionEffectType = PotionEffectType.RESISTANCE;
            break;
         case 2:
            potionEffectType = PotionEffectType.HASTE;
            break;
         case 3:
            potionEffectType = PotionEffectType.HEALTH_BOOST;
            break;
         case 4:
            potionEffectType = PotionEffectType.REGENERATION;
            break;
         case 5:
            potionEffectType = PotionEffectType.SPEED;
      }

      player.addPotionEffect(new PotionEffect(potionEffectType, new Random().nextInt(201) + 100, new Random().nextInt(4)));
   }

   public static enum PosilkaState {
      NOT_SPAWNED,
      WAITING,
      BREAKABLE,
      OPENED,
      REMOVING;

      private PosilkaState() {
      }
   }

   public static enum PosilkaType {
      DEFAULT,
      EPIC,
      LEGENDARY;

      private PosilkaType() {
      }
   }
}
