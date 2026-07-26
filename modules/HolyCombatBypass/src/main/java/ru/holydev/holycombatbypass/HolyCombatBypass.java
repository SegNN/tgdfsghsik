package ru.holydev.holycombatbypass;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AntiRelog blocks every command while a player is in PvP and offers no permission escape:
 * its PlayerCommandPreprocessEvent handler only looks at `disable-commands-in-pvp`, the PvP
 * state and the command whitelist - `antirelog.bypass` is checked when PvP *starts*, never here.
 *
 * <p>That handler runs at the default NORMAL priority, so this listener sits at HIGHEST and
 * simply un-cancels the event for staff. Nothing in AntiRelog's own jar is modified, so this
 * survives updating it.
 */
public final class HolyCombatBypass extends JavaPlugin implements Listener {
   private static final String[] BYPASS = {"holycombat.commands", "antirelog.bypass"};

   @Override
   public void onEnable() {
      this.saveDefaultConfig();
      this.getServer().getPluginManager().registerEvents(this, this);
      this.getLogger().info("Enabled - staff can use commands during PvP");
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
   public void onCommand(PlayerCommandPreprocessEvent e) {
      if (!e.isCancelled()) {
         return;
      }

      Player player = e.getPlayer();
      for (String node : BYPASS) {
         if (player.hasPermission(node)) {
            e.setCancelled(false);
            return;
         }
      }
   }
}
