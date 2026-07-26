package ru.kotikstasika.eventpolana.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.eventpolana.EventPolana;
import ru.kotikstasika.eventpolana.config.PluginConfig;

public class ShearsService {
   private final EventPolana plugin;
   private final PluginConfig config;
   private final NamespacedKey durabilityKey;
   private final Random random = new Random();

   public ShearsService(EventPolana plugin) {
      this.plugin = plugin;
      this.config = new PluginConfig(plugin);
      this.durabilityKey = new NamespacedKey(plugin, "shears");
   }

   public ItemStack createCustomShears() {
      ItemStack shears = new ItemStack(Material.SHEARS);
      ItemMeta meta = shears.getItemMeta();
      if (meta == null) {
         return shears;
      } else {
         int minDurability = this.config.getShearsMinDurability();
         int maxDurabilityConfig = this.config.getShearsMaxDurability();
         int durability = this.random.nextInt(maxDurabilityConfig - minDurability + 1) + minDurability;
         meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getShearsDisplayName()));
         meta.addEnchant(Enchantment.CHANNELING, 1, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         List<String> lore = new ArrayList<>();
         List<String> configDescription = this.config.getShearsDescription();
         if (configDescription != null && !configDescription.isEmpty()) {
            for (String line : configDescription) {
               String formattedLine = line.replace("{durability}", String.valueOf(durability));
               lore.add(ChatColor.translateAlternateColorCodes('&', formattedLine));
            }
         } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8Прочность: " + durability));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Особенности"));
            lore.add(ChatColor.translateAlternateColorCodes('&', " &f- используется &6на мероприятии «Цветочная поляна»&f,"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "   &6для нахождения драгоценных вещей&f,"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "   &fсрывая цветы в зоне мероприятия."));
            lore.add("");
         }

         meta.setLore(lore);
         PersistentDataContainer container = meta.getPersistentDataContainer();
         container.set(this.durabilityKey, PersistentDataType.INTEGER, durability);
         short maxDurability = shears.getType().getMaxDurability();
         shears.setDurability((short)(maxDurability - durability));
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         shears.setItemMeta(meta);
         return shears;
      }
   }

   public boolean isCustomShears(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() == Material.SHEARS) {
         ItemMeta meta = itemStack.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.durabilityKey, PersistentDataType.INTEGER);
      } else {
         return false;
      }
   }

   public int getDurability(ItemStack shears) {
      ItemMeta meta = shears.getItemMeta();
      if (meta == null) {
         return 0;
      } else {
         Integer d = (Integer)meta.getPersistentDataContainer().get(this.durabilityKey, PersistentDataType.INTEGER);
         return d == null ? 0 : d;
      }
   }

   public void setDurability(ItemStack shears, int durability) {
      ItemMeta meta = shears.getItemMeta();
      if (meta != null) {
         meta.getPersistentDataContainer().set(this.durabilityKey, PersistentDataType.INTEGER, durability);
         List<String> lore = meta.getLore();
         if (lore != null && !lore.isEmpty()) {
            String firstLine = lore.get(0);
            if (firstLine.contains("Прочность:")) {
               lore.set(0, ChatColor.translateAlternateColorCodes('&', "&8Прочность: " + durability));
               meta.setLore(lore);
            }
         }

         short maxDurability = shears.getType().getMaxDurability();
         shears.setDurability((short)(maxDurability - durability));
         shears.setItemMeta(meta);
      }
   }
}
