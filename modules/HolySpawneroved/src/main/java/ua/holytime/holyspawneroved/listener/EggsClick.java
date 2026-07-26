package ua.holytime.holyspawneroved.listener;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.holytime.holyspawneroved.HolySpawneroved;
import ua.holytime.holyspawneroved.Utils.RandomEggs;
import ua.holytime.holyspawneroved.item.EggType;
import ua.holytime.holyspawneroved.item.Item;

public class EggsClick {
   private final HolySpawneroved plugin;
   private final Item items;
   private final RandomEggs randomEggs;
   private final Map<EggType, ItemStack> eggCache;
   private static final int[] SLOTS = new int[]{10, 12, 14, 16};
   private static final String[] MENU_KEYS = new String[]{"Ender-eggs", "eggs-1", "eggs-2", "eggs-3"};
   private static final EggType[] REQUIRED_EGGS = new EggType[]{EggType.END_SPLINTER_EGG, EggType.SHARD_EGG, EggType.SHARD_EGG, EggType.SHARD_EGG};

   public EggsClick(HolySpawneroved plugin) {
      this.plugin = plugin;
      this.items = new Item(plugin);
      this.randomEggs = new RandomEggs(plugin);
      this.eggCache = new HashMap<>();

      for (EggType type : EggType.values()) {
         this.eggCache.put(type, this.items.createEgg(type));
      }
   }

   public void handleEggClick(Player player, int slot) {
      ConfigurationSection itemsConfig = this.plugin.getConfig().getConfigurationSection("item");
      if (itemsConfig != null) {
         for (int i = 0; i < SLOTS.length; i++) {
            if (slot == SLOTS[i]) {
               this.processExchange(player, itemsConfig, MENU_KEYS[i], REQUIRED_EGGS[i]);
               break;
            }
         }
      }
   }

   private void processExchange(Player player, ConfigurationSection itemsConfig, String menuKey, EggType requiredEgg) {
      ConfigurationSection itemSection = itemsConfig.getConfigurationSection(menuKey);
      if (itemSection != null) {
         int price = itemSection.getInt("price");
         int playerEggs = this.getEggCount(player, requiredEgg);
         if (playerEggs >= price) {
            this.removeEggs(player, price, requiredEgg);
            if (itemSection.contains("change")) {
               double chance = itemSection.getDouble("change") / 100.0;
               if (Math.random() > chance) {
                  return;
               }
            }

            String category = itemSection.getString("category");
            this.randomEggs.executeRandomCommand(category, player);
         }
      }
   }

   private int getEggCount(Player player, EggType eggType) {
      int total = 0;
      ItemStack targetEgg = this.eggCache.get(eggType);

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && item.isSimilar(targetEgg)) {
            total += item.getAmount();
         }
      }

      return total;
   }

   private void removeEggs(Player player, int amount, EggType eggType) {
      ItemStack targetEgg = this.eggCache.get(eggType);
      int remaining = amount;
      ItemStack[] contents = player.getInventory().getContents();

      for (int i = 0; i < contents.length && remaining > 0; i++) {
         ItemStack item = contents[i];
         if (item != null && item.isSimilar(targetEgg)) {
            int itemAmount = item.getAmount();
            if (itemAmount > remaining) {
               item.setAmount(itemAmount - remaining);
               remaining = 0;
            } else {
               remaining -= itemAmount;
               player.getInventory().setItem(i, null);
            }
         }
      }
   }
}
