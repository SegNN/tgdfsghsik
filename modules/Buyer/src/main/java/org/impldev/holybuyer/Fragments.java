package org.impldev.holybuyer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Боевые фрагменты - предмет из другого плагина, поэтому узнаём его по виду.
 *
 * <p>Сверяем материал и очищенное от цветов название: фрагмент раскрашен градиентом, и коды
 * цвета в нём могут поменяться при любой правке чужого конфига, а само слово - нет.
 */
public final class Fragments {
   private final Material material;
   private final String name;

   public Fragments(JavaPlugin plugin) {
      Material configured = Material.matchMaterial(
            plugin.getConfig().getString("fragments.material", "PRISMARINE_SHARD"));
      this.material = configured == null ? Material.PRISMARINE_SHARD : configured;
      this.name = plugin.getConfig().getString("fragments.name-contains", "Боевой фрагмент").toLowerCase();
   }

   public boolean is(ItemStack stack) {
      if (stack == null || stack.getType() != this.material || !stack.hasItemMeta()) {
         return false;
      }

      String display = stack.getItemMeta().getDisplayName();
      return ChatColor.stripColor(display).toLowerCase().contains(this.name);
   }

   public int count(Player player) {
      int total = 0;
      for (ItemStack stack : player.getInventory().getStorageContents()) {
         if (this.is(stack)) {
            total += stack.getAmount();
         }
      }

      return total;
   }

   /** Забирает нужное количество; если не хватает, не забирает ничего. */
   public boolean take(Player player, int amount) {
      if (this.count(player) < amount) {
         return false;
      }

      int left = amount;
      ItemStack[] contents = player.getInventory().getStorageContents();
      for (int i = 0; i < contents.length && left > 0; i++) {
         ItemStack stack = contents[i];
         if (!this.is(stack)) {
            continue;
         }

         int take = Math.min(left, stack.getAmount());
         left -= take;
         if (stack.getAmount() == take) {
            contents[i] = null;
         } else {
            stack.setAmount(stack.getAmount() - take);
         }
      }

      player.getInventory().setStorageContents(contents);
      return true;
   }
}
