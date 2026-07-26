package ua.fichadev.fichaexpbottle.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichaexpbottle.FichaExpBottle;

public class BottleUseListener implements Listener {
   public BottleUseListener() {
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE || !item.hasItemMeta()) {
               return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasCustomModelData()) {
               return;
            }

            int expAmount = meta.getCustomModelData();
            ConfigurationSection bottleList = FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles");
            if (bottleList != null) {
               boolean isValid = false;

               for (String key : bottleList.getKeys(false)) {
                  ConfigurationSection section = bottleList.getConfigurationSection(key);
                  if (section != null && section.getInt("value") == expAmount) {
                     isValid = true;
                     break;
                  }
               }

               if (isValid) {
                  event.setCancelled(true);
                  if (item.getAmount() > 1) {
                     item.setAmount(item.getAmount() - 1);
                  } else {
                     player.getInventory().setItemInMainHand(null);
                  }

                  ExperienceOrb orb = (ExperienceOrb)player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
                  orb.setExperience(expAmount);
                  player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0F, 1.0F);
                  player.updateInventory();
               }
            }
         }
      }
   }
}
