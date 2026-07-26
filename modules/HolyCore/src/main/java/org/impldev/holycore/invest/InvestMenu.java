package org.impldev.holycore.invest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.invest.Category.Kind;
import org.impldev.holycore.utils.Parser;

/** Меню инвестиций: показ состояния, подтверждение выбора и покупка очка. */
public final class InvestMenu implements Listener {
   private static final String TITLE = "&rМеню инвестиций";
   private static final int SIZE = 54;
   private static final int HELP_SLOT = 45;
   private static final int TOP_SLOT = 53;

   private final JavaPlugin plugin;
   private final InvestData data;
   private final InvestEffects effects;

   /** у кого какой слот сейчас ждёт подтверждения; одновременно он может быть только один */
   private final Map<UUID, Integer> pending = new HashMap<>();

   public InvestMenu(JavaPlugin plugin, InvestData data, InvestEffects effects) {
      this.plugin = plugin;
      this.data = data;
      this.effects = effects;
   }

   public void open(Player player) {
      Inventory inv = Bukkit.createInventory(null, SIZE, Parser.color(TITLE));
      this.pending.remove(player.getUniqueId());
      this.render(inv, player);
      player.openInventory(inv);
   }

   private void render(Inventory inv, Player player) {
      for (Category category : Categories.all().values()) {
         inv.setItem(category.slot(), this.icon(category));
      }

      inv.setItem(HELP_SLOT, item(Material.BOOK, "&r &r&x&0&0&d&8&f&fПомощь по инвестициям", List.of(
            "",
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Инвестиция даёт очки для прокачки",
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f глобальных бустеров, улучшения их эффекта",
            "&r &r&x&0&0&d&8&f&f&l▍&r&f и участия в рейтинге игроков за призы.")));

      inv.setItem(TOP_SLOT, this.topItem(player));
   }

