package ru.kotikstasika.eventship;

import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.eventship.anchor.RespawnAnchorManager;
import ru.kotikstasika.eventship.barrel.BarrelManager;
import ru.kotikstasika.eventship.commands.EventShipCommand;
import ru.kotikstasika.eventship.event.EventShipManager;
import ru.kotikstasika.eventship.hologram.HologramManager;
import ru.kotikstasika.eventship.listeners.EventShipListener;

public final class EventShip extends JavaPlugin {
   private EventShipManager eventManager;
   private HologramManager hologramManager;
   private BarrelManager.BarrelInventory barrelInventory;
   private RespawnAnchorManager.AnchorInventory anchorInventory;

   public EventShip() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.hologramManager = new HologramManager(this);
      this.eventManager = new EventShipManager(this);
      EventShipCommand commandExecutor = new EventShipCommand(this);
      this.getCommand("ship").setExecutor(commandExecutor);
      this.getCommand("ship").setTabCompleter(commandExecutor);
      this.getServer().getPluginManager().registerEvents(new EventShipListener(this), this);
      this.barrelInventory = new BarrelManager.BarrelInventory();
      this.anchorInventory = new RespawnAnchorManager.AnchorInventory();
   }

   public void onDisable() {
      if (this.eventManager != null && this.eventManager.isActive()) {
         this.eventManager.removeEvent();
      }

      if (this.hologramManager != null) {
         this.hologramManager.removeAllHolograms();
      }
   }

   public EventShipManager getEventManager() {
      return this.eventManager;
   }

   public BarrelManager.BarrelInventory getBarrelInventory() {
      return this.barrelInventory;
   }

   public RespawnAnchorManager.AnchorInventory getAnchorInventory() {
      return this.anchorInventory;
   }
}
