package ua.holydev.holyautocraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import ua.holydev.holyautocraft.HolyAutoCraft;
import ua.holydev.holyautocraft.utils.ColorUtils;

public class Config {
   private final NamespacedKey craftKey;
   private final String craftName;
   private final String renameText;
   private final boolean enableGlow;
   private final List<String> craftLore;
   private final Set<Material> allowedContainers;

   public Config(HolyAutoCraft plugin) {
      FileConfiguration cfg = plugin.getConfig();
      this.craftKey = new NamespacedKey(plugin, "autocraft");
      this.renameText = cfg.getString("autocraft.renameIn", "автокрафт");
      this.craftName = ColorUtils.apply(cfg.getString("autocraft.name", "&#b595e8Автокрафт"));
      this.enableGlow = cfg.getBoolean("autocraft.glow", true);
      this.allowedContainers = Set.of(
         Material.CHEST,
         Material.TRAPPED_CHEST,
         Material.BARREL,
         Material.SHULKER_BOX,
         Material.WHITE_SHULKER_BOX,
         Material.ORANGE_SHULKER_BOX,
         Material.MAGENTA_SHULKER_BOX,
         Material.LIGHT_BLUE_SHULKER_BOX,
         Material.YELLOW_SHULKER_BOX,
         Material.LIME_SHULKER_BOX,
         Material.PINK_SHULKER_BOX,
         Material.GRAY_SHULKER_BOX,
         Material.LIGHT_GRAY_SHULKER_BOX,
         Material.CYAN_SHULKER_BOX,
         Material.PURPLE_SHULKER_BOX,
         Material.BLUE_SHULKER_BOX,
         Material.BROWN_SHULKER_BOX,
         Material.GREEN_SHULKER_BOX,
         Material.RED_SHULKER_BOX,
         Material.BLACK_SHULKER_BOX,
         Material.DISPENSER,
         Material.DROPPER,
         Material.HOPPER
      );
      List<String> rawLore = cfg.getStringList("autocraft.lore");
      List<String> translatedLore = new ArrayList<>();
      if (!rawLore.isEmpty()) {
         for (String line : rawLore) {
            translatedLore.add(ColorUtils.apply(line));
         }
      }

      this.craftLore = translatedLore.isEmpty() ? null : translatedLore;
   }

   public NamespacedKey getCraftKey() {
      return this.craftKey;
   }

   public String getRenameText() {
      return this.renameText;
   }

   public String getCraftName() {
      return this.craftName;
   }

   public boolean isGlowEnabled() {
      return this.enableGlow;
   }

   public List<String> getCraftLore() {
      return this.craftLore;
   }

   public Set<Material> getAllowedContainers() {
      return this.allowedContainers;
   }
}
