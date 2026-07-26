package org.dimasik.liteauction.backend.utils.tags;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.dimasik.liteauction.backend.enums.CategoryType;

public final class TagUtil {
   public static HashSet<String> getAllTags(ItemStack item) {
      HashSet<String> tags = new HashSet<>();
      if (item != null && !item.getType().isAir()) {
         tags.add(item.getType().name().toLowerCase());
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return tags;
         } else {
            if (meta.hasDisplayName()) {
               String name = meta.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
               if (!name.isEmpty()) {
                  tags.add("name: " + name);
               }
            }

            if (meta.hasEnchants()) {
               for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                  String enchantTag = String.format("enchantment: %s:%d", entry.getKey().getKey().getKey().toLowerCase(), entry.getValue());
                  tags.add(enchantTag);
               }
            }

            if (meta instanceof PotionMeta potionMeta) {
               for (PotionEffect effect : potionMeta.getCustomEffects()) {
                  String potionTag = String.format(
                     "potion: %s:%d:%d", effect.getType().getName().toLowerCase(), effect.getDuration(), effect.getAmplifier() + 1
                  );
                  tags.add(potionTag);
               }

               PotionData basePotionData = potionMeta.getBasePotionData();
               if (basePotionData.getType().getEffectType() != null) {
                  String potionTag = "basepotion: " + basePotionData.getType().getEffectType().getName().toLowerCase();
                  tags.add(potionTag);
               }
            }

            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            for (NamespacedKey key : pdc.getKeys()) {
               try {
                  if (pdc.has(key, PersistentDataType.STRING)) {
                     String value = (String)pdc.get(key, PersistentDataType.STRING);
                     tags.add(String.format("pdc: %s:%s:%s", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.INTEGER)) {
                     Integer value = (Integer)pdc.get(key, PersistentDataType.INTEGER);
                     tags.add(String.format("pdc: %s:%s:%d", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.DOUBLE)) {
                     Double value = (Double)pdc.get(key, PersistentDataType.DOUBLE);
                     tags.add(String.format("pdc: %s:%s:%f", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.LONG)) {
                     Long value = (Long)pdc.get(key, PersistentDataType.LONG);
                     tags.add(String.format("pdc: %s:%s:%d", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.FLOAT)) {
                     Float value = (Float)pdc.get(key, PersistentDataType.FLOAT);
                     tags.add(String.format("pdc: %s:%s:%f", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.SHORT)) {
                     Short value = (Short)pdc.get(key, PersistentDataType.SHORT);
                     tags.add(String.format("pdc: %s:%s:%d", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.BYTE)) {
                     Byte value = (Byte)pdc.get(key, PersistentDataType.BYTE);
                     tags.add(String.format("pdc: %s:%s:%d", key.getNamespace(), key.getKey(), value));
                  } else if (pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
                     byte[] value = (byte[])pdc.get(key, PersistentDataType.BYTE_ARRAY);
                     tags.add(String.format("pdc: %s:%s:byte_array[%d]", key.getNamespace(), key.getKey(), value.length));
                  } else if (pdc.has(key, PersistentDataType.INTEGER_ARRAY)) {
                     int[] value = (int[])pdc.get(key, PersistentDataType.INTEGER_ARRAY);
                     tags.add(String.format("pdc: %s:%s:int_array[%d]", key.getNamespace(), key.getKey(), value.length));
                  } else if (pdc.has(key, PersistentDataType.LONG_ARRAY)) {
                     long[] value = (long[])pdc.get(key, PersistentDataType.LONG_ARRAY);
                     tags.add(String.format("pdc: %s:%s:long_array[%d]", key.getNamespace(), key.getKey(), value.length));
                  } else if (pdc.has(key, PersistentDataType.TAG_CONTAINER)) {
                     PersistentDataContainer value = (PersistentDataContainer)pdc.get(key, PersistentDataType.TAG_CONTAINER);
                     tags.add(String.format("pdc: %s:%s:container[%d_keys]", key.getNamespace(), key.getKey(), value.getKeys().size()));
                  }
               } catch (Exception var7) {
               }
            }

            if (meta.hasCustomModelData()) {
               tags.add(String.format("custommodeldata: %d", meta.getCustomModelData()));
            }

            if (meta instanceof SkullMeta) {
               String texture = getHeadTexture((SkullMeta)meta);
               if (texture != null && !texture.isEmpty()) {
                  tags.add(String.format("head: %s", texture));
               }
            }

            if (meta.isUnbreakable()) {
               tags.add("unbreakable");
            }

            return tags;
         }
      } else {
         return tags;
      }
   }

   public static HashSet<String> getPartialTags(ItemStack item) {
      HashSet<String> tags = new HashSet<>();
      tags.add(item.getType().name().toLowerCase());
      if (item != null && !item.getType().isAir()) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return tags;
         } else {
            if (meta.hasDisplayName()) {
               String name = meta.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
               if (!name.isEmpty()) {
                  tags.add("name: " + name);
               }
            }

            if (meta instanceof SkullMeta) {
               String texture = getHeadTexture((SkullMeta)meta);
               if (texture != null && !texture.isEmpty()) {
                  String headTag = String.format("head: %s", texture);
                  tags.add(headTag);
               }
            }

            return tags;
         }
      } else {
         return tags;
      }
   }

   private static String getHeadTexture(SkullMeta skullMeta) {
      try {
         Field profileField = skullMeta.getClass().getDeclaredField("profile");
         profileField.setAccessible(true);
         GameProfile profile = (GameProfile)profileField.get(skullMeta);
         if (profile != null) {
            for (Property property : profile.getProperties().get("textures")) {
               if (property.getName().equals("textures")) {
                  return property.getValue();
               }
            }
         }

         return null;
      } catch (IllegalAccessException | NoSuchFieldException var5) {
         throw new RuntimeException(var5);
      }
   }

   public static String[] getItemCategories(Set<String> itemTags) {
      Set<CategoryType> categories = new HashSet<>();
      categories.add(CategoryType.ALL);
      if (itemTags != null && !itemTags.isEmpty()) {
         Set<String> lowerItemTags = new HashSet<>();

         for (String tag : itemTags) {
            lowerItemTags.add(tag.toLowerCase());
         }

         for (CategoryType category : CategoryType.values()) {
            if (category != CategoryType.ALL) {
               for (String categoryTag : category.getTags()) {
                  if (lowerItemTags.contains(categoryTag.toLowerCase())) {
                     categories.add(category);
                     break;
                  }
               }
            }
         }

         return categories.stream().map(CategoryType::getDisplayName).toArray(String[]::new);
      } else {
         return categories.stream().map(CategoryType::getDisplayName).toArray(String[]::new);
      }
   }

   @Generated
   private TagUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
