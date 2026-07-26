package ru.holydev.holycustom.listeners.traps;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.SkinMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holycustom.menus.skins.SkinsMenu;
import ru.holydev.holypremium.PremiumAPI;

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
            HolyCustom.mainMenu.open(player);
         }

         if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(HolyCustom.getInstance(), "stan-type"), PersistentDataType.STRING)) {
            if (((String)itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .get(new NamespacedKey(HolyCustom.getInstance(), "stan-type"), PersistentDataType.STRING))
               .equalsIgnoreCase("true")) {
               if (!new PremiumAPI().checkPremium(player)) {
                  player.sendMessage("&c▶ &fДля этого скина нужен премиум");
                  return;
               }

               HolyCustom.playerData
                  .get(player)
                  .put(
                     "stan_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(HolyCustom.getInstance(), "stan-type"), PersistentDataType.STRING)
                  );
            } else {
               HolyCustom.playerData
                  .get(player)
                  .put(
                     "stan_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(HolyCustom.getInstance(), "stan-type"), PersistentDataType.STRING)
                  );
            }
         }

         if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(HolyCustom.getInstance(), "trap-type"), PersistentDataType.STRING)) {
            if (((String)itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .get(new NamespacedKey(HolyCustom.getInstance(), "trap-type"), PersistentDataType.STRING))
               .equalsIgnoreCase("true")) {
               if (!new PremiumAPI().checkPremium(player)) {
                  player.sendMessage("&c▶ &fДля этого скина нужен премиум");
                  return;
               }

               HolyCustom.playerData
                  .get(player)
                  .put(
                     "trap_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(HolyCustom.getInstance(), "trap-type"), PersistentDataType.STRING)
                  );
            } else {
               HolyCustom.playerData
                  .get(player)
                  .put(
                     "trap_skin",
                     (String)itemStack.getItemMeta()
                        .getPersistentDataContainer()
                        .get(new NamespacedKey(HolyCustom.getInstance(), "trap-type"), PersistentDataType.STRING)
                  );
            }
         }

         HolyCustom.getInstance();
         new SkinsMenu(HolyCustom.menuItems).open(player);
      }
   }
}
