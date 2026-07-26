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

public class MantiExpEffects {
   private MantiExpKeeper plugin;
   private Player player;
   Inventory inventory;

   public MantiExpEffects(MantiExpKeeper plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.open();
   }

   public void open() {
      this.inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.plugin.effectsTitle);
      this.setupBackground();
      this.setupCategoryItems();
      this.setupEffectItems();
      this.player.openInventory(this.inventory);
   }

   private void setupBackground() {
      ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(" ");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "null");
      itemStack.setItemMeta(itemMeta);
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
         26,
         27,
         28,
         29,
         30,
         31,
         32,
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
         this.inventory.setItem(slot, itemStack);
      }
   }

   private void setupCategoryItems() {
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
      itemsItem.setItemMeta(itemsMeta);
      this.inventory.setItem(4, itemsItem);
      ItemStack potionsItem = new ItemStack(Material.POTION);
      ItemMeta potionsMeta = potionsItem.getItemMeta();
      PotionMeta potionMeta = (PotionMeta)potionsMeta;
      potionMeta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE, false, false));
      potionsMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      potionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d▶ Зелья хранителя"));
      potionsMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "cathegory_effects");
      List<String> potionsLore = new ArrayList<>();
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', ""));
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', "&d &n▪&f Зелья с долгими эффектами, которые"));
      potionsLore.add(ChatColor.translateAlternateColorCodes('&', "&d ▪&f возможно получить у Хранителя Опыта."));
      potionsMeta.setLore(potionsLore);
      potionsMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      potionsMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
      potionsItem.setItemMeta(potionsMeta);
      this.inventory.setItem(5, potionsItem);
   }

   private void setupEffectItems() {
      this.addEffect("haste", "Спешка I", 100, 100, PotionType.SLOW_FALLING, 18, true);
      this.addEffect("fire_resistance", "Огнестойкость", 100, 100, PotionType.FIRE_RESISTANCE, 19, false);
      this.addEffect("water_breath", "Водное дыхание", 100, 100, PotionType.WATER_BREATHING, 20, false);
      this.addEffect("night_vision", "Ночное зрение", 100, 100, PotionType.WATER_BREATHING, 21, false);
      this.addEffect("saturation", "Сытость", 100, 100, PotionType.FIRE_RESISTANCE, 22, true);
      this.addEffect("speed3", "Скорость III", 225, 150, PotionType.SWIFTNESS, 23, false);
      this.addEffect("regeneration", "Регенерация I", 300, 175, PotionType.REGENERATION, 24, false);
      this.addEffect("resistance", "Сопротивление I", 300, 175, PotionType.TURTLE_MASTER, 25, false);
      this.addEffect("health_boost", "Прилив здоровья I", 400, 200, PotionType.HEALING, 26, false);
   }

   private void addEffect(String type, String name, int pricet, int pricee, PotionType potion, int slot, boolean noPvp) {
      ItemStack itemStack = new ItemStack(Material.POTION);
      ItemMeta itemMeta = itemStack.getItemMeta();
      PotionMeta potionMeta = (PotionMeta)itemMeta;
      potionMeta.setBasePotionData(new PotionData(potion, false, false));
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F " + name));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, type);
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &7Особенности:"));
      if (noPvp) {
         lore.add(ChatColor.translateAlternateColorCodes('&', " &x&E&1&5&5&F&F- не действует в PvP"));
      }

      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&E&1&5&5&F&F- после покупки на вас накладывается"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&E&1&5&5&F&F  эффект «" + name + "» на 24 часа."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&0&5&F&B&0&0" + pricee + " ур. опыта &7(ЛКМ)"));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "pricee"), PersistentDataType.INTEGER, pricee);
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&F&F&7&0&0&0" + pricet + " сапфиров &7(ПКМ)"));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "pricet"), PersistentDataType.INTEGER, pricet);
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(slot, itemStack);
      this.plugin.hasAbility(this.player.getName(), type).thenAcceptAsync(hasAbility -> {
         if (hasAbility) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               ItemStack purchasedItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
               ItemMeta purchasedMeta = purchasedItem.getItemMeta();
               purchasedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " "));
               purchasedMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "null");
               this.plugin.getAbilityTime(this.player.getName(), type).thenAcceptAsync(abilityTime -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  ArrayList<String> purchasedLore = new ArrayList<>();
                  purchasedLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F &n▪&f Эффект « &x&E&1&5&5&F&F" + name + " &f» уже приобретён"));
                  purchasedLore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F ▪&f Эффект истекает: &x&E&1&5&5&F&F" + abilityTime));
                  purchasedLore.add(ChatColor.translateAlternateColorCodes('&', ""));
                  purchasedMeta.setLore(purchasedLore);
                  purchasedItem.setItemMeta(purchasedMeta);
                  this.inventory.setItem(slot, purchasedItem);
               }));
            });
         }
      });
   }
}
