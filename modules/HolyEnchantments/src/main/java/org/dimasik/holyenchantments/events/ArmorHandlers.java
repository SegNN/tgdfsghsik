package org.dimasik.holyenchantments.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;
import org.dimasik.holyenchantments.enchantscore.levels.IntLevel;
import org.dimasik.holyenchantments.utils.EncoderSound;
import org.dimasik.holyenchantments.utils.Random;

public class ArmorHandlers implements Listener {
   private final HolyEnchantments plugin;
   private final EnchantsMap enchantsMap;
   private final HashMap<UUID, Integer> lavaMoveSet = new HashMap<>();
   private final HashMap<Player, AbstractLevel> jumperSet = new HashMap<>();
   private final List<Block> lavaMoveBlocks = new ArrayList<>();

   public ArmorHandlers(HolyEnchantments plugin, EnchantsMap enchantsMap) {
      this.plugin = plugin;
      this.enchantsMap = enchantsMap;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onChangeArmor(PlayerArmorChangeEvent event) {
      Player player = event.getPlayer();
      this.lavaMoveSet.remove(player.getUniqueId());
      this.jumperSet.remove(player);
      this.checkItems(player);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onMove(PlayerMoveEvent event) {
      if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
         UUID uuid = event.getPlayer().getUniqueId();
         if (this.lavaMoveSet.containsKey(uuid)) {
            Block center = event.getTo().getBlock().getRelative(BlockFace.DOWN);
            int durability = 0;

            for (int x = -this.lavaMoveSet.get(uuid); x <= this.lavaMoveSet.get(uuid); x++) {
               for (int z = -this.lavaMoveSet.get(uuid); z <= this.lavaMoveSet.get(uuid); z++) {
                  Block block = center.getRelative(x, 0, z);
                  if (block.getType() == Material.LAVA && (block.getY() == 255 || !(block.getRelative(BlockFace.UP).getBlockData() instanceof Slab))) {
                     block.setType(Material.OBSIDIAN);
                     durability++;
                  }
               }
            }

            ItemStack itemStack = event.getPlayer().getInventory().getBoots();
            if (itemStack == null) {
               return;
            }
            byte var9 = switch (itemStack.getEnchantmentLevel(Enchantment.DURABILITY)) {
               case 0 -> 0;
               case 1 -> 25;
               case 2 -> 36;
               case 3 -> 45;
               default -> 50;
            };
            if (durability > 0) {
               for (int k = 0; k < durability; k++) {
                  if (Random.getPercent() > var9) {
                     itemStack.setDurability((short)(itemStack.getDurability() + 1));
                  }
               }

               if (itemStack.getDurability() > itemStack.getType().getMaxDurability()) {
                  event.getPlayer().getInventory().setBoots((ItemStack)null);
               } else {
                  event.getPlayer().getInventory().setBoots(itemStack);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent event) {
      if (this.lavaMoveBlocks.contains(event.getBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onJoin(PlayerJoinEvent event) {
      this.checkItems(event.getPlayer());
   }

   private void checkItems(Player player) {
      if (player.getEquipment() != null) {
         for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack != null && itemStack.hasItemMeta()) {
               PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
               if (!persistentDataContainer.isEmpty() && persistentDataContainer.has(this.enchantsMap.lavaMove.getNamespacedKey(), PersistentDataType.INTEGER)) {
                  AbstractLevel level = this.enchantsMap.lavaMove.getLevel(persistentDataContainer);
                  if (level != null && level.random()) {
                     EncoderSound.playSound(player, this.enchantsMap.lavaMove.getSoundAlone());
                     EncoderSound.playSound(player.getLocation(), this.enchantsMap.lavaMove.getSoundAround());
                     this.lavaMoveSet.put(player.getUniqueId(), ((IntLevel)level).getValue());
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.lavaMoveSet.remove(player.getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onKick(PlayerKickEvent event) {
      Player player = event.getPlayer();
      this.lavaMoveSet.remove(player.getUniqueId());
   }
}
