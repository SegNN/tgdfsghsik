package org.dimasik.liteauction.frontend.menus.bids.menus;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class RemoveItem extends AbstractMenu {
   private boolean forceClose = false;
   private BidItem bidItem;
   private Main back;

   public RemoveItem(BidItem bidItem, Main back) {
      this.bidItem = bidItem;
      this.back = back;
   }

   public RemoveItem compile() {
      try {
         this.inventory = ConfigUtils.buildInventory(
            this,
            "design/menus/bids/remove_item.yml",
            "inventory-type",
            ConfigManager.getString("design/menus/bids/remove_item.yml", "gui-title", "&x&0&0&D&8&F&F Снять предмет с продажи")
         );
         ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0▶ &x&D&5&D&B&D&CСнять предмет с продажи"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(0, itemStack);
         this.inventory.setItem(1, itemStack);
         this.inventory.setItem(2, itemStack);
         this.inventory.setItem(9, itemStack);
         this.inventory.setItem(10, itemStack);
         this.inventory.setItem(11, itemStack);
         this.inventory.setItem(18, itemStack);
         this.inventory.setItem(19, itemStack);
         this.inventory.setItem(20, itemStack);
         itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color(" "));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(3, itemStack);
         this.inventory.setItem(4, itemStack);
         this.inventory.setItem(5, itemStack);
         this.inventory.setItem(12, itemStack);
         this.inventory.setItem(14, itemStack);
         this.inventory.setItem(21, itemStack);
         this.inventory.setItem(22, itemStack);
         this.inventory.setItem(23, itemStack);
         itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CОтменить"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(6, itemStack);
         this.inventory.setItem(7, itemStack);
         this.inventory.setItem(8, itemStack);
         this.inventory.setItem(15, itemStack);
         this.inventory.setItem(16, itemStack);
         this.inventory.setItem(17, itemStack);
         this.inventory.setItem(24, itemStack);
         this.inventory.setItem(25, itemStack);
         this.inventory.setItem(26, itemStack);
         itemStack = this.bidItem.decodeItemStack();
         itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         if (itemMeta != null && itemMeta.getLore() != null) {
            lore = itemMeta.getLore();
         }

         lore.addAll(ConfigManager.getStringList("design/menus/bids/remove_item.yml", "item.lore").stream().map(s -> Parser.color(s)).toList());
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(13, itemStack);
         return this;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   @Generated
   public boolean isForceClose() {
      return this.forceClose;
   }

   @Generated
   public BidItem getBidItem() {
      return this.bidItem;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }

   @Generated
   public void setForceClose(boolean forceClose) {
      this.forceClose = forceClose;
   }
}
