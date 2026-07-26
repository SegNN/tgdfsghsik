package ru.kotikstasika.kotiktotemrune;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.kotiktotemrune.command.CommandGive;
import ru.kotikstasika.kotiktotemrune.util.AnvilUse;
import ru.kotikstasika.kotiktotemrune.util.TotemUseListener;

public final class KotikTotemRune extends JavaPlugin {
   public static KotikTotemRune instance;

   public KotikTotemRune() {
   }

   public void onEnable() {
      instance = this;
      Bukkit.getPluginCommand("kotiktotemrune").setExecutor(new CommandGive());
      this.getServer().getPluginManager().registerEvents(new AnvilUse(this), this);
      this.getServer().getPluginManager().registerEvents(new TotemUseListener(this), this);
   }

   public static KotikTotemRune getInstance() {
      return instance;
   }
}
