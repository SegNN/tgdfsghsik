package ru.kotikstasika.kotikdang.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.data.UniqueShulkerSession;
import ru.kotikstasika.kotikdang.util.HologramManager;

public class UniqueShulkerOpenTask extends BukkitRunnable {
   private final KotikDang plugin;
   private final UniqueShulkerSession session;
   private final HologramManager hologramManager;
   private final List<ItemStack> lootItems;
   private final List<Integer> availableSlots;
   private int currentItemIndex = 0;
   private int tickCount = 0;
   private static final int FIXED_ITEM_SPAWN_INTERVAL = 1;

   public UniqueShulkerOpenTask(KotikDang plugin, UniqueShulkerSession session, HologramManager hologramManager) {
      this.plugin = plugin;
      this.session = session;
      this.hologramManager = hologramManager;
      int menuSize = plugin.getConfig().getInt("menu.size", 36);
      this.lootItems = plugin.getDangUniversalInstance().generateLoot(session.getShulkerLocation().getCurrentRarity(), menuSize);
      this.availableSlots = new ArrayList<>();

      for (int i = 0; i < menuSize; i++) {
         this.availableSlots.add(i);
      }

      Collections.shuffle(this.availableSlots);
   }

   public void run() {
      this.tickCount++;
      Player player = this.session.getPlayer();
      if (!player.isOnline()) {
         this.plugin.getDangUniversalInstance().endSession(player, false);
      } else {
         this.hologramManager.updateOpeningHologram(this.session);
         if (this.tickCount % 1 == 0 && this.currentItemIndex < this.lootItems.size() && this.currentItemIndex < this.availableSlots.size()) {
            this.spawnItem();
         }

         if (this.session.getRemainingSeconds() <= 0) {
            this.plugin.getDangUniversalInstance().endSession(player, true);
         }
      }
   }

   private void spawnItem() {
      if (this.currentItemIndex < this.lootItems.size() && this.currentItemIndex < this.availableSlots.size()) {
         Inventory inventory = this.session.getInventory();
         int slot = this.availableSlots.get(this.currentItemIndex);
         ItemStack item = this.lootItems.get(this.currentItemIndex);
         inventory.setItem(slot, item);
         Player player = this.session.getPlayer();
         player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.5F);
         this.currentItemIndex++;
      }
   }
}
