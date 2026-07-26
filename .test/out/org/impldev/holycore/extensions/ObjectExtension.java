package org.impldev.holycore.extensions;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.impldev.holycore.HolyCore;
import org.impldev.holycore.checker.ShulkerTotemChecker;

public class ObjectExtension implements Listener {
   private final HolyCore plugin;
   private final Essentials essentials;

   public ObjectExtension(HolyCore plugin, Essentials essentials) {
      this.plugin = plugin;
      this.essentials = essentials;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      event.getPlayer().setGlowing(false);
      event.getPlayer().setInvulnerable(false);
      event.setJoinMessage(null);

      for (World world : Bukkit.getWorlds()) {
         world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      event.setQuitMessage(null);
   }

   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      this.handlePlayerLeave(event.getPlayer());
   }

   @EventHandler
   public void onLeave(PlayerKickEvent event) {
      this.handlePlayerLeave(event.getPlayer());
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      event.setDeathMessage(null);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onAdvacement(PlayerAdvancementDoneEvent event) {
   }

   private void handlePlayerLeave(Player player) {
      ShulkerTotemChecker checker = new ShulkerTotemChecker(this.plugin);
      checker.checkShulker(player);
      int shulkers = 0;
      PlayerInventory inventory = player.getInventory();

      for (int i = 0; i <= 40; i++) {
         ItemStack itemStack = inventory.getItem(i);
         if (itemStack != null && itemStack.getType().toString().endsWith("SHULKER_BOX")) {
            if (++shulkers > 1) {
               inventory.setItem(i, null);
               player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
               player.spigot()
                  .sendMessage(
                     ChatMessageType.ACTION_BAR,
                     TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&x&F&C&0&0&C&9Режим шалкера. &fЛивнешь - все шалкера выпадут."))
                  );
            }
         }
      }
   }

   @EventHandler
   public void onPickup(PlayerPickupItemEvent event) {
      ShulkerTotemChecker checker = new ShulkerTotemChecker(this.plugin);
      checker.checkShulker(event.getPlayer());
      if (event.getItem().getItemStack().getType() == Material.TOTEM_OF_UNDYING) {
         int totems = 0;

         for (ItemStack itemStack : event.getPlayer().getInventory()) {
            if (itemStack != null && itemStack.getType() == Material.TOTEM_OF_UNDYING) {
               if (++totems >= 4) {
                  event.getPlayer()
                     .spigot()
                     .sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(
                           ChatColor.translateAlternateColorCodes('&', "&c ▶ &fВы не можете носить с собой больше &6x4 Тотем бессмертия")
                        )
                     );
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onSpawner(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK
         && event.getItem() != null
         && event.getClickedBlock() != null
         && event.getClickedBlock().getType() == Material.SPAWNER) {
         ItemStack itemStack = event.getItem();
         Material material = itemStack.getType();
         if (material == Material.CREEPER_SPAWN_EGG || material == Material.WITCH_SPAWN_EGG || material == Material.VILLAGER_SPAWN_EGG) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c▶ &fЭтого моба нельзя вставить в спавнер!"));
         }
      }
   }

   @EventHandler
   public void onShield(PlayerInteractEvent event) {
      if (event.getItem() != null && event.getItem().getType() == Material.SHIELD) {
         event.getItem().setAmount(0);
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onMending(PlayerItemMendEvent event) {
      ItemStack item = event.getItem();
      if (item != null) {
         String name = item.getType().toString().toUpperCase();
         if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS")) {
            int repairAmount = event.getRepairAmount();
            event.getPlayer().giveExp((int)(repairAmount * 0.25));
            int mendingLevel = item.getEnchantmentLevel(Enchantment.MENDING);
            if (mendingLevel < 1) {
               mendingLevel = 1;
            }

            int newRepairAmount = (int)(repairAmount * 0.75 * mendingLevel / 2.0);
            if (newRepairAmount < 1 && item.getDurability() > 0) {
               newRepairAmount = 1;
            }

            event.setRepairAmount(newRepairAmount);
         }
      }
   }

   @EventHandler
   public void onKick(PlayerKickEvent event) {
      if (event.getReason().equals("Kicked for spamming")) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPickup(PlayerAttemptPickupItemEvent event) {
      Player player = event.getPlayer();
      if (this.essentials.getUser(player).isVanished()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onCommand(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (event.getMessage().startsWith("/warp logo") && !player.hasPermission("essentials.warp.logo")) {
         event.setCancelled(true);
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cОшибка: &4Такого варпа не существует."));
      } else if (event.getMessage().startsWith("/sethome")) {
         com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
         Location weLocation = BukkitAdapter.adapt(player.getLocation());
         RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
         RegionQuery query = container.createQuery();

         for (ProtectedRegion region : query.getApplicableRegions(weLocation)) {
            if (!region.getOwners().contains(player.getUniqueId()) && !region.getMembers().contains(player.getUniqueId())) {
               event.setCancelled(true);
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', "&x&F&B&2&2&2&2 ▶ &fВы не можете использовать эту команду &x&F&B&2&2&2&2в чужом регионе")
               );
               return;
            }
         }
      }
   }

   @EventHandler
   public void death(PlayerDeathEvent event) {
      event.setDeathMessage(null);
      Player player = event.getEntity();
      Player killer = player.getKiller();
      org.bukkit.Location location = player.getLocation();
      int x = (int)Math.round(location.getX());
      int y = (int)Math.round(location.getY());
      int z = (int)Math.round(location.getZ());
      if (killer != null) {
         player.sendMessage(
            ChatColor.translateAlternateColorCodes(
               '&',
               "&x&F&B&0&0&0&0▶ &fВы убиты игроком &x&F&B&0&0&0&0" + killer.getName() + " &fна координатах &x&F&B&0&0&0&0" + x + ", " + y + ", " + z + "&f."
            )
         );
      } else {
         player.sendMessage(
            ChatColor.translateAlternateColorCodes('&', "&x&F&B&0&0&0&0▶ &fВы погибли на координатах &x&F&B&0&0&0&0" + x + ", " + y + ", " + z + "&f.")
         );
      }
   }
}
