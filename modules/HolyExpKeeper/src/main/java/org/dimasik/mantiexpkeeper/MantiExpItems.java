package org.dimasik.mantiexpkeeper;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class MantiExpItems {
   private MantiExpKeeper plugin;
   private Player player;
   Inventory inventory;

   public MantiExpItems(MantiExpKeeper plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.open();
   }

   public void open() {
      this.inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.plugin.itemsTitle);
      ItemStack glassItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta glassMeta = glassItem.getItemMeta();
      glassMeta.setDisplayName(" ");
      glassMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "null");
      glassItem.setItemMeta(glassMeta);
      int[] backgroundSlots = new int[]{
         0,
         1,
         2,
         6,
         7,
         8,
         9,
         10,
         11,
         12,
         13,
         14,
         15,
         16,
         17,
         18,
         19,
         20,
         24,
         25,
         26,
         27,
         28,
         29,
         33,
         34,
         35,
         36,
         37,
         38,
         39,
         40,
         41,
         42,
         43,
         44,
         45,
         46,
         47,
         48,
         49,
         50,
         51,
         52,
         53
      };

      for (int slot : backgroundSlots) {
         this.inventory.setItem(slot, glassItem);
      }

      ItemStack abilitiesItem = new ItemStack(Material.FEATHER);
      ItemMeta abilitiesMeta = abilitiesItem.getItemMeta();
      abilitiesMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a▶ Эффекты хранителя"));
      abilitiesMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "cathegory_abilities");
      List<String> abilitiesLore = new ArrayList<>();
      abilitiesLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      abilitiesLore.add(ChatColor.translateAlternateColorCodes('&', "&a &n▪&f Все предметы и эффекты, которые"));
      abilitiesLore.add(ChatColor.translateAlternateColorCodes('&', "&a ▪&f возможно получить у Хранителя Опыта."));
      abilitiesMeta.setLore(abilitiesLore);
      abilitiesItem.setItemMeta(abilitiesMeta);
      this.inventory.setItem(3, abilitiesItem);
      ItemStack itemsItem = new ItemStack(Material.JACK_O_LANTERN);
      ItemMeta itemsMeta = itemsItem.getItemMeta();
      itemsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6▶ Прочие предметы"));
      itemsMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "cathegory_items");
      List<String> itemsLore = new ArrayList<>();
      itemsLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemsLore.add(ChatColor.translateAlternateColorCodes('&', "&6 &n▪&f Прочие полезные предметы, которые"));
      itemsLore.add(ChatColor.translateAlternateColorCodes('&', "&6 ▪&f возможно получить у Хранителя Опыта."));
      itemsMeta.setLore(itemsLore);
      itemsMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemsMeta.addEnchant(Enchantment.LUCK, 1, false);
      itemsItem.setItemMeta(itemsMeta);
      this.inventory.setItem(4, itemsItem);
      ItemStack potionsItem = new ItemStack(Material.POTION);
      ItemMeta potionsMeta = potionsItem.getItemMeta();
      PotionMeta potionMeta = (PotionMeta)potionsMeta;
      potionMeta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE, false, false));
      potionsMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
      potionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d▶ Зелья хранителя"));
      potionsMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "cathegory_effects");
      List<String> potionsLore = new ArrayList<>();
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', "&d &n▪&f Зелья с долгими эффектами, которые"));
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', "&d ▪&f возможно получить у Хранителя Опыта."));
      potionsMeta.setLore(potionsLore);
      potionsItem.setItemMeta(potionsMeta);
      this.inventory.setItem(5, potionsItem);
      this.setupItem("explosive", 20, 21);
      this.setupItem("jake", 25, 22);
      this.setupItem("bottle", 50, 23);
      this.setupItem("snowball", 25, 30);
      this.setupItem("jump_three", 30, 31);
      this.setupItem("artefact", 150, 32);
      this.player.openInventory(this.inventory);
   }

   private void setupItem(String type, int pricee, int slot) {
      ItemStack itemStack;
      switch (type) {
         case "explosive":
            itemStack = this.plugin.mantiExpItemUtil.getExplosive(1);
            break;
         case "jake":
            itemStack = this.plugin.mantiExpItemUtil.getJake(1);
            break;
         case "bottle":
            itemStack = this.plugin.mantiExpItemUtil.getBottle(1);
            break;
         case "snowball":
            itemStack = this.plugin.mantiExpItemUtil.getSnowball(1);
            break;
         case "jump_three":
            itemStack = this.plugin.mantiExpItemUtil.getJumpThree(1);
            break;
         case "artefact":
            itemStack = this.plugin.mantiExpItemUtil.getArtefact();
            break;
         default:
            return;
      }

      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, type);
      List<String> lore = itemMeta.getLore();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&0&5&F&B&0&0" + pricee + " ур. опыта &7(ЛКМ)"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(slot, itemStack);
   }
}
