package ru.nezxenka.holykits.managers;

import java.util.Map;
import ru.nezxenka.holykits.util.LegacyBlobReader;
import ru.nezxenka.holykits.util.LegacySkullFix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.holykits.HolyKits;

public class KitManager {
   private final HolyKits plugin;
   private final Map<String, Kit> kits = new HashMap<>();

   public KitManager(HolyKits plugin) {
      this.plugin = plugin;
      this.loadKits();
   }

   private void loadKits() {
      FileConfiguration config = this.plugin.getConfig();
      // головы из 1.16.5 хранят профиль в старом формате, 1.21.4 разбирает его в пустой
      // профиль -> текстура теряется. Чиним прямо при загрузке, файл на диске не трогаем.
      Map<String, String> legacyBlobs = LegacyBlobReader.read(new java.io.File(this.plugin.getDataFolder(), "config.yml"));
      int repairedSkulls = 0;

      for (String kitName : config.getKeys(false)) {
         if (config.isConfigurationSection(kitName)) {
            ConfigurationSection kitSection = config.getConfigurationSection(kitName);
            Kit kit = new Kit(kitName, kitSection.getInt("priority", 0), kitSection.getLong("cooldown", 0L));
            if (kitSection.isConfigurationSection("items")) {
               ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");

               for (String slotStr : itemsSection.getKeys(false)) {
                  try {
                     int slot = Integer.parseInt(slotStr);
                     ItemStack item = itemsSection.getItemStack(slotStr);
                     if (item != null) {
                        if (LegacySkullFix.repair(item, legacyBlobs.get(kitName + ".items." + slotStr))) {
                           repairedSkulls++;
                        }

                        kit.addItem(slot, item);
                     }
                  } catch (NumberFormatException var111) {
                     this.plugin.getLogger().warning("Некорректный слот " + slotStr + " в наборе " + kitName);
                  }
               }
            }

            this.kits.put(kitName, kit);
         }
      }

      if (repairedSkulls > 0) {
         this.plugin.getLogger().info("Restored legacy (1.16.5) skull textures for " + repairedSkulls + " item(s) in kits");
      }
   }

   public boolean createKit(String kitName, Player player) {
      if (this.kits.containsKey(kitName)) {
         this.getKit(kitName).saveFromInventory(player.getInventory());
         this.saveKit(this.getKit(kitName));
         return false;
      } else {
         Kit kit = new Kit(kitName, 0, 0L);
         kit.saveFromInventory(player.getInventory());
         this.kits.put(kitName, kit);
         this.saveKit(kit);
         return true;
      }
   }

   public boolean deleteKit(String kitName) {
      if (this.kits.containsKey(kitName)) {
         this.kits.remove(kitName);
         FileConfiguration config = this.plugin.getConfig();
         config.set(kitName, null);
         this.plugin.saveConfig();
         return true;
      } else {
         return false;
      }
   }

   public void setKitCooldown(String kitName, long cooldown) {
      Kit kit = this.kits.get(kitName);
      if (kit != null) {
         kit.setCooldown(cooldown);
         this.saveKit(kit);
      }
   }

   public void setKitPriority(String kitName, int priority) {
      Kit kit = this.kits.get(kitName);
      if (kit != null) {
         kit.setPriority(priority);
         this.saveKit(kit);
      }
   }

   public void clearCooldown(Player player, String kitName) {
      this.plugin.getDatabase().clearCooldown(player.getUniqueId(), kitName);
   }

   private void saveKit(Kit kit) {
      FileConfiguration config = this.plugin.getConfig();
      ConfigurationSection kitSection = config.createSection(kit.getName());
      kitSection.set("priority", kit.getPriority());
      kitSection.set("cooldown", kit.getCooldown());
      ConfigurationSection itemsSection = kitSection.createSection("items");
      kit.getItems().forEach((slot, item) -> itemsSection.set(String.valueOf(slot), item));
      ConfigurationSection armorSection = kitSection.createSection("armor");
      this.plugin.saveConfig();
   }

   public List<Kit> getAvailableKits(Player player) {
      return this.kits.values().stream().filter(kit -> player.hasPermission("holykits.kit." + kit.getName())).filter(Kit::isValid).collect(Collectors.toList());
   }

   public List<Kit> getAllKits() {
      return new ArrayList<>(this.kits.values());
   }

   public Optional<Kit> getBestPriorityKit(Player player, String kitName) {
      if (!this.hasKit(kitName)) {
         return Optional.empty();
      } else {
         Kit requestedKit = this.getKit(kitName);
         if (!player.hasPermission("holykits.kit." + kitName)) {
            return Optional.empty();
         } else if (!requestedKit.isValid()) {
            return Optional.empty();
         } else if (requestedKit.getPriority() == -1) {
            return Optional.of(requestedKit);
         } else {
            int maxPriority = this.getAvailableKits(player)
               .stream()
               .filter(kit -> kit.getPriority() != -1)
               .mapToInt(Kit::getPriority)
               .max()
               .orElse(Integer.MIN_VALUE);
            return requestedKit.getPriority() == maxPriority ? Optional.of(requestedKit) : Optional.empty();
         }
      }
   }

   public Kit getKit(String kitName) {
      return this.kits.get(kitName);
   }

   public boolean hasKit(String kitName) {
      return this.kits.containsKey(kitName);
   }
}
