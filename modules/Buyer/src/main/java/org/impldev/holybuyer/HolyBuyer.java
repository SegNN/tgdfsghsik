package org.impldev.holybuyer;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holybuyer.menu.MenuService;
import org.jetbrains.annotations.NotNull;

/**
 * Скупщик: продажа ресурсов за монеты и очки скупщика.
 *
 * <p>Прошлый скупщик хранил всё в MySQL, но обе его таблицы с игровыми данными на сервере
 * пустые, а база из конфига не существует - переносить было нечего, поэтому хранение переехало
 * в yml рядом с плагином. Плейсхолдеры {@code %buyer_points%} и {@code %buyer_koeff%} остались
 * с прежними именами: на них держатся голограмма топа и таблица ajLeaderboards.
 */
public final class HolyBuyer extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
   private static final List<String> SUBCOMMANDS = List.of("auto", "shop", "items", "settings");

   private final Random random = new Random();

   private Catalogue catalogue;
   private BuyerData data;
   private Prices prices;
   private Sales sales;
   private Shop shop;
   private Fragments fragments;
   private MenuService menus;
   private Economy economy;

   @Override
   public void onEnable() {
      this.saveDefaultConfig();

      this.catalogue = new Catalogue(this);
      this.data = new BuyerData(this);
      Bonuses bonuses = new Bonuses(this, this.catalogue);
      this.prices = new Prices(this, this.catalogue, this.data, bonuses);
      this.economy = this.hookEconomy();
      this.sales = new Sales(this.catalogue, this.data, this.prices, this.economy);
      this.shop = new Shop(this);
      this.fragments = new Fragments(this);
      this.menus = new MenuService(this);

      if (this.economy == null) {
         this.getLogger().warning("Vault не найден: продажи будут считаться, но монеты не начислятся");
      }

      AutoSell autoSell = new AutoSell(this);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.getServer().getPluginManager().registerEvents(this.menus, this);
      this.getServer().getPluginManager().registerEvents(autoSell, this);

      long period = Math.max(20L, this.getConfig().getLong("auto-sell.period-ticks", 100L));
      this.getServer().getScheduler().runTaskTimer(this, autoSell::tick, period, period);

      for (String name : List.of("buyer", "b")) {
         if (this.getCommand(name) != null) {
            this.getCommand(name).setExecutor(this);
            this.getCommand(name).setTabCompleter(this);
         }
      }

      // Профили тех, кто уже в игре: после /reload события входа не будет.
      for (Player player : this.getServer().getOnlinePlayers()) {
         this.data.profile(player.getUniqueId(), player.getName());
      }

      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         new BuyerPlaceholders(this).register();
      }

      // Сдачи копятся у всех сразу, поэтому общий счётчик пишем по таймеру, а не после
      // каждой продажи: иначе на людном сервере файл переписывался бы по десять раз в секунду.
      this.getServer().getScheduler().runTaskTimer(this, this.data::saveGlobal, 6000L, 6000L);
   }

   @Override
   public void onDisable() {
      if (this.data != null) {
         this.data.saveAll();
      }
   }

   private Economy hookEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return null;
      }

      RegisteredServiceProvider<Economy> provider = this.getServer().getServicesManager().getRegistration(Economy.class);
      return provider == null ? null : provider.getProvider();
   }

   public Catalogue catalogue() {
      return this.catalogue;
   }

   public BuyerData data() {
      return this.data;
   }

   public Prices prices() {
      return this.prices;
   }

   public Sales sales() {
      return this.sales;
   }

   public Shop shop() {
      return this.shop;
   }

   public Fragments fragments() {
      return this.fragments;
   }

   public MenuService menus() {
      return this.menus;
   }

   public Random random() {
      return this.random;
   }

   public void deposit(Player player, double coins) {
      if (this.economy != null && coins > 0.0) {
         this.economy.depositPlayer(player, coins);
      }
   }

   /** Временный бустер из магазина. Складывается с бустером из {@code /boosters}. */
   public void grantBooster(Player player, int percent, int minutes) {
      BuyerData.Profile profile = this.data.profile(player.getUniqueId(), player.getName());
      profile.shopBoosterPercent = percent;
      profile.shopBoosterUntil = Math.max(profile.shopBoosterUntil, System.currentTimeMillis())
            + minutes * 60_000L;
      profile.touch();
      this.data.savePlayer(profile);
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.data.profile(event.getPlayer().getUniqueId(), event.getPlayer().getName());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.data.unload(event.getPlayer().getUniqueId());
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("Команда только для игроков.");
         return true;
      }

      String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
      switch (sub) {
         case "auto" -> this.menus.openAuto(player, args.length > 1 ? args[1] : null);
         case "shop" -> this.menus.openShop(player);
         case "items" -> this.menus.openItems(player);
         case "settings" -> this.menus.openSettings(player);
         default -> this.menus.openMain(player);
      }

      return true;
   }

   @Override
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String label, @NotNull String[] args) {
      if (args.length == 1) {
         String prefix = args[0].toLowerCase(Locale.ROOT);
         return SUBCOMMANDS.stream().filter(s -> s.startsWith(prefix)).toList();
      }

      if (args.length == 2 && args[0].equalsIgnoreCase("auto")) {
         return List.copyOf(this.catalogue.categories().keySet());
      }

      return List.of();
   }
}
