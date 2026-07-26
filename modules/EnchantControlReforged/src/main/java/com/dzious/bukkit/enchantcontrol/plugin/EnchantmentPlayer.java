package com.dzious.bukkit.enchantcontrol.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;

public class EnchantmentPlayer {
   List<Map<Enchantment, EnchantmentOffer>> registeredOffers = new ArrayList<>();

   public EnchantmentPlayer() {
   }

   public void setOffers(EnchantmentOffer[] newOffers, List<Enchantment> oldOffers) {
      this.registeredOffers.clear();

      for (int i = 0; i < newOffers.length; i++) {
         Map<Enchantment, EnchantmentOffer> offer = new HashMap<>();
         offer.put(oldOffers.get(i), newOffers[i]);
         this.registeredOffers.add(offer);
      }
   }

   public boolean hasOffers() {
      return !this.registeredOffers.isEmpty();
   }

   public void clearOffers() {
      this.registeredOffers.clear();
   }

   public Map<Enchantment, EnchantmentOffer> getOffers(int requestedIdx) {
      Map<Enchantment, EnchantmentOffer> offers = this.registeredOffers.get(requestedIdx);
      this.registeredOffers.clear();
      return offers;
   }
}
