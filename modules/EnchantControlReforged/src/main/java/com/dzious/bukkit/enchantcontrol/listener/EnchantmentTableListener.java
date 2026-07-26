package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class EnchantmentTableListener implements Listener {
   private final EnchantControl plugin;

   public EnchantmentTableListener(EnchantControl plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEnchantmentPreparation(PrepareItemEnchantEvent e) {
      if (e.getItem().getType() == Material.BOOK || this.plugin.getEnchantmentManager().hasEnchantActive(e.getItem())) {
         this.plugin.getLogManager().logDebugConsole("Generating offers : ");
         List<Enchantment> offersList = new ArrayList<>();

         for (int i = 0; i < e.getOffers().length; i++) {
            if (e.getOffers()[i] != null) {
               this.plugin.getLogManager().logDebugConsole("Offer " + i + " is " + ChatColor.GREEN + e.getOffers()[i].getEnchantment().toString());
               offersList.add(e.getOffers()[i].getEnchantment());
            } else {
               this.plugin.getLogManager().logDebugConsole("Offer " + i + " is " + ChatColor.GREEN + "null");
               offersList.add(null);
            }
         }

         for (int ix = 0; ix < e.getOffers().length; ix++) {
            if (e.getOffers()[ix] != null) {
               if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(e.getOffers()[ix].getEnchantment()) <= 0) {
                  Enchantment newEnchantment = this.plugin.getEnchantmentManager().rerollEnchantment(e.getItem(), offersList);
                  if (newEnchantment != null) {
                     e.getOffers()[ix].setEnchantment(newEnchantment);
                     if (e.getOffers()[ix].getEnchantmentLevel() > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment)) {
                        e.getOffers()[ix].setEnchantmentLevel(this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment));
                     }
                  } else {
                     e.getOffers()[ix] = null;
                  }
               } else if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(e.getOffers()[ix].getEnchantment())
                  < e.getOffers()[ix].getEnchantmentLevel()) {
                  e.getOffers()[ix].setEnchantmentLevel(this.plugin.getEnchantmentManager().getAffectedEnchantments().get(e.getOffers()[ix].getEnchantment()));
               }
            }
         }

         for (int ixx = 0; ixx < e.getOffers().length; ixx++) {
            if (e.getOffers()[ixx] != null) {
               this.plugin.getLogManager().logDebugConsole("Final offer " + ixx + " is " + ChatColor.GREEN + e.getOffers()[ixx].getEnchantment().toString());
            } else {
               this.plugin.getLogManager().logDebugConsole("Final offer " + ixx + " is " + ChatColor.GREEN + "null");
            }
         }

         this.plugin.getLogManager().logDebugConsole(offersList.toString());
         this.plugin.getPlayersManager().getPlayer(e.getEnchanter().getUniqueId()).setOffers(e.getOffers(), offersList);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEnchantItem(EnchantItemEvent e) {
      Map<Enchantment, EnchantmentOffer> offers = this.plugin.getPlayersManager().getPlayer(e.getEnchanter().getUniqueId()).getOffers(e.whichButton());

      for (Entry<Enchantment, Integer> enchantment : e.getEnchantsToAdd().entrySet()) {
         if (offers.containsKey(enchantment.getKey())) {
            e.getEnchantsToAdd().remove(enchantment.getKey());
            e.getEnchantsToAdd().put(offers.get(enchantment.getKey()).getEnchantment(), offers.get(enchantment.getKey()).getEnchantmentLevel());
            break;
         }
      }

      List<Enchantment> enchantments = new ArrayList<>();

      for (Entry<Enchantment, Integer> enchantmentx : e.getEnchantsToAdd().entrySet()) {
         enchantments.add(enchantmentx.getKey());
      }

      for (Entry<Enchantment, Integer> enchantmentx : e.getEnchantsToAdd().entrySet()) {
         if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()) <= 0) {
            Enchantment newEnchantment = this.plugin.getEnchantmentManager().rerollEnchantment(e.getItem(), enchantments);
            if (newEnchantment != null) {
               Map<Enchantment, Integer> newApplicableEnchantment = new HashMap<>();
               if (enchantmentx.getValue() > this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment)) {
                  newApplicableEnchantment.put(newEnchantment, this.plugin.getEnchantmentManager().getAffectedEnchantments().get(newEnchantment));
               } else {
                  newApplicableEnchantment.put(newEnchantment, enchantmentx.getValue());
               }
            }

            e.getEnchantsToAdd().remove(enchantmentx.getKey());
         } else if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey()) < enchantmentx.getValue()) {
            e.getEnchantsToAdd()
               .replace(
                  enchantmentx.getKey(), enchantmentx.getValue(), this.plugin.getEnchantmentManager().getAffectedEnchantments().get(enchantmentx.getKey())
               );
         }
      }

      this.plugin.getLogManager().logDebugConsole(ChatColor.BLUE + "Final Enchantments from " + e.whichButton() + " : ");
      this.plugin.getLogManager().logDebugConsole(ChatColor.GREEN + e.getEnchantsToAdd().toString());
   }
}
