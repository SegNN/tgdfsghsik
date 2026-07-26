package ru.kotikstasika.eventship.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.KeyType;

public class LootManager {
   private final EventShip plugin;
   private final Random random = new Random();
   private final LootAPI externalLootApi;

   public LootManager(EventShip plugin) {
      this.plugin = plugin;
      this.externalLootApi = new LootAPI();
   }

   public List<ItemStack> generateLootFromExternalOrConfig(int inventorySize, int level, KeyType ignored) {
      try {
         int min = Math.max(1, inventorySize / 2);
         int count = this.random.nextInt(inventorySize - min + 1) + min;
         List<ItemStack> external = this.externalLootApi.getRandomItems(count, level);
         if (external != null) {
            return external;
         }
      } catch (Throwable var8) {
      }

      return new ArrayList<>();
   }

   public int mapKeyTypeToExternalLevel(KeyType keyType) {
      if (keyType == null) {
         return 1;
      } else {
         switch (keyType) {
            case DEFAULT:
               return 1;
            case RARE:
               return 2;
            case UNIQUE:
            case SECRET:
               return 3;
            default:
               return 1;
         }
      }
   }

   public List<ItemStack> getRandomItemsExact(int count, int level) {
      try {
         List<ItemStack> external = this.externalLootApi.getRandomItems(count, level);
         return (List<ItemStack>)(external != null ? external : new ArrayList<>());
      } catch (Throwable var4) {
         return new ArrayList<>();
      }
   }
}
