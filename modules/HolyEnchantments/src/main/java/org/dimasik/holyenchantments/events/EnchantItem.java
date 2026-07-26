package org.dimasik.holyenchantments.events;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;
import org.dimasik.holyenchantments.utils.Random;

public class EnchantItem implements Listener {
   private final HolyEnchantments plugin;

   public EnchantItem(HolyEnchantments plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEnchant(EnchantItemEvent event) {
      ItemStack itemStack = event.getItem();
      Material material = itemStack.getType();
      List<MEnchant> enchants = this.plugin
         .getEnchantManager()
         .getEnchantsNameSpaced()
         .values()
         .stream()
         .filter(mEnchant -> mEnchant.getTools().contains(material))
         .collect(Collectors.toList());
      if (!enchants.isEmpty()) {
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            for (MEnchant menchant : enchants) {
               int chance = menchant.getLevels().values().stream().mapToInt(AbstractLevel::getEnchantTableChance).sum();
               if (Random.getPercent() <= chance) {
                  int level = Random.generateLevelForEnchantTable(menchant.getLevels());
                  itemStack.setItemMeta(this.plugin.getEnchantManager().applyEnchant(itemStack.getItemMeta(), menchant, level));
               }
            }
         });
      }
   }
}
