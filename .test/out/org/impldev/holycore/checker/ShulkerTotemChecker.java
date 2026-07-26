package org.impldev.holycore.checker;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.impldev.holycore.HolyCore;

public class ShulkerTotemChecker extends BukkitRunnable {
   private final HolyCore plugin;

   public ShulkerTotemChecker(HolyCore plugin) {
      this.plugin = plugin;
   }

   public void run() {
      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         this.checkShulker(player);
         this.checkTotems(player);
      }
   }

   public void checkShulker(Player player) {
      int shulkers = 0;
      PlayerInventory inventory = player.getInventory();

      for (int i = 0; i <= 40; i++) {
         ItemStack itemStack = inventory.getItem(i);
         if (itemStack != null && itemStack.getType().toString().endsWith("SHULKER_BOX")) {
            if (++shulkers > 1) {
               player.spigot()
                  .sendMessage(
                     ChatMessageType.ACTION_BAR,
                     TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&x&F&C&0&0&C&9Режим шалкера. &fЛивнешь - все шалкера выпадут."))
                  );
               return;
            }
         }
      }
   }

   public void checkTotems(Player player) {
      int totems = 0;
      PlayerInventory inventory = player.getInventory();

      for (int i = 0; i <= 40; i++) {
         ItemStack itemStack = inventory.getItem(i);
         if (itemStack != null && itemStack.getType() == Material.TOTEM_OF_UNDYING) {
            if (++totems > 4) {
               inventory.setItem(i, null);
               player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
               player.spigot()
                  .sendMessage(
                     ChatMessageType.ACTION_BAR,
                     TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes('&', "&c ▶ &fВы не можете носить с собой больше &6x4 Тотем бессмертия")
                     )
                  );
            }
         }
      }
   }
}
