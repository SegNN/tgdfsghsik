package org.dimasik.eventgruz;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.eventmanager.Event;
import org.dimasik.eventmanager.EventManager;
import org.dimasik.lootmanager.LootAPI;

public class GruzManager implements Listener {
   public final EventGruz plugin;
   private EditSession editSession;
   public int gruzTime;
   public BukkitTask gruzTimer;
   private final Map<Location, Hologram> gruzHolograms = new HashMap<>();
   private final Map<Location, Inventory> gruzInventories = new HashMap<>();
   private Location currentGruzLocation = null;
   private GruzManager.GruzType currentGruzType = null;
   private GruzManager.GruzState state = null;
   private String displayName;
   private String color;
   public final List<Location> activeCubes = new ArrayList<>();

   public GruzManager(EventGruz plugin) {
      this.plugin = plugin;
   }

   public void start(GruzManager.GruzType type) {
      if (this.currentGruzLocation != null) {
         this.plugin.getLogger().warning("Груз уже существует. Удалите текущий груз перед созданием нового.");
      } else {
         this.currentGruzType = type;
         World world = Bukkit.getWorld("world");
         if (world != null) {
            Random random = new Random();
            int x = random.nextInt(5001) - 2500;
            int z = random.nextInt(5001) - 2500;
            int y = world.getHighestBlockYAt(x, z) + 1;
            this.state = GruzManager.GruzState.GRUZ_WAITING;
            Location loc = new Location(world, x, y, z);
            loc.add(0.5, 0.5, 0.5);
            if (type == GruzManager.GruzType.PEACEFUL) {
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&2&7&7&A&9&4◀&x&2&8&7&C&9&7&m &x&2&9&7&F&9&A&m &x&2&9&8&1&9&D&m &x&2&A&8&4&A&1&m &x&2&B&8&6&A&4&m &x&2&C&8&8&A&7&m &x&2&C&8&B&A&A&m &x&2&D&8&D&A&D&m &x&2&E&8&F&B&0&m &x&2&F&9&2&B&3&m &x&2&F&9&4&B&6&m &x&3&0&9&7&B&A&m &x&3&1&9&9&B&D&m &x&3&2&9&B&C&0&m &x&3&2&9&E&C&3&m &x&3&3&A&0&C&6&m &x&3&2&9&E&C&3&m &x&3&2&9&B&C&0&m &x&3&1&9&9&B&D&m &x&3&0&9&7&B&A&m &x&2&F&9&4&B&6&m &x&2&F&9&2&B&3&m &x&2&E&8&F&B&0&m &x&2&D&8&D&A&D&m &x&2&C&8&B&A&A&m &x&2&C&8&8&A&7&m &x&2&B&8&6&A&4&m &x&2&A&8&4&A&1&m &x&2&9&8&1&9&D&m &x&2&9&7&F&9&A&m &x&2&8&7&C&9&7&m &x&2&7&7&A&9&4▶"
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&2&7&7&A&9&4&n▍&f   &x&2&7&7&A&9&4&lМ&x&2&9&8&2&9&E&lи&x&2&C&8&9&A&8&lр&x&2&E&9&1&B&2&lн&x&3&1&9&8&B&C&lы&x&3&3&A&0&C&6&lй &x&2&E&9&1&B&2&lг&x&2&C&8&9&A&8&lр&x&2&9&8&2&9&E&lу&x&2&7&7&A&9&4&lз"
                  )
               );
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&2&7&7&A&9&4&n▍&f Появился на координатах"));
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&2&7&7&A&9&4▍&f X: &x&3&3&A&0&C&6"
                        + loc.getBlockX()
                        + " &fY: &x&3&3&A&0&C&6"
                        + loc.getBlockY()
                        + " &fZ: &x&3&3&A&0&C&6"
                        + loc.getBlockZ()
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&2&7&7&A&9&4◀&x&2&8&7&C&9&7&m &x&2&9&7&F&9&A&m &x&2&9&8&1&9&D&m &x&2&A&8&4&A&1&m &x&2&B&8&6&A&4&m &x&2&C&8&8&A&7&m &x&2&C&8&B&A&A&m &x&2&D&8&D&A&D&m &x&2&E&8&F&B&0&m &x&2&F&9&2&B&3&m &x&2&F&9&4&B&6&m &x&3&0&9&7&B&A&m &x&3&1&9&9&B&D&m &x&3&2&9&B&C&0&m &x&3&2&9&E&C&3&m &x&3&3&A&0&C&6&m &x&3&2&9&E&C&3&m &x&3&2&9&B&C&0&m &x&3&1&9&9&B&D&m &x&3&0&9&7&B&A&m &x&2&F&9&4&B&6&m &x&2&F&9&2&B&3&m &x&2&E&8&F&B&0&m &x&2&D&8&D&A&D&m &x&2&C&8&B&A&A&m &x&2&C&8&8&A&7&m &x&2&B&8&6&A&4&m &x&2&A&8&4&A&1&m &x&2&9&8&1&9&D&m &x&2&9&7&F&9&A&m &x&2&8&7&C&9&7&m &x&2&7&7&A&9&4▶"
                  )
               );
               this.displayName = "&x&2&7&7&A&9&4&lМ&x&2&9&8&2&9&E&lи&x&2&C&8&9&A&8&lр&x&2&E&9&1&B&2&lн&x&3&1&9&8&B&C&lы&x&3&3&A&0&C&6&lй &x&2&E&9&1&B&2&lг&x&2&C&8&9&A&8&lр&x&2&9&8&2&9&E&lу&x&2&7&7&A&9&4&lз";
               this.color = "&x&2&7&7&A&9&4";
            } else if (type == GruzManager.GruzType.EXPLOSIVE) {
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&B&1&5&2&0&8◀&x&B&4&5&3&0&8&m &x&B&7&5&5&0&9&m &x&B&9&5&6&0&9&m &x&B&C&5&8&0&A&m &x&B&F&5&9&0&A&m &x&C&2&5&B&0&A&m &x&C&5&5&C&0&B&m &x&C&8&5&E&0&B&m &x&C&A&5&F&0&B&m &x&C&D&6&0&0&C&m &x&D&0&6&2&0&C&m &x&D&3&6&3&0&D&m &x&D&6&6&5&0&D&m &x&D&8&6&6&0&D&m &x&D&B&6&8&0&E&m &x&D&E&6&9&0&E&m &x&D&B&6&8&0&E&m &x&D&8&6&6&0&D&m &x&D&6&6&5&0&D&m &x&D&3&6&3&0&D&m &x&D&0&6&2&0&C&m &x&C&D&6&0&0&C&m &x&C&A&5&F&0&B&m &x&C&8&5&E&0&B&m &x&C&5&5&C&0&B&m &x&C&2&5&B&0&A&m &x&B&F&5&9&0&A&m &x&B&C&5&8&0&A&m &x&B&9&5&6&0&9&m &x&B&7&5&5&0&9&m &x&B&4&5&3&0&8&m &x&B&1&5&2&0&8▶"
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&B&1&5&2&0&8&n▍&f   &x&B&1&5&2&0&8&lВ&x&B&9&5&6&0&9&lз&x&C&0&5&A&0&A&lр&x&C&8&5&E&0&B&lы&x&C&F&6&1&0&C&lв&x&D&7&6&5&0&D&lн&x&D&E&6&9&0&E&lо&x&D&7&6&5&0&D&lй &x&C&8&5&E&0&B&lг&x&C&0&5&A&0&A&lр&x&B&9&5&6&0&9&lу&x&B&1&5&2&0&8&lз"
                  )
               );
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&B&1&5&2&0&8&n▍&f Появился на координатах"));
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&B&1&5&2&0&8▍&f X: &x&D&E&6&9&0&E"
                        + loc.getBlockX()
                        + " &fY: &x&D&E&6&9&0&E"
                        + loc.getBlockY()
                        + " &fZ: &x&D&E&6&9&0&E"
                        + loc.getBlockZ()
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&B&1&5&2&0&8◀&x&B&4&5&3&0&8&m &x&B&7&5&5&0&9&m &x&B&9&5&6&0&9&m &x&B&C&5&8&0&A&m &x&B&F&5&9&0&A&m &x&C&2&5&B&0&A&m &x&C&5&5&C&0&B&m &x&C&8&5&E&0&B&m &x&C&A&5&F&0&B&m &x&C&D&6&0&0&C&m &x&D&0&6&2&0&C&m &x&D&3&6&3&0&D&m &x&D&6&6&5&0&D&m &x&D&8&6&6&0&D&m &x&D&B&6&8&0&E&m &x&D&E&6&9&0&E&m &x&D&B&6&8&0&E&m &x&D&8&6&6&0&D&m &x&D&6&6&5&0&D&m &x&D&3&6&3&0&D&m &x&D&0&6&2&0&C&m &x&C&D&6&0&0&C&m &x&C&A&5&F&0&B&m &x&C&8&5&E&0&B&m &x&C&5&5&C&0&B&m &x&C&2&5&B&0&A&m &x&B&F&5&9&0&A&m &x&B&C&5&8&0&A&m &x&B&9&5&6&0&9&m &x&B&7&5&5&0&9&m &x&B&4&5&3&0&8&m &x&B&1&5&2&0&8▶"
                  )
               );
               this.displayName = "&x&B&1&5&2&0&8&lВ&x&B&9&5&6&0&9&lз&x&C&0&5&A&0&A&lр&x&C&8&5&E&0&B&lы&x&C&F&6&1&0&C&lв&x&D&7&6&5&0&D&lн&x&D&E&6&9&0&E&lо&x&D&7&6&5&0&D&lй &x&C&8&5&E&0&B&lг&x&C&0&5&A&0&A&lр&x&B&9&5&6&0&9&lу&x&B&1&5&2&0&8&lз";
               this.color = "&x&B&1&5&2&0&8";
            } else if (type == GruzManager.GruzType.DEATH) {
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&9&0&0&0&0◀&x&5&B&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&E&0&0&0&0&m &x&6&0&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&E&0&0&0&0&m &x&7&0&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&3&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&0&0&0&0&0&m &x&6&E&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&0&0&0&0&0&m &x&5&E&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&B&0&0&0&0&m &x&5&9&0&0&0&0▶"
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&9&0&0&0&0&n▍&f   &x&5&9&0&0&0&0&lС&x&5&C&0&0&0&0&lм&x&6&0&0&0&0&0&lе&x&6&3&0&0&0&0&lр&x&6&6&0&0&0&0&lт&x&6&9&0&0&0&0&lе&x&6&D&0&0&0&0&lл&x&7&0&0&0&0&0&lь&x&7&3&0&0&0&0&lн&x&6&F&0&0&0&0&lы&x&6&C&0&0&0&0&lй &x&6&4&0&0&0&0&lг&x&6&0&0&0&0&0&lр&x&5&D&0&0&0&0&lу&x&5&9&0&0&0&0&lз ☠"
                  )
               );
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&x&5&9&0&0&0&0&n▍&f Появился на координатах"));
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&9&0&0&0&0▍&f X: &x&7&3&0&0&0&0"
                        + loc.getBlockX()
                        + " &fY: &x&7&3&0&0&0&0"
                        + loc.getBlockY()
                        + " &fZ: &x&7&3&0&0&0&0"
                        + loc.getBlockZ()
                  )
               );
               Bukkit.broadcastMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&5&9&0&0&0&0◀&x&5&B&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&E&0&0&0&0&m &x&6&0&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&E&0&0&0&0&m &x&7&0&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&3&0&0&0&0&m &x&7&1&0&0&0&0&m &x&7&0&0&0&0&0&m &x&6&E&0&0&0&0&m &x&6&D&0&0&0&0&m &x&6&B&0&0&0&0&m &x&6&9&0&0&0&0&m &x&6&8&0&0&0&0&m &x&6&6&0&0&0&0&m &x&6&4&0&0&0&0&m &x&6&3&0&0&0&0&m &x&6&1&0&0&0&0&m &x&6&0&0&0&0&0&m &x&5&E&0&0&0&0&m &x&5&C&0&0&0&0&m &x&5&B&0&0&0&0&m &x&5&9&0&0&0&0▶"
                  )
               );
               this.displayName = "&x&5&9&0&0&0&0&lС&x&5&C&0&0&0&0&lм&x&6&0&0&0&0&0&lе&x&6&3&0&0&0&0&lр&x&6&6&0&0&0&0&lт&x&6&9&0&0&0&0&lе&x&6&D&0&0&0&0&lл&x&7&0&0&0&0&0&lь&x&7&3&0&0&0&0&lн&x&6&F&0&0&0&0&lы&x&6&C&0&0&0&0&lй &x&6&4&0&0&0&0&lг&x&6&0&0&0&0&0&lр&x&5&D&0&0&0&0&lу&x&5&9&0&0&0&0&lз";
               this.color = "&x&5&9&0&0&0&0";
            }

            this.currentGruzLocation = loc;
            File schematicFile;
            if (this.currentGruzType == GruzManager.GruzType.DEATH) {
               schematicFile = new File("plugins/WorldEdit/schematics/gruz_deadly.schem");
            } else if (this.currentGruzType == GruzManager.GruzType.PEACEFUL) {
               schematicFile = new File("plugins/WorldEdit/schematics/gruz_peaceful.schem");
            } else {
               schematicFile = new File("plugins/WorldEdit/schematics/gruz_explosive.schem");
            }

            if (!schematicFile.exists()) {
               this.plugin.getLogger().warning("Схематика груза не найдена!");
            } else {
               ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
               if (format == null) {
                  this.plugin.getLogger().warning("Не удалось определить формат схематики!");
               } else {
                  try {
                     ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

                     try {
                        Clipboard clipboard = reader.read();
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        this.editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(loc.getWorld()), -1);
                        BlockVector3 to = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        Operation operation = holder.createPaste(this.editSession).to(to).ignoreAirBlocks(true).build();
                        Operations.complete(operation);
                        this.editSession.flushSession();
                     } catch (Throwable var16) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var15) {
                              var16.addSuppressed(var15);
                           }
                        }

                        throw var16;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                  } catch (Exception var17) {
                     this.plugin.getLogger().warning("Ошибка при вставке схематики: " + var17.getMessage());
                     return;
                  }

                  this.startParticleTask(loc.clone());
                  this.createRegion(loc.clone(), type);
                  this.createHologram(loc.clone(), type);
                  if (this.currentGruzType == GruzManager.GruzType.DEATH) {
                     this.createStan(loc, 50, 600L);
                     Location location = loc.clone().add(-3.0, -11.0, 3.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(4.0, -11.0, 3.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(4.0, -11.0, -4.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(-3.0, -11.0, -4.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                  } else {
                     Location location = this.currentGruzLocation.clone().add(0.0, -1.0, 4.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(-4.0, -1.0, 0.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(0.0, -1.0, -4.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                     location = this.currentGruzLocation.clone().add(4.0, -1.0, 0.0);
                     this.gruzInventories.put(location, Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName)));
                  }

                  this.gruzTimer = (new BukkitRunnable() {
                     int timeLeft = 300;

                     public void run() {
                        GruzManager.this.gruzTime = this.timeLeft;
                        if (GruzManager.this.state == GruzManager.GruzState.GRUZ_WAITING) {
                           if (this.timeLeft <= 0) {
                              this.timeLeft = 300;
                              GruzManager.this.openGruz();
                           } else {
                              this.timeLeft--;
                              GruzManager.this.updateHologram();
                           }

                           GruzManager.this.currentGruzLocation.getWorld().playSound(GruzManager.this.currentGruzLocation, Sound.UI_BUTTON_CLICK, 1.0F, 0.5F);
                        } else if (GruzManager.this.state == GruzManager.GruzState.GRUZ_OPENED) {
                           if (this.timeLeft <= 0) {
                              GruzManager.this.removeGruz();
                              this.cancel();
                              return;
                           }

                           this.timeLeft--;
                           GruzManager.this.updateHologram();
                           GruzManager.this.currentGruzLocation.getWorld().playSound(GruzManager.this.currentGruzLocation, Sound.UI_BUTTON_CLICK, 1.0F, 0.5F);
                        }
                     }
                  }).runTaskTimer(this.plugin, 0L, 20L);
               }
            }
         }
      }
   }

   public void setGruzTimer(final int newTime) {
      if (this.gruzTimer != null && !this.gruzTimer.isCancelled()) {
         this.gruzTimer.cancel();
      }

      this.gruzTimer = (new BukkitRunnable() {
         int timeLeft = newTime;

         public void run() {
            GruzManager.this.gruzTime = this.timeLeft;
            if (GruzManager.this.state == GruzManager.GruzState.GRUZ_WAITING) {
               if (this.timeLeft <= 0) {
                  this.timeLeft = 300;
                  GruzManager.this.openGruz();
               } else {
                  this.timeLeft--;
                  GruzManager.this.updateHologram();
               }

               GruzManager.this.currentGruzLocation.getWorld().playSound(GruzManager.this.currentGruzLocation, Sound.UI_BUTTON_CLICK, 1.0F, 0.5F);
            } else if (GruzManager.this.state == GruzManager.GruzState.GRUZ_OPENED) {
               if (this.timeLeft <= 0) {
                  GruzManager.this.removeGruz();
                  this.cancel();
               } else {
                  this.timeLeft--;
                  GruzManager.this.updateHologram();
               }

               GruzManager.this.currentGruzLocation.getWorld().playSound(GruzManager.this.currentGruzLocation, Sound.UI_BUTTON_CLICK, 1.0F, 0.5F);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block block = event.getClickedBlock();
         if (block == null) {
            return;
         }

         Location location = block.getLocation();

         for (Location loc : this.gruzInventories.keySet()) {
            if (loc.getWorld().equals(location.getWorld())
               && loc.getBlockX() == location.getBlockX()
               && loc.getBlockY() == location.getBlockY()
               && loc.getBlockZ() == location.getBlockZ()) {
               event.setCancelled(true);
               if (this.state == GruzManager.GruzState.GRUZ_OPENED) {
                  event.getPlayer().openInventory(this.gruzInventories.get(loc));
               }
               break;
            }
         }
      }
   }

   public void createStan(final Location center, int size, final long duration) {
      final World world = center.getWorld();
      final double minX = center.getX() - size / 2.0;
      final double minY = center.getY() - size / 2.0;
      final double minZ = center.getZ() - size / 2.0;
      final double maxX = center.getX() + size / 2.0;
      final double maxY = center.getY() + size / 2.0;
      final double maxZ = center.getZ() + size / 2.0;
      this.activeCubes.add(center);
      final double step = 0.75;
      Color color = Color.fromBGR(16759867);
      final DustOptions dustOptions = new DustOptions(color, 1.0F);
      DustOptions finalDustOptions = dustOptions;
      (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            public void run() {
               if (System.currentTimeMillis() - this.startTime >= duration * 1000L) {
                  GruzManager.this.activeCubes.remove(center);
                  this.cancel();
               } else {
                  for (Player player1 : world.getPlayers()) {
                     Location location = player1.getLocation();
                     if (location.getX() >= minX
                        && location.getX() <= maxX
                        && location.getY() >= minY
                        && location.getY() <= maxY
                        && location.getZ() >= minZ
                        && location.getZ() <= maxZ) {
                        player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0, false, false, true));
                     }
                  }

                  for (double x = minX; x <= maxX; x += step) {
                     world.spawnParticle(Particle.REDSTONE, x, minY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, x, minY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, x, maxY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, x, maxY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                  }

                  for (double y = minY; y <= maxY; y += step) {
                     world.spawnParticle(Particle.REDSTONE, minX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, minX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, maxX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, maxX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                  }

                  for (double z = minZ; z <= maxZ; z += step) {
                     world.spawnParticle(Particle.REDSTONE, minX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, minX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, maxX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                     world.spawnParticle(Particle.REDSTONE, maxX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true);
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
         && event.getItem() != null
         && event.getItem().getType() == Material.ENDER_PEARL
         && this.isInsideCube(event.getPlayer().getLocation())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onConsume(PlayerItemConsumeEvent event) {
      if (this.isInsideCube(event.getPlayer().getLocation()) && event.getItem() != null && event.getItem().getType() == Material.CHORUS_FRUIT) {
         event.setCancelled(true);
      }
   }

   private boolean isInsideCube(Location location) {
      for (Location cubeCenter : this.activeCubes) {
         double minX = cubeCenter.getX() - 25.0;
         double minY = cubeCenter.getY() - 25.0;
         double minZ = cubeCenter.getZ() - 25.0;
         double maxX = cubeCenter.getX() + 25.0;
         double maxY = cubeCenter.getY() + 25.0;
         double maxZ = cubeCenter.getZ() + 25.0;
         if (location.getX() >= minX
            && location.getX() <= maxX
            && location.getY() >= minY
            && location.getY() <= maxY
            && location.getZ() >= minZ
            && location.getZ() <= maxZ) {
            return true;
         }
      }

      return false;
   }

   public void openGruz() {
      this.state = GruzManager.GruzState.GRUZ_OPENED;
      if (this.currentGruzType == GruzManager.GruzType.EXPLOSIVE) {
         this.currentGruzLocation.getWorld().createExplosion(this.currentGruzLocation.clone().add(0.0, 2.0, 0.0), 2.0F, false, false);

         for (Player player : this.currentGruzLocation.getWorld().getPlayers()) {
            if (player.getLocation().distance(this.currentGruzLocation) <= 25.0) {
               double damage = 24.0 * player.getLocation().distance(this.currentGruzLocation);
               player.damage(damage);
            }
         }
      } else if (this.currentGruzType == GruzManager.GruzType.PEACEFUL) {
         for (Player playerx : this.currentGruzLocation.getWorld().getPlayers()) {
            if (playerx.getLocation().distance(this.currentGruzLocation) <= 10.0) {
               Location p = playerx.getLocation();
               Vector direction = new Location(p.getWorld(), p.getX(), p.getY(), p.getZ())
                  .toVector()
                  .subtract(this.currentGruzLocation.clone().subtract(0.0, 4.0, 0.0).toVector())
                  .normalize()
                  .multiply(1.5);
               playerx.setVelocity(direction);
            }
         }
      } else {
         for (Player playerxx : this.currentGruzLocation.getWorld().getPlayers()) {
            if (playerxx.getLocation().distance(this.currentGruzLocation.clone().subtract(0.0, 11.0, 0.0)) <= 10.0) {
               Location p = playerxx.getLocation();
               Vector direction = new Location(p.getWorld(), p.getX(), p.getY(), p.getZ())
                  .toVector()
                  .subtract(this.currentGruzLocation.clone().subtract(0.0, 15.0, 0.0).toVector())
                  .normalize()
                  .multiply(1.5);
               playerxx.setVelocity(direction);
            }
         }
      }

      if (this.currentGruzType == GruzManager.GruzType.DEATH) {
         Location location = this.currentGruzLocation.clone().add(-3.0, -11.0, 3.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(4.0, -11.0, 3.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(4.0, -11.0, -4.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(-3.0, -11.0, -4.0);
         this.createInventory(location);
      } else {
         Location location = this.currentGruzLocation.clone().add(0.0, -1.0, 4.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(-4.0, -1.0, 0.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(0.0, -1.0, -4.0);
         this.createInventory(location);
         location = this.currentGruzLocation.clone().add(4.0, -1.0, 0.0);
         this.createInventory(location);
      }
   }

   @EventHandler
   public void onItemInteract(InventoryClickEvent event) {
      if (this.displayName != null) {
         if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.displayName))
            && event.getCurrentItem() != null
            && event.getCurrentItem().getType() != Material.AIR) {
            if (event.getWhoClicked().getCooldown(Material.BEDROCK) == 0) {
               event.getWhoClicked().setCooldown(Material.BEDROCK, 4);
            } else {
               event.setCancelled(true);
               event.getWhoClicked().sendActionBar(Component.text("Вы забираете предметы слишком быстро!"));
            }
         }
      }
   }

   private void createInventory(Location location) {
      final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', this.displayName));
      this.gruzInventories.put(location, inventory);
      int random = new Random().nextInt(27) + 27;
      int level = 1;
      if (this.currentGruzType == GruzManager.GruzType.EXPLOSIVE) {
         level = 2;
      }

      if (this.currentGruzType == GruzManager.GruzType.DEATH) {
         level = 3;
      }

      final List<ItemStack> items = new LootAPI().getRandomItems(random, level);
      final List<Integer> emptySlots = new ArrayList<>();

      for (int i = 0; i < inventory.getSize(); i++) {
         if (inventory.getItem(i) == null) {
            emptySlots.add(i);
         }
      }

      if (items != null && !items.isEmpty()) {
         (new BukkitRunnable() {
            public void run() {
               Random randomx = new Random();
               if (!emptySlots.isEmpty() && !items.isEmpty()) {
                  int randomSlot = emptySlots.remove(randomx.nextInt(emptySlots.size()));
                  inventory.setItem(randomSlot, items.remove(0));
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 2L);
      }
   }

   private void createRegion(Location loc, GruzManager.GruzType type) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager != null) {
         loc.add(loc.getX() < 0.0 ? 1.0 : 0.0, 0.0, loc.getZ() < 0.0 ? 1.0 : 0.0);
         BlockVector3 pos1 = BlockVector3.at(loc.getBlockX() + 40, 0, loc.getBlockZ() + 40);
         BlockVector3 pos2 = BlockVector3.at(loc.getBlockX() - 40, 255, loc.getBlockZ() - 40);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion("gruz_event", pos1, pos2);
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         region.setFlag(Flags.USE, State.DENY);
         region.setFlag(Flags.PVP, type != GruzManager.GruzType.EXPLOSIVE && type != GruzManager.GruzType.DEATH ? State.DENY : State.ALLOW);
         region.setFlag(flag, State.DENY);
         region.setFlag(Flags.CHEST_ACCESS, State.DENY);
         region.setFlag(Flags.TNT, State.DENY);
         region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
         region.setFlag(Flags.FIRE_SPREAD, State.DENY);
         region.setFlag(Flags.LAVA_FIRE, State.DENY);
         regionManager.addRegion(region);
      }
   }

   private void startParticleTask(final Location loc) {
      (new BukkitRunnable() {
         public void run() {
            if (GruzManager.this.state == GruzManager.GruzState.GRUZ_NOT_SPAWNED) {
               this.cancel();
            } else {
               if (GruzManager.this.currentGruzType == GruzManager.GruzType.DEATH) {
                  Location location = loc.clone().add(-3.0, -11.5, 3.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(4.0, -11.5, 3.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(4.0, -11.5, -4.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(-3.0, -11.5, -4.0);
                  GruzManager.this.spawnParticle(location);
               } else {
                  Location location = loc.clone().add(0.0, -1.5, 4.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(-4.0, -1.5, 0.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(0.0, -1.5, -4.0);
                  GruzManager.this.spawnParticle(location);
                  location = loc.clone().add(4.0, -1.5, 0.0);
                  GruzManager.this.spawnParticle(location);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 4L);
   }

   private void spawnParticle(Location location) {
      double radiusBottom = 0.65;
      int particleCountBottom = 20;

      for (int i = 0; i < particleCountBottom; i++) {
         double angle = (Math.PI * 2) * i / particleCountBottom;
         double x = location.getX() + radiusBottom * Math.cos(angle);
         double z = location.getZ() + radiusBottom * Math.sin(angle);
         location.getWorld().spawnParticle(Particle.SMOKE_LARGE, x, location.getY(), z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      double radiusTop = 0.5;
      int particleCountTop = 20;

      for (int i = 0; i < particleCountTop; i++) {
         double angle = (Math.PI * 2) * i / particleCountTop;
         double x = location.getX() + radiusTop * Math.cos(angle);
         double z = location.getZ() + radiusTop * Math.sin(angle);
         location.getWorld().spawnParticle(Particle.FLAME, x, location.getY() + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   public void removeGruz() {
      EventManager.getInstance().events.remove("gruz");
      this.state = GruzManager.GruzState.GRUZ_NOT_SPAWNED;
      if (this.editSession != null) {
         this.editSession.undo(this.editSession);
      }

      if (this.currentGruzLocation != null) {
         RegionManager regionManager = WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(this.currentGruzLocation.getWorld()));
         if (regionManager != null) {
            regionManager.removeRegion("gruz_event");
         }
      }

      for (Inventory inventory : this.gruzInventories.values()) {
         for (HumanEntity humanEntity : new ArrayList<>(inventory.getViewers())) {
            humanEntity.closeInventory();
         }
      }

      this.gruzInventories.clear();

      for (Hologram hologram : new ArrayList<>(this.gruzHolograms.values())) {
         DHAPI.removeHologram(hologram.getName());
      }

      this.gruzHolograms.clear();
      if (this.gruzTimer != null && !this.gruzTimer.isCancelled()) {
         this.gruzTimer.cancel();
      }

      this.currentGruzLocation = null;
      this.currentGruzType = null;
   }

   private void createHologram(Location loc, GruzManager.GruzType type) {
      if (this.currentGruzType == GruzManager.GruzType.DEATH) {
         Location location = loc.clone().add(-3.0, -9.5, 3.0);
         String hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         Hologram hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(4.0, -9.5, 3.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(4.0, -9.5, -4.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(-3.0, -9.5, -4.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
      } else {
         Location location = loc.clone().add(0.0, 0.5, 4.0);
         String hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         Hologram hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(-4.0, 0.5, 0.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(0.0, 0.5, -4.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
         location = loc.clone().add(4.0, 0.5, 0.0);
         hologramName = "gruz_hologram_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
         hologram = DHAPI.createHologram(hologramName, location);
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.displayName));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + "5 мин., 0 сек."));
         this.gruzHolograms.put(hologram.getLocation(), hologram);
      }

      EventManager.getInstance()
         .events
         .put(
            "gruz",
            new Event(
               this.displayName,
               this.color
                  + "▶ "
                  + this.displayName
                  + "&f находится на координатах "
                  + this.color
                  + this.currentGruzLocation.getBlockX()
                  + " "
                  + this.currentGruzLocation.getBlockY()
                  + " "
                  + this.currentGruzLocation.getBlockZ()
                  + " &fи взорвется через "
                  + this.color
                  + "5 мин., 0 сек."
            )
         );
   }

   public void updateHologram() {
      Iterator<Hologram> iterator = this.gruzHolograms.values().iterator();
      if (this.state == GruzManager.GruzState.GRUZ_WAITING) {
         int min = this.gruzTime / 60;
         int sec = this.gruzTime % 60;

         while (iterator.hasNext()) {
            Hologram hologram = iterator.next();
            DHAPI.setHologramLine(hologram, 0, ChatColor.translateAlternateColorCodes('&', this.displayName));
            DHAPI.setHologramLine(
               hologram, 1, ChatColor.translateAlternateColorCodes('&', "Будет &aоткрыт &fчерез " + this.color + min + " мин., " + sec + " сек.")
            );
         }

         EventManager.getInstance()
            .events
            .put(
               "gruz",
               new Event(
                  this.displayName,
                  this.color
                     + "▶ "
                     + this.displayName
                     + "&f находится на координатах "
                     + this.color
                     + this.currentGruzLocation.getBlockX()
                     + " "
                     + this.currentGruzLocation.getBlockY()
                     + " "
                     + this.currentGruzLocation.getBlockZ()
                     + " &fи взорвется через "
                     + this.color
                     + min
                     + " мин., "
                     + sec
                     + " сек."
               )
            );
      } else if (this.state == GruzManager.GruzState.GRUZ_OPENED) {
         int min = this.gruzTime / 60;
         int sec = this.gruzTime % 60;

         while (iterator.hasNext()) {
            Hologram hologram = iterator.next();
            DHAPI.setHologramLine(hologram, 0, ChatColor.translateAlternateColorCodes('&', this.displayName));
            DHAPI.setHologramLine(
               hologram, 1, ChatColor.translateAlternateColorCodes('&', "Будет &cзакрыт &fчерез " + this.color + min + " мин., " + sec + " сек.")
            );
         }

         EventManager.getInstance()
            .events
            .put(
               "gruz",
               new Event(
                  this.displayName,
                  this.color
                     + "▶ "
                     + this.displayName
                     + "&f находится на координатах "
                     + this.color
                     + this.currentGruzLocation.getBlockX()
                     + " "
                     + this.currentGruzLocation.getBlockY()
                     + " "
                     + this.currentGruzLocation.getBlockZ()
                     + " &fи будет удален через "
                     + this.color
                     + min
                     + " мин., "
                     + sec
                     + " сек."
               )
            );
      } else {
         while (iterator.hasNext()) {
            Hologram hologram = iterator.next();
            DHAPI.removeHologram(hologram.getName());
            iterator.remove();
         }
      }
   }

   private static enum GruzState {
      GRUZ_OPENED,
      GRUZ_NOT_SPAWNED,
      GRUZ_WAITING;

      private GruzState() {
      }
   }

   public static enum GruzType {
      EXPLOSIVE,
      PEACEFUL,
      DEATH;

      private GruzType() {
      }
   }
}
