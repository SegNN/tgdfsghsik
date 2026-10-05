package org.impldev.holybuyer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Авто-скупщик: продаёт то, что игрок отметил в фильтре.
 *
 * <p>Обычный режим раз в несколько секунд разбирает инвентарь. Быстрая продажа перехватывает
 * предмет ещё на подборе - так он не занимает слот даже на мгновение, что и нужно на копалке.
 */
public final class AutoSell implements Listener {
   private static final int STORAGE = 36;
   private static final int HOTBAR = 9;

   private final HolyBuyer plugin;

   public AutoSell(HolyBuyer plugin) {
      this.plugin = plugin;
   }

   /** Обход всех, у кого включён авто-скупщик. */
   public void tick() {
      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         BuyerData.Profile profile = this.plugin.data().cached(player.getUniqueId());
         if (profile == null || profile.autoMode == BuyerData.AutoMode.OFF || profile.autoSell.isEmpty()) {
            continue;
         }

         Inventory inventory = player.getInventory();
         int from = profile.ignoreHotbar ? HOTBAR : 0;
         Sales.Result result = this.plugin.sales().sell(player, profile, inventory, from, STORAGE,
               item -> profile.autoSell.contains(item.id()));

         if (!result.empty() && profile.autoSellMessages) {
            this.report(player, result);
         }
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onPickup(EntityPickupItemEvent event) {
      if (!(event.getEntity() instanceof Player player)) {
         return;
      }

      BuyerData.Profile profile = this.plugin.data().cached(player.getUniqueId());
      if (profile == null || !profile.fastSell || profile.autoMode == BuyerData.AutoMode.OFF) {
         return;
      }

      ItemStack stack = event.getItem().getItemStack();
      BuyerItem item = this.plugin.catalogue().match(stack);
      if (item == null || !profile.autoSell.contains(item.id())) {
         return;
      }

      event.setCancelled(true);
      event.getItem().remove();

      Prices.Breakdown breakdown = this.plugin.prices().breakdown(player, profile, item);
      int amount = stack.getAmount();
      this.plugin.data().addSold(profile, item.id(), amount);
      this.plugin.data().addPoints(profile, item.category(), breakdown.points() * amount);
      this.plugin.deposit(player, breakdown.current() * amount);

      if (profile.autoSellMessages) {
         this.report(player, new Sales.Result(amount, breakdown.current() * amount, breakdown.points() * amount));
      }
   }

   /**
    * Подсказка цены при смене предмета в руке.
    *
    * <p>Показываем над хотбаром, а не дописываем в лор предмета: лор пришлось бы вшивать в сам
    * предмет, и он остался бы в нём после выключения настройки - в сундуках, в чужих руках и в
    * рецептах.
    */
   @EventHandler
   public void onHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      BuyerData.Profile profile = this.plugin.data().cached(player.getUniqueId());
      if (profile == null || !profile.priceHints) {
         return;
      }

      ItemStack stack = player.getInventory().getItem(event.getNewSlot());
      BuyerItem item = this.plugin.catalogue().match(stack);
      if (item == null) {
         return;
      }

      Prices.Breakdown breakdown = this.plugin.prices().breakdown(player, profile, item);
      player.sendActionBar(legacy("&r" + Texts.ORANGE + item.name() + "&r&f: " + Texts.COINS
            + Texts.num(breakdown.current()) + Texts.COINS + " монет ¤&r&f за штуку, "
            + Texts.COINS + Texts.num(breakdown.current() * stack.getAmount()) + Texts.COINS + " ¤&r&f за всё"));
   }

   private void report(Player player, Sales.Result result) {
      player.sendActionBar(legacy("&r" + Texts.ORANGE + "▶ &r&fПродано " + Texts.ORANGE + result.items()
            + "&r&f шт. за " + Texts.COINS + Texts.num(result.coins()) + Texts.COINS + " ¤&r&f и "
            + Texts.POINTS + Texts.dec1(result.points()) + Texts.POINTS + " очков"));
   }

   private static Component legacy(String text) {
      return LegacyComponentSerializer.legacySection().deserialize(Texts.color(text));
   }
}
