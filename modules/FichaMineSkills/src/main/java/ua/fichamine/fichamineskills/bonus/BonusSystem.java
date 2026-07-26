package ua.fichamine.fichamineskills.bonus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichamine.fichamineskills.database.DatabaseManager;
import ua.fichamine.fichamineskills.util.Logger;

public class BonusSystem {
   private static final Map<String, List<BonusSystem.Bonus>> BONUSES_BY_TYPE = new HashMap<>();
   private static final Map<Integer, Map<Integer, BonusSystem.Bonus>> BONUSES_BY_PAGE_AND_SLOT = new HashMap<>();
   private static final Map<String, BonusSystem.BonusEffect> BONUS_EFFECTS = new HashMap<>();

   public BonusSystem() {
   }

   private static void initializeBonusEffects() {
      BONUS_EFFECTS.put("emeraldization", new BonusSystem.BonusEffect("Изумрудизация", "emerald", 0.1, 0.2, 0.3));
      BONUS_EFFECTS.put("auto_smelting", new BonusSystem.BonusEffect("Авто-плавка", "furnace", 100.0, 200.0, 300.0));
      BONUS_EFFECTS.put("luck", new BonusSystem.BonusEffect("Удача", "gold_ingot", 25.0, 40.0, 50.0, 75.0, 100.0, 125.0));
      BONUS_EFFECTS.put("quest_exp", new BonusSystem.BonusEffect("Опыт квестовика", "experience_bottle", 10.0, 15.0, 30.0, 45.0, 60.0, 75.0));
      BONUS_EFFECTS.put("repair", new BonusSystem.BonusEffect("Восстановление", "anvil", 1.0));
      BONUS_EFFECTS.put("speed", new BonusSystem.BonusEffect("Скорость", "sugar", 10.0, 15.0));
      BONUS_EFFECTS.put("rage", new BonusSystem.BonusEffect("Ярость", "blaze_powder", 0.001, 0.002));
      BONUS_EFFECTS.put("universal_key", new BonusSystem.BonusEffect("Универсальный ключ", "tripwire_hook", 1.0E-5, 2.0E-5, 3.0E-5, 4.0E-5));
   }

   private static void initializeBonuses() {
      createBonus("emeraldization", 1, 10, 250, 1);
      createBonus("auto_smelting", 1, 11, 400, 1);
      createBonus("luck", 1, 12, 500, 1);
      createBonus("quest_exp", 1, 14, 2800, 1);
      createBonus("repair", 1, 15, 3200, 1);
      createBonus("emeraldization", 1, 16, 4000, 2);
      createBonus("repair", 1, 19, 210, 1);
      createBonus("speed", 1, 21, 600, 1);
      createBonus("speed", 1, 23, 2400, 2);
      createBonus("rage", 1, 25, 6400, 1);
      createBonus("quest_exp", 1, 28, 175, 1);
      createBonus("quest_exp", 1, 30, 700, 2);
      createBonus("luck", 1, 32, 2000, 2);
      createBonus("luck", 1, 34, 7040, 3);
      createBonus("luck", 1, 36, 25, 1);
      createBonus("speed", 1, 37, 125, 1);
      createBonus("repair", 1, 39, 800, 2);
      createBonus("emeraldization", 1, 40, 1000, 2);
      createBonus("universal_key", 1, 41, 1600, 1);
      createBonus("speed", 1, 43, 7680, 3);
      createBonus("quest_exp", 1, 44, 8320, 3);
      createBonus("speed", 2, 10, 15360, 4);
      createBonus("quest_exp", 2, 11, 16640, 4);
      createBonus("repair", 2, 12, 19200, 4);
      createBonus("emeraldization", 2, 14, 44800, 5);
      createBonus("universal_key", 2, 15, 51200, 2);
      createBonus("luck", 2, 16, 56320, 5);
      createBonus("luck", 2, 19, 14080, 4);
      createBonus("emeraldization", 2, 21, 22400, 4);
      createBonus("repair", 2, 23, 38400, 5);
      createBonus("speed", 2, 25, 61440, 5);
      createBonus("universal_key", 2, 28, 12800, 3);
      createBonus("rage", 2, 30, 25600, 2);
      createBonus("quest_exp", 2, 32, 33280, 5);
      createBonus("quest_exp", 2, 34, 66560, 6);
      createBonus("repair", 2, 36, 9600, 6);
      createBonus("emeraldization", 2, 37, 11200, 6);
      createBonus("universal_key", 2, 39, 25600, 4);
      createBonus("luck", 2, 40, 28160, 6);
      createBonus("speed", 2, 41, 30720, 6);
      createBonus("repair", 2, 43, 76800, 7);
      createBonus("emeraldization", 2, 44, 89600, 7);
      int total = BONUSES_BY_PAGE_AND_SLOT.values().stream().mapToInt(m -> m.values().size()).sum();
   }

