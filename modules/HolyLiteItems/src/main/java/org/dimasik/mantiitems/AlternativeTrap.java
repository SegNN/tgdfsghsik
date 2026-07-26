package org.dimasik.mantiitems;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AlternativeTrap {
   private final MantiItems plugin;
   private final Location location;
   private final Player player;
   private final boolean onlyAir;
   private final Material baseMaterial;
   private final int radius;
   private String regionName;
   private List<BlockState> resetList = new ArrayList<>();

   public AlternativeTrap(MantiItems plugin, Location location, Player player, Material baseMaterial, int radius, boolean onlyAir) {
      this.plugin = plugin;
      this.location = location;
      this.player = player;
      this.baseMaterial = baseMaterial;
      this.radius = radius;
      this.onlyAir = onlyAir;
      this.spawn();
   }

   private void spawn() {
      this.regionName = "alternativetrap_" + this.location.getBlockX() + "_" + this.location.getBlockY() + "_" + this.location.getBlockZ();
      this.location.clone().subtract(0.0, 1.0, 0.0).createExplosion(2.0F, false, false);
      this.placeBlocks(this.regionName);
      this.rg(this.location);

      for (Player player : this.location.getWorld().getPlayers()) {
         if (player != this.player && player.getLocation().toVector().distance(this.location.toVector()) <= 5.0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false, false));
         }
      }

      (new BukkitRunnable() {
         public void run() {
            AlternativeTrap.this.delete();
         }
      }).runTaskLater(this.plugin, 240L);
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
         BlockVector3 min = BlockVector3.at(center.getX() - this.radius, center.getY() - (this.radius - 1), center.getZ() - this.radius);
         BlockVector3 max = BlockVector3.at(center.getX() + this.radius, center.getY() + (this.radius + 1), center.getZ() + this.radius);
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

   private void placeBlocks(String trapRegionId) {
      Random random = new Random();
      Location center = this.location.clone();

      for (int x = -this.radius; x <= this.radius; x++) {
         for (int y = -this.radius; y <= this.radius; y++) {
            for (int z = -this.radius; z <= this.radius; z++) {
               Location loc = center.clone().add(x, y + 1, z);
               if (x != this.radius && x != -this.radius && y != this.radius && y != -this.radius && z != this.radius && z != -this.radius) {
                  Block block = loc.getBlock();
                  if (block.getType() != Material.BEDROCK && this.shouldPlaceBlock(loc, trapRegionId) && !BlocksMemory.contains(loc)) {
                     BlockState state = loc.getBlock().getState();
                     if (!(state instanceof InventoryHolder)
                        && (!this.onlyAir || state.getType() == Material.AIR || state.getType() == Material.CAVE_AIR || state.getType() == Material.VOID_AIR)) {
                        this.resetList.add(state);
                        loc.getBlock().setType(Material.AIR, false);
                        BlocksMemory.add(loc);
                     }
                  }
               } else {
                  Block block = loc.getBlock();
                  if (block.getType() != Material.BEDROCK && this.shouldPlaceBlock(loc, trapRegionId) && !BlocksMemory.contains(loc)) {
                     BlockState state = loc.getBlock().getState();
                     if (!(state instanceof InventoryHolder)
                        && (!this.onlyAir || state.getType() == Material.AIR || state.getType() == Material.CAVE_AIR || state.getType() == Material.VOID_AIR)) {
                        this.resetList.add(state);
                        boolean isHorizontalEdge = (y == this.radius || y == -this.radius)
                           && (x == this.radius || x == -this.radius || z == this.radius || z == -this.radius);
                        boolean isVerticalCornerEdge = y != this.radius
                           && y != -this.radius
                           && (x == this.radius || x == -this.radius)
                           && (z == this.radius || z == -this.radius);
                        boolean isTopBottomFace = (y == this.radius || y == -this.radius)
                           && x != this.radius
                           && x != -this.radius
                           && z != this.radius
                           && z != -this.radius;
                        boolean isSideFace = y != this.radius
                              && y != -this.radius
                              && (x == this.radius || x == -this.radius)
                              && z != this.radius
                              && z != -this.radius
                           || (z == this.radius || z == -this.radius) && x != this.radius && x != -this.radius;
                        BlockData blockData = null;
                        if (isHorizontalEdge) {
                           Material stairsMaterial = this.getStairsMaterial();
                           if (stairsMaterial != null) {
                              blockData = stairsMaterial.createBlockData();
                              if (blockData instanceof Stairs) {
                                 Stairs stairs = (Stairs)blockData;
                                 stairs.setFacing(this.getOppositeFacingFromCenter(loc, center));
                                 stairs.setShape(this.getStairShapeForCorner(loc, center));
                                 stairs.setHalf(y == this.radius ? Half.BOTTOM : Half.TOP);
                              }
                           }
                        } else if (isVerticalCornerEdge) {
                           if (random.nextDouble() < 0.66) {
                              Material wallMaterial = this.getWallMaterial();
                              if (wallMaterial != null) {
                                 blockData = wallMaterial.createBlockData();
                                 if (blockData instanceof Wall) {
                                    Wall wall = (Wall)blockData;
                                    wall.setUp(true);
                                    this.configureWallConnections(wall, loc);
                                 }
                              }
                           } else {
                              blockData = this.baseMaterial.createBlockData();
                           }
                        } else if (isTopBottomFace) {
                           if (random.nextDouble() < 0.25) {
                              Material slabMaterial = this.getSlabMaterial();
                              if (slabMaterial != null) {
                                 blockData = slabMaterial.createBlockData();
                                 if (blockData instanceof Slab) {
                                    Slab slab = (Slab)blockData;
                                    slab.setType(random.nextBoolean() ? Type.TOP : Type.BOTTOM);
                                 }
                              }
                           } else {
                              blockData = this.baseMaterial.createBlockData();
                           }
                        } else if (isSideFace) {
                           double chance = random.nextDouble();
                           if (chance < 0.75) {
                              blockData = this.baseMaterial.createBlockData();
                           } else if (chance < 0.875) {
                              Material stairsMaterial = this.getStairsMaterial();
                              if (stairsMaterial != null) {
                                 blockData = stairsMaterial.createBlockData();
                                 if (blockData instanceof Stairs) {
                                    Stairs stairs = (Stairs)blockData;
                                    stairs.setFacing(this.getFacingForSideFace(loc, center));
                                    stairs.setShape(Shape.values()[random.nextInt(Shape.values().length)]);
                                    stairs.setHalf(random.nextBoolean() ? Half.TOP : Half.BOTTOM);
                                 }
                              }
                           } else {
                              Material slabMaterial = this.getSlabMaterial();
                              if (slabMaterial != null) {
                                 blockData = slabMaterial.createBlockData();
                                 if (blockData instanceof Slab) {
                                    Slab slab = (Slab)blockData;
                                    slab.setType(random.nextBoolean() ? Type.TOP : Type.BOTTOM);
                                 }
                              }
                           }
                        }

                        if (blockData == null) {
                           blockData = this.baseMaterial.createBlockData();
                        }

                        block.setBlockData(blockData, false);
                        BlocksMemory.add(loc);
                     }
                  }
               }
            }
         }
      }
   }

   private Shape getStairShapeForCorner(Location loc, Location center) {
      int x = loc.getBlockX() - center.getBlockX();
      int z = loc.getBlockZ() - center.getBlockZ();
      BlockFace mainDirection = this.getOppositeFacingFromCenter(loc, center);
      if (x == this.radius && z == this.radius) {
         if (mainDirection == BlockFace.NORTH) {
            return Shape.OUTER_LEFT;
         } else {
            return mainDirection == BlockFace.EAST ? Shape.OUTER_RIGHT : Shape.STRAIGHT;
         }
      } else if (x == this.radius && z == -this.radius) {
         if (mainDirection == BlockFace.SOUTH) {
            return Shape.OUTER_RIGHT;
         } else {
            return mainDirection == BlockFace.WEST ? Shape.OUTER_LEFT : Shape.STRAIGHT;
         }
      } else if (x == -this.radius && z == this.radius) {
         if (mainDirection == BlockFace.NORTH) {
            return Shape.OUTER_RIGHT;
         } else {
            return mainDirection == BlockFace.WEST ? Shape.OUTER_LEFT : Shape.STRAIGHT;
         }
      } else if (x != -this.radius || z != -this.radius) {
         return Shape.STRAIGHT;
      } else if (mainDirection == BlockFace.SOUTH) {
         return Shape.OUTER_LEFT;
      } else {
         return mainDirection == BlockFace.EAST ? Shape.OUTER_RIGHT : Shape.STRAIGHT;
      }
   }

   private void configureWallConnections(Wall wall, Location loc) {
      World world = loc.getWorld();
      if (world != null) {
         for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block adjacent = loc.getBlock().getRelative(face);
            Material adjacentType = adjacent.getType();
            boolean shouldConnect = !adjacentType.isAir() && adjacentType.isSolid() && adjacentType != Material.GLASS && adjacentType != Material.GLASS_PANE;
            if (shouldConnect) {
               wall.setHeight(face, Height.LOW);
            } else {
               wall.setHeight(face, Height.NONE);
            }
         }

         wall.setUp(true);
      }
   }

   private Material getStairsMaterial() {
      String materialName = this.baseMaterial.name();
      if (materialName.endsWith("_BRICKS")) {
         return Material.getMaterial(materialName.replace("_BRICKS", "_BRICK_STAIRS"));
      } else if (materialName.endsWith("_PLANKS")) {
         return Material.getMaterial(materialName.replace("_PLANKS", "_STAIRS"));
      } else if (materialName.endsWith("_BLOCK")) {
         return Material.getMaterial(materialName.replace("_BLOCK", "_STAIRS"));
      } else if (materialName.equals("PURPUR_BLOCK")) {
         return Material.PURPUR_STAIRS;
      } else if (materialName.equals("PRISMARINE")) {
         return Material.PRISMARINE_STAIRS;
      } else if (materialName.equals("RED_SANDSTONE")) {
         return Material.RED_SANDSTONE_STAIRS;
      } else {
         Material stairs = Material.getMaterial(materialName + "_STAIRS");
         return stairs != null ? stairs : this.baseMaterial;
      }
   }

   private Material getSlabMaterial() {
      String materialName = this.baseMaterial.name();
      if (materialName.endsWith("_BRICKS")) {
         return Material.getMaterial(materialName.replace("_BRICKS", "_BRICK_SLAB"));
      } else if (materialName.endsWith("_PLANKS")) {
         return Material.getMaterial(materialName.replace("_PLANKS", "_SLAB"));
      } else if (materialName.endsWith("_BLOCK")) {
         return Material.getMaterial(materialName.replace("_BLOCK", "_SLAB"));
      } else if (materialName.equals("PURPUR_BLOCK")) {
         return Material.PURPUR_SLAB;
      } else if (materialName.equals("PRISMARINE")) {
         return Material.PRISMARINE_SLAB;
      } else if (materialName.equals("RED_SANDSTONE")) {
         return Material.RED_SANDSTONE_SLAB;
      } else {
         Material slab = Material.getMaterial(materialName + "_SLAB");
         return slab != null ? slab : this.baseMaterial;
      }
   }

   private Material getWallMaterial() {
      String materialName = this.baseMaterial.name();
      if (materialName.endsWith("_BRICKS")) {
         Material wall = Material.getMaterial(materialName.replace("_BRICKS", "_BRICK_WALL"));
         return wall != null ? wall : this.baseMaterial;
      } else if (materialName.equals("PURPUR_BLOCK")) {
         Material wall = Material.getMaterial("PURPUR_WALL");
         return wall != null ? wall : this.baseMaterial;
      } else if (materialName.equals("PRISMARINE")) {
         Material wall = Material.getMaterial("PRISMARINE_WALL");
         return wall != null ? wall : this.baseMaterial;
      } else if (materialName.equals("RED_SANDSTONE")) {
         Material wall = Material.getMaterial("RED_SANDSTONE_WALL");
         return wall != null ? wall : this.baseMaterial;
      } else if (materialName.equals("END_STONE_BRICKS")) {
         return Material.END_STONE_BRICK_WALL;
      } else if (materialName.equals("STONE_BRICKS")) {
         return Material.STONE_BRICK_WALL;
      } else if (materialName.equals("NETHER_BRICKS")) {
         return Material.NETHER_BRICK_WALL;
      } else if (materialName.equals("RED_NETHER_BRICKS")) {
         Material wall = Material.getMaterial("RED_NETHER_BRICK_WALL");
         return wall != null ? wall : this.baseMaterial;
      } else {
         Material wall = Material.getMaterial(materialName + "_WALL");
         return wall != null ? wall : this.baseMaterial;
      }
   }

   private BlockFace getOppositeFacingFromCenter(Location blockLoc, Location center) {
      Vector direction = blockLoc.toVector().subtract(center.toVector());
      double x = direction.getX();
      double z = direction.getZ();
      if (Math.abs(x) > Math.abs(z)) {
         return x > 0.0 ? BlockFace.WEST : BlockFace.EAST;
      } else {
         return z > 0.0 ? BlockFace.NORTH : BlockFace.SOUTH;
      }
   }

   private BlockFace getFacingForSideFace(Location blockLoc, Location center) {
      Vector direction = blockLoc.toVector().subtract(center.toVector());
      double x = direction.getX();
      double z = direction.getZ();
      if (Math.abs(x) > Math.abs(z)) {
         return x > 0.0 ? BlockFace.EAST : BlockFace.WEST;
      } else {
         return z > 0.0 ? BlockFace.SOUTH : BlockFace.NORTH;
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

         return canPlaceBlock;
      }
   }
}
