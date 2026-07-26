package org.dimasik.holyenchantments.enchantscore;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.enchantslist.armor.Impenetrable;
import org.dimasik.holyenchantments.enchantscore.enchantslist.armor.LavaMove;
import org.dimasik.holyenchantments.enchantscore.enchantslist.axe.Fermer;
import org.dimasik.holyenchantments.enchantscore.enchantslist.hoe.Delicious;
import org.dimasik.holyenchantments.enchantscore.enchantslist.hoe.Sowing;
import org.dimasik.holyenchantments.enchantscore.enchantslist.pickaxe.Boor;
import org.dimasik.holyenchantments.enchantscore.enchantslist.pickaxe.Breakless;
import org.dimasik.holyenchantments.enchantscore.enchantslist.pickaxe.ExpBoost;
import org.dimasik.holyenchantments.enchantscore.enchantslist.pickaxe.Magnet;
import org.dimasik.holyenchantments.enchantscore.enchantslist.pickaxe.Melting;
import org.dimasik.holyenchantments.enchantscore.enchantslist.sword.Critical;
import org.dimasik.holyenchantments.enchantscore.enchantslist.sword.Destroyer;
import org.dimasik.holyenchantments.enchantscore.enchantslist.sword.Farmer;
import org.dimasik.holyenchantments.enchantscore.enchantslist.sword.Filter;
import org.dimasik.holyenchantments.enchantscore.enchantslist.trident.Aim;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;
import org.dimasik.holyenchantments.enchantscore.levels.DoubleLevel;
import org.dimasik.holyenchantments.enchantscore.levels.IntLevel;
import org.dimasik.holyenchantments.utils.MColor;

public class EnchantsMap {
   public Filter filter;
   public Sowing sowing;
   public Delicious delicious;
   public Impenetrable impenetrable;
   public Breakless breakless;
   public ExpBoost expBoost;
   public Critical critical;
   public Boor boor;
   public Melting melting;
   public LavaMove lavaMove;
   public Destroyer destroyer;
   public Magnet magnet;
   public Fermer fermer;
   public Farmer farmer;
   public Aim aim;

   public EnchantsMap(HolyEnchantments plugin) {
      File file = new File(plugin.getDataFolder(), "enchants.yml");
      if (!file.exists()) {
         try (InputStream is = plugin.getResource("enchants.yml")) {
            ByteStreams.copy(is, Files.newOutputStream(file.toPath()));
         } catch (IOException var21) {
            throw new RuntimeException(var21);
         }
      }

      FileConfiguration enchantsConfig = YamlConfiguration.loadConfiguration(file);

      for (String enchantKey : enchantsConfig.getKeys(false)) {
         ConfigurationSection enchantSection = enchantsConfig.getConfigurationSection(enchantKey);
         if (enchantSection != null) {
            String key = enchantKey.toLowerCase();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "holyenchantments-" + key);
            String displayName = MColor.prefix(enchantSection.getString("name", ""));
            List<Material> tools = new ArrayList<>();

            for (String toolName : enchantSection.getStringList("tools")) {
               try {
                  tools.add(Material.valueOf(toolName.toUpperCase()));
               } catch (NullPointerException var19) {
                  plugin.getLogger().warning("Инструмент " + toolName + " указан неверно! Для зачарования " + enchantKey);
               }
            }

            String soundAlone = enchantSection.getString("sounds.alone", "");
            String soundAround = enchantSection.getString("sounds.around", "");
            int maxAnvilLevel = enchantSection.getInt("max-anvil-level", Integer.MAX_VALUE);
            HashMap<Integer, AbstractLevel> levels = new HashMap<>();
            Particle particle = null;
            ConfigurationSection levelSection = enchantSection.getConfigurationSection("levels");
            MEnchant enchant = this.createEnchant(
               plugin, key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel, levelSection
            );
            if (enchant != null) {
               plugin.getEnchantManager().registerEnchant(key, namespacedKey, enchant);
            }
         }
      }
   }

   private MEnchant createEnchant(
      HolyEnchantments plugin,
      String key,
      NamespacedKey namespacedKey,
      String displayName,
      String soundAlone,
      String soundAround,
      Particle particle,
      List<Material> tools,
      HashMap<Integer, AbstractLevel> levels,
      int maxAnvilLevel,
      ConfigurationSection levelSection
   ) {
      if (levelSection != null) {
         for (String levelKey : levelSection.getKeys(false)) {
            int level = Integer.parseInt(levelKey);
            int chance = levelSection.getInt(levelKey + ".chance");
            int enchTableChance = levelSection.getInt(levelKey + ".enchant-table-chance");
            switch (key) {
               case "impenetrable":
               case "expboost":
               case "critical":
               case "destroyer":
               case "fermer":
               case "farmer":
                  double multiplier = levelSection.getDouble(levelKey + ".multiplier");
                  levels.put(level, new DoubleLevel(chance, enchTableChance, multiplier));
                  break;
               case "aim":
                  double dRadius = levelSection.getDouble(levelKey + ".radius");
                  levels.put(level, new DoubleLevel(chance, enchTableChance, dRadius));
                  break;
               case "boor":
                  int lenght = levelSection.getInt(levelKey + ".lenght");
                  levels.put(level, new IntLevel(chance, enchTableChance, lenght));
                  break;
               case "lava-move":
                  int radius = levelSection.getInt(levelKey + ".radius");
                  levels.put(level, new IntLevel(chance, enchTableChance, radius));
                  break;
               default:
                  levels.put(level, new AbstractLevel(chance, enchTableChance));
            }
         }
      }

      switch (key) {
         case "filter":
            this.filter = new Filter(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.filter;
         case "sowing":
            this.sowing = new Sowing(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.sowing;
         case "delicious":
            this.delicious = new Delicious(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.delicious;
         case "impenetrable":
            this.impenetrable = new Impenetrable(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.impenetrable;
         case "breakless":
            this.breakless = new Breakless(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.breakless;
         case "expboost":
            this.expBoost = new ExpBoost(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.expBoost;
         case "critical":
            this.critical = new Critical(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.critical;
         case "boor":
            this.boor = new Boor(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.boor;
         case "melting":
            this.melting = new Melting(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.melting;
         case "lava-move":
            this.lavaMove = new LavaMove(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.lavaMove;
         case "destroyer":
            this.destroyer = new Destroyer(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.destroyer;
         case "magnet":
            this.magnet = new Magnet(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.magnet;
         case "fermer":
            this.fermer = new Fermer(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.fermer;
         case "farmer":
            this.farmer = new Farmer(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.farmer;
         case "aim":
            this.aim = new Aim(key, namespacedKey, displayName, soundAlone, soundAround, particle, tools, levels, maxAnvilLevel);
            return this.aim;
         default:
            return null;
      }
   }
}
