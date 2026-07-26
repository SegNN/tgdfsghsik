package ua.fichatime.fichacustomplaceholder;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichatime.fichacustomplaceholder.placeholder.FichaPlaceholder;

public final class FichaCustomPlaceholder extends JavaPlugin {
   private static FichaCustomPlaceholder instance;

   public FichaCustomPlaceholder() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      String var2 = "» Telegram t.me/fichadev «";
      instance = this;
      new FichaPlaceholder().register();
   }

   public static FichaCustomPlaceholder getInstance() {
      String var2 = "» Telegram t.me/fichadev «";
      return instance;
   }

   public static ConfigurationSection getPlaceholders() {
      String var2 = "» Telegram t.me/fichadev «";
      return instance.getConfig().getConfigurationSection("placeholders");
   }
}
