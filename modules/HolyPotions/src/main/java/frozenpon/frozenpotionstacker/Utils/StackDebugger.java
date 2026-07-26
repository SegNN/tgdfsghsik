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
 * Печатает состояние каждого слота с зельем раз в секунду и кричит, как только оно изменилось.
 * Нужен, чтобы поймать МОМЕНТ пропажи стака: видно, обрезали его (amt упал) или вынесли целиком
 * (слот опустел), и был ли на предмете компонент max_stack_size в этот момент.
 *
 * Включается `debug: true` в config.yml, по умолчанию выключен.
 */
public final class StackDebugger extends BukkitRunnable {
   private final Main plugin;
   /** player -> (slot -> "amount/maxStackSize") */
   private final Map<String, Map<Integer, String>> last = new HashMap<>();

   public StackDebugger(Main plugin) {
      this.plugin = plugin;
   }

   private static String describe(ItemStack item) {
      if (item == null || item.getType() == Material.AIR) {
         return "-";
      }

      ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
      String mss = meta != null && meta.hasMaxStackSize() ? String.valueOf(meta.getMaxStackSize()) : "НЕТ";
      return item.getType() + " amt=" + item.getAmount() + " mss=" + mss + " bukkitMax=" + item.getMaxStackSize();
   }

   private static boolean isPotion(ItemStack item) {
      if (item == null) {
         return false;
      }

      String n = item.getType().name();
      return n.equals("POTION") || n.equals("SPLASH_POTION") || n.equals("LINGERING_POTION");
   }

   /** player -> сколько всего зелий (штук) было в прошлый тик */
   private final Map<String, Integer> lastTotal = new HashMap<>();

   @Override
   public void run() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         Map<Integer, String> prev = this.last.computeIfAbsent(player.getName(), k -> new HashMap<>());
         Map<Integer, String> now = new HashMap<>();
         ItemStack[] contents = player.getInventory().getContents();

         int total = 0;
         for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (isPotion(item)) {
               now.put(slot, describe(item));
               total += item.getAmount();
            }
         }

         // ключевой вопрос: зелья реально ИСЧЕЗЛИ или просто слились в другой слот?
         Integer wasTotal = this.lastTotal.put(player.getName(), total);
         if (wasTotal != null && wasTotal != total) {
            String verdict = total < wasTotal ? "ПОТЕРЯ " + (wasTotal - total) + " шт." : "прибавка";
            this.plugin.getLogger().warning("[DBG] " + player.getName() + " ВСЕГО зелий: " + wasTotal + " -> " + total + "  (" + verdict + ")");
         }

         // что изменилось со прошлой секунды
         for (Map.Entry<Integer, String> e : now.entrySet()) {
            String was = prev.get(e.getKey());
            if (was == null) {
               this.plugin.getLogger().info("[DBG] " + player.getName() + " слот " + e.getKey() + " ПОЯВИЛОСЬ: " + e.getValue());
            } else if (!was.equals(e.getValue())) {
               this.plugin.getLogger().warning("[DBG] " + player.getName() + " слот " + e.getKey() + " ИЗМЕНИЛОСЬ: " + was + "  ->  " + e.getValue());
            }
         }
         for (Map.Entry<Integer, String> e : prev.entrySet()) {
            if (!now.containsKey(e.getKey())) {
               this.plugin.getLogger().warning("[DBG] " + player.getName() + " слот " + e.getKey() + " ПРОПАЛО: было " + e.getValue());
            }
         }

         this.last.put(player.getName(), now);
      }
   }
}
