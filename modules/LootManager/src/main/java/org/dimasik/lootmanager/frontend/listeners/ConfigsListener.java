package org.dimasik.lootmanager.frontend.listeners;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.Config;
import org.dimasik.lootmanager.frontend.menus.Configs;
import org.dimasik.lootmanager.frontend.menus.Main;
import org.dimasik.lootmanager.frontend.menus.NameSelect;

public class ConfigsListener implements Listener {
   public ConfigsListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof Configs) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         Configs configs = (Configs)inventory.getHolder();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();

         try {
            if (slot < 45) {
               String config = configs.getConfigs().get(slot);
               if (config != null) {
                  new Config(config, configs).setPlayer(player).compile().open();
               }
            } else if (slot == 45) {
               Main main = configs.getBack();
               main.compile().open();
            } else if (slot == 47) {
               player.sendMessage(Parser.color("&#00D4FB▶ &fКонфиги обновлены."));
               player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0F, 1.0F);
               int newPage = configs.getPage();
               List<String> cfgs = LootManager.getInstance().getDatabaseManager().getConfigNames().get();
               int pages = cfgs.size() / 45 + (cfgs.size() % 45 == 0 ? 0 : 1);
               newPage = Math.min(pages, newPage);
               newPage = Math.max(1, newPage);
               Configs newConfigs = new Configs(newPage, configs.getBack()).setPlayer(player).compile();
               newConfigs.open();
            } else if (slot == 48) {
               int newPage = configs.getPage() - 1;
               List<String> cfgs = LootManager.getInstance().getDatabaseManager().getConfigNames().get();
               int pages = cfgs.size() / 45 + (cfgs.size() % 45 == 0 ? 0 : 1);
               newPage = Math.min(pages, newPage);
               newPage = Math.max(1, newPage);
               if (newPage != configs.getPage()) {
                  player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
                  Configs newConfigs = new Configs(newPage, configs.getBack()).setPlayer(player).compile();
                  newConfigs.open();
               }
            } else if (slot == 50) {
               int newPage = configs.getPage() + 1;
               List<String> cfgs = LootManager.getInstance().getDatabaseManager().getConfigNames().get();
               int pages = cfgs.size() / 45 + (cfgs.size() % 45 == 0 ? 0 : 1);
               newPage = Math.min(pages, newPage);
               newPage = Math.max(1, newPage);
               if (newPage != configs.getPage()) {
                  player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
                  Configs newConfigs = new Configs(newPage, configs.getBack()).setPlayer(player).compile();
                  newConfigs.open();
               }
            } else if (slot == 53) {
               player.closeInventory();
               Bukkit.getScheduler().runTaskLater(LootManager.getInstance(), () -> new NameSelect(player, "", configs), 1L);
            }
         } catch (Exception var10) {
            throw new RuntimeException(var10);
         }
      }
   }
}
