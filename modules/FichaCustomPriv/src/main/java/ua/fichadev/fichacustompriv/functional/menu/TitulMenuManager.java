package ua.fichadev.fichacustompriv.functional.menu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class TitulMenuManager {
   private FichaCustomPriv plugin;
   private HashMap<UUID, String> playerTitles;

   public TitulMenuManager(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.playerTitles = new HashMap<>();
   }

   public void openMainMenu(Player player) {
      Inventory menu = Bukkit.createInventory((InventoryHolder)null, 54, HexUtil.color("Кастомные титулы"));
      this.fillMainMenu(menu, player);
      player.openInventory(menu);
   }

   private void fillMainMenu(Inventory menu, Player player) {
      this.addPlayerTitles(menu, player);
      this.addStaticItems(menu, player);
      this.addDecoration(menu);
   }

   private void addPlayerTitles(Inventory menu, Player player) {
      List<Map<String, String>> playerTitles = this.getPlayerTitles(player.getUniqueId().toString());
      int slot = 10;

      for (Map<String, String> title : playerTitles) {
         if (slot > 43) {
            break;
         }

         ItemStack titleItem = this.createTitleItem(title);
         menu.setItem(slot, titleItem);
         if ((++slot - 11) % 9 == 0) {
            slot += 2;
         }
      }
   }

   private List<Map<String, String>> getPlayerTitles(String playerUUID) {
      List<Map<String, String>> allTitles = new ArrayList<>();

      for (Map<String, String> title : this.plugin.getTitulManager().getPendingTitles()) {
         if (playerUUID.equals(title.get("player_uuid"))) {
            Map<String, String> titleData = new HashMap<>(title);
            titleData.put("status", "pending");
            allTitles.add(titleData);
         }
      }

      for (Map<String, String> titlex : this.plugin.getTitulManager().getApprovedTitles()) {
         if (playerUUID.equals(titlex.get("player_uuid"))) {
            Map<String, String> titleData = new HashMap<>(titlex);
            titleData.put("status", "approved");
            allTitles.add(titleData);
         }
      }

      return allTitles;
   }

   private ItemStack createTitleItem(Map<String, String> title) {
      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      String titleText = title.get("title");
      String status = title.get("status");
      String createdTime = title.get("created_time");
      String formattedTime = this.formatTime(createdTime);
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.color("&6&n▍&f Титул: &e" + titleText));
      if ("approved".equals(status)) {
         lore.add(HexUtil.color("&6&n▍&f Статус: &aОдобрен"));
      } else {
         lore.add(HexUtil.color("&6&n▍&f Статус: &eНа рассмотрении"));
      }

      lore.add(HexUtil.color("&6&n▍&f Создан: &7" + formattedTime));
      lore.add("");
      if ("approved".equals(status)) {
         lore.add(HexUtil.color("&a▸ Нажмите чтобы выбрать этот титул"));
      } else {
         lore.add(HexUtil.color("&7▸ Ожидает одобрения администратора"));
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private String formatTime(String timeString) {
      try {
         long timestamp = Long.parseLong(timeString);
         SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
         return outputFormat.format(new Date(timestamp));
      } catch (Exception var5) {
         return timeString;
      }
   }

   private void addStaticItems(Inventory menu, Player player) {
      ItemStack clearItem = new ItemStack(Material.BARRIER);
      ItemMeta clearMeta = clearItem.getItemMeta();
      clearMeta.setDisplayName(HexUtil.color("&x&F&F&2&7&0&0✘ &fУбрать титул в табе и чате"));
      List<String> clearLore = new ArrayList<>();
      clearLore.add(HexUtil.color(""));
      clearLore.add(HexUtil.color(" &x&F&F&2&7&0&0&l&n▍&f Нажмите, чтобы убрать текущий титул"));
      clearLore.add(HexUtil.color(" &x&F&F&2&7&0&0&l▍&f из таба и чата"));
      clearMeta.setLore(clearLore);
      clearItem.setItemMeta(clearMeta);
      menu.setItem(0, clearItem);
      ItemStack createItem = new ItemStack(Material.ITEM_FRAME);
      ItemMeta createMeta = createItem.getItemMeta();
      createMeta.setDisplayName(HexUtil.color(" &x&5&A&D&9&F&B❏ Меню создания титула ❏"));
      List<String> createLore = new ArrayList<>();
      createLore.add(HexUtil.color(""));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f Нажмите, чтобы перейти в меню"));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l▍&f создания кастомного титула"));
      createLore.add(HexUtil.color(""));
      createMeta.setLore(createLore);
      createItem.setItemMeta(createMeta);
      menu.setItem(49, createItem);
   }

   private void addDecoration(Inventory menu) {
      ItemStack cyanPane = this.createGlassPane(Material.CYAN_STAINED_GLASS_PANE, "");
      int[] cyanSlots = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 48, 50, 52, 53};

      for (int slot : cyanSlots) {
         menu.setItem(slot, cyanPane);
      }

      ItemStack orangePane = this.createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, "");
      int[] orangeSlots = new int[]{18, 26, 27, 35};

      for (int slot : orangeSlots) {
         menu.setItem(slot, orangePane);
      }
   }

   private ItemStack createGlassPane(Material material, String displayName) {
      ItemStack pane = new ItemStack(material);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(HexUtil.color(displayName));
      pane.setItemMeta(meta);
      return pane;
   }

   public void openCreationMenu(Player player) {
      Inventory menu = Bukkit.createInventory((InventoryHolder)null, 54, HexUtil.color("Создание титула"));
      this.fillCreationMenu(menu, player);
      player.openInventory(menu);
   }

   private void fillCreationMenu(Inventory menu, Player player) {
      this.addCreationItems(menu, player);
      this.addDecoration(menu);
   }

   private void addCreationItems(Inventory menu, Player player) {
      ItemStack infoItem = new ItemStack(Material.WRITABLE_BOOK);
      ItemMeta infoMeta = infoItem.getItemMeta();
      infoMeta.setDisplayName(HexUtil.color(" &x&5&A&D&9&F&BИнформация для создания титулов"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(HexUtil.color(""));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f При нажатии на эту кнопку в чате"));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f появится ссылка, где вы сможете ознакомиться"));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f с правилами создания титула и также"));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l▍&f с подробной инструкцией по созданию титула."));
      infoLore.add(HexUtil.color(""));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f Ссылка кликабельна и откроет страничку в вашем"));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f браузере. Вы также сможете скопировать ссылку"));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f и вставить её в нужный браузер вручную."));
      infoLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l▍&f Не создавайте титул, пока не ознакомитесь!"));
      infoLore.add(HexUtil.color(""));
      infoMeta.setLore(infoLore);
      infoItem.setItemMeta(infoMeta);
      menu.setItem(21, infoItem);
      ItemStack createItem = new ItemStack(Material.PAPER);
      ItemMeta createMeta = createItem.getItemMeta();
      createMeta.setDisplayName(HexUtil.color(" &x&5&A&D&9&F&BСоздать личный титул"));
      List<String> createLore = new ArrayList<>();
      createLore.add(HexUtil.color(""));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f После нажатия на эту кнопку, меню закроется,"));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l&n▍&f вам в чат будет необходимо ввести желаемый титул."));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l▍&f Пожалуйста, внимательно ознакомьтесь с инструкцией"));
      createLore.add(HexUtil.color(" &x&5&A&D&9&F&B&l▍&f и правилами перед созданием титула."));
      createLore.add(HexUtil.color(""));
      createMeta.setLore(createLore);
      createItem.setItemMeta(createMeta);
      menu.setItem(31, createItem);
      ItemStack backItem = new ItemStack(Material.ARROW);
      ItemMeta backMeta = backItem.getItemMeta();
      backMeta.setDisplayName(HexUtil.color("&x&5&A&D&9&F&B◀ &fНазад"));
      backItem.setItemMeta(backMeta);
      menu.setItem(53, backItem);
   }

   public void setPlayerTitle(Player player, String title) {
      this.playerTitles.put(player.getUniqueId(), title);
   }

   public String getPlayerTitle(Player player) {
      return this.playerTitles.getOrDefault(player.getUniqueId(), player.getName());
   }

   public void clearPlayerData(Player player) {
      this.playerTitles.remove(player.getUniqueId());
   }
}
