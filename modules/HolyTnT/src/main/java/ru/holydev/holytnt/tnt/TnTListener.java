package ru.holydev.holytnt.tnt;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.espi.protectionstones.PSRegion;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;

public class TnTListener implements Listener {
   private HolyTnT plugin;
   private final HashMap<TNTPrimed, TnTType> ignitedTntMap = new HashMap<>();
   private final HashMap<TNTPrimed, UUID> ignitedTntOwner = new HashMap<>();
   private final HashSet<Snowball> snowballs = new HashSet<>();
   private final Random random = new Random();
   private final Map<Location, Long> protectedBlocks = new ConcurrentHashMap<>();
   private static final long PROTECTION_TIME = 300000L;
   private final HashSet<Location> spawnersToDrop = new HashSet<>();
   private final Map<Location, Long> gunCooldowns = new ConcurrentHashMap<>();
   private static final long GUN_COOLDOWN_MS = 2000L;

   public TnTListener(HolyTnT plugin) {
      this.plugin = plugin;
   }

   private boolean isChestProtectedByKotikDang(Block block) {
      if (!(block.getState() instanceof Chest)) {
         return false;
      } else {
         DangGrief dangGrief = this.plugin.getDangGrief();
         return dangGrief == null ? false : dangGrief.hasAnchorInRegion(block.getLocation());
      }
   }

   private void addProtectedBlocks(List<Location> locations) {
      long expirationTime = System.currentTimeMillis() + 300000L;

      for (Location loc : locations) {
         this.protectedBlocks.put(loc, expirationTime);
      }

      Bukkit.getScheduler().runTaskLater(this.plugin, this::cleanExpiredProtections, 6000L);
   }

   private void cleanExpiredProtections() {
      long currentTime = System.currentTimeMillis();
      this.protectedBlocks.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
   }

   private boolean isBlockProtected(Location location) {
      Long expirationTime = this.protectedBlocks.get(location);
      if (expirationTime == null) {
         return false;
      } else if (System.currentTimeMillis() > expirationTime) {
         this.protectedBlocks.remove(location);
         return false;
      } else {
         return true;
      }
   }

