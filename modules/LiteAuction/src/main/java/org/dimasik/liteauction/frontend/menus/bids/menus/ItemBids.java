package org.dimasik.liteauction.frontend.menus.bids.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.storage.models.Bid;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class ItemBids extends AbstractMenu {
   private boolean forceClose = false;
   private Main back;
   private BidItem bidItem;
   private HashMap<Integer, Bid> bids = new HashMap<>();
   private HashMap<Integer, Integer> availableBids = new HashMap<>();
   private List<Integer> availableBidsSlots = new ArrayList<>();

   public ItemBids(BidItem bidItem, Main back) {
      this.bidItem = bidItem;
      this.back = back;
   }

   public ItemBids compile() {
      try {
         this.bids.clear();
         this.availableBids.clear();
         this.availableBidsSlots.clear();
         this.inventory = ConfigUtils.buildInventory(
            this,
            "design/menus/bids/item_bids.yml",
            "inventory-type",
            ConfigManager.getString("design/menus/bids/item_bids.yml", "gui-title", "&x&0&0&D&8&F&F Просмотр лота")
         );
         ItemStack itemStack = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color(" "));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(0, itemStack);
         this.inventory.setItem(1, itemStack);
         this.inventory.setItem(2, itemStack);
         this.inventory.setItem(3, itemStack);
         this.inventory.setItem(5, itemStack);
         this.inventory.setItem(6, itemStack);
         this.inventory.setItem(7, itemStack);
         this.inventory.setItem(8, itemStack);
         this.inventory.setItem(9, itemStack);
         this.inventory.setItem(10, itemStack);
         this.inventory.setItem(11, itemStack);
         this.inventory.setItem(12, itemStack);
         this.inventory.setItem(13, itemStack);
         this.inventory.setItem(14, itemStack);
         this.inventory.setItem(15, itemStack);
         this.inventory.setItem(16, itemStack);
         this.inventory.setItem(17, itemStack);
         this.inventory.setItem(20, itemStack);
         this.inventory.setItem(24, itemStack);
         this.inventory.setItem(27, itemStack);
         this.inventory.setItem(29, itemStack);
         this.inventory.setItem(31, itemStack);
         this.inventory.setItem(33, itemStack);
         this.inventory.setItem(35, itemStack);
         this.inventory.setItem(36, itemStack);
         this.inventory.setItem(38, itemStack);
         this.inventory.setItem(40, itemStack);
         this.inventory.setItem(42, itemStack);
         this.inventory.setItem(44, itemStack);
         this.inventory.setItem(49, itemStack);
         this.bidItem = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(this.bidItem.getId()).get().get();
         itemStack = this.bidItem.decodeItemStack();
         itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         if (itemMeta != null && itemMeta.getLore() != null) {
            lore = itemMeta.getLore();
         }

         lore.add(Parser.color(""));
         lore.add(
            Parser.color(
               " &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F "
                  + String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(this.bidItem.getTags()))
            )
         );
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Продавец:&x&0&0&D&8&F&F " + this.bidItem.getPlayer()));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Истекает через:&x&0&0&D&8&F&F " + Formatter.getTimeUntilExpiration(this.bidItem)));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Текущая цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(this.bidItem.getCurrentPrice())));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Начальная цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(this.bidItem.getStartPrice())));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена шага:&x&0&0&D&8&F&F " + Formatter.formatPrice(this.bidItem.getStep())));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CДанный товар можно"));
         lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  купить &x&0&0&D&8&F&Fтолько полностью&x&D&5&D&B&D&C."));
         lore.add(Parser.color(""));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(4, itemStack);
         List<Integer> slotsList = List.of(18, 19, 28, 37, 46, 47, 48, 39, 30, 21, 22, 23, 32, 41, 50, 51, 52, 43, 34, 25, 26);
         int size = slotsList.size() - 1;
         int slot = 0;
         int bidsCount = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemIdCount(this.bidItem.getId()).get();
         int startIndex = size * (bidsCount / size) - (size * (bidsCount / size) > 0 ? 1 : 0);
         List<Bid> bids = LiteAuction.getInstance()
            .getDatabaseManager()
            .getBidsManager()
            .getBidsByItemId(this.bidItem.getId(), startIndex, slotsList.size() + 1)
            .get();
         int pages = bids.size() / 45 + (bids.size() % 45 == 0 ? 0 : 1);
         int money = this.bidItem.getCurrentPrice();
         if (!bids.isEmpty()) {
            money = bids.get(bids.size() - 1).getPrice() + this.bidItem.getStep();
         }

         for (int i = 0; i < bids.size() && slot < size; i++) {
            Bid bid = bids.get(i);
            this.bids.put(slotsList.get(slot), bid);
            ItemStack itemStackx = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta itemMetax = itemStackx.getItemMeta();
            itemMetax.setDisplayName(Parser.color("&x&0&5&F&B&0&0 Установлена ставка"));
            List<String> lorex = new ArrayList<>();
            lorex.add(Parser.color(""));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0&n▍"));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Владелец ставки:&x&0&5&F&B&0&0 " + bid.getPlayer()));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Сумма ставки:&x&0&5&F&B&0&0 " + Formatter.formatPrice(bid.getPrice())));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0▍"));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Текущая цена лота: &x&0&5&F&B&0&0" + Formatter.formatPrice(this.bidItem.getCurrentPrice())));
            lorex.add(Parser.color(" &x&0&5&F&B&0&0▍"));
            lorex.add(Parser.color(""));
            itemMetax.setLore(lorex);
            itemStackx.setItemMeta(itemMetax);
            this.inventory.setItem(slotsList.get(slot), itemStackx);
            slot++;
         }

         while (slot < size + 1) {
            this.availableBidsSlots.add(slotsList.get(slot));
            this.availableBids.put(slotsList.get(slot), money);
            ItemStack itemStackx = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMetax = itemStackx.getItemMeta();
            itemMetax.setDisplayName(Parser.color("&x&0&0&D&8&F&F Доступна ставка"));
            List<String> lorex = new ArrayList<>();
            lorex.add(Parser.color(""));
            lorex.add(Parser.color(" &x&0&0&D&8&F&F▶&f Нажмите, чтобы сделать ставку в &x&0&0&D&8&F&F" + Formatter.formatPrice(money)));
            lorex.add(Parser.color(""));
            itemMetax.setLore(lorex);
            itemStackx.setItemMeta(itemMetax);
            this.inventory.setItem(slotsList.get(slot), itemStackx);
            money += this.bidItem.getStep();
            slot++;
         }

         Pair<ItemStack, Integer> back = ConfigUtils.buildItem("design/menus/bids/item_bids.yml", "back", "&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад");
         this.inventory.setItem(back.getRight(), back.getLeft());
         Pair<ItemStack, Integer> help = ConfigUtils.buildItem("design/menus/bids/item_bids.yml", "help", "&x&0&0&D&8&F&F Информация про систему ставок");
         this.inventory.setItem(help.getRight(), help.getLeft());
         return this;
      } catch (Exception var14) {
         throw new RuntimeException(var14);
      }
   }

   @Generated
   public boolean isForceClose() {
      return this.forceClose;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }

   @Generated
   public BidItem getBidItem() {
      return this.bidItem;
   }

   @Generated
   public HashMap<Integer, Bid> getBids() {
      return this.bids;
   }

   @Generated
   public HashMap<Integer, Integer> getAvailableBids() {
      return this.availableBids;
   }

   @Generated
   public List<Integer> getAvailableBidsSlots() {
      return this.availableBidsSlots;
   }

   @Generated
   public void setForceClose(boolean forceClose) {
      this.forceClose = forceClose;
   }
}
