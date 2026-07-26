package ru.kotikstasika.holysfers;

import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.holysfers.commands.Commands;
import ru.kotikstasika.holysfers.sferomant.listeners.FurnaceEvent;
import ru.kotikstasika.holysfers.sferomant.listeners.MenuClick;
import ru.kotikstasika.holysfers.utils.Utils;

public final class HolySfers extends JavaPlugin {
   private static HolySfers instance;
   private Utils utils;
   private Commands commands;

   public HolySfers() {
   }

   public void onEnable() {
      instance = this;
      this.setUtils();
      this.registerCommands();
      this.registerListeners();
      this.registerFurnace();
   }

   void setUtils() {
      this.utils = new Utils();
      this.utils.startSphereTask();
   }

   void registerCommands() {
      this.commands = new Commands();
      this.getCommand("holysfers").setExecutor(this.commands);
      this.getCommand("holysfers").setTabCompleter(this.commands);
      this.getCommand("sferomant").setExecutor(this.commands);
   }

   void registerListeners() {
      this.getServer().getPluginManager().registerEvents(this.utils, this);
      this.getServer().getPluginManager().registerEvents(new MenuClick(), this);
      this.getServer().getPluginManager().registerEvents(new FurnaceEvent(), this);
   }

   void registerFurnace() {
      try {
         FurnaceRecipe headRecipe = new FurnaceRecipe(new NamespacedKey(this, "hs_head_smelt"), this.utils.createShardItem(12), Material.PLAYER_HEAD, 3.0F, 400);
         this.getServer().addRecipe(headRecipe);
      } catch (Throwable var2) {
         this.getLogger().warning("Не удалось зарегистрировать рецепты плавки: " + var2.getMessage());
      }
   }

   @Generated
   public static HolySfers getInstance() {
      return instance;
   }

   @Generated
   public Utils getUtils() {
      return this.utils;
   }

   @Generated
   public Commands getCommands() {
      return this.commands;
   }
}
