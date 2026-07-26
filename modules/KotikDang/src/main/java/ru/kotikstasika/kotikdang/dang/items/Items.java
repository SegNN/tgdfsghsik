package ru.kotikstasika.kotikdang.dang.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;

public class Items implements Listener {
   private final KotikDang plugin;
   public static NamespacedKey compassKey;
   public static NamespacedKey universalKey;
   private final NamespacedKey compassCooldownKey;
   private final NamespacedKey compassTargetIdKey;
   private final NamespacedKey compassTargetWorldKey;
   private final NamespacedKey compassTargetXKey;
   private final NamespacedKey compassTargetYKey;
   private final NamespacedKey compassTargetZKey;
   private final Map<UUID, Long> clickDebounceMs = new HashMap<>();
   private static final long CLICK_DEBOUNCE = 500L;
   private static final long EIGHT_HOURS = 28800000L;

   public Items(KotikDang plugin) {
      this.plugin = plugin;
      compassKey = new NamespacedKey(plugin, "compass");
      universalKey = new NamespacedKey(plugin, "universal_key");
      this.compassCooldownKey = new NamespacedKey(plugin, "compass_cd_until");
      this.compassTargetIdKey = new NamespacedKey(plugin, "compass_target_id");
      this.compassTargetWorldKey = new NamespacedKey(plugin, "compass_target_world");
      this.compassTargetXKey = new NamespacedKey(plugin, "compass_target_x");
      this.compassTargetYKey = new NamespacedKey(plugin, "compass_target_y");
      this.compassTargetZKey = new NamespacedKey(plugin, "compass_target_z");
   }

