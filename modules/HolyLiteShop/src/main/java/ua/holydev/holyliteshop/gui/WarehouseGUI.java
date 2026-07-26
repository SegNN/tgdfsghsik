package ua.holydev.holyliteshop.gui;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.utils.ColorUtils;

public class WarehouseGUI implements InventoryHolder {
   private static final int ITEMS_PER_PAGE = 36;
   private final HolyLiteShop plugin;
   private final Player player;
   private Inventory inventory;
   private final int page;

   public WarehouseGUI(HolyLiteShop plugin, Player player, int page) {
      this.plugin = plugin;
      this.player = player;
      this.page = page;
   }

   public void open() {
      this.inventory = Bukkit.createInventory(this, 54, ColorUtils.color("Склад купленных предметов"));
      this.updateInventory();
      this.player.openInventory(this.inventory);
   }

   private void updateInventory() {
      this.inventory.clear();
      this.setupBorders();
      this.setupWarehouseItems();
      this.setupNavigationButtons();
   }

   private void setupBorders() {
      ItemStack gray = this.createGlassPane();

      for (int i = 0; i <= 8; i++) {
         this.inventory.setItem(i, gray);
      }

      int[] bottomSlots = new int[]{45, 46, 47, 49, 51, 52, 53};

      for (int slot : bottomSlots) {
         this.inventory.setItem(slot, gray);
      }
   }

   private ItemStack createGlassPane() {
      ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(ColorUtils.color("&r"));
      pane.setItemMeta(meta);
      return pane;
   }

   private void setupWarehouseItems() {
      List<ItemStack> warehouseItems = this.plugin.getDataManager().getWarehouseItems(this.player.getUniqueId());
      int startIndex = this.page * 36;
      int endIndex = Math.min(startIndex + 36, warehouseItems.size());

      for (int i = startIndex; i < endIndex; i++) {
         ItemStack item = warehouseItems.get(i);
         int slot = 9 + (i - startIndex);
         this.inventory.setItem(slot, item);
      }
   }

   private void setupNavigationButtons() {
      ItemStack prevButton = new ItemStack(Material.ARROW);
      ItemMeta prevMeta = prevButton.getItemMeta();
      prevMeta.setDisplayName(ColorUtils.color(" &r&#00c7ff◀ Предыдущая страница"));
      prevButton.setItemMeta(prevMeta);
      this.inventory.setItem(48, prevButton);
      ItemStack nextButton = new ItemStack(Material.SPECTRAL_ARROW);
      ItemMeta nextMeta = nextButton.getItemMeta();
      nextMeta.setDisplayName(ColorUtils.color(" &r&#00c7ffСледующая страница ▶"));
      nextButton.setItemMeta(nextMeta);
      this.inventory.setItem(50, nextButton);
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public int getPage() {
      return this.page;
   }
}
