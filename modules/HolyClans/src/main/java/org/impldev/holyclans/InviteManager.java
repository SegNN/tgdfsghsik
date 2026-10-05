package org.impldev.holyclans;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит активные приглашения в кланы. Приглашение живёт {@code timeoutMs} и
 * автоматически удаляется по истечении.
 */
public final class InviteManager {
   /** Кого пригласили → от какого клана. */
   private final Map<UUID, Invite> pending = new ConcurrentHashMap<>();

   public record Invite(Clan clan, UUID inviter, long expiresAt) {}

   public void add(UUID target, Clan clan, UUID inviter, long timeoutMs) {
      this.pending.put(target, new Invite(clan, inviter, System.currentTimeMillis() + timeoutMs));
   }

   public Invite get(UUID target) {
      Invite inv = this.pending.get(target);
      if (inv != null && System.currentTimeMillis() > inv.expiresAt()) {
         this.pending.remove(target);
         return null;
      }
      return inv;
   }

   public boolean has(UUID target) {
      return this.get(target) != null;
   }

   public boolean hasFromClan(UUID target, Clan clan) {
      Invite inv = this.get(target);
      return inv != null && inv.clan() == clan;
   }

   public Invite remove(UUID target) {
      return this.pending.remove(target);
   }

   /** Убрать просроченные приглашения. */
   public void cleanup() {
      long now = System.currentTimeMillis();
      Iterator<Map.Entry<UUID, Invite>> it = this.pending.entrySet().iterator();
      while (it.hasNext()) {
         if (now > it.next().getValue().expiresAt()) {
            it.remove();
         }
      }
   }
}
