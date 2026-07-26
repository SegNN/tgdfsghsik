package ua.fichadev.fichaeventsun.task;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.lootmanager.LootAPI;
import ua.fichadev.fichaeventsun.FichaEventSun;

public class LootDropTask {
   private final FichaEventSun plugin;

   public LootDropTask(FichaEventSun plugin) {
      this.plugin = plugin;
   }

   public void start() {
      final int interval = this.plugin.getCfg().getLootDropIntervalTicks();
      (new BukkitRunnable() {
         public void run() {
            LootDropTask.this.dropLoot();
            LootDropTask.this.plugin.getHologramManager().setLootCountdown(interval / 20);
         }
      }).runTaskTimer(this.plugin, interval, interval);
   }

   private void dropLoot() {
      Location coreLoc = this.plugin.getCfg().getCoreLocation();
      if (coreLoc != null && coreLoc.getWorld() != null) {
         List<Player> players = this.plugin.getRewardManager().getPlayersInRadius();
         if (!players.isEmpty()) {
            String source = this.plugin.getCfg().getLootSource();
            List<ItemStack> items = new ArrayList<>();
            if (source.equalsIgnoreCase("lootmanager") || source.equalsIgnoreCase("both")) {
               items.addAll(this.getLootManagerItems());
            }

            if (source.equalsIgnoreCase("custom") || source.equalsIgnoreCase("both")) {
               items.addAll(this.getCustomItems());
            }

            if (!items.isEmpty()) {
               Location dropLoc = coreLoc.clone().add(0.5, 1.0, 0.5);

               for (ItemStack item : items) {
                  if (item != null) {
                     coreLoc.getWorld().dropItemNaturally(dropLoc, item);
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> getLootManagerItems() {
      int level = this.plugin.getCfg().getLootLevel();
      int count = this.plugin.getCfg().getLootCount();
      String category = this.plugin.getCfg().getLootCategory();
      LootAPI lootAPI = new LootAPI();
      List<ItemStack> items;
      if (category != null && !category.isEmpty()) {
         items = lootAPI.getRandomItemsByCategoryByLvL(count, category, level);
      } else {
         items = lootAPI.getRandomItemsByExactLevel(count, level);
      }

      return (List<ItemStack>)(items != null ? items : new ArrayList<>());
   }

   private List<ItemStack> getCustomItems() {
      List<ItemStack> result = new ArrayList<>();
      int count = this.plugin.getCfg().getCustomLootCount();

      for (int i = 0; i < count; i++) {
         ItemStack item = this.plugin.getCustomLootManager().getRandomItem();
         if (item != null) {
            result.add(item);
         }
      }

      return result;
   }
}
