package ru.kotikstasika.holyliteend.end.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.end.managers.EndEvent;
import ru.kotikstasika.holyliteend.utils.ColorUtils;
import ua.fichatime.fichaspawneroved.item.EggType;
import ua.fichatime.fichaspawneroved.item.Item;

public class EndListener implements Listener {
   private final HolyLiteEnd plugin;
   private final Map<UUID, Long> lastMessageTime = new HashMap<>();
   private final Map<UUID, Long> lastTeleportTime = new HashMap<>();
   private final List<String> blockedCommands = Arrays.asList("/sethome", "/createhome", "/clan", "/setwarp");

   public EndListener(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDragonSpawn(EntitySpawnEvent event) {
      if (event.getEntity() instanceof EnderDragon && EndEvent.isEventActive()) {
         this.plugin.getDragonManager().onDragonSpawn((EnderDragon)event.getEntity());
      }
   }

   @EventHandler
   public void onDragonDamage(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof EnderDragon) {
         this.plugin.getDragonManager().onDragonDamage(event);
      }
   }

   @EventHandler
   public void onDragonDeath(EntityDeathEvent event) {
      if (event.getEntity() instanceof EnderDragon) {
         event.getDrops().removeIf(item -> item.getType() == Material.DRAGON_EGG);
         this.plugin.getDragonManager().onDragonDeath(event);
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      World targetWorld = event.getTo().getWorld();
      boolean hasEgg = false;
      ItemStack[] contents = player.getInventory().getContents();

      for (ItemStack item : contents) {
         if (item != null && EggItem.isDragonEgg(item)) {
            hasEgg = true;
            break;
         }
      }

      if (targetWorld != null && targetWorld.getName().equals("world_nether") && hasEgg) {
         event.setCancelled(true);
         player.sendMessage(
            ColorUtils.translateColors(
               "&x&f&b&0&0&0&0▶ &fВы не можете зайти в данным мир с &x&6&A&2&0&A&FЯ&x&7&4&2&3&B&Fй&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа&f!"
            )
         );
      } else {
         if (targetWorld != null && targetWorld.getName().equals("world_the_end")) {
            long currentTime = System.currentTimeMillis();
            long lastTeleport = this.lastTeleportTime.getOrDefault(player.getUniqueId(), 0L);
            if (currentTime - lastTeleport < 1000L) {
               return;
            }

            if (!EndEvent.isEventActive()) {
               event.setCancelled(true);
               long lastTime = this.lastMessageTime.getOrDefault(player.getUniqueId(), 0L);
               if (currentTime - lastTime > 500L) {
                  player.sendMessage(ColorUtils.translateColors("&b▶ &fВ настоящий момент измерение закрыто. &bПриходи в воскресенье в 16:00!"));
                  this.lastMessageTime.put(player.getUniqueId(), currentTime);
               }
            } else {
               Location spawnLocation = this.plugin.getEndSpawnLocation();
               if (spawnLocation != null && event.getCause() == TeleportCause.END_PORTAL) {
                  event.setCancelled(true);
                  this.lastTeleportTime.put(player.getUniqueId(), currentTime);
                  this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                     player.teleport(spawnLocation);
                     this.showBossBarOnJoin(player);
                  }, 1L);
               } else {
                  this.showBossBarOnJoin(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      this.showBossBarOnJoin(player);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.removeDragonEggsAndDrop(player);
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removeDragonEggs(player), 1L);
      this.showBossBarOnJoin(player);
      if (!EndEvent.isEventActive()) {
         World world = player.getWorld();
         if (world.getName().equals("world_the_end")) {
            (new BukkitRunnable() {
               public void run() {
                  Location spawnLocation = new Location(Bukkit.getWorld("spawn"), 0.0, 100.0, 0.0);
                  player.teleport(spawnLocation);
               }
            }).runTaskLater(this.plugin, 2L);
         }
      }
   }

   public void showBossBarOnJoin(Player player) {
      if (EndEvent.isEventActive()) {
         if (this.plugin.getEndBossBarManager().getEndBossBar() != null && !this.plugin.getEndBossBarManager().getEndBossBar().getPlayers().contains(player)) {
            this.plugin.getEndBossBarManager().getEndBossBar().addPlayer(player);
            long remainingTime = this.plugin.getEndEventManager().getRemainingTime();
            this.plugin.getEndBossBarManager().updateBossBar(remainingTime);
         }

         if (this.plugin.getDragonManager() != null) {
            this.plugin.getDragonManager().addPlayerToDragonBossBar(player);
         }
      }
   }

   private void removeDragonEggsAndDrop(Player player) {
      PlayerInventory inventory = player.getInventory();
      boolean changed = false;

      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack item = inventory.getItem(slot);
         if (this.isDragonEgg(item)) {
            player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
            inventory.setItem(slot, null);
            changed = true;
         }
      }

      ItemStack[] armor = inventory.getArmorContents();

      for (int i = 0; i < armor.length; i++) {
         if (this.isDragonEgg(armor[i])) {
            player.getWorld().dropItemNaturally(player.getLocation(), armor[i].clone());
            armor[i] = null;
            changed = true;
         }
      }

      inventory.setArmorContents(armor);
      ItemStack offhand = inventory.getItemInOffHand();
      if (this.isDragonEgg(offhand)) {
         player.getWorld().dropItemNaturally(player.getLocation(), offhand.clone());
         inventory.setItemInOffHand(null);
         changed = true;
      }

      if (changed) {
         player.updateInventory();
      }
   }

   private void removeDragonEggs(Player player) {
      PlayerInventory inventory = player.getInventory();
      boolean changed = false;

      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack item = inventory.getItem(slot);
         if (this.isDragonEgg(item)) {
            inventory.setItem(slot, null);
            changed = true;
         }
      }

      ItemStack[] armor = inventory.getArmorContents();

      for (int i = 0; i < armor.length; i++) {
         if (this.isDragonEgg(armor[i])) {
            armor[i] = null;
            changed = true;
         }
      }

      inventory.setArmorContents(armor);
      if (this.isDragonEgg(inventory.getItemInOffHand())) {
         inventory.setItemInOffHand(null);
         changed = true;
      }

      if (changed) {
         player.updateInventory();
      }
   }

   private boolean isDragonEgg(ItemStack item) {
      return item != null && item.getType() == Material.DRAGON_EGG;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld().getName().equals("world_the_end") && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
         player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fИспользование этого эффекта &x&f&b&0&0&0&0запрещено в енд-мире"));
         player.removePotionEffect(PotionEffectType.INVISIBILITY);
      }

      if (!EndEvent.isEventActive()) {
         World world = player.getWorld();
         if (world.getName().equals("world_the_end")) {
            Location spawnLocation = new Location(Bukkit.getWorld("spawn"), 0.0, 100.0, 0.0);
            player.teleport(spawnLocation);
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld().getName().equals("world_the_end")) {
         Material type = event.getBlock().getType();
         if (type == Material.TNT || type == Material.DISPENSER || type == Material.RAIL || type == Material.BLACK_CONCRETE_POWDER) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fДанный предмет недоступен в энд-мире!"));
         }
      }
   }

