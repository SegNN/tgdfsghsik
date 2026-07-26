package ru.extas7y.saveitems.object;

import java.util.ArrayList;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.database.MyDatabase;

public class SaveItemsObject implements Listener {
   private SaveItems plugin;
   private MyDatabase dbHandler;

   public SaveItemsObject(SaveItems plugin, MyDatabase dbHandler) {
      this.plugin = plugin;
      this.dbHandler = dbHandler;
   }

   private ItemStack getConfirm(String command, double price, String itemId) {
      ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&5&F&B&0&0 ▶ &x&D&5&D&B&D&CКупить"));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "confirm");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "itemId"), PersistentDataType.STRING, itemId);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "command"), PersistentDataType.STRING, command);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "price"), PersistentDataType.DOUBLE, price);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack getCancel(String command) {
      ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&2&2&2&2 ▶ &x&D&5&D&B&D&CОтменить покупку"));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "cancel");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "command"), PersistentDataType.STRING, command);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack getOther() {
      ItemStack itemStack = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " "));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "other");
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public void display(Player player, ItemStack itemStack, String item, double price, String command) {
      Inventory inventory = Bukkit.createInventory(
         player,
         27,
         ChatColor.translateAlternateColorCodes(
            '&',
            "Покупка предмета                                                                                                                                                                                                                                                   [saveditems:buy]"
         )
      );
      int[] other = new int[]{3, 4, 5, 12, 14, 21, 22, 23};
      int[] confirm = new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20};
      int[] cancel = new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26};

      for (int i : other) {
         inventory.setItem(i, this.getOther());
      }

      for (int i : confirm) {
         inventory.setItem(i, this.getConfirm(command, price, item));
      }

      for (int i : cancel) {
         inventory.setItem(i, this.getCancel(command));
      }

      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (itemMeta.getLore() != null) {
         lore = itemMeta.getLore();
      }

      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&0&8&0&8 &n▍&f Вы уверены что хотите &x&F&B&0&8&0&8подтвердить &fпокупку предмета?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&0&8&0&8 ▍&f Цена: &x&F&B&0&8&0&8" + price + " сапфиров ☀"));
      lore.add("");
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "item");
      itemStack.setItemMeta(itemMeta);
      inventory.setItem(13, itemStack);
      player.openInventory(inventory);
   }

   @EventHandler
   public void click(InventoryClickEvent e) {
      if (e.getView()
         .getTitle()
         .equals(
            ChatColor.translateAlternateColorCodes(
               '&',
               "Покупка предмета                                                                                                                                                                                                                                                   [saveditems:buy]"
            )
         )) {
         e.setCancelled(true);
         Player player = (Player)e.getWhoClicked();
         if (((String)e.getCurrentItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "none"))
            .equals("cancel")) {
            Bukkit.dispatchCommand(
               Bukkit.getConsoleSender(),
               PlaceholderAPI.setPlaceholders(
                  player,
                  (String)e.getCurrentItem()
                     .getItemMeta()
                     .getPersistentDataContainer()
                     .getOrDefault(new NamespacedKey(this.plugin, "command"), PersistentDataType.STRING, "am open shop %player_name%")
               )
            );
         } else if (((String)e.getCurrentItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this.plugin, "buyitem"), PersistentDataType.STRING, "none"))
            .equals("confirm")) {
            if (Double.parseDouble(PlaceholderAPI.setPlaceholders(player, "%playerpoints_points%"))
               >= (Double)e.getCurrentItem()
                  .getItemMeta()
                  .getPersistentDataContainer()
                  .getOrDefault(new NamespacedKey(this.plugin, "price"), PersistentDataType.DOUBLE, 0.0)) {
               player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(),
                  "p take "
                     + player.getName()
                     + " "
                     + ((Double)e.getCurrentItem()
                           .getItemMeta()
                           .getPersistentDataContainer()
                           .getOrDefault(new NamespacedKey(this.plugin, "price"), PersistentDataType.DOUBLE, 0.0))
                        .intValue()
               );
               this.plugin
                  .addItemInventory(
                     player.getInventory(),
                     this.dbHandler
                        .getItem(
                           (String)e.getCurrentItem()
                              .getItemMeta()
                              .getPersistentDataContainer()
                              .getOrDefault(new NamespacedKey(this.plugin, "itemId"), PersistentDataType.STRING, "default")
                        ),
                     player.getLocation()
                  );
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(),
                  PlaceholderAPI.setPlaceholders(
                     player,
                     (String)e.getCurrentItem()
                        .getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(this.plugin, "command"), PersistentDataType.STRING, "am open shop %player_name%")
                  )
               );
            } else {
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&6▶ &fУ вас &6недостаточно &fсапфиров&f. Приобрести сапфиры можно на сайте - &x&0&0&D&9&F&FExampleWorld.su"
                  )
               );
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(),
                  PlaceholderAPI.setPlaceholders(
                     player,
                     (String)e.getCurrentItem()
                        .getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(this.plugin, "command"), PersistentDataType.STRING, "am open shop %player_name%")
                  )
               );
            }
         }
      }
   }
}
