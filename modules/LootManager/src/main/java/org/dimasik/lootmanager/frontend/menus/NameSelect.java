package org.dimasik.lootmanager.frontend.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.NameData;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class NameSelect implements InventoryHolder {
   public final Player player;
   private Inventory anvilInventory;

   public NameSelect(Player player, String name, Configs back) {
      this.player = player;
      this.openAnvil();
      LootManager.nameDatas.put(player, new NameData(back, name, false));
   }

   private void openAnvil() {
      this.anvilInventory = Bukkit.createInventory(this, InventoryType.ANVIL, Parser.color("Введите название"));
      ItemStack resetItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta resetMeta = resetItem.getItemMeta();
      if (resetMeta != null) {
         resetMeta.setDisplayName(Parser.color("&f"));
         List<String> resetLore = new ArrayList<>();
         resetLore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Сбросить на начальное значение"));
         resetLore.add(Parser.color(""));
         resetMeta.setLore(resetLore);
         resetItem.setItemMeta(resetMeta);
      }

      this.anvilInventory.setItem(0, resetItem);
      ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta cancelMeta = cancelItem.getItemMeta();
      if (cancelMeta != null) {
         cancelMeta.setDisplayName(" ");
         List<String> cancelLore = new ArrayList<>();
         cancelLore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Отменить ввод названия"));
         cancelLore.add(Parser.color(""));
         cancelMeta.setLore(cancelLore);
         cancelItem.setItemMeta(cancelMeta);
      }

      this.anvilInventory.setItem(1, cancelItem);
      ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
      ItemMeta confirmMeta = confirmItem.getItemMeta();
      if (confirmMeta != null) {
         confirmMeta.setDisplayName(" ");
         List<String> confirmLore = new ArrayList<>();
         confirmLore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Утвердить название &x&0&5&F&B&0&0[ЛКМ]"));
         confirmLore.add(Parser.color(""));
         confirmMeta.setLore(confirmLore);
         confirmItem.setItemMeta(confirmMeta);
      }

      this.anvilInventory.setItem(2, confirmItem);
      this.player.openInventory(this.anvilInventory);
   }

   @NotNull
   public Inventory getInventory() {
      return this.anvilInventory;
   }
}
