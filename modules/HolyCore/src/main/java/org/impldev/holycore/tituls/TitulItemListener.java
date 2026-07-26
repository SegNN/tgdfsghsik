package org.impldev.holycore.tituls;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.impldev.holycore.utils.Parser;

/**
 * Активация титула-предмета по правому клику.
 *
 * <p>Право в предмете хранится последней строкой лора, а не в PDC: так предмет переживает
 * передачу через сундуки, аукцион и обмен между игроками - именно ради обмена его и заводили.
 */
public final class TitulItemListener implements Listener {
   private final TitulRegistry registry;

   public TitulItemListener(TitulRegistry registry) {
      this.registry = registry;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
         return;
      }

      ItemStack item = event.getItem();
      if (item == null || item.getType() != Material.NAME_TAG || !item.hasItemMeta()) {
         return;
      }

      List<String> lore = item.getItemMeta().getLore();
      if (lore == null || lore.isEmpty()) {
         return;
      }

      String last = ChatColor.stripColor(lore.get(lore.size() - 1)).trim();
      Titul titul = this.registry.byPermission(last);
      if (titul == null) {
         return;
      }

      Player player = event.getPlayer();
      event.setCancelled(true);

      if (player.hasPermission(titul.permission())) {
         player.sendMessage(Parser.color("&r&x&f&f&2&2&2&2▶ &r&fЭтот титул у вас уже есть."));
         return;
      }

      Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
            "lp user " + player.getName() + " permission set " + titul.permission() + " true");
      item.setAmount(item.getAmount() - 1);
      player.sendMessage(Parser.color("&r&x&f&f&7&0&0&0▶ &r&fТитул добавлен в &r&x&f&f&7&0&0&0/tituls&r&f."));
      player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
   }
}
