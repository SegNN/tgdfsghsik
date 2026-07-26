package ru.lolodimo4ka.holygoldspawner.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holygoldspawner.events.Config;

public class Sound {
   private final JavaPlugin plugin;
   private final Config configManager;

   public Sound(JavaPlugin plugin, Config configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   public void playPlacementSound(Player player) {
      player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
   }

   public void playWitherSound(Player player) {
      player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
   }
}
