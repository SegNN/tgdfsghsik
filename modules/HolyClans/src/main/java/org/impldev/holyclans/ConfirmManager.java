package org.impldev.holyclans;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит ожидающие подтверждения на удаление клана или передачу лидерства.
 */
public final class ConfirmManager {
   public enum Action { DELETE, NEWLEADER }

   public record Pending(Action action, UUID target, long expiresAt) {}

   private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

   public void add(UUID player, Action action, UUID target, long timeoutMs) {
      this.pending.put(player, new Pending(action, target, System.currentTimeMillis() + timeoutMs));
   }

   public Pending get(UUID player) {
      Pending p = this.pending.get(player);
      if (p != null && System.currentTimeMillis() > p.expiresAt()) {
         this.pending.remove(player);
         return null;
      }
      return p;
   }

   public Pending remove(UUID player) {
      return this.pending.remove(player);
   }
}
