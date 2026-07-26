package org.dimasik.liteauction;

import java.io.File;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteauction.api.events.EventManager;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import org.dimasik.liteauction.backend.communication.impl.Local;
import org.dimasik.liteauction.backend.communication.impl.Nats;
import org.dimasik.liteauction.backend.communication.impl.RabbitMQ;
import org.dimasik.liteauction.backend.communication.impl.Redis;
import org.dimasik.liteauction.backend.communication.impl.WebSocket;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;
import org.dimasik.liteauction.backend.listeners.JoinListener;
import org.dimasik.liteauction.backend.storage.databases.AbstractDatabase;
import org.dimasik.liteauction.backend.storage.databases.impl.Mysql;
import org.dimasik.liteauction.backend.storage.databases.impl.SQLite;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.nms.ContainerUtil;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.liteauction.economy.impl.StickEco;
import org.dimasik.liteauction.economy.impl.VaultEco;
import org.dimasik.liteauction.frontend.commands.CommandExecutor;
import org.dimasik.liteauction.frontend.commands.impl.Admin;
import org.dimasik.liteauction.frontend.commands.impl.Help;
import org.dimasik.liteauction.frontend.commands.impl.History;
import org.dimasik.liteauction.frontend.commands.impl.Player;
import org.dimasik.liteauction.frontend.commands.impl.Search;
import org.dimasik.liteauction.frontend.commands.impl.Sell;
import org.dimasik.liteauction.frontend.commands.impl.Sound;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.dimasik.liteauction.frontend.menus.bids.listeners.ItemBidsListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.ConfirmItemListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.CountBuyItemListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.MainListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.RemoveItemListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.SellListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.UnsoldListener;

public final class LiteAuction extends JavaPlugin {
   private static LiteAuction instance;
   private AbstractDatabase databaseManager;
   private AbstractCommunication communicationManager;
   private CommandExecutor commandExecutor;
   private ItemStack boughtItem;
   private EventManager eventManager;
   private EconomyEditor economyEditor;

   public LiteAuction() {
   }

   public void onEnable() {
      instance = this;
      new UpdateChecker(this, super.getFile()).checkForUpdates();
      super.getLogger().info("   [   зᴀгᴘʏзᴋᴀ ᴋᴏʜфигуᴘᴀции   ]   ");
      this.setupConfig();
      this.setupDatabase();
      this.setupEvents();
      this.setupEconomy();
      this.setupCommand();
      this.setupListeners();
      this.setupBoughtItem();
      if (ConfigManager.getBoolean("settings/database.yml", "isHead", true)) {
         this.startRunnable();
      }
   }

   private void setupConfig() {
      String[] files = new String[]{
         "config.yml",
         "settings/database.yml",
         "settings/settings.yml",
         "design/commands/sell.yml",
         "design/commands/search.yml",
         "design/commands/main.yml",
         "design/commands/sound.yml",
         "design/commands/history.yml",
         "design/menus/main.yml",
         "design/menus/market/main.yml",
         "design/menus/market/confirm_item.yml",
         "design/menus/market/count_buy_item.yml",
         "design/menus/market/remove_item.yml",
         "design/menus/market/sell.yml",
         "design/menus/market/unsold.yml",
         "design/menus/bids/main.yml",
         "design/menus/bids/item_bids.yml",
         "design/menus/bids/remove_item.yml",
         "design/menus/bids/sell.yml"
      };

      for (String filePath : files) {
         File file = new File(super.getDataFolder(), filePath);
         if (!file.exists()) {
            super.saveResource(filePath, false);
         }
      }

      try {
         ConfigManager.init(this);
         super.getLogger().info("   |   ᴋᴏʜфигуᴘᴀция уᴄпᴇшʜᴏ зᴀгᴘʏжᴇʜᴀ");
      } catch (UnsupportedConfigurationException var7) {
         super.getLogger().warning("   |   ᴏшибᴋᴀ зᴀгᴘузᴋи ᴋᴏʜфигуᴘᴀции: " + var7.getMessage());
         Bukkit.getPluginManager().disablePlugin(this);
      }
   }