   private static void createBonus(String effectType, int page, int slot, int requiredPoints, int level) {
      BonusSystem.BonusEffect effect = BONUS_EFFECTS.get(effectType);
      if (effect == null) {
         Logger.warning("Неизвестный тип эффекта: " + effectType);
      } else {
         BonusSystem.Bonus bonus = new BonusSystem.Bonus(slot, effect, level, requiredPoints, page);
         BONUSES_BY_PAGE_AND_SLOT.computeIfAbsent(page, k -> new HashMap<>()).put(slot, bonus);
         BONUSES_BY_TYPE.computeIfAbsent(effectType, k -> new ArrayList<>()).add(bonus);
      }
   }

   public static List<BonusSystem.Bonus> getBonusesForPage(int page) {
      Map<Integer, BonusSystem.Bonus> bySlot = BONUSES_BY_PAGE_AND_SLOT.get(page);
      return bySlot == null
         ? Collections.emptyList()
         : bySlot.values().stream().sorted(Comparator.comparingInt(BonusSystem.Bonus::getSlot)).collect(Collectors.toList());
   }

   public static BonusSystem.Bonus getBonusByPageAndSlot(int page, int slot) {
      Map<Integer, BonusSystem.Bonus> bySlot = BONUSES_BY_PAGE_AND_SLOT.get(page);
      return bySlot == null ? null : bySlot.get(slot);
   }

   public static List<String> getActiveEffects(double playerPoints) {
      Map<String, BonusSystem.Bonus> maxLevelEffects = new HashMap<>();
      BONUSES_BY_PAGE_AND_SLOT.values().forEach(map -> {
         for (BonusSystem.Bonus bonus : map.values()) {
            if (bonus.isUnlocked(playerPoints)) {
               String effectType = bonus.getEffect().getType();
               BonusSystem.Bonus currentMax = maxLevelEffects.get(effectType);
               if (currentMax == null || bonus.getLevel() > currentMax.getLevel()) {
                  maxLevelEffects.put(effectType, bonus);
               }
            }
         }
      });
      return maxLevelEffects.values().stream().sorted(Comparator.comparing(b -> b.getEffect().getName().toLowerCase())).map(bonus -> {
         double val = bonus.getEffect().getValue(bonus.getLevel());
         if (val <= 0.0) {
            return null;
         } else {
            String shown = Math.abs(val - Math.round(val)) < 1.0E-9 ? String.valueOf(Math.round(val)) : String.format("%.3f", val).replaceAll("\\.?0+$", "");
            String levelRoman = toRoman(bonus.getLevel());
            String colored = getColoredEffectName(bonus.getEffect().getName());
            return colored + " — " + levelRoman + " &f(" + shown + "%)";
         }
      }).filter(Objects::nonNull).map(effect -> ChatColor.translateAlternateColorCodes('&', "&5&o  &x&0&0&D&8&F&F• " + effect)).collect(Collectors.toList());
   }

   public static void checkForNewUnlocks(Player player, double oldPoints, double newPoints, DatabaseManager databaseManager) {
      if (oldPoints < newPoints) {
         List<BonusSystem.Bonus> unlockedBonuses = new ArrayList<>();

         for (Map<Integer, BonusSystem.Bonus> bySlot : BONUSES_BY_PAGE_AND_SLOT.values()) {
            for (BonusSystem.Bonus bonus : bySlot.values()) {
               if (oldPoints < bonus.getRequiredPoints() && newPoints >= bonus.getRequiredPoints()) {
                  unlockedBonuses.add(bonus);
                  databaseManager.recordBonusUnlockAsync(player.getUniqueId(), bonus.getEffect().getName(), bonus.getLevel(), bonus.getRequiredPoints());
               }
            }
         }

         if (!unlockedBonuses.isEmpty()) {
            unlockedBonuses.sort(Comparator.comparingInt(BonusSystem.Bonus::getRequiredPoints));
            BonusSystem.Bonus highestBonus = unlockedBonuses.get(unlockedBonuses.size() - 1);
            String effectColored = getColoredEffectName(highestBonus.getEffect().getName());
            String message = ChatColor.translateAlternateColorCodes(
               '&', "&b▶ &fВы получили новый &bуровень &fэффекта " + effectColored + " &fу квестовика Автошахт (&x&0&0&F&1&F&d/mineskills&f)."
            );
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
         }
      }
   }

