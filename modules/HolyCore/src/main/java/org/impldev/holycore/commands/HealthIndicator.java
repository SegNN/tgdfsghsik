package org.impldev.holycore.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.utils.Parser;
import org.jetbrains.annotations.NotNull;

/**
 * Переключатель показа здоровья над головами игроков.
 *
 * <p>Сам индикатор рисует TAB через belowname-objective, поэтому второй плагин в тот же слот
 * лезть не должен - они бы затирали друг друга. Вместо этого здесь отдаётся плейсхолдер
 * {@code %holyindicator_state%}, а TAB по нему гасит объектив своим disable-condition. Тогда
 * при выключении индикатор именно исчезает, не оставляя над головой никакого текста.
 *
 * <p>Имя плейсхолдера содержит подчёркивание намеренно: PlaceholderAPI 2.12.3 не разбирает
 * плейсхолдеры без него и копирует их в вывод дословно.
 */
public class HealthIndicator extends PlaceholderExpansion implements CommandExecutor {
   private final JavaPlugin plugin;
   private final File file;
   /** кто ВЫКЛЮЧИЛ индикатор; по умолчанию он включён у всех, как было до команды */
   private final Set<UUID> disabled = new HashSet<>();

   public HealthIndicator(JavaPlugin plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "healthindicator.yml");
      this.load();
   }

   private void load() {
      if (!this.file.isFile()) {
         return;
      }

      FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
      for (String raw : cfg.getStringList("disabled")) {
         try {
            this.disabled.add(UUID.fromString(raw));
         } catch (IllegalArgumentException ignored) {
            // мусор в файле не должен мешать плагину включиться
         }
      }
   }

   private void save() {
      FileConfiguration cfg = new YamlConfiguration();
      cfg.set("disabled", this.disabled.stream().map(UUID::toString).toList());

      try {
         this.plugin.getDataFolder().mkdirs();
         cfg.save(this.file);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить healthindicator.yml: " + e.getMessage());
      }
   }

   public boolean isEnabled(UUID uuid) {
      return !this.disabled.contains(uuid);
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      }

      Player player = (Player)sender;
      UUID uuid = player.getUniqueId();
      boolean nowEnabled;
      if (this.disabled.remove(uuid)) {
         nowEnabled = true;
      } else {
         this.disabled.add(uuid);
         nowEnabled = false;
      }

      this.save();
      player.sendMessage(Parser.color(nowEnabled
            ? "&#00a700❤ &fПоказ здоровья над игроками &#00a700включён&f."
            : "&#a70000❤ &fПоказ здоровья над игроками &#a70000выключен&f."));
      return true;
   }

   // --- PlaceholderAPI ---

   @Override
   public @NotNull String getIdentifier() {
      return "holyindicator";
   }

   @Override
   public @NotNull String getAuthor() {
      return "HolyZako";
   }

   @Override
   public @NotNull String getVersion() {
      return "1.0";
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Override
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null || !params.equalsIgnoreCase("state")) {
         return null;
      }

      return this.isEnabled(player.getUniqueId()) ? "on" : "off";
   }
}
