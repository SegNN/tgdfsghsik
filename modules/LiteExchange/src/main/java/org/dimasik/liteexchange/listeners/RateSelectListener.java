package org.dimasik.liteexchange.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.RateData;
import org.dimasik.liteexchange.menu.Create;
import org.dimasik.liteexchange.util.PacketSender;
import org.dimasik.liteexchange.util.Parser;

public class RateSelectListener implements Listener {
   public RateSelectListener() {
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      if (LiteExchange.rateDatas.containsKey(player)) {
         event.getInventory().clear();
         if (!LiteExchange.rateDatas.get(player).forceClose) {
            int sourceRate = LiteExchange.rateDatas.get(player).sourceRate;
            int coins = LiteExchange.rateDatas.get(player).coins;
            Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
               if (event.getPlayer() != null) {
                  new Create(player, sourceRate, coins);
               }
            }, 1L);
         }

         LiteExchange.rateDatas.remove(player);
      }
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      Player player = (Player)event.getView().getPlayer();
      if (LiteExchange.rateDatas.containsKey(player)) {
         AnvilInventory anvilInventory = event.getInventory();
         String text = event.getInventory().getRenameText();

         try {
            int rate = Integer.parseInt(text);
            if (rate > 0) {
               LiteExchange.rateDatas.get(player).breaked = false;
               LiteExchange.rateDatas.get(player).rate = rate;
               ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
               ItemMeta itemMeta = itemStack.getItemMeta();
               itemMeta.setDisplayName(" ");
               ArrayList lore = new ArrayList();
               lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Утвердить новый курс &x&0&5&F&B&0&0[ЛКМ]"));
               lore.add(Parser.color(""));
               itemMeta.setLore(lore);
               itemStack.setItemMeta(itemMeta);
               event.setResult(itemStack);
               anvilInventory.setResult(itemStack);
               Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
                  if (anvilInventory.getViewers().isEmpty()) {
                     PacketSender.sendAnvilSlotUpdate(player, 2, itemStack);
                  }
               }, 1L);
            } else {
               LiteExchange.rateDatas.get(player).breaked = true;
               ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
               ItemMeta itemMeta = itemStack.getItemMeta();
               itemMeta.setDisplayName(" ");
               ArrayList lore = new ArrayList();
               lore.add(Parser.color(" &x&F&F&2&2&2&2▶&f Вам нужно указать целое число!"));
               lore.add(Parser.color(""));
               itemMeta.setLore(lore);
               itemStack.setItemMeta(itemMeta);
               event.setResult(itemStack);
               anvilInventory.setResult(itemStack);
               Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
                  if (anvilInventory.getViewers().isEmpty()) {
                     PacketSender.sendAnvilSlotUpdate(player, 2, itemStack);
                  }
               }, 1L);
            }
         } catch (NumberFormatException var10) {
            LiteExchange.rateDatas.get(player).breaked = true;
            ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(" ");
            ArrayList lore = new ArrayList();
            lore.add(Parser.color(" &x&F&F&2&2&2&2▶&f Вам нужно указать целое число!"));
            lore.add(Parser.color(""));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            event.setResult(itemStack);
            anvilInventory.setResult(itemStack);
            Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
               if (anvilInventory.getViewers().isEmpty()) {
                  PacketSender.sendAnvilSlotUpdate(player, 2, itemStack);
               }
            }, 1L);
         }
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (LiteExchange.rateDatas.containsKey(player)) {
         Inventory inventory = event.getView().getTopInventory();
         if (event.getClickedInventory() == null) {
            return;
         }

         if (event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 1) {
               player.closeInventory();
            } else if (slot == 2) {
               if (LiteExchange.rateDatas.get(player).breaked) {
                  return;
               }

               RateData rateData = LiteExchange.rateDatas.get(player);
               int rate = rateData.rate;
               int coins = rateData.coins;
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               long requiredMoneyLong = (long)rate * coins;
               if (requiredMoneyLong > 2147483647L || requiredMoneyLong < 0L) {
                  rate = rateData.sourceRate;
                  rateData.forceClose = true;
                  player.closeInventory();
                  player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7ИДИ НАХУЙ ПО СОСИ У МЕНЧИКА ЭТО НЕ ДКОД ЫЫЫЫЫ"));
                  Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> new Create(player, rate, coins), 1L);
                  return;
               }

               int requiredMoney = (int)requiredMoneyLong;
               if (balance < 0.0 || balance < requiredMoney) {
                  rate = rateData.sourceRate;
                  rateData.forceClose = true;
                  player.closeInventory();
                  Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> new Create(player, rate, coins), 1L);
                  return;
               }

               rateData.forceClose = true;
               player.closeInventory();
               Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> new Create(player, rate, coins), 1L);
            }
         } else if (event.isShiftClick()) {
            event.setCancelled(true);
         }
      }
   }

   private ItemStack getBlack(int rate) {
      ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(String.valueOf(rate));
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack getRed() {
      ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Отменить ввод курса"));
      lore.add(Parser.color(""));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack getLime() {
      ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Утвердить новый курс &x&0&5&F&B&0&0[ЛКМ]"));
      lore.add(Parser.color(""));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }
}
