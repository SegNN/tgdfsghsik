package ru.extas7y.saveitems;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.extas7y.saveitems.command.CommandHandler;
import ru.extas7y.saveitems.database.MyDatabase;
import ru.extas7y.saveitems.object.SaveItemsObject;

public final class SaveItems extends JavaPlugin {
   private static SaveItems instance;
   private MyDatabase dbHandler;

   public SaveItems() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      FileConfiguration config = this.getConfig();
      String type = config.getString("database.type", "SQLite");
      if (type.equalsIgnoreCase("MySQL")) {
         String host = this.getConfig().getString("database.host");
         String name = this.getConfig().getString("database.name");
         String user = this.getConfig().getString("database.user");
         String pass = this.getConfig().getString("database.pass");
         this.dbHandler = new MyDatabase(this, type, host, name, user, pass);
      } else {
         this.dbHandler = new MyDatabase(this, type, null, null, null, null);
      }

      SaveItemsObject savedItemsInventory = new SaveItemsObject(this, this.dbHandler);
      this.getServer().getPluginManager().registerEvents(savedItemsInventory, this);
      this.getCommand("saveitems").setExecutor(new CommandHandler(this, this.dbHandler, savedItemsInventory));
      Bukkit.getLogger().info("Плагин успешно запущен! Используется: " + type);
   }

   public void onDisable() {
      this.dbHandler.closeConnection();
   }

   public void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
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

      Bukkit.getScheduler().runTask(this, () -> location.getWorld().dropItemNaturally(location, itemStack));
   }

   @Generated
   public static SaveItems getInstance() {
      return instance;
   }

   @Generated
   public MyDatabase getDbHandler() {
      return this.dbHandler;
   }
}
