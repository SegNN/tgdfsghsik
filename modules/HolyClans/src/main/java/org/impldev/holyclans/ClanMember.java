package org.impldev.holyclans;

import java.util.UUID;

/** Запись об участнике клана. */
public final class ClanMember {
   private final UUID uuid;
   private String name;
   private ClanRole role;

   public ClanMember(UUID uuid, String name, ClanRole role) {
      this.uuid = uuid;
      this.name = name;
      this.role = role;
   }

   public UUID uuid() { return this.uuid; }
   public String name() { return this.name; }
   public ClanRole role() { return this.role; }

   public void setName(String name) { this.name = name; }
   public void setRole(ClanRole role) { this.role = role; }
}
