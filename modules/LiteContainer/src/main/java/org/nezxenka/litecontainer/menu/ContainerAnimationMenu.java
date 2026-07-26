package org.nezxenka.litecontainer.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.nezxenka.litecontainer.LiteContainer;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.config.PluginConfigManager;
import org.nezxenka.litecontainer.utils.ColorUtils;
import org.nezxenka.litecontainer.utils.ContainerItem;
import org.nezxenka.litecontainer.utils.ItemRarity;
import org.nezxenka.litecontainer.utils.Utils;

public class ContainerAnimationMenu extends Menu {
   private static final Material DEFAULT_RARITY_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;
   private final PluginConfigManager config;
   private final LootManager lootManager;
   private final EnumMap<ItemRarity, Material> rarities;
   private final String title;
   private final List<String> broadcast;
   private final Random random = ThreadLocalRandom.current();
   private final int maxTicks;
   private final List<Integer> slots;
   private BukkitTask task;
   private boolean fastPrize = false;
   private int step = 0;
   private double totalSapphires = 0.0;
   private double currentSessionSapphires = 0.0;
   private ItemRarity currentRarity = ItemRarity.DEFAULT;

   public ContainerAnimationMenu(PluginConfigManager configManager, LootManager lootManager) {
      this.lootManager = lootManager;
      this.config = configManager;
      this.title = configManager.getAnimationMenuTitle();
      this.maxTicks = configManager.getAnimationItemTicks();
      this.slots = configManager.getAnimationSlots();
      this.broadcast = new ArrayList<>(configManager.getOpenBroadcast());
      this.rarities = configManager.getRarities();
   }

   @Override
   public String getTitle() {
      return ColorUtils.colorize(this.title.replace("{price}", "0"));
   }

   @Override
   public int getSize() {
      return this.config.getAnimationConfig().getInt("rows", 3) * 9;
   }

   @Override
   public void getItemsAndButtons() {
      this.addDecor();
      this.totalSapphires = LiteContainer.getInstance().getDatabaseManager().getTotalSapphires(this.getOpener().getName());
      this.task = this.runAnimation();
   }

   private void addDecor() {
      ConfigurationSection decorSection = this.config.getAnimationConfig().getConfigurationSection("decor");
      if (decorSection != null) {
         for (String key : decorSection.getKeys(false)) {
            try {
               Material material = Material.valueOf(key.toUpperCase());

               for (Integer slot : decorSection.getIntegerList(key)) {
                  if (slot < this.getSize()) {
                     this.getInventory().setItem(slot, new ItemStack(material));
                  }
               }
            } catch (IllegalArgumentException var8) {
               LiteContainer.getInstance().getLogger().severe("Ошибка при загрузке декораций для анимационного меню: " + var8.getMessage());
            }
         }
      }
   }

