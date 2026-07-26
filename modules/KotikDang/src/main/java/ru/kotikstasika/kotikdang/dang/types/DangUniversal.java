package ru.kotikstasika.kotikdang.dang.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.items.Items;
import ru.kotikstasika.kotikdang.data.Rarity;
import ru.kotikstasika.kotikdang.data.UniqueShulkerLocation;
import ru.kotikstasika.kotikdang.data.UniqueShulkerSession;
import ru.kotikstasika.kotikdang.menu.UniqueShulkerMenu;
import ru.kotikstasika.kotikdang.tasks.UniqueShulkerOpenTask;
import ru.kotikstasika.kotikdang.util.ColorUtil;
import ru.kotikstasika.kotikdang.util.HologramManager;

public class DangUniversal implements Listener {
   private final KotikDang plugin;
   private final LootAPI lootAPI;
   private final Map<UUID, UniqueShulkerSession> activeSessions = new ConcurrentHashMap<>();
   private final Map<UUID, UniqueShulkerSession> locationSessions = new ConcurrentHashMap<>();
   private final Map<String, UniqueShulkerLocation> shulkerLocations = new ConcurrentHashMap<>();
   private final Set<UUID> viewers = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Map<UUID, Inventory> viewerInventories = new ConcurrentHashMap<>();
   private final HologramManager hologramManager;
   private final Items items;

   public DangUniversal(KotikDang plugin, Items items) {
      this.plugin = plugin;
      this.lootAPI = new LootAPI();
      this.hologramManager = new HologramManager(plugin);
      this.items = items;
      Bukkit.getScheduler().runTaskLater(plugin, this::initializeAllShulkers, 40L);
   }

   private void initializeAllShulkers() {
      ConfigurationSection unicalSection = this.plugin.getConfig().getConfigurationSection("unical");
      if (unicalSection != null) {
         for (String key : unicalSection.getKeys(false)) {
            ConfigurationSection shulkerConfig = unicalSection.getConfigurationSection(key);
            if (shulkerConfig != null && shulkerConfig.contains("location")) {
               String worldName = shulkerConfig.getString("location.world");
               double x = shulkerConfig.getDouble("location.x");
               double y = shulkerConfig.getDouble("location.y");
               double z = shulkerConfig.getDouble("location.z");
               float yaw = (float)shulkerConfig.getDouble("location.yaw");
               float pitch = (float)shulkerConfig.getDouble("location.pitch");
               World world = Bukkit.getWorld(worldName);
               if (world != null) {
                  Location location = new Location(world, x, y, z, yaw, pitch);
                  UniqueShulkerLocation shulkerLoc = new UniqueShulkerLocation(UUID.randomUUID(), location, key, shulkerConfig.getInt("lootlvl", 3));
                  this.shulkerLocations.put(key, shulkerLoc);
                  this.updateShulkerBlock(shulkerLoc);
                  this.hologramManager.createIdleHologram(shulkerLoc);
               }
            }
         }
      }
   }

   private void updateShulkerBlock(UniqueShulkerLocation shulkerLocation) {
      Location loc = shulkerLocation.getLocation();
      if (loc != null && loc.getWorld() != null) {
         Block block = loc.getBlock();
         Material shulkerMaterial = this.getShulkerMaterial(shulkerLocation.getCurrentRarity());
         block.setType(shulkerMaterial);
      }
   }

   private Material getShulkerMaterial(Rarity rarity) {
      return switch (rarity) {
         case PURPLE -> Material.PURPLE_SHULKER_BOX;
         case RED -> Material.RED_SHULKER_BOX;
      };
   }

