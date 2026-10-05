package org.impldev.holyclans;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ClanListener implements Listener {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9&]+$");
   private final HolyClans plugin;

   public ClanListener(HolyClans plugin) {
      this.plugin = plugin;
   }

   @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
   public void onDamage(EntityDamageByEntityEvent event) {
      if (!(event.getEntity() instanceof Player victim)) return;
      Player attacker = this.getAttacker(event);
      if (attacker == null) return;
      if (attacker.equals(victim)) return;

      Clan aClan = this.plugin.data().byPlayer(attacker.getUniqueId());
      if (aClan == null) return;
      Clan vClan = this.plugin.data().byPlayer(victim.getUniqueId());
      if (vClan != aClan) return;

      if (!aClan.pvp()) {
         event.setCancelled(true);
      }
   }

   private Player getAttacker(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player p) return p;
      if (event.getDamager() instanceof org.bukkit.entity.Projectile proj
            && proj.getShooter() instanceof Player p) return p;
      return null;
   }

   @SuppressWarnings("deprecation")
   @EventHandler(priority = EventPriority.LOWEST)
   public void onChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();

      if (this.plugin.awaitingName().contains(uuid)) {
         event.setCancelled(true);
         this.plugin.awaitingName().remove(uuid);
         String name = event.getMessage();
         Bukkit.getScheduler().runTask(this.plugin, () -> this.handleCreateName(player, name));
         return;
      }

      if (this.plugin.clanChatActive().contains(uuid)) {
         Clan clan = this.plugin.data().byPlayer(uuid);
         if (clan != null) {
            event.setCancelled(true);
            String text = event.getMessage();
            Component msg = Texts.color("&r&x&0&0&d&8&f&fКлан-Чат &r&x&e&7&e&7&e&7| &r&x&0&0&d&8&f&f" + player.getName() + "&r&x&0&0&d&8&f&f: &r&f" + text);
            for (UUID member : clan.members().keySet()) {
               Player online = Bukkit.getPlayer(member);
               if (online != null) {
                  online.sendMessage(msg);
               }
            }
         }
      }
   }

   private void handleCreateName(Player player, String name) {
      int maxLen = this.plugin.getConfig().getInt("max-name-length", 12);

      if (name.length() > maxLen || !NAME_PATTERN.matcher(name).matches()) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fНекорректное название клана."));
         return;
      }

      if (this.plugin.data().byName(name) != null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТакое название клана уже занято, попробуйте другое."));
         return;
      }

      long cost = this.plugin.getConfig().getLong("creation-cost", 5_000_000L);
      if (this.plugin.economy() == null || this.plugin.economy().getBalance(player) < cost) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вас недостаточно средств."));
         return;
      }

      if (this.plugin.data().byPlayer(player.getUniqueId()) != null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВы уже состоите в клане."));
         return;
      }

      this.plugin.economy().withdrawPlayer(player, cost);
      Clan clan = this.plugin.data().create(name, player.getUniqueId(), player.getName());
      if (clan == null) {
         this.plugin.economy().depositPlayer(player, cost);
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТакое название клана уже занято, попробуйте другое."));
         return;
      }

      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fВы создали клан."));
   }
}
