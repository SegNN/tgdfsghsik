package org.impldev.holybuyer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

/**
 * Что скупщик принимает и по каким начальным ценам - читается из items.yml.
 *
 * @see BuyerItem
 */
public final class Catalogue {
   /**
    * Категория товаров.
    *
    * @param topBonus прибавка к цене, если у игрока это категория №1 по заработанным очкам
    */
   public record Category(String id, String name, Material icon, double topBonus) {
   }

   private final Map<String, Category> categories = new LinkedHashMap<>();
   private final Map<String, BuyerItem> items = new LinkedHashMap<>();
   private final Map<Material, List<BuyerItem>> byMaterial = new LinkedHashMap<>();

   public Catalogue(JavaPlugin plugin) {
      plugin.saveResource("items.yml", false);
      File file = new File(plugin.getDataFolder(), "items.yml");
      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

      double ratio = plugin.getConfig().getDouble("points-ratio", 0.2);

      ConfigurationSection cats = cfg.getConfigurationSection("categories");
      if (cats != null) {
         for (String id : cats.getKeys(false)) {
            this.categories.put(id, new Category(
                  id,
                  cats.getString(id + ".name", id),
                  material(cats.getString(id + ".icon"), Material.CHEST),
                  cats.getDouble(id + ".top-bonus", 0.0)));
         }
      }

      ConfigurationSection section = cfg.getConfigurationSection("items");
      if (section == null) {
         plugin.getLogger().severe("items.yml пуст - скупщик не сможет ничего принять");
         return;
      }

      for (String id : section.getKeys(false)) {
         String category = section.getString(id + ".category", "");
         if (!this.categories.containsKey(category)) {
            plugin.getLogger().warning("Товар " + id + ": неизвестная категория " + category);
            continue;
         }

         Set<Material> materials = EnumSet.noneOf(Material.class);
         for (String raw : section.getStringList(id + ".materials")) {
            Material material = material(raw, null);
            if (material == null) {
               plugin.getLogger().warning("Товар " + id + ": нет такого предмета - " + raw);
               continue;
            }

            materials.add(material);
         }

         if (materials.isEmpty()) {
            continue;
         }

         PotionType potion = null;
         String rawPotion = section.getString(id + ".potion");
         if (rawPotion != null) {
            try {
               potion = PotionType.valueOf(rawPotion.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
               plugin.getLogger().warning("Товар " + id + ": нет такого зелья - " + rawPotion);
               continue;
            }
         }

         double price = section.getDouble(id + ".price", 0.0);
         BuyerItem item = new BuyerItem(
               id,
               category,
               section.getString(id + ".name", id),
               price,
               section.getDouble(id + ".points", price * ratio),
               materials.iterator().next(),
               materials,
               potion,
               section.getStringList(id + ".also"));

         this.items.put(id, item);
         for (Material material : materials) {
            this.byMaterial.computeIfAbsent(material, k -> new ArrayList<>()).add(item);
         }
      }

      plugin.getLogger().info("Скупщик: загружено товаров - " + this.items.size()
            + ", категорий - " + this.categories.size());
   }

   private static Material material(String raw, Material fallback) {
      if (raw == null) {
         return fallback;
      }

      Material material = Material.matchMaterial(raw);
      return material == null ? fallback : material;
   }

   /**
    * Товар, под который подходит предмет из инвентаря, или null.
    *
    * <p>Список по материалу почти всегда из одного элемента; несколько бывает только у зелий,
    * и тогда решает тип эффекта.
    */
   public BuyerItem match(ItemStack stack) {
      if (stack == null) {
         return null;
      }

      List<BuyerItem> candidates = this.byMaterial.get(stack.getType());
      if (candidates == null) {
         return null;
      }

      for (BuyerItem item : candidates) {
         if (item.matches(stack)) {
            return item;
         }
      }

      return null;
   }

   public BuyerItem item(String id) {
      return this.items.get(id);
   }

   public Map<String, BuyerItem> items() {
      return Collections.unmodifiableMap(this.items);
   }

   public Category category(String id) {
      return this.categories.get(id);
   }

   public Map<String, Category> categories() {
      return Collections.unmodifiableMap(this.categories);
   }

   /** Товары одной категории в порядке возрастания начальной цены - так они и лежали в меню. */
   public List<BuyerItem> inCategory(String category) {
      List<BuyerItem> out = new ArrayList<>();
      for (BuyerItem item : this.items.values()) {
         if (item.category().equals(category)) {
            out.add(item);
         }
      }

      out.sort((a, b) -> Double.compare(a.price(), b.price()));
      return out;
   }
}
