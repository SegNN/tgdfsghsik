package org.impldev.holybuyer.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.HolyBuyer;

/** Открытие меню и разбор кликов по ним. */
public final class MenuService implements Listener {
   private final HolyBuyer plugin;

   public MenuService(HolyBuyer plugin) {
      this.plugin = plugin;
   }

   private BuyerData.Profile profile(Player player) {
      return this.plugin.data().profile(player.getUniqueId(), player.getName());
   }

   public void openMain(Player player) {
      this.open(new MainMenu(this.plugin, player, this.profile(player)));
   }

   public void openAuto(Player player, String category) {
      this.open(new AutoMenu(this.plugin, player, this.profile(player), category));
   }

   public void openShop(Player player) {
      this.open(new ShopMenu(this.plugin, player, this.profile(player)));
   }

   public void openItems(Player player) {
      this.open(new ItemsMenu(this.plugin, player, this.profile(player)));
   }

   public void openSettings(Player player) {
      this.open(new SettingsMenu(this.plugin, player, this.profile(player)));
   }

   /**
    * Открытие всегда откладывается на тик.
    *
    * <p>Меню открывают в основном кнопками из другого меню, то есть прямо внутри обработки
    * клика. Если подменить инвентарь в этот момент, клиент дорисует его по итогам ещё не
    * доигранного клика, и на экране остаются висеть призрачные предметы.
    */
   private void open(BuyerMenu menu) {
      this.plugin.getServer().getScheduler().runTask(this.plugin, menu::open);
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() instanceof BuyerMenu menu) {
         menu.click(event);
      }
   }

   /**
    * Протаскивание мышью по служебной строке запрещено.
    *
    * <p>Клик по ней уже отменяется, но перетаскивание - отдельное событие: без этой проверки
    * предмет можно было бы размазать по кнопкам и подменить их.
    */
   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      InventoryHolder holder = event.getInventory().getHolder();
      if (!(holder instanceof BuyerMenu)) {
         return;
      }

      int size = event.getInventory().getSize();
      boolean service = holder instanceof MainMenu
            ? event.getRawSlots().stream().anyMatch(slot -> slot >= MainMenu.SELL_ZONE && slot < size)
            : event.getRawSlots().stream().anyMatch(slot -> slot < size);

      if (service) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder() instanceof BuyerMenu menu) {
         menu.close(event);
      }
   }
}
