package org.impldev.holycore;

import com.earth2me.essentials.Essentials;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.checker.ShulkerTotemChecker;
import org.impldev.holycore.commands.Bc;
import org.impldev.holycore.commands.Links;
import org.impldev.holycore.commands.Suicide;
import org.impldev.holycore.database.DatabaseManager;
import org.impldev.holycore.extensions.AfkExtension;
import org.impldev.holycore.extensions.CommandExtension;
import org.impldev.holycore.extensions.CraftingExtension;
import org.impldev.holycore.extensions.EntityExtension;
import org.impldev.holycore.extensions.ExperienceExtension;
import org.impldev.holycore.extensions.InventoryExtension;
import org.impldev.holycore.extensions.JoinExtension;
import org.impldev.holycore.extensions.ObjectExtension;
import org.impldev.holycore.extensions.PlayerExtension;
import org.impldev.holycore.extensions.RegionCommandExtension;
import org.impldev.holycore.extensions.SpawnExtension;
import org.impldev.holycore.extensions.SpawnerLimitExtension;
import org.impldev.holycore.extensions.TridentExtension;
import org.impldev.holycore.rewards.Rewards;

public final class HolyCore extends JavaPlugin {
   private static HolyCore instance;
   public List<NamespacedKey> recipeKeys = new ArrayList<>();
   private DatabaseManager databaseManager;

   public HolyCore() {
   }

   public void onEnable() {
      instance = this;
      this.setupRecipes();
      this.saveDefaultConfig();
      this.databaseManager = new DatabaseManager(this);
      this.databaseManager.initializeDatabase();
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
      Essentials ess = (Essentials)this.getServer().getPluginManager().getPlugin("Essentials");
      if (ess != null) {
         this.getServer().getPluginManager().registerEvents(new ObjectExtension(this, ess), this);
      } else {
         this.getLogger().warning("Essentials not found! Disabling pickup restriction feature.");
      }

      this.getServer().getPluginManager().registerEvents(new InventoryExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new JoinExtension(this, this.databaseManager), this);
      this.getServer().getPluginManager().registerEvents(new EntityExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new CraftingExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new ExperienceExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new SpawnExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new SpawnerLimitExtension(), this);
      this.getServer().getPluginManager().registerEvents(new Rewards(this, this.databaseManager), this);
      this.getServer().getPluginManager().registerEvents(new CommandExtension(), this);
      this.getServer().getPluginManager().registerEvents(new PlayerExtension(), this);
      this.getServer().getPluginManager().registerEvents(new RegionCommandExtension(this), this);
      this.getServer().getPluginManager().registerEvents(new TridentExtension(), this);
      AfkExtension afkExtension = new AfkExtension(this);
      this.getServer().getPluginManager().registerEvents(afkExtension, this);
      this.getCommand("afk").setExecutor(afkExtension);
      new ShulkerTotemChecker(this).runTaskTimer(this, 0L, 50L);
      this.getCommand("suicide").setExecutor(new Suicide(this));
      this.getCommand("broadcast").setExecutor(new Bc(this));
      this.getCommand("links").setExecutor(new Links(this));
   }

   private void setupRecipes() {
      NamespacedKey gapleKey = new NamespacedKey(this, "gapple");
      ShapedRecipe gapleRecipe = new ShapedRecipe(gapleKey, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
      gapleRecipe.shape(new String[]{"AAA", "ABA", "AAA"});
      gapleRecipe.setIngredient('A', Material.GOLD_BLOCK);
      gapleRecipe.setIngredient('B', Material.APPLE);
      Bukkit.addRecipe(gapleRecipe);
      this.recipeKeys.add(gapleKey);
      NamespacedKey debrisKey = new NamespacedKey(this, "ancientdebris");
      ShapedRecipe debrisRecipe = new ShapedRecipe(debrisKey, new ItemStack(Material.ANCIENT_DEBRIS, 1));
      debrisRecipe.shape(new String[]{"AAA", "ABA", "AAA"});
      debrisRecipe.setIngredient('A', Material.NETHERRACK);
      debrisRecipe.setIngredient('B', Material.NETHERITE_SCRAP);
      Bukkit.addRecipe(debrisRecipe);
      this.recipeKeys.add(debrisKey);
   }

   public void removeRecipes() {
      for (NamespacedKey key : this.recipeKeys) {
         Bukkit.removeRecipe(key);
      }

      this.recipeKeys.clear();
   }

   public void onDisable() {
      this.removeRecipes();
   }

   @Generated
   public static HolyCore getInstance() {
      return instance;
   }
}
