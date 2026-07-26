package frozenpon.frozenpotionstacker.Utils;

import frozenpon.frozenpotionstacker.Main;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Потиковая слежка за зельями: инвентарь, эндер-сундук и КУРСОР.
 *
 * <p>Курсор здесь главное. Зелья пропадают именно при взятии мышкой, то есть в момент, когда
 * предмет уходит из слота на курсор. Без слежки за курсором это неотличимо от удаления: слот
 * пустеет в обоих случаях. Считая курсор частью инвентаря, мы видим разницу - предмет просто
 * переехал или его действительно уничтожили.
 *
 * <p>В момент реальной потери печатается всё, что может указать на виновника: полный дамп
 * предмета, состояние игрока и лента событий Bukkit за последние тики ({@link EventSpy}).
 * Если лента пуста - предмет убрали мимо событийной модели.
 *
 * <p>Включается `debug: true` в config.yml.
 */
public final class StackDebugger extends BukkitRunnable {
   private final Main plugin;
   private final EventSpy spy;
   /** игрок -> (слот -> описание); курсор лежит под ключом -1 */
   private final Map<String, Map<Integer, String>> last = new HashMap<>();
   /** игрок -> сколько всего зелий было в прошлом тике (с учётом курсора) */
   private final Map<String, Integer> lastTotal = new HashMap<>();
   /** игрок -> просадка, ожидающая подтверждения: {было, тик обнаружения} */
   private final Map<String, int[]> pending = new HashMap<>();

   /**
    * Сколько тиков ждать, прежде чем счесть просадку настоящей потерей.
    *
    * <p>Перекладывание предмета не атомарно: в креативе клиент шлёт изъятие и вставку
    * разными пакетами, да и обычный клик проходит через курсор. Счётчик при этом на
    * тик-другой проседает, хотя ничего не пропало. Без выдержки детектор кричит на
    * каждое перекладывание и его показаниям нельзя верить.
    */
   private static final int CONFIRM_TICKS = 5;

   private static final int CURSOR = -1;

   public StackDebugger(Main plugin, EventSpy spy) {
      this.plugin = plugin;
      this.spy = spy;
   }

   private static boolean isPotion(ItemStack item) {
      if (item == null) {
         return false;
      }

      String n = item.getType().name();
      return n.equals("POTION") || n.equals("SPLASH_POTION") || n.equals("LINGERING_POTION");
   }

   private static String describe(ItemStack item) {
      if (item == null || item.getType() == Material.AIR) {
         return "-";
      }

      ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
      String mss = meta != null && meta.hasMaxStackSize() ? String.valueOf(meta.getMaxStackSize()) : "НЕТ";
      return item.getType() + " amt=" + item.getAmount() + " mss=" + mss + " bukkitMax=" + item.getMaxStackSize();
   }

   /** Полный дамп - нужен, чтобы понять, чем этот предмет отличается от выживающих. */
   private static String dump(ItemStack item) {
      if (item == null) {
         return "-";
      }

      StringBuilder sb = new StringBuilder(describe(item));
      try {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            sb.append("\n      meta=").append(meta.getClass().getSimpleName());
            sb.append(" displayName=").append(meta.hasDisplayName() ? meta.getDisplayName() : "нет");
            sb.append(" customModelData=").append(meta.hasCustomModelData() ? meta.getCustomModelData() : "нет");
            sb.append("\n      asString=").append(meta.getAsString());
            if (!meta.getPersistentDataContainer().isEmpty()) {
               sb.append("\n      PDC=").append(meta.getPersistentDataContainer().getKeys());
            }
         }
      } catch (Throwable t) {
         sb.append(" [дамп меты упал: ").append(t).append("]");
      }

      return sb.toString();
   }

   private static String state(Player player) {
      return "мир=" + player.getWorld().getName()
            + " xyz=" + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()
            + " gm=" + player.getGameMode()
            + " hp=" + String.format("%.1f", player.getHealth())
            + " окно=" + player.getOpenInventory().getType()
            + " курсор=" + describe(player.getItemOnCursor());
   }

   @Override
   public void run() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         Map<Integer, String> prev = this.last.computeIfAbsent(player.getName(), k -> new HashMap<>());
         Map<Integer, String> now = new HashMap<>();
         int total = 0;

         ItemStack[] contents = player.getInventory().getContents();
         for (int slot = 0; slot < contents.length; slot++) {
            if (isPotion(contents[slot])) {
               now.put(slot, describe(contents[slot]));
               total += contents[slot].getAmount();
            }
         }

         // курсор - полноправная часть инвентаря, иначе взятие мышкой выглядит как пропажа
         ItemStack cursor = player.getItemOnCursor();
         if (isPotion(cursor)) {
            now.put(CURSOR, describe(cursor));
            total += cursor.getAmount();
         }

         Integer wasTotal = this.lastTotal.put(player.getName(), total);
         int[] wait = this.pending.get(player.getName());

         if (wait != null && total >= wait[0]) {
            // счётчик вернулся - предмет просто переезжал, тревога отменяется
            this.pending.remove(player.getName());
            wait = null;
         }

         if (wait == null && wasTotal != null && wasTotal > total) {
            this.pending.put(player.getName(), new int[]{wasTotal, Bukkit.getCurrentTick()});
         }

         boolean confirmed = false;
         if (wait != null && Bukkit.getCurrentTick() - wait[1] >= CONFIRM_TICKS) {
            confirmed = true;
            int lost = wait[0] - total;
            this.pending.remove(player.getName());
            this.plugin.getLogger().warning(
                  "\n================ ПОТЕРЯ ЗЕЛИЙ =================="
                  + "\n  тик " + Bukkit.getCurrentTick() + ", игрок " + player.getName()
                  + "\n  всего было " + wait[0] + " -> стало " + total + "  (уничтожено " + lost
                  + " шт., подтверждено спустя " + CONFIRM_TICKS + " тик.)"
                  + "\n  состояние: " + state(player)
                  + "\n  события Bukkit за последние тики:"
                  + "\n      " + this.spy.recent(player)
                  + "\n  исчезнувшие слоты:" + this.diff(prev, now)
                  + "\n================================================");
         }

         // подробный дамп того, что пропало, чтобы видеть отличия предмета
         for (Map.Entry<Integer, String> e : prev.entrySet()) {
            if (!now.containsKey(e.getKey()) && confirmed) {
               int slot = e.getKey();
               ItemStack still = slot == CURSOR ? player.getItemOnCursor()
                     : (slot < contents.length ? contents[slot] : null);
               this.plugin.getLogger().warning("  [слот " + (slot == CURSOR ? "КУРСОР" : slot) + "] было: " + e.getValue()
                     + "\n      сейчас там: " + dump(still));
            }
         }

         this.last.put(player.getName(), now);
      }
   }

   private String diff(Map<Integer, String> prev, Map<Integer, String> now) {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<Integer, String> e : prev.entrySet()) {
         String cur = now.get(e.getKey());
         if (cur == null) {
            sb.append("\n      ").append(e.getKey() == CURSOR ? "КУРСОР" : "слот " + e.getKey())
              .append(" ПУСТ, было: ").append(e.getValue());
         } else if (!cur.equals(e.getValue())) {
            sb.append("\n      ").append(e.getKey() == CURSOR ? "КУРСОР" : "слот " + e.getKey())
              .append(": ").append(e.getValue()).append("  ->  ").append(cur);
         }
      }

      return sb.length() == 0 ? " (слоты не менялись - предмет убыл иначе)" : sb.toString();
   }
}
