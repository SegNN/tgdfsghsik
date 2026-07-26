package org.dimasik.holyenchantments;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.holyenchantments.commands.EnchantCommand;
import org.dimasik.holyenchantments.enchantscore.EnchantManager;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;
import org.dimasik.holyenchantments.events.Anvil;
import org.dimasik.holyenchantments.events.ArmorHandlers;
import org.dimasik.holyenchantments.events.BreakHandlers;
import org.dimasik.holyenchantments.events.DamageHandlers;
import org.dimasik.holyenchantments.events.EnchantItem;
import org.dimasik.holyenchantments.events.InteractItemHandlers;
import org.dimasik.holyenchantments.events.TridentListener;
import org.dimasik.holyenchantments.grindstone.GrindstoneListener;
import org.dimasik.holyenchantments.grindstone.GrindstoneMenu;
import org.dimasik.holyenchantments.utils.Schedulers;
import org.jetbrains.annotations.NotNull;

public final class HolyEnchantments extends JavaPlugin {
   private static HolyEnchantments instance;
   private EnchantManager enchantManager;

   public HolyEnchantments() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      instance = this;
      Schedulers.load(this);
      this.enchantManager = new EnchantManager(this);
      EnchantsMap enchantsMap = new EnchantsMap(this);
      new EnchantCommand(this.enchantManager);
      NamespacedKey spawnerKey = new NamespacedKey(this, "holyenchantments-spawner-key");
      PluginManager pluginManager = Bukkit.getPluginManager();
      pluginManager.registerEvents(new Anvil(this.enchantManager), this);
      pluginManager.registerEvents(new EnchantItem(this), this);
      pluginManager.registerEvents(new BreakHandlers(enchantsMap, spawnerKey), this);
      pluginManager.registerEvents(new DamageHandlers(enchantsMap), this);
      pluginManager.registerEvents(new ArmorHandlers(this, enchantsMap), this);
      pluginManager.registerEvents(new InteractItemHandlers(enchantsMap), this);
      pluginManager.registerEvents(new TridentListener(enchantsMap), this);
      pluginManager.registerEvents(new GrindstoneListener(), this);
      this.getCommand("grindstone").setExecutor(new HolyEnchantments.CMDExecutor());
   }

   public void onDisable() {
   }

   public static HolyEnchantments getInstance() {
      return instance;
   }

   public EnchantManager getEnchantManager() {
      return this.enchantManager;
   }

   private class CMDExecutor implements CommandExecutor {
      private CMDExecutor() {
      }

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (sender instanceof Player) {
            new GrindstoneMenu((Player)sender, null);
         }

         return false;
      }
   }
}
