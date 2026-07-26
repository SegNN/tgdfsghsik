package ru.holydev.holycustom.listeners.prem;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.holders.PremDeathPlayerMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.manager.ConfigManager;
import ru.holydev.holycustom.menus.MainMenu;
import ru.holydev.holycustom.menus.prem.PremDeathMeMenu;
import ru.holydev.holycustom.menus.prem.PremPearlMenu;
import ru.holydev.holycustom.utils.HexUtil;

public class PremDeathPlayerListener implements Listener {
   private final HolyCustom plugin;
   private final ConfigManager configManager;

   public PremDeathPlayerListener(HolyCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   @EventHandler
   public void onSoundMenuClick(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof PremDeathPlayerMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getSlot());
      }
   }

   private void inventoryClick(Player player, int slot) {
      MenuItems menuItems = new MenuItems();
      switch (slot) {
         case 2:
            new PremPearlMenu(menuItems).open(player);
            return;
         case 4:
            new PremDeathMeMenu(menuItems).open(player);
            return;
         case 45:
            new MainMenu(menuItems).open(player);
            return;
         default:
            if (this.plugin.getConfig().contains("killparticles")) {
               for (String key : this.plugin.getConfig().getConfigurationSection("killparticles.items").getKeys(false)) {
                  String path = "killparticles.items." + key;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     String particleName = this.plugin.getConfig().getString(path + ".particle");

                     try {
                        Particle particle = Particle.valueOf(particleName);
                        HolyCustom.playerData.get(player).put("kill_particle", particle.name());
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
   public void on(PlayerDeathEvent e) {
      Player killer = e.getEntity().getKiller();
      if (killer != null && HolyCustom.playerData.get(killer).containsKey("kill_particle")) {
         try {
            Particle particle = Particle.valueOf(HolyCustom.playerData.get(killer).get("kill_particle").toUpperCase());
            Location location = e.getEntity().getLocation().clone();
            killer.spawnParticle(particle, location.add(0.0, 1.0, 0.0), new Random().nextInt(2) + 3, 0.5, 0.5, 0.5, 0.0);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }
}
