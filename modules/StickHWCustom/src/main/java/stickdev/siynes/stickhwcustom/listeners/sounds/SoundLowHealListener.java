package stickdev.siynes.stickhwcustom.listeners.sounds;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.SoundLowHealMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.manager.ConfigManager;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundDeathMeMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundDeathPlayerMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundHitMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundLowHealMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundVolumeMenu;

public class SoundLowHealListener implements Listener {
   private final StickHWCustom plugin;
   private final ConfigManager configManager;

   public SoundLowHealListener(StickHWCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof SoundLowHealMenuHolder) {
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
         case 3:
            new SoundDeathMeMenu(menuItems).open(player);
            return;
         case 5:
            new SoundDeathPlayerMenu(menuItems).open(player);
            return;
         case 45:
            new MainMenu(menuItems).open(player);
            return;
         default:
            if (clickType == ClickType.LEFT) {
               if (this.plugin.getConfig().contains("lowheal")) {
                  for (String key : this.plugin.getConfig().getConfigurationSection("lowheal.items").getKeys(false)) {
                     String path = "lowheal.items." + key;
                     int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                     if (configSlot == slot) {
                        String soundName = this.plugin.getConfig().getString(path + ".sound");

                        try {
                           Sound sound = Sound.valueOf(soundName);
                           if (StickHWCustom.getPlayerData(player, "lowhealth_sound").equalsIgnoreCase(sound.name())) {
                              StickHWCustom.playerData.get(player).remove("lowhealth_sound");
                           } else {
                              StickHWCustom.playerData.get(player).put("lowhealth_sound", sound.name());
                              StickHWCustom.playerData.get(player).put("sound_volume", "1.0");
                              StickHWCustom.playerData.get(player).put("sound_pitch", "1.0");
                              player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                           }
                        } catch (IllegalArgumentException var12) {
                           player.sendMessage("§cОшибка: звук " + soundName + " не найден!");
                        }

                        SoundLowHealMenu soundHitMenu = new SoundLowHealMenu(menuItems);
                        soundHitMenu.open(player);
                        return;
                     }
                  }
               }
            } else if (clickType == ClickType.RIGHT && this.plugin.getConfig().contains("lowheal")) {
               for (String keyx : this.plugin.getConfig().getConfigurationSection("lowheal.items").getKeys(false)) {
                  String path = "lowheal.items." + keyx;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String soundName = this.plugin.getConfig().getString(path + ".sound");

                     try {
                        Sound sound = Sound.valueOf(soundName);
                        if (StickHWCustom.getPlayerData(player, "lowhealth_sound").equalsIgnoreCase(sound.name())) {
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
   public void onLowHealth(EntityDamageEvent e) {
      if (e.getEntity() instanceof Player) {
         final Player player = (Player)e.getEntity();
         (new BukkitRunnable() {
               public void run() {
                  if (!(player.getHealth() > 4.5)
                     && StickHWCustom.playerData.get(player) != null
                     && StickHWCustom.playerData.get(player).containsKey("lowhealth_sound")) {
                     try {
                        Sound sound = Sound.valueOf(StickHWCustom.playerData.get(player).get("lowhealth_sound").toUpperCase());
                        if (StickHWCustom.playerData.get(player).containsKey("sound_volume")) {
                           Float volume = Float.valueOf(StickHWCustom.playerData.get(player).get("sound_volume"));
                           Float pitch = Float.valueOf(StickHWCustom.playerData.get(player).getOrDefault("sound_pitch", "1.0"));
                           player.playSound(player.getLocation(), sound, volume, pitch);
                        } else {
                           Float volume = Float.valueOf("1.0");
                           Float pitch = Float.valueOf(StickHWCustom.playerData.get(player).getOrDefault("sound_pitch", "1.0"));
                           player.playSound(player.getLocation(), sound, volume, pitch);
                        }
                     } catch (Exception var4) {
                        var4.printStackTrace();
                     }
                  }
               }
            })
            .runTaskLater(this.plugin, 1L);
      }
   }
}
