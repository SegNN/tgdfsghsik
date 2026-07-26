package ua.holytime.holyspawneroved.Utils;

import java.util.List;
import ua.holytime.holyspawneroved.HolySpawneroved;

public class Config {
   private final HolySpawneroved plugin;

   public Config(HolySpawneroved plugin) {
      this.plugin = plugin;
   }

   public String getMaterial(String path) {
      return this.plugin.getConfig().getString("item." + path + ".material");
   }

   public String getName(String path) {
      return this.plugin.getConfig().getString("item." + path + ".name");
   }

   public List<String> getLore(String path) {
      return this.plugin.getConfig().getStringList("item." + path + ".lore");
   }

   public int getPrice(String path) {
      return this.plugin.getConfig().getInt("item." + path + ".price");
   }

   public String getCategory(String path) {
      return this.plugin.getConfig().getString("item." + path + ".category");
   }
}