   public void addLocation(Location location, String unicalName) {
      ConfigurationSection unicalSection = this.plugin.getConfig().getConfigurationSection("unical");
      if (unicalSection == null) {
         unicalSection = this.plugin.getConfig().createSection("unical");
      }

      ConfigurationSection shulkerConfig = unicalSection.getConfigurationSection(unicalName);
      if (shulkerConfig == null) {
         shulkerConfig = unicalSection.createSection(unicalName);
         shulkerConfig.set("lootlvl", 3);
      }

      int lootLevel = shulkerConfig.getInt("lootlvl", 3);
      shulkerConfig.set("location.world", location.getWorld().getName());
      shulkerConfig.set("location.x", location.getX());
      shulkerConfig.set("location.y", location.getY());
      shulkerConfig.set("location.z", location.getZ());
      shulkerConfig.set("location.yaw", location.getYaw());
      shulkerConfig.set("location.pitch", location.getPitch());
      this.plugin.saveConfig();
      UniqueShulkerLocation shulkerLoc = new UniqueShulkerLocation(UUID.randomUUID(), location, unicalName, lootLevel);
      this.shulkerLocations.put(unicalName, shulkerLoc);
      this.updateShulkerBlock(shulkerLoc);
      this.hologramManager.createIdleHologram(shulkerLoc);
   }

