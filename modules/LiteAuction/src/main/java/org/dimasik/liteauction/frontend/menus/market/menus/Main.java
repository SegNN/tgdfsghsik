package org.dimasik.liteauction.frontend.menus.market.menus;

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
import org.dimasik.liteauction.api.events.market.compile.MarketSellItemAddEvent;
import org.dimasik.liteauction.api.events.market.compile.PostMarketCompileEvent;
import org.dimasik.liteauction.api.events.market.compile.PreMarketCompileEvent;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

public class Main extends AbstractMenu {
   private MarketSortingType sortingType;
   private CategoryType categoryType;
   private HashSet<String> filters;
   private int page;
   private String player;
   private HashMap<Integer, SellItem> items = new HashMap<>();

   public Main(int page) {
      this.sortingType = MarketSortingType.CHEAPEST_FIRST;
      this.categoryType = CategoryType.ALL;
      this.filters = new HashSet<>();
      this.page = page;
   }

   public Main compile() {
      try {
         PreMarketCompileEvent preEvent = new PreMarketCompileEvent(this.viewer, this.page, this.player, this.sortingType, this.categoryType, this.filters);
         LiteAuction.getInstance().getEventManager().triggerEvent(preEvent);
         if (preEvent.isCancelled()) {
            return this;
         } else {
            this.page = preEvent.getPage();
            this.player = preEvent.getTarget();
            this.sortingType = preEvent.getSortingType();
            this.categoryType = preEvent.getCategoryType();
            this.filters = preEvent.getFilters();
            this.items.clear();
            int slot = 0;
            int itemsCnt = LiteAuction.getInstance()
               .getDatabaseManager()
               .getSellItemsManager()
               .getItemsCount(this.player, this.sortingType, this.filters, this.categoryType)
               .get();
            List<SellItem> items = LiteAuction.getInstance()
               .getDatabaseManager()
               .getSellItemsManager()
               .getItems(this.player, this.sortingType, this.filters, this.categoryType, this.page, 45)
               .get();
            int pages = itemsCnt / 45 + (itemsCnt % 45 == 0 ? 0 : 1);
            PostMarketCompileEvent postEvent = new PostMarketCompileEvent(
               this.viewer, items, this.page, this.player, this.sortingType, this.categoryType, this.filters
            );
            LiteAuction.getInstance().getEventManager().triggerEvent(postEvent);
            if (postEvent.isCancelled()) {
               return this;
            } else {
               items = postEvent.getItems();
               this.inventory = ConfigUtils.buildInventory(
                  this,
                  "design/menus/market/main.yml",
                  "inventory-type",
                  PlaceholderUtils.replace(
                     ConfigManager.getString("design/menus/market/main.yml", "gui-title", "&0Аукцион (%current_page%/%pages_amount%)"),
                     true,
                     new Pair<>("%current_page%", String.valueOf(this.page)),
                     new Pair<>("%pages_amount%", String.valueOf(pages))
                  )
               );

               for (int i = 0; i < items.size() && slot < 45; i++) {
                  SellItem sellItem = items.get(i);
                  MarketSellItemAddEvent event = new MarketSellItemAddEvent(sellItem, this.inventory, slot);
                  LiteAuction.getInstance().getEventManager().triggerEvent(event);
                  if (!event.isCancelled()) {
                     this.items.put(slot, sellItem);
                     ItemStack itemStack = sellItem.decodeItemStack();
                     ItemMeta itemMeta = itemStack.getItemMeta();
                     List<String> lore = new ArrayList<>();
                     if (itemMeta != null && itemMeta.getLore() != null) {
                        lore = itemMeta.getLore();
                     }

                     lore.addAll(
                        ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.main")
                           .stream()
                           .map(
                              s -> PlaceholderUtils.replace(
                                 s,
                                 true,
                                 new Pair<>(
                                    "%categories%",
                                    String.join(
                                       ConfigManager.getString("design/menus/main.yml", "category-splitter", "&f, &x&0&0&D&8&F&F"),
                                       TagUtil.getItemCategories(sellItem.getTags())
                                    )
                                 ),
                                 new Pair<>("%seller%", sellItem.getPlayer()),
                                 new Pair<>("%expirytime%", Formatter.getTimeUntilExpiration(sellItem)),
                                 new Pair<>("%price%", String.valueOf(sellItem.getPrice())),
                                 new Pair<>("%full_price%", String.valueOf(sellItem.getPrice() * sellItem.getAmount())),
                                 new Pair<>("%format:price%", Formatter.formatPrice(sellItem.getPrice())),
                                 new Pair<>("%format:full_price%", Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount()))
                              )
                           )
                           .toList()
                     );
                     if (sellItem.isByOne()) {
                        lore.addAll(
                           ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.by-one")
                              .stream()
                              .map(s -> PlaceholderUtils.replace(s, true))
                              .toList()
                        );
                     }

                     if (sellItem.getPlayer().equalsIgnoreCase(this.viewer.getName())) {
                        lore.addAll(
                           ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.seller")
                              .stream()
                              .map(s -> PlaceholderUtils.replace(s, true))
                              .toList()
                        );
                     } else {
                        lore.addAll(
                           ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy")
                              .stream()
                              .map(s -> PlaceholderUtils.replace(s, true))
                              .toList()
                        );
                        if (!sellItem.isByOne()) {
                           lore.addAll(
                              ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy-by-one")
                                 .stream()
                                 .map(s -> PlaceholderUtils.replace(s, true))
                                 .toList()
                           );
                        }
                     }

                     itemMeta.setLore(lore);
                     itemStack.setItemMeta(itemMeta);
                     itemStack.setAmount(sellItem.getAmount());
                     this.inventory.setItem(slot, itemStack);
                     slot++;
                  }
               }

               int item_count = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItemsCount(this.viewer.getName()).get();
               Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                  "design/menus/market/main.yml",
                  "on-sell-items",
                  "&x&0&0&D&8&F&F ❏ Товары на продаже ❏",
                  new Pair<>("%item_count%", String.valueOf(item_count))
               );
               this.inventory.setItem(entry.getRight(), entry.getLeft());
               item_count = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(this.viewer.getName()).get();
               entry = ConfigUtils.buildItem(
                  "design/menus/market/main.yml",
                  "unsold-items",
                  "&x&0&0&D&8&F&F ❏ Просроченные товары ❏",
                  new Pair<>("%item_count%", String.valueOf(item_count))
               );
               this.inventory.setItem(entry.getRight(), entry.getLeft());
               Pair<ItemStack, Integer> entryx = ConfigUtils.buildItem(
                  "design/menus/market/main.yml", "update", "&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить аукцион"
               );
               this.inventory.setItem(entryx.getRight(), entryx.getLeft());
               Pair<ItemStack, Integer> entryxx = ConfigUtils.buildItem("design/menus/market/main.yml", "prev-page", "&x&0&0&D&8&F&F◀ Предыдущая страница");
               this.inventory.setItem(entryxx.getRight(), entryxx.getLeft());
               Pair<ItemStack, Integer> entryxxx = ConfigUtils.buildItem("design/menus/market/main.yml", "switch", "&x&0&0&D&8&F&F Помощь по аукциону");
               this.inventory.setItem(entryxxx.getRight(), entryxxx.getLeft());
               Pair<ItemStack, Integer> entryxxxx = ConfigUtils.buildItem("design/menus/market/main.yml", "next-page", "&6Следующая страница ▶");
               this.inventory.setItem(entryxxxx.getRight(), entryxxxx.getLeft());
               Pair<ItemStack, Integer> entryxxxxx = ConfigUtils.buildItem("design/menus/market/main.yml", "help", "&x&0&0&D&8&F&F Помощь по системе аукциона:");
               this.inventory.setItem(entryxxxxx.getRight(), entryxxxxx.getLeft());
               ItemStack itemStackx = ConfigUtils.getBaseItem("design/menus/market/main.yml", "sorting.material", Material.HOPPER);
               ItemMeta itemMetax = itemStackx.getItemMeta();
               itemMetax.setDisplayName(
                  Parser.color(ConfigManager.getString("design/menus/market/main.yml", "sorting.displayname", "&x&0&0&D&8&F&F Сортировка"))
               );
               List<String> lorex = new ArrayList<>();

               for (MarketSortingType sortingType : MarketSortingType.values()) {
                  if (this.sortingType == sortingType) {
                     lorex.add(
                        Parser.color(
                           ConfigManager.getString("design/menus/market/main.yml", "sorting.prefix.selected", "&o&6&6✔&6 &6") + sortingType.getDisplayName()
                        )
                     );
                  } else {
                     lorex.add(
                        Parser.color(
                           Parser.color(
                              ConfigManager.getString("design/menus/market/main.yml", "sorting.prefix.unselected", "&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&C")
                                 + sortingType.getDisplayName()
                           )
                        )
                     );
                  }
               }

               itemMetax.setLore(lorex);
               if (ConfigManager.getBoolean("design/menus/market/main.yml", "sorting.glow", false)) {
                  itemMetax.addEnchant(Enchantment.UNBREAKING, 1, true);
                  itemMetax.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               }

               itemStackx.setItemMeta(itemMetax);
               this.inventory.setItem(ConfigManager.getInt("design/menus/market/main.yml", "sorting.slot", 52), itemStackx);
               ItemStack itemStackxx = ConfigUtils.getBaseItem("design/menus/market/main.yml", "category.material", Material.CHEST_MINECART);
               ItemMeta itemMetaxx = itemStackxx.getItemMeta();
               itemMetaxx.setDisplayName(
                  Parser.color(ConfigManager.getString("design/menus/market/main.yml", "category.displayname", "&x&0&0&D&8&F&F Категории предметов"))
               );
               lorex = new ArrayList<>();

               for (CategoryType categoryType : CategoryType.values()) {
                  if (this.categoryType == categoryType) {
                     lorex.add(
                        Parser.color(
                           ConfigManager.getString("design/menus/market/main.yml", "category.prefix.selected", "&o&6&6✔&6 &6") + categoryType.getDisplayName()
                        )
                     );
                  } else {
                     lorex.add(
                        Parser.color(
                           Parser.color(
                              ConfigManager.getString("design/menus/market/main.yml", "category.prefix.unselected", "&o&x&9&C&F&9&F&F● &f")
                                 + categoryType.getDisplayName()
                           )
                        )
                     );
                  }
               }

               itemMetaxx.setLore(lorex);
               if (ConfigManager.getBoolean("design/menus/market/main.yml", "category.glow", false)) {
                  itemMetaxx.addEnchant(Enchantment.UNBREAKING, 1, true);
                  itemMetaxx.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               }

               itemStackxx.setItemMeta(itemMetaxx);
               this.inventory.setItem(ConfigManager.getInt("design/menus/market/main.yml", "category.slot", 53), itemStackxx);
               return this;
            }
         }
      } catch (InterruptedException | ExecutionException var14) {
         throw new RuntimeException(var14);
      }
   }

   public Main setTarget(String player) {
      this.player = player;
      return this;
   }

   @Generated
   public MarketSortingType getSortingType() {
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
   public HashMap<Integer, SellItem> getItems() {
      return this.items;
   }

   @Generated
   public void setSortingType(MarketSortingType sortingType) {
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
