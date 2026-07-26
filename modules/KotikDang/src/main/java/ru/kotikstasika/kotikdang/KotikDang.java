package ru.kotikstasika.kotikdang;

import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.kotikdang.command.Commands;
import ru.kotikstasika.kotikdang.dang.items.Items;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;
import ru.kotikstasika.kotikdang.dang.types.DangUniversal;

public class KotikDang extends JavaPlugin {
   public static KotikDang plugin;
   private Items items;
   private DangGrief dangGrief;
   private DangUniversal dangUniversal;

   public KotikDang() {
   }

   public void onEnable() {
      plugin = this;
      this.saveDefaultConfig();
      this.items = new Items(this);
      this.dangGrief = new DangGrief(this);
      this.dangUniversal = new DangUniversal(this, this.items);
      Commands commands = new Commands(this, this.items, this.dangUniversal, this.dangGrief);
      this.getCommand("kotikdang").setExecutor(commands);
      this.getCommand("kotikdang").setTabCompleter(commands);
      this.getServer().getPluginManager().registerEvents(this.items, this);
      this.getServer().getPluginManager().registerEvents(this.dangGrief, this);
      this.getServer().getPluginManager().registerEvents(this.dangUniversal, this);
   }

   public void onDisable() {
      if (this.dangUniversal != null) {
         this.dangUniversal.stopAllSessions();
      }
   }

   public Items getItemsInstance() {
      return this.items;
   }

   public DangGrief getDangGriefInstance() {
      return this.dangGrief;
   }

   public DangUniversal getDangUniversalInstance() {
      return this.dangUniversal;
   }

   public int removeGrifAlready() {
      return this.dangGrief != null ? this.dangGrief.removeGrifAlready() : 0;
   }
}
