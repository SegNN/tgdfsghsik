package ua.holydev.holycustompriv.functional.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.holydev.holycustompriv.HolyCustomPriv;
import ua.holydev.holycustompriv.utils.HexUtil;

public class TitulAdmin implements InventoryHolder {
   private HolyCustomPriv plugin;
   private Inventory inventory;
   private int currentPage;
   private int maxPages;
   private Map<ItemStack, Integer> titleItems;

   public TitulAdmin(HolyCustomPriv plugin, int page) {
      this.plugin = plugin;
      this.currentPage = Math.max(1, page);
      this.maxPages = 1;
      this.titleItems = new HashMap<>();
      this.buildMenu();
   }

   public void open(Player player) {
      player.openInventory(this.inventory);
   }

   private void buildMenu() {
      this.titleItems.clear();
      List<Map<String, String>> titles = this.plugin.getTitulManager().getPendingTitles();
      this.maxPages = Math.max(1, (int)Math.ceil(titles.size() / 28.0));
      String title = "Заявки на к.титулы (" + this.currentPage + "/" + this.maxPages + ")";
      this.inventory = Bukkit.createInventory(this, 54, HexUtil.color(title));
      this.addTitles(titles);
      this.addNavigationButtons();
      this.addDecoration();
   }

   private void addTitles(List<Map<String, String>> titles) {
      int startIndex = (this.currentPage - 1) * 28;
      int endIndex = Math.min(startIndex + 28, titles.size());
      int slot = 10;

      for (int i = startIndex; i < endIndex && slot <= 43; i++) {
         Map<String, String> title = titles.get(i);
         ItemStack item = this.createTitleItem(title);
         if (item != null) {
            this.inventory.setItem(slot, item);
            this.titleItems.put(item, Integer.parseInt(title.get("id")));
            if ((++slot - 10) % 7 == 0) {
               slot += 2;
            }
         }
      }
   }

   private ItemStack createTitleItem(Map<String, String> title) {
      try {
         String titleText = title.get("title");
         if (titleText != null && !titleText.trim().isEmpty()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(HexUtil.color("&fНовая заявка на кастомный титул"));
            List<String> lore = new ArrayList<>();
            lore.add(HexUtil.color(" "));
            lore.add(HexUtil.color(" &6&l&n▍&f Заявка от &6" + title.get("player_name")));
            lore.add(HexUtil.color(" &6&l▍ &fТитул: " + titleText));
            lore.add(HexUtil.color(""));
            lore.add(HexUtil.color(" &6&l&n▍&f &x&5&9&f&f&0&0ЛКМ &f- принять заявку"));
            lore.add(HexUtil.color(" &6&l▍&f &cПКМ &f- отклонить заявку"));
            lore.add(HexUtil.color(" "));
            lore.add(HexUtil.color("&c ℹ &fБудьте внимательны! Принимайте/отклоняйте"));
            lore.add(HexUtil.color(" &fзаявку строго по правилам создания кастомных"));
            lore.add(HexUtil.color(" &fтитулов."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
         } else {
            return null;
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         return null;
      }
   }

   private void addNavigationButtons() {
      int prevPageSlot = 45;
      int nextPageSlot = 53;
      if (this.currentPage > 1) {
         ItemStack prev = this.createNavigationItem("prev");
         this.inventory.setItem(prevPageSlot, prev);
      }

      if (this.currentPage < this.maxPages) {
         ItemStack next = this.createNavigationItem("next");
         this.inventory.setItem(nextPageSlot, next);
      }

      ItemStack close = this.createNavigationItem("close");
      this.inventory.setItem(49, close);
   }

   private ItemStack createNavigationItem(String type) {
      Material material;
      String displayName;
      switch (type) {
         case "close":
            material = Material.BARRIER;
            displayName = "&cЗакрыть меню";
            break;
         case "prev":
            material = Material.ARROW;
            displayName = "&6◀ &fПредыдущая страница";
            break;
         case "next":
            material = Material.ARROW;
            displayName = "&fСледующая страница &6▶";
            break;
         default:
            material = Material.BARRIER;
            displayName = " ";
      }

      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(HexUtil.color(displayName));
         item.setItemMeta(meta);
      }

      return item;
   }

   private void addDecoration() {
      int[] cyanSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44};
      int[] orangeSlots = new int[]{45, 46, 47, 48, 50, 51, 52, 53};

      for (int slot : cyanSlots) {
         if (slot != 45 && slot != 53 && slot != 49) {
            ItemStack decorItem = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
            ItemMeta decorMeta = decorItem.getItemMeta();
            if (decorMeta != null) {
               decorMeta.setDisplayName(HexUtil.color(" "));
               decorItem.setItemMeta(decorMeta);
            }

            this.inventory.setItem(slot, decorItem);
         }
      }

      for (int slotx : orangeSlots) {
         if (slotx != 45 && slotx != 53 && slotx != 49) {
            ItemStack decorItem = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            ItemMeta decorMeta = decorItem.getItemMeta();
            if (decorMeta != null) {
               decorMeta.setDisplayName(HexUtil.color(" "));
               decorItem.setItemMeta(decorMeta);
            }

            this.inventory.setItem(slotx, decorItem);
         }
      }
   }

   public Integer getTitleId(ItemStack item) {
      return this.titleItems.get(item);
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public int getMaxPages() {
      return this.maxPages;
   }
}
