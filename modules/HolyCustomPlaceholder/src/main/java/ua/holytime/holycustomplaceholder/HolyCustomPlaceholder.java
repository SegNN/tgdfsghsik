package ua.holytime.holycustomplaceholder;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holytime.holycustomplaceholder.placeholder.HolyPlaceholder;

public final class HolyCustomPlaceholder extends JavaPlugin {
   private static HolyCustomPlaceholder instance;

   public HolyCustomPlaceholder() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      String var2 = "» Telegram t.me/ExampleTG «";
      instance = this;
      new HolyPlaceholder().register();
   }

   public static HolyCustomPlaceholder getInstance() {
      String var2 = "» Telegram t.me/ExampleTG «";
      return instance;
   }

   public static ConfigurationSection getPlaceholders() {
      String var2 = "» Telegram t.me/ExampleTG «";
      return instance.getConfig().getConfigurationSection("placeholders");
   }
}