   private ItemStack icon(Category category) {
      int level = this.data.level(category.id());
      int progress = this.data.progress(category.id());

      String name = "&r&x&f&f&a&2&0&0 " + category.title()
            + " &r&x&e&7&e&7&e&7[&r&x&0&5&f&b&0&0" + level
            + "&r&x&e&7&e&7&e&7/&r&x&0&5&f&b&0&0" + category.maxLevel() + " &r&fур.&r&x&e&7&e&7&e&7]";

      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.addAll(category.lore());
      lore.add("");

      switch (category.kind()) {
         case BONUS -> lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Сейчас: &r&x&0&5&f&b&0&0+"
               + trim(level * category.perLevel()) + "%");
         case TIME -> {
            lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f По умолчанию:&r&x&e&7&e&7&e&7 "
                  + time(category.baseValue()));
            lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Сейчас:&r&x&0&5&f&b&0&0 "
                  + time(this.effects.timeFor(category)) + " &r&x&e&7&e&7&e&7(&r&x&0&5&f&b&0&0-"
                  + trim(level * category.perLevel()) + "%&r&x&e&7&e&7&e&7)");
         }
         case BORDER -> {
            lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Текущие границы:");
            lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Адский мир - &r&x&0&5&f&b&0&0"
                  + this.effects.borderRadius("nether", level) + " (радиус)");
            lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Обычный мир - &r&x&0&5&f&b&0&0"
                  + this.effects.borderRadius("normal", level) + " (радиус)");
            lore.add("&r &r&x&f&f&a&2&0&0&l▍&r&f Эндер-мир - &r&x&0&5&f&b&0&0"
                  + this.effects.borderRadius("end", level) + " (радиус)");
         }
         case EXTERNAL -> {
         }
      }

      lore.add("&r &r&x&f&f&a&2&0&0&l&n▍&r&f Прогресс:&r&x&e&7&e&7&e&7 [&r&x&f&f&2&2&2&2"
            + progress + "&r&x&e&7&e&7&e&7/&r&x&0&5&f&b&0&0" + category.need() + "&r&x&e&7&e&7&e&7]");
      lore.add("&r &r&x&f&f&a&2&0&0&l▍&r&f Цена: &r&x&f&f&a&2&0&0" + category.price() + " ¤");
      lore.add("");
      lore.add("&r &r&x&f&f&a&2&0&0&l&n◢&r&f Нажмите, чтобы получить &r&x&0&5&f&b&0&01 Очко Инвестиций");
      lore.add("&r &r&x&f&f&a&2&0&0&l◤&r&f за &r&x&f&f&a&2&0&0" + category.price() + " ¤ &r&fсо своего баланса");

      return item(category.material(), name, lore);
   }

   private ItemStack confirmIcon() {
      return item(Material.LIME_STAINED_GLASS_PANE, "&r&x&0&0&d&8&f&f Вы уверены?", List.of(
            "",
            "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы подтвердить свой выбор"));
   }

   private ItemStack topItem(Player player) {
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add("&r&f Лучшие игроки по количеству &r&x&0&5&f&b&0&0Очков Инвестиций");
      lore.add("");

      List<InvestData.TopEntry> top = this.data.top(10);
      for (int i = 0; i < top.size(); i++) {
         InvestData.TopEntry entry = top.get(i);
         String place = "#" + (i + 1);
         lore.add("&r &r&3" + place + (place.length() < 3 ? "  " : " ") + "&r&8|  &r&x&0&0&d&8&f&f"
               + entry.name() + "  &r&x&0&5&f&b&0&0" + entry.points() + "  &r&fочков");
      }

      lore.add("");
      lore.add("&r&f Статистика обнуляется &r&x&0&0&d&8&f&fкаждый понедельник&r&f.");
      lore.add("");
      lore.add("&r &r&x&0&0&d&8&f&f&l• &r&fКоличество ваших очков: &r&x&0&5&f&b&0&0"
            + this.data.points(player.getUniqueId()));

      return item(Material.MAP, "&r &r&x&0&0&d&8&f&fТоп по очкам инвестиций &r&7(обновление - 5 мин.)", lore);
   }

   private static ItemStack item(Material material, String name, List<String> lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Parser.color(name));
         meta.setLore(lore.stream().map(Parser::color).toList());
         stack.setItemMeta(meta);
      }

      return stack;
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player player)) {
         return;
      }

      if (!event.getView().getTitle().equals(Parser.color(TITLE))) {
         return;
      }

      event.setCancelled(true);
      if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
         return;
      }

      int slot = event.getSlot();
      Category category = Categories.bySlot(slot);
      if (category == null) {
         return;
      }

      Inventory inv = event.getView().getTopInventory();
      Integer waiting = this.pending.get(player.getUniqueId());

      if (waiting == null || waiting != slot) {
         // подтверждение всегда одно: прежний слот возвращается к обычному виду
         if (waiting != null) {
            Category previous = Categories.bySlot(waiting);
            if (previous != null) {
               inv.setItem(waiting, this.icon(previous));
            }
         }

         this.pending.put(player.getUniqueId(), slot);
         inv.setItem(slot, this.confirmIcon());
         return;
      }

      this.pending.remove(player.getUniqueId());
      this.buy(player, category, inv);
   }

   private void buy(Player player, Category category, Inventory inv) {
      if (this.data.level(category.id()) >= category.maxLevel()) {
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶&r&f Это направление уже прокачано до конца."));
         inv.setItem(category.slot(), this.icon(category));
         return;
      }

      RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
      if (rsp == null) {
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶&r&f Экономика недоступна, попробуйте позже."));
         return;
      }

      Economy economy = rsp.getProvider();
      double price = category.price();
      if (economy.getBalance(player) < price) {
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶&r&f У Вас недостаточно денег для данной инвестиции."));
         inv.setItem(category.slot(), this.icon(category));
         return;
      }

      if (!economy.withdrawPlayer(player, price).transactionSuccess()) {
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶&r&f Не удалось списать средства."));
         return;
      }

      boolean levelUp = this.data.invest(player.getUniqueId(), player.getName(), category);
      if (levelUp) {
         this.effects.apply();
         this.announce(category);
      }

      player.sendMessage(Parser.color("&r&x&0&5&f&b&0&0▶&r&f Вы вложили &r&x&0&5&f&b&0&01 Очко Инвестиций&r&f в "
            + "&r&x&f&f&a&2&0&0" + category.title() + "&r&f."));

      // перерисовываем всё меню: прогресс и рейтинг меняются сразу
      this.render(inv, player);
   }

   /**
    * Объявление о новом уровне - в том же виде, в каком его показывал исходный сервер:
    * название направления, новый уровень и одной строкой то, что изменилось в игре.
    */
   private void announce(Category category) {
      int level = this.data.level(category.id());
      long percent = Math.round(level * category.perLevel());

      String what;
      String effect;
      switch (category.id()) {
         case "gunpowder" -> {
            what = "порох с криперов";
            effect = "Теперь криперы приносят &r&x&0&5&f&b&0&0на &r&x&0&5&f&b&0&0" + percent
                  + "&r&x&0&5&f&b&0&0% больше&r&f пороха";
         }
         case "experience" -> {
            what = "опыт с мобов";
            effect = "Теперь мобы приносят &r&x&0&5&f&b&0&0на &r&x&0&5&f&b&0&0" + percent
                  + "&r&x&0&5&f&b&0&0% больше&r&f опыта";
         }
         case "voting" -> {
            what = "ивенты";
            effect = "Теперь голосования проводятся &r&x&0&5&f&b&0&0чаще на &r&x&0&5&f&b&0&0" + percent
                  + "&r&x&0&5&f&b&0&0%";
         }
         case "automine" -> {
            what = "автошахты";
            effect = "Теперь автошахты обновляются &r&x&0&5&f&b&0&0быстрее на &r&x&0&5&f&b&0&0" + percent
                  + "&r&x&0&5&f&b&0&0%";
         }
         case "treasures" -> {
            what = "сокровища";
            effect = "Теперь сокровища появляются &r&x&0&5&f&b&0&0чаще на &r&x&0&5&f&b&0&0" + percent
                  + "&r&x&0&5&f&b&0&0%";
         }
         case "buyer" -> {
            what = "товар скупщика";
            effect = "Теперь скупщик платит &r&x&0&5&f&b&0&0на &r&x&0&5&f&b&0&0" + level
                  + "&r&x&0&5&f&b&0&0% больше";
         }
         case "borders" -> {
            what = "границы миров";
            effect = "Теперь границы миров &r&x&0&5&f&b&0&0шире на &r&x&0&5&f&b&0&0"
                  + Math.round(level * 10.0) + "&r&x&0&5&f&b&0&0 блоков";
         }
         default -> {
            what = category.title().toLowerCase();
            effect = "Эффект направления усилен";
         }
      }

      Bukkit.broadcastMessage("");
      Bukkit.broadcastMessage(Parser.color("&r&x&0&0&d&8&f&f&l&n▍&r&f Уровень инвестиций в " + what));
      Bukkit.broadcastMessage(Parser.color("&r&x&0&0&d&8&f&f&l&n▍&r&f был увеличен до &r&x&0&0&d&8&f&f"
            + level + "&r&x&0&0&d&8&f&f уровня&r&f!"));
      Bukkit.broadcastMessage(Parser.color("&r&x&0&0&d&8&f&f&l&n▍"));
      Bukkit.broadcastMessage(Parser.color("&r&x&0&0&d&8&f&f&l▍&r&f " + effect));
      Bukkit.broadcastMessage("");
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      this.pending.remove(event.getPlayer().getUniqueId());
   }

   private static String trim(double value) {
      return value == Math.rint(value) ? String.valueOf((long)value) : String.valueOf(value);
   }

   /** "6м., 42с." или "84м." - секунды показываются только когда они есть, как на исходном сервере. */
   private static String time(double seconds) {
      long total = Math.round(seconds);
      long minutes = total / 60;
      long rest = total % 60;
      return rest == 0 ? minutes + "м." : minutes + "м., " + rest + "с.";
   }
}
