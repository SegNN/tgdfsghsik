package ua.fichatime.fichaspawneroved.Utils;

import java.util.List;
import ua.fichatime.fichaspawneroved.FichaSpawneroved;

public class Config {
   private final FichaSpawneroved plugin;

   public Config(FichaSpawneroved plugin) {
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
