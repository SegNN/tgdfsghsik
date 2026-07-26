package org.dimasik.liteauction.frontend.menus.bids.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class Sell extends AbstractMenu {
   private boolean forceClose = false;
   private int page;
   private Main back;
   private HashMap<Integer, BidItem> items = new HashMap<>();

   public Sell(int page, Main back) {
      this.page = page;
      this.back = back;
   }

   public Sell compile() {
      try {
         this.items.clear();
         int slot = 0;
         List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getPlayerItems(this.viewer.getName(), this.page, 45).get();
         int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
         this.inventory = ConfigUtils.buildInventory(
            this,
            "design/menus/bids/sell.yml",
            "inventory-type",
            ConfigManager.getString("design/menus/bids/sell.yml", "gui-title", "&x&0&0&D&8&F&F Товары на продаже")
         );

         for (int i = 0; i < items.size() && slot < 45; i++) {
            BidItem bidItem = items.get(i);
            this.items.put(slot, bidItem);
            ItemStack itemStack = bidItem.decodeItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (itemMeta != null && itemMeta.getLore() != null) {
               lore = itemMeta.getLore();
            }

            lore.addAll(
               ConfigManager.getStringList("design/menus/bids/sell.yml", "active-items.lore.main")
                  .stream()
                  .map(
                     s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(bidItem.getTags()))),
                        new Pair<>("%expirytime%", Formatter.getTimeUntilExpiration(bidItem)),
                        new Pair<>("%current_price%", Formatter.formatPrice(bidItem.getCurrentPrice()))
                     )
                  )
                  .toList()
            );
            lore.addAll(ConfigManager.getStringList("design/menus/bids/sell.yml", "active-items.lore.by-one").stream().map(s -> Parser.color(s)).toList());
            lore.addAll(ConfigManager.getStringList("design/menus/bids/sell.yml", "active-items.lore.action").stream().map(s -> Parser.color(s)).toList());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(slot, itemStack);
            slot++;
         }

         Pair<ItemStack, Integer> back = ConfigUtils.buildItem("design/menus/bids/sell.yml", "back", "&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад");
         this.inventory.setItem(back.getRight(), back.getLeft());
         Pair<ItemStack, Integer> prev = ConfigUtils.buildItem("design/menus/bids/sell.yml", "prev-page", "&x&0&0&D&8&F&F◀ Предыдущая страница");
         this.inventory.setItem(prev.getRight(), prev.getLeft());
         Pair<ItemStack, Integer> next = ConfigUtils.buildItem("design/menus/bids/sell.yml", "next-page", "&6Следующая страница ▶");
         this.inventory.setItem(next.getRight(), next.getLeft());
         Pair<ItemStack, Integer> help = ConfigUtils.buildItem("design/menus/bids/sell.yml", "help", "&x&0&0&D&8&F&F Помощь по аукциону");
         this.inventory.setItem(help.getRight(), help.getLeft());
         return this;
      } catch (Exception var9) {
         throw new RuntimeException(var9);
      }
   }

   public Sell setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   @Generated
   public boolean isForceClose() {
      return this.forceClose;
   }

   @Generated
   public int getPage() {
      return this.page;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }

   @Generated
   public HashMap<Integer, BidItem> getItems() {
      return this.items;
   }

   @Generated
   public void setForceClose(boolean forceClose) {
      this.forceClose = forceClose;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }
}
