package ua.holydev.holybambuk;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holybambuk.command.BambukCommand;
import ua.holydev.holybambuk.item.BambukItem;
import ua.holydev.holybambuk.listener.BambukListener;
import ua.holydev.holybambuk.listener.BowReleaseListener;
import ua.holydev.holybambuk.manager.GrabManager;

public final class HolyBambuk extends JavaPlugin {
   private BambukItem item;
   private GrabManager grabManager;

   public HolyBambuk() {
   }

   public void onLoad() {
      PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
      PacketEvents.getAPI().load();
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.item = new BambukItem(this);
      this.grabManager = new GrabManager(this, this.item);
      this.getServer().getPluginManager().registerEvents(new BambukListener(this, this.item, this.grabManager), this);
      BambukCommand bambukCmd = new BambukCommand(this.item);
      this.getCommand("bambuk").setExecutor(bambukCmd);
      this.getCommand("bambuk").setTabCompleter(bambukCmd);
      PacketEvents.getAPI().getEventManager().registerListener(new BowReleaseListener(this, this.grabManager));

      try {
         PacketEvents.getAPI().init();
      } catch (Throwable var3) {
         this.getLogger().warning("PacketEvents init failed (likely due to plugin reload with online players): " + var3.getMessage());
         this.getLogger().warning("Perform a full server restart for PacketEvents to inject online players correctly.");
      }
   }

   public void onDisable() {
      if (this.grabManager != null) {
         this.grabManager.releaseAll();
      }

      PacketEvents.getAPI().terminate();
   }
}