   public void onClose(@NotNull InventoryCloseEvent e) {
      LiteContainer.getInstance().getMenuManager().unregisterMenu(this);
      if (!this.fastPrize) {
         if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
            this.fastPrize();
         }
      }
   }

   private void fastPrize() {
      Player player = this.getOpener();
      if (player != null) {
         for (int i = this.step; i < this.slots.size(); i++) {
            ContainerItem item = this.lootManager.getRandomItem(this.currentRarity);
            if (item != null && item.itemStack() != null) {
               Utils.addItemInventory(player, item.itemStack());
               this.currentSessionSapphires = this.currentSessionSapphires + item.price();
            }

            this.updateRarity();
         }

         this.broadcastPrise();
      }
   }

   private void broadcastPrise() {
      Player player = this.getOpener();
      if (player != null) {
         LiteContainer.getInstance().getDatabaseManager().addSapphires(player.getName(), this.currentSessionSapphires);

         for (String msg : this.broadcast) {
            String message = ColorUtils.colorize(
               msg.replace("{player}", player.getName()).replace("{price}", String.format("%.2f", this.currentSessionSapphires))
            );
            Bukkit.broadcastMessage(message);
         }
      }
   }

   private BukkitTask runAnimation() {
      final Player player = this.getOpener();
      return player == null ? null : (new BukkitRunnable() {
         int currentSlotIndex = 0;
         int currentTicks = 0;
         ContainerItem currentItem = null;

         public void run() {
            try {
               if (this.currentSlotIndex >= ContainerAnimationMenu.this.slots.size()) {
                  this.finishAnimation();
                  return;
               }

               if (this.currentItem == null) {
                  this.currentItem = ContainerAnimationMenu.this.lootManager.getRandomItem(ContainerAnimationMenu.this.currentRarity);
                  if (this.currentItem == null) {
                     this.currentSlotIndex++;
                     return;
                  }

                  ContainerAnimationMenu.this.setItemsRarity(ContainerAnimationMenu.this.slots.get(this.currentSlotIndex), this.currentItem.rarity());
               }

               if (this.currentTicks <= ContainerAnimationMenu.this.maxTicks) {
                  this.updateSlotAnimation(ContainerAnimationMenu.this.slots.get(this.currentSlotIndex), this.currentItem);
                  this.currentTicks++;
               } else {
                  this.completeSlot(ContainerAnimationMenu.this.slots.get(this.currentSlotIndex), this.currentItem);
                  this.currentSlotIndex++;
                  this.currentTicks = 0;
                  this.currentItem = null;
               }
            } catch (Exception var2) {
               LiteContainer.getInstance().getLogger().severe("Ошибка в анимации контейнера: " + var2.getMessage());
               this.finishAnimation();
            }
         }

         private void updateSlotAnimation(int slot, ContainerItem item) {
            ContainerAnimationMenu.this.getInventory().setItem(slot, item.itemStack());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 2.0F);
         }

         private void completeSlot(int slot, ContainerItem item) {
            if (item.itemStack() != null) {
               Utils.addItemInventory(player, item.itemStack().clone());
               ContainerAnimationMenu.this.currentSessionSapphires = ContainerAnimationMenu.this.currentSessionSapphires + item.price();
               ContainerAnimationMenu.this.getInventory().setItem(slot, item.itemStack());
            }

            ContainerAnimationMenu.this.updateRarity();
            this.updateInventoryTitle();
         }

         private void finishAnimation() {
            ContainerAnimationMenu.this.fastPrize = true;
            player.closeInventory();
            ContainerAnimationMenu.this.broadcastPrise();
            this.cancel();
         }

         private void updateInventoryTitle() {
            double currentTotal = ContainerAnimationMenu.this.totalSapphires + ContainerAnimationMenu.this.currentSessionSapphires;
            String newTitle = ColorUtils.colorize(ContainerAnimationMenu.this.title.replace("{price}", String.format("%.2f", currentTotal)));
            Inventory newInventory = Bukkit.createInventory(ContainerAnimationMenu.this, ContainerAnimationMenu.this.getSize(), newTitle);
            newInventory.setContents(ContainerAnimationMenu.this.getInventory().getContents());
            ContainerAnimationMenu.this.setInventory(newInventory);
            ContainerAnimationMenu.this.fastPrize = true;
            player.openInventory(newInventory);
            ContainerAnimationMenu.this.fastPrize = false;
         }
      }).runTaskTimer(LiteContainer.getInstance(), 5L, 4L);
   }

   private void updateRarity() {
      if (this.random.nextInt(5) == 0) {
         this.currentRarity = Utils.upgradeRarity(this.currentRarity);
      }
   }

   private void setItemsRarity(int slot, ItemRarity rarity) {
      Material rarityMaterial = this.rarities.getOrDefault(rarity, DEFAULT_RARITY_MATERIAL);
      Inventory inv = this.getInventory();
      int slotAbove = slot - 9;
      int slotBelow = slot + 9;
      if (slotAbove >= 0) {
         inv.setItem(slotAbove, new ItemStack(rarityMaterial));
      }

      if (slotBelow < inv.getSize()) {
         inv.setItem(slotBelow, new ItemStack(rarityMaterial));
      }
   }

   @Override
   public Inventory getInventory() {
      return this.inventory;
   }

   @Override
   public Player getOpener() {
      return this.opener;
   }

   @Override
   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }
}
