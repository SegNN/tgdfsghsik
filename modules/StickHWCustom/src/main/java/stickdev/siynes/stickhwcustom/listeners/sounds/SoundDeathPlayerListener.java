package stickdev.siynes.stickhwcustom.listeners.sounds;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.SoundDeathPlayerHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.manager.ConfigManager;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundDeathPlayerMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundHitMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundLowHealMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundVolumeMenu;

public class SoundDeathPlayerListener implements Listener {
   private final StickHWCustom plugin;
   private final ConfigManager configManager;

   public SoundDeathPlayerListener(StickHWCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof SoundDeathPlayerHolder) {
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
               if (this.plugin.getConfig().contains("deathplayer")) {
                  for (String key : this.plugin.getConfig().getConfigurationSection("deathplayer.items").getKeys(false)) {
                     String path = "deathplayer.items." + key;
                     int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                     if (configSlot == slot) {
                        String soundName = this.plugin.getConfig().getString(path + ".sound");

                        try {
                           Sound sound = Sound.valueOf(soundName);
                           if (StickHWCustom.getPlayerData(player, "kill_sound").equalsIgnoreCase(sound.name())) {
                              StickHWCustom.playerData.get(player).remove("kill_sound");
                           } else {
                              StickHWCustom.playerData.get(player).put("kill_sound", sound.name());
                              StickHWCustom.playerData.get(player).put("sound_volume", "1.0");
                              StickHWCustom.playerData.get(player).put("sound_pitch", "1.0");
                              player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                           }
                        } catch (IllegalArgumentException var12) {
                           player.sendMessage("§cОшибка: звук " + soundName + " не найден!");
                        }

                        SoundDeathPlayerMenu soundHitMenu = new SoundDeathPlayerMenu(menuItems);
                        soundHitMenu.open(player);
                        return;
                     }
                  }
               }
            } else if (clickType == ClickType.RIGHT && this.plugin.getConfig().contains("deathplayer")) {
               for (String keyx : this.plugin.getConfig().getConfigurationSection("deathplayer.items").getKeys(false)) {
                  String path = "deathplayer.items." + keyx;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String soundName = this.plugin.getConfig().getString(path + ".sound");

                     try {
                        Sound sound = Sound.valueOf(soundName);
                        if (StickHWCustom.getPlayerData(player, "kill_sound").equalsIgnoreCase(sound.name())) {
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
      Player damager = e.getEntity().getKiller();
      if (damager != null && StickHWCustom.playerData.get(damager).containsKey("kill_sound")) {
         Sound sound = Sound.valueOf(StickHWCustom.playerData.get(damager).get("kill_sound").toUpperCase());
         if (StickHWCustom.playerData.get(damager).containsKey("sound_volume")) {
            Float volume = Float.valueOf(StickHWCustom.playerData.get(damager).get("sound_volume"));
            Float pitch = Float.valueOf(StickHWCustom.playerData.get(damager).getOrDefault("sound_pitch", "1.0"));
            damager.playSound(damager.getLocation(), sound, volume, pitch);
         } else {
            Float volume = Float.valueOf("1.0");
            Float pitch = Float.valueOf(StickHWCustom.playerData.get(damager).getOrDefault("sound_pitch", "1.0"));
            damager.playSound(damager.getLocation(), sound, volume, pitch);
         }
      }
   }
}
