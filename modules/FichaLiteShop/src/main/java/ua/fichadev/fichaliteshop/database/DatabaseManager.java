package ua.fichadev.fichaliteshop.database;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public interface DatabaseManager {
   void savePlayerShop(UUID var1, List<String> var2, Map<String, Double> var3, long var4);

   void loadPlayerShop(UUID var1, List<String> var2, Map<String, Double> var3, long[] var4);

   void addWarehouseItem(UUID var1, ItemStack var2);

   List<ItemStack> getWarehouseItems(UUID var1);

   ItemStack removeWarehouseItem(UUID var1, int var2);

   void addPurchaseStat(UUID var1, String var2, int var3);

   Map<String, Integer> getPlayerPurchaseStats(UUID var1);

   void close();
}
