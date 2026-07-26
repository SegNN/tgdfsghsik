package stickdev.siynes.stickhwcustom.listeners.colorname;

import java.awt.Color;
import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.ColorNameMenuHolder;

public class ColorNameListener implements Listener {
   public ColorNameListener() {
   }

   @EventHandler
   public void onSoundMenuClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player player = (Player)e.getWhoClicked();
         if (e.getClickedInventory() != null
            && e.getClickedInventory().getHolder() != null
            && e.getClickedInventory().getHolder() instanceof ColorNameMenuHolder) {
            e.setCancelled(true);
            this.inventoryClick(player, e.getCurrentItem());
         }
      }
   }

   private void inventoryClick(Player player, ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         NamespacedKey colorKey = new NamespacedKey(StickHWCustom.getInstance(), "color");
         if (itemStack.getItemMeta().getPersistentDataContainer().has(colorKey, PersistentDataType.STRING)) {
            String color = (String)itemStack.getItemMeta().getPersistentDataContainer().get(colorKey, PersistentDataType.STRING);
            player.setDisplayName(this.colorizeNickname(player.getName(), color, "#FFFFFF", true));
            StickHWCustom.getInstance();
            if (!StickHWCustom.playerData.containsKey(player)) {
               StickHWCustom.getInstance();
               StickHWCustom.playerData.put(player, new HashMap<>());
            }

            StickHWCustom.getInstance();
            StickHWCustom.playerData.get(player).put("color_name", color);
         }
      }
   }

   @EventHandler
   public void on(PlayerJoinEvent e) {
      final Player player = e.getPlayer();
      (new BukkitRunnable() {
         public void run() {
            StickHWCustom.getInstance();
            if (StickHWCustom.playerData.containsKey(player)) {
               StickHWCustom.getInstance();
               if (StickHWCustom.playerData.get(player).containsKey("color_name")) {
                  StickHWCustom.getInstance();
                  String color = StickHWCustom.playerData.get(player).get("color_name").toUpperCase();
                  player.setDisplayName(ColorNameListener.this.colorizeNickname(player.getName(), color, "#FFFFFF", true));
               }
            }
         }
      }).runTaskLater(StickHWCustom.getInstance(), 2L);
   }

   private String colorizeNickname(String playerName, String color1, String color2, Boolean bold) {
      int length = playerName.length();
      ChatColor startColor = ChatColor.of(color1);
      ChatColor endColor = ChatColor.of(color2);
      TextComponent coloredNickname = new TextComponent();

      for (int i = 0; i < length; i++) {
         double ratio = (double)i / (length - 1);
         int red = (int)(startColor.getColor().getRed() * (1.0 - ratio) + endColor.getColor().getRed() * ratio);
         int green = (int)(startColor.getColor().getGreen() * (1.0 - ratio) + endColor.getColor().getGreen() * ratio);
         int blue = (int)(startColor.getColor().getBlue() * (1.0 - ratio) + endColor.getColor().getBlue() * ratio);
         ChatColor color = ChatColor.of(new Color(red, green, blue));
         TextComponent letter = new TextComponent(String.valueOf(playerName.charAt(i)));
         letter.setColor(color);
         if (bold) {
            letter.setBold(true);
         }

         coloredNickname.addExtra(letter);
      }

      return TextComponent.toLegacyText(new BaseComponent[]{coloredNickname});
   }
}