   public boolean startSession(Player player, UniqueShulkerLocation shulkerLocation) {
      if (this.locationSessions.containsKey(shulkerLocation.getId())) {
         return false;
      } else {
         Inventory inventory = UniqueShulkerMenu.create(this.plugin, shulkerLocation.getLootLevel());
         UniqueShulkerSession session = new UniqueShulkerSession(player, shulkerLocation, inventory);
         this.activeSessions.put(player.getUniqueId(), session);
         this.locationSessions.put(shulkerLocation.getId(), session);
         this.hologramManager.removeIdleHologram(shulkerLocation);
         this.hologramManager.createOpeningHologram(session);
         player.openInventory(inventory);
         UniqueShulkerOpenTask task = new UniqueShulkerOpenTask(this.plugin, session, this.hologramManager);
         task.runTaskTimer(this.plugin, 20L, 20L);
         session.setTask(task);
         player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.5F);
         return true;
      }
   }

   public void endSession(Player player, boolean completed) {
      UniqueShulkerSession session = this.activeSessions.remove(player.getUniqueId());
      if (session != null) {
         this.locationSessions.remove(session.getShulkerLocation().getId());
         session.cancel();
         player.closeInventory();
         this.closeAllViewers(session);
         this.hologramManager.removeOpeningHologram(session);
         this.respawnShulker(session.getShulkerLocation());
      }
   }

   private void closeAllViewers(UniqueShulkerSession session) {
      Inventory sessionInv = session.getInventory();

      for (Entry<UUID, Inventory> entry : new ArrayList<>(this.viewerInventories.entrySet())) {
         if (entry.getValue().equals(sessionInv)) {
            Player viewer = Bukkit.getPlayer(entry.getKey());
            if (viewer != null) {
               viewer.closeInventory();
            }

            this.viewers.remove(entry.getKey());
            this.viewerInventories.remove(entry.getKey());
         }
      }
   }

   public void stopSession(Player player) {
      UniqueShulkerSession session = this.activeSessions.get(player.getUniqueId());
      if (session != null) {
         this.endSession(player, false);
      }
   }

   public void respawnShulker(UniqueShulkerLocation shulkerLocation) {
      shulkerLocation.respawn();
      this.updateShulkerBlock(shulkerLocation);
      this.hologramManager.createIdleHologram(shulkerLocation);
   }

   public void respawnAllShulkers() {
      for (UniqueShulkerLocation shulkerLoc : this.shulkerLocations.values()) {
         this.respawnShulker(shulkerLoc);
      }
   }

   public boolean isShulkerBusy(UniqueShulkerLocation location) {
      return this.locationSessions.containsKey(location.getId());
   }

   public UniqueShulkerSession getSession(Player player) {
      return this.activeSessions.get(player.getUniqueId());
   }

   public UniqueShulkerSession getSessionByLocation(UniqueShulkerLocation location) {
      return this.locationSessions.get(location.getId());
   }

   public void stopAllSessions() {
      for (UniqueShulkerSession session : new ArrayList<>(this.activeSessions.values())) {
         this.endSession(session.getPlayer(), false);
      }
   }

   public boolean hasActiveSession(Player player) {
      return this.activeSessions.containsKey(player.getUniqueId());
   }

   public boolean isViewer(Player player) {
      return this.viewers.contains(player.getUniqueId());
   }

   public UniqueShulkerLocation getShulkerLocationAt(Location location) {
      for (UniqueShulkerLocation shulkerLoc : this.shulkerLocations.values()) {
         Location loc = shulkerLoc.getLocation();
         if (loc != null
            && loc.getBlockX() == location.getBlockX()
            && loc.getBlockY() == location.getBlockY()
            && loc.getBlockZ() == location.getBlockZ()
            && loc.getWorld() != null
            && loc.getWorld().equals(location.getWorld())) {
            return shulkerLoc;
         }
      }

      return null;
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();
            if (block != null) {
               Player player = event.getPlayer();
               Location location = block.getLocation();
               UniqueShulkerLocation shulkerLocation = this.getShulkerLocationAt(location);
               if (shulkerLocation != null) {
                  event.setCancelled(true);
                  if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                     UniqueShulkerSession existingSession = this.getSession(player);
                     if (existingSession != null) {
                        if (existingSession.getShulkerLocation().getId().equals(shulkerLocation.getId())) {
                           if (existingSession.getRemainingSeconds() > 0) {
                              player.openInventory(existingSession.getInventory());
                           } else {
                              player.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fВремя открытия истекло!"));
                           }
                        } else {
                           player.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fВы уже открываете другой шалкер!"));
                        }

                        return;
                     }

                     if (this.isShulkerBusy(shulkerLocation)) {
                        UniqueShulkerSession activeSession = this.getSessionByLocation(shulkerLocation);
                        if (activeSession != null && activeSession.getRemainingSeconds() > 0) {
                           this.viewers.add(player.getUniqueId());
                           this.viewerInventories.put(player.getUniqueId(), activeSession.getInventory());
                           player.openInventory(activeSession.getInventory());
                        } else {
                           player.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fЭтот шалкер уже открывается другим игроком!"));
                        }

                        return;
                     }

                     ItemStack mainHand = player.getInventory().getItemInMainHand();
                     if (!this.items.isUniversalKey(mainHand)) {
                        player.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fЭтот шалкер можно открыть только &#FB6335Универсальным ключом&f."));
                        return;
                     }

                     if (!player.hasPermission("kotikdang.unique.use")) {
                        player.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fУ вас нет прав!"));
                        return;
                     }

                     this.items.consumeKey(player);
                     this.startSession(player, shulkerLocation);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (this.viewers.contains(player.getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (this.viewers.contains(player.getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         if (this.viewers.contains(player.getUniqueId())) {
            this.viewers.remove(player.getUniqueId());
            this.viewerInventories.remove(player.getUniqueId());
            return;
         }

         UniqueShulkerSession session = this.getSession(player);
         if (session != null) {
            session.setInventoryOpen(false);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (this.viewers.contains(player.getUniqueId())) {
         this.viewers.remove(player.getUniqueId());
         this.viewerInventories.remove(player.getUniqueId());
      }

      if (this.hasActiveSession(player)) {
         this.stopSession(player);
      }
   }

   public List<ItemStack> generateLoot(Rarity rarity, int count) {
      List<ItemStack> result = new ArrayList<>();

      try {
         int rarityLevel = rarity == Rarity.PURPLE ? 3 : 4;
         List<ItemStack> generatedLoot = this.lootAPI.getRandomItems(count, rarityLevel);
         result.addAll(generatedLoot);
      } catch (Exception var6) {
         this.plugin.getLogger().warning("Ошибка при генерации лута: " + var6.getMessage());
      }

      return result;
   }

   public Rarity getRandomRarity() {
      return Rarity.random();
   }
}
