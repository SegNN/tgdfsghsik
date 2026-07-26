package ua.fichadev.fichaexpbottle.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichaexpbottle.FichaExpBottle;
import ua.fichadev.fichaexpbottle.utils.ColorUtils;
import ua.fichadev.fichaexpbottle.utils.ExpUtils;

public class MenuListener implements Listener {
   private final HashMap<UUID, Long> cooldowns = new HashMap<>();
   private static final String MENU_TITLE = "Обменник опыта на пузырьки&r";

   public MenuListener() {
   }

   public static void openExpMenu(Player player) {
      String title = ColorUtils.colorize("Обменник опыта на пузырьки&r");
      int size = 36;
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, size, title);
      int playerExp = ExpUtils.getTotalExperience(player);
      int[] slots = new int[]{10, 12, 14, 16};
      int[] amounts = new int[]{1, 15, 30, 50};
      ItemStack vanillaBottle = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
      ItemMeta vMeta = vanillaBottle.getItemMeta();
      vMeta.setDisplayName(ColorUtils.colorize("&r&#00D5FCОбычный пузырек с опытом&r"));
      List<String> vLore = new ArrayList<>();
      vLore.add(ColorUtils.colorize("&r"));
      vLore.add(ColorUtils.colorize("&r&l&#ff7000&n▍&r&#e7e7e7 Для обмена требуется &r&#ffc90011 опыта&r"));
      vLore.add(ColorUtils.colorize("&r&l&#ff7000▍&r&#e7e7e7 У вас опыта: &r&#ffc900" + playerExp + "&r"));
      vLore.add(ColorUtils.colorize("&r"));
      vLore.add(ColorUtils.colorize("&r&#ff7000▶ &r&#e7e7e7Обменять &r&#ff700011 опыта&r&#e7e7e7 на обычный пузырек&r"));
      vLore.add(ColorUtils.colorize("&r"));
      vLore.add(ColorUtils.colorize("&r&#ff7000▶ &r&#ff7000 ШИФТ + ЛКМ: &r&#e7e7e7обменять весь опыт&r"));
      vLore.add(ColorUtils.colorize("&r"));
      vMeta.setLore(vLore);
      vanillaBottle.setItemMeta(vMeta);
      inv.setItem(slots[0], vanillaBottle);
      List<String> menuBottles = FichaExpBottle.getInstance().getPluginConfig().getStringList("menu_bottles");

      for (int i = 0; i < menuBottles.size(); i++) {
         int currentSlotIndex = i + 1;
         if (currentSlotIndex >= slots.length) {
            break;
         }

         String bottleKey = menuBottles.get(i);
         ConfigurationSection bottleConfig = FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles." + bottleKey);
         if (bottleConfig != null) {
            int requiredExp = bottleConfig.getInt("value");
            String bottleName = ColorUtils.colorize(bottleConfig.getString("name"));
            ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, amounts[currentSlotIndex]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(bottleName);
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&r"));
            lore.add(ColorUtils.colorize("&r&l&#ff7000&n▍&r&#e7e7e7 Для обмена требуется &r&#ffc900" + requiredExp + " опыта (" + bottleKey + " ур.)&r"));
            lore.add(ColorUtils.colorize("&r&l&#ff7000▍&r&#e7e7e7 У вас опыта: &r&#ffc900" + playerExp + "&r"));
            lore.add(ColorUtils.colorize("&r"));
            lore.add(
               ColorUtils.colorize(
                  "&r&#ff7000▶ &r&#e7e7e7Обменять &r&#ff7000" + requiredExp + " опыта&r&#e7e7e7 на бутылку с &r&#ff7000" + bottleKey + " ур. опыта&r"
               )
            );
            lore.add(ColorUtils.colorize("&r"));
            lore.add(ColorUtils.colorize("&r&#ff7000▶ &r&#ff7000 ШИФТ + ЛКМ: &r&#e7e7e7обменять весь опыт&r"));
            lore.add(ColorUtils.colorize("&r"));
            meta.setLore(lore);
            meta.setCustomModelData(requiredExp);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            item.setItemMeta(meta);
            inv.setItem(slots[currentSlotIndex], item);
         }
      }

