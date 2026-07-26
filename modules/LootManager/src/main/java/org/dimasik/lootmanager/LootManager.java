package org.dimasik.lootmanager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.lootmanager.backend.command.LootCommand;
import org.dimasik.lootmanager.backend.database.DatabaseManager;
import org.dimasik.lootmanager.backend.managers.ItemManager;
import org.dimasik.lootmanager.backend.models.NameData;
import org.dimasik.lootmanager.frontend.listeners.ConfigListener;
import org.dimasik.lootmanager.frontend.listeners.ConfigsListener;
import org.dimasik.lootmanager.frontend.listeners.EditItemListener;
import org.dimasik.lootmanager.frontend.listeners.MainListener;
import org.dimasik.lootmanager.frontend.listeners.NameSelectListener;
import org.dimasik.lootmanager.frontend.listeners.PropertiesListener;
import org.dimasik.lootmanager.frontend.listeners.RandomItemsListener;
import org.dimasik.lootmanager.frontend.listeners.RemoveItemListener;

public final class LootManager extends JavaPlugin {
   private static LootManager instance;
   private File itemsFile;
   private YamlConfiguration itemsConfig;
   public ItemManager itemManager;
   private DatabaseManager databaseManager;
   public static HashMap<Player, NameData> nameDatas = new HashMap<>();

   public LootManager() {
   }

   public void onEnable() {
      instance = this;
      this.setupItemsFile();
      this.saveDefaultConfig();
      FileConfiguration config = this.getConfig();
      this.itemManager = new ItemManager(this);
      this.databaseManager = new DatabaseManager(
         config.getString("mysql.host"), config.getString("mysql.user"), config.getString("mysql.password"), config.getString("mysql.database")
      );
      LootCommand lootCommand = new LootCommand(this);
      this.getCommand("lootmanager").setExecutor(lootCommand);
      this.getCommand("lootmanager").setTabCompleter(lootCommand);
      this.getServer().getPluginManager().registerEvents(new MainListener(), this);
      this.getServer().getPluginManager().registerEvents(new RemoveItemListener(), this);
      this.getServer().getPluginManager().registerEvents(new EditItemListener(), this);
      this.getServer().getPluginManager().registerEvents(new PropertiesListener(), this);
      this.getServer().getPluginManager().registerEvents(new RandomItemsListener(), this);
      this.getServer().getPluginManager().registerEvents(new ConfigsListener(), this);
      this.getServer().getPluginManager().registerEvents(new ConfigListener(), this);
      this.getServer().getPluginManager().registerEvents(new NameSelectListener(), this);
   }

   public void onDisable() {
      this.saveItemsConfig();
   }

   private void setupItemsFile() {
      this.itemsFile = new File(this.getDataFolder(), "items.yml");
      if (!this.itemsFile.exists()) {
         this.itemsFile.getParentFile().mkdirs();
         this.saveResource("items.yml", false);
      }

      this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsFile);
   }

   public void saveItemsConfig() {
      try {
         this.itemsConfig.save(this.itemsFile);
      } catch (IOException var2) {
         var2.printStackTrace();
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
   public static LootManager getInstance() {
      return instance;
   }

   @Generated
   public YamlConfiguration getItemsConfig() {
      return this.itemsConfig;
   }

   @Generated
   public ItemManager getItemManager() {
      return this.itemManager;
   }

   @Generated
   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }
}
