package org.dimasik.litebinds.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.dimasik.litebinds.LiteBinds;
import org.dimasik.litebinds.database.ActionType;
import org.dimasik.litebinds.database.PlayerActions;

public class EventListener implements Listener {
   private final Map<UUID, ItemStack> lastHeldItems = new HashMap<>();

   public EventListener() {
      Bukkit.getScheduler().runTaskTimer(LiteBinds.getInstance(), () -> {
         for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack newItem = player.getInventory().getItemInHand();
            Bukkit.getScheduler().runTaskLater(LiteBinds.getInstance(), () -> this.lastHeldItems.put(player.getUniqueId(), newItem.clone()), 1L);
         }
      }, 0L, 1L);
   }

   @EventHandler
   public void on(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      ItemStack droppedItem = event.getItemDrop().getItemStack();
      ItemStack lastHeldItem = this.lastHeldItems.get(player.getUniqueId());
      if (lastHeldItem != null && lastHeldItem.isSimilar(droppedItem) && droppedItem.getType() == Material.NETHERITE_SWORD) {
         try {
            Optional<PlayerActions> playerActionsOptional = LiteBinds.getInstance().getDatabaseManager().getPlayerActions(player.getName()).get();
            PlayerActions playerActions = playerActionsOptional.orElseGet(
               () -> new PlayerActions(player.getName(), ActionType.NONE, ActionType.NONE, ActionType.NONE)
            );
            ActionType actionType = playerActions.getActionDrop();
            this.trigger(event, player, actionType);
         } catch (Exception var8) {
            throw new RuntimeException(var8);
         }
      }
   }

   @EventHandler
   public void on(PlayerSwapHandItemsEvent event) {
      Player player = event.getPlayer();
      if (event.getOffHandItem() != null && event.getOffHandItem().getType() == Material.NETHERITE_SWORD) {
         try {
            Optional<PlayerActions> playerActionsOptional = LiteBinds.getInstance().getDatabaseManager().getPlayerActions(player.getName()).get();
            PlayerActions playerActions = playerActionsOptional.orElseGet(
               () -> new PlayerActions(player.getName(), ActionType.NONE, ActionType.NONE, ActionType.NONE)
            );
            ActionType actionType = playerActions.getActionSwap();
            this.trigger(event, player, actionType);
         } catch (Exception var6) {
            throw new RuntimeException(var6);
         }
      }
   }

   @EventHandler
   public void on(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (event.getItem() != null && event.getItem().getType() == Material.NETHERITE_SWORD) {
            try {
               Optional<PlayerActions> playerActionsOptional = LiteBinds.getInstance().getDatabaseManager().getPlayerActions(player.getName()).get();
               PlayerActions playerActions = playerActionsOptional.orElseGet(
                  () -> new PlayerActions(player.getName(), ActionType.NONE, ActionType.NONE, ActionType.NONE)
               );
               ActionType actionType = playerActions.getActionInteract();
               this.trigger(event, player, actionType);
            } catch (Exception var6) {
               throw new RuntimeException(var6);
            }
         }
      }
   }

   private void trigger(Cancellable event, Player player, ActionType actionType) {
      switch (actionType) {
         case SNOWBALL:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackxxxxxx : player.getInventory()) {
               if (itemStackxxxxxx != null
                  && itemStackxxxxxx.hasItemMeta()
                  && ((String)itemStackxxxxxx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("snowball")) {
                  Snowball snowball = (Snowball)player.launchProjectile(Snowball.class);
                  snowball.setItem(itemStackxxxxxx);
                  snowball.setShooter(player);
                  PlayerLaunchProjectileEvent ev = new PlayerLaunchProjectileEvent(player, itemStackxxxxxx, snowball);
                  Bukkit.getServer().getPluginManager().callEvent(ev);
                  if (!ev.isCancelled()) {
                     itemStackxxxxxx.setAmount(itemStackxxxxxx.getAmount() - 1);
                  }

                  return;
               }
            }
            break;
         case JAKE:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackxxxxx : player.getInventory()) {
               if (itemStackxxxxx != null
                  && itemStackxxxxx.hasItemMeta()
                  && ((String)itemStackxxxxx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("jake")) {
                  Bukkit.getServer()
                     .getPluginManager()
                     .callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStackxxxxx, null, BlockFace.DOWN));
                  return;
               }
            }
            break;
         case ALTERNATIVE_TRAP:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackxxxx : player.getInventory()) {
               if (itemStackxxxx != null
                  && itemStackxxxx.hasItemMeta()
                  && ((String)itemStackxxxx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("alternative_trap")) {
                  Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStackxxxx, null, BlockFace.DOWN));
                  return;
               }
            }
            break;
         case STAN:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackxxx : player.getInventory()) {
               if (itemStackxxx != null
                  && itemStackxxx.hasItemMeta()
                  && ((String)itemStackxxx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("stan")) {
                  Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStackxxx, null, BlockFace.DOWN));
                  return;
               }
            }
            break;
         case TRAP:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackxx : player.getInventory()) {
               if (itemStackxx != null
                  && itemStackxx.hasItemMeta()
                  && ((String)itemStackxx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("explosive_trap")) {
                  Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStackxx, null, BlockFace.DOWN));
                  return;
               }
            }
            break;
         case EXPLOSIVE:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStackx : player.getInventory()) {
               if (itemStackx != null
                  && itemStackx.hasItemMeta()
                  && ((String)itemStackx.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "item-type"), PersistentDataType.STRING, "none"))
                     .equalsIgnoreCase("explosive_stuff")) {
                  Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStackx, null, BlockFace.DOWN));
                  return;
               }
            }
            break;
         case BACKPACK:
            Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyBackPack");
            if (plugin == null) {
               return;
            }

            event.setCancelled(true);

            for (ItemStack itemStack : player.getInventory()) {
               if (itemStack != null
                  && itemStack.hasItemMeta()
                  && (Integer)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "backpack-level"), PersistentDataType.INTEGER, -1)
                     == 4) {
                  Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStack, null, BlockFace.DOWN));
                  return;
               }
            }
      }
   }
}
