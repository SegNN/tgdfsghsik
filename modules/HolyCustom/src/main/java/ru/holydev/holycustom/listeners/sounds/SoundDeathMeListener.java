package ru.holydev.holycustom.listeners.sounds;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.SoundDeathMeMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.manager.ConfigManager;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holycustom.menus.sounds.SoundDeathMeMenu;
import ru.holydev.holycustom.menus.sounds.SoundDeathPlayerMenu;
import ru.holydev.holycustom.menus.sounds.SoundHitMenu;
import ru.holydev.holycustom.menus.sounds.SoundLowHealMenu;
import ru.holydev.holycustom.menus.sounds.SoundVolumeMenu;

public class SoundDeathMeListener implements Listener {
   private final HolyCustom plugin;
   private final ConfigManager configManager;

   public SoundDeathMeListener(HolyCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof SoundDeathMeMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getSlot(), e.getClick());
      }
   }

   private void inventoryClick(Player player, int slot, ClickType clickType) {
      MenuItems menuItems = new MenuItems();
      switch (slot) {
         case 2:
            new SoundHitMenu(menuItems).open(player);
            return;
         case 4:
            new SoundLowHealMenu(menuItems).open(player);
            return;
         case 5:
            new SoundDeathPlayerMenu(menuItems).open(player);
            return;
         case 45:
            new MainMenu(menuItems).open(player);
            return;
         default:
            if (clickType == ClickType.LEFT) {
               if (this.plugin.getConfig().contains("deathme")) {
                  for (String key : this.plugin.getConfig().getConfigurationSection("deathme.items").getKeys(false)) {
                     String path = "deathme.items." + key;
                     int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                     if (configSlot == slot) {
                        String soundName = this.plugin.getConfig().getString(path + ".sound");

                        try {
                           Sound sound = Sound.valueOf(soundName);
                           if (HolyCustom.getPlayerData(player, "death_sound").equalsIgnoreCase(sound.name())) {
                              HolyCustom.playerData.get(player).remove("death_sound");
                           } else {
                              HolyCustom.playerData.get(player).put("death_sound", sound.name());
                              HolyCustom.playerData.get(player).put("sound_volume", "1.0");
                              HolyCustom.playerData.get(player).put("sound_pitch", "1.0");
                              player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                           }
                        } catch (IllegalArgumentException var12) {
                           player.sendMessage("§cОшибка: звук " + soundName + " не найден!");
                        }

                        SoundDeathMeMenu soundHitMenu = new SoundDeathMeMenu(menuItems);
                        soundHitMenu.open(player);
                        return;
                     }
                  }
               }
            } else if (clickType == ClickType.RIGHT && this.plugin.getConfig().contains("deathme")) {
               for (String keyx : this.plugin.getConfig().getConfigurationSection("deathme.items").getKeys(false)) {
                  String path = "deathme.items." + keyx;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String soundName = this.plugin.getConfig().getString(path + ".sound");

                     try {
                        Sound sound = Sound.valueOf(soundName);
                        if (HolyCustom.getPlayerData(player, "death_sound").equalsIgnoreCase(sound.name())) {
                           SoundVolumeMenu soundVolumeMenu = new SoundVolumeMenu(menuItems);
                           soundVolumeMenu.open(player);
                        }
                     } catch (IllegalArgumentException var13) {
                        player.sendMessage("§cОшибка: звук " + soundName + " не найден!");
                     }
                  }
               }
            }
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent e) {
      Player player = e.getEntity();
      if (HolyCustom.playerData.get(player) != null && HolyCustom.playerData.get(player).containsKey("death_sound")) {
         try {
            Sound sound = Sound.valueOf(HolyCustom.playerData.get(player).get("death_sound").toUpperCase());
            if (HolyCustom.playerData.get(player).containsKey("sound_volume")) {
               Float volume = Float.valueOf(HolyCustom.playerData.get(player).get("sound_volume"));
               Float pitch = Float.valueOf(HolyCustom.playerData.get(player).getOrDefault("sound_pitch", "1.0"));
               player.playSound(player.getLocation(), sound, volume, pitch);
            } else {
               Float volume = Float.valueOf("1.0");
               Float pitch = Float.valueOf(HolyCustom.playerData.get(player).getOrDefault("sound_pitch", "1.0"));
               player.playSound(player.getLocation(), sound, volume, pitch);
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }
}
