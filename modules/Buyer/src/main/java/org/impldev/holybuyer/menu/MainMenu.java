package org.impldev.holybuyer.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.HolyBuyer;
import org.impldev.holybuyer.Sales;
import org.impldev.holybuyer.Texts;

/**
 * Главное меню скупщика: сюда кладут предметы и подтверждают сделку.
 *
 * <p>Слоты 0-44 отданы игроку, нижняя строка - служебная.
 */
public final class MainMenu extends BuyerMenu {
   public static final int SELL_ZONE = 45;
   private static final int HELP = 45;
   private static final int AUTO = 46;
   private static final int SHOP = 49;
   private static final int CONFIRM = 53;

   private final HolyBuyer plugin;
   private final BuyerData.Profile profile;

   public MainMenu(HolyBuyer plugin, Player player, BuyerData.Profile profile) {
      super(player, 54, "&rПоложи предметы для продажи");
      this.plugin = plugin;
      this.profile = profile;
   }

   @Override
   public void render() {
      for (int slot = SELL_ZONE; slot < 54; slot++) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      this.inventory.setItem(HELP, this.help());
      this.inventory.setItem(AUTO, Menus.item(Material.NETHER_STAR,
            "&r " + Texts.ORANGE + "❏ Авто-скупщик ❏",
            "",
            "&r " + Texts.ORANGE + "&l&n▍" + Texts.ORANGE + " Автосдача предметов &r&fскупщику.",
            "&r " + Texts.ORANGE + "&l▍&r&f Установка фильтра на предметы.",
            "",
            "&r " + Texts.ORANGE + "▶ &r&fНажмите, чтобы открыть"));
      this.inventory.setItem(SHOP, Menus.item(Material.LODESTONE,
            "&r " + Texts.CYAN + "❏ Магазин скупщика ❏",
            "",
            "&r " + Texts.CYAN + "&l&n▍&r&f Раздел, где можно обменять Очки",
            "&r " + Texts.CYAN + "&l▍&r&f скупщика на предметы или бустеры.",
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы открыть"));

      this.renderConfirm();
   }

   private ItemStack help() {
      double coef = this.plugin.prices().baseCoef(this.player, this.profile);
      String bar = "&r " + Texts.CYAN + "&l&n▍";

      List<String> lore = new ArrayList<>(List.of(
            "&r " + Texts.ORANGE + "● &r&fБазовый коэффициент: " + Texts.ORANGE
                  + Texts.signed1(coef) + Texts.ORANGE + "%",
            "",
            bar,
            bar + "&r&f " + Texts.CYAN + "Как продать предмет Скупщику?",
            bar + "&r&f ",
            bar + "&r&f Перенесите желаемый предмет в это",
            bar + "&r&f меню и подтвердите сделку в правом",
            bar + "&r&f нижнем углу меню.",
            bar,
            bar + "&r&f " + Texts.CYAN + "Команды",
            bar + "&r&f ",
            bar + "&r&f " + Texts.CYAN + "/b auto &r&f– открыть авто-скупщик",
            bar + "&r&f " + Texts.CYAN + "/b items &r&f– статистика товаров",
            bar + "&r&f " + Texts.CYAN + "/b settings &r&f– настройки Скупщика",
            bar + "&r&f " + Texts.CYAN + "/b shop &r&f– магазин Скупщика",
            bar,
            bar + "&r&f Подробнее о системе Скупщика на",
            bar + "&r&f странице вики: " + Texts.CYAN + "wiki.ExampleWorld.su",
            "&r " + Texts.CYAN + "&l▍"));

      return Menus.item(Material.BOOK, "", lore);
   }

   /** Кнопка сделки: красная, пока класть нечего, и зелёная с суммой, когда есть что продать. */
   private void renderConfirm() {
      Sales.Result quote = this.plugin.sales().quote(this.player, this.profile, this.inventory, 0, SELL_ZONE, null);
      if (quote.empty()) {
         this.inventory.setItem(CONFIRM, Menus.item(Material.RED_STAINED_GLASS_PANE, "",
               "&r " + Texts.RED + "✘ &r&fВы пока ничего",
               "&r    " + Texts.RED + "не продаете &r&fскупщику.",
               ""));
         return;
      }

      this.inventory.setItem(CONFIRM, Menus.item(Material.LIME_STAINED_GLASS_PANE, "",
            "&r " + Texts.POINTS + "✔ &r&fК продаже: " + Texts.POINTS + Texts.num(quote.items()) + "&r&f шт.",
            "",
            "&r " + Texts.ORANGE + "&l&n▍&r&f Вы получите: " + Texts.COINS + Texts.num(quote.coins()) + Texts.COINS + " монет ¤",
            "&r " + Texts.ORANGE + "&l▍&r&f И ещё: " + Texts.POINTS + Texts.dec1(quote.points()) + Texts.POINTS + " очков",
            "",
            "&r " + Texts.ORANGE + "▶ &r&fНажмите, чтобы продать"));
   }

   @Override
   public void click(InventoryClickEvent event) {
      int slot = event.getRawSlot();
      if (slot >= SELL_ZONE && slot < 54) {
         event.setCancelled(true);
         switch (slot) {
            case AUTO -> this.plugin.menus().openAuto(this.player, null);
            case SHOP -> this.plugin.menus().openShop(this.player);
            case CONFIRM -> this.confirm();
            default -> {
            }
         }

         return;
      }

      // Всё, что кладут в верхнюю часть, меняет сумму сделки - кнопку надо перерисовать.
      // На следующий тик, потому что прямо сейчас предмет ещё не перемещён.
      this.plugin.getServer().getScheduler().runTask(this.plugin, this::renderConfirm);
   }

   private void confirm() {
      Sales.Result result = this.plugin.sales().sell(this.player, this.profile, this.inventory, 0, SELL_ZONE, null);
      if (result.empty()) {
         this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
         return;
      }

      this.player.sendMessage(Texts.color("&r " + Texts.ORANGE + "▶ &r&fПродано " + Texts.ORANGE
            + Texts.num(result.items()) + "&r&f предметов на " + Texts.COINS + Texts.num(result.coins())
            + Texts.COINS + " монет ¤&r&f и " + Texts.POINTS + Texts.dec1(result.points())
            + Texts.POINTS + " очков&r&f."));
      this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      this.render();
   }

   /** Всё, что скупщик не принял, возвращается игроку, а не пропадает вместе с меню. */
   @Override
   public void close(InventoryCloseEvent event) {
      for (int slot = 0; slot < SELL_ZONE; slot++) {
         ItemStack stack = this.inventory.getItem(slot);
         if (stack == null || stack.getType() == Material.AIR) {
            continue;
         }

         this.inventory.setItem(slot, null);
         for (ItemStack leftover : this.player.getInventory().addItem(stack).values()) {
            this.player.getWorld().dropItemNaturally(this.player.getLocation(), leftover);
         }
      }
   }
}
