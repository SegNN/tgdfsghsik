package ru.holydev.holycustom.listeners.sounds;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.SoundHitMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.manager.ConfigManager;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holycustom.menus.sounds.SoundDeathMeMenu;
import ru.holydev.holycustom.menus.sounds.SoundDeathPlayerMenu;
import ru.holydev.holycustom.menus.sounds.SoundHitMenu;
import ru.holydev.holycustom.menus.sounds.SoundLowHealMenu;
import ru.holydev.holycustom.menus.sounds.SoundVolumeMenu;

public class SoundHitListener implements Listener {
   private final HolyCustom plugin;
   private final ConfigManager configManager;

   public SoundHitListener(HolyCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof SoundHitMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getSlot(), e.getClick());
      }
   }

   private void inventoryClick(Player player, int slot, ClickType clickType) {
      MenuItems menuItems = new MenuItems();
      switch (slot) {
         case 3:
            new SoundDeathMeMenu(menuItems).open(player);
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
               if (this.plugin.getConfig().contains("hitsounds")) {
                  for (String key : this.plugin.getConfig().getConfigurationSection("hitsounds.items").getKeys(false)) {
                     String path = "hitsounds.items." + key;
                     int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                     if (configSlot == slot) {
                        String soundName = this.plugin.getConfig().getString(path + ".sound");

                        try {
                           Sound sound = Sound.valueOf(soundName);
                           if (HolyCustom.getPlayerData(player, "hit_sound").equalsIgnoreCase(sound.name())) {
                              HolyCustom.playerData.get(player).remove("hit_sound");
                           } else {
                              HolyCustom.playerData.get(player).put("hit_sound", sound.name());
                              HolyCustom.playerData.get(player).put("sound_volume", "1.0");
                              HolyCustom.playerData.get(player).put("sound_pitch", "1.0");
                              player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                           }
                        } catch (IllegalArgumentException var12) {
                           player.sendMessage("§cОшибка: звук " + soundName + " не найден!");
                        }

                        SoundHitMenu soundHitMenu = new SoundHitMenu(menuItems);
                        soundHitMenu.open(player);
                        return;
                     }
                  }
               }
            } else if (clickType == ClickType.RIGHT && this.plugin.getConfig().contains("hitsounds")) {
               for (String keyx : this.plugin.getConfig().getConfigurationSection("hitsounds.items").getKeys(false)) {
                  String path = "hitsounds.items." + keyx;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String soundName = this.plugin.getConfig().getString(path + ".sound");

                     try {
                        Sound sound = Sound.valueOf(soundName);
                        if (HolyCustom.getPlayerData(player, "hit_sound").equalsIgnoreCase(sound.name())) {
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
   public void on(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player) {
         Player damager = (Player)e.getDamager();
         if (this.isCriticalHit(damager) && HolyCustom.playerData.get(damager).containsKey("hit_sound")) {
            Sound sound = Sound.valueOf(HolyCustom.playerData.get(damager).get("hit_sound").toUpperCase());
            if (HolyCustom.playerData.get(damager).containsKey("sound_volume")) {
               Float volume = Float.valueOf(HolyCustom.playerData.get(damager).get("sound_volume"));
               Float pitch = Float.valueOf(HolyCustom.playerData.get(damager).getOrDefault("sound_pitch", "1.0"));
               damager.playSound(damager.getLocation(), sound, volume, pitch);
            } else {
               Float volume = Float.valueOf("1.0");
               Float pitch = Float.valueOf(HolyCustom.playerData.get(damager).getOrDefault("sound_pitch", "1.0"));
               damager.playSound(damager.getLocation(), sound, volume, pitch);
            }
         }
      }
   }

   private boolean isCriticalHit(Player player) {
      return player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && !player.isInsideVehicle();
   }
}
