package org.dimasik.holyenchantments.grindstone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.EnchantManager;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.utils.MColor;
import org.dimasik.holyenchantments.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class GrindstoneMenu implements InventoryHolder {
   public Inventory inventory;
   public boolean forceClose = false;
   public HashMap<Integer, Enchantment> vanillaEnchantments = new HashMap<>();
   public HashMap<Integer, Integer> vanillaEnchantmentLevels = new HashMap<>();
   public HashMap<Integer, MEnchant> customEnchantments = new HashMap<>();
   public HashMap<Integer, Integer> customEnchantmentLevels = new HashMap<>();
   public HashMap<Integer, Enchantment> vanillaEnchantmentsList = new HashMap<>();
   public HashMap<Integer, Integer> vanillaEnchantmentsListLevels = new HashMap<>();
   public HashMap<Integer, MEnchant> customEnchantmentsList = new HashMap<>();
   public HashMap<Integer, Integer> customEnchantmentsListLevels = new HashMap<>();

   public GrindstoneMenu(Player player, ItemStack itemStack) {
      this.inventory = Bukkit.createInventory(this, 45, "Точило");
      ItemStack gray = this.buildButton(Material.GRAY_STAINED_GLASS_PANE, 1, false, " ");
      ItemStack orange = this.buildButton(Material.ORANGE_STAINED_GLASS_PANE, 1, false, " ");
      this.inventory.setItem(0, gray);
      this.inventory.setItem(1, gray);
      this.inventory.setItem(2, gray);
      this.inventory.setItem(3, gray);
      this.inventory.setItem(5, gray);
      this.inventory.setItem(6, gray);
      this.inventory.setItem(7, gray);
      this.inventory.setItem(8, gray);
      this.inventory.setItem(9, gray);
      this.inventory.setItem(10, gray);
      this.inventory.setItem(11, gray);
      this.inventory.setItem(17, gray);
      this.inventory.setItem(18, gray);
      this.inventory.setItem(19, itemStack);
      this.inventory.setItem(20, gray);
      this.inventory.setItem(27, gray);
      this.inventory.setItem(28, gray);
      this.inventory.setItem(29, gray);
      this.inventory.setItem(35, gray);
      this.inventory.setItem(36, gray);
      this.inventory.setItem(37, gray);
      this.inventory.setItem(38, gray);
      this.inventory.setItem(39, gray);
      this.inventory.setItem(40, gray);
      this.inventory.setItem(41, gray);
      this.inventory.setItem(42, gray);
      this.inventory.setItem(43, gray);
      this.inventory.setItem(44, gray);
      this.buildEnchantments(true);
      player.openInventory(this.inventory);
   }

   public void buildEnchantments(boolean updateItem) {
      ItemStack itemStack = this.inventory.getItem(19);
      int[] slots = new int[]{12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
      int index = 0;
      if (updateItem) {
         this.updateAllEnchantments();
      }

      ItemStack orange = this.buildButton(Material.ORANGE_STAINED_GLASS_PANE, 1, false, " ");
      this.inventory.setItem(12, orange);
      this.inventory.setItem(13, orange);
      this.inventory.setItem(14, orange);
      this.inventory.setItem(15, orange);
      this.inventory.setItem(16, orange);
      this.inventory.setItem(21, orange);
      this.inventory.setItem(22, orange);
      this.inventory.setItem(23, orange);
      this.inventory.setItem(24, orange);
      this.inventory.setItem(25, orange);
      this.inventory.setItem(30, orange);
      this.inventory.setItem(31, orange);
      this.inventory.setItem(32, orange);
      this.inventory.setItem(33, orange);
      this.inventory.setItem(34, orange);
      if (itemStack != null) {
         for (Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            if (this.vanillaEnchantments.containsValue(entry.getKey())) {
               this.inventory
                  .setItem(
                     slots[index],
                     this.buildButton(
                        Material.BOOK,
                        1,
                        false,
                        " " + this.translateEnchantment(entry.getKey(), entry.getValue()),
                        "",
                        " &x&F&F&A&2&0&0&l&n▍&7 Зачарование: " + this.translateEnchantment(entry.getKey(), entry.getValue()),
                        " &x&F&F&A&2&0&0&l&n▍&7 Цена: &e15 &6exp",
                        " &x&F&F&A&2&0&0&l▍&7 Продажа: &x&F&F&2&2&2&2Включена",
                        "",
                        " &x&F&F&A&2&0&0▶ &fНажмите, чтобы отменить выбор для продажи"
                     )
                  );
            } else {
               this.inventory
                  .setItem(
                     slots[index],
                     this.buildButton(
                        Material.ENCHANTED_BOOK,
                        1,
                        true,
                        " " + this.translateEnchantment(entry.getKey(), entry.getValue()),
                        "",
                        " &x&F&F&A&2&0&0&l&n▍&7 Зачарование: " + this.translateEnchantment(entry.getKey(), entry.getValue()),
                        " &x&F&F&A&2&0&0&l&n▍&7 Цена: &e15 &6exp",
                        " &x&F&F&A&2&0&0&l▍&7 Продажа: &x&0&5&F&B&0&0Выключена",
                        "",
                        " &x&F&F&A&2&0&0▶ &fНажмите, чтобы выбрать для продажи"
                     )
                  );
            }

            index++;
         }

         EnchantManager enchantManager = HolyEnchantments.getInstance().getEnchantManager();

         for (Entry<String, NamespacedKey> entry : enchantManager.getEnchantsNames().entrySet()) {
            if (itemStack.getItemMeta().getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)) {
               if (this.customEnchantments.containsValue(enchantManager.getEnchantsNameSpaced().get(entry.getValue()))) {
                  this.inventory
                     .setItem(
                        slots[index],
                        this.buildButton(
                           Material.BOOK,
                           1,
                           false,
                           " "
                              + this.translateEnchantment(
                                 enchantManager.getEnchantsNameSpaced().get(entry.getValue()),
                                 (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(entry.getValue(), PersistentDataType.INTEGER)
                              ),
                           "",
                           " &x&F&F&A&2&0&0&l&n▍&7 Зачарование: "
                              + this.translateEnchantment(
                                 enchantManager.getEnchantsNameSpaced().get(entry.getValue()),
                                 (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(entry.getValue(), PersistentDataType.INTEGER)
                              ),
                           " &x&F&F&A&2&0&0&l&n▍&7 Цена: &e15 &6exp",
                           " &x&F&F&A&2&0&0&l▍&7 Продажа: &x&F&F&2&2&2&2Включена",
                           "",
                           " &x&F&F&A&2&0&0▶ &fНажмите, чтобы отменить выбор для продажи"
                        )
                     );
               } else {
                  this.inventory
                     .setItem(
                        slots[index],
                        this.buildButton(
                           Material.ENCHANTED_BOOK,
                           1,
                           true,
                           " "
                              + this.translateEnchantment(
                                 enchantManager.getEnchantsNameSpaced().get(entry.getValue()),
                                 (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(entry.getValue(), PersistentDataType.INTEGER)
                              ),
                           "",
                           " &x&F&F&A&2&0&0&l&n▍&7 Зачарование: "
                              + this.translateEnchantment(
                                 enchantManager.getEnchantsNameSpaced().get(entry.getValue()),
                                 (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(entry.getValue(), PersistentDataType.INTEGER)
                              ),
                           " &x&F&F&A&2&0&0&l&n▍&7 Цена: &e15 &6exp",
                           " &x&F&F&A&2&0&0&l▍&7 Продажа: &x&0&5&F&B&0&0Выключена",
                           "",
                           " &x&F&F&A&2&0&0▶ &fНажмите, чтобы выбрать для продажи"
                        )
                     );
               }

               index++;
            }
         }
      } else {
         this.vanillaEnchantments.clear();
         this.customEnchantments.clear();
         this.vanillaEnchantmentsList.clear();
         this.customEnchantmentsList.clear();
      }

      if (this.vanillaEnchantments.isEmpty() && this.customEnchantments.isEmpty()) {
         this.inventory
            .setItem(
               26,
               this.buildButton(
                  Material.RED_STAINED_GLASS_PANE,
                  1,
                  true,
                  " &x&F&F&2&2&2&2Нечего продавать ;(",
                  "",
                  " &x&F&F&2&2&2&2&l&n▍&f Для продажи выбери зачарования, ",
                  " &x&F&F&2&2&2&2&l▍&f которые хочешь обменять на опыт "
               )
            );
         this.inventory
            .setItem(
               4,
               this.buildButton(
                  Material.NETHER_STAR, 1, true, " &x&0&5&F&B&0&0Включить всё", "", " &x&0&5&F&B&0&0▶ &fНажмите, чтобы включить", " &fпродажу всех зачарований"
               )
            );
      } else {
         int cnt = this.vanillaEnchantments.size() + this.customEnchantments.size();
         List<String> lore = new ArrayList<>();
         lore.add("");
         lore.add("&f Зачарования, которые будут потеряны: ");
         lore.add("");

         for (Entry<Integer, Enchantment> enchantments : this.vanillaEnchantments.entrySet()) {
            lore.add(
               " &x&F&F&2&2&2&2&l• "
                  + this.translateEnchantment(enchantments.getValue(), this.vanillaEnchantmentLevels.get(enchantments.getKey()))
                  + " &fза &e15"
            );
         }

         for (Entry<Integer, MEnchant> enchantments : this.customEnchantments.entrySet()) {
            lore.add(
               " &x&F&F&2&2&2&2&l• "
                  + this.translateEnchantment(enchantments.getValue(), this.customEnchantmentLevels.get(enchantments.getKey()))
                  + " &fза &e15"
            );
         }

         lore.add("");
         lore.add(" &x&0&5&F&B&0&0&l• &fЗа это вы получите: &e" + cnt * 15 + " &fопыта");
         lore.add("");
         lore.add(" &x&0&5&F&B&0&0▶ &fНажмите, чтобы продать выбранные зачарования");
         String[] string = new String[0];
         this.inventory.setItem(26, this.buildButton(Material.LIME_STAINED_GLASS_PANE, 1, true, " &x&0&5&F&B&0&0Продать всё", lore.toArray(string)));
         this.inventory
            .setItem(
               4,
               this.buildButton(
                  Material.NETHER_STAR,
                  1,
                  true,
                  " &x&F&F&2&2&2&2Выключить всё",
                  "",
                  " &x&F&F&2&2&2&2▶ &fНажмите, чтобы выключить",
                  " &fпродажу всех зачарований"
               )
            );
      }
   }

   public void updateAllEnchantments() {
      ItemStack itemStack = this.inventory.getItem(19);
      this.vanillaEnchantments.clear();
      this.customEnchantments.clear();
      this.vanillaEnchantmentsList.clear();
      this.customEnchantmentsList.clear();
      if (itemStack != null) {
         int[] slots = new int[]{12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
         int index = 0;

         for (Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            this.vanillaEnchantmentsList.put(slots[index], entry.getKey());
            this.vanillaEnchantmentsListLevels.put(slots[index], entry.getValue());
            index++;
         }

         EnchantManager enchantManager = HolyEnchantments.getInstance().getEnchantManager();

         for (Entry<String, NamespacedKey> entry : enchantManager.getEnchantsNames().entrySet()) {
            if (itemStack.getItemMeta().getPersistentDataContainer().has(entry.getValue(), PersistentDataType.INTEGER)) {
               this.customEnchantmentsList.put(slots[index], enchantManager.getEnchantsNameSpaced().get(entry.getValue()));
               this.customEnchantmentsListLevels
                  .put(slots[index], (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(entry.getValue(), PersistentDataType.INTEGER));
               index++;
            }
         }
      }
   }

   public ItemStack buildButton(Material material, int cnt, boolean glow, String name, String... lore) {
      ItemStack itemStack = new ItemStack(material, cnt);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (glow) {
         itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemMeta.setDisplayName(Parser.color(name));
      List<String> lore2 = new ArrayList<>();

      for (String s : lore) {
         lore2.add(Parser.color(s));
      }

      itemMeta.setLore(lore2);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public String translateEnchantment(Enchantment enchantment, int level) {
      String enchantmentName = enchantment.getKey().getKey();

      String romanLevel = switch (level) {
         case 0 -> "";
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> "enchantment.level." + level;
      };
      switch (enchantmentName) {
         case "protection":
            return "&x&F&F&A&2&0&0Защита &x&F&F&A&2&0&0" + romanLevel;
         case "fire_protection":
            return "&x&F&F&A&2&0&0Огнеупорность &x&F&F&A&2&0&0" + romanLevel;
         case "feather_falling":
            return "&x&F&F&A&2&0&0Невесомость &x&F&F&A&2&0&0" + romanLevel;
         case "blast_protection":
            return "&x&F&F&A&2&0&0Взрывоустойчивость &x&F&F&A&2&0&0" + romanLevel;
         case "projectile_protection":
            return "&x&F&F&A&2&0&0Защита от снарядов &x&F&F&A&2&0&0" + romanLevel;
         case "respiration":
            return "&x&F&F&A&2&0&0Подводное дыхание &x&F&F&A&2&0&0" + romanLevel;
         case "aqua_affinity":
            return "&x&F&F&A&2&0&0&x&F&F&A&2&0&0Подводник &x&F&F&A&2&0&0" + romanLevel;
         case "thorns":
            return "&x&F&F&A&2&0&0&x&F&F&A&2&0&0Шипы &x&F&F&A&2&0&0" + romanLevel;
         case "depth_strider":
            return "&x&F&F&A&2&0&0&x&F&F&A&2&0&0Подводная хотьба &x&F&F&A&2&0&0" + romanLevel;
         case "frost_walker":
            return "&x&F&F&A&2&0&0&x&F&F&A&2&0&0Ледоход &x&F&F&A&2&0&0" + romanLevel;
         case "binding_curse":
            return "&x&F&F&A&2&0&0&x&F&F&A&2&0&0Проклятие несъемности &x&F&F&A&2&0&0" + romanLevel;
         case "sharpness":
            return "&x&F&F&A&2&0&0Острота &x&F&F&A&2&0&0" + romanLevel;
         case "smite":
            return "&x&F&F&A&2&0&0Небесная кара &x&F&F&A&2&0&0" + romanLevel;
         case "bane_of_arthropods":
            return "&x&F&F&A&2&0&0Бич членистоногих &x&F&F&A&2&0&0" + romanLevel;
         case "knockback":
            return "&x&F&F&A&2&0&0Отдача &x&F&F&A&2&0&0" + romanLevel;
         case "fire_aspect":
            return "&x&F&F&A&2&0&0Заговор огня &x&F&F&A&2&0&0" + romanLevel;
         case "looting":
            return "&x&F&F&A&2&0&0Добыча &x&F&F&A&2&0&0" + romanLevel;
         case "sweeping":
            return "&x&F&F&A&2&0&0Разящий клинок &x&F&F&A&2&0&0" + romanLevel;
         case "efficiency":
            return "&x&F&F&A&2&0&0Эффективность &x&F&F&A&2&0&0" + romanLevel;
         case "silk_touch":
            return "&x&F&F&A&2&0&0Шелковое касание &x&F&F&A&2&0&0" + romanLevel;
         case "unbreaking":
            return "&x&F&F&A&2&0&0Прочность &x&F&F&A&2&0&0" + romanLevel;
         case "fortune":
            return "&x&F&F&A&2&0&0Удача &x&F&F&A&2&0&0" + romanLevel;
         case "power":
            return "&x&F&F&A&2&0&0Сила &x&F&F&A&2&0&0" + romanLevel;
         case "punch":
            return "&x&F&F&A&2&0&0Откидывание &x&F&F&A&2&0&0" + romanLevel;
         case "flame":
            return "&x&F&F&A&2&0&0Воспланемение &x&F&F&A&2&0&0" + romanLevel;
         case "infinity":
            return "&x&F&F&A&2&0&0Бесконечность &x&F&F&A&2&0&0" + romanLevel;
         case "luck_of_the_sea":
            return "&x&F&F&A&2&0&0Везучий моряк &x&F&F&A&2&0&0" + romanLevel;
         case "lure":
            return "&x&F&F&A&2&0&0Приманка &x&F&F&A&2&0&0" + romanLevel;
         case "loyalty":
            return "&x&F&F&A&2&0&0Верность &x&F&F&A&2&0&0" + romanLevel;
         case "impaling":
            return "&x&F&F&A&2&0&0Пронзание &x&F&F&A&2&0&0" + romanLevel;
         case "riptide":
            return "&x&F&F&A&2&0&0Тягун &x&F&F&A&2&0&0" + romanLevel;
         case "channeling":
            return "&x&F&F&A&2&0&0Громовержец &x&F&F&A&2&0&0" + romanLevel;
         case "mending":
            return "&x&F&F&A&2&0&0Починка &x&F&F&A&2&0&0" + romanLevel;
         case "vanishing_curse":
            return "&x&F&F&A&2&0&0Проклятие утраты &x&F&F&A&2&0&0" + romanLevel;
         case "multishot":
            return "&x&F&F&A&2&0&0Тройной выстрел &x&F&F&A&2&0&0" + romanLevel;
         case "quick_charge":
            return "&x&F&F&A&2&0&0Быстрая перезарядка &x&F&F&A&2&0&0" + romanLevel;
         case "piercing":
            return "&x&F&F&A&2&0&0Пронзающая стрела &x&F&F&A&2&0&0" + romanLevel;
         case "soul_speed":
            return "&x&F&F&A&2&0&0Скорость души &x&F&F&A&2&0&0" + romanLevel;
         default:
            return "&x&F&F&A&2&0&0" + enchantmentName + " &x&F&F&A&2&0&0" + romanLevel;
      }
   }

   public String translateEnchantment(MEnchant mEnchant, int level) {
      String displayName = MColor.prefix(mEnchant.getDisplayName());
      return "&x&F&F&A&2&0&0"
         + displayName.replace("%level", "&x&F&F&A&2&0&0" + HolyEnchantments.getInstance().getEnchantManager().levelsFormat.get(level))
            .replace(Parser.color("&7"), "");
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }
}
