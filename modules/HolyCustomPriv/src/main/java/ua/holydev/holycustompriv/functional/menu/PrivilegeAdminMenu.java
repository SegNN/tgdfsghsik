package ua.holydev.holycustompriv.functional.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.holydev.holycustompriv.HolyCustomPriv;
import ua.holydev.holycustompriv.utils.HexUtil;

public class PrivilegeAdminMenu implements InventoryHolder {
   private HolyCustomPriv plugin;
   private Inventory inventory;
   private int currentPage;
   private int maxPages;
   private Map<ItemStack, Integer> privilegeItems;

   private static Map<Integer, Material> createDecorationSlots() {
      Map<Integer, Material> slots = new HashMap<>();
      int[] cyanSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44};
      int[] orangeSlots = new int[]{45, 46, 47, 48, 50, 51, 52, 53};
      Arrays.stream(cyanSlots).forEach(slot -> slots.put(slot, Material.CYAN_STAINED_GLASS_PANE));
      Arrays.stream(orangeSlots).forEach(slot -> slots.put(slot, Material.ORANGE_STAINED_GLASS_PANE));
      return Collections.unmodifiableMap(slots);
   }

   public PrivilegeAdminMenu(HolyCustomPriv plugin, int page) {
      this.plugin = plugin;
      this.currentPage = Math.max(1, page);
      this.maxPages = 1;
      this.privilegeItems = new HashMap<>();
      List<Map<String, String>> privileges = plugin.getPrivilegeManager().getPendingPrivileges();
      this.maxPages = Math.max(1, (int)Math.ceil(privileges.size() / 28.0));
      String title = "Заявки на к.привилегии (" + this.currentPage + "/" + this.maxPages + ")";
      this.inventory = Bukkit.createInventory(this, 54, HexUtil.color(title));
      this.buildMenu();
   }

   public void open(Player player) {
      player.openInventory(this.inventory);
   }

   private void buildMenu() {
      this.privilegeItems.clear();
      this.inventory.clear();
      List<Map<String, String>> privileges = this.plugin.getPrivilegeManager().getPendingPrivileges();
      this.maxPages = Math.max(1, (int)Math.ceil(privileges.size() / 28.0));
      this.addPrivileges(privileges);
      this.addNavigationButtons();
      this.addDecoration();
   }

   private void addPrivileges(List<Map<String, String>> privileges) {
      int startIndex = (this.currentPage - 1) * 28;
      int endIndex = Math.min(startIndex + 28, privileges.size());
      int slot = 10;

      for (int i = startIndex; i < endIndex && slot <= 43; i++) {
         Map<String, String> privilege = privileges.get(i);
         ItemStack item = this.createPrivilegeItem(privilege);
         this.inventory.setItem(slot, item);
         this.privilegeItems.put(item, Integer.parseInt(privilege.get("id")));
         if ((++slot - 10) % 7 == 0) {
            slot += 2;
         }
      }
   }

   private ItemStack createPrivilegeItem(Map<String, String> privilege) {
      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(HexUtil.color("&fНовая заявка на кастомную привилегию"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.color(" "));
      lore.add(HexUtil.color(" &6&l&n▍&f Заявка от &6" + privilege.get("player_name")));
      lore.add(HexUtil.color(" &6&l▍ &fПривилегия: " + privilege.get("privilege_text")));
      lore.add(HexUtil.color(""));
      lore.add(HexUtil.color(" &6&l&n▍&f &x&5&9&f&f&0&0ЛКМ &f- принять заявку"));
      lore.add(HexUtil.color(" &6&l▍&f &cПКМ &f- отклонить заявку"));
      lore.add(HexUtil.color(" "));
      lore.add(HexUtil.color("&c ℹ &fБудьте внимательны! Принимайте/отклоняйте"));
      lore.add(HexUtil.color(" &fзаявку строго по правилам создания кастомных"));
      lore.add(HexUtil.color(" &fпривилегий."));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private void addNavigationButtons() {
      int prevPageSlot = 45;
      int nextPageSlot = 53;
      if (this.currentPage > 1) {
         ItemStack close = this.createConfiguredItem("prev");
         this.inventory.setItem(prevPageSlot, close);
      }

      if (this.currentPage < this.maxPages) {
         ItemStack close = this.createConfiguredItem("next");
         this.inventory.setItem(nextPageSlot, close);
      }

      ItemStack close = this.createConfiguredItem("close");
      this.inventory.setItem(49, close);
   }

   private void addDecoration() {
      for (Entry<Integer, Material> entry : createDecorationSlots().entrySet()) {
         int slot = entry.getKey();
         Material material = entry.getValue();
         if ((slot != 45 || this.currentPage <= 1) && (slot != 53 || this.currentPage >= this.maxPages) && slot != 49) {
            ItemStack decorItem = new ItemStack(material);
            ItemMeta decorMeta = decorItem.getItemMeta();
            if (decorMeta != null) {
               decorMeta.setDisplayName(HexUtil.color(" "));
               decorMeta.addItemFlags(ItemFlag.values());
               decorItem.setItemMeta(decorMeta);
            }

            this.inventory.setItem(slot, decorItem);
         }
      }
   }

   private ItemStack createConfiguredItem(String key) {
      Map<String, Material> materials = Map.of("close", Material.BARRIER, "prev", Material.ARROW, "next", Material.ARROW);
      Map<String, String> names = Map.of("close", "&cЗакрыть меню", "prev", "&6◀ &fПредыдущая страница", "next", "&fСледующая страница &6▶");
      Material material = materials.getOrDefault(key, Material.BARRIER);
      String displayName = names.getOrDefault(key, " ");
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(HexUtil.color(displayName));
         item.setItemMeta(meta);
      }

      return item;
   }

   public Integer getPrivilegeId(ItemStack item) {
      return this.privilegeItems.get(item);
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public int size() {
      return this.privilegeItems.size();
   }
}
