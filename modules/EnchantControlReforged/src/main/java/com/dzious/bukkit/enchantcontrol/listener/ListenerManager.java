package com.dzious.bukkit.enchantcontrol.listener;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.enchantments.Enchantment;

public class ListenerManager {
   private final EnchantControl plugin;
   private Map<String, Boolean> configurableListenersValues = new HashMap<>();

   public ListenerManager(EnchantControl plugin) {
      this.plugin = plugin;
      this.initMap();

      for (Entry<String, Boolean> listener : this.configurableListenersValues.entrySet()) {
         if (plugin.getConfigManager().doPathExist(listener.getKey())) {
            this.configurableListenersValues.replace(listener.getKey(), plugin.getConfigManager().getBooleanFromPath(listener.getKey()));
         }
      }
   }

   private void initMap() {
      this.configurableListenersValues.put("mending_repair", true);
      this.configurableListenersValues.put("update_inventory", false);
   }

   public void onEnable() {
      this.plugin.getLogManager().logInfo("ListenerManager OnEnable");
      this.plugin.getServer().getPluginManager().registerEvents(new AnvilListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new EnchantmentTableListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new FishingListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new LootGenerationListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new MobsLootListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new PlayerEnchantmentListener(this.plugin), this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(new VillagerTradeListener(this.plugin), this.plugin);
      if (this.plugin.getEnchantmentManager().getAffectedEnchantments().get(Enchantment.MENDING) < 1 && !this.configurableListenersValues.get("mending_repair")
         )
       {
         this.plugin.getServer().getPluginManager().registerEvents(new MendingCancelerListener(), this.plugin);
      }

      if (this.configurableListenersValues.get("update_inventory")) {
         this.plugin.getLogManager().logDebugConsole("Here");
         this.plugin.getServer().getPluginManager().registerEvents(new InventoryListener(this.plugin), this.plugin);
      }
   }
}