   public static boolean hasBonus(double playerPoints, String effectType, int minLevel) {
      List<BonusSystem.Bonus> bonuses = BONUSES_BY_TYPE.get(effectType);
      return bonuses == null
         ? false
         : bonuses.stream().filter(bonus -> bonus.isUnlocked(playerPoints)).mapToInt(BonusSystem.Bonus::getLevel).max().orElse(0) >= minLevel;
   }

   public static int getAvailablePage(double playerPoints) {
      return playerPoints >= 8320.0 ? 2 : 1;
   }

   public static double getBonusMultiplier(double playerPoints, String effectType) {
      List<BonusSystem.Bonus> bonuses = BONUSES_BY_TYPE.get(effectType);
      return bonuses == null
         ? 0.0
         : bonuses.stream()
            .filter(bonus -> bonus.isUnlocked(playerPoints))
            .mapToDouble(bonus -> bonus.getEffect().getValue(bonus.getLevel()))
            .max()
            .orElse(0.0);
   }

   public static int getMaxUnlockedLevel(double playerPoints, String effectType) {
      List<BonusSystem.Bonus> bonuses = BONUSES_BY_TYPE.get(effectType);
      return bonuses == null ? 0 : bonuses.stream().filter(b -> b.isUnlocked(playerPoints)).mapToInt(BonusSystem.Bonus::getLevel).max().orElse(0);
   }

   private static String toRoman(int number) {
      String[] romans = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
      return number >= 0 && number < romans.length ? romans[number] : String.valueOf(number);
   }

   public static String getColoredEffectName(String effectName) {
      switch (effectName) {
         case "Изумрудизация":
            return "&x&0&A&E&2&1&9" + effectName;
         case "Авто-плавка":
            return "&d" + effectName;
         case "Удача":
            return "&x&D&B&E&A&6&1" + effectName;
         case "Опыт квестовика":
            return "&x&1&E&E&2&6&7" + effectName;
         case "Восстановление":
            return "&x&C&E&7&1&E&A" + effectName;
         case "Скорость":
            return "&b" + effectName;
         case "Ярость":
            return "&x&E&A&6&A&6&A" + effectName;
         case "Универсальный ключ":
            return "&x&F&F&7&0&0&0" + effectName;
         default:
            return effectName;
      }
   }

   private static String[] getEffectLongDescription(String effectName) {
      switch (effectName) {
         case "Изумрудизация":
            return new String[]{"Изумрудизация превращает выпадающие", "при добыче блоков алмазы в изумруды."};
         case "Авто-плавка":
            return new String[]{"Авто-плавка позволяет сразу перплавить", "руду при добыче блоков на автошахте."};
         case "Удача":
            return new String[]{"Увеличивает количество выпадаемых", "ресурсов при добыче блоков на автошахте."};
         case "Опыт квестовика":
            return new String[]{"Увеличивает выпадаемый опыт у квестовика", "при добыче блоков на автошахте."};
         case "Восстановление":
            return new String[]{"Восстановление ремонтирует инструмент", "при добыче блоков на единицу прочности."};
         case "Скорость":
            return new String[]{"Увеличивает скорость передвижения", "по всей территории автошахты."};
         case "Ярость":
            return new String[]{"Режим ярости увеличивает выпадаемые ресурсы", "при добыче блоков на автошахте в 2 раза."};
         case "Универсальный ключ":
            return new String[]{"Шанс получить уникальный ключ", "при добыче блоков на автошахте."};
         default:
            return new String[]{"Описание эффекта", "отсутствует"};
      }
   }

   static {
      initializeBonusEffects();
      initializeBonuses();
   }

   public static class Bonus {
      private final int slot;
      private final BonusSystem.BonusEffect effect;
      private final int level;
      private final int requiredPoints;
      private final int page;

      public Bonus(int slot, BonusSystem.BonusEffect effect, int level, int requiredPoints, int page) {
         this.slot = slot;
         this.effect = effect;
         this.level = level;
         this.requiredPoints = requiredPoints;
         this.page = page;
      }

