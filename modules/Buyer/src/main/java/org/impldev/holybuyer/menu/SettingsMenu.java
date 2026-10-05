package org.impldev.holybuyer.menu;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.HolyBuyer;
import org.impldev.holybuyer.Texts;

/** {@code /b settings} - четыре переключателя работы авто-скупщика. */
public final class SettingsMenu extends BuyerMenu {
   private static final int FAST = 20;
   private static final int HOTBAR = 22;
   private static final int HINTS = 24;
   private static final int MESSAGES = 31;
   private static final int BACK = 49;

   private final HolyBuyer plugin;
   private final BuyerData.Profile profile;

   public SettingsMenu(HolyBuyer plugin, Player player, BuyerData.Profile profile) {
      super(player, 54, "&rНастройки Скупщика");
      this.plugin = plugin;
      this.profile = profile;
   }

   @Override
   public void render() {
      this.inventory.clear();
      for (int slot = 0; slot < 54; slot++) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      this.inventory.setItem(FAST, this.toggle(Material.HOPPER, "Быстрая продажа", this.profile.fastSell,
            List.of("Продавать предметы до того, как", "они попадут к вам в инвентарь.")));
      this.inventory.setItem(HOTBAR, this.toggle(Material.SHIELD, "Игнорирование слотов", this.profile.ignoreHotbar,
            List.of("Не продавать предметы из хотбара", "и из руки.")));
      this.inventory.setItem(HINTS, this.toggle(Material.PAPER, "Режим подсказки цен", this.profile.priceHints,
            List.of("Дописывать в описание предмета", "его стоимость у Скупщика.")));
      this.inventory.setItem(MESSAGES, this.toggle(Material.BELL, "Сообщение при авто-продаже",
            this.profile.autoSellMessages,
            List.of("Писать в чат, что предметы", "проданы Скупщику.")));

      this.inventory.setItem(BACK, Menus.item(Material.ARROW, "&r" + Texts.RED + "◀ &r&fНазад"));
   }

   private ItemStack toggle(Material material, String title, boolean enabled, List<String> description) {
      String bar = "&r " + Texts.CYAN + "&l&n▍&r&f ";
      return Menus.item(material, "&r " + Texts.CYAN + title,
            "",
            bar + description.get(0),
            "&r " + Texts.CYAN + "&l▍&r&f " + description.get(1),
            "",
            enabled
                  ? "&r " + Texts.POINTS + "✔ &r&fВключено"
                  : "&r " + Texts.RED + "✘ &r&fВыключено",
            "",
            "&r " + Texts.CYAN + "▶ &r&fНажмите, чтобы переключить");
   }

   @Override
   public void click(InventoryClickEvent event) {
      event.setCancelled(true);
      switch (event.getRawSlot()) {
         case FAST -> this.profile.fastSell = !this.profile.fastSell;
         case HOTBAR -> this.profile.ignoreHotbar = !this.profile.ignoreHotbar;
         case HINTS -> this.profile.priceHints = !this.profile.priceHints;
         case MESSAGES -> this.profile.autoSellMessages = !this.profile.autoSellMessages;
         case BACK -> {
            this.plugin.menus().openMain(this.player);
            return;
         }
         default -> {
            return;
         }
      }

      this.profile.touch();
      this.plugin.data().savePlayer(this.profile);
      this.player.playSound(this.player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0F, 1.0F);
      this.render();
   }
}
