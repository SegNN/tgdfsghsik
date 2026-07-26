package org.dimasik.lootmanager.frontend.listeners;

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
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.NameData;
import org.dimasik.lootmanager.backend.utils.PacketSender;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.Configs;
import org.dimasik.lootmanager.frontend.menus.NameSelect;

public class NameSelectListener implements Listener {
   public NameSelectListener() {
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      if (LootManager.nameDatas.containsKey(player)) {
         event.getInventory().clear();
         NameData nameData = LootManager.nameDatas.get(player);
         if (!nameData.getForceClose()) {
            Bukkit.getScheduler().runTaskLater(LootManager.getInstance(), () -> {
               if (event.getPlayer() != null) {
                  nameData.getBack().compile().open();
               }
            }, 1L);
         }

         LootManager.nameDatas.remove(player);
      }
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      Player player = (Player)event.getView().getPlayer();
      if (LootManager.nameDatas.containsKey(player)) {
         AnvilInventory anvilInventory = event.getInventory();
         String text = event.getInventory().getRenameText();
         NameData nameData = LootManager.nameDatas.get(player);
         nameData.setName(text);

         while (nameData.getName().contains(" ")) {
            nameData.setName(nameData.getName().replace(" ", ""));
         }

         if (text != null && !text.isEmpty()) {
            LootManager.nameDatas.get(player).setName(text);
            ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(" ");
            List<String> lore = new ArrayList<>();
            lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Утвердить название &x&0&5&F&B&0&0[ЛКМ]"));
            lore.add(Parser.color(""));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            event.setResult(itemStack);
            anvilInventory.setResult(itemStack);
            Bukkit.getScheduler().runTaskLater(LootManager.getInstance(), () -> {
               if (anvilInventory.getViewers().isEmpty()) {
                  PacketSender.sendAnvilSlotUpdate(player, 2, itemStack);
               }
            }, 1L);
         } else {
            LootManager.nameDatas.get(player).setName("");
            ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(" ");
            List<String> lore = new ArrayList<>();
            lore.add(Parser.color(" &x&F&F&2&2&2&2▶&f Вам нужно указать непустую строку!"));
            lore.add(Parser.color(""));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            event.setResult(itemStack);
            anvilInventory.setResult(itemStack);
            Bukkit.getScheduler().runTaskLater(LootManager.getInstance(), () -> {
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
      if (LootManager.nameDatas.containsKey(player)) {
         Inventory inventory = event.getView().getTopInventory();
         NameData nameData = LootManager.nameDatas.get(player);
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         int slot = event.getSlot();
         if (slot == 1) {
            player.closeInventory();
         } else if (slot == 2) {
            if (!nameData.getName().isEmpty()) {
               nameData.setForceClose(true);
               String name = nameData.getName();

               while (name.contains(" ")) {
                  name = name.replace(" ", "");
               }

               player.closeInventory();
               String finalName = name;
               String configData = LootManager.getInstance().getItemsConfig().saveToString();
               LootManager.getInstance().getDatabaseManager().saveConfig(name, configData).thenAccept(success -> {
                  if (success) {
                     player.sendMessage(Parser.color("&#00D4FB▶ &#E7E7E7Конфигурация &#00D4FB" + finalName + " &#E7E7E7успешно загружена!"));
                  } else {
                     player.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при загрузке конфигурации"));
                  }

                  Bukkit.getScheduler().runTask(LootManager.getInstance(), () -> {
                     Configs configs = nameData.getBack();
                     configs.compile().open();
                  });
               }).exceptionally(e -> {
                  Bukkit.getScheduler().runTask(LootManager.getInstance(), () -> {
                     player.sendMessage(Parser.color("&#FF2222▶ &#E7E7E7Ошибка при удалении: &#FF2222" + e.getMessage()));
                     Configs configs = nameData.getBack();
                     configs.compile().open();
                  });
                  return null;
               });
            } else {
               nameData.setForceClose(true);
               event.setCursor(null);
               player.closeInventory();
               Bukkit.getScheduler().runTaskLater(LootManager.getInstance(), () -> new NameSelect(player, nameData.getName(), nameData.getBack()), 1L);
            }
         }
      }
   }
}
