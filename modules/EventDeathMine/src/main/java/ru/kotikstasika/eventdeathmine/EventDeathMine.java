package ru.kotikstasika.eventdeathmine;

import lombok.Generated;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.eventdeathmine.command.DeathMineCommand;
import ru.kotikstasika.eventdeathmine.listener.DeathMineListener;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;

public final class EventDeathMine extends JavaPlugin {
   private DeathMineManager manager;
   private EventDeathMine instance;

   public EventDeathMine() {
   }

   public void onEnable() {
      this.instance = this;
      this.manager = new DeathMineManager(this);
      DeathMineCommand cmd = new DeathMineCommand(this.manager);
      this.getCommand("deathmine").setExecutor(cmd);
      this.getCommand("deathmine").setTabCompleter(cmd);
      this.getServer().getPluginManager().registerEvents(new DeathMineListener(this.manager), this);
      this.manager.startBossBarUpdater();
   }

   public void onDisable() {
      this.manager.removeMine(null);
      this.manager.stopBossBarUpdater();
   }

   @Generated
   public EventDeathMine getInstance() {
      return this.instance;
   }
}
