package org.impldev.holycore.tituls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.utils.Parser;

/** Меню титулов: страницы, сортировка, снятие активного и выдача титула предметом. */
public final class TitulMenu implements Listener {
   private static final int SIZE = 54;
   private static final int[] CONTENT = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
   private static final int[] GRAY = {0, 8, 45, 53};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35, 36, 44};
   private static final int REMOVE_SLOT = 46;
   private static final int PREV_SLOT = 48;
   private static final int SORT_SLOT = 49;
   private static final int NEXT_SLOT = 50;

   /** Редкость у всех титулов одинаковая - отдельного источника этих данных на сервере нет. */
   static final String RARITY = "&r&x&0&0&a&d&c&cК&r&x&0&0&b&8&d&9е&r&x&0&0&c&3&e&6й"
         + "&r&x&0&0&c&d&f&2с&r&x&0&0&d&8&f&fо&r&x&0&0&c&a&e&eв&r&x&0&0&b&b&d&dы&r&x&0&0&a&d&c&cй";

   /** Порядок сортировки: 0 - сначала новые, 1 - сначала старые, 2 - сначала кастомные. */
   private final Map<UUID, Integer> sort = new HashMap<>();
   private final Map<UUID, Integer> page = new HashMap<>();
   /** чей список сейчас открыт - для /tituls <ник> */
   private final Map<UUID, String> viewing = new HashMap<>();

   private final JavaPlugin plugin;
   private final TitulRegistry registry;

   public TitulMenu(JavaPlugin plugin, TitulRegistry registry) {
      this.plugin = plugin;
      this.registry = registry;
   }

   public void open(Player viewer, Player owner, int pageNumber) {
      this.page.put(viewer.getUniqueId(), pageNumber);
      this.viewing.put(viewer.getUniqueId(), owner.getName());

      Inventory inv = Bukkit.createInventory(null, SIZE,
            Parser.color("&rТитулы &r" + owner.getName()));
      this.render(inv, viewer, owner);
      viewer.openInventory(inv);
   }

   private List<Titul> owned(Player owner, int mode) {
      List<Titul> list = new ArrayList<>(this.registry.all().stream()
            .filter(t -> owner.hasPermission(t.permission()))
            .toList());

      Comparator<Titul> byAge = Comparator.comparingInt(Titul::seasonRank).thenComparingInt(Titul::order);
      switch (mode) {
         case 1 -> list.sort(byAge);
         case 2 -> list.sort(Comparator.comparing(Titul::season).thenComparingInt(Titul::order));
         default -> list.sort(byAge.reversed());
      }

      return list;
   }

   private void render(Inventory inv, Player viewer, Player owner) {
      inv.clear();
      for (int slot : GRAY) {
         inv.setItem(slot, pane(Material.GRAY_STAINED_GLASS_PANE));
      }
      for (int slot : ORANGE) {
         inv.setItem(slot, pane(Material.ORANGE_STAINED_GLASS_PANE));
      }

      int mode = this.sort.getOrDefault(viewer.getUniqueId(), 0);
      int pageNumber = this.page.getOrDefault(viewer.getUniqueId(), 1);
      List<Titul> list = this.owned(owner, mode);

      int from = (pageNumber - 1) * CONTENT.length;
      for (int i = 0; i < CONTENT.length && from + i < list.size(); i++) {
         inv.setItem(CONTENT[i], this.icon(list.get(from + i)));
      }

      inv.setItem(REMOVE_SLOT, item(Material.RED_DYE, "&r &r&x&f&f&2&2&2&2✘ Снять активный титул", List.of(
            "",
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Если у вас был активен титул,",
            "&r &r&x&0&0&d&8&f&f&l▍&r&f то он не будет отображаться.",
            "",
            "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы снять титул")));

      inv.setItem(PREV_SLOT, pageNumber > 1
            ? item(Material.ARROW, "&r &r&x&0&0&d&8&f&f◀ Предыдущая страница", List.of())
            : pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
      inv.setItem(NEXT_SLOT, from + CONTENT.length < list.size()
            ? item(Material.ARROW, "&r &r&x&f&f&7&0&0&0Следующая страница ▶", List.of())
            : pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE));

      inv.setItem(SORT_SLOT, item(Material.HOPPER, "&r &r&x&0&0&d&8&f&fСортировка", List.of(
            "",
            mark(mode == 0) + "Сначала новые",
            mark(mode == 1) + "Сначала старые",
            mark(mode == 2) + "Сначала кастомные",
            "",
            "&r &r&x&0&0&d&8&f&f● &r&fДоступно титулов: &r&x&0&0&d&8&f&f" + list.size(),
            "")));
   }

   private static String mark(boolean active) {
      return active
            ? "&r &r&x&f&f&7&0&0&0✔ &r&x&f&f&7&0&0&0"
            : "&r &r&x&9&c&f&9&f&f●&r&f &r&f";
   }

   private ItemStack icon(Titul titul) {
      return item(Material.NAME_TAG, "&r " + titul.text(), List.of(
            "",
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Редкость: " + RARITY,
            "&r &r&x&0&0&d&8&f&f&l▍&r&f Дата появления: &r&x&0&0&d&8&f&f" + titul.date(),
            ""));
   }

   /** Титул в виде предмета - его можно передать другому и активировать по ПКМ. */
   static ItemStack asItem(Titul titul) {
      return item(Material.NAME_TAG, titul.text(), List.of(
            "",
            "&r &r&x&f&f&7&0&0&0Титул",
            "&r &r&x&f&f&7&0&0&0&l&n▍&r&f Редкость: " + RARITY,
            "&r &r&x&f&f&7&0&0&0&l▍&r&f Дата появления: &r&x&f&f&7&0&0&0" + titul.date(),
            "",
            "&r&8Особенности:",
            "&r &r&f– можно активировать, чтобы получить этот",
            "&r    &r&fтитул себе в &r&x&f&f&7&0&0&0/tituls&r&f, при нажатии на ПКМ.",
            "",
            "&r&8" + titul.permission()));
   }

   private static ItemStack pane(Material material) {
      return item(material, "&r ", List.of());
   }

   private static ItemStack item(Material material, String name, List<String> lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Parser.color(name));
         if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(Parser::color).toList());
         }

         stack.setItemMeta(meta);
      }

      return stack;
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player viewer)) {
         return;
      }

      String ownerName = this.viewing.get(viewer.getUniqueId());
      if (ownerName == null || !event.getView().getTitle().equals(Parser.color("&rТитулы &r" + ownerName))) {
         return;
      }

      event.setCancelled(true);
      if (event.getClickedInventory() != event.getView().getTopInventory()) {
         return;
      }

      Player owner = Bukkit.getPlayerExact(ownerName);
      if (owner == null) {
         viewer.closeInventory();
         return;
      }

      int slot = event.getSlot();
      int mode = this.sort.getOrDefault(viewer.getUniqueId(), 0);
      int pageNumber = this.page.getOrDefault(viewer.getUniqueId(), 1);

      if (slot == SORT_SLOT) {
         this.sort.put(viewer.getUniqueId(), (mode + 1) % 3);
         this.render(event.getView().getTopInventory(), viewer, owner);
         return;
      }

      if (slot == PREV_SLOT && pageNumber > 1) {
         this.page.put(viewer.getUniqueId(), pageNumber - 1);
         this.render(event.getView().getTopInventory(), viewer, owner);
         return;
      }

      if (slot == NEXT_SLOT) {
         this.page.put(viewer.getUniqueId(), pageNumber + 1);
         this.render(event.getView().getTopInventory(), viewer, owner);
         return;
      }

      if (slot == REMOVE_SLOT) {
         // чужой титул снимать нельзя - смотреть можно, распоряжаться нет
         if (!owner.equals(viewer)) {
            return;
         }

         Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
               "lp user " + viewer.getName() + " meta removesuffix 1");
         viewer.sendMessage(Parser.color("&r&x&0&0&d&8&f&f▶ &r&fТитул снят."));
         return;
      }

      int index = indexOf(slot);
      if (index < 0) {
         return;
      }

      List<Titul> list = this.owned(owner, mode);
      int position = (pageNumber - 1) * CONTENT.length + index;
      if (position >= list.size()) {
         return;
      }

      Titul titul = list.get(position);
      if (!owner.equals(viewer)) {
         return;
      }

      if (event.isRightClick()) {
         viewer.getInventory().addItem(asItem(titul));
         viewer.sendMessage(Parser.color("&r&x&f&f&7&0&0&0▶ &r&fТитул выдан предметом."));
         return;
      }

      Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
            "lp user " + viewer.getName() + " meta setsuffix 1 \"" + titul.text() + "\" server=lite");
      viewer.sendMessage(Parser.color("&r&x&f&b&5&e&1&e[&l⚡&r&x&f&b&5&e&1&e] &r&7Вы успешно установили себе титул!"));
      viewer.playSound(viewer.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
   }

   private static int indexOf(int slot) {
      for (int i = 0; i < CONTENT.length; i++) {
         if (CONTENT[i] == slot) {
            return i;
         }
      }

      return -1;
   }
}