      ItemStack infoItem = new ItemStack(Material.NETHERITE_SWORD);
      ItemMeta infoMeta = infoItem.getItemMeta();
      infoMeta.setDisplayName(ColorUtils.colorize(" &r&#00D5FCМой опыт&r"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(ColorUtils.colorize("&r"));
      infoLore.add(ColorUtils.colorize("&r &r&l&#00D5FC&n▍&r&#e7e7e7 Множитель опыта: &r&#ff7000x1.0&r"));
      infoLore.add(ColorUtils.colorize("&r &r&l&#00D5FC▍&r&#e7e7e7 Скорость добычи: &r&#00D5FC" + playerExp + " ед./ч&r"));
      infoLore.add(ColorUtils.colorize("&r"));
      infoLore.add(ColorUtils.colorize("&r &r&#00D5FCТоп по скорости добычи:&r"));
      infoLore.add(ColorUtils.colorize("&r"));
      List<Entry<String, Integer>> topPlayers = ExpUtils.getTopPlayers(3);

      for (int i = 0; i < 3; i++) {
         if (i < topPlayers.size()) {
            Entry<String, Integer> entry = topPlayers.get(i);
            infoLore.add(
               ColorUtils.colorize(
                  "&r &r&#00D5FC#" + (i + 1) + " &r&#aaaaaa| &r&#ff7000" + entry.getKey() + " &r&#ffffff" + entry.getValue() + " опыта в час&r"
               )
            );
         } else {
            infoLore.add(ColorUtils.colorize("&r &r&#00D5FC#" + (i + 1) + " &r&#aaaaaa| &r&#ff7000нет &r&#ffffff0.00 опыта в час&r"));
         }
      }

      infoMeta.setLore(infoLore);
      infoMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      infoItem.setItemMeta(infoMeta);
      inv.setItem(31, infoItem);
      player.openInventory(inv);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         String title = event.getView().getTitle();
         String configTitle = ColorUtils.colorize("Обменник опыта на пузырьки&r");
         if (title.equals(configTitle)) {
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
               UUID playerId = player.getUniqueId();
               long currentTime = System.currentTimeMillis();
               if (!this.cooldowns.containsKey(playerId) || currentTime - this.cooldowns.get(playerId) >= 0L) {
                  this.cooldowns.put(playerId, currentTime);
                  int slot = event.getSlot();
                  ClickType clickType = event.getClick();
                  int[] slots = new int[]{10, 12, 14, 16};
                  if (slot == slots[0]) {
                     this.handleVanillaExchange(player, clickType);
                     return;
                  }

                  List<String> menuBottles = FichaExpBottle.getInstance().getPluginConfig().getStringList("menu_bottles");

                  for (int i = 0; i < menuBottles.size(); i++) {
                     if (slot == slots[i + 1]) {
                        this.handleExpExchange(player, menuBottles.get(i), clickType);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private void handleVanillaExchange(Player player, ClickType clickType) {
      int requiredExp = 11;
      int playerExp = ExpUtils.getTotalExperience(player);
      if (playerExp < requiredExp) {
         player.sendMessage(ColorUtils.colorize("&4▶ &fУ Вас &4недостаточно опыта &fдля обмена"));
      } else if (!this.hasEmptyBottle(player)) {
         player.sendMessage(ColorUtils.colorize("&4▶ &fУ Вас нет &4пустого пузырька &fв инвентаре"));
      } else {
         if (clickType.isShiftClick()) {
            int bottleCount = Math.min(playerExp / requiredExp, this.countEmptyBottles(player));
            int totalExpUsed = bottleCount * requiredExp;
            this.removeEmptyBottles(player, bottleCount);
            ExpUtils.setTotalExperience(player, playerExp - totalExpUsed);
            this.giveItems(player, new ItemStack(Material.EXPERIENCE_BOTTLE, bottleCount));
         } else {
            this.removeEmptyBottles(player, 1);
            ExpUtils.setTotalExperience(player, playerExp - requiredExp);
            this.giveItems(player, new ItemStack(Material.EXPERIENCE_BOTTLE, 1));
         }

         player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
         openExpMenu(player);
      }
   }

   private void handleExpExchange(Player player, String bottleKey, ClickType clickType) {
      ConfigurationSection bottleConfig = FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles." + bottleKey);
      if (bottleConfig != null) {
         int requiredExp = bottleConfig.getInt("value");
         String bottleName = ColorUtils.colorize(bottleConfig.getString("name"));
         List<String> bottleLore = bottleConfig.getStringList("lore");
         int playerExp = ExpUtils.getTotalExperience(player);
         if (playerExp < requiredExp) {
            player.sendMessage(ColorUtils.colorize("&4▶ &fУ Вас &4недостаточно опыта &fдля обмена"));
         } else if (!this.hasEmptyBottle(player)) {
            player.sendMessage(ColorUtils.colorize("&4▶ &fУ Вас нет &4пустого пузырька &fв инвентаре"));
         } else {
            if (clickType.isShiftClick()) {
               int bottleCount = Math.min(playerExp / requiredExp, this.countEmptyBottles(player));
               int totalExpUsed = bottleCount * requiredExp;
               this.removeEmptyBottles(player, bottleCount);
               ExpUtils.setTotalExperience(player, playerExp - totalExpUsed);
               this.giveExpBottles(player, requiredExp, bottleName, bottleLore, bottleCount);
            } else {
               this.removeEmptyBottles(player, 1);
               ExpUtils.setTotalExperience(player, playerExp - requiredExp);
               this.giveExpBottles(player, requiredExp, bottleName, bottleLore, 1);
            }

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            openExpMenu(player);
         }
      }
   }

   private boolean hasEmptyBottle(Player player) {
      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && item.getType() == Material.GLASS_BOTTLE) {
            return true;
         }
      }

      return false;
   }

   private int countEmptyBottles(Player player) {
      int count = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && item.getType() == Material.GLASS_BOTTLE) {
            count += item.getAmount();
         }
      }

      return count;
   }

   private void removeEmptyBottles(Player player, int amount) {
      int remaining = amount;

      for (int i = 0; i < player.getInventory().getSize(); i++) {
         ItemStack item = player.getInventory().getItem(i);
         if (item != null && item.getType() == Material.GLASS_BOTTLE) {
            if (item.getAmount() <= remaining) {
               remaining -= item.getAmount();
               player.getInventory().setItem(i, null);
            } else {
               item.setAmount(item.getAmount() - remaining);
               remaining = 0;
            }

            if (remaining == 0) {
               break;
            }
         }
      }
   }

   private void giveItems(Player player, ItemStack item) {
      HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{item});

      for (ItemStack dropped : leftover.values()) {
         player.getWorld().dropItem(player.getLocation(), dropped);
      }
   }

   private void giveExpBottles(Player player, int expAmount, String name, List<String> lore, int count) {
      ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
      ItemMeta meta = bottle.getItemMeta();
      meta.setDisplayName(name);
      meta.setCustomModelData(expAmount);
      List<String> coloredLore = new ArrayList<>();

      for (String line : lore) {
         coloredLore.add(ColorUtils.colorize(line));
      }

      meta.setLore(coloredLore);
      bottle.setItemMeta(meta);

      while (count > 0) {
         int stackSize = Math.min(count, 64);
         ItemStack stack = bottle.clone();
         stack.setAmount(stackSize);
         this.giveItems(player, stack);
         count -= stackSize;
      }
   }
}
