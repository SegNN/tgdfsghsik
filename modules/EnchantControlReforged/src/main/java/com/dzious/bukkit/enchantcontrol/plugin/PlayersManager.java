package com.dzious.bukkit.enchantcontrol.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayersManager {
   Map<UUID, EnchantmentPlayer> players = new HashMap<>();

   public PlayersManager() {
   }

   public void registerPlayer(UUID uuid) {
      this.players.put(uuid, new EnchantmentPlayer());
   }

   public void removePlayer(UUID uuid) {
      this.players.get(uuid);
      this.players.remove(uuid);
   }

   public EnchantmentPlayer getPlayer(UUID uuid) {
      return this.players.get(uuid);
   }
}
