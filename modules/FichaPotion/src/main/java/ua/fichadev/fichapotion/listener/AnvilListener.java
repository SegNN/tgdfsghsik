package ua.fichadev.fichapotion.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import ua.fichadev.fichapotion.FichaPotion;
import ua.fichadev.fichapotion.util.ExpUtil;
import ua.fichadev.fichapotion.util.PotionManager;

public class AnvilListener implements Listener {
   private final FichaPotion plugin;

   public AnvilListener(FichaPotion plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      AnvilInventory inv = event.getInventory();
      ItemStack first = inv.getFirstItem();
      ItemStack second = inv.getSecondItem();
      if (first != null && second != null) {
         if (first.getType() == Material.POTION && second.getType() == Material.POTION) {
            if (first.getAmount() == second.getAmount()) {
               PotionMeta firstMeta = (PotionMeta)first.getItemMeta();
               PotionMeta secondMeta = (PotionMeta)second.getItemMeta();
               if (firstMeta != null && secondMeta != null) {
                  PotionData firstData = firstMeta.getBasePotionData();
                  PotionData secondData = secondMeta.getBasePotionData();
                  PotionManager pm = this.plugin.getPotionManager();
                  String firstType = pm.getPotionType(first);
                  String secondType = pm.getPotionType(second);
                  String resultType = this.matchRecipe(firstData, secondData, firstType, secondType);
                  if (resultType != null) {
                     int cost = ExpUtil.getLevelsFromExp(first.getAmount() * 280);
                     inv.setRepairCost(cost);
                     ItemStack result = pm.getPotion(resultType);
                     if (result != null) {
                        result.setAmount(first.getAmount());
                        event.setResult(result);
                     }
                  }
               }
            }
         }
      }
   }

   private String matchRecipe(PotionData first, PotionData second, String firstType, String secondType) {
      if (first.getType() == PotionType.STRENGTH && second.getType() == PotionType.STRENGTH) {
         if (!first.isExtended() && !second.isExtended() && first.isUpgraded() && second.isUpgraded()) {
            return "strenght";
         }

         if (!first.isExtended() && !second.isExtended() && !first.isUpgraded() && !second.isUpgraded()) {
            return "strenght1";
         }
      }

      if (first.getType() == PotionType.SPEED && second.getType() == PotionType.SPEED) {
         if (!first.isExtended() && !second.isExtended() && first.isUpgraded() && second.isUpgraded()) {
            return "speed";
         }

         if (!first.isExtended() && !second.isExtended() && !first.isUpgraded() && !second.isUpgraded()) {
            return "speed1";
         }
      }

      if (first.getType() == PotionType.INSTANT_HEAL
         && second.getType() == PotionType.INSTANT_HEAL
         && !first.isExtended()
         && !second.isExtended()
         && first.isUpgraded()
         && second.isUpgraded()) {
         return "heal";
      } else if (firstType.equalsIgnoreCase("strenght1") && secondType.equalsIgnoreCase("strenght1")) {
         return "strenght2";
      } else {
         return firstType.equalsIgnoreCase("speed1") && secondType.equalsIgnoreCase("speed1") ? "speed2" : null;
      }
   }
}