   public void giveCompass(Player player) {
      ItemStack item = new ItemStack(Material.COMPASS);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&3&B&C&7&F&BО&x&3&E&C&C&F&AС&x&4&1&D&1&F&9о&x&4&3&D&6&F&7б&x&4&6&D&B&F&6ы&x&4&9&E&0&F&5й &x&4&C&E&4&F&4к&x&4&F&E&9&F&3о&x&5&2&E&E&F&2м&x&5&4&F&3&F&0п&x&5&7&F&8&E&Fа&x&5&A&F&D&E&Eс"
         )
      );
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&8Особенность:"));
      lore.add(
         ChatColor.translateAlternateColorCodes(
            '&', "&5&o&7 • &x&E&7&E&7&E&7ведёт к &x&5&A&F&D&E&Eближайшемуx&E&7&E&7&E&7 или &x&5&A&F&D&E&Eслучайному сокровищу&x&E&7&E&7&E&7,"
         )
      );
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o   &x&E&7&E&7&E&7после нажатия &x&5&A&F&D&E&EПКМ&x&E&7&E&7&E&7, держа предмет в руках;"));
      lore.add(
         ChatColor.translateAlternateColorCodes(
            '&', "&5&o&7 • &x&E&7&E&7&E&7можно использовать&x&5&A&F&D&E&E раз&x&E&7&E&7&E&7 в &x&5&A&F&D&E&E8 часов&x&E&7&E&7&E&7."
         )
      );
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      meta.setLore(lore);
      meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      meta.getPersistentDataContainer().set(compassKey, PersistentDataType.STRING, "1");
      item.setItemMeta(meta);
      player.getInventory().addItem(new ItemStack[]{item});
   }

   public void giveUnick(Player player) {
      ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
      ItemMeta meta = item.getItemMeta();
      meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Универсальный ключ"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&f"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&fИспользуйте данный ключ для открытия"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&fданжа с секретным хранилищем."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&f"));
      meta.setLore(lore);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      meta.getPersistentDataContainer().set(universalKey, PersistentDataType.BYTE, (byte)1);
      item.setItemMeta(meta);
      player.getInventory().addItem(new ItemStack[]{item});
   }

   public boolean isUniversalKey(ItemStack item) {
      return item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().has(universalKey, PersistentDataType.BYTE) : false;
   }

   public boolean consumeKey(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      if (this.isUniversalKey(mainHand)) {
         if (mainHand.getAmount() > 1) {
            mainHand.setAmount(mainHand.getAmount() - 1);
         } else {
            player.getInventory().setItemInMainHand(null);
         }

         return true;
      } else {
         return false;
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      ItemStack item = event.getItemInHand();
      if (item != null && item.hasItemMeta()) {
         PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
         if (container.has(universalKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onRightClick(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            long now = System.currentTimeMillis();
            Long lastClick = this.clickDebounceMs.get(player.getUniqueId());
            if (lastClick == null || now - lastClick >= 500L) {
               this.clickDebounceMs.put(player.getUniqueId(), now);
               ItemStack item = player.getInventory().getItemInMainHand();
               if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
                  ItemMeta meta = item.getItemMeta();
                  PersistentDataContainer container = meta.getPersistentDataContainer();
                  if (container.has(compassKey, PersistentDataType.STRING)) {
                     this.handleCompassUse(event, player, meta, container, now);
                  }
               }
            }
         }
      }
   }

   private void handleCompassUse(PlayerInteractEvent event, Player player, ItemMeta meta, PersistentDataContainer container, long now) {
      Long cooldownUntil = (Long)container.get(this.compassCooldownKey, PersistentDataType.LONG);
      if (cooldownUntil != null && now < cooldownUntil) {
         long timeLeftMs = cooldownUntil - now;
         String timeFormatted = this.formatHms(timeLeftMs);
         player.sendMessage(
            ChatColor.translateAlternateColorCodes(
               '&', "&c▶ &fПоиск нового сокровища при помощи этого компаса будет доступен только через &c" + timeFormatted + "&f."
            )
         );
         String worldName = (String)container.get(this.compassTargetWorldKey, PersistentDataType.STRING);
         Double x = (Double)container.get(this.compassTargetXKey, PersistentDataType.DOUBLE);
         Double y = (Double)container.get(this.compassTargetYKey, PersistentDataType.DOUBLE);
         Double z = (Double)container.get(this.compassTargetZKey, PersistentDataType.DOUBLE);
         if (worldName != null && x != null && y != null && z != null) {
            World targetWorld = Bukkit.getWorld(worldName);
            if (targetWorld != null) {
               Location lastTarget = new Location(targetWorld, x, y, z);
               player.setCompassTarget(lastTarget);
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     String.format(
                        "&b▶ &fСтрелка компаса установлена на последний найденный регион по координатам: &b%d&f, &b%d&f.",
                        lastTarget.getBlockX(),
                        lastTarget.getBlockZ()
                     )
                  )
               );
            }
         }
      } else {
         this.findAndSetNewTarget(player, meta, container, now);
      }
   }

   private void findAndSetNewTarget(Player player, ItemMeta meta, PersistentDataContainer container, long now) {
      DangGrief dangGrief = this.plugin.getDangGriefInstance();
      if (dangGrief != null && dangGrief.isDatabaseConnected()) {
         Random random = new Random();
         String targetId = dangGrief.getRandomUnraidedDangId(player.getWorld(), random);
         if (targetId == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c▶ &fНе удалось найти подходящее сокровище. Попробуйте позже"));
         } else {
            Location targetLocation = dangGrief.getDangLocation(targetId);
            if (targetLocation == null) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c▶ &fНе удалось найти подходящее сокровище. Попробуйте позже"));
            } else {
               container.set(this.compassTargetIdKey, PersistentDataType.STRING, targetId);
               container.set(this.compassTargetWorldKey, PersistentDataType.STRING, targetLocation.getWorld().getName());
               container.set(this.compassTargetXKey, PersistentDataType.DOUBLE, targetLocation.getX());
               container.set(this.compassTargetYKey, PersistentDataType.DOUBLE, targetLocation.getY());
               container.set(this.compassTargetZKey, PersistentDataType.DOUBLE, targetLocation.getZ());
               container.set(this.compassCooldownKey, PersistentDataType.LONG, now + 28800000L);
               ItemStack itemInHand = player.getInventory().getItemInMainHand();
               itemInHand.setItemMeta(meta);
               player.setCompassTarget(targetLocation);
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     String.format(
                        "&b▶ &fНайдено сокровище по координатам: &b%d&f, &b%d&f. Теперь этот компас будет снова доступен только через 8 часов.",
                        targetLocation.getBlockX(),
                        targetLocation.getBlockZ()
                     )
                  )
               );
            }
         }
      } else {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &cНевозможно получить координаты сейчас. Повторите позже."));
      }
   }

   private String formatHms(long milliseconds) {
      long totalSeconds = Math.max(0L, milliseconds / 1000L);
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
   }
}
