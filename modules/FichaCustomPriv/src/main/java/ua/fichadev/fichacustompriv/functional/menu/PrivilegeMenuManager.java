package ua.fichadev.fichacustompriv.functional.menu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class PrivilegeMenuManager {
   private FichaCustomPriv plugin;
   private HashMap<UUID, String> playerColors;
   private HashMap<UUID, String> playerPrefixes;

   public PrivilegeMenuManager(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.playerColors = new HashMap<>();
      this.playerPrefixes = new HashMap<>();
   }

   public void openMainMenu(Player player) {
      Inventory menu = Bukkit.createInventory((InventoryHolder)null, 54, HexUtil.color("Кастомные привилегии"));
      this.fillMainMenu(menu, player);
      player.openInventory(menu);
   }

   private void fillMainMenu(Inventory menu, Player player) {
      this.addPlayerPrivileges(menu, player);
      this.addStaticItems(menu, player);
      this.addDecoration(menu);
   }

   private void addPlayerPrivileges(Inventory menu, Player player) {
      List<Map<String, String>> playerPrivileges = this.getPlayerPrivileges(player.getUniqueId().toString());
      int slot = 10;

      for (Map<String, String> privilege : playerPrivileges) {
         if (slot > 43) {
            break;
         }

         ItemStack privilegeItem = this.createPrivilegeItem(privilege);
         menu.setItem(slot, privilegeItem);
         if ((++slot - 11) % 9 == 0) {
            slot += 2;
         }
      }
   }

   private List<Map<String, String>> getPlayerPrivileges(String playerUUID) {
      List<Map<String, String>> allPrivileges = new ArrayList<>();

      for (Map<String, String> priv : this.plugin.getPrivilegeManager().getPendingPrivileges()) {
         if (playerUUID.equals(priv.get("player_uuid"))) {
            Map<String, String> privilege = new HashMap<>(priv);
            privilege.put("status", "pending");
            allPrivileges.add(privilege);
         }
      }

      for (Map<String, String> privx : this.plugin.getPrivilegeManager().getApprovedPrivileges()) {
         if (playerUUID.equals(privx.get("player_uuid"))) {
            Map<String, String> privilege = new HashMap<>(privx);
            privilege.put("status", "approved");
            allPrivileges.add(privilege);
         }
      }

      allPrivileges.sort((p1, p2) -> {
         try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date1 = sdf.parse(p1.get("created_time"));
            Date date2 = sdf.parse(p2.get("created_time"));
            return date2.compareTo(date1);
         } catch (Exception var51) {
            return 0;
         }
      });
      return allPrivileges;
   }

   private ItemStack createPrivilegeItem(Map<String, String> privilege) {
      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      String privilegeName = privilege.get("privilege_text");
      String status = privilege.get("status");
      String createdTime = privilege.get("created_time");
      String color = privilege.get("privilege_color");
      String id = privilege.get("id");
      String formattedTime = this.formatTime(createdTime);
      List<String> lore = new ArrayList<>();
      meta.setDisplayName(HexUtil.color(color + privilegeName));
      lore.add(HexUtil.color("&6&n▍&f Кастомка: &e" + privilegeName));
      if ("approved".equals(status)) {
         lore.add(HexUtil.color("&6&n▍&f Статус: &aОдобрен"));
      } else {
         lore.add(HexUtil.color("&6&n▍&f Статус: &eНа рассмотрении"));
      }

      lore.add(HexUtil.color("&6&n▍&f Создан: &7" + formattedTime));
      lore.add("");
      if ("approved".equals(status)) {
         lore.add(HexUtil.color("&a▸ Нажмите чтобы выбрать эту привилегию"));
      } else {
         lore.add(HexUtil.color("&7▸ Ожидает одобрения администратора"));
      }

      lore.add(HexUtil.color("&7ID: " + id));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private String formatTime(String timeString) {
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
         Date date = sdf.parse(timeString);
         SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
         return outputFormat.format(date);
      } catch (Exception var51) {
         return timeString;
      }
   }

   private void addStaticItems(Inventory menu, Player player) {
      ItemStack infoItem = new ItemStack(Material.WRITABLE_BOOK);
      ItemMeta infoMeta = infoItem.getItemMeta();
      infoMeta.setDisplayName(HexUtil.color(" &#00d8ffИнформация для создания кастомного доната"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(HexUtil.color(""));
      infoLore.add(HexUtil.color(" &6&n▍&f  При нажатии на эту кнопку, в чате"));
      infoLore.add(HexUtil.color(" &6&n▍&f появится ссылка, где вы сможете ознакомиться"));
      infoLore.add(HexUtil.color(" &6&n▍&f с правилами и принципами создания доната и увидеть"));
      infoLore.add(HexUtil.color(" &6▍&f инструкцию по его созданию."));
      infoLore.add(HexUtil.color(""));
      infoLore.add(HexUtil.color(" &6&n▍&f  Ссылка кликабельна и откроет страничку в вашем"));
      infoLore.add(HexUtil.color(" &6&n▍&f бразуере. Также можнго скопировать ссылку"));
      infoLore.add(HexUtil.color(" &6&n▍&f и вставить её в нужный браузер вручную."));
      infoLore.add(HexUtil.color(" &6&n▍&f Не начинайте создание, пока не ознакомитесь с инструкцией,"));
      infoLore.add(HexUtil.color(" &6▍&f это может привести к утрате возможности создания."));
      infoMeta.setLore(infoLore);
      infoItem.setItemMeta(infoMeta);
      menu.setItem(47, infoItem);
      ItemStack createItem = new ItemStack(Material.PAPER);
      ItemMeta createMeta = createItem.getItemMeta();
      createMeta.setDisplayName(HexUtil.color(" &#00d8ffСоздать кастомную привилегию"));
      List<String> createLore = new ArrayList<>();
      createLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Нажмите, чтобы перейти в меню"));
      createLore.add(HexUtil.color(" &#5bdcfe&l▍&f создания кастомной привилегии"));
      createLore.add(HexUtil.color(" "));
      createLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Кол-во жетонов на 30 дн: &#00d8ff" + this.plugin.getTokenManager().getPrivilegeTokens30(player) + " шт."));
      createLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Кол-во жетонов на 90 дн: &#00d8ff" + this.plugin.getTokenManager().getPrivilegeTokens90(player) + " шт."));
      createLore.add(
         HexUtil.color(" &#5bdcfe&l▍&f Кол-во жетонов на навсегда: &#00d8ff" + this.plugin.getTokenManager().getPrivilegeTokensInfinity(player) + " шт.")
      );
      createLore.add(HexUtil.color("   &7&o*Первым используется жетон"));
      createLore.add(HexUtil.color("   &7&o     &7&oна меньший срок"));
      createMeta.setLore(createLore);
      createItem.setItemMeta(createMeta);
      menu.setItem(49, createItem);
      ItemStack generalInfo = new ItemStack(Material.BOOK);
      ItemMeta generalMeta = generalInfo.getItemMeta();
      generalMeta.setDisplayName(HexUtil.color(" &#00d8ffОбщая информация"));
      List<String> generalLore = new ArrayList<>();
      generalLore.add(HexUtil.color(""));
      generalLore.add(HexUtil.color(" &6&n▍&f  В этом меню можно ознакомиться со всей"));
      generalLore.add(HexUtil.color(" &6&n▍&f необходимой информацией для создания"));
      generalLore.add(HexUtil.color(" &6&n▍&f своего уникального кастомного доната, а"));
      generalLore.add(HexUtil.color(" &6▍&f также сделать его."));
      generalLore.add(HexUtil.color(""));
      generalLore.add(HexUtil.color(" &6&n▍&f  Количество исправлений ограничено, при"));
      generalLore.add(HexUtil.color(" &6&n▍&f создании кастомного доната это нужно учитывать."));
      generalLore.add(HexUtil.color(" &6&n▍&f Если намеренно пытаться нарушить правила или"));
      generalLore.add(HexUtil.color(" &6&n▍&f найти в них лазейки при создании, то"));
      generalLore.add(HexUtil.color(" &6&n▍&f можно так и не получить кастомный донат на"));
      generalLore.add(HexUtil.color(" &6▍&f свой аккаунт."));
      generalLore.add(HexUtil.color(""));
      generalMeta.setLore(generalLore);
      generalInfo.setItemMeta(generalMeta);
      menu.setItem(51, generalInfo);
   }

   private void addDecoration(Inventory menu) {
      ItemStack cyanPane = this.createGlassPane(Material.CYAN_STAINED_GLASS_PANE, "");
      int[] cyanSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 48, 50, 52, 53};

      for (int slot : cyanSlots) {
         menu.setItem(slot, cyanPane);
      }

      ItemStack orangePane = this.createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, "");
      int[] orangeSlots = new int[]{18, 26, 27, 35};

      for (int currentSlot : orangeSlots) {
         menu.setItem(currentSlot, orangePane);
      }
   }

   private ItemStack createGlassPane(Material material, String displayName) {
      ItemStack pane = new ItemStack(material);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(HexUtil.color(displayName));
      meta.addItemFlags(ItemFlag.values());
      pane.setItemMeta(meta);
      return pane;
   }

   public void openCreationMenu(Player player) {
      Inventory menu = Bukkit.createInventory((InventoryHolder)null, 54, HexUtil.color("Создание привилегии"));
      this.fillCreationMenu(menu, player);
      player.openInventory(menu);
   }

   private void fillCreationMenu(Inventory menu, Player player) {
      String currentColor = this.playerColors.getOrDefault(player.getUniqueId(), "&#ffffff");
      String currentPrefix = this.playerPrefixes.getOrDefault(player.getUniqueId(), player.getName());
      ItemStack colorItem = new ItemStack(Material.NAME_TAG);
      ItemMeta colorMeta = colorItem.getItemMeta();
      colorMeta.setDisplayName(HexUtil.color(" &#00d8ffНастройка цвета"));
      List<String> colorLore = new ArrayList<>();
      colorLore.add(HexUtil.color(""));
      colorLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Нажмите, чтобы настроить цвет"));
      colorLore.add(HexUtil.color(" &#5bdcfe&l▍&f чата кастомной привилегии"));
      colorLore.add(HexUtil.color(""));
      colorLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f На данный момент цвет выглядит:"));
      colorLore.add(HexUtil.color(" &#5bdcfe&l▍&f " + currentColor + "Навернуть бы китовую акулу."));
      colorLore.add(HexUtil.color(""));
      colorMeta.setLore(colorLore);
      colorItem.setItemMeta(colorMeta);
      menu.setItem(24, colorItem);
      ItemStack previewItem = new ItemStack(Material.PAPER);
      ItemMeta previewMeta = previewItem.getItemMeta();
      previewMeta.setDisplayName(HexUtil.color(" &#00ebffИтоговый вариант отображения:"));
      List<String> previewLore = new ArrayList<>();
      previewLore.add(HexUtil.color(""));
      previewLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Кастомка в чате:"));
      String playerName = player.getName();
      String fullPrivilege = HexUtil.color(currentPrefix + " ");
      previewLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f  &6ɢ&7 | " + fullPrivilege + " &f" + playerName + ": " + currentColor + "Навернуть бы китовую акулу."));
      previewLore.add(HexUtil.color(" &#5bdcfe&l&n▍"));
      previewLore.add(HexUtil.color(" &#5bdcfe&l▍&f Кастомка в табе: &f " + fullPrivilege));
      previewLore.add(HexUtil.color(""));
      previewMeta.setLore(previewLore);
      previewItem.setItemMeta(previewMeta);
      menu.setItem(11, previewItem);
      ItemStack textItem = new ItemStack(Material.ARMOR_STAND);
      ItemMeta textMeta = textItem.getItemMeta();
      textMeta.setDisplayName(HexUtil.color(" &#00d8ffНастройка текста"));
      List<String> textLore = new ArrayList<>();
      textLore.add(HexUtil.color(""));
      textLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f Нажмите, чтобы настроить текст"));
      textLore.add(HexUtil.color(" &#5bdcfe&l▍&f кастомной привилегии"));
      textLore.add(HexUtil.color(""));
      textLore.add(HexUtil.color(" &#5bdcfe&l&n▍&f На данный момент привилегия выглядит:"));
      textLore.add(HexUtil.color(" &#5bdcfe&l▍&f " + currentColor + currentPrefix + " "));
      textLore.add(HexUtil.color(""));
      textMeta.setLore(textLore);
      textItem.setItemMeta(textMeta);
      menu.setItem(22, textItem);
      ItemStack cancelItem = new ItemStack(Material.BARRIER);
      ItemMeta cancelMeta = cancelItem.getItemMeta();
      cancelMeta.setDisplayName(HexUtil.color(" &#ff2222✘ Отменить создание привилегии"));
      List<String> cancelLore = new ArrayList<>();
      cancelLore.add(HexUtil.color(""));
      cancelLore.add(HexUtil.color(" &#ff2222&l&n▍&f После нажатия создание привилегии"));
      cancelLore.add(HexUtil.color(" &#ff2222&l&n▍&f прекратится, при этом жетон"));
      cancelLore.add(HexUtil.color(" &#ff2222&l▍&f не будет потрачен."));
      cancelLore.add(HexUtil.color(""));
      cancelMeta.setLore(cancelLore);
      cancelItem.setItemMeta(cancelMeta);
      menu.setItem(19, cancelItem);
      ItemStack confirmItem = new ItemStack(Material.LIME_WOOL);
      ItemMeta confirmMeta = confirmItem.getItemMeta();
      confirmMeta.setDisplayName(HexUtil.color(" &#20e200✔ Подтвердить и создать кастомную привилегию"));
      List<String> confirmLore = new ArrayList<>();
      confirmLore.add(HexUtil.color(""));
      confirmLore.add(HexUtil.color(" &#20e200&l&n▍&f  Вы точно ознакомились со всеми правилами"));
      confirmLore.add(HexUtil.color(" &#20e200&l&n▍&f и готовы создавать привилегию. При выборе"));
      confirmLore.add(HexUtil.color(" &#20e200&l&n▍&f этого варианта меню закроется и Ваша"));
      confirmLore.add(HexUtil.color(" &#20e200&l&n▍&f привилегия будет отправлена на рассмотрение."));
      confirmLore.add(HexUtil.color(" &#20e200&l▍&f Будет списан &#ff22221 жетон&f на привилегию"));
      confirmLore.add(HexUtil.color(""));
      confirmMeta.setLore(confirmLore);
      confirmItem.setItemMeta(confirmMeta);
      menu.setItem(29, confirmItem);
      ItemStack orangePane = this.createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, "");
      int[] orangeSlots = new int[]{18, 26, 27, 35};

      for (int slot : orangeSlots) {
         menu.setItem(slot, orangePane);
      }

      ItemStack cyanPane = this.createGlassPane(Material.CYAN_STAINED_GLASS_PANE, "");
      int[] cyanSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

      for (int currentSlot : cyanSlots) {
         menu.setItem(currentSlot, cyanPane);
      }
   }

   public void setPlayerColor(Player player, String color) {
      this.playerColors.put(player.getUniqueId(), color);
   }

   public String getPlayerColor(Player player) {
      return this.playerColors.getOrDefault(player.getUniqueId(), "&f");
   }

   public void setPlayerPrefix(Player player, String prefix) {
      this.playerPrefixes.put(player.getUniqueId(), prefix);
   }

   public String getPlayerPrefix(Player player) {
      return this.playerPrefixes.getOrDefault(player.getUniqueId(), player.getName());
   }

   public void clearPlayerData(Player player) {
      UUID playerId = player.getUniqueId();
      this.playerColors.remove(playerId);
      this.playerPrefixes.remove(playerId);
   }
}
