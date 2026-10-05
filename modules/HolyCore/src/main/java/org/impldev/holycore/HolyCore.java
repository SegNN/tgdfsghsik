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
import org.impldev.holycore.commands.BalTop;
import org.impldev.holycore.commands.Bc;
import org.impldev.holycore.commands.HealthIndicator;
import org.impldev.holycore.commands.Links;
import org.impldev.holycore.commands.Suicide;
import org.impldev.holycore.commands.WExtend;
import org.impldev.holycore.boosters.BoosterCommand;
import org.impldev.holycore.tituls.TitulCommand;
import org.impldev.holycore.tituls.TitulItemListener;
import org.impldev.holycore.tituls.TitulMenu;
import org.impldev.holycore.tituls.TitulRegistry;
import org.impldev.holycore.boosters.BoosterData;
import org.impldev.holycore.boosters.BoosterMenu;
import org.impldev.holycore.invest.InvestCommand;
import org.impldev.holycore.invest.InvestData;
import org.impldev.holycore.invest.InvestEffects;
import org.impldev.holycore.invest.InvestMenu;
import org.impldev.holycore.invest.InvestPlaceholders;
import org.impldev.holycore.commands.Wolker;
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
      this.getCommand("wolker").setExecutor(new Wolker());

      // одна и та же реализация на все три страницы - иначе каждая держала бы свой кэш
      // и пересчитывала топ заново
      this.getCommand("wextend").setExecutor(new WExtend());

      InvestData investData = new InvestData(this);
      investData.resetWeekIfNeeded();
      investData.payoutIfNeeded();
      // Раз в минуту, а не по таймеру до полуночи: обе проверки сравнивают дату, поэтому
      // сервер, перезапущенный или лежавший в 00:00, всё равно доведёт сброс и выплату.
      this.getServer().getScheduler().runTaskTimer(this, () -> {
         investData.resetWeekIfNeeded();
         investData.payoutIfNeeded();
      }, 1200L, 1200L);
      InvestEffects investEffects = new InvestEffects(this, investData);
      InvestMenu investMenu = new InvestMenu(this, investData, investEffects);
      this.getServer().getPluginManager().registerEvents(investEffects, this);
      this.getServer().getPluginManager().registerEvents(investMenu, this);
      this.getCommand("invest").setExecutor(new InvestCommand(investMenu));
      // границы выставляем после загрузки миров, иначе мир ещё не готов принять размер
      this.getServer().getScheduler().runTask(this, investEffects::apply);

      BoosterData boosterData = new BoosterData(this);
      BoosterMenu boosterMenu = new BoosterMenu(this, boosterData);
      this.getServer().getPluginManager().registerEvents(boosterMenu, this);
      this.getCommand("boosters").setExecutor(new BoosterCommand(boosterMenu));

      TitulRegistry titulRegistry = new TitulRegistry(this);
      TitulMenu titulMenu = new TitulMenu(this, titulRegistry);
      this.getServer().getPluginManager().registerEvents(titulMenu, this);
      this.getServer().getPluginManager().registerEvents(new TitulItemListener(titulRegistry), this);
      this.getCommand("tituls").setExecutor(new TitulCommand(titulMenu));

      BalTop balTop = new BalTop(this);
      this.getCommand("baltop").setExecutor(balTop);
      this.getCommand("baltop2").setExecutor(balTop);
      this.getCommand("baltop3").setExecutor(balTop);

      HealthIndicator healthIndicator = new HealthIndicator(this);
      this.getCommand("healthindicator").setExecutor(healthIndicator);
      // без PlaceholderAPI переключатель просто не на что повесить - TAB гасит объектив по плейсхолдеру
      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         healthIndicator.register();
         new InvestPlaceholders(investData, investEffects).register();
      } else {
         this.getLogger().warning("PlaceholderAPI не найден: /healthindicator не сможет управлять индикатором TAB");
      }
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
