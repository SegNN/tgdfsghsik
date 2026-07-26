package org.dimasik.liteauction.frontend.menus.market.menus;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class ConfirmItem extends AbstractMenu {
   private boolean forceClose = false;
   private SellItem sellItem;
   private Main back;

   public ConfirmItem(SellItem sellItem, Main back) {
      this.sellItem = sellItem;
      this.back = back;
   }

   public ConfirmItem compile() {
      try {
         this.inventory = ConfigUtils.buildInventory(
            this,
            "design/menus/market/confirm_item.yml",
            "inventory-type",
            ConfigManager.getString("design/menus/market/confirm_item.yml", "gui-title", "&x&0&0&D&8&F&F Покупка предмета")
         );
         List<Integer> approveSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "approve.slot");
         if (!approveSlots.isEmpty()) {
            Pair<ItemStack, Integer> item = ConfigUtils.buildItem("design/menus/market/confirm_item.yml", "approve", "&x&0&5&F&B&0&0 ▶ &x&D&5&D&B&D&CКупить");

            for (Integer s : approveSlots) {
               this.inventory.setItem(s, item.getLeft());
            }
         }

         List<Integer> neutralSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "neutral.slot");
         if (!neutralSlots.isEmpty()) {
            Pair<ItemStack, Integer> item = ConfigUtils.buildItem("design/menus/market/confirm_item.yml", "neutral", " ");

            for (Integer s : neutralSlots) {
               this.inventory.setItem(s, item.getLeft());
            }
         }

         List<Integer> cancelSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "cancel.slot");
         if (!cancelSlots.isEmpty()) {
            Pair<ItemStack, Integer> item = ConfigUtils.buildItem(
               "design/menus/market/confirm_item.yml", "cancel", "&x&F&F&2&2&2&2 ▶ &x&D&5&D&B&D&CОтменить покупку"
            );

            for (Integer s : cancelSlots) {
               this.inventory.setItem(s, item.getLeft());
            }
         }

         ItemStack itemStack = this.sellItem.decodeItemStack();
         itemStack.setAmount(this.sellItem.getAmount());
         ItemMeta itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         if (itemMeta != null && itemMeta.getLore() != null) {
            lore = itemMeta.getLore();
         }

         lore.addAll(
            ConfigManager.getStringList("design/menus/market/confirm_item.yml", "item.lore")
               .stream()
               .map(
                  s -> PlaceholderUtils.replace(
                     s,
                     true,
                     new Pair<>("%categories%", String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(this.sellItem.getTags()))),
                     new Pair<>("%seller%", this.sellItem.getPlayer()),
                     new Pair<>("%full_price%", Formatter.formatPrice(this.sellItem.getPrice() * this.sellItem.getAmount())),
                     new Pair<>("%price%", Formatter.formatPrice(this.sellItem.getPrice()))
                  )
               )
               .toList()
         );
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(ConfigManager.getInt("design/menus/market/confirm_item.yml", "item.slot", 13), itemStack);
         return this;
      } catch (Exception var7) {
         throw new RuntimeException(var7);
      }
   }

   public ConfirmItem setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   @Generated
   public boolean isForceClose() {
      return this.forceClose;
   }

   @Generated
   public SellItem getSellItem() {
      return this.sellItem;
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
