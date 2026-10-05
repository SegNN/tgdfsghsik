package org.impldev.holybuyer.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.BuyerItem;
import org.impldev.holybuyer.Catalogue;
import org.impldev.holybuyer.HolyBuyer;
import org.impldev.holybuyer.Prices;
import org.impldev.holybuyer.Texts;

/**
 * Фильтр авто-продаж: что скупщик забирает сам и по какой цене.
 *
 * <p>Левый столбец - категории, остальное - товары выбранной категории с полным разбором цены.
 */
public final class AutoMenu extends BuyerMenu {
   /** слоты категорий - по одному на строку */
   private static final int[] CATEGORY_SLOTS = {0, 9, 18, 27, 36};
   /** слоты товаров: две клетки слева каждой строки заняты категорией и разделителем */
   private static final int[] ITEM_SLOTS = {
         2, 3, 4, 5, 6, 7, 8,
         11, 12, 13, 14, 15, 16, 17,
         20, 21, 22, 23, 24, 25, 26,
         29, 30, 31, 32, 33, 34, 35};

   private static final int ENABLE_ALL = 46;
   private static final int DISABLE_ALL = 47;
   private static final int MODE = 49;
   private static final int FRAGMENTS = 51;
   private static final int BACK = 53;

   private final HolyBuyer plugin;
   private final BuyerData.Profile profile;
   private final Map<Integer, String> slotItems = new HashMap<>();
   private final Map<Integer, String> slotCategories = new HashMap<>();
   private String selected;

   public AutoMenu(HolyBuyer plugin, Player player, BuyerData.Profile profile, String selected) {
      super(player, 54, "&rФильтр Авто-продаж");
      this.plugin = plugin;
      this.profile = profile;
      this.selected = selected != null && plugin.catalogue().category(selected) != null
            ? selected
            : plugin.catalogue().categories().keySet().iterator().next();
   }

