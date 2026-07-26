package org.dimasik.liteauction.backend.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.Material;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.json.JSONObject;

public final class ItemNameUtil {
   private static JSONObject translations;
   private static boolean loaded = false;
   private static final Map<String, String> reverseTranslations = new HashMap<>();
   private static final Map<String, String> customTags = new HashMap<>();

   public static String getLocalizedItemName(Material material) {
      loadTranslationsIfNeeded();
      if (translations == null) {
         return formatMaterialName(material);
      } else {
         String materialKey = material.getKey().getKey();
         return translations.optString(materialKey, formatMaterialName(material));
      }
   }

   public static boolean containsTag(String tag) {
      loadTranslationsIfNeeded();
      String lowerTag = tag.toLowerCase();
      return reverseTranslations.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(tag))
         || customTags.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(tag));
   }

   public static HashSet<String> escapeTag(String russianName) {
      loadTranslationsIfNeeded();

      for (Entry<String, String> entry : customTags.entrySet()) {
         if (entry.getKey().equalsIgnoreCase(russianName)) {
            return new HashSet<>(Set.of(entry.getValue().split(",")));
         }
      }

      for (Entry<String, String> entryx : reverseTranslations.entrySet()) {
         if (entryx.getKey().equalsIgnoreCase(russianName)) {
            return new HashSet<>(Set.of(entryx.getValue().split(",")));
         }
      }

      return null;
   }

   public static synchronized void loadTranslationsIfNeeded() {
      if (!loaded) {
         try {
            File dataFolder = LiteAuction.getInstance().getDataFolder();
            if (!dataFolder.exists()) {
               dataFolder.mkdirs();
            }

            File assetsFile = new File(dataFolder, "assets.json");
            if (!assetsFile.exists()) {
               try (InputStream in = LiteAuction.getInstance().getResource("assets.json")) {
                  if (in == null) {
                     LiteAuction.getInstance().getLogger().warning("Файл assets.json не найден в ресурсах плагина!");
                     translations = new JSONObject();
                     loaded = true;
                     return;
                  }

                  Files.copy(in, assetsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            }

            String content = new String(Files.readAllBytes(assetsFile.toPath()), StandardCharsets.UTF_8);
            translations = new JSONObject(content);
            reverseTranslations.clear();

            for (String key : translations.keySet()) {
               reverseTranslations.put(translations.getString(key), key);
            }
         } catch (Exception var7) {
            LiteAuction.getInstance().getLogger().log(Level.SEVERE, "Ошибка загрузки переводов", (Throwable)var7);
            translations = new JSONObject();
         }

         customTags.clear();
         customTags.putAll(ConfigManager.getCUSTOM_TAGS());
         loaded = true;
      }
   }

   private static String formatMaterialName(Material material) {
      String[] parts = material.name().split("_");
      StringBuilder result = new StringBuilder();

      for (String part : parts) {
         if (!result.isEmpty()) {
            result.append(" ");
         }

         result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
      }

      return result.toString();
   }

   @Generated
   private ItemNameUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static Map<String, String> getReverseTranslations() {
      return reverseTranslations;
   }

   @Generated
   public static Map<String, String> getCustomTags() {
      return customTags;
   }
}
