package org.dimasik.mantiitems;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ExplosiveTrap {
   private final MantiItems plugin;
   private final Location location;
   private final Player player;
   private final boolean onlyAir;
   private final boolean push;
   private final String schematicName;
   private String regionName;
   private List<BlockState> resetList = new ArrayList<>();

   public ExplosiveTrap(MantiItems plugin, Location location, Player player, String schematicName, Boolean push, Boolean onlyAir) {
      this.plugin = plugin;
      this.location = location;
      this.player = player;
      this.schematicName = schematicName;
      this.onlyAir = onlyAir;
      this.push = push;
      this.spawn();
   }

   private void spawn() {
      this.regionName = "explosivetrap_" + this.location.getBlockX() + "_" + this.location.getBlockY() + "_" + this.location.getBlockZ();
      this.location.clone().subtract(0.0, 1.0, 0.0).createExplosion(2.0F, false, false);
      this.rg(this.location);

      for (Player player : this.location.getWorld().getPlayers()) {
         if (player != this.player && player.getLocation().toVector().distance(this.location.toVector()) <= 5.0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false, false));
         }
      }

      this.plugin.createStan(this.player, this.location, 30, 15L);
      this.placeSchematicBlocks(this.loadSchematic(this.location, this.schematicName), this.regionName);
      (new BukkitRunnable() {
         public void run() {
            ExplosiveTrap.this.removeWeb();
         }
      }).runTaskLater(this.plugin, 220L);
      (new BukkitRunnable() {
         public void run() {
            ExplosiveTrap.this.push();
         }
      }).runTaskLater(this.plugin, 220L);
      (new BukkitRunnable() {
         public void run() {
            ExplosiveTrap.this.push();
         }
      }).runTaskLater(this.plugin, 230L);
      (new BukkitRunnable() {
         public void run() {
            ExplosiveTrap.this.delete();
         }
      }).runTaskLater(this.plugin, 240L);
   }

   public void removeWeb() {
      for (BlockState state : this.resetList) {
         if (state.getLocation().getBlock().getType() == Material.COBWEB) {
            state.getLocation().getBlock().setType(Material.AIR);
         }
      }
   }

   public void push() {
      if (this.push) {
         for (Player player : this.location.getWorld().getPlayers()) {
            if (player.getLocation().toVector().distance(this.location.clone().subtract(0.0, 2.0, 0.0).toVector()) <= 5.0) {
               player.setVelocity(new Vector(0.0, 1.5, 0.0));
            }
         }
      }
   }

   public void delete() {
      Iterator<BlockState> iterator = this.resetList.iterator();

      while (iterator.hasNext()) {
         BlockState state = iterator.next();
         BlocksMemory.remove(state.getLocation());
         state.update(true);
         iterator.remove();
      }

      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(this.location.getWorld()));
      if (regionManager != null) {
         try {
            regionManager.removeRegion(this.regionName);
            regionManager.save();
         } catch (StorageException var4) {
            var4.printStackTrace();
         }
      }

      this.plugin.traps.remove(this);
   }

   private void rg(Location center) {
      World world = center.getWorld();
      if (world != null) {
         BlockVector3 min = BlockVector3.at(center.getX() - 6.0, center.getY() - 6.0, center.getZ() - 6.0);
         BlockVector3 max = BlockVector3.at(center.getX() + 6.0, center.getY() + 6.0, center.getZ() + 6.0);
         ProtectedRegion region = new ProtectedCuboidRegion(this.regionName, min, max);
         Flag<?> var15 = WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         Flag<?> physics = WorldGuard.getInstance().getFlagRegistry().get("physics");
         StateFlag var16 = (StateFlag)var15;
         StateFlag physicsFlag = (StateFlag)physics;
         region.setFlag(Flags.TNT, State.DENY);
         region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
         region.setFlag(Flags.CHEST_ACCESS, State.DENY);
         region.setFlag(Flags.USE, State.DENY);
         region.setFlag(Flags.INTERACT, State.DENY);
         region.setFlag(Flags.PVP, State.ALLOW);
         region.setFlag(var16, State.DENY);
         if (physicsFlag != null) {
            region.setFlag(physicsFlag, State.DENY);
         }

         region.setPriority(5);
         RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
         if (regionManager != null) {
            regionManager.addRegion(region);
         }
      }
   }

   private List<SchematicBlock> loadSchematic(Location origin, String schematicName) {
      List<SchematicBlock> blocks = new ArrayList<>();
      if (schematicName != null && !schematicName.equalsIgnoreCase("none")) {
         File schematicFile = new File("plugins/WorldEdit/schematics/" + schematicName + ".schem");
         if (!schematicFile.exists()) {
            Bukkit.getLogger().warning("Схематика " + schematicName + " не найдена!");
            return blocks;
         } else {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
               Bukkit.getLogger().warning("Не удалось определить формат схематики " + schematicName);
               return blocks;
            } else {
               try {
                  ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
                  Throwable var7 = null;

                  try {
                     Clipboard clipboard = reader.read();
                     BlockVector3 min = clipboard.getMinimumPoint();
                     BlockVector3 max = clipboard.getMaximumPoint();
                     BlockVector3 originPoint = clipboard.getOrigin();

                     for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                           for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                              BlockVector3 relativePos = BlockVector3.at(x, y, z);
                              com.sk89q.worldedit.world.block.BlockState blockState = clipboard.getBlock(relativePos);
                              int offsetX = x - originPoint.getBlockX();
                              int offsetY = y - originPoint.getBlockY();
                              int offsetZ = z - originPoint.getBlockZ();
                              Location targetLocation = origin.clone().add(offsetX, offsetY, offsetZ);
                              Material material = BukkitAdapter.adapt(blockState.getBlockType());
                              BlockData blockData = BukkitAdapter.adapt(blockState);
                              blocks.add(new SchematicBlock(targetLocation, material, blockData));
                           }
                        }
                     }
                  } catch (Throwable var31) {
                     var7 = var31;
                     throw var31;
                  } finally {
                     if (reader != null) {
                        if (var7 != null) {
                           try {
                              reader.close();
                           } catch (Throwable var30) {
                              var7.addSuppressed(var30);
                           }
                        } else {
                           reader.close();
                        }
                     }
                  }
               } catch (Exception var33) {
                  Bukkit.getLogger().severe("Ошибка при загрузке схематики: " + var33.getMessage());
                  var33.printStackTrace();
               }

               return blocks;
            }
         }
      } else {
         return blocks;
      }
   }

   private void placeSchematicBlocks(List<SchematicBlock> blocks, String trapRegionId) {
      for (SchematicBlock block : blocks) {
         if (block.getMaterial() != Material.AIR
            && block.getMaterial() != Material.CAVE_AIR
            && block.getMaterial() != Material.VOID_AIR
            && block.getLocation().getBlock().getType() != Material.BEDROCK) {
            Location location = block.getLocation();
            if (this.shouldPlaceBlock(location, trapRegionId) && !BlocksMemory.contains(location)) {
               BlockState state = location.getBlock().getState();
               if (!(state instanceof InventoryHolder)
                  && (!this.onlyAir || state.getType() == Material.AIR || state.getType() == Material.CAVE_AIR || state.getType() == Material.VOID_AIR)) {
                  this.resetList.add(state);
                  Material material = block.getMaterial();
                  BlockData blockData = block.getBlockData();
                  BlockState blockState = location.getBlock().getState();
                  blockState.setType(material);
                  blockState.setBlockData(blockData);
                  blockState.update(true, false);
                  BlocksMemory.add(location);
               }
            }
         }
      }
   }

   private boolean shouldPlaceBlock(Location location, String trapRegionId) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      if (regions.isEmpty()) {
         return true;
      } else {
         ProtectedRegion highestPriorityRegion = null;
         boolean canPlaceBlock = true;

         for (ProtectedRegion region : regions) {
            if (!region.getId().equals(trapRegionId) && (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority())) {
               highestPriorityRegion = region;
            }
         }

         if (highestPriorityRegion != null) {
            StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
            if (flag != null) {
               canPlaceBlock = highestPriorityRegion.getFlag(flag) != State.DENY;
            }
         }

         return canPlaceBlock || location.getBlock().getType().isAir();
      }
   }
}
