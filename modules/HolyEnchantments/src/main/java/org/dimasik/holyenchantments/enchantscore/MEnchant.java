package org.dimasik.holyenchantments.enchantscore;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;

public class MEnchant {
   private final String key;
   private final NamespacedKey namespacedKey;
   private final String displayName;
   private final List<Material> tools;
   private final HashMap<Integer, AbstractLevel> levels;
   private final Particle particleType;
   private final String soundActivateAlone;
   private final String soundActivateAround;
   private final Integer maxAnvilLevel;

   public MEnchant(
      String key,
      NamespacedKey namespacedKey,
      String displayName,
      String soundActivateAlone,
      String soundActivateAround,
      Particle particleType,
      List<Material> tools,
      HashMap<Integer, AbstractLevel> levels,
      Integer maxAnvilLevel
   ) {
      this.key = key;
      this.namespacedKey = namespacedKey;
      this.displayName = displayName;
      this.soundActivateAlone = soundActivateAlone;
      this.soundActivateAround = soundActivateAround;
      this.particleType = particleType;
      this.tools = tools;
      this.levels = levels;
      this.maxAnvilLevel = maxAnvilLevel;
   }

   public AbstractLevel getLevel(PersistentDataContainer data) {
      return this.levels.getOrDefault(data.get(this.namespacedKey, PersistentDataType.INTEGER), (AbstractLevel)null);
   }

   public boolean randomChance(PersistentDataContainer data) {
      AbstractLevel level = this.levels.getOrDefault(data.get(this.namespacedKey, PersistentDataType.INTEGER), (AbstractLevel)null);
      return level == null ? false : level.random();
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public Particle getParticleType() {
      return this.particleType;
   }

   public String getSoundAlone() {
      return this.soundActivateAlone;
   }

   public String getSoundAround() {
      return this.soundActivateAround;
   }

   public List<Material> getTools() {
      return this.tools;
   }

   public NamespacedKey getNamespacedKey() {
      return this.namespacedKey;
   }

   public String getKey() {
      return this.key;
   }

   public HashMap<Integer, AbstractLevel> getLevels() {
      return this.levels;
   }

   public Integer getMaxAnvilLevel() {
      return this.maxAnvilLevel;
   }
}