   @EventHandler
   public void onEntitySpawn(EntitySpawnEvent event) {
      if (event.getEntity().getWorld().getName().equals("world_the_end") && event.getEntity().getType() == EntityType.ENDER_CRYSTAL) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld().getName().equals("world_the_end") && event.getBlock().getType() == Material.BLACK_CONCRETE_POWDER) {
         event.setCancelled(true);
         event.getBlock().setType(Material.AIR);
         int amount = (int)(Math.random() * 5.0) + 1;
         Item itemCreator = new Item(this.plugin.getFichaSpawneroved());
         ItemStack endSplinterEgg = itemCreator.createEgg(EggType.END_SPLINTER_EGG);
         endSplinterEgg.setAmount(amount);
         event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), endSplinterEgg);
      }

      if (event.getBlock().getType() == Material.CHEST) {
         Location loc = event.getBlock().getLocation();

         try {
            this.plugin.getDatabaseManager().removeDragonEgg(loc);
         } catch (Exception var6) {
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld().getName().equals("world_the_end")) {
         if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.END_CRYSTAL) {
            event.setCancelled(true);
            event.getClickedBlock().setType(Material.AIR);
            if (player.getOpenInventory() != null && player.getOpenInventory().getType() != InventoryType.PLAYER) {
               player.closeInventory();
            }

            return;
         }

         if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
            event.getClickedBlock().setType(Material.AIR);
            return;
         }
      }
   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld().getName().equals("world_the_end")) {
         String command = event.getMessage().toLowerCase();

         for (String blockedCommandStr : this.blockedCommands) {
            if (player.hasPermission("holyliteend.admin")) {
               return;
            }

            if (command.startsWith(blockedCommandStr.toLowerCase())) {
               event.setCancelled(true);
               player.sendMessage(
                  ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fВы не можете использовать эту &x&f&b&0&0&0&0команду &fв &x&f&b&0&0&0&0энд-мире!")
               );
            }
         }
      }
   }
}
