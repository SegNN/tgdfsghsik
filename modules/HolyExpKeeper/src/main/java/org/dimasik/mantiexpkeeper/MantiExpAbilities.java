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

public class MantiExpAbilities {
   private MantiExpKeeper plugin;
   private Player player;
   Inventory inventory;

   public MantiExpAbilities(MantiExpKeeper plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.open();
   }

   public void open() {
      this.inventory = Bukkit.createInventory((InventoryHolder)null, 54, this.plugin.effectsAbilities);
      this.setupBackground();
      this.setupCategoryItems();
      this.setupItemPurchases();
      this.setupAbilityItems();
      this.player.openInventory(this.inventory);
   }

   private void setupBackground() {
      ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(" ");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "null");
      itemStack.setItemMeta(itemMeta);
      int[] backgroundSlots = new int[]{
         0, 1, 2, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 22, 26, 27, 31, 35, 36, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
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
      abilitiesMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      abilitiesMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
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
      potionsItem.setItemMeta(potionsMeta);
      this.inventory.setItem(5, potionsItem);
   }

   private void setupItemPurchases() {
      this.addItemPurchase("hunter", 100, 100, 19);
      this.addItemPurchase("snowman", 110, 100, 20);
      this.addItemPurchase("illuminator", 110, 100, 21);
      this.addItemPurchase("enderman", 150, 125, 28);
      this.addItemPurchase("antifantom", 150, 125, 29);
      this.addItemPurchase("telekines", 225, 150, 30);
      this.addItemPurchase("gravity", 225, 150, 37);
      this.addItemPurchase("vampire", 300, 175, 38);
      this.addItemPurchase("justice", 300, 175, 39);
   }

   private void setupAbilityItems() {
      this.addAbilityItem("hunter", "Охотник", 275, 165, 23);
      this.addAbilityItem("snowman", "Снеговик", 300, 175, 24);
      this.addAbilityItem("illuminator", "Иллюминатор", 300, 175, 25);
      this.addAbilityItem("enderman", "Эндермен", 400, 200, 32);
      this.addAbilityItem("antifantom", "АнтиФантом", 400, 200, 33);
      this.addAbilityItem("telekines", "Телекинез", 600, 250, 34);
      this.addAbilityItem("gravity", "Гравитация", 600, 250, 41);
      this.addAbilityItem("vampire", "Вампиризм", 1000, 300, 42);
      this.addAbilityItem("justice", "Справедливость", 1000, 300, 43);
   }

   private void addAbilityItem(String type, String name, int pricet, int pricee, int slot) {
      ItemStack itemStack = this.getAbilityItemStack(type);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, type);
      List<String> lore = new ArrayList<>();
      if (itemMeta.getLore() != null) {
         lore.addAll(itemMeta.getLore());
      }

      lore.add(ChatColor.translateAlternateColorCodes('&', " &7Особенности способности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&F&F&7&0&0&0- не требуется держать в инвентаре"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&F&F&7&0&0&0- после покупки на вас накладывается"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&F&F&7&0&0&0  способность «" + name + "» до конца вайпа."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&0&5&F&B&0&0" + pricee + " ур. опыта &7(ЛКМ)"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&F&F&7&0&0&0" + pricet + " сапфиров &7(ПКМ)"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(slot, itemStack);
      this.plugin
         .hasAbility(this.player.getName(), type)
         .thenAcceptAsync(
            hasAbility -> {
               if (hasAbility) {
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           ItemStack purchasedItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                           ItemMeta purchasedMeta = purchasedItem.getItemMeta();
                           purchasedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " "));
                           purchasedMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "null");
                           this.plugin
                              .getAbilityTime(this.player.getName(), type)
                              .thenAcceptAsync(
                                 abilityTime -> Bukkit.getScheduler()
                                    .runTask(
                                       this.plugin,
                                       () -> {
                                          List<String> purchasedLore = new ArrayList<>();
                                          purchasedLore.add(
                                             ChatColor.translateAlternateColorCodes(
                                                '&', "&x&F&F&7&0&0&0 &n▪&f Способность « &x&F&F&7&0&0&0" + name + " &f» уже приобретена"
                                             )
                                          );
                                          purchasedLore.add(
                                             ChatColor.translateAlternateColorCodes(
                                                '&', "&x&F&F&7&0&0&0 ▪&f Способность истекает: &x&F&F&7&0&0&0" + abilityTime
                                             )
                                          );
                                          purchasedLore.add(ChatColor.translateAlternateColorCodes('&', ""));
                                          purchasedMeta.setLore(purchasedLore);
                                          purchasedItem.setItemMeta(purchasedMeta);
                                          this.inventory.setItem(slot, purchasedItem);
                                       }
                                    )
                              );
                        }
                     );
               }
            }
         );
   }

   private void addItemPurchase(String type, int pricet, int pricee, int slot) {
      ItemStack itemStack = this.getAbilityItemStack(type);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING, "item_" + type);
      List<String> lore = new ArrayList<>();
      if (itemMeta.getLore() != null) {
         lore.addAll(itemMeta.getLore());
      }

      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&0&5&F&B&0&0" + pricee + " ур. опыта &7(ЛКМ)"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 ▶ &fКупить за &x&F&F&7&0&0&0" + pricet + " сапфиров &7(ПКМ)"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(slot, itemStack);
   }

   private ItemStack getAbilityItemStack(String type) {
      switch (type) {
         case "hunter":
            return this.plugin.mantiExpItemUtil.getHunter();
         case "snowman":
            return this.plugin.mantiExpItemUtil.getSnowman();
         case "illuminator":
            return this.plugin.mantiExpItemUtil.getIlluminator();
         case "enderman":
            return this.plugin.mantiExpItemUtil.getEnderman();
         case "antifantom":
            return this.plugin.mantiExpItemUtil.getAntifantom();
         case "telekines":
            return this.plugin.mantiExpItemUtil.getTelekines();
         case "gravity":
            return this.plugin.mantiExpItemUtil.getGravity();
         case "vampire":
            return this.plugin.mantiExpItemUtil.getVampire();
         case "justice":
            return this.plugin.mantiExpItemUtil.getJustice();
         default:
            return new ItemStack(Material.STONE);
      }
   }
}
