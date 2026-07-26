package org.dimasik.eventcontainer;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;

public class ContainerManager implements Listener {
   private final EventContainer plugin;
   public EditSession editSession;
   public Location containerLocation = null;
   public ContainerManager.ContainerType containerType;
   public Hologram containerHologram;
   public int containerTime = 180;
   public int items = 1;
   public int maxitems = 1;
   public BukkitTask containerTask;
   public ContainerManager.ContainerState state = ContainerManager.ContainerState.NOT_SPAWNED;
   public String currentOpener = "";
   public HashMap<String, Integer> playerItems = new HashMap<>();
   public String displayName = "";
   public String color = "";

   public ContainerManager(EventContainer plugin) {
      this.plugin = plugin;
   }

   public void start(ContainerManager.ContainerType containerType) {
      if (this.state != ContainerManager.ContainerState.NOT_SPAWNED) {
         this.plugin.getLogger().info("Контейнер уже создан!");
      } else {
         this.playerItems = new HashMap<>();
         this.currentOpener = "";
         this.containerType = containerType;
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
            this.containerLocation = loc;
            this.maxitems = new Random().nextInt(32) + 33;
            this.items = this.maxitems;
            File schematicFile;
            if (containerType == ContainerManager.ContainerType.DEFAULT) {
               schematicFile = new File("plugins/WorldEdit/schematics/container_default.schem");
            } else {
               schematicFile = new File("plugins/WorldEdit/schematics/container_epic.schem");
            }

            if (!schematicFile.exists()) {
               this.plugin.getLogger().warning("Схематика контейнера не найдена!");
            } else {
               this.state = ContainerManager.ContainerState.WAITING;
               this.createRegion(loc);
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&2&4&D&F&F◀&x&5&3&4&F&F&F&m &x&5&3&5&1&F&F&m &x&5&4&5&3&F&F&m &x&5&5&5&5&F&F&m &x&5&5&5&7&F&F&m &x&5&6&5&9&F&F&m &x&5&7&5&B&F&F&m &x&5&8&5&D&F&F&m &x&5&8&5&F&F&E&m &x&5&9&6&1&F&E&m &x&5&A&6&3&F&E&m &x&5&A&6&5&F&E&m &x&5&B&6&7&F&E&m &x&5&C&6&9&F&E&m &x&5&C&6&B&F&E&m &x&5&D&6&D&F&E&m &x&5&C&6&B&F&E&m &x&5&C&6&9&F&E&m &x&5&B&6&7&F&E&m &x&5&A&6&5&F&E&m &x&5&A&6&3&F&E&m &x&5&9&6&1&F&E&m &x&5&8&5&F&F&E&m &x&5&8&5&D&F&F&m &x&5&7&5&B&F&F&m &x&5&6&5&9&F&F&m &x&5&5&5&7&F&F&m &x&5&5&5&5&F&F&m &x&5&4&5&3&F&F&m &x&5&3&5&1&F&F&m &x&5&3&4&F&F&F&m &x&5&2&4&D&F&F▶"
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&2&4&D&F&F&n▍&f   &x&5&2&4&D&F&F&lК&x&5&5&5&5&F&F&lо&x&5&8&5&D&F&F&lн&x&5&A&6&5&F&E&lт&x&5&D&6&D&F&E&lе&x&5&A&6&5&F&E&lй&x&5&8&5&D&F&F&lн&x&5&5&5&5&F&F&lе&x&5&2&4&D&F&F&lр"
                  )
               );
               if (containerType == ContainerManager.ContainerType.DEFAULT) {
                  Bukkit.broadcastMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&5&2&4&D&F&F&n▍&f Редкость: &x&2&6&D&C&F&F&lО&x&2&5&E&0&F&F&lб&x&2&4&E&3&F&F&lы&x&2&3&E&7&F&F&lч&x&2&2&E&B&F&F&lн&x&2&1&E&E&F&F&lы&x&2&0&F&2&F&F&lй"
                     )
                  );
               } else {
                  Bukkit.broadcastMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&5&2&4&D&F&F&n▍&f Редкость: &x&B&7&2&6&F&F&lЭ&x&B&9&2&5&F&F&lп&x&B&C&2&5&F&F&lи&x&B&E&2&4&F&F&lч&x&C&0&2&3&F&F&lе&x&C&2&2&2&F&F&lс&x&C&5&2&2&F&F&lк&x&C&7&2&1&F&F&lи&x&C&9&2&0&F&F&lй"
                     )
                  );
               }

               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&5&2&4&D&F&F&n▍&f Появилась на координатах"));
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&2&4&D&F&F▍&f X: &x&2&F&B&4&F&F"
                        + loc.getBlockX()
                        + " &fY: &x&2&F&B&4&F&F"
                        + loc.getBlockY()
                        + " &fZ: &x&2&F&B&4&F&F"
                        + loc.getBlockZ()
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&2&4&D&F&F◀&x&5&3&4&F&F&F&m &x&5&3&5&1&F&F&m &x&5&4&5&3&F&F&m &x&5&5&5&5&F&F&m &x&5&5&5&7&F&F&m &x&5&6&5&9&F&F&m &x&5&7&5&B&F&F&m &x&5&8&5&D&F&F&m &x&5&8&5&F&F&E&m &x&5&9&6&1&F&E&m &x&5&A&6&3&F&E&m &x&5&A&6&5&F&E&m &x&5&B&6&7&F&E&m &x&5&C&6&9&F&E&m &x&5&C&6&B&F&E&m &x&5&D&6&D&F&E&m &x&5&C&6&B&F&E&m &x&5&C&6&9&F&E&m &x&5&B&6&7&F&E&m &x&5&A&6&5&F&E&m &x&5&A&6&3&F&E&m &x&5&9&6&1&F&E&m &x&5&8&5&F&F&E&m &x&5&8&5&D&F&F&m &x&5&7&5&B&F&F&m &x&5&6&5&9&F&F&m &x&5&5&5&7&F&F&m &x&5&5&5&5&F&F&m &x&5&4&5&3&F&F&m &x&5&3&5&1&F&F&m &x&5&3&4&F&F&F&m &x&5&2&4&D&F&F▶"
                  )
               );
               this.displayName = "&x&5&2&4&D&F&FК&x&5&5&5&5&F&Fо&x&5&8&5&D&F&Fн&x&5&A&6&5&F&Eт&x&5&D&6&D&F&Eе&x&5&A&6&5&F&Eй&x&5&8&5&D&F&Fн&x&5&5&5&5&F&Fе&x&5&2&4&D&F&Fр";
               this.color = "&x&5&2&4&D&F&F";
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
      if (this.containerTask != null && !this.containerTask.isCancelled()) {
         this.containerTask.cancel();
      }

      this.containerTask = (new BukkitRunnable() {
         int timer = newTime;

         public void run() {
            ContainerManager.this.containerTime = this.timer;
            if (this.timer <= 0) {
               ContainerManager.this.handleContainerOpen();
               this.cancel();
            } else {
               this.timer--;
               ContainerManager.this.updateHologram();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void handleContainerOpen() {
      this.state = ContainerManager.ContainerState.OPENED;
      this.updateHologram();
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() == EventContainer.holder) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onClick(PlayerInteractEvent event) {
      if (event.getClickedBlock() != null && this.containerLocation != null) {
         Location location = event.getClickedBlock().getLocation();
         if (location.getBlockX() == this.containerLocation.getBlockX()
            && location.getBlockY() == this.containerLocation.getBlockY()
            && location.getBlockZ() == this.containerLocation.getBlockZ()
            && location.getWorld() == this.containerLocation.getWorld()) {
            event.setCancelled(true);
            if (this.state == ContainerManager.ContainerState.OPENED) {
               this.handleClick(event.getPlayer());
            }
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder() == EventContainer.holder) {
         this.currentOpener = "";
      }
   }

   public void handleClick(Player player) {
      if (this.currentOpener.equalsIgnoreCase("")) {
         this.currentOpener = player.getName();
         player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1200, 0, false, false, false));
         new ContainerMenu(this.plugin, player, this.containerType == ContainerManager.ContainerType.DEFAULT ? 1 : 2);
      } else {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.color + "▶ &fКонтейнер уже открывает " + this.color + this.currentOpener));
      }
   }

   public void startRemovingContainer() {
      this.state = ContainerManager.ContainerState.REMOVING;
      this.updateHologram();
      this.containerTime = 180;
      this.containerTask = (new BukkitRunnable() {
         public void run() {
            if (ContainerManager.this.containerTime <= 0) {
               ContainerManager.this.removeContainer();
               this.cancel();
            } else {
               ContainerManager.this.containerTime--;
               ContainerManager.this.updateHologram();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void removeContainer() {
      if (this.state == ContainerManager.ContainerState.NOT_SPAWNED) {
         this.plugin.getLogger().info("Посылка не создана!");
      } else {
         this.state = ContainerManager.ContainerState.NOT_SPAWNED;
         if (this.editSession != null) {
            this.editSession.undo(this.editSession);
         }

         if (this.containerLocation != null) {
            RegionManager regionManager = WorldGuard.getInstance()
               .getPlatform()
               .getRegionContainer()
               .get(BukkitAdapter.adapt(this.containerLocation.getWorld()));
            if (regionManager != null) {
               regionManager.removeRegion("container_event");
            }
         }

         if (this.containerHologram != null) {
            DHAPI.removeHologram(this.containerHologram.getName());
         }

         if (this.containerTask != null && !this.containerTask.isCancelled()) {
            this.containerTask.cancel();
            this.containerTask = null;
         }

         List<Entry<String, Integer>> playerItemsResult = new ArrayList<>(this.playerItems.entrySet());
         playerItemsResult.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
         Bukkit.broadcastMessage(
            ChatColor.translateAlternateColorCodes('&', "&x&5&2&4&D&F&F &n◢&f Ивент " + this.displayName + " &fбыл завершен. Топ игроков, которые")
         );
         Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&5&2&4&D&F&F ◤&f унесли &x&5&D&6&D&F&Eбольше всего предметов &fс ивента."));
         Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ""));

         for (int i = 0; i < 5; i++) {
            String string = ChatColor.translateAlternateColorCodes(
               '&', "&x&5&2&4&D&F&F &n▍&f  #%number%  &x&5&D&6&D&F&E%key% &f- &x&5&D&6&D&F&E%value% предметов"
            );
            if (i == 4) {
               string = ChatColor.translateAlternateColorCodes('&', "&x&5&2&4&D&F&F ▍&f  #%number%  &x&5&D&6&D&F&E%key% &f- &x&5&D&6&D&F&E%value% предметов");
            }

            if (playerItemsResult.size() > i) {
               Bukkit.broadcastMessage(
                  string.replace("%number%", String.valueOf(i + 1))
                     .replace("%key%", playerItemsResult.get(i).getKey())
                     .replace("%value%", String.valueOf(playerItemsResult.get(i).getValue()))
               );
            } else {
               Bukkit.broadcastMessage(string.replace("%number%", String.valueOf(i + 1)).replace("%key%", "---").replace("%value%", "---"));
            }
         }

         EventManager.getInstance().events.remove("container");
         this.containerLocation = null;
      }
   }

   public void createHologram() {
      Hologram hologram = DHAPI.createHologram(
         "container_" + this.containerLocation.getBlockX() + "_" + this.containerLocation.getBlockY() + "_" + this.containerLocation.getBlockZ(),
         this.containerLocation.clone().add(0.0, 1.5, 0.0)
      );
      DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
      DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aдоступен &fчерез " + this.color + "3 мин., 0 сек."));
      this.containerHologram = hologram;
      EventManager.getInstance()
         .events
         .put(
            "container",
            new Event(
               this.displayName,
               this.color
                  + "▶ "
                  + this.displayName
                  + " &fна координатах "
                  + this.color
                  + this.containerLocation.getBlockX()
                  + " "
                  + this.containerLocation.getBlockY()
                  + " "
                  + this.containerLocation.getBlockZ()
                  + " &fбудет доступен через "
                  + this.color
                  + "3 мин., 0 сек."
            )
         );
   }

   public void updateHologram() {
      if (this.state == ContainerManager.ContainerState.WAITING) {
         int min = this.containerTime / 60;
         int sec = this.containerTime % 60;
         DHAPI.setHologramLine(
            this.containerHologram, 1, ChatColor.translateAlternateColorCodes('&', "Будет &aдоступен &fчерез " + this.color + min + " мин., " + sec + " сек.")
         );
         EventManager.getInstance()
            .events
            .put(
               "container",
               new Event(
                  this.displayName,
                  this.color
                     + "▶ "
                     + this.displayName
                     + " &fна координатах "
                     + this.color
                     + this.containerLocation.getBlockX()
                     + " "
                     + this.containerLocation.getBlockY()
                     + " "
                     + this.containerLocation.getBlockZ()
                     + " &fбудет доступен через "
                     + this.color
                     + min
                     + " мин., "
                     + sec
                     + " сек."
               )
            );
      } else if (this.state == ContainerManager.ContainerState.OPENED) {
         DHAPI.setHologramLine(
            this.containerHologram,
            1,
            ChatColor.translateAlternateColorCodes('&', "Осталось предметов: " + this.color + "[ " + this.items + " / " + this.maxitems + " ]")
         );
         EventManager.getInstance()
            .events
            .put(
               "container",
               new Event(
                  this.displayName,
                  this.color
                     + "▶ "
                     + this.displayName
                     + " &fна координатах "
                     + this.color
                     + this.containerLocation.getBlockX()
                     + " "
                     + this.containerLocation.getBlockY()
                     + " "
                     + this.containerLocation.getBlockZ()
                     + " &fсодержит еще "
                     + this.color
                     + this.items
                     + " предмета"
               )
            );
      } else if (this.state == ContainerManager.ContainerState.REMOVING) {
         int min = this.containerTime / 60;
         int sec = this.containerTime % 60;
         DHAPI.setHologramLine(
            this.containerHologram, 1, ChatColor.translateAlternateColorCodes('&', "Будет &cудален &fчерез " + this.color + min + " мин., " + sec + " сек.")
         );
         EventManager.getInstance()
            .events
            .put(
               "container",
               new Event(
                  this.displayName,
                  this.color
                     + "▶ "
                     + this.displayName
                     + " &fна координатах "
                     + this.color
                     + this.containerLocation.getBlockX()
                     + " "
                     + this.containerLocation.getBlockY()
                     + " "
                     + this.containerLocation.getBlockZ()
                     + " &fбудет удален через "
                     + this.color
                     + min
                     + " мин., "
                     + sec
                     + " сек."
               )
            );
      } else if (this.state == ContainerManager.ContainerState.NOT_SPAWNED && this.containerHologram != null) {
         DHAPI.removeHologram(this.containerHologram.getName());
      }
   }

   private void createRegion(Location loc) {
      loc = loc.clone();
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager != null) {
         loc.add(loc.getX() < 0.0 ? 1.0 : 0.0, 0.0, loc.getZ() < 0.0 ? 1.0 : 0.0);
         BlockVector3 pos1 = BlockVector3.at(loc.getX() + 40.0, 0.0, loc.getZ() + 40.0);
         BlockVector3 pos2 = BlockVector3.at(loc.getX() - 40.0, 255.0, loc.getZ() - 40.0);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion("container_event", pos1, pos2);
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

   public static enum ContainerState {
      NOT_SPAWNED,
      WAITING,
      OPENED,
      REMOVING;

      private ContainerState() {
      }
   }

   public static enum ContainerType {
      DEFAULT,
      EPIC;

      private ContainerType() {
      }
   }
}
