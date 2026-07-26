package org.impldev.holycore.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.utils.Parser;

/** Меню бустеров: показ текущих, улучшение за сапфиры, сводный список. */
public final class BoosterMenu implements Listener {
   private static final String TITLE = "&rБустеры";
   private static final int SIZE = 45;
   private static final int LIST_SLOT = 37;
   private static final int BACK_SLOT = 43;
   /** на сколько продлевается бустер при покупке уровня */
   private static final long DURATION_MS = 48L * 60L * 60L * 1000L;

   private static final int[] GRAY = {0, 8, 36, 44};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35};

   private final JavaPlugin plugin;
   private final BoosterData data;

   public BoosterMenu(JavaPlugin plugin, BoosterData data) {
      this.plugin = plugin;
      this.data = data;
   }

   public void open(Player player) {
      Inventory inv = Bukkit.createInventory(null, SIZE, Parser.color(TITLE));
      this.render(inv, player);
      player.openInventory(inv);
   }

   private void render(Inventory inv, Player player) {
      for (int slot : GRAY) {
         inv.setItem(slot, pane(Material.GRAY_STAINED_GLASS_PANE));
      }
      for (int slot : ORANGE) {
         inv.setItem(slot, pane(Material.ORANGE_STAINED_GLASS_PANE));
      }

      for (Booster booster : Boosters.all().values()) {
         inv.setItem(booster.slot(), this.icon(booster, player));
      }

      inv.setItem(LIST_SLOT, this.listItem(player));
      inv.setItem(BACK_SLOT, item(Material.ARROW, "&r &r&x&f&f&2&2&2&2◀ &r&fНазад", List.of()));
   }

   private ItemStack icon(Booster booster, Player player) {
      UUID uuid = player.getUniqueId();
      int level = this.data.level(uuid, booster.id());
      int bonus = booster.bonus(level);
      int next = booster.bonus(level + 1);
      int price = booster.priceFor(level + 1);

      String name = "&r " + booster.color() + booster.title()
            + " &r&x&e&7&e&7&e&7(+&r&x&e&7&e&7&e&7" + bonus + "&r&x&e&7&e&7&e&7%)";

      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add("&r " + booster.accent() + "Эффект бустера:");
      lore.add("&r " + booster.accent() + "&l&n▍&r&x&e&7&e&7&e&7 " + booster.effect().get(0));
      lore.add("&r " + booster.accent() + "&l▍&r&x&e&7&e&7&e&7 " + booster.effect().get(1));
      lore.add("");
      lore.add("&r &r&x&f&f&7&0&0&0● &r&fСледующее улучшение:&r&x&0&5&f&b&0&0 +&r&x&0&5&f&b&0&0"
            + next + "&r&x&0&5&f&b&0&0%");
      lore.add("");
      lore.add("&r &r&x&f&f&7&0&0&0▶ &r&fНажмите, чтобы улучшить");
      lore.add("&r    &r&fбустер за &r&x&f&b&0&f&d&4" + price + "&r&x&f&b&0&f&d&4 сапфиров ☀");
      lore.add("");
      lore.add("&r    &r&fБудет активен ещё: &r&x&f&f&7&0&0&0" + time(this.data.remaining(uuid, booster.id())));

      return item(booster.material(), name, lore);
   }

   private ItemStack listItem(Player player) {
      UUID uuid = player.getUniqueId();
      List<String> lore = new ArrayList<>();
      lore.add("");

      for (Booster booster : Boosters.all().values()) {
         int level = this.data.level(uuid, booster.id());
         lore.add("&r  &r&7• " + booster.color() + shortName(booster) + " &r&f– " + level
               + " ур. &r&8| &r&fбонус: &r&x&0&5&f&b&0&0+" + booster.bonus(level) + "%");
      }

      lore.add("");
      lore.add("&r  &r&7• &r&fвсего потрачено: &r&x&f&b&0&f&d&4" + this.data.spent(uuid)
            + "&r&x&f&b&0&f&d&4 сапфиров ☀");
      lore.add("");

      return item(Material.FILLED_MAP, "&r &r&x&0&0&d&8&f&fСписок бустеров", lore);
   }

   private static String shortName(Booster booster) {
      return switch (booster.id()) {
         case "experience" -> "опыт";
         case "mobmoney" -> "монеты с мобов";
         case "buyer" -> "скупщик";
         case "crops" -> "культуры";
         case "mobdamage" -> "урон по мобам";
         default -> booster.title().toLowerCase();
      };
   }

   private static ItemStack pane(Material material) {
      return item(material, "&r ", List.of(""));
   }

   private static ItemStack item(Material material, String name, List<String> lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Parser.color(name));
         if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(Parser::color).toList());
         }

         stack.setItemMeta(meta);
      }

      return stack;
   }

   /** "43ч. 24м. 44с." - как на исходном сервере; ноли старших разрядов не печатаются. */
   private static String time(long ms) {
      if (ms <= 0L) {
         return "не активен";
      }

      long total = ms / 1000L;
      long hours = total / 3600L;
      long minutes = total % 3600L / 60L;
      long seconds = total % 60L;

      StringBuilder sb = new StringBuilder();
      if (hours > 0) {
         sb.append(hours).append("ч. ");
      }
      if (hours > 0 || minutes > 0) {
         sb.append(minutes).append("м. ");
      }

      return sb.append(seconds).append("с.").toString();
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player player)
            || !event.getView().getTitle().equals(Parser.color(TITLE))) {
         return;
      }

      event.setCancelled(true);
      if (event.getClickedInventory() != event.getView().getTopInventory()) {
         return;
      }

      if (event.getSlot() == BACK_SLOT) {
         player.closeInventory();
         return;
      }

      Booster booster = Boosters.bySlot(event.getSlot());
      if (booster == null) {
         return;
      }

      int level = this.data.level(player.getUniqueId(), booster.id());
      int price = booster.priceFor(level + 1);
      if (!Sapphires.take(player, price)) {
         player.sendMessage(Parser.color(
               "&r&x&f&f&2&2&2&2▶ &r&fУ вас &r&x&f&f&2&2&2&2недостаточно сапфиров&r&f для улучшения бустера."));
         return;
      }

      this.data.upgrade(player.getUniqueId(), booster.id(), DURATION_MS, price);
      this.render(event.getView().getTopInventory(), player);
      player.sendMessage(Parser.color("&r&x&0&5&f&b&0&0▶&r&f " + booster.color() + booster.title()
            + "&r&f улучшен до &r&x&0&5&f&b&0&0" + this.data.level(player.getUniqueId(), booster.id()) + " ур.&r&f"));
   }
}
