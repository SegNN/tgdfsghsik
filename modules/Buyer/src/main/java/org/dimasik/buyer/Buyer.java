package org.dimasik.buyer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Buyer extends JavaPlugin implements Listener, CommandExecutor {
   private DatabaseConnector databaseConnector;
   public KoefficentManager koefficentManager;
   public PricesManager pricesManager;
   public PlayerDataManager playerDataManager;
   public BuyerAutoMenu buyerAutoMenu;
   public BukkitTask autoBuyerTask;
   private Map<Material, Double> prices;
   public HashMap<Player, Boolean> toggledAutoBuyers = new HashMap<>();
   public HashMap<Player, String> autoBuyerTypes = new HashMap<>();
   private Economy economy;
   private final String INVENTORY_TITLE = "Скупщик предметов                                                                                                                                                                                                                                                                                                                                                [mantibuyer:buyer]";
   private final String AUTOBUYER_BUTTON_NAME = "&x&F&F&7&0&0&0 ▶ &x&E&7&E&7&E&7Нажмите, чтобы перейти в меню авто-скупщика";
   private final String SUCCESS_MESSAGE = "&x&F&F&7&0&0&0 ▶ &fВы &x&F&F&7&0&0&0успешно продали %items% (+%points% поинтов к множителю) &fпредметов и заработали &x&F&F&C&9&0&0%price% монет. &fВаш множитель теперь составляет: &x&F&F&C&9&0&0%newmultiplier%.";

   public Buyer() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      if (!this.setupEconomy()) {
         this.getLogger().severe("Vault не найден! Плагин будет отключен.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.databaseConnector = new DatabaseConnector(this);
         this.koefficentManager = new KoefficentManager(this.databaseConnector);
         this.pricesManager = new PricesManager(this.databaseConnector);
         this.playerDataManager = new PlayerDataManager(this.databaseConnector);
         this.buyerAutoMenu = new BuyerAutoMenu(this);
         this.loadPrices();
         this.getServer().getPluginManager().registerEvents(this, this);
         this.getServer().getPluginManager().registerEvents(this.buyerAutoMenu, this);
         this.getCommand("buyer").setExecutor(this);
         this.getCommand("b").setExecutor(this);
         BuyerTabCompleter tabCompleter = new BuyerTabCompleter();
         this.getCommand("buyer").setTabCompleter(tabCompleter);
         this.getCommand("b").setTabCompleter(tabCompleter);

         for (Player player : Bukkit.getOnlinePlayers()) {
            try {
               Double koeff = this.koefficentManager.getKoefficent(player.getName()).get();
               if (koeff == null || koeff == 0.0) {
                  this.koefficentManager.addKoefficent(player.getName(), 0.0);
               }
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         for (Player player : Bukkit.getOnlinePlayers()) {
            this.toggledAutoBuyers.put(player, false);
         }

         if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BuyerPlaceholderExpansion(this).register();
         }

         this.autoBuyerTask = (new BukkitRunnable() {
            public void run() {
               for (Entry<Player, Boolean> entry : Buyer.this.toggledAutoBuyers.entrySet()) {
                  Player player = entry.getKey();
                  Boolean isEnabled = entry.getValue();
                  if (player != null && isEnabled) {
                     PlayerInventory inventory = player.getInventory();
                     double totalPrice = 0.0;
                     double totalPoints = 0.0;

                     for (int i = 0; i < 36; i++) {
                        ItemStack itemStack = inventory.getItem(i);

                        try {
                           if (itemStack != null && Buyer.this.playerDataManager.isEnabled(player.getName(), itemStack.getType().name()).get()) {
                              double itemPrice = Buyer.this.pricesManager.getPrice(itemStack.getType().name()).get() * itemStack.getAmount();
                              double itemPoints = Buyer.this.pricesManager.getPoints(itemStack.getType().name()).get() * itemStack.getAmount();
                              totalPrice += itemPrice;
                              totalPoints += itemPoints;
                              inventory.setItem(i, null);
                           }
                        } catch (Exception var16) {
                           var16.printStackTrace();
                        }
                     }

                     if (totalPoints > 0.0) {
                        Buyer.this.koefficentManager.addKoefficent(player.getName(), totalPoints);
                     }

                     if (totalPrice > 0.0) {
                        try {
                           double koeff = Buyer.this.koefficentManager.getKoefficent(player.getName()).get();
                           totalPrice *= koeff;
                           Buyer.this.economy.depositPlayer(player, totalPrice);
                        } catch (Exception var17) {
                           var17.printStackTrace();
                        }
                     }
                  }
               }
            }
         }).runTaskTimer(this, 0L, 20L);
         this.getLogger().info("Купить нового скупщика можно в тгк https://t.me/ExampleTG");
         this.getLogger().info("Скупщик полностью без багов и дюпов!");
         this.getLogger().info("Имеется поддержка к плагину и помощь с плагином");
      }
   }

   public void onDisable() {
      if (this.autoBuyerTask != null) {
         this.autoBuyerTask.cancel();
      }

      if (this.databaseConnector != null) {
         this.databaseConnector.closeConnection();
      }

      this.getLogger().info("Купить нового скупщика можно в тгк https://t.me/ExampleTG");
      this.getLogger().info("Скупщик полностью без багов и дюпов!");
      this.getLogger().info("Имеется поддержка к плагину и помощь с плагином");
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
         }

         return this.economy != null;
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.toggledAutoBuyers.put(event.getPlayer(), false);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.toggledAutoBuyers.remove(event.getPlayer());
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if ((command.getName().equalsIgnoreCase("b") || command.getName().equalsIgnoreCase("buyer")) && sender instanceof Player) {
         Player player = (Player)sender;
         if (sender.hasPermission("buyer.items") && args.length >= 5 && args[0].equalsIgnoreCase("additem")) {
            StringBuilder displayNameBuilder = new StringBuilder();

            for (int i = 4; i < args.length; i++) {
               displayNameBuilder.append(args[i]).append(" ");
            }

            String displayName = displayNameBuilder.toString().trim();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            this.pricesManager.addPrice(itemInHand.getType().name(), displayName, Double.parseDouble(args[3]), Double.parseDouble(args[2]), args[1]);
            sender.sendMessage("Предмет добавлен: " + displayName);
            return true;
         } else if (args.length >= 1 && args[0].equalsIgnoreCase("auto")) {
            this.buyerAutoMenu.open(player);
            return true;
         } else {
            try {
               Double koeff = this.koefficentManager.getKoefficent(player.getName()).get();
               if (koeff == null || koeff == 0.0) {
                  this.koefficentManager.addKoefficent(player.getName(), 0.0);
               }

               this.openBuyerInventory(player);
            } catch (Exception var9) {
               var9.printStackTrace();
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private double roundPoints(double points) {
      return Math.round(points * 10000.0) / 10000.0;
   }

   public void openBuyerInventory(Player player) {
      Inventory inventory = Bukkit.createInventory(
         null,
         54,
         this.colorize(
            "Скупщик предметов                                                                                                                                                                                                                                                                                                                                                [mantibuyer:buyer]"
         )
      );

      for (int i = 45; i < 53; i++) {
         ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
         ItemMeta paneMeta = pane.getItemMeta();
         paneMeta.setDisplayName(" ");
         pane.setItemMeta(paneMeta);
         inventory.setItem(i, pane);
      }

      if (this.getConfig().getBoolean("show-promo-panel", true)) {
         ItemStack promoPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
         ItemMeta promoPaneMeta = promoPane.getItemMeta();
         promoPaneMeta.setDisplayName(" ");
         List<String> promoLore = new ArrayList<>();
         promoLore.add(this.colorize("&x&F&F&0&0&0&0&lХотите нормального нового скупщика с HW?"));
         promoLore.add(this.colorize("&7Тогда покупайте его в тгк &x&F&F&0&0&E&3@HolyDev"));
         promoLore.add(this.colorize("&7С поддержкой обновления"));
         promoPaneMeta.setLore(promoLore);
         promoPane.setItemMeta(promoPaneMeta);
         inventory.setItem(49, promoPane);
      }

      inventory.setItem(53, this.getSelButton(0.0, 0.0));
      ItemStack autobuyerButton = new ItemStack(Material.NETHER_STAR);
      ItemMeta autobuyerButtonMeta = autobuyerButton.getItemMeta();
      autobuyerButtonMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(this.colorize("&x&F&F&7&0&0&0 ▶ &x&E&7&E&7&E&7Нажмите, чтобы перейти в меню авто-скупщика"));
      lore.add(this.colorize(""));
      autobuyerButtonMeta.setLore(lore);
      autobuyerButton.setItemMeta(autobuyerButtonMeta);
      inventory.setItem(46, autobuyerButton);

      try {
         double koeff = this.koefficentManager.getKoefficent(player.getName()).get();
         double points = this.koefficentManager.getPoints(player.getName()).get();
         inventory.setItem(45, this.getInfoButton(koeff, points));
         player.openInventory(inventory);
      } catch (Exception var10) {
         var10.printStackTrace();
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getView()
         .getTitle()
         .equals(
            this.colorize(
               "Скупщик предметов                                                                                                                                                                                                                                                                                                                                                [mantibuyer:buyer]"
            )
         )) {
         Player player = (Player)event.getWhoClicked();
         Inventory inventory = event.getInventory();

         try {
            double koeff = this.koefficentManager.getKoefficent(player.getName()).get();
            this.updateSellButton(inventory, koeff, player);
         } catch (Exception var16) {
            var16.printStackTrace();
         }

         if (event.getRawSlot() >= 45 && event.getRawSlot() < 54) {
            event.setCancelled(true);
         }

         ItemStack clickedItem = event.getCurrentItem();
         if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            if (event.getClick() == ClickType.DOUBLE_CLICK
               && (
                  clickedItem.getType() == Material.WRITABLE_BOOK
                     || clickedItem.getType() == Material.NETHER_STAR
                     || clickedItem.getType() == Material.LIME_DYE
                     || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE
                     || clickedItem.getType() == Material.RED_STAINED_GLASS_PANE
               )) {
               event.setCancelled(true);
            }

            if (event.getRawSlot() == 53) {
               int itemCount = 0;

               for (int i = 0; i < 45; i++) {
                  ItemStack itemStack = inventory.getItem(i);
                  if (itemStack != null) {
                     itemCount += itemStack.getAmount();
                  }
               }

               double points = this.calculateTotalPoints(inventory);

               try {
                  double koeff = this.koefficentManager.getKoefficent(player.getName()).get();
                  double totalPrice = this.calculateTotalPrice(inventory, koeff);
                  if (totalPrice > 0.0) {
                     this.sellItems(player, inventory, totalPrice, this.calculateTotalPoints(inventory));
                     double newKoeff = this.koefficentManager.getKoefficent(player.getName()).get();
                     String message = "&x&F&F&7&0&0&0 ▶ &fВы &x&F&F&7&0&0&0успешно продали %items% (+%points% поинтов к множителю) &fпредметов и заработали &x&F&F&C&9&0&0%price% монет. &fВаш множитель теперь составляет: &x&F&F&C&9&0&0%newmultiplier%."
                        .replace("%price%", this.formatDouble(totalPrice))
                        .replace("%points%", this.formatDouble(points))
                        .replace("%items%", String.valueOf(itemCount))
                        .replace("%newmultiplier%", this.formatDouble(newKoeff));
                     player.sendMessage(this.colorize(message));
                     player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                  }
               } catch (Exception var15) {
                  var15.printStackTrace();
               }
            } else if (event.getRawSlot() == 46) {
               this.buyerAutoMenu.open(player);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getView()
         .getTitle()
         .equals(
            this.colorize(
               "Скупщик предметов                                                                                                                                                                                                                                                                                                                                                [mantibuyer:buyer]"
            )
         )) {
         Player player = (Player)event.getPlayer();

         for (int i = 0; i < 45; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               player.getInventory().addItem(new ItemStack[]{item});
            }
         }
      }
   }

   private void updateSellButton(final Inventory inventory, final double koeff, final Player player) {
      (new BukkitRunnable() {
         public void run() {
            inventory.setItem(53, Buyer.this.getSelButton(Buyer.this.calculateTotalPrice(inventory, koeff), Buyer.this.calculateTotalPoints(inventory)));

            try {
               double playerKoeff = Buyer.this.koefficentManager.getKoefficent(player.getName()).get();
               double playerPoints = Buyer.this.koefficentManager.getPoints(player.getName()).get();
               inventory.setItem(45, Buyer.this.getInfoButton(playerKoeff, playerPoints));
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }
      }).runTaskLater(this, 1L);
   }

   private ItemStack getSelButton(double price, double points) {
      ItemStack sellButton = new ItemStack(Material.LIME_DYE);
      ItemMeta sellButtonMeta = sellButton.getItemMeta();
      sellButtonMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(this.colorize("&x&F&F&7&0&0&0 ▶ &x&E&7&E&7&E&7Нажмите, чтобы сдать все предметы"));
      lore.add(this.colorize("    &x&E&7&E&7&E&7и получить на баланс &x&F&F&C&9&0&0" + this.formatDouble(price) + " монеток,"));
      lore.add(this.colorize("    &x&E&7&E&7&E&7и ещё дополнительно &x&0&5&F&B&0&0" + this.formatDouble(points) + " очков."));
      lore.add(this.colorize(""));
      sellButtonMeta.setLore(lore);
      sellButton.setItemMeta(sellButtonMeta);
      return sellButton;
   }

   private ItemStack getInfoButton(double koeff, double points) {
      ItemStack infoButton = new ItemStack(Material.WRITABLE_BOOK);
      ItemMeta infoButtonMeta = infoButton.getItemMeta();
      infoButtonMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(this.colorize("&x&F&F&7&0&0&0 ● &fТекущий множитель &x&F&F&C&9&0&0x" + this.formatDouble(koeff) + " (" + this.formatDouble(points) + ")"));
      lore.add(this.colorize(""));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f "));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f В этом меню Вы можете обменивать"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f предметы на монеты. Более ценные"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f ресурсы стоят больше Монет"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f и быстрее повышают &x&0&0&D&8&F&Fобщий коэффициент,"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f чем предметы, которые легко добыть."));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f Некоторые ресурсы обменять на"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f Монеты невозможно."));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f "));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f В основном можно сдавать след. предметы:"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f &x&0&0&D&8&F&Fлут с мобов, ценные ресурсы с шахты,"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f &x&0&0&D&8&F&Fрастения и древесину. &fЧем больше и"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f ценнее предметов вы сдаете, тем выше"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f становится ваш множитель монет"));
      lore.add(this.colorize("&x&F&F&7&0&0&0 ▍&f "));
      infoButtonMeta.setLore(lore);
      infoButton.setItemMeta(infoButtonMeta);
      return infoButton;
   }

   private void sellItems(Player player, Inventory inventory, double totalPrice, double totalPoints) {
      totalPoints = this.roundPoints(totalPoints);

      for (int i = 0; i < 45; i++) {
         ItemStack item = inventory.getItem(i);
         if (item != null && item.getType() != Material.AIR) {
            try {
               Double itemPrice = this.pricesManager.getPrice(item.getType().name()).get();
               if (itemPrice != null && itemPrice != 0.0) {
                  inventory.setItem(i, new ItemStack(Material.AIR));
               }
            } catch (Exception var10) {
               var10.printStackTrace();
            }
         }
      }

      this.economy.depositPlayer(player, totalPrice);
      this.koefficentManager.addKoefficent(player.getName(), totalPoints);
   }

   private double calculateKoefficent(double points) {
      points = this.roundPoints(points);
      double koeff = 1.0 + points / 125000.0;
      return Math.min(koeff, 5.0);
   }

   private double calculateTotalPrice(Inventory inventory, double koeff) {
      try {
         double totalPrice = 0.0;

         for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               Double itemPrice = this.pricesManager.getPrice(item.getType().name()).get();
               if (itemPrice != null) {
                  totalPrice += itemPrice * item.getAmount();
               }
            }
         }

         return totalPrice * koeff;
      } catch (Exception var9) {
         var9.printStackTrace();
         return 0.0;
      }
   }

   private double calculateTotalPoints(Inventory inventory) {
      try {
         double totalPoints = 0.0;

         for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               Double itemPoints = this.pricesManager.getPoints(item.getType().name()).get();
               if (itemPoints != null) {
                  totalPoints += itemPoints * item.getAmount();
               }
            }
         }

         return this.roundPoints(totalPoints);
      } catch (Exception var7) {
         var7.printStackTrace();
         return 0.0;
      }
   }

   private void loadPrices() {
      this.prices = new HashMap<>();
      if (this.getConfig().getConfigurationSection("prices") != null) {
         for (String key : this.getConfig().getConfigurationSection("prices").getKeys(false)) {
            Material material = Material.getMaterial(key);
            double price = this.getConfig().getDouble("prices." + key);
            if (material != null && price > 0.0) {
               this.prices.put(material, price);
            }
         }
      }
   }

   private String colorize(String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }

   private String[] colorize(String[] text) {
      for (int i = 0; i < text.length; i++) {
         text[i] = this.colorize(text[i]);
      }

      return text;
   }

   private String formatDouble(double value) {
      return String.format("%.2f", value).replace(',', '.');
   }

   public Economy getEconomy() {
      return this.economy;
   }
}