      public ItemStack createItem(double playerPoints) {
         boolean isUnlocked = playerPoints >= this.requiredPoints;
         Material pane = isUnlocked ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
         ItemStack item = new ItemStack(pane);
         ItemMeta meta = item.getItemMeta();
         String name = isUnlocked ? "&x&0&5&F&B&0&0✔ Бонус получен" : "&x&F&F&2&2&2&2✘ Бонус ещё не получен";
         meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
         List<String> lore = new ArrayList<>();
         String coloredName = BonusSystem.getColoredEffectName(this.effect.getName());
         String valuePercent = String.format("%.3f", this.effect.getValue(this.level)).replaceAll("\\.?0+$", "");
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o &7• " + coloredName + "&f &x&E&7&E&7&E&7(+" + valuePercent + "%)"));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
         String[] descLines = BonusSystem.getEffectLongDescription(this.effect.getName());
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&0&0&D&8&F&F&l&n▍&f " + descLines[0]));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&0&0&D&8&F&F&l▍&f " + descLines[1]));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o &x&0&0&D&8&F&F&n◢&x&0&0&D&8&F&F Прогресс получения:"));
         String var10002 = String.format("%.0f", playerPoints);
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o &x&0&0&D&8&F&F◤&x&E&7&E&7&E&7 " + var10002 + "&7 / &x&E&7&E&7&E&7" + this.requiredPoints));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
         meta.setLore(lore);
         item.setItemMeta(meta);
         return item;
      }

      public boolean isUnlocked(double playerPoints) {
         return playerPoints >= this.requiredPoints;
      }

      public int getSlot() {
         return this.slot;
      }

      public BonusSystem.BonusEffect getEffect() {
         return this.effect;
      }

      public int getLevel() {
         return this.level;
      }

      public int getRequiredPoints() {
         return this.requiredPoints;
      }

      public int getPage() {
         return this.page;
      }
   }

   public static class BonusEffect {
      private final String name;
      private final String type;
      private final String material;
      private final double[] values;
      private final String description;
      private final String effectDescription;

      public BonusEffect(String name, String material, double... values) {
         this.name = name;
         this.type = name.toLowerCase().replace(" ", "_");
         this.material = material;
         this.values = values;
         this.description = this.generateDescription(name);
         this.effectDescription = this.generateEffectDescription(name);
      }

      private String generateDescription(String name) {
         switch (name) {
            case "Изумрудизация":
               return "Превращает выпадающие при добыче блоков алмазы в изумруды";
            case "Авто-плавка":
               return "Автоматически плавит руду при добыче";
            case "Удача":
               return "Увеличивает количество выпадаемых ресурсов";
            case "Опыт квестовика":
               return "Увеличивает выпадаемый опыт у квестовика";
            case "Восстановление":
               return "Ремонтирует инструмент при добыче";
            case "Скорость":
               return "Увеличивает скорость передвижения по автошахте";
            case "Ярость":
               return "Режим ярости увеличивает выпадаемые ресурсы в 2 раза";
            case "Универсальный ключ":
               return "Шанс получить уникальный ключ при добыче";
            default:
               return "Неизвестный эффект";
         }
      }

      private String generateEffectDescription(String name) {
         switch (name) {
            case "Изумрудизация":
               return "Шанс превращения алмазов в изумруды";
            case "Авто-плавка":
               return "Автоматическая плавка руды";
            case "Удача":
               return "Увеличение количества ресурсов";
            case "Опыт квестовика":
               return "Увеличение выпадаемого опыта";
            case "Восстановление":
               return "Ремонт инструмента";
            case "Скорость":
               return "Увеличение скорости передвижения";
            case "Ярость":
               return "Режим ярости";
            case "Универсальный ключ":
               return "Шанс получения ключа";
            default:
               return "Неизвестный эффект";
         }
      }

      public double getValue(int level) {
         if (this.values.length == 0) {
            return 0.0;
         } else if (level <= 0) {
            return 0.0;
         } else {
            return level > this.values.length ? this.values[this.values.length - 1] : this.values[level - 1];
         }
      }

      public String getName() {
         return this.name;
      }

      public String getType() {
         return this.type;
      }

      public String getMaterial() {
         return this.material;
      }

      public String getDescription() {
         return this.description;
      }

      public String getEffectDescription() {
         return this.effectDescription;
      }

      public String getDisplayName() {
         return this.name;
      }
   }
}
