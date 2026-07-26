package frozenpon.frozenpotionstacker.Utils;

import frozenpon.frozenpotionstacker.Main;
import java.util.EnumSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
   private final Main stacker;
   private EnumSet<Material> potions;
   public int stackPotion;
   public int stackSplash;
   boolean lookPotion;
   boolean lookSplash;
   private final Material POTION;
   private final Material SPLASH_POTION;

   public InventoryListener(Main plugin, int potion, int splash) {
      this.stacker = plugin;
      this.stackPotion = potion;
      this.stackSplash = splash;
      this.POTION = Material.POTION;
      this.SPLASH_POTION = Material.matchMaterial("SPLASH_POTION");
      this.setupLook();
   }

   private void setupLook() {
      this.lookPotion = this.stackPotion > 1;
      this.lookSplash = this.stackSplash > 1;
      if (this.lookPotion && this.lookSplash) {
         if (this.SPLASH_POTION != null) {
            this.potions = EnumSet.of(this.POTION, this.SPLASH_POTION);
         } else {
            this.potions = EnumSet.of(this.POTION);
         }
      } else if (this.lookPotion) {
         this.potions = EnumSet.of(this.POTION);
      } else {
         this.potions = EnumSet.of(this.SPLASH_POTION);
      }
   }

   public void setStacks(int potion, int splash) {
      this.stackPotion = potion;
      this.stackSplash = splash;
      this.setupLook();
   }

   /**
    * On 1.20.5+ the stack limit comes from the item's own max_stack_size data component, not from a
    * global NMS field, so it has to be stamped onto the item itself. Patching Material.maxStack the
    * way the 1.16.5 build did no longer affects how the server stacks anything.
    */
   private void applyStackSize(ItemStack item) {
      if (item == null || !ReflectionUtils.isModern() || !this.potions.contains(item.getType())) {
         return;
      }

      int size = item.getType() == this.POTION ? this.stackPotion : this.stackSplash;
      if (size <= 1) {
         return;
      }

      ItemMeta meta = item.getItemMeta();
      if (meta != null && (!meta.hasMaxStackSize() || meta.getMaxStackSize() != size)) {
         meta.setMaxStackSize(size);
         item.setItemMeta(meta);
      }
   }

   /**
    * Stamps every potion in an inventory. Without this a stack carried over from 1.16.5 has no
    * max_stack_size component at all, so the server still treats its real limit as 1: the moment
    * the stack is touched it gets clamped and the rest of the potions are destroyed.
    */
   public int sweep(Inventory inventory) {
      if (inventory == null || !ReflectionUtils.isModern()) {
         return 0;
      }

      int fixed = 0;
      for (ItemStack item : inventory.getContents()) {
         if (item != null && this.potions.contains(item.getType())) {
            int before = item.hasItemMeta() && item.getItemMeta().hasMaxStackSize() ? item.getItemMeta().getMaxStackSize() : -1;
            this.applyStackSize(item);
            if (item.hasItemMeta() && item.getItemMeta().hasMaxStackSize() && item.getItemMeta().getMaxStackSize() != before) {
               fixed++;
            }
         }
      }

      return fixed;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.sweep(player.getInventory());
      this.sweep(player.getEnderChest());
      player.updateInventory();
   }

   @EventHandler(ignoreCancelled = true)
   public void onPickup(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player) {
         this.applyStackSize(event.getItem().getItemStack());
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      this.sweep(event.getInventory());
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      InventoryAction action = event.getAction();
      if (action != InventoryAction.CLONE_STACK && action != InventoryAction.HOTBAR_SWAP && action != InventoryAction.NOTHING) {
         this.handleInventoryInteractEvent(event, event.getCurrentItem(), event.getCursor());
      }
   }

   @EventHandler
   public void onIventoryDrag(InventoryDragEvent event) {
      this.handleInventoryInteractEvent(event, event.getOldCursor(), event.getCursor());
   }

   @EventHandler
   public void onInventoryCreative(InventoryCreativeEvent event) {
      this.handleInventoryInteractEvent(event, event.getCurrentItem(), event.getCursor());
   }

   private void handleInventoryInteractEvent(InventoryInteractEvent event, ItemStack stack, ItemStack alt) {
      ItemStack cur;
      if (stack != null && stack.getType() != Material.AIR) {
         cur = stack;
      } else {
         cur = alt;
      }

      if (cur != null && this.potions.contains(cur.getType())) {
         this.applyStackSize(stack);
         this.applyStackSize(alt);
         this.stacker.getServer().getScheduler().runTask(this.stacker, () -> ReflectionUtils.updateInventory((Player)event.getWhoClicked()));
      }
   }
}
