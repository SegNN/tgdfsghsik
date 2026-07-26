package ua.fichatime.fichaspawneroved;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichatime.fichaspawneroved.command.FichaCommands;
import ua.fichatime.fichaspawneroved.gui.Menu;
import ua.fichatime.fichaspawneroved.listener.Event;

public final class FichaSpawneroved extends JavaPlugin {
   private FileConfiguration config;

   public FichaSpawneroved() {
   }

   public void onEnable() {
      if (!this.getDataFolder().exists()) {
         this.getDataFolder().mkdirs();
      }

      File configFile = new File(this.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         try (InputStream in = this.getResource("config.yml")) {
            Files.copy(in, configFile.toPath());
         } catch (IOException var17) {
            this.getLogger().severe("Не удалось создать config.yml!");
            var17.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }
      }

      this.config = YamlConfiguration.loadConfiguration(configFile);

      try {
         FichaCommands commands = new FichaCommands(this);
         this.getCommand("eggkeeper").setExecutor(commands);
         this.getCommand("eggkeeper").setTabCompleter(commands);
         this.getServer().getPluginManager().registerEvents(new Event(this), this);
      } catch (Exception var14) {
         this.getLogger().severe("Произошла ошибка при запуске плагина!");
         var14.printStackTrace();
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void onDisable() {
      for (Player player : this.getServer().getOnlinePlayers()) {
         if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) {
            player.closeInventory();
         }
      }
   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public void reloadPluginConfig() {
      this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
   }
}
