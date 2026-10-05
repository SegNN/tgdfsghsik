package org.impldev.holybuyer.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holybuyer.BuyerData;
import org.impldev.holybuyer.HolyBuyer;
import org.impldev.holybuyer.Shop;
import org.impldev.holybuyer.Texts;

/** Магазин скупщика: очки в обмен на предметы, бустеры и ульту. */
public final class ShopMenu extends BuyerMenu {
   private static final int[] GRAY = {0, 8, 45, 53};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35, 36, 44};
   private static final int[] LIGHT = {48, 50};
   private static final int BALANCE = 49;
   private static final int BACK = 52;

   private final HolyBuyer plugin;
   private final BuyerData.Profile profile;
   private final Map<Integer, Shop.Entry> shown = new HashMap<>();

   public ShopMenu(HolyBuyer plugin, Player player, BuyerData.Profile profile) {
      super(player, 54, "&rМагазин");
      this.plugin = plugin;
      this.profile = profile;
   }

   @Override
   public void render() {
      this.inventory.clear();
      this.shown.clear();

      for (int slot : GRAY) {
         this.inventory.setItem(slot, Menus.FILLER);
      }

      for (int slot : ORANGE) {
         this.inventory.setItem(slot, Menus.pane(Material.ORANGE_STAINED_GLASS_PANE));
      }

      for (int slot : LIGHT) {
         this.inventory.setItem(slot, Menus.pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
      }

      this.inventory.setItem(BACK, Menus.item(Material.ARROW, "&r" + Texts.RED + "◀ &r&fНазад"));
      this.inventory.setItem(BALANCE, this.balance());

      if (!this.plugin.shop().open()) {
         this.inventory.setItem(22, Menus.item(Material.BARRIER, "&r" + Texts.RED + "Магазин ещё закрыт",
               "",
               "&r " + Texts.CYAN + "&l&n▍&r&f Первые сутки после вайпа магазин",
               "&r " + Texts.CYAN + "&l▍&r&f скупщика не работает.",
               "",
               "&r " + Texts.ORANGE + "● &r&fОткроется через: " + Texts.COINS
                     + left(this.plugin.shop().opensIn())));
         return;
      }

      this.plugin.shop().showcase().forEach((slot, entry) -> {
         if (slot < 0 || slot >= 54) {
            return;
         }

         this.inventory.setItem(slot, this.render(entry));
         this.shown.put(slot, entry);
      });
   }

   private ItemStack render(Shop.Entry entry) {
      long price = this.plugin.shop().price(entry);
      List<String> lore = new ArrayList<>(entry.lore());
      lore.add("");
      lore.add(grantable(entry)
            ? "&r " + Texts.CYAN + "▶ &r&fКупить за " + Texts.POINTS + Texts.num(price)
                  + Texts.POINTS + " очков"
            : "&r " + Texts.RED + "✘ &r&fТовар пока недоступен");

      return Menus.item(entry.material(), entry.name(), lore);
   }

   /**
    * Может ли товар вообще что-то выдать.
    *
    * <p>Предметы из чужих плагинов выдаются командами из shop.yml. Пока команда не прописана,
    * покупка не должна проходить: иначе очки спишутся, а игрок не получит ничего.
    */
   private static boolean grantable(Shop.Entry entry) {
      return entry.action() != Shop.Action.NONE || entry.give() || !entry.commands().isEmpty();
   }

   private ItemStack balance() {
      String bar = "&r " + Texts.POINTS + "&l&n▍";
      return Menus.item(Material.EMERALD, "",
            bar,
            bar + "&r&f Ваш баланс: " + Texts.POINTS + Texts.num(this.profile.points) + Texts.POINTS + " очков",
            bar,
            bar + "&r&f Сдавайте предметы, чтобы",
            bar + "&r&f заработать очки Скупщика",
            "&r " + Texts.POINTS + "&l▍",
            "");
   }

   @Override
   public void click(InventoryClickEvent event) {
      event.setCancelled(true);
      int slot = event.getRawSlot();
      if (slot == BACK) {
         this.plugin.menus().openMain(this.player);
         return;
      }

      Shop.Entry entry = this.shown.get(slot);
      if (entry == null) {
         return;
      }

      if (!grantable(entry)) {
         this.player.sendMessage(Texts.color("&r " + Texts.RED + "✘ &r&fЭтот товар ещё не настроен: "
               + "в shop.yml у него не задана команда выдачи."));
         this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
         return;
      }

      long price = this.plugin.shop().price(entry);
      if (!this.plugin.data().takePoints(this.profile, price)) {
         this.player.sendMessage(Texts.color("&r " + Texts.RED + "✘ &r&fНе хватает очков скупщика: нужно "
               + Texts.POINTS + Texts.num(price) + "&r&f, у вас " + Texts.POINTS
               + Texts.num(this.profile.points) + "&r&f."));
         this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
         return;
      }

      this.grant(entry);
      this.plugin.shop().bought(entry);
      this.plugin.data().savePlayer(this.profile);
      this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      this.render();
   }

   /**
    * Выдача покупки.
    *
    * <p>Своё - опыт, бустер и ульту - выдаём напрямую. Всё остальное живёт в других плагинах,
    * поэтому идёт через список команд из shop.yml: жёсткая ссылка на чужой плагин уронила бы
    * магазин целиком, стоило бы этому плагину пропасть.
    */
   private void grant(Shop.Entry entry) {
      switch (entry.action()) {
         case EXP -> this.player.giveExp(entry.amount());
         case BUYER_BOOSTER -> {
            this.plugin.grantBooster(this.player, entry.percent(), entry.minutes());
            this.player.sendMessage(Texts.color("&r " + Texts.ORANGE + "▶ &r&fБустер Скупщика "
                  + Texts.COINS + "+" + entry.percent() + "%&r&f на " + Texts.COINS + entry.minutes()
                  + "&r&f мин. активирован."));
         }
         case ULTA -> {
            this.profile.ultaUntil = Math.max(this.profile.ultaUntil, System.currentTimeMillis())
                  + entry.minutes() * 60_000L;
            this.profile.touch();
            this.player.sendMessage(Texts.color("&r " + Texts.ORANGE + "▶ &r&fУльта Скупщика активна "
                  + Texts.COINS + entry.minutes() + "&r&f мин."));
         }
         default -> {
         }
      }

      if (entry.give()) {
         ItemStack stack = Menus.item(entry.material(), entry.name(), entry.lore());
         stack.setAmount(Math.max(1, entry.amount()));
         for (ItemStack leftover : this.player.getInventory().addItem(stack).values()) {
            this.player.getWorld().dropItemNaturally(this.player.getLocation(), leftover);
         }
      }

      for (String command : entry.commands()) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
               command.replace("%player%", this.player.getName()));
      }
   }

   private static String left(long millis) {
      long minutes = millis / 60_000L;
      return minutes / 60L + " ч. " + minutes % 60L + " мин.";
   }
}
