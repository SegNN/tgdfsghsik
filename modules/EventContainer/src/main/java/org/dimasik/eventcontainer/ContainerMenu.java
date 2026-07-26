package org.dimasik.eventcontainer;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.lootmanager.LootAPI;

public class ContainerMenu {
   public EventContainer plugin;
   public Player player;
   public int level;
   public int itemlevel = 1;

   public ContainerMenu(EventContainer plugin, Player player, int level) {
      this.plugin = plugin;
      this.player = player;
      this.level = level;
      this.open();
   }

   public void open() {
      final Inventory inv = Bukkit.createInventory(EventContainer.holder, 27, ChatColor.translateAlternateColorCodes('&', "Контейнер"));
      ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta paneMeta = pane.getItemMeta();
      paneMeta.setDisplayName(" ");
      pane.setItemMeta(paneMeta);

      for (int i = 0; i < 27; i++) {
         inv.setItem(i, pane);
      }

      this.player.openInventory(inv);
      (new BukkitRunnable() {
            int slot = 9;
            int ticks = 0;

            public void run() {
               if (ContainerMenu.this.player == null) {
                  this.cancel();
               } else if (!inv.getViewers().contains(ContainerMenu.this.player)) {
                  this.cancel();
               } else if (EventContainer.getInstance().containerManager.items <= 0) {
                  inv.close();
                  EventContainer.getInstance().containerManager.startRemovingContainer();
               } else if (this.slot < 18 && EventContainer.getInstance().containerManager.state == ContainerManager.ContainerState.OPENED) {
                  switch (this.ticks) {
                     case 0:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                     case 3:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 7:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 12:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 19:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 27:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 35:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
                        ContainerMenu.this.updateItem(inv, this.slot, ContainerMenu.this.level);
                        break;
                     case 50:
                        ContainerMenu.this.player.playSound(ContainerMenu.this.player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
                        ContainerMenu.this.addItemInventory(
                           ContainerMenu.this.player.getInventory(), inv.getItem(this.slot), ContainerMenu.this.player.getLocation()
                        );
                        ContainerMenu.this.tryUpgradeLevel();
                        this.ticks = 0;
                        EventContainer.getInstance()
                           .containerManager
                           .playerItems
                           .put(
                              ContainerMenu.this.player.getName(),
                              EventContainer.getInstance().containerManager.playerItems.getOrDefault(ContainerMenu.this.player.getName(), 0) + 1
                           );
                        EventContainer.getInstance().containerManager.items--;
                        EventContainer.getInstance().containerManager.updateHologram();
                        this.slot++;
                  }

                  this.ticks++;
               } else {
                  inv.close();
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   public void updateItem(Inventory inventory, int slot, int level) {
      List<ItemStack> items = new LootAPI().getRandomItems(1, level + this.itemlevel / 3);
      if (items != null && items.size() != 0) {
         inventory.setItem(slot, items.get(0));
      } else {
         inventory.setItem(slot, null);
      }

      inventory.setItem(slot - 9, this.getDesignPane());
      inventory.setItem(slot + 9, this.getDesignPane());
   }

   public ItemStack getDesignPane() {
      ItemStack itemStack = new ItemStack(Material.STONE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(" ");
      itemStack.setItemMeta(itemMeta);
      switch (this.itemlevel) {
         case 1:
            itemStack.setType(Material.BLACK_STAINED_GLASS_PANE);
            break;
         case 2:
            itemStack.setType(Material.BLUE_STAINED_GLASS_PANE);
            break;
         case 3:
            itemStack.setType(Material.ORANGE_STAINED_GLASS_PANE);
            break;
         case 4:
            itemStack.setType(Material.RED_STAINED_GLASS_PANE);
      }

      return itemStack;
   }

   public void tryUpgradeLevel() {
      if (this.itemlevel != 4) {
         int random = new Random().nextInt(4);
         if (random == 0) {
            this.itemlevel++;
         }
      }
   }

   public void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      if (itemStack != null) {
         for (int id = 0; id < inventory.getStorageContents().length; id++) {
            ItemStack item = inventory.getItem(id);
            if (item == null || item.getType().isAir()) {
               inventory.addItem(new ItemStack[]{itemStack});
               return;
            }

            if (item.isSimilar(itemStack)) {
               int count = item.getMaxStackSize() - item.getAmount();
               if (count > 0) {
                  if (itemStack.getAmount() <= count) {
                     inventory.addItem(new ItemStack[]{itemStack});
                     return;
                  }

                  ItemStack i = itemStack.clone();
                  i.setAmount(count);
                  inventory.addItem(new ItemStack[]{i});
                  itemStack.setAmount(itemStack.getAmount() - count);
               }
            }
         }

         Bukkit.getScheduler().runTask(this.plugin, () -> location.getWorld().dropItemNaturally(location, itemStack));
      }
   }
}
