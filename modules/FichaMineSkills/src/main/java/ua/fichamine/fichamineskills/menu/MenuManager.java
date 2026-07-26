package ua.fichamine.fichamineskills.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichamineskills.bonus.BonusSystem;
import ua.fichamine.fichamineskills.database.DatabaseManager;

public class MenuManager {
   private final JavaPlugin plugin;
   private final DatabaseManager databaseManager;
   private final MenuManager.MenuHolder menuHolder;

   public MenuManager(JavaPlugin plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
      this.menuHolder = new MenuManager.MenuHolder();
   }

   public void openMenu(Player player) {
      try {
         CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
         pointsFuture.thenAcceptAsync(points -> Bukkit.getScheduler().runTask(this.plugin, () -> {
            try {
               Inventory inventory = this.createMenu(player, points);
               player.openInventory(inventory);
            } catch (Exception var4) {
            }
         })).exceptionally(e -> null);
      } catch (Exception var3) {
      }
   }

   private Inventory createMenu(Player player, double points) {
      int currentPage = BonusSystem.getAvailablePage(points);
      String title = currentPage == 1 ? "Квестовик (Автошахта)" : "Квестовик (Автошахта) 2";
      Inventory inventory = Bukkit.createInventory(this.menuHolder, 54, title);
      this.createFrame(inventory);
      this.createInfoItems(inventory, points);
      this.createBonusItems(inventory, points, currentPage);
      return inventory;
   }

   private void createFrame(Inventory inventory) {
      ItemStack frameItem = this.createFrameItem();

      for (int i = 0; i < 9; i++) {
         inventory.setItem(i, frameItem);
         inventory.setItem(45 + i, frameItem);
      }
   }

   private void createInfoItems(Inventory inventory, double points) {
      inventory.setItem(45, this.createInfoBook());
      inventory.setItem(53, this.createInfoLectern(points));
   }

   private void createBonusItems(Inventory inventory, double points, int page) {
      for (BonusSystem.Bonus bonus : BonusSystem.getBonusesForPage(page)) {
         if (bonus.getSlot() >= 10 && bonus.getSlot() <= 44) {
            ItemStack bonusItem = bonus.createItem(points);
            ItemMeta meta = bonusItem.getItemMeta();
            if (meta != null) {
               meta.removeItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               bonusItem.setItemMeta(meta);
            }

            inventory.setItem(bonus.getSlot(), bonusItem);
         }
      }
   }

   private ItemStack createFrameItem() {
      ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(" ");
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createInfoBook() {
      ItemStack book = new ItemStack(Material.BOOK);
      ItemMeta meta = book.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Кто такой квестовик?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f Это NPC, который выдаёт задания"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f и награды за прогресс на автошахте."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f За выполнение заданий вы получаете"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f бонусы, которые помогают в игре."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Как получать бонусы?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f За опыт открываются новые уровни бонусов:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f скорость, удача, авто-плавка и др."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f Прогресс и активные бонусы — в этом меню."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Как зарабатывать опыт?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l&n▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f Опыт даётся за добычу блоков на автошахте."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&x&0&0&D&8&F&F&l&n▍&f Чем ценнее блок, тем больше опыта."));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F&l▍"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      meta.setLore(lore);
      book.setItemMeta(meta);
      return book;
   }

   private ItemStack createInfoLectern(double points) {
      ItemStack lectern = new ItemStack(Material.LECTERN);
      ItemMeta meta = lectern.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7"));
      List<String> effects = BonusSystem.getActiveEffects(points);
      List<String> lore = new ArrayList<>();
      Object[] var10003 = new Object[]{points};
      lore.add(
         ChatColor.translateAlternateColorCodes(
            '&', "&5&o&x&0&0&D&8&F&F▶&x&E&7&E&7&E&7 Вы имеете &x&0&0&D&8&F&F" + String.format("%.0f", var10003) + " &fопыта."
         )
      );
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o &7Активные эффекты:"));
      if (effects.isEmpty()) {
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o  &8эффекты отсутствуют"));
      } else {
         lore.addAll(effects);
      }

      meta.setLore(lore);
      lectern.setItemMeta(meta);
      return lectern;
   }

   public static class MenuHolder implements InventoryHolder {
      public MenuHolder() {
      }

      public Inventory getInventory() {
         return null;
      }
   }
}