   @EventHandler
   public void onBlockPlaceProtection(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
         Block placedBlock = event.getBlock();
         Material blockType = placedBlock.getType();
         if (blockType == Material.OBSIDIAN || blockType == Material.CRYING_OBSIDIAN || blockType == Material.ANCIENT_DEBRIS) {
            Location blockLoc = placedBlock.getLocation();
            if (this.isBlockProtected(blockLoc)) {
               event.setCancelled(true);
               Long expirationTime = this.protectedBlocks.get(blockLoc);
               long timeLeft = (expirationTime - System.currentTimeMillis()) / 1000L;
               long minutes = timeLeft / 60L;
               long seconds = timeLeft % 60L;
               event.getPlayer()
                  .sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&f&f&0&0&0&0▶ &fВы не можете установить стот блок тут еще &x&f&f&0&0&0&0"
                           + minutes
                           + " мин. "
                           + seconds
                           + " сек. &fиз-за действующего рейд-блока"
                     )
                  );
               event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            }
         }
      }
   }

   private boolean breakBlock(Location location) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      ProtectedRegion highestPriorityRegion = null;

      for (ProtectedRegion region : regions) {
         if (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority()) {
            highestPriorityRegion = region;
         }
      }

      if (highestPriorityRegion == null) {
         return true;
      } else {
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("other-explosion");
         if (flag == null) {
            return true;
         } else {
            State flagState = (State)highestPriorityRegion.getFlag(flag);
            return flagState == null || flagState == State.ALLOW;
         }
      }
   }

   private boolean isInRegion(Location location) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      return !regions.isEmpty();
   }

   @EventHandler
   public void onInteract1(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getItemMeta() != null) {
         String itemType = (String)event.getItem()
            .getItemMeta()
            .getPersistentDataContainer()
            .getOrDefault(new NamespacedKey(this.plugin, "tnt-item"), PersistentDataType.STRING, "nul");
         if (itemType.equalsIgnoreCase("explosive")) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      ItemStack firstItem = event.getInventory().getFirstItem();
      if (firstItem != null && firstItem.getItemMeta() != null) {
         String itemType = (String)firstItem.getItemMeta()
            .getPersistentDataContainer()
            .getOrDefault(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, "nul");
         if (!itemType.equalsIgnoreCase("nul")) {
            event.setResult((ItemStack)null);
         }
      }
   }

   @EventHandler
   public void onSpawn(EntitySpawnEvent event) {
      if (event.getEntity() instanceof TNTPrimed) {
         TNTPrimed primedTnt = (TNTPrimed)event.getEntity();
         if (primedTnt.getSource() == null && event.getEntity().getCustomName() == null) {
            primedTnt.setCustomName(ChatColor.translateAlternateColorCodes('&', "&7Динамит"));
            primedTnt.setCustomNameVisible(true);
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
         if (event.getBlock().getType() == Material.TNT && event.getItemInHand() != null && event.getItemInHand().getItemMeta() != null) {
            String tntType = (String)event.getItemInHand()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, "nul");
            if (!tntType.equalsIgnoreCase("nul")) {
               try {
                  this.plugin.getData().addData(event.getBlock().getLocation(), TnTType.valueOf(tntType.toUpperCase()));
               } catch (IOException var5) {
                  var5.printStackTrace();
               }
            }
         }

         if (event.getBlock().getType() == Material.DISPENSER && event.getItemInHand() != null && event.getItemInHand().getItemMeta() != null) {
            String tntType = (String)event.getItemInHand()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, "nul");
            if (tntType.equalsIgnoreCase(TnTType.TNT_GUN.name().toLowerCase())) {
               try {
                  this.plugin.getData().addData(event.getBlock().getLocation(), TnTType.TNT_GUN);
                  if (event.getBlock().getState() instanceof Dispenser) {
                     this.setupTntGunMenu((Dispenser)event.getBlock().getState());
                  }
               } catch (IOException var4) {
                  var4.printStackTrace();
               }
            }
         }
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (!event.isCancelled()) {
         if (event.getBlock().getType() == Material.DISPENSER) {
            try {
               TnTType tntType = this.plugin.getData().getTntType(event.getBlock().getLocation());
               if (tntType != null && tntType == TnTType.TNT_GUN) {
                  this.plugin.getData().removeData(event.getBlock().getLocation());
                  if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                     ItemStack tntGunItem = this.plugin.getHandler().getTnt(1, TnTType.TNT_GUN);
                     event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), tntGunItem);
                  }

                  event.getBlock().setType(Material.AIR);
                  this.cleanupLockedPaneDropsAround(event.getBlock().getChunk());
               }
            } catch (IOException var5) {
               var5.printStackTrace();
            }
         }

         if (event.getBlock().getType() == Material.TNT) {
            try {
               TnTType tntType = this.plugin.getData().getTntType(event.getBlock().getLocation());
               if (tntType != null) {
                  this.plugin.getData().removeData(event.getBlock().getLocation());
                  event.setDropItems(false);
                  if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                     event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), this.plugin.getHandler().getTnt(1, tntType));
                  }

                  event.getBlock().setType(Material.AIR);
               }
            } catch (IOException var4) {
               var4.printStackTrace();
            }
         }
      }
   }

   @EventHandler
   public void onPistonExtend(BlockPistonExtendEvent event) {
      List<Block> tntBlocks = new ArrayList<>();

      for (Block block : event.getBlocks()) {
         if (block.getType() == Material.TNT || block.getType() == Material.DISPENSER) {
            tntBlocks.add(block);
         }
      }

      for (int i = tntBlocks.size() - 1; i >= 0; i--) {
         Block blockx = tntBlocks.get(i);
         this.handleTntMove(blockx, blockx.getRelative(event.getDirection()));
      }

      event.setCancelled(false);
   }

   @EventHandler
   public void onPistonRetract(BlockPistonRetractEvent event) {
      List<Block> tntBlocks = new ArrayList<>();

      for (Block block : event.getBlocks()) {
         if (block.getType() == Material.TNT || block.getType() == Material.DISPENSER) {
            tntBlocks.add(block);
         }
      }

      for (int i = tntBlocks.size() - 1; i >= 0; i--) {
         Block blockx = tntBlocks.get(i);
         this.handleTntMove(blockx, blockx.getRelative(event.getDirection()));
      }

      event.setCancelled(false);
   }

   @EventHandler
   public void onRedstone(BlockRedstoneEvent event) {
      if (event.getBlock().getType() == Material.TNT) {
         try {
            TnTType tntType = this.plugin.getData().getTntType(event.getBlock().getLocation());
            if (tntType != null) {
               event.getBlock().setType(Material.AIR);
               this.plugin.getData().removeData(event.getBlock().getLocation());
               event.getBlock().getLocation().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
               TNTPrimed primedTnt = this.createCustomTnt(event.getBlock().getLocation(), tntType);
               this.ignitedTntMap.put(primedTnt, tntType);
            }
         } catch (IOException var41) {
            var41.printStackTrace();
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getClickedBlock() != null
         && event.getAction() == Action.RIGHT_CLICK_BLOCK
         && event.getItem() != null
         && event.getItem().getType() == Material.FLINT_AND_STEEL
         && event.getClickedBlock().getType() == Material.TNT) {
         try {
            TnTType tntType = this.plugin.getData().getTntType(event.getClickedBlock().getLocation());
            if (tntType != null) {
               this.plugin.getData().removeData(event.getClickedBlock().getLocation());
               event.setCancelled(true);
               event.getClickedBlock().setType(Material.AIR);
               event.getClickedBlock().getLocation().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
               TNTPrimed primedTnt = this.createCustomTnt(event.getClickedBlock().getLocation(), tntType);
               this.ignitedTntMap.put(primedTnt, tntType);
               this.ignitedTntOwner.put(primedTnt, event.getPlayer().getUniqueId());
            }
         } catch (IOException var41) {
            var41.printStackTrace();
         }
      }
   }

   @EventHandler
   public void onTNTPrime(TNTPrimeEvent event) {
      if (event.getBlock().getType() == Material.TNT) {
         try {
            TnTType tntType = this.plugin.getData().getTntType(event.getBlock().getLocation());
            if (tntType != null) {
               this.plugin.getData().removeData(event.getBlock().getLocation());
               event.getBlock().getLocation().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
               TNTPrimed primedTnt = this.createCustomTnt(event.getBlock().getLocation(), tntType);
               this.ignitedTntMap.put(primedTnt, tntType);
               event.setCancelled(true);
               event.getBlock().setType(Material.AIR);
            }
         } catch (IOException var41) {
            var41.printStackTrace();
         }
      }
   }

   public void spawnTnT(Location location, TnTType tntType, double velocity, int fuse) {
      TNTPrimed primedTnt = this.createCustomTnt(location, tntType);
      primedTnt.setFuseTicks(fuse);
      primedTnt.setVelocity(location.getDirection().multiply(velocity));
      this.ignitedTntMap.put(primedTnt, tntType);
   }

   @EventHandler
   public void onExplode(BlockExplodeEvent event) {
      List<Block> spawnersToRemove = new ArrayList<>();

      for (Block block : event.blockList()) {
         if (block.getType() == Material.SPAWNER) {
            spawnersToRemove.add(block);
         }
      }

      event.blockList().removeAll(spawnersToRemove);
      event.blockList().removeIf(blockx -> {
         if (this.isChestProtectedByKotikDang(blockx)) {
            return true;
         } else if (!this.breakBlock(blockx.getLocation())) {
            return true;
         } else {
            this.handleExplosionBlockInteraction(blockx, null);
            return this.shouldRemoveBlock(blockx);
         }
      });
   }

   private void handleExplosionBlockInteraction(Block block, UUID ownerUUID) {
      if (block.getType() == Material.TNT) {
         try {
            TnTType tntType = this.plugin.getData().getTntType(block.getLocation());
            if (tntType != null) {
               this.plugin.getData().removeData(block.getLocation());
               block.setType(Material.AIR);
               block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
               TNTPrimed primedTnt = this.createCustomTnt(block.getLocation(), tntType);
               this.ignitedTntMap.put(primedTnt, tntType);
               if (ownerUUID != null) {
                  this.ignitedTntOwner.put(primedTnt, ownerUUID);
               }
            }
         } catch (IOException var6) {
            var6.printStackTrace();
         }
      }

      if (block.getType() == Material.DISPENSER) {
         try {
            TnTType tntType = this.plugin.getData().getTntType(block.getLocation());
            if (tntType != null && tntType == TnTType.TNT_GUN) {
               this.plugin.getData().removeData(block.getLocation());
               block.getWorld().dropItemNaturally(block.getLocation().clone().add(0.5, 0.5, 0.5), this.plugin.getHandler().getTnt(1, TnTType.TNT_GUN));
               block.setType(Material.AIR);
               this.cleanupLockedPaneDropsAround(block.getChunk());
            }
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }
   }

   private boolean shouldRemoveBlock(Block block) {
      if (!this.breakBlock(block.getLocation())) {
         return true;
      } else if (block.getType() == Material.IRON_BLOCK
         || block.getType() == Material.GOLD_BLOCK
         || block.getType() == Material.DIAMOND_BLOCK
         || block.getType() == Material.EMERALD_ORE) {
         PSRegion region = PSRegion.fromLocation(block.getLocation());
         if (region != null) {
            Location protectLoc = region.getProtectBlock().getLocation();
            if (protectLoc.getBlockX() == block.getLocation().getBlockX()
               && protectLoc.getBlockY() == block.getLocation().getBlockY()
               && protectLoc.getBlockZ() == block.getLocation().getBlockZ()) {
               this.removeRgWithDurability(block.getLocation(), 1);
               return true;
            }
         }

         return true;
      } else if (block.getType() != Material.BEDROCK
         && block.getType() != Material.BARRIER
         && block.getType() != Material.END_PORTAL
         && block.getType() != Material.END_GATEWAY
         && block.getType() != Material.END_PORTAL_FRAME
         && block.getType() != Material.SPAWNER
         && !this.isWater(block.getLocation())) {
         if (block.getState() instanceof InventoryHolder) {
            if (this.isChestProtectedByKotikDang(block)) {
               return true;
            }

            block.breakNaturally();
         } else {
            BlockState state = block.getState();
            state.setType(Material.AIR);
            state.update(true, state.getType() == Material.WATER);
         }

         return true;
      } else {
         return true;
      }
   }

   @EventHandler
   public void onDispense(BlockDispenseEvent event) {
      ItemStack item = event.getItem();
      if (item != null && item.getType() != Material.AIR && !event.isCancelled()) {
         Block block = event.getBlock();

         try {
            TnTType tntType = this.plugin.getData().getTntType(block.getLocation());
            if (tntType != null && tntType == TnTType.TNT_GUN) {
               this.handleTntGunDispense(event);
               return;
            }
         } catch (IOException var13) {
            var13.printStackTrace();
         }

         if (item.getType() == Material.TNT) {
            event.setCancelled(true);
            Location location = block.getLocation();
            Directional directional = (Directional)block.getBlockData();
            BlockFace face = directional.getFacing();
            Location spawnLocation = location.clone().add(0.5, 0.0, 0.5).add(face.getDirection());
            block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
            TNTPrimed primedTnt = (TNTPrimed)location.getWorld().spawnEntity(spawnLocation, EntityType.TNT);
            primedTnt.setFuseTicks(100);
            if (item.getItemMeta() != null) {
               PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
               NamespacedKey key = new NamespacedKey(this.plugin, "tnt");
               String tntType = (String)pdc.getOrDefault(key, PersistentDataType.STRING, "none");
               if (!tntType.equalsIgnoreCase("none")) {
                  String displayName = this.plugin.getHandler().getTnt(1, TnTType.valueOf(tntType.toUpperCase())).getItemMeta().getDisplayName();
                  primedTnt.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
                  primedTnt.setCustomNameVisible(true);
                  this.ignitedTntMap.put(primedTnt, TnTType.valueOf(tntType.toUpperCase()));
               }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               Inventory dispenserInventory = this.getInventoryFromDispenser(location);
               if (dispenserInventory != null) {
                  dispenserInventory.removeItem(new ItemStack[]{item.asOne()});
               }
            }, 1L);
         }
      }
   }

   public Inventory getInventoryFromDispenser(Location location) {
      Block block = location.getBlock();
      if (block.getState() instanceof Dispenser) {
         Dispenser dispenser = (Dispenser)block.getState();
         return dispenser.getInventory();
      } else {
         return null;
      }
   }

   @EventHandler
   public void onEntityExplode(EntityExplodeEvent event) {
      if (!event.isCancelled()) {
         TnTType tntType = null;
         UUID ownerUUID = null;
         if (event.getEntity() instanceof TNTPrimed && this.ignitedTntMap.containsKey((TNTPrimed)event.getEntity())) {
            event.setCancelled(true);
            TNTPrimed tnt = (TNTPrimed)event.getEntity();
            tntType = this.ignitedTntMap.get(tnt);
            ownerUUID = this.ignitedTntOwner.get(tnt);
            this.createExplosion(event.getEntity(), event.getLocation(), tntType, ownerUUID);
            if (tntType == TnTType.A || tntType == TnTType.B || tntType == TnTType.C4 || tntType == TnTType.WAVE) {
               this.activateRaidTax(event.getLocation());
            }

            this.ignitedTntMap.remove(tnt);
            this.ignitedTntOwner.remove(tnt);
         }

         UUID finalOwnerUUID = ownerUUID;
         List<Block> spawnersToRemove = new ArrayList<>();

         for (Block block : event.blockList()) {
            if (block.getType() == Material.SPAWNER) {
               spawnersToRemove.add(block);
            }
         }

         event.blockList().removeAll(spawnersToRemove);
         event.blockList().removeIf(blockx -> {
            if (this.isChestProtectedByKotikDang(blockx)) {
               return true;
            } else if (!this.breakBlock(blockx.getLocation())) {
               return true;
            } else {
               this.handleExplosionBlockInteraction(blockx, finalOwnerUUID);
               return event.isCancelled() ? false : this.shouldRemoveBlock(blockx);
            }
         });
      }
   }

   private void activateRaidTax(Location location) {
      try {
         Plugin holyPSAddonPlugin = Bukkit.getPluginManager().getPlugin("HolyPSAddon");
         if (holyPSAddonPlugin != null) {
            Class<?> holyClass = holyPSAddonPlugin.getClass();
            Method activateRaidTaxMethod = holyClass.getMethod("activateRaidTax", Location.class);
            activateRaidTaxMethod.invoke(holyPSAddonPlugin, location);
         }
      } catch (Exception var51) {
         this.plugin.getLogger().warning("Failed to activate raid tax: " + var51.getMessage());
      }
   }

   @EventHandler
   public void onSand(final EntityChangeBlockEvent event) {
      event.setCancelled(false);
      if (event.isCancelled()) {
         event.getBlock().setType(Material.SAND);
      }

      (new BukkitRunnable() {
         public void run() {
            for (Entity entity : event.getBlock().getWorld().getEntities()) {
               if (entity.getType() == EntityType.ITEM) {
                  double distance = entity.getLocation().toVector().distance(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5).toVector());
                  if (distance <= 0.1) {
                     Item item = (Item)entity;
                     item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                  }
               }
            }
         }
      }).runTaskLater(this.plugin, 1L);
   }

   private void handleTntMove(Block oldBlock, Block newBlock) {
      try {
         TnTType tntType = this.plugin.getData().getTntType(oldBlock.getLocation());
         if (tntType != null) {
            this.plugin.getData().removeData(oldBlock.getLocation());
            this.plugin.getData().addData(newBlock.getLocation(), tntType);
         }
      } catch (IOException var41) {
         var41.printStackTrace();
      }
   }

   public boolean isBlockWaterlogged(Block block) {
      if (block.getBlockData() instanceof Waterlogged) {
         Waterlogged waterlogged = (Waterlogged)block.getBlockData();
         return waterlogged.isWaterlogged();
      } else {
         return false;
      }
   }

   @EventHandler
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity().getType() == EntityType.ITEM
         && (event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)) {
         event.setCancelled(true);
      }
   }

   public void createIceSphere(Location location) {
      final List<BlockState> resetList = new ArrayList<>();

      for (int x = -2; x <= 2; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
               Location blockLocation = location.clone().add(x, y, z);
               if (!(blockLocation.getBlock().getState() instanceof InventoryHolder) && this.breakBlock(blockLocation)) {
                  Block block = blockLocation.getBlock();
                  if (!this.isChestProtectedByKotikDang(block)
                     && block.getType() != Material.ICE
                     && block.getType() != Material.NETHERITE_BLOCK
                     && block.getType() != Material.ANCIENT_DEBRIS
                     && block.getType() != Material.IRON_BLOCK
                     && block.getType() != Material.GOLD_BLOCK
                     && block.getType() != Material.DIAMOND_BLOCK
                     && block.getType() != Material.EMERALD_ORE
                     && block.getType() != Material.BEDROCK
                     && block.getType() != Material.BARRIER
                     && block.getType() != Material.END_PORTAL
                     && block.getType() != Material.END_GATEWAY
                     && block.getType() != Material.END_PORTAL_FRAME
                     && block.getType() != Material.SPAWNER) {
                     double distance = blockLocation.distance(location);
                     if (distance <= 2.0) {
                        resetList.add(block.getState());
                        block.setType(Material.ICE);
                     }
                  }
               }
            }
         }
      }

      location.getWorld().createExplosion(location, 4.0F, false, false);
      (new BukkitRunnable() {
         public void run() {
            for (BlockState blockState : resetList) {
               blockState.update(true, blockState.getType() == Material.WATER);
               Location blockLocation = blockState.getLocation();
               blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F);
            }
         }
      }).runTaskLater(this.plugin, 100L);
   }

   public void spawnStealer(Location location, int chance, int color, boolean isInWater) {
      location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      if (isInWater) {
         Color[] colors = new Color[]{Color.fromRGB(1384278)};
         this.drawSquare(location.getWorld(), location.clone().add(0.0, 0.5, 0.0), 0.5, colors);
      } else {
         int randomValue = this.random.nextInt(100);
         Block closestSpawner = null;
         double closestDistance = Double.MAX_VALUE;

         for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
               for (int z = -2; z <= 2; z++) {
                  Location blockLocation = location.clone().add(x, y, z);
                  double distance = blockLocation.distance(location);
                  if (distance <= 2.5) {
                     Block block = blockLocation.getBlock();
                     if (block.getType() == Material.SPAWNER && distance < closestDistance) {
                        closestDistance = distance;
                        closestSpawner = block;
                     }
                  }
               }
            }
         }

         if (closestSpawner != null) {
            CreatureSpawner spawner = (CreatureSpawner)closestSpawner.getState();
            EntityType mobType = spawner.getSpawnedType();
            closestSpawner.setType(Material.AIR);
            Color[] colors = new Color[]{Color.fromRGB(color)};
            this.drawSquare(closestSpawner.getWorld(), closestSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 0.5, colors);
            boolean mobDropped = randomValue < chance;
            ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
            if (mobDropped) {
               Plugin holySpawnersPlugin = Bukkit.getPluginManager().getPlugin("HolySpawners");
               if (holySpawnersPlugin != null) {
                  try {
                     Class<?> holySpawnersClass = holySpawnersPlugin.getClass();
                     Method getInstanceMethod = holySpawnersClass.getMethod("getInstance");
                     Object holySpawnersInstance = getInstanceMethod.invoke(null);
                     Method getConfigManagerMethod = holySpawnersClass.getMethod("getConfigManager");
                     Object configManager = getConfigManagerMethod.invoke(holySpawnersInstance);
                     Class<?> configManagerClass = configManager.getClass();
                     Method getSpawnerNameMethod = configManagerClass.getMethod("getSpawnerName", String.class);
                     Method getSpawnerLoreMethod = configManagerClass.getMethod("getSpawnerLore", String.class);
                     String entityName = mobType.name();
                     String name = (String)getSpawnerNameMethod.invoke(configManager, entityName);
                     List<String> lore = (List<String>)getSpawnerLoreMethod.invoke(configManager, entityName);
                     spawnerItem = this.createSpawnerItemWithCustomData(mobType, name, lore);
                  } catch (Exception var27) {
                     this.plugin.getLogger().warning("Failed to use HolySpawners for stealer spawner: " + var27.getMessage());
                     ItemMeta meta = spawnerItem.getItemMeta();
                     if (meta != null) {
                        meta.setDisplayName(ChatColor.RESET + mobType.name());
                        spawnerItem.setItemMeta(meta);
                     }
                  }
               } else {
                  ItemMeta meta = spawnerItem.getItemMeta();
                  if (meta != null) {
                     meta.setDisplayName(ChatColor.RESET + mobType.name());
                     spawnerItem.setItemMeta(meta);
                  }
               }
            }

            Item item = location.getWorld().dropItem(location.clone().add(0.5, 0.5, 0.5), spawnerItem);
            item.setVelocity(new Vector(0, 0, 0));
            item.setPickupDelay(0);
            location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
            if (mobDropped) {
               String soundName = "ENTITY_" + mobType.name() + "_DEATH";

               try {
                  Sound sound = Sound.valueOf(soundName);
                  location.getWorld().playSound(location, sound, 1.0F, 1.0F);
               } catch (IllegalArgumentException var26) {
                  location.getWorld().playSound(location, Sound.ENTITY_PIG_DEATH, 1.0F, 1.0F);
               }
            } else {
               location.getWorld().playSound(location, Sound.ENTITY_PIG_DEATH, 1.0F, 1.0F);
            }
         } else {
            Color[] colorsx = new Color[]{Color.fromRGB(16717056)};
            this.drawSquare(location.getWorld(), location.clone().add(0.0, 0.5, 0.0), 0.5, colorsx);
         }
      }
   }

   private ItemStack createSpawnerItemWithCustomData(EntityType mobType, String name, List<String> lore) {
      ItemStack spawner = new ItemStack(Material.SPAWNER);
      BlockStateMeta meta = (BlockStateMeta)spawner.getItemMeta();
      if (meta != null) {
         CreatureSpawner spawnerState = (CreatureSpawner)meta.getBlockState();
         spawnerState.setSpawnedType(mobType);
         meta.setBlockState(spawnerState);
         meta.setDisplayName(this.translateColors(name));
         List<String> translatedLore = new ArrayList<>();

         for (String loreLine : lore) {
            translatedLore.add(this.translateColors(loreLine));
         }

         meta.setLore(translatedLore);
         spawner.setItemMeta(meta);
      }

      return spawner;
   }

   private String translateColors(String text) {
      Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
      Matcher matcher = HEX_PATTERN.matcher(text);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hexCode = matcher.group();

         try {
            net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(hexCode);
            matcher.appendReplacement(buffer, color.toString());
         } catch (Exception var71) {
            matcher.appendReplacement(buffer, hexCode);
         }
      }

      matcher.appendTail(buffer);
      return ChatColor.translateAlternateColorCodes('&', buffer.toString());
   }

   private void drawSquare(World world, Location center, double size, Color[] colors) {
      int colorIndex = 0;

      for (double y = center.getX() - size; y <= center.getX() + size; y += size / 10.0) {
         this.spawnParticleSquare(world, y, center.getY() - size, center.getZ() - size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, y, center.getY() - size, center.getZ() + size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, y, center.getY() + size, center.getZ() + size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, y, center.getY() + size, center.getZ() - size, colors[colorIndex % colors.length]);
         colorIndex++;
      }

      for (double var9 = center.getZ() - size; var9 <= center.getZ() + size; var9 += size / 10.0) {
         this.spawnParticleSquare(world, center.getX() - size, center.getY() - size, var9, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() - size, center.getY() + size, var9, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() + size, center.getY() - size, var9, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() + size, center.getY() + size, var9, colors[colorIndex % colors.length]);
         colorIndex++;
      }

      for (double var10 = center.getY() - size; var10 <= center.getY() + size; var10 += size / 10.0) {
         this.spawnParticleSquare(world, center.getX() - size, var10, center.getZ() - size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() - size, var10, center.getZ() + size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() + size, var10, center.getZ() - size, colors[colorIndex % colors.length]);
         this.spawnParticleSquare(world, center.getX() + size, var10, center.getZ() + size, colors[colorIndex % colors.length]);
         colorIndex++;
      }
   }

   private void spawnParticleSquare(World world, double x, double y, double z, Color color) {
      world.spawnParticle(Particle.DUST, new Location(world, x, y, z), 1, 0.0, 0.0, 0.0, 0.0, new DustOptions(color, 1.0F), true);
   }

   private void createExplosion(Entity entity, Location location, TnTType tntType, UUID ownerUUID) {
      float power = 4.0F;
      boolean breaksBlocks = false;
      boolean worksInWater = false;
      boolean isInWaterOrLava = entity.isInWater() || entity.isInLava() || this.isWater(location);
      switch (tntType) {
         case A:
            power *= 3.0F;
            break;
         case B:
            power *= 10.0F;
            break;
         case C4:
            breaksBlocks = true;
            worksInWater = false;
            break;
         case WAVE:
            breaksBlocks = true;
            worksInWater = true;
            break;
         case B2:
            this.destroyLargeArea(location, ownerUUID);
            return;
         case ICE:
            this.createIceSphere(location);
            return;
         case STEALER:
            location.getWorld().createExplosion(location, 4.0F, false, false);
            this.spawnStealer(location, 50, 15663359, isInWaterOrLava);
            return;
         case LUCK_STEALER:
            location.getWorld().createExplosion(location, 4.0F, false, false);
            this.spawnStealer(location, 75, 779007, isInWaterOrLava);
            return;
      }

      if (!worksInWater && isInWaterOrLava) {
         location.getWorld().createExplosion(location, power, false, false);
      } else {
         location.getWorld().createExplosion(location, power, false, true);
      }

      if (breaksBlocks && (worksInWater || !isInWaterOrLava)) {
         this.processExplosionBlocks(location, tntType, worksInWater);
      }
   }

   private void destroyLargeArea(Location location, UUID ownerUUID) {
      for (int x = -12; x <= 12; x++) {
         for (int y = -12; y <= 12; y++) {
            for (int z = -12; z <= 12; z++) {
               Location blockLoc = location.clone().add(x, y, z);
               Block block = blockLoc.getBlock();
               Material type = block.getType();
               if (type != Material.BEDROCK
                  && type != Material.BARRIER
                  && type != Material.END_PORTAL_FRAME
                  && type != Material.END_PORTAL
                  && type != Material.END_GATEWAY
                  && type != Material.SPAWNER
                  && type != Material.DISPENSER
                  && type != Material.OBSIDIAN
                  && type != Material.CRYING_OBSIDIAN
                  && type != Material.ANCIENT_DEBRIS
                  && type != Material.NETHERITE_BLOCK
                  && !this.isChestProtectedByKotikDang(block)
                  && this.breakBlock(blockLoc)
                  && this.canB2BreakInRegion(blockLoc, ownerUUID)) {
                  block.setType(Material.AIR);
               }
            }
         }
      }

      location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.7F);
      location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 1, 0.0, 0.0, 0.0, 0.0);
   }

   private boolean canB2BreakInRegion(Location blockLoc, UUID ownerUUID) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();

      for (ProtectedRegion wgRegion : query.getApplicableRegions(BukkitAdapter.adapt(blockLoc)).getRegions()) {
         if (!"__global__".equalsIgnoreCase(wgRegion.getId())) {
            if (ownerUUID == null) {
               return false;
            }

            boolean isMember = wgRegion.getOwners().contains(ownerUUID) || wgRegion.getMembers().contains(ownerUUID);
            if (!isMember) {
               return false;
            }

            PSRegion psRegion = PSRegion.fromWGRegion(blockLoc.getWorld(), wgRegion);
            if (psRegion != null) {
               Location protectLoc = psRegion.getProtectBlock().getLocation();
               if (protectLoc.getBlockX() == blockLoc.getBlockX()
                  && protectLoc.getBlockY() == blockLoc.getBlockY()
                  && protectLoc.getBlockZ() == blockLoc.getBlockZ()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private void processExplosionBlocks(Location location, TnTType tntType, boolean worksInWater) {
      List<Location> destroyedBlocks = new ArrayList<>();
      if (tntType != TnTType.C4 && tntType != TnTType.WAVE) {
         int radius = 3;

         for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
               for (int z = -radius; z <= radius; z++) {
                  Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                  if (block != null
                     && this.breakBlock(block.getLocation())
                     && !(block.getState() instanceof InventoryHolder)
                     && block.getType() != Material.BEDROCK
                     && block.getType() != Material.BARRIER
                     && block.getType() != Material.END_GATEWAY
                     && block.getType() != Material.END_PORTAL_FRAME) {
                     double distance = location.toVector().distance(block.getLocation().clone().add(0.5, 0.5, 0.5).toVector());
                     if (distance <= 3.5 && (this.isBlockWaterlogged(block) || worksInWater)) {
                        destroyedBlocks.add(block.getLocation().clone());
                        this.breakBlockSafely(block);
                     }
                  }
               }
            }
         }
      } else {
         this.processSquareExplosion(location, tntType, destroyedBlocks);
      }

      if ((tntType == TnTType.C4 || tntType == TnTType.WAVE) && !destroyedBlocks.isEmpty()) {
         List<Location> regionBlocks = new ArrayList<>();

         for (Location loc : destroyedBlocks) {
            PSRegion region = PSRegion.fromLocation(loc);
            if (region != null) {
               regionBlocks.add(loc);
            }
         }

         if (!regionBlocks.isEmpty()) {
            this.addProtectedBlocks(regionBlocks);
         }
      }
   }

   private void processSquareExplosion(Location location, TnTType tntType, List<Location> destroyedBlocks) {
      int radius = 2;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
               if (block != null
                  && this.breakBlock(block.getLocation())
                  && !(block.getState() instanceof InventoryHolder)
                  && block.getType() != Material.BEDROCK
                  && block.getType() != Material.BARRIER
                  && block.getType() != Material.END_GATEWAY
                  && block.getType() != Material.END_PORTAL_FRAME
                  && block.getType() != Material.SPAWNER) {
                  Material blockType = block.getType();
                  if (blockType == Material.OBSIDIAN
                     || blockType == Material.CRYING_OBSIDIAN
                     || blockType == Material.ANCIENT_DEBRIS
                     || blockType == Material.NETHERITE_BLOCK
                     || blockType == Material.IRON_BLOCK
                     || blockType == Material.GOLD_BLOCK
                     || blockType == Material.DIAMOND_BLOCK
                     || blockType == Material.EMERALD_ORE) {
                     this.processSpecialBlockDestruction(block, tntType, destroyedBlocks);
                  } else if (this.isBlockWaterlogged(block) || tntType == TnTType.WAVE) {
                     destroyedBlocks.add(block.getLocation().clone());
                     this.breakBlockSafely(block);
                  }
               }
            }
         }
      }
   }

   private void processSpecialBlockDestruction(Block block, TnTType tntType, List<Location> destroyedBlocks) {
      Material blockType = block.getType();
      int durabilityToRemove = tntType == TnTType.WAVE ? 2 : 1;
      if (blockType == Material.ANCIENT_DEBRIS) {
         boolean wasRgBlock = this.removeRgWithDurability(block.getLocation(), durabilityToRemove);
         if (wasRgBlock) {
            destroyedBlocks.add(block.getLocation().clone());
            if (tntType == TnTType.WAVE) {
               block.setType(Material.OBSIDIAN);
            } else {
               block.setType(Material.CRYING_OBSIDIAN);
            }

            block.getState().update(true, true);
         }
      } else if (blockType == Material.CRYING_OBSIDIAN) {
         boolean wasRgBlock = this.removeRgWithDurability(block.getLocation(), durabilityToRemove);
         if (wasRgBlock) {
            destroyedBlocks.add(block.getLocation().clone());
            if (tntType == TnTType.WAVE) {
               this.breakBlockSafely(block);
            } else {
               block.setType(Material.OBSIDIAN);
               block.getState().update(true, true);
            }
         }
      } else if (blockType == Material.OBSIDIAN) {
         boolean wasRgBlock = this.removeRgWithDurability(block.getLocation(), durabilityToRemove);
         if (wasRgBlock) {
            destroyedBlocks.add(block.getLocation().clone());
            this.breakBlockSafely(block);
         }
      } else if (blockType == Material.NETHERITE_BLOCK) {
         boolean wasRgBlock = this.removeRgWithDurability(block.getLocation(), durabilityToRemove);
         if (wasRgBlock) {
            destroyedBlocks.add(block.getLocation().clone());
            this.breakBlockSafely(block);
         }
      } else if (blockType == Material.IRON_BLOCK
         || blockType == Material.GOLD_BLOCK
         || blockType == Material.DIAMOND_BLOCK
         || blockType == Material.EMERALD_ORE) {
         boolean wasRgBlock = this.removeRgWithDurability(block.getLocation(), durabilityToRemove);
         if (wasRgBlock) {
            destroyedBlocks.add(block.getLocation().clone());
            this.breakBlockSafely(block);
         }
      }
   }

   private void breakBlockSafely(Block block) {
      if (!(block.getState() instanceof InventoryHolder) && block.getType() != Material.SPAWNER) {
         if (this.isChestProtectedByKotikDang(block)) {
            return;
         }

         BlockState state = block.getState();
         state.setType(Material.AIR);
         state.update(true, true);
         block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F);
         block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.05, block.getBlockData());
      }
   }

   private boolean removeRg(Location location) {
      PSRegion region = PSRegion.fromLocation(location);
      if (region == null) {
         return true;
      } else {
         Location protectLoc = region.getProtectBlock().getLocation();
         if (protectLoc.getBlockX() == location.getBlockX() && protectLoc.getBlockY() == location.getBlockY() && protectLoc.getBlockZ() == location.getBlockZ()
            )
          {
            region.deleteRegion(true);
            return false;
         } else {
            return true;
         }
      }
   }

   private boolean removeRgWithDurability(Location location, int durabilityToRemove) {
      PSRegion region = PSRegion.fromLocation(location);
      if (region == null) {
         return true;
      } else {
         Location protectLoc = region.getProtectBlock().getLocation();
         if (protectLoc.getBlockX() == location.getBlockX() && protectLoc.getBlockY() == location.getBlockY() && protectLoc.getBlockZ() == location.getBlockZ()
            )
          {
            try {
               Plugin holyPlugin = Bukkit.getPluginManager().getPlugin("HolyPSAddon");
               if (holyPlugin != null) {
                  Class<?> holyClass = holyPlugin.getClass();
                  Method breakRegionMethod = holyClass.getMethod("breakRegionWithDurability", Location.class, int.class);
                  this.plugin.getLogger().info("Calling breakRegionWithDurability with durability: " + durabilityToRemove);
                  Boolean result = (Boolean)breakRegionMethod.invoke(holyPlugin, location, durabilityToRemove);
                  return result != null && result;
               }
            } catch (Exception var91) {
               this.plugin.getLogger().warning("Failed to call HolyPSAddon method: " + var91.getMessage());
               region.deleteRegion(true);
               return false;
            }

            region.deleteRegion(true);
            return false;
         } else {
            return true;
         }
      }
   }

   public TNTPrimed createCustomTnt(Location location, TnTType tntType) {
      TNTPrimed primedTnt = (TNTPrimed)location.getWorld().spawnEntity(location.add(0.5, 0.0, 0.5), EntityType.TNT);
      primedTnt.setFuseTicks(100);
      String displayName = this.plugin.getHandler().getTnt(1, tntType).getItemMeta().getDisplayName();
      primedTnt.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
      primedTnt.setCustomNameVisible(true);
      return primedTnt;
   }

   public boolean isWater(Location location) {
      Block block = location.getBlock();
      if (block.getType() != Material.WATER && block.getType() != Material.LAVA) {
         if ((block.getType() == Material.KELP_PLANT || block.getType() == Material.SEAGRASS || block.getType() == Material.TALL_SEAGRASS)
            && block.getBlockData() instanceof Waterlogged) {
            Waterlogged waterlogged = (Waterlogged)block.getBlockData();
            return waterlogged.isWaterlogged();
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public void handleTntGunDispense(BlockDispenseEvent event) {
      event.setCancelled(true);
      Block block = event.getBlock();
      Location gunLocation = block.getLocation();
      long now = System.currentTimeMillis();
      Long lastShot = this.gunCooldowns.get(gunLocation);
      if (lastShot == null || now - lastShot >= 2000L) {
         this.gunCooldowns.put(gunLocation, now);
         BlockFace facing = this.getDispenserFacing(block);
         Block blockInFront = block.getRelative(facing);
         if (blockInFront.isPassable() || blockInFront.getType() == Material.AIR) {
            Inventory dispenserInventory = this.getInventoryFromDispenser(block.getLocation());
            if (dispenserInventory != null) {
               ItemStack center = dispenserInventory.getItem(4);
               if (center != null && center.getType() == Material.TNT) {
                  String tntType = "normal";
                  if (center.hasItemMeta()) {
                     PersistentDataContainer pdc = center.getItemMeta().getPersistentDataContainer();
                     tntType = (String)pdc.getOrDefault(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, "normal");
                  }

                  Location spawnLocation = block.getRelative(facing).getLocation().clone().add(0.5, 0.0, 0.5);
                  block.getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
                  final Snowball snowball = (Snowball)block.getWorld().spawn(spawnLocation, Snowball.class);
                  this.snowballs.add(snowball);
                  final TNTPrimed tnt;
                  if (!tntType.equalsIgnoreCase("normal")) {
                     tnt = this.createCustomTnt(spawnLocation, TnTType.valueOf(tntType.toUpperCase()));
                     this.ignitedTntMap.put(tnt, TnTType.valueOf(tntType.toUpperCase()));
                  } else {
                     tnt = (TNTPrimed)block.getWorld().spawnEntity(spawnLocation, EntityType.TNT);
                     tnt.setCustomName(ChatColor.translateAlternateColorCodes('&', "&7Динамит"));
                     tnt.setCustomNameVisible(true);
                  }

                  tnt.setFuseTicks(200);
                  tnt.setGravity(false);
                  snowball.addPassenger(tnt);
                  snowball.setGravity(false);
                  final Vector initialVelocity = new Vector(facing.getModX() * 1.0, facing.getModY() * 1.0, facing.getModZ() * 1.0);
                  (new BukkitRunnable() {
                     private int distance = 0;
                     private int ticksInAir = 0;

                     public void run() {
                        if (!snowball.isDead() && !tnt.isDead()) {
                           Location tntLoc = tnt.getLocation();
                           Block blockInFrontCheck = tntLoc.getBlock();
                           if (!blockInFrontCheck.isPassable() && blockInFrontCheck.getType() != Material.AIR) {
                              tnt.setFuseTicks(0);
                              snowball.remove();
                              TnTListener.this.snowballs.remove(snowball);
                              this.cancel();
                              return;
                           }

                           this.ticksInAir++;
                           double speedMultiplier = 1.0 - this.ticksInAir / 200.0;
                           if (speedMultiplier < 0.3) {
                              speedMultiplier = 0.3;
                           }

                           Vector currentVelocity = initialVelocity.clone().multiply(speedMultiplier);
                           snowball.setVelocity(currentVelocity);
                           this.distance++;
                        } else {
                           if (!tnt.isDead()) {
                              tnt.setFuseTicks(0);
                           }

                           if (!snowball.isDead()) {
                              snowball.remove();
                           }

                           TnTListener.this.snowballs.remove(snowball);
                           this.cancel();
                        }
                     }
                  }).runTaskTimer(this.plugin, 0L, 1L);
                  Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                     if (center.getAmount() > 1) {
                        center.setAmount(center.getAmount() - 1);
                        dispenserInventory.setItem(4, center);
                     } else {
                        dispenserInventory.setItem(4, null);
                     }
                  }, 1L);
               }
            }
         }
      }
   }

   private BlockFace getDispenserFacing(Block block) {
      if (block.getBlockData() instanceof Directional) {
         Directional directional = (Directional)block.getBlockData();
         return directional.getFacing();
      } else {
         return BlockFace.NORTH;
      }
   }

   @EventHandler
   public void onHit(ProjectileHitEvent event) {
      if (event.getEntity() instanceof Snowball) {
         Snowball snowball = (Snowball)event.getEntity();
         if (this.snowballs.contains(snowball)) {
            if (event.getHitBlock() == null) {
               event.setCancelled(true);
            } else {
               this.snowballs.remove(snowball);
               if (!snowball.getPassengers().isEmpty() && snowball.getPassengers().get(0) instanceof TNTPrimed) {
                  TNTPrimed tntPrimed = (TNTPrimed)snowball.getPassengers().get(0);
                  tntPrimed.setFuseTicks(0);
               }

               event.getEntity().remove();
            }
         }
      }
   }

   @EventHandler
   public void onDamage1(EntityDamageEvent event) {
      if (event.getEntity() instanceof Snowball) {
         Snowball snowball = (Snowball)event.getEntity();
         if (this.snowballs.contains(snowball)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() instanceof Dispenser) {
         Dispenser dispenser = (Dispenser)event.getInventory().getHolder();

         try {
            TnTType type = this.plugin.getData().getTntType(dispenser.getLocation());
            if (type == null || type != TnTType.TNT_GUN) {
               return;
            }
         } catch (IOException var5) {
            return;
         }

         if (event.getRawSlot() < event.getInventory().getSize()) {
            int slot = event.getRawSlot();
            if (slot != 4) {
               event.setCancelled(true);
               return;
            }

            ItemStack current = event.getCurrentItem();
            if (current != null
               && current.hasItemMeta()
               && current.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "locked-pane"), PersistentDataType.BYTE)) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler
   public void onInventoryMoveItem(InventoryMoveItemEvent event) {
      if (event.getSource().getHolder() instanceof Dispenser) {
         Dispenser dispenser = (Dispenser)event.getSource().getHolder();

         try {
            TnTType type = this.plugin.getData().getTntType(dispenser.getLocation());
            if (type != null && type == TnTType.TNT_GUN) {
               event.setCancelled(true);
            }
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

      if (event.getDestination().getHolder() instanceof Dispenser) {
         Dispenser dispenser = (Dispenser)event.getDestination().getHolder();

         try {
            TnTType type = this.plugin.getData().getTntType(dispenser.getLocation());
            if (type != null && type == TnTType.TNT_GUN) {
               event.setCancelled(true);
            }
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   private ItemStack createLockedPane() {
      ItemStack pane = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7 "));
      meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "locked-pane"), PersistentDataType.BYTE, (byte)1);
      pane.setItemMeta(meta);
      return pane;
   }

   private void setupTntGunMenu(Dispenser dispenser) {
      Inventory inv = dispenser.getInventory();
      ItemStack pane = this.createLockedPane();

      for (int slot = 0; slot < inv.getSize(); slot++) {
         if (slot != 4) {
            inv.setItem(slot, pane);
         }
      }
   }

   private boolean isLockedPaneItem(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc.has(new NamespacedKey(this.plugin, "locked-pane"), PersistentDataType.BYTE);
         }
      }
   }

   private void cleanupLockedPaneDropsAround(Chunk centerChunk) {
      World world = centerChunk.getWorld();
      int centerX = centerChunk.getX();
      int centerZ = centerChunk.getZ();

      for (int cx = centerX - 1; cx <= centerX + 1; cx++) {
         for (int cz = centerZ - 1; cz <= centerZ + 1; cz++) {
            Chunk chunk = world.getChunkAt(cx, cz);
            if (chunk.isLoaded()) {
               for (Entity entity : chunk.getEntities()) {
                  if (entity instanceof Item) {
                     Item dropped = (Item)entity;
                     if (this.isLockedPaneItem(dropped.getItemStack())) {
                        entity.remove();
                     }
                  }
               }
            }
         }
      }
   }
}
