package org.nezxenka.litecontainer.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nezxenka.litecontainer.LiteContainer;
import org.nezxenka.litecontainer.config.DatabaseManager;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.config.PluginConfigManager;
import org.nezxenka.litecontainer.utils.ColorUtils;

public class ContainerOpenMenu extends Menu {
   private final PluginConfigManager config;
   private final DatabaseManager databaseManager;
   private final LootManager lootManager;

   public ContainerOpenMenu(PluginConfigManager configManager, DatabaseManager databaseManager, LootManager lootManager) {
      this.databaseManager = databaseManager;
      this.config = configManager;
      this.lootManager = lootManager;
   }

   @Override
   public String getTitle() {
      return ColorUtils.colorize(this.config.getOpenMenuConfig().getString("title", "Контейнеры"));
   }

   @Override
   public int getSize() {
      return this.config.getOpenMenuConfig().getInt("rows", 3) * 9;
   }

   @Override
   public void getItemsAndButtons() {
      this.addOpenItem();
      this.addDecor();
   }

   private void addOpenItem() {
      ConfigurationSection itemSection = this.config.getOpenMenuConfig().getConfigurationSection("items.open_item");
      if (itemSection != null) {
         final int containers = this.databaseManager.getValue(this.getOpener().getName());
         ItemStack item = this.createItem(itemSection, containers);
         this.buttons.add(new Button(item, new Integer[]{itemSection.getInt("slot")}) {
            @Override
            public void onClick(InventoryClickEvent e) {
               if (e.getWhoClicked() instanceof Player player) {
                  if (containers > 0) {
                     new ContainerAnimationMenu(ContainerOpenMenu.this.config, ContainerOpenMenu.this.lootManager).show(player);
                     ContainerOpenMenu.this.async(() -> ContainerOpenMenu.this.databaseManager.setValue(player.getName(), containers - 1));
                  } else {
                     for (String message : ContainerOpenMenu.this.config.getMainConfig().getStringList("messages.no_keys_message")) {
                        player.sendMessage(ColorUtils.colorize(message));
                     }
                  }
               }
            }
         });
      }
   }

   private ItemStack createItem(ConfigurationSection section, int containers) {
      Material material = Material.valueOf(section.getString("material", "RED_SHULKER_BOX").toUpperCase());
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String displayName = section.getString("display_name", "").replace("{containers}", String.valueOf(containers));
         meta.setDisplayName(ColorUtils.colorize(displayName));
         List<String> lore = section.getStringList("lore");

         for (int i = 0; i < lore.size(); i++) {
            lore.set(i, ColorUtils.colorize(lore.get(i).replace("{containers}", String.valueOf(containers))));
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private void addDecor() {
      ConfigurationSection decorSection = this.config.getOpenMenuConfig().getConfigurationSection("decor");
      if (decorSection != null) {
         for (String key : decorSection.getKeys(false)) {
            try {
               Material material = Material.valueOf(key.toUpperCase());
               this.addDecorItems(material, decorSection.getIntegerList(key));
            } catch (IllegalArgumentException var5) {
               LiteContainer.getInstance().getLogger().severe("Ошибка при загрузке декораций: " + var5.getMessage());
            }
         }
      }
   }

   private void async(Runnable runnable) {
      CompletableFuture.runAsync(runnable);
   }
}
