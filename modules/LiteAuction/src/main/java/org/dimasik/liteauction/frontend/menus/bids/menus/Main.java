package org.dimasik.liteauction.frontend.menus.bids.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class Main extends AbstractMenu {
   private BidsSortingType sortingType;
   private CategoryType categoryType;
   private HashSet<String> filters;
   private int page;
   private String player;
   private HashMap<Integer, BidItem> items = new HashMap<>();

   public Main(int page) {
      this.sortingType = BidsSortingType.CHEAPEST_FIRST;
      this.categoryType = CategoryType.ALL;
      this.filters = new HashSet<>();
      this.page = page;
   }

   public Main compile() {
      try {
         this.items.clear();
         int slot = 0;
         int itemsCnt = LiteAuction.getInstance()
            .getDatabaseManager()
            .getBidItemsManager()
            .getItemsCount(this.player, this.sortingType, this.filters, this.categoryType)
            .get();
         List<BidItem> items = LiteAuction.getInstance()
            .getDatabaseManager()
            .getBidItemsManager()
            .getItems(this.player, this.sortingType, this.filters, this.categoryType, this.page, 45)
            .get();
         int pages = itemsCnt / 45 + (itemsCnt % 45 == 0 ? 0 : 1);
         this.inventory = ConfigUtils.buildInventory(
            this,
            "design/menus/bids/main.yml",
            "inventory-type",
            PlaceholderUtils.replace(
               ConfigManager.getString("design/menus/bids/main.yml", "gui-title", "&0Аукцион (%current_page%/%pages_amount%)"),
               true,
               new Pair<>("%current_page%", String.valueOf(this.page)),
               new Pair<>("%pages_amount%", String.valueOf(pages))
            )
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
               ConfigManager.getStringList("design/menus/bids/main.yml", "active-items.lore.main")
                  .stream()
                  .map(
                     s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(bidItem.getTags()))),
                        new Pair<>("%seller%", bidItem.getPlayer()),
                        new Pair<>("%expirytime%", Formatter.getTimeUntilExpiration(bidItem)),
                        new Pair<>("%current_price%", Formatter.formatPrice(bidItem.getCurrentPrice())),
                        new Pair<>("%step_price%", Formatter.formatPrice(bidItem.getStep()))
                     )
                  )
                  .toList()
            );
            if (bidItem.getPlayer().equalsIgnoreCase(this.viewer.getName())) {
               lore.addAll(ConfigManager.getStringList("design/menus/bids/main.yml", "active-items.lore.seller").stream().map(s -> Parser.color(s)).toList());
            } else {
               lore.addAll(ConfigManager.getStringList("design/menus/bids/main.yml", "active-items.lore.buyer").stream().map(s -> Parser.color(s)).toList());
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(slot, itemStack);
            slot++;
         }

         int item_count = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getPlayerItemsCount(this.viewer.getName()).get();
         Pair<ItemStack, Integer> onSell = ConfigUtils.buildItem(
            "design/menus/bids/main.yml", "on-sell-items", "&x&0&0&D&8&F&F ❏ Товары на продаже ❏", new Pair<>("%item_count%", String.valueOf(item_count))
         );
         this.inventory.setItem(onSell.getRight(), onSell.getLeft());
         int unsold_count = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(this.viewer.getName()).get();
         Pair<ItemStack, Integer> unsold = ConfigUtils.buildItem(
            "design/menus/bids/main.yml", "unsold-items", "&x&0&0&D&8&F&F ❏ Просроченные товары ❏", new Pair<>("%item_count%", String.valueOf(unsold_count))
         );
         this.inventory.setItem(unsold.getRight(), unsold.getLeft());
         Pair<ItemStack, Integer> update = ConfigUtils.buildItem("design/menus/bids/main.yml", "update", "&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить аукцион");
         this.inventory.setItem(update.getRight(), update.getLeft());
         Pair<ItemStack, Integer> prev = ConfigUtils.buildItem("design/menus/bids/main.yml", "prev-page", "&x&0&0&D&8&F&F◀ Предыдущая страница");
         this.inventory.setItem(prev.getRight(), prev.getLeft());
         Pair<ItemStack, Integer> switchItem = ConfigUtils.buildItem("design/menus/bids/main.yml", "switch", "&x&0&0&D&8&F&F Помощь по аукциону");
         this.inventory.setItem(switchItem.getRight(), switchItem.getLeft());
         Pair<ItemStack, Integer> next = ConfigUtils.buildItem("design/menus/bids/main.yml", "next-page", "&6Следующая страница ▶");
         this.inventory.setItem(next.getRight(), next.getLeft());
         Pair<ItemStack, Integer> help = ConfigUtils.buildItem("design/menus/bids/main.yml", "help", "&x&0&0&D&8&F&F Помощь по системе аукциона:");
         this.inventory.setItem(help.getRight(), help.getLeft());
         ItemStack itemStackx = ConfigUtils.getBaseItem("design/menus/bids/main.yml", "sorting.material", Material.HOPPER);
         ItemMeta itemMetax = itemStackx.getItemMeta();
         itemMetax.setDisplayName(Parser.color(ConfigManager.getString("design/menus/bids/main.yml", "sorting.displayname", "&x&0&0&D&8&F&F Сортировка")));
         List<String> lorex = new ArrayList<>();

         for (BidsSortingType sortingType : BidsSortingType.values()) {
            if (this.sortingType == sortingType) {
               lorex.add(
                  Parser.color(ConfigManager.getString("design/menus/bids/main.yml", "sorting.prefix.selected", "&o&6&6✔&6 &6") + sortingType.getDisplayName())
               );
            } else {
               lorex.add(
                  Parser.color(
                     Parser.color(
                        ConfigManager.getString("design/menus/bids/main.yml", "sorting.prefix.unselected", "&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&C")
                           + sortingType.getDisplayName()
                     )
                  )
               );
            }
         }

         itemMetax.setLore(lorex);
         if (ConfigManager.getBoolean("design/menus/bids/main.yml", "sorting.glow", false)) {
            itemMetax.addEnchant(Enchantment.UNBREAKING, 1, true);
            itemMetax.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         itemStackx.setItemMeta(itemMetax);
         this.inventory.setItem(ConfigManager.getInt("design/menus/bids/main.yml", "sorting.slot", 52), itemStackx);
         itemStackx = ConfigUtils.getBaseItem("design/menus/bids/main.yml", "category.material", Material.CHEST_MINECART);
         itemMetax = itemStackx.getItemMeta();
         itemMetax.setDisplayName(
            Parser.color(ConfigManager.getString("design/menus/bids/main.yml", "category.displayname", "&x&0&0&D&8&F&F Категории предметов"))
         );
         lorex = new ArrayList<>();

         for (CategoryType categoryType : CategoryType.values()) {
            if (this.categoryType == categoryType) {
               lorex.add(
                  Parser.color(
                     ConfigManager.getString("design/menus/bids/main.yml", "category.prefix.selected", "&o&6&6✔&6 &6") + categoryType.getDisplayName()
                  )
               );
            } else {
               lorex.add(
                  Parser.color(
                     Parser.color(
                        ConfigManager.getString("design/menus/bids/main.yml", "category.prefix.unselected", "&o&x&9&C&F&9&F&F● &f")
                           + categoryType.getDisplayName()
                     )
                  )
               );
            }
         }

         itemMetax.setLore(lorex);
         if (ConfigManager.getBoolean("design/menus/bids/main.yml", "category.glow", false)) {
            itemMetax.addEnchant(Enchantment.UNBREAKING, 1, true);
            itemMetax.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         itemStackx.setItemMeta(itemMetax);
         this.inventory.setItem(ConfigManager.getInt("design/menus/bids/main.yml", "category.slot", 53), itemStackx);
         return this;
      } catch (InterruptedException | ExecutionException var21) {
         throw new RuntimeException(var21);
      }
   }

   public Main setTarget(String player) {
      this.player = player;
      return this;
   }

   @Generated
   public BidsSortingType getSortingType() {
      return this.sortingType;
   }

   @Generated
   public CategoryType getCategoryType() {
      return this.categoryType;
   }

   @Generated
   public HashSet<String> getFilters() {
      return this.filters;
   }

   @Generated
   public int getPage() {
      return this.page;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public HashMap<Integer, BidItem> getItems() {
      return this.items;
   }

   @Generated
   public void setSortingType(BidsSortingType sortingType) {
      this.sortingType = sortingType;
   }

   @Generated
   public void setCategoryType(CategoryType categoryType) {
      this.categoryType = categoryType;
   }

   @Generated
   public void setFilters(HashSet<String> filters) {
      this.filters = filters;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }
}