   private void setupDatabase() {
      String var1 = ConfigManager.getString("settings/database.yml", "database.type", "Redis").toLowerCase();
      switch (var1) {
         case "mysql":
            this.databaseManager = new Mysql(
               ConfigManager.getString("settings/database.yml", "database.global.host", "localhost"),
               ConfigManager.getString("settings/database.yml", "database.global.user", "root"),
               ConfigManager.getString("settings/database.yml", "database.global.password", "сайнес гпт кодер"),
               ConfigManager.getString("settings/database.yml", "database.global.database", "lite_auction")
            );
            break;
         case "sqlite":
            this.databaseManager = new SQLite(ConfigManager.getString("settings/database.yml", "database.local.file", "database.db"));
            break;
         case "default":
            throw new UnsupportedConfigurationException("Тип базы данных не существует!");
      }

      this.databaseManager.createTables().join();
      var1 = ConfigManager.getString("settings/database.yml", "communication.type", "Redis").toLowerCase();
      switch (var1) {
         case "redis":
            this.communicationManager = new Redis(
               ConfigManager.getString("settings/database.yml", "communication.redis.host", "localhost"),
               ConfigManager.getInt("settings/database.yml", "communication.redis.port", 6379),
               ConfigManager.getString("settings/database.yml", "communication.redis.password", "сайнес гпт кодер"),
               ConfigManager.getString("settings/database.yml", "communication.redis.channel", "auction")
            );
            break;
         case "rabbitmq":
            this.communicationManager = new RabbitMQ(
               ConfigManager.getString("settings/database.yml", "communication.rabbitmq.host", "localhost"),
               ConfigManager.getInt("settings/database.yml", "communication.rabbitmq.port", 5672),
               ConfigManager.getString("settings/database.yml", "communication.rabbitmq.user", "root"),
               ConfigManager.getString("settings/database.yml", "communication.rabbitmq.password", "сайнес гпт кодер"),
               ConfigManager.getString("settings/database.yml", "communication.rabbitmq.vhost", "/"),
               ConfigManager.getString("settings/database.yml", "communication.rabbitmq.channel", "auction")
            );
            break;
         case "nats":
            this.communicationManager = new Nats(
               ConfigManager.getStringList("settings/database.yml", "communication.nats.host").toArray(new String[0]),
               ConfigManager.getString("settings/database.yml", "communication.nats.user", "root"),
               ConfigManager.getString("settings/database.yml", "communication.nats.password", "сайнес гпт кодер"),
               ConfigManager.getString("settings/database.yml", "communication.nats.channel", "auction")
            );
            break;
         case "websocket":
            this.communicationManager = new WebSocket(
               ConfigManager.getString("settings/database.yml", "communication.websocket.host", "localhost"),
               ConfigManager.getInt("settings/database.yml", "communication.websocket.port", 6379),
               ConfigManager.getString("settings/database.yml", "communication.websocket.password", "сайнес гпт кодер"),
               ConfigManager.getString("settings/database.yml", "communication.websocket.channel", "auction")
            );
            break;
         case "local":
            this.communicationManager = new Local();
            break;
         case "default":
            throw new UnsupportedConfigurationException("Метод коммуникации не существует!");
      }
   }

   private void setupEvents() {
      this.eventManager = new EventManager();
   }

   private void setupEconomy() {
      if (ConfigManager.getECONOMY_EDITOR().equalsIgnoreCase("StickEco")) {
         this.economyEditor = new StickEco();
      } else {
         this.economyEditor = new VaultEco();
      }
   }

   private void setupCommand() {
      this.commandExecutor = new CommandExecutor();
      PluginCommand command = this.getCommand("ah");

      assert command != null;

      command.setExecutor(this.commandExecutor);
      command.setTabCompleter(this.commandExecutor);
      new Sell("sell").register();
      new Search("search").register();
      new Help("help").register();
      new Sound("sound").register();
      new Player("player").register();
      new Admin("admin").register();
      new History("hist").register();
   }

   private void setupListeners() {
      PluginManager pluginManager = super.getServer().getPluginManager();
      pluginManager.registerEvents(new JoinListener(), this);
      new MainListener().register();
      new org.dimasik.liteauction.frontend.menus.bids.listeners.MainListener().register();
      new SellListener().register();
      new org.dimasik.liteauction.frontend.menus.bids.listeners.SellListener().register();
      new RemoveItemListener().register();
      new org.dimasik.liteauction.frontend.menus.bids.listeners.RemoveItemListener().register();
      new ItemBidsListener().register();
      new UnsoldListener().register();
      new ConfirmItemListener().register();
      new CountBuyItemListener().register();
   }

   private void setupBoughtItem() {
      this.boughtItem = new ItemStack(Material.BARRIER);
      ItemMeta itemMeta = this.boughtItem.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CЭтот предмет &x&F&F&2&2&2&2уже купили&x&D&5&D&B&D&C!"));
      this.boughtItem.setItemMeta(itemMeta);
   }

   public void startRunnable() {
      Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> getInstance().getDatabaseManager().moveExpiredItems(), 0L, 20L);
   }

   public void onDisable() {
      for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
         Inventory inventory = ContainerUtil.getActiveContainer(player);
         InventoryHolder holder = inventory.getHolder();
         if (holder instanceof AbstractMenu) {
            inventory.close();
         }
      }

      if (this.communicationManager != null) {
         this.communicationManager.close();
      }
   }

   public static void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (int id = 0; id < inventory.getStorageContents().length; id++) {
         ItemStack item = inventory.getItem(id);
         if (item == null || item.getType().isAir()) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int count = item.getMaxStackSize() - item.getAmount();
            if (count > 0) {
               if (itemStack.getAmount() <= count) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack i = itemStack.clone();
               i.setAmount(count);
               inventory.addItem(new ItemStack[]{i});
               itemStack.setAmount(itemStack.getAmount() - count);
            }
         }
      }

      Bukkit.getScheduler().runTask(instance, () -> location.getWorld().dropItemNaturally(location, itemStack));
   }

   @Generated
   public AbstractDatabase getDatabaseManager() {
      return this.databaseManager;
   }

   @Generated
   public AbstractCommunication getCommunicationManager() {
      return this.communicationManager;
   }

   @Generated
   public CommandExecutor getCommandExecutor() {
      return this.commandExecutor;
   }

   @Generated
   public ItemStack getBoughtItem() {
      return this.boughtItem;
   }

   @Generated
   public EventManager getEventManager() {
      return this.eventManager;
   }

   @Generated
   public EconomyEditor getEconomyEditor() {
      return this.economyEditor;
   }

   @Generated
   public static LiteAuction getInstance() {
      return instance;
   }
}
