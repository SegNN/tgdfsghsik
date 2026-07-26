package org.nezxenka.litecontainer.util;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Достаёт сырые значения `internal:` из YAML вместе с их полным путём.
 *
 * <p>Через загруженный YamlConfiguration до них не добраться: Bukkit десериализует ItemStack
 * сразу при чтении, и блоб в объект уже не попадает. А в нём для головы лежит профиль в
 * формате 1.16.5 (SkullProfile{Properties,Id}), который 1.21.4 молча разбирает в пустой
 * профиль - отсюда головы Steve.
 *
 * <p>Путь строится по отступам, поэтому годится и для вложенной структуры китов
 * (`<кит>.items.<слот>`), а не только для плоского списка.
 */
public final class LegacyBlobReader {
   private LegacyBlobReader() {
   }

   /** Ключи-числа YAML пишет как '12' - кавычки в путь попадать не должны. */
   private static String unquote(String key) {
      if (key.length() >= 2
            && ((key.charAt(0) == '\'' && key.charAt(key.length() - 1) == '\'')
             || (key.charAt(0) == '"' && key.charAt(key.length() - 1) == '"'))) {
         return key.substring(1, key.length() - 1);
      }

      return key;
   }

   public static Map<String, String> read(File file) {
      Map<String, String> out = new HashMap<>();
      if (file == null || !file.isFile()) {
         return out;
      }

      try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
         // стек пар (отступ, имя ключа) - текущий путь от корня
         Deque<int[]> indents = new ArrayDeque<>();
         Deque<String> names = new ArrayDeque<>();
         String line;

         while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
               continue;
            }

            int colon = trimmed.indexOf(':');
            if (colon <= 0) {
               continue;
            }

            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
               indent++;
            }

            String key = unquote(trimmed.substring(0, colon).trim());
            String value = trimmed.substring(colon + 1).trim();

            while (!indents.isEmpty() && indents.peek()[0] >= indent) {
               indents.pop();
               names.pop();
            }

            if ("internal".equals(key) && !value.isEmpty()) {
               StringBuilder path = new StringBuilder();
               String[] parts = names.toArray(new String[0]);
               for (int i = parts.length - 1; i >= 0; i--) {
                  // `internal` лежит внутри блока `meta:` предмета - это деталь сериализации
                  // ItemStack, в пути её быть не должно
                  if (i == 0 && "meta".equals(parts[i])) {
                     continue;
                  }

                  if (path.length() > 0) {
                     path.append('.');
                  }
                  path.append(parts[i]);
               }

               out.put(path.toString(), value);
            } else if (value.isEmpty()) {
               indents.push(new int[]{indent});
               names.push(key);
            }
         }
      } catch (Exception ignored) {
         // конфиг нечитаем - просто нечего чинить
      }

      return out;
   }
}