   @Override
   public void render() {
      this.inventory.clear();
      this.slotItems.clear();
      this.slotCategories.clear();

      for (int slot : new int[]{1, 10, 19, 28}) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      for (int slot = 37; slot <= 45; slot++) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      this.renderCategories();
      this.renderItems();

      this.inventory.setItem(ENABLE_ALL, Menus.item(Material.LIME_DYE,
            "&r " + Texts.POINTS + "✔ Включить все товары",
            "",
            "&r " + Texts.CYAN + "&l&n▍&r&f Включение авто-продажи для",
            "&r " + Texts.CYAN + "&l▍&r&f всех товаров из этой категории",
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы включить"));
      this.inventory.setItem(DISABLE_ALL, Menus.item(Material.RED_DYE,
            "&r " + Texts.RED + "✘ Отключить все товары",
            "",
            "&r " + Texts.CYAN + "&l&n▍&r&f Отключение авто-продажи для",
            "&r " + Texts.CYAN + "&l▍&r&f всех товаров из этой категории",
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы отключить"));
      this.inventory.setItem(MODE, this.mode());
      this.inventory.setItem(FRAGMENTS, this.fragments());
      this.inventory.setItem(BACK, Menus.item(Material.ARROW, "&r" + Texts.RED + "◀ &r&fНазад"));
   }

   private void renderCategories() {
      String top = this.plugin.data().topCategory(this.profile);
      int index = 0;
      for (Catalogue.Category category : this.plugin.catalogue().categories().values()) {
         if (index >= CATEGORY_SLOTS.length) {
            break;
         }

         int slot = CATEGORY_SLOTS[index++];
         double points = this.profile.categoryPoints.getOrDefault(category.id(), 0.0);
         boolean isTop = category.id().equals(top);

         List<String> lore = new ArrayList<>(List.of(
               "",
               "&r " + Texts.CYAN + "● &r&fНакоплено очков: " + Texts.POINTS + Texts.num(points),
               ""));

         if (isTop) {
            lore.add("&r " + Texts.ORANGE + "&l&n▍&r&f Бонус " + Texts.COINS + Texts.signed1(category.topBonus())
                  + Texts.COINS + "% монет ¤&r&f применяется,");
            lore.add("&r " + Texts.ORANGE + "&l▍&r&f так как, эта категория – ТОП #1");
         } else {
            lore.add("&r " + Texts.CYAN + "&l&n▍&r&f Бонус " + Texts.COINS + Texts.signed(category.topBonus())
                  + Texts.COINS + "% монет ¤&r&f, если");
            lore.add("&r " + Texts.CYAN + "&l▍&r&f эта категория станет ТОП #1");
         }

         lore.add("");
         lore.add("&r " + Texts.CYAN + (category.id().equals(this.selected)
               ? "● &r&fКатегория уже открыта"
               : "▶ &r&fНажмите, чтобы открыть категорию"));

         ItemStack icon = Menus.item(category.icon(),
               "&r " + Texts.CYAN + "❏ &r&fКатегория: " + Texts.CYAN + category.name() + Texts.CYAN + " ❏", lore);
         if (category.id().equals(this.selected)) {
            icon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
         }

         this.inventory.setItem(slot, icon);
         this.slotCategories.put(slot, category.id());
      }
   }

   private void renderItems() {
      List<BuyerItem> items = this.plugin.catalogue().inCategory(this.selected);
      for (int i = 0; i < items.size() && i < ITEM_SLOTS.length; i++) {
         BuyerItem item = items.get(i);
         int slot = ITEM_SLOTS[i];
         this.inventory.setItem(slot, this.render(item));
         this.slotItems.put(slot, item.id());
      }
   }

   /**
    * Описание товара: сначала откуда взялась цена, потом сама цена.
    *
    * <p>Строки надбавок показываются только когда надбавка есть - пустые "+0%" в описании
    * ничего не объясняют и только удлиняют его.
    */
   private ItemStack render(BuyerItem item) {
      Prices.Breakdown b = this.plugin.prices().breakdown(this.player, this.profile, item);
      String bar = "&r " + Texts.ORANGE + "&l&n▍&r&f ";
      String last = "&r " + Texts.ORANGE + "&l▍&r&f ";

      List<String> lore = new ArrayList<>();
      lore.add("");

      if (!item.also().isEmpty()) {
         lore.add("&r&f " + Texts.ORANGE + "● &r&fА также доступно для продажи:");
         lore.add("&r   " + Texts.ORANGE + String.join(Texts.ORANGE + ", " + Texts.ORANGE, item.also()));
         lore.add("");
      }

      List<String> rows = new ArrayList<>();
      rows.add(b.personal() >= 0.0
            ? "Сдали мало: " + Texts.GREEN + Texts.signed(b.personal()) + Texts.GREEN + "%"
            : "Сдали много: " + Texts.RED + Texts.signed(b.personal()) + Texts.RED + "%");
      rows.add("&r&fБазовый коэффициент: " + Texts.POINTS + Texts.signed(b.baseCoef()) + Texts.POINTS + "%");
      if (b.categoryBonus() != 0.0) {
         rows.add("&r&fБонус за категорию: " + Texts.POINTS + Texts.signed1(b.categoryBonus()) + Texts.POINTS + "%");
      }
      if (b.invest() != 0.0) {
         rows.add("&r&fБуст от инвестиций: " + Texts.INVEST + Texts.signed(b.invest()) + "%");
      }
      if (b.localBonus() != 0.0) {
         rows.add("&r&fЛокальный бонус: " + Texts.COINS + Texts.signed1(b.localBonus()) + Texts.COINS + "%");
      }
      if (b.ulta() != 0.0) {
         rows.add("&r&fУльта Скупщика: " + Texts.POINTS + Texts.signed(b.ulta()) + Texts.POINTS + "%");
      }
      rows.add("&r&fБустер Скупщика: " + Texts.POINTS + Texts.signed(b.booster()) + Texts.POINTS + "%");

      for (int i = 0; i < rows.size(); i++) {
         lore.add((i == rows.size() - 1 ? last : bar) + rows.get(i));
      }

      lore.add("");
      lore.add(bar + "Стандартная цена: " + Texts.COINS + Texts.num(b.standard()) + Texts.COINS + " монет ¤");
      lore.add(bar + "Текущая цена: " + Texts.COINS + Texts.num(b.current()) + Texts.COINS + " монет ¤");
      lore.add(last + "Очки за сдачу: " + Texts.POINTS + "+" + Texts.POINTS + Texts.dec1(b.points()));
      lore.add("");

      boolean enabled = this.profile.autoSell.contains(item.id());
      lore.add(enabled
            ? "&r " + Texts.POINTS + "✔ &r&fАвто-продажа включена &r&8(нажмите, чтобы выключить)"
            : "&r " + Texts.RED + "✘ &r&fАвто-продажа выключена &r&8(нажмите, чтобы включить)");

      ItemStack stack = Menus.item(item.icon(), "&r" + Texts.ORANGE + item.name(), lore);
      if (enabled) {
         stack.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
      }

      return stack;
   }

   private ItemStack mode() {
      BuyerData.AutoMode current = this.profile.autoMode;
      return Menus.item(Material.COMPARATOR, "&r " + Texts.CYAN + "Авто-продажа",
            line(current == BuyerData.AutoMode.ON, "Включено"),
            line(current == BuyerData.AutoMode.UNTIL_QUIT, "Включено (до выхода)"),
            line(current == BuyerData.AutoMode.OFF, "Выключено"),
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы переключить");
   }

   private static String line(boolean active, String text) {
      return active
            ? "&r" + Texts.ORANGE + "✔ &r&f" + text
            : "&r" + Texts.BLUE + "● &r&f" + text;
   }

   private ItemStack fragments() {
      Catalogue.Category category = this.plugin.catalogue().category(this.selected);
      int cost = this.plugin.prices().fragmentCost(this.profile.fragmentAttempts + 1);
      double step = this.plugin.getConfig().getDouble("fragments.bonus-percent", 20.0);

      return Menus.item(Material.PRISMARINE_CRYSTALS, "&r " + Texts.ORANGE + "Повысить локальный бонус",
            "",
            "&r " + Texts.ORANGE + "&l&n▍&r&f Вы получите дополнительно " + Texts.COINS + "+"
                  + Texts.COINS + Texts.dec1(step) + Texts.COINS + "%&r&f к",
            "&r " + Texts.ORANGE + "&l&n▍&r&f локальному бонусу случайного товара",
            "&r " + Texts.ORANGE + "&l▍&r&f из категории " + Texts.ORANGE + category.name(),
            "",
            "&r " + Texts.ORANGE + "▶ &r&fНажмите, чтобы купить повышение",
            "&r    &r&fза x&r&f" + cost + "&r&f " + Texts.ORANGE + "Боевой фрагмент");
   }

   @Override
   public void click(InventoryClickEvent event) {
      event.setCancelled(true);
      int slot = event.getRawSlot();
      if (slot < 0 || slot >= 54) {
         return;
      }

      String category = this.slotCategories.get(slot);
      if (category != null) {
         this.selected = category;
         this.click(Sound.UI_BUTTON_CLICK);
         this.render();
         return;
      }

      String itemId = this.slotItems.get(slot);
      if (itemId != null) {
         if (!this.profile.autoSell.remove(itemId)) {
            this.profile.autoSell.add(itemId);
         }

         this.profile.touch();
         this.plugin.data().savePlayer(this.profile);
         this.click(Sound.BLOCK_LEVER_CLICK);
         this.render();
         return;
      }

      switch (slot) {
         case ENABLE_ALL -> this.toggleAll(true);
         case DISABLE_ALL -> this.toggleAll(false);
         case MODE -> this.switchMode(event.getClick());
         case FRAGMENTS -> this.buyLocalBonus();
         case BACK -> this.plugin.menus().openMain(this.player);
         default -> {
         }
      }
   }

   private void toggleAll(boolean enable) {
      for (BuyerItem item : this.plugin.catalogue().inCategory(this.selected)) {
         if (enable) {
            this.profile.autoSell.add(item.id());
         } else {
            this.profile.autoSell.remove(item.id());
         }
      }

      this.profile.touch();
      this.plugin.data().savePlayer(this.profile);
      this.click(Sound.BLOCK_LEVER_CLICK);
      this.render();
   }

   /** Левая кнопка листает режимы вперёд, правая - назад. */
   private void switchMode(ClickType type) {
      BuyerData.AutoMode[] modes = BuyerData.AutoMode.values();
      int step = type.isRightClick() ? modes.length - 1 : 1;
      this.profile.autoMode = modes[(this.profile.autoMode.ordinal() + step) % modes.length];
      this.profile.touch();
      this.plugin.data().savePlayer(this.profile);
      this.click(Sound.UI_BUTTON_CLICK);
      this.render();
   }

   private void buyLocalBonus() {
      int cost = this.plugin.prices().fragmentCost(this.profile.fragmentAttempts + 1);
      if (!this.plugin.fragments().take(this.player, cost)) {
         this.player.sendMessage(Texts.color("&r " + Texts.RED + "✘ &r&fНужно " + Texts.ORANGE + cost
               + "&r&f Боевых фрагментов."));
         this.click(Sound.BLOCK_NOTE_BLOCK_BASS);
         return;
      }

      List<BuyerItem> items = this.plugin.catalogue().inCategory(this.selected);
      if (items.isEmpty()) {
         return;
      }

      BuyerItem lucky = items.get(this.plugin.random().nextInt(items.size()));
      this.profile.fragmentAttempts++;
      this.profile.localBonus.merge(lucky.id(), 1, Integer::sum);
      this.profile.touch();
      this.plugin.data().savePlayer(this.profile);

      double step = this.plugin.getConfig().getDouble("fragments.bonus-percent", 20.0);
      this.player.sendMessage(Texts.color("&r " + Texts.ORANGE + "▶ &r&fЛокальный бонус " + Texts.COINS
            + Texts.signed1(step) + Texts.COINS + "%&r&f достался товару " + Texts.ORANGE + lucky.name() + "&r&f."));
      this.click(Sound.ENTITY_PLAYER_LEVELUP);
      this.render();
   }

   private void click(Sound sound) {
      this.player.playSound(this.player.getLocation(), sound, 1.0F, 1.0F);
   }
}
