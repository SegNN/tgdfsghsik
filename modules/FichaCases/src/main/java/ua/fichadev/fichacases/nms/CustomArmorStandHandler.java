package ua.fichadev.fichacases.nms;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

public class CustomArmorStandHandler implements Listener {
   public CustomArmorStandHandler(Plugin plugin) {
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   /**
    * No-op. The 1.16.5 stands were packet only, so they had to be re-sent to every player that
    * joined or switched world. They are real server side entities now and the entity tracker
    * takes care of that on its own.
    */
   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
   }

   /**
    * No-op, see {@link #onJoin(PlayerJoinEvent)}.
    */
   @EventHandler
   public void onWorldChange(PlayerChangedWorldEvent e) {
   }

   /**
    * The stands are spawned non persistent so a hard crash cannot leave them in the region files,
    * but that also means unloading their chunk deletes them. Recreate them when the chunk returns,
    * otherwise a case title would disappear for good the first time nobody is standing near it.
    */
   @EventHandler
   public void onChunkLoad(ChunkLoadEvent e) {
      for (CustomArmorStand stand : new ArrayList<>(CustomArmorStand.handlerList)) {
         if (stand.isAt(e.getChunk())) {
            stand.respawnIfMissing();
         }
      }
   }

   public void onDisable() {
      for (CustomArmorStand customArmorStand : new ArrayList<>(CustomArmorStand.handlerList)) {
         customArmorStand.remove();
      }
   }
}
