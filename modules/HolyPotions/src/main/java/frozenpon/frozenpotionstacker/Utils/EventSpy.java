package frozenpon.frozenpotionstacker.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Пишет в кольцевой буфер всё, что происходило с игроком через Bukkit API, с номером тика.
 *
 * <p>Смысл: когда зелье пропадает, надо знать, было ли в этом же тике хоть одно событие,
 * через которое предмет мог убрать плагин. Если буфер пуст - значит предмет убрали мимо
 * событийной модели (сам сервер или плагин, который лезет прямо в NMS), и искать надо
 * совсем в другом месте.
 */
public final class EventSpy implements Listener {
   private static final int KEEP = 12;

   /** игрок -> последние события, самое свежее первым */
   private final Map<String, Deque<String>> log = new HashMap<>();

   private void note(Player player, String what) {
      if (player == null) {
         return;
      }

      Deque<String> d = this.log.computeIfAbsent(player.getName(), k -> new ArrayDeque<>());
      d.addFirst("t" + Bukkit.getCurrentTick() + " " + what);
      while (d.size() > KEEP) {
         d.removeLast();
      }
   }

   /** Что случилось с игроком за последние тики - в одну строку. */
   public String recent(Player player) {
      Deque<String> d = this.log.get(player.getName());
      if (d == null || d.isEmpty()) {
         return "СОБЫТИЙ НЕ БЫЛО";
      }

      return String.join(" | ", d);
   }

   private static String item(org.bukkit.inventory.ItemStack i) {
      return i == null ? "-" : i.getType() + "x" + i.getAmount();
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
   public void onClick(InventoryClickEvent e) {
      this.note((Player) e.getWhoClicked(), "CLICK action=" + e.getAction() + " slot=" + e.getSlot()
            + " raw=" + e.getRawSlot() + " cur=" + item(e.getCurrentItem()) + " cursor=" + item(e.getCursor())
            + " inv=" + e.getInventory().getType() + " cancelled=" + e.isCancelled());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDrag(InventoryDragEvent e) {
      this.note((Player) e.getWhoClicked(), "DRAG slots=" + e.getRawSlots() + " cancelled=" + e.isCancelled());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDrop(PlayerDropItemEvent e) {
      this.note(e.getPlayer(), "DROP " + item(e.getItemDrop().getItemStack()) + " cancelled=" + e.isCancelled());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onConsume(PlayerItemConsumeEvent e) {
      this.note(e.getPlayer(), "CONSUME " + item(e.getItem()) + " cancelled=" + e.isCancelled());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPickup(EntityPickupItemEvent e) {
      if (e.getEntity() instanceof Player p) {
         this.note(p, "PICKUP " + item(e.getItem().getItemStack()) + " cancelled=" + e.isCancelled());
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onOpen(InventoryOpenEvent e) {
      this.note((Player) e.getPlayer(), "INV_OPEN " + e.getInventory().getType());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onClose(InventoryCloseEvent e) {
      this.note((Player) e.getPlayer(), "INV_CLOSE " + e.getInventory().getType());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onCmd(PlayerCommandPreprocessEvent e) {
      this.note(e.getPlayer(), "CMD " + e.getMessage() + " cancelled=" + e.isCancelled());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDamage(EntityDamageEvent e) {
      if (e.getEntity() instanceof Player p) {
         this.note(p, "DAMAGE " + e.getCause() + " " + String.format("%.1f", e.getFinalDamage()));
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDeath(PlayerDeathEvent e) {
      this.note(e.getEntity(), "DEATH keepInv=" + e.getKeepInventory());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onRespawn(PlayerRespawnEvent e) {
      this.note(e.getPlayer(), "RESPAWN");
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onTeleport(PlayerTeleportEvent e) {
      this.note(e.getPlayer(), "TP " + e.getCause() + " -> " + e.getTo().getWorld().getName());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onSwap(PlayerSwapHandItemsEvent e) {
      this.note(e.getPlayer(), "SWAP_HAND");
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onJoin(PlayerJoinEvent e) {
      this.note(e.getPlayer(), "JOIN");
   }
}
