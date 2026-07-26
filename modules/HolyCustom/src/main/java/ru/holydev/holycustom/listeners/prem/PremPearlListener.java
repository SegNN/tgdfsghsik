package ru.holydev.holycustom.listeners.prem;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.PremPearlMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.manager.ConfigManager;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holycustom.menus.prem.PremDeathMeMenu;
import ru.holydev.holycustom.menus.prem.PremDeathPlayerMenu;
import ru.holydev.holycustom.utils.HexUtil;

public class PremPearlListener implements Listener {
   private final HolyCustom plugin;
   private final ConfigManager configManager;

   public PremPearlListener(HolyCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void onSoundMenuClick(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof PremPearlMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getSlot());
      }
   }

   private void inventoryClick(Player player, int slot) {
      MenuItems menuItems = new MenuItems();
      switch (slot) {
         case 3:
            new PremDeathPlayerMenu(menuItems).open(player);
            return;
         case 4:
            new PremDeathMeMenu(menuItems).open(player);
            return;
         case 45:
            new MainMenu(menuItems).open(player);
            return;
         default:
            if (this.plugin.getConfig().contains("pearlparticles")) {
               for (String key : this.plugin.getConfig().getConfigurationSection("pearlparticles.items").getKeys(false)) {
                  String path = "pearlparticles.items." + key;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String particleName = this.plugin.getConfig().getString(path + ".particle");

                     try {
                        Particle particle = Particle.valueOf(particleName);
                        HolyCustom.playerData.get(player).put("pearl_particle", particle.name());
                        player.sendMessage(
                           HexUtil.translate(
                              this.plugin
                                 .getConfig()
                                 .getString("messages.acceptparticle")
                                 .replace("{particle}", this.plugin.getConfig().getString(path + ".name"))
                           )
                        );
                     } catch (IllegalArgumentException var10) {
                        player.sendMessage("§cОшибка: частица " + particleName + " не найдена!");
                     }

                     return;
                  }
               }
            }
      }
   }

   @EventHandler
   public void on(PlayerTeleportEvent e) {
      if (e.getCause() == TeleportCause.ENDER_PEARL) {
         Player player = e.getPlayer();
         if (HolyCustom.playerData.get(player).containsKey("pearl_particle")) {
            try {
               Particle particle = Particle.valueOf(HolyCustom.playerData.get(player).get("pearl_particle").toUpperCase());
               Location location = e.getTo().clone();
               player.spawnParticle(particle, location.add(0.0, 1.0, 0.0), new Random().nextInt(2) + 3, 0.5, 0.5, 0.5, 0.0);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }
      }
   }
}
