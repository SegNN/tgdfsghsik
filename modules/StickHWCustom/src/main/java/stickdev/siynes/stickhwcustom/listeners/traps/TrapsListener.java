package stickdev.siynes.stickhwcustom.listeners.traps;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.SkinMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwcustom.menus.skins.SkinsMenu;
import stickdev.siynes.stickhwpremium.PremiumAPI;

public class TrapsListener implements Listener {
   public TrapsListener() {
   }

   @EventHandler
   public void onSoundMenuClick(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof SkinMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getCurrentItem());
         if (e.getSlot() == 53) {
            MenuItems menuItems = new MenuItems();
            MainMenu mainMenu = new MainMenu(menuItems);
            mainMenu.open(player);
         }
      }
   }

   private void inventoryClick(Player player, ItemStack itemStack) {
      if (itemStack != null) {
         if (itemStack.getType() == Material.ARROW) {
            StickHWCustom.mainMenu.open(player);
         }

         if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(StickHWCustom.getInstance(), "stan-type"), PersistentDataType.STRING)) {
            if (((String)itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .get(new NamespacedKey(StickHWCustom.getInstance(), "stan-type"), PersistentDataType.STRING))
               .equalsIgnoreCase("true")) {
               if (!new PremiumAPI().checkPremium(player)) {
                  player.sendMessage("&c▶ &fДля этого скина нужен премиум");
                  return;
               }

               StickHWCustom.playerData
                  .get(player)
                  .put(
                     "stan_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(StickHWCustom.getInstance(), "stan-type"), PersistentDataType.STRING)
                  );
            } else {
               StickHWCustom.playerData
                  .get(player)
                  .put(
                     "stan_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(StickHWCustom.getInstance(), "stan-type"), PersistentDataType.STRING)
                  );
            }
         }

         if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(StickHWCustom.getInstance(), "trap-type"), PersistentDataType.STRING)) {
            if (((String)itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .get(new NamespacedKey(StickHWCustom.getInstance(), "trap-type"), PersistentDataType.STRING))
               .equalsIgnoreCase("true")) {
               if (!new PremiumAPI().checkPremium(player)) {
                  player.sendMessage("&c▶ &fДля этого скина нужен премиум");
                  return;
               }

               StickHWCustom.playerData
                  .get(player)
                  .put(
                     "trap_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(StickHWCustom.getInstance(), "trap-type"), PersistentDataType.STRING)
                  );
            } else {
               StickHWCustom.playerData
                  .get(player)
                  .put(
                     "trap_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(StickHWCustom.getInstance(), "trap-type"), PersistentDataType.STRING)
                  );
            }
         }

         StickHWCustom.getInstance();
         new SkinsMenu(StickHWCustom.menuItems).open(player);
      }
   }
}
