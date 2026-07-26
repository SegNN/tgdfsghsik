package ru.kotikstasika.holysfers.sferomant.listeners;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.holysfers.HolySfers;
import ru.kotikstasika.holysfers.utils.Utils;

public class FurnaceEvent implements Listener {
   private final HolySfers plugin;
   private final Utils utils;
   private final Random random = new Random();

   public FurnaceEvent() {
      this.plugin = HolySfers.getInstance();
      this.utils = HolySfers.getInstance().getUtils();
   }

   @EventHandler
   public void onFurnaceSmelt(FurnaceSmeltEvent event) {
      ItemStack source = event.getSource();
      if (source != null && !source.getType().isAir() && (source.getType() == Material.PLAYER_HEAD || source.getType() == Material.TOTEM_OF_UNDYING)) {
         ItemMeta meta = source.getItemMeta();
         if (meta != null) {
            NamespacedKey hsKey = new NamespacedKey(this.plugin, "holysfers");
            Integer hsTag = (Integer)meta.getPersistentDataContainer().get(hsKey, PersistentDataType.INTEGER);
            if (hsTag != null && hsTag == 1) {
               NamespacedKey rarityKey = new NamespacedKey(this.plugin, "rarity");
               String rarity = (String)meta.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);

               Utils.SphereType sphereType;
               try {
                  sphereType = rarity == null ? Utils.SphereType.DEFAULT : Utils.SphereType.valueOf(rarity);
               } catch (IllegalArgumentException var17) {
                  sphereType = Utils.SphereType.DEFAULT;
               }

               if (this.random.nextDouble() < 0.95) {
                  event.setCancelled(true);
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     Furnace furnacex = (Furnace)event.getBlock().getState();
                     FurnaceInventory invx = furnacex.getInventory();
                     ItemStack smelting = invx.getSmelting();
                     if (smelting != null) {
                        invx.setSmelting(smelting);
                     }
                  });
               } else {
                  int shardCount = this.calculateShardCount(sphereType);
                  Furnace furnace = (Furnace)event.getBlock().getState();
                  FurnaceInventory inv = furnace.getInventory();
                  int base = 0;
                  ItemStack existing = inv.getResult();
                  if (existing != null) {
                     ItemMeta exMeta = existing.getItemMeta();
                     NamespacedKey oskKey = new NamespacedKey(this.plugin, "oskolok");
                     if (exMeta != null && (Integer)exMeta.getPersistentDataContainer().getOrDefault(oskKey, PersistentDataType.INTEGER, 0) == 1) {
                        base = existing.getAmount();
                     }
                  }

                  if (existing != null) {
                     ItemMeta exMeta = existing.getItemMeta();
                     NamespacedKey oskKey = new NamespacedKey(this.plugin, "oskolok");
                     boolean isShard = exMeta != null && (Integer)exMeta.getPersistentDataContainer().getOrDefault(oskKey, PersistentDataType.INTEGER, 0) == 1;
                     if (!isShard) {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                           Furnace f = (Furnace)event.getBlock().getState();
                           FurnaceInventory i = f.getInventory();
                           ItemStack sm = i.getSmelting();
                           if (sm != null) {
                              i.setSmelting(sm);
                           }
                        });
                        return;
                     }
                  }

                  int room = Math.max(0, 64 - base);
                  if (room <= 0) {
                     event.setCancelled(true);
                     Bukkit.getScheduler().runTask(this.plugin, () -> {
                        Furnace f = (Furnace)event.getBlock().getState();
                        FurnaceInventory i = f.getInventory();
                        ItemStack sm = i.getSmelting();
                        if (sm != null) {
                           i.setSmelting(sm);
                        }
                     });
                     return;
                  }

                  int toAdd = Math.min(shardCount, room);
                  ItemStack result = this.utils.createShardItem(toAdd);
                  event.setResult(result);
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     Furnace furnace1 = (Furnace)event.getBlock().getState();
                     FurnaceInventory inv1 = furnace1.getInventory();
                     ItemStack smelting = inv1.getSmelting();
                     if (smelting != null) {
                        inv1.setSmelting(smelting);
                     }
                  });
               }
            } else {
               event.setCancelled(true);
            }
         }
      }
   }

   private int calculateShardCount(Utils.SphereType sphereType) {
      switch (sphereType) {
         case DEFAULT:
            return this.random.nextInt(4) + 1;
         case EPIC:
            return this.random.nextInt(7) + 3;
         case LEGENDARY:
            return this.random.nextInt(7) + 9;
         case MYTHIC:
            return this.random.nextInt(11) + 15;
         case CUSTOM:
            return this.random.nextInt(11) + 20;
         default:
            return 1;
      }
   }
}
