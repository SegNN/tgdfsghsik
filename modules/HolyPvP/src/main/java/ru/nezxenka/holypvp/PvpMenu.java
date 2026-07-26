package ru.nezxenka.holypvp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PvpMenu {
   private final HolyPvP plugin;
   private final Player player;

   public PvpMenu(HolyPvP plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.open();
   }

   private void open() {
      Inventory inventory = Bukkit.createInventory(
         this.player,
         27,
         "Поиск PvP                                                                                                                                                                                                                                                                            [holypvp:pvp]"
      );
      ItemStack infoItem = new ItemStack(Material.NETHER_STAR);
      ItemMeta infoMeta = infoItem.getItemMeta();
      infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&CПолезная информация"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(
         ChatColor.translateAlternateColorCodes(
            '&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C Нажав на &x&2&E&E&6&0&0зеленую кнопку &x&F&C&F&C&F&Cили &x&F&6&0&F&D&0фиолетовую кнопку &x&F&C&F&C&F&Cвы"
         )
      );
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C активируете поиск оппонента,"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C▍&x&F&C&F&C&F&C оповестив об этом весь сервер"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&A&1&A&0&B красная кнопка &x&F&C&F&C&F&Cотменяет"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C▍&x&F&C&F&C&F&C статус поиска"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C Когда соперник найдется, вы будете"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C телепортированы к нему в радиусе"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C▍&x&F&C&F&C&F&C 30 блоков, о чем оповестят обоих игроков"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C Главная задача - победить своего"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C соперника. Наградой послужат все"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C&n▍&x&F&C&F&C&F&C ресурсы проигравшего и знаменательное"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&E&8&F&C▍&x&F&C&F&C&F&C поздравление в чате. Приятной битвы!\""));
      infoMeta.setLore(infoLore);
      infoMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "info");
      infoItem.setItemMeta(infoMeta);
      inventory.setItem(18, infoItem);
      boolean isSearching = false;

      try {
         isSearching = this.plugin.hasPlayerProfile(this.player.getName()).get();
      } catch (Exception var9) {
      }

      if (!isSearching) {
         ItemStack greenItem = new ItemStack(Material.LIME_CONCRETE);
         ItemMeta greenMeta = greenItem.getItemMeta();
         greenMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&5&F&8&0&0▶ Поиск поединка"));
         List<String> greenLore = new ArrayList<>();
         greenLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&5&F&8&0&0&n▍&x&F&C&F&C&F&C Нажмите, чтобы &x&0&5&F&8&0&0начать поиск"));
         greenLore.add(ChatColor.translateAlternateColorCodes('&', "&x&0&5&F&8&0&0▍&x&F&C&F&C&F&C противника для ПВП"));
         greenMeta.setLore(greenLore);
         greenMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "pvp");
         greenItem.setItemMeta(greenMeta);
         inventory.setItem(11, greenItem);
      }

      if (!isSearching) {
         ItemStack purpleItem = new ItemStack(Material.PURPLE_CONCRETE);
         ItemMeta purpleMeta = purpleItem.getItemMeta();
         purpleMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&8&0&F&D&1▶ Поиск смертельной схватки"));
         List<String> purpleLore = new ArrayList<>();
         purpleLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&8&0&F&D&1&n▍&x&F&C&F&C&F&C Нажмите, чтобы &x&F&8&0&F&D&1начать поиск"));
         purpleLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&8&0&F&D&1▍&x&F&C&F&C&F&C противника для ПВП"));
         purpleMeta.setLore(purpleLore);
         purpleMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "smert");
         purpleItem.setItemMeta(purpleMeta);
         inventory.setItem(15, purpleItem);
      }

      if (isSearching) {
         ItemStack redItem = new ItemStack(Material.RED_CONCRETE);
         ItemMeta redMeta = redItem.getItemMeta();
         redMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&C&2&2&2&2● Покинуть сражение"));
         List<String> redLore = new ArrayList<>();
         redLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&C&2&2&2&2&n▍&x&F&C&F&C&F&C Нажмите, чтобы &x&F&C&2&2&2&2отменить сражение"));
         redLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&C&2&2&2&2▍&x&F&C&F&C&F&C вам будет засчитано поражение"));
         redMeta.setLore(redLore);
         redMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "cancel");
         redItem.setItemMeta(redMeta);
         inventory.setItem(13, redItem);
      }

      this.player.openInventory(inventory);
   }
}
