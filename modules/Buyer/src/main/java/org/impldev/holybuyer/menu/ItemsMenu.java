package org.impldev.holybuyer.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.BuyerItem;
import org.impldev.holybuyer.HolyBuyer;
import org.impldev.holybuyer.Prices;
import org.impldev.holybuyer.Texts;

/** {@code /b items} - весь список товаров с сортировками и статистикой. */
public final class ItemsMenu extends BuyerMenu {
   private static final int PER_PAGE = 45;
   private static final int SORT = 48;
   private static final int PREV = 45;
   private static final int NEXT = 53;
   private static final int BACK = 49;

   /** Порядок, в котором показывать товары. */
   private enum Sort {
      HOT("Горячие товары"),
      EXPENSIVE("Самые дорогие"),
      CHEAP("Самые дешёвые"),
      PROFIT("Выгодные лично вам");

      private final String title;

      Sort(String title) {
         this.title = title;
      }
   }

   private final HolyBuyer plugin;
   private final BuyerData.Profile profile;
   private final Map<Integer, String> slots = new HashMap<>();
   private Sort sort = Sort.HOT;
   private int page;

   public ItemsMenu(HolyBuyer plugin, Player player, BuyerData.Profile profile) {
      super(player, 54, "&rТовары Скупщика");
      this.plugin = plugin;
      this.profile = profile;
   }

   @Override
   public void render() {
      this.inventory.clear();
      this.slots.clear();

      List<BuyerItem> items = this.sorted();
      int from = this.page * PER_PAGE;
      for (int i = 0; i < PER_PAGE && from + i < items.size(); i++) {
         BuyerItem item = items.get(from + i);
         this.inventory.setItem(i, this.render(item));
         this.slots.put(i, item.id());
      }

      for (int slot = PER_PAGE; slot < 54; slot++) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      this.inventory.setItem(SORT, Menus.item(Material.HOPPER, "&r " + Texts.CYAN + "Сортировка",
            "",
            "&r " + Texts.ORANGE + "● &r&fСейчас: " + Texts.ORANGE + this.sort.title,
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы сменить"));
      this.inventory.setItem(BACK, Menus.item(Material.ARROW, "&r" + Texts.RED + "◀ &r&fНазад"));

      if (this.page > 0) {
         this.inventory.setItem(PREV, Menus.item(Material.ARROW, "&r" + Texts.CYAN + "◀ &r&fСтраница "
               + this.page));
      }

      if (from + PER_PAGE < items.size()) {
         this.inventory.setItem(NEXT, Menus.item(Material.ARROW, "&r" + Texts.CYAN + "Страница "
               + (this.page + 2) + " &r&f▶"));
      }
   }

   private List<BuyerItem> sorted() {
      List<BuyerItem> items = new ArrayList<>(this.plugin.catalogue().items().values());
      Comparator<BuyerItem> comparator = switch (this.sort) {
         case HOT -> Comparator.comparingDouble(
               (BuyerItem item) -> this.plugin.data().globalSold(item.id())).reversed();
         case EXPENSIVE -> Comparator.comparingDouble(
               (BuyerItem item) -> this.plugin.prices().standard(item)).reversed();
         case CHEAP -> Comparator.comparingDouble(item -> this.plugin.prices().standard(item));
         case PROFIT -> Comparator.comparingDouble(
               (BuyerItem item) -> this.plugin.prices().breakdown(this.player, this.profile, item).current())
               .reversed();
      };

      items.sort(comparator);
      return items;
   }

   private ItemStack render(BuyerItem item) {
      Prices.Breakdown b = this.plugin.prices().breakdown(this.player, this.profile, item);
      String bar = "&r " + Texts.ORANGE + "&l&n▍&r&f ";

      List<String> lore = new ArrayList<>(List.of(
            "",
            bar + "Категория: " + Texts.CYAN + this.plugin.catalogue().category(item.category()).name(),
            bar + "Начальная цена: " + Texts.COINS + Texts.num(item.price()) + Texts.COINS + " монет ¤",
            bar + "Стандартная цена: " + Texts.COINS + Texts.num(b.standard()) + Texts.COINS + " монет ¤",
            bar + "Текущая цена: " + Texts.COINS + Texts.num(b.current()) + Texts.COINS + " монет ¤",
            "&r " + Texts.ORANGE + "&l▍&r&f Очки за сдачу: " + Texts.POINTS + "+" + Texts.dec1(b.points()),
            "",
            bar + "Сдано на сервере: " + Texts.CYAN + Texts.num(this.plugin.data().globalSold(item.id())) + "&r&f шт.",
            "&r " + Texts.ORANGE + "&l▍&r&f Сдано вами: " + Texts.CYAN
                  + Texts.num(this.plugin.data().personalSold(this.profile, item.id())) + "&r&f шт."));

      return Menus.item(item.icon(), "&r" + Texts.ORANGE + item.name(), lore);
   }

   @Override
   public void click(InventoryClickEvent event) {
      event.setCancelled(true);
      switch (event.getRawSlot()) {
         case SORT -> {
            this.sort = Sort.values()[(this.sort.ordinal() + 1) % Sort.values().length];
            this.page = 0;
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            this.render();
         }
         case PREV -> {
            if (this.page > 0) {
               this.page--;
               this.render();
            }
         }
         case NEXT -> {
            if ((this.page + 1) * PER_PAGE < this.plugin.catalogue().items().size()) {
               this.page++;
               this.render();
            }
         }
         case BACK -> this.plugin.menus().openMain(this.player);
         default -> {
         }
      }
   }
}
