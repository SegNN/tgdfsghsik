package org.impldev.holyclans;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Один клан. Все поля мутабельные — после изменения нужно вызвать {@code data.save(clan)}.
 */
public final class Clan {
   private final String name;
   private UUID leader;
   private long treasury;
   private int points;
   private int level;
   private int slots;
   private boolean pvp;
   private int glowColor;
   private String glowMode; // OFF, TEMP, ON

   private final Map<UUID, ClanMember> members;
   private final Map<Integer, Location> homes;

   public Clan(String name, UUID leader, long treasury, int points, int level, int slots,
               boolean pvp, int glowColor, String glowMode,
               Map<UUID, ClanMember> members, Map<Integer, Location> homes) {
      this.name = name;
      this.leader = leader;
      this.treasury = treasury;
      this.points = points;
      this.level = level;
      this.slots = slots;
      this.pvp = pvp;
      this.glowColor = glowColor;
      this.glowMode = glowMode;
      this.members = members;
      this.homes = homes;
   }

   public String name() { return this.name; }
   public String nameLower() { return this.name.toLowerCase(); }
   public UUID leader() { return this.leader; }
   public long treasury() { return this.treasury; }
   public int points() { return this.points; }
   public int level() { return this.level; }
   public int slots() { return this.slots; }
   public boolean pvp() { return this.pvp; }
   public int glowColor() { return this.glowColor; }
   public String glowMode() { return this.glowMode; }
   public Map<UUID, ClanMember> members() { return this.members; }
   public Map<Integer, Location> homes() { return this.homes; }

   public void setLeader(UUID leader) { this.leader = leader; }
   public void setTreasury(long treasury) { this.treasury = treasury; }
   public void setPoints(int points) { this.points = points; }
   public void setLevel(int level) { this.level = level; }
   public void setSlots(int slots) { this.slots = slots; }
   public void setPvp(boolean pvp) { this.pvp = pvp; }
   public void setGlowColor(int color) { this.glowColor = color; }
   public void setGlowMode(String mode) { this.glowMode = mode; }

   public boolean isLeader(UUID uuid) { return this.leader.equals(uuid); }

   public boolean isDeputy(UUID uuid) {
      ClanMember m = this.members.get(uuid);
      return m != null && m.role() == ClanRole.DEPUTY;
   }

   public boolean isLeaderOrDeputy(UUID uuid) {
      return this.isLeader(uuid) || this.isDeputy(uuid);
   }

   /** Строка с именами участников через запятую, для лора. */
   public String memberNames() {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (ClanMember m : this.members.values()) {
         if (!first) sb.append(", ");
         sb.append("§7").append(m.name());
         first = false;
      }
      sb.append("§7.");
      return sb.toString();
   }
}
