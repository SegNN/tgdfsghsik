package ua.fichadev.fichacases.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemBuilder {
   private ItemStack item;

   public ItemBuilder(ItemStack item) {
      if (item == null) {
         this.item = new ItemStack(Material.AIR);
      } else {
         this.item = item.clone();
      }
   }

   public ItemBuilder(Material material) {
      this.item = new ItemStack(material);
   }

   public ItemMeta meta() {
      return this.item.getItemMeta();
   }

   public ItemBuilder meta(ItemMeta meta) {
      this.item.setItemMeta(meta);
      return this;
   }

   public String name() {
      return this.meta().hasDisplayName() ? this.meta().getDisplayName() : "";
   }

   public ItemBuilder name(String name) {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else {
         meta.setDisplayName(name);
         this.meta(meta);
         return this;
      }
   }

   public List<String> lore() {
      return (List<String>)(this.meta().getLore() == null ? new ArrayList<>() : this.meta().getLore());
   }

   public ItemBuilder lore(List<String> lore) {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else {
         meta.setLore(lore);
         this.meta(meta);
         return this;
      }
   }

   public ItemBuilder addLore(String... lore) {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else {
         List<String> old = this.lore();

         for (String line : lore) {
            old.add(line);
         }

         meta.setLore(old);
         this.meta(meta);
         return this;
      }
   }

   public ItemBuilder addLoreAbove(String... lore) {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else {
         List<String> old = this.lore();
         List<String> toAdd = Arrays.asList(lore);
         Collections.reverse(toAdd);
         old.addAll(0, toAdd);
         meta.setLore(old);
         this.meta(meta);
         return this;
      }
   }

   public ItemBuilder color(Color color) {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else if (!(meta instanceof LeatherArmorMeta)) {
         return this;
      } else if (color == null) {
         return this;
      } else {
         LeatherArmorMeta armorMeta = (LeatherArmorMeta)meta;
         armorMeta.setColor(color);
         this.meta(armorMeta);
         return this;
      }
   }

   public ItemBuilder removeAttributes() {
      ItemMeta meta = this.meta();
      if (meta == null) {
         return this;
      } else {
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS});
         this.meta(meta);
         return this;
      }
   }

   public ItemBuilder amount(int amount) {
      this.item.setAmount(amount);
      return this;
   }

   public int amount() {
      return this.item.getAmount();
   }

   public ItemBuilder glow(boolean isGlow) {
      ItemMeta meta = this.meta();
      if (meta != null && isGlow) {
         meta.addEnchant(Enchantment.DURABILITY, 1, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         this.meta(meta);
         return this;
      } else {
         return this;
      }
   }

   public ItemStack build() {
      return this.item;
   }

   public ItemBuilder save(ConfigurationSection section) {
      section.set("material", this.item.getType().name());
      ItemMeta meta = this.meta();
      if (meta != null) {
         section.set("name", meta.getDisplayName());
         section.set("lore", meta.getLore());
      }

      return this;
   }

   public static ItemBuilder fromConfig(ConfigurationSection section) {
      Material material = Material.valueOf(section.getString("material", "AIR"));
      String name = section.getString("name", "");
      List<String> lore = section.getStringList("lore");
      return new ItemBuilder(material).name(name).lore(lore);
   }

   public static ItemBuilder fromConfig(ConfigurationSection section, Function<String, String> textHandler) {
      Material material = Material.valueOf(section.getString("material", "AIR"));
      String name = textHandler.apply(section.getString("name", ""));
      boolean glow = section.getBoolean("glow");
      List<String> lore = new ArrayList<>();

      for (String line : section.getStringList("lore")) {
         if (!line.contains("%lucks%")) {
            lore.add(textHandler.apply(line));
         } else {
            String finalLine = textHandler.apply(line);
            List<String> lucks = List.of(finalLine.split("\n"));
            if (!lucks.isEmpty() && !finalLine.isBlank()) {
               for (String luckLine : lucks) {
                  lore.add(textHandler.apply(luckLine));
               }
            }
         }
      }

      return new ItemBuilder(material).name(name).lore(lore).glow(glow);
   }
}
