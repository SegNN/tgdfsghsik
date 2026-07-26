package org.dimasik.antilagmachine;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiLagMachine extends JavaPlugin implements Listener {
   public HashMap<Location, Integer> blocks = new HashMap<>();
   public HashMap<Player, Integer> players = new HashMap<>();
   private HashMap<UUID, Long> lastUseTimes = new HashMap<>();
   private HashMap<Block, Long> lastRedstoneTimes = new HashMap<>();
   private static final long COOLDOWN = 500L;
   private static final long REDSTONECOOLDOWN = 250L;

   public AntiLagMachine() {
   }

   public void onEnable() {
      this.getServer().getPluginManager().registerEvents(this, this);
   }

   public boolean isRedstoneBlock(Block block) {
      return block.getType() == Material.LEVER
         || block.getType().toString().contains("BUTTON")
         || block.getType().toString().contains("DOOR")
         || block.getType() == Material.REDSTONE_TORCH
         || block.getType() == Material.LECTERN;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
         Block clickedBlock = event.getClickedBlock();
         if (this.isRedstoneBlock(clickedBlock)) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (this.lastUseTimes.containsKey(playerId)) {
               long timeElapsed = currentTime - this.lastUseTimes.get(playerId);
               if (timeElapsed < 500L) {
                  event.setCancelled(true);
                  return;
               }
            }

            this.lastUseTimes.put(playerId, currentTime);
         }
      }
   }

   @EventHandler
   public void onRedstoneTick(BlockRedstoneEvent event) {
      if (Bukkit.getTPS()[0] <= 12.0) {
         this.blocks.put(event.getBlock().getLocation(), this.blocks.getOrDefault(event.getBlock().getLocation(), 0) + 1);
         if (this.blocks.get(event.getBlock().getLocation()) == 4 || this.blocks.get(event.getBlock().getLocation()) % 16 == 0) {
            for (Player p : event.getBlock().getWorld().getPlayers()) {
               if (event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5).toVector().distance(p.getLocation().toVector()) <= 15.0) {
                  this.players.put(p, this.players.getOrDefault(p, 0) + 1);
                  if (this.players.get(p) == 2) {
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shame ban " + p.getName());
                  }
               }
            }
         }

         event.setNewCurrent(0);
      }
   }

   @EventHandler
   public void onRedstoneTick2(BlockRedstoneEvent event) {
      Block block = event.getBlock();
      if (block.getType() != Material.DISPENSER && block.getType() != Material.DROPPER) {
         long currentTime = System.currentTimeMillis();
         if (this.lastRedstoneTimes.containsKey(block)) {
            long timeElapsed = currentTime - this.lastRedstoneTimes.get(block);
            if (timeElapsed < 250L) {
               event.setNewCurrent(0);
               return;
            }
         }

         this.lastRedstoneTimes.put(block, currentTime);
      }
   }

   public void onDisable() {
   }
}
