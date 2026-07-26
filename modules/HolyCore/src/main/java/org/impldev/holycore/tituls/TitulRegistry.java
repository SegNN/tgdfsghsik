package org.impldev.holycore.tituls;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Список титулов читается из тех же файлов DeluxeMenus, по которым работает старое меню /titul.
 *
 * <p>Своего хранилища титулов здесь нет намеренно. Владение уже выражено правами LuckPerms
 * (tituls.summer2024.1), а тексты лежат в gui_menus/titul/*.yml - завести рядом второй список
 * значило бы получить два источника правды, которые разойдутся при первом же добавлении титула.
 *
 * <p>В каждом сезонном меню на титул заведено по два предмета в одном слоте: один показывается
 * при отсутствии права, второй при наличии. Берём только второй - у него в view_requirement
 * стоит `has permission`, и именно его текст видит владелец титула.
 */
public final class TitulRegistry {
   private final JavaPlugin plugin;
   private final List<Titul> tituls = new ArrayList<>();

   public TitulRegistry(JavaPlugin plugin) {
      this.plugin = plugin;
      this.load();
   }

   public List<Titul> all() {
      return this.tituls;
   }

   public Titul byPermission(String permission) {
      return this.tituls.stream().filter(t -> t.permission().equals(permission)).findFirst().orElse(null);
   }

   public void reload() {
      this.tituls.clear();
      this.load();
   }

   private void load() {
      File dir = new File(this.plugin.getDataFolder().getParentFile(), "DeluxeMenus/gui_menus/titul");
      File[] files = dir.listFiles((d, n) -> n.endsWith(".yml") && n.contains("season"));
      if (files == null) {
         this.plugin.getLogger().warning("Меню титулов DeluxeMenus не найдено: " + dir);
         return;
      }

      for (File file : files) {
         YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
         ConfigurationSection items = cfg.getConfigurationSection("items");
         if (items == null) {
            continue;
         }

         int order = 0;
         for (String key : items.getKeys(false)) {
            String type = items.getString(key + ".view_requirement.requirements.perm_check.type");
            String permission = items.getString(key + ".view_requirement.requirements.perm_check.permission");
            String name = items.getString(key + ".display_name");
            if (permission == null || name == null || !"has permission".equalsIgnoreCase(type)) {
               continue;
            }

            String[] parts = permission.split("\\.");
            String season = parts.length > 1 ? parts[1] : "unknown";
            this.tituls.add(new Titul(permission, name.trim(), season, order++));
         }
      }

      this.tituls.sort(Comparator.comparingInt(Titul::seasonRank).thenComparingInt(Titul::order));
      this.plugin.getLogger().info("Титулов загружено: " + this.tituls.size());
   }
}
