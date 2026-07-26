package ua.fichadev.fichabambuk.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ua.fichadev.fichabambuk.manager.GrabManager;

public final class BowReleaseListener extends PacketListenerAbstract {
   private final Plugin plugin;
   private final GrabManager grabManager;

   public BowReleaseListener(Plugin plugin, GrabManager grabManager) {
      super(PacketListenerPriority.NORMAL);
      this.plugin = plugin;
      this.grabManager = grabManager;
   }

   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == Client.PLAYER_DIGGING) {
         WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
         if (wrapper.getAction() == DiggingAction.RELEASE_USE_ITEM) {
            if (event.getPlayer() instanceof Player) {
               Player player = (Player)event.getPlayer();
               Bukkit.getScheduler().runTask(this.plugin, () -> this.grabManager.release(player));
            }
         }
      }
   }
}
