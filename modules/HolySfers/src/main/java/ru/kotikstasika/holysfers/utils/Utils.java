package ru.kotikstasika.holysfers.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.holysfers.HolySfers;

public class Utils implements Listener {
   private final HolySfers plugin = HolySfers.getInstance();

   public void startSphereTask() {
      BukkitTask bukkitTask = (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               Utils.this.checkHasteSphere(player);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 80L);
   }

   @EventHandler
   public void onItemSwap(PlayerSwapHandItemsEvent event) {
      this.checkHasteSphere(event.getPlayer());
   }

   public Utils() {
   }

   public ItemStack getSkull(ItemStack head, String base64) {
      if (base64.isEmpty()) {
         return head;
      } else {
         SkullMeta meta = (SkullMeta)head.getItemMeta();
         PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "CustomSkull");
         profile.setProperty(new ProfileProperty("textures", base64));
         meta.setPlayerProfile(profile);
         head.setItemMeta(meta);
         return head;
      }
   }

   public void checkHasteSphere(final Player player) {
      (new BukkitRunnable() {
            public void run() {
               if (player.getInventory().getItemInOffHand() != null) {
                  if (player.getInventory().getItemInOffHand().getItemMeta() != null) {
                     if (player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer() != null) {
                        if (player.getInventory()
                           .getItemInOffHand()
                           .getItemMeta()
                           .getPersistentDataContainer()
                           .has(new NamespacedKey(Utils.this.plugin, "HASTE"), PersistentDataType.INTEGER)) {
                           if (PlaceholderAPI.setPlaceholders(player, "%antirelog_isinpvp%").equalsIgnoreCase("yes")) {
                              if (player.hasPotionEffect(PotionEffectType.HASTE)
                                 && !player.getPotionEffect(PotionEffectType.HASTE).hasParticles()) {
                                 player.removePotionEffect(PotionEffectType.HASTE);
                              }

                              return;
                           }

                           player.addPotionEffect(
                              new PotionEffect(
                                 PotionEffectType.HASTE,
                                 100,
                                 (Integer)player.getInventory()
                                       .getItemInOffHand()
                                       .getItemMeta()
                                       .getPersistentDataContainer()
                                       .get(new NamespacedKey(Utils.this.plugin, "HASTE"), PersistentDataType.INTEGER)
                                    - 1,
                                 false,
                                 false,
                                 true
                              )
                           );
                        } else if (player.hasPotionEffect(PotionEffectType.HASTE)
                           && !player.getPotionEffect(PotionEffectType.HASTE).hasParticles()) {
                           player.removePotionEffect(PotionEffectType.HASTE);
                        }
                     } else if (player.hasPotionEffect(PotionEffectType.HASTE) && !player.getPotionEffect(PotionEffectType.HASTE).hasParticles()) {
                        player.removePotionEffect(PotionEffectType.HASTE);
                     }
                  } else if (player.hasPotionEffect(PotionEffectType.HASTE) && !player.getPotionEffect(PotionEffectType.HASTE).hasParticles()) {
                     player.removePotionEffect(PotionEffectType.HASTE);
                  }
               } else if (player.hasPotionEffect(PotionEffectType.HASTE) && !player.getPotionEffect(PotionEffectType.HASTE).hasParticles()) {
                  player.removePotionEffect(PotionEffectType.HASTE);
               }
            }
         })
         .runTaskLater(this.plugin, 1L);
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      AnvilInventory inventory = event.getInventory();
      ItemStack firstItem = inventory.getFirstItem();
      ItemStack secondItem = inventory.getSecondItem();
      if (firstItem != null && secondItem != null) {
         if (firstItem.getType() == Material.PLAYER_HEAD && secondItem.getType() == Material.PLAYER_HEAD) {
            NamespacedKey key = new NamespacedKey(this.plugin, "holysfers");
            if ((Integer)firstItem.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0) == 1
               && (Integer)secondItem.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0) == 1) {
               NamespacedKey rarityKey = new NamespacedKey(this.plugin, "rarity");
               String firstRarity = (String)firstItem.getItemMeta().getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);
               String secondRarity = (String)secondItem.getItemMeta().getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);
               if (firstRarity != null && firstRarity.equals(secondRarity)) {
                  if ("MYTHIC".equals(firstRarity) || "LEGENDARY".equals(firstRarity) || "CUSTOM".equals(firstRarity)) {
                     return;
                  }

                  ItemStack result = new ItemStack(Material.PLAYER_HEAD);
                  ItemMeta resultMeta = result.getItemMeta();
                  resultMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                  String newRarity = "";
                  String base64 = "";
                  switch (firstRarity) {
                     case "DEFAULT":
                        newRarity = "EPIC";
                        resultMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&a&0&0&f&dЭпическая сфера"));
                        base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzYmNhZjZkMjY3OWQ4ZDdkOWJmNmE0NzRhNDhhNzdhOGU5MTc0N2ExMDg0YzA5MjU2ZWJjODZjYjc0ODExIn19fQ==";
                        break;
                     case "EPIC":
                        newRarity = "LEGENDARY";
                        resultMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&8&2&f&dЛегендарная сфера"));
                        base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM5MzY1NjQyYzZlZGRjZmVkZjViNWUxNGUyYmM3MTI1N2Q5ZTRhMzM2M2QxMjNjNmYzM2M1NWNhZmJmNmQifX19";
                  }

                  resultMeta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, newRarity);
                  List<String> lore = new ArrayList<>();
                  this.copyEffects(firstItem, resultMeta, lore);
                  this.copyEffects(secondItem, resultMeta, lore);
                  lore.add(ChatColor.translateAlternateColorCodes('&', ""));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7Возьмите предмет в левую руку,"));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7чтобы активировать магию."));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7&oПодробнее о сферах - &x&e&7&e&7&e&7&o/itemhelp"));
                  resultMeta.setLore(lore);
                  resultMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                  result.setItemMeta(resultMeta);
                  result = this.getSkull(result, base64);
                  if (firstItem.getAmount() != 1 && secondItem.getAmount() != 1) {
                     return;
                  }

                  inventory.setRepairCost(50 * firstItem.getAmount());
                  result.setAmount(firstItem.getAmount());
                  event.setResult(result);
               } else if (firstRarity != null && firstRarity.equals("LEGENDARY") && secondRarity.equals("EPIC")) {
                  ItemStack result = new ItemStack(Material.PLAYER_HEAD);
                  ItemMeta resultMeta = result.getItemMeta();
                  resultMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                  String newRarity = "";
                  String base64 = "";
                  newRarity = "MYTHIC";
                  resultMeta.setDisplayName(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "&x&B&F&0&0&0&0М&x&C&7&0&0&0&0и&x&C&F&0&0&0&0ф&x&D&7&0&0&0&0и&x&D&F&0&0&0&0ч&x&E&7&0&0&0&0е&x&E&F&0&0&0&0с&x&F&7&0&0&0&0к&x&F&F&0&0&0&0а&x&F&6&0&0&0&0я &x&E&4&0&0&0&0с&x&D&A&0&0&0&0ф&x&D&1&0&0&0&0е&x&C&8&0&0&0&0р&x&B&F&0&0&0&0а"
                     )
                  );
                  base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFmZjJlYjQ5OGU1YzZhMDQ0ODRmMGM5Zjc4NWI0NDg0NzlhYjIxM2RmOTVlYzkxMTc2YTMwOGExMmFkZDcwIn19fQ==";
                  resultMeta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, newRarity);
                  List<String> lore = new ArrayList<>();
                  this.copyEffects(firstItem, resultMeta, lore);
                  this.copyEffects(secondItem, resultMeta, lore);
                  lore.add(ChatColor.translateAlternateColorCodes('&', ""));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7&oВозьмите предмет в левую руку,"));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7&oчтобы активировать магию."));
                  lore.add(ChatColor.translateAlternateColorCodes('&', "&7&oПодробнее о сферах - &x&e&7&e&7&e&7&o/magic"));
                  resultMeta.setLore(lore);
                  resultMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                  result.setItemMeta(resultMeta);
                  result = this.getSkull(result, base64);
                  if (firstItem.getAmount() != 1 && secondItem.getAmount() != 1) {
                     return;
                  }

                  inventory.setRepairCost(50 * firstItem.getAmount());
                  result.setAmount(firstItem.getAmount());
                  event.setResult(result);
               }
            }
         }

         if (firstItem.getType() == Material.PLAYER_HEAD && secondItem.getType() == Material.TOTEM_OF_UNDYING) {
            NamespacedKey key = new NamespacedKey(this.plugin, "holysfers");
            if ((Integer)firstItem.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0) == 1) {
               ItemStack result = firstItem.clone();
               result.setType(Material.TOTEM_OF_UNDYING);
               ItemMeta resultMeta = result.getItemMeta();
               NamespacedKey rarityKey = new NamespacedKey(this.plugin, "rarity");
               String rarity = (String)resultMeta.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);
               if ("MYTHIC".equals(rarity) || "CUSTOM".equals(rarity)) {
                  return;
               }

               if (rarity != null) {
                  switch (rarity) {
                     case "DEFAULT":
                        resultMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&6&9&9&f&dОбычный талисман"));
                        break;
                     case "EPIC":
                        resultMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&a&0&0&f&dЭпический талисман"));
                        break;
                     case "LEGENDARY":
                        resultMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&8&2&f&dЛегендарный талисман"));
                        break;
                     case "MYTHIC":
                        resultMeta.setDisplayName(
                           ChatColor.translateAlternateColorCodes(
                              '&',
                              "&x&B&F&0&0&0&0М&x&C&6&0&0&0&0и&x&C&D&0&0&0&0ф&x&D&4&0&0&0&0и&x&D&B&0&0&0&0ч&x&E&3&0&0&0&0е&x&E&A&0&0&0&0с&x&F&1&0&0&0&0к&x&F&8&0&0&0&0и&x&F&F&0&0&0&0й &x&F&1&0&0&0&0т&x&E&A&0&0&0&0а&x&E&3&0&0&0&0л&x&D&B&0&0&0&0и&x&D&4&0&0&0&0с&x&C&D&0&0&0&0м&x&C&6&0&0&0&0а&x&B&F&0&0&0&0н"
                           )
                        );
                  }
               }

               resultMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
               resultMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               result.setItemMeta(resultMeta);
               inventory.setRepairCost(60);
               event.setResult(result);
            }
         }
      }
   }

   private void copyEffects(ItemStack source, ItemMeta targetMeta, List<String> lore) {
      NamespacedKey damageKey = new NamespacedKey(this.plugin, "DAMAGE");
      NamespacedKey armorKey = new NamespacedKey(this.plugin, "ARMOR");
      NamespacedKey speedKey = new NamespacedKey(this.plugin, "SPEED");
      NamespacedKey hasteKey = new NamespacedKey(this.plugin, "HASTE");
      NamespacedKey attackSpeedKey = new NamespacedKey(this.plugin, "ATTACK_SPEED");
      NamespacedKey healthKey = new NamespacedKey(this.plugin, "HEALTH");
      if (source.getItemMeta().getPersistentDataContainer().has(damageKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(damageKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(damageKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
         targetMeta.getPersistentDataContainer().remove(damageKey);
         String damageLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Урон " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Урон", damageLore);
         targetMeta.addAttributeModifier(
            Attribute.ATTACK_DAMAGE,
            new AttributeModifier(UUID.randomUUID(), "GENERIC_ATTACK_DAMAGE", newLevel, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.getPersistentDataContainer().set(damageKey, PersistentDataType.INTEGER, newLevel);
      }

      if (source.getItemMeta().getPersistentDataContainer().has(armorKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(armorKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(armorKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.removeAttributeModifier(Attribute.ARMOR);
         targetMeta.removeAttributeModifier(Attribute.ARMOR_TOUGHNESS);
         targetMeta.getPersistentDataContainer().remove(armorKey);
         String armorLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Броня " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Броня", armorLore);
         targetMeta.addAttributeModifier(
            Attribute.ARMOR, new AttributeModifier(UUID.randomUUID(), "GENERIC_ARMOR", newLevel, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.addAttributeModifier(
            Attribute.ARMOR_TOUGHNESS,
            new AttributeModifier(UUID.randomUUID(), "GENERIC_ARMOR_TOUGHNESS", newLevel, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.getPersistentDataContainer().set(armorKey, PersistentDataType.INTEGER, newLevel);
      }

      if (source.getItemMeta().getPersistentDataContainer().has(speedKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(speedKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(speedKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.removeAttributeModifier(Attribute.MOVEMENT_SPEED);
         targetMeta.getPersistentDataContainer().remove(speedKey);
         String speedLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Скорость " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Скорость", speedLore);
         targetMeta.addAttributeModifier(
            Attribute.MOVEMENT_SPEED,
            new AttributeModifier(UUID.randomUUID(), "GENERIC_MOVEMENT_SPEED", newLevel / 100.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.getPersistentDataContainer().set(speedKey, PersistentDataType.INTEGER, newLevel);
      }

      if (source.getItemMeta().getPersistentDataContainer().has(attackSpeedKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(attackSpeedKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(attackSpeedKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.removeAttributeModifier(Attribute.ATTACK_SPEED);
         targetMeta.getPersistentDataContainer().remove(attackSpeedKey);
         String speedLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Скорость атаки " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Скорость атаки", speedLore);
         targetMeta.addAttributeModifier(
            Attribute.ATTACK_SPEED,
            new AttributeModifier(UUID.randomUUID(), "GENERIC_ATTACK_SPEED", newLevel / 100.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.getPersistentDataContainer().set(attackSpeedKey, PersistentDataType.INTEGER, newLevel);
      }

      if (source.getItemMeta().getPersistentDataContainer().has(hasteKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(hasteKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(hasteKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.getPersistentDataContainer().remove(hasteKey);
         String speedLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Спешка " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Спешка", speedLore);
         targetMeta.getPersistentDataContainer().set(hasteKey, PersistentDataType.INTEGER, newLevel);
      }

      if (source.getItemMeta().getPersistentDataContainer().has(healthKey, PersistentDataType.INTEGER)) {
         int sourceLevel = (Integer)source.getItemMeta().getPersistentDataContainer().get(healthKey, PersistentDataType.INTEGER);
         int targetLevel = (Integer)targetMeta.getPersistentDataContainer().getOrDefault(healthKey, PersistentDataType.INTEGER, 0);
         int newLevel = sourceLevel == targetLevel ? sourceLevel + 1 : Math.max(sourceLevel, targetLevel);
         targetMeta.removeAttributeModifier(Attribute.MAX_HEALTH);
         targetMeta.getPersistentDataContainer().remove(healthKey);
         String healthLore = ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Макс. здоровье " + convertInt(newLevel));
         this.updateLore(lore, "&7• &x&e&7&e&7&e&7Макс. здоровье", healthLore);
         targetMeta.addAttributeModifier(
            Attribute.MAX_HEALTH,
            new AttributeModifier(UUID.randomUUID(), "GENERIC_MAX_HEALTH", newLevel * 2.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
         );
         targetMeta.getPersistentDataContainer().set(healthKey, PersistentDataType.INTEGER, newLevel);
      }
   }

   @EventHandler
   public void onJoin(final PlayerJoinEvent event) {
      (new BukkitRunnable() {
         public void run() {
            if (event.getPlayer() != null) {
               Utils.this.checkHasteSphere(event.getPlayer());
            }
         }
      }).runTaskLater(this.plugin, 2L);
   }

   private void updateLore(List<String> lore, String prefix, String newLore) {
      String translatedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);

      for (int i = 0; i < lore.size(); i++) {
         if (lore.get(i).startsWith(translatedPrefix + " I")) {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', newLore));
            return;
         }
      }

      lore.add(0, ChatColor.translateAlternateColorCodes('&', newLore));
   }

   public void addToSphere(Player player, Utils.EffectType effectType, int level) {
      ItemStack itemStack = player.getItemInHand();
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            player.sendMessage("Предмет не поддерживает метаданные.");
         } else {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
               lore = new ArrayList<>();
            }

            NamespacedKey key = new NamespacedKey(this.plugin, "holysfers");
            if (key == null) {
               player.sendMessage("Ошибка при создании ключа.");
            } else {
               switch (effectType) {
                  case DAMAGE:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Урон " + convertInt(level)));
                     itemMeta.addAttributeModifier(
                        Attribute.ATTACK_DAMAGE,
                        new AttributeModifier(UUID.randomUUID(), "GENERIC_ATTACK_DAMAGE", level, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "DAMAGE"), PersistentDataType.INTEGER, level);
                     break;
                  case ARMOR:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Броня " + convertInt(level)));
                     itemMeta.addAttributeModifier(
                        Attribute.ARMOR, new AttributeModifier(UUID.randomUUID(), "GENERIC_ARMOR", level, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.addAttributeModifier(
                        Attribute.ARMOR_TOUGHNESS,
                        new AttributeModifier(UUID.randomUUID(), "GENERIC_ARMOR_TOUGHNESS", level, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "ARMOR"), PersistentDataType.INTEGER, level);
                     break;
                  case SPEED:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Скорость " + convertInt(level)));
                     itemMeta.addAttributeModifier(
                        Attribute.MOVEMENT_SPEED,
                        new AttributeModifier(UUID.randomUUID(), "GENERIC_MOVEMENT_SPEED", level / 100.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "SPEED"), PersistentDataType.INTEGER, level);
                     break;
                  case HEALTH:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Макс. здоровье " + convertInt(level)));
                     itemMeta.addAttributeModifier(
                        Attribute.MAX_HEALTH,
                        new AttributeModifier(UUID.randomUUID(), "GENERIC_MAX_HEALTH", level * 2.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HEALTH"), PersistentDataType.INTEGER, level);
                     break;
                  case ATTACK_SPEED:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Скорость атаки " + convertInt(level)));
                     itemMeta.addAttributeModifier(
                        Attribute.ATTACK_SPEED,
                        new AttributeModifier(UUID.randomUUID(), "GENERIC_ATTACK_SPEED", level / 100.0F, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
                     );
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "ATTACK_SPEED"), PersistentDataType.INTEGER, level);
                     break;
                  case HASTE:
                     lore.add(0, ChatColor.translateAlternateColorCodes('&', "&7• &x&e&7&e&7&e&7Спешка " + convertInt(level)));
                     itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                     itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "HASTE"), PersistentDataType.INTEGER, level);
               }

               itemMeta.setLore(lore);
               itemStack.setItemMeta(itemMeta);
               player.setItemInHand(itemStack);
            }
         }
      } else {
         player.sendMessage("Вы должны держать предмет в руке.");
      }
   }

   public void setSphereType(Player player, Utils.SphereType sphereType, Boolean sphere) {
      ItemStack itemStack = player.getItemInHand();
      ItemMeta item = itemStack.getItemMeta();
      if (item == null) {
         player.sendMessage("Предмет не имеет метаданных.");
      } else {
         NamespacedKey key = new NamespacedKey(this.plugin, "rarity");
         if (key == null) {
            player.sendMessage("Ошибка при создании ключа.");
         } else {
            item.getPersistentDataContainer().set(key, PersistentDataType.STRING, sphereType.toString());
            String base64 = "";
            if (sphere) {
               switch (sphereType) {
                  case DEFAULT:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&6&9&9&f&dОбычная сфера"));
                     base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5NTI5M2FjYmNkNGY1NWZhZjU5NDdiZmM1MTM1MDM4YjI3NWE3YWI4MTA4NzM0MWI5ZWM2ZTQ1M2U4MzkifX19";
                     break;
                  case EPIC:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&a&0&0&f&dЭпическая сфера"));
                     base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzYmNhZjZkMjY3OWQ4ZDdkOWJmNmE0NzRhNDhhNzdhOGU5MTc0N2ExMDg0YzA5MjU2ZWJjODZjYjc0ODExIn19fQ==";
                     break;
                  case LEGENDARY:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&8&2&f&dЛегендарная сфера"));
                     base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM5MzY1NjQyYzZlZGRjZmVkZjViNWUxNGUyYmM3MTI1N2Q5ZTRhMzM2M2QxMjNjNmYzM2M1NWNhZmJmNmQifX19";
                     break;
                  case MYTHIC:
                     item.setDisplayName(
                        ChatColor.translateAlternateColorCodes(
                           '&',
                           "&x&B&F&0&0&0&0М&x&C&7&0&0&0&0и&x&C&F&0&0&0&0ф&x&D&7&0&0&0&0и&x&D&F&0&0&0&0ч&x&E&7&0&0&0&0е&x&E&F&0&0&0&0с&x&F&7&0&0&0&0к&x&F&F&0&0&0&0а&x&F&6&0&0&0&0я &x&E&4&0&0&0&0с&x&D&A&0&0&0&0ф&x&D&1&0&0&0&0е&x&C&8&0&0&0&0р&x&B&F&0&0&0&0а"
                        )
                     );
                     base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFmZjJlYjQ5OGU1YzZhMDQ0ODRmMGM5Zjc4NWI0NDg0NzlhYjIxM2RmOTVlYzkxMTc2YTMwOGExMmFkZDcwIn19fQ==";
                  case CUSTOM:
               }
            } else {
               switch (sphereType) {
                  case DEFAULT:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&6&9&9&f&dОбычный талисман"));
                     break;
                  case EPIC:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&a&0&0&f&dЭпический талисман"));
                     break;
                  case LEGENDARY:
                     item.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&8&2&f&dЛегендарный талисман"));
                     break;
                  case MYTHIC:
                     item.setDisplayName(
                        ChatColor.translateAlternateColorCodes(
                           '&',
                           "&x&B&F&0&0&0&0М&x&C&6&0&0&0&0и&x&C&D&0&0&0&0ф&x&D&4&0&0&0&0и&x&D&B&0&0&0&0ч&x&E&3&0&0&0&0е&x&E&A&0&0&0&0с&x&F&1&0&0&0&0к&x&F&8&0&0&0&0и&x&F&F&0&0&0&0й &x&F&1&0&0&0&0т&x&E&A&0&0&0&0а&x&E&3&0&0&0&0л&x&D&B&0&0&0&0и&x&D&4&0&0&0&0с&x&C&D&0&0&0&0м&x&C&6&0&0&0&0а&x&B&F&0&0&0&0н"
                        )
                     );
                  case CUSTOM:
               }
            }

            itemStack.setItemMeta(item);
            if (sphere && sphereType != Utils.SphereType.CUSTOM) {
               itemStack = this.getSkull(itemStack, base64);
            }

            player.setItemInHand(itemStack);
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      this.checkHasteSphere(event.getPlayer());
      if (event.getItem() != null && event.getItem().getItemMeta() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         NamespacedKey key = new NamespacedKey(this.plugin, "holysfers");
         if ((Integer)event.getItem().getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0) == 1) {
            event.setCancelled(true);
         }
      }
   }

   public static String convertInt(int i) {
      if (i >= 1 && i <= 3999) {
         int[] arabicValues = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] romanSymbols = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for (int j = 0; j < arabicValues.length; j++) {
            while (i >= arabicValues[j]) {
               result.append(romanSymbols[j]);
               i -= arabicValues[j];
            }
         }

         return result.toString();
      } else {
         throw new IllegalArgumentException("Число должно быть в диапазоне от 1 до 3999");
      }
   }

   public void setName(Player player, String name) {
      ItemStack itemStack = player.getItemInHand();
      ItemMeta item = player.getItemInHand().getItemMeta();
      item.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      itemStack.setItemMeta(item);
      player.setItemInHand(itemStack);
   }

   public void addLore(Player player) {
      ItemStack itemStack = player.getItemInHand();
      ItemMeta item = itemStack.getItemMeta();
      NamespacedKey key = new NamespacedKey(this.plugin, "holysfers");
      item.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
      if (itemStack.getType() == Material.TOTEM_OF_UNDYING) {
         item.addEnchant(Enchantment.UNBREAKING, 1, true);
         item.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      item.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      itemStack.setItemMeta(item);
      player.setItemInHand(itemStack);
   }

   public ItemStack createShardItem(int amount) {
      ItemStack shards = new ItemStack(Material.PLAYER_HEAD, amount);
      ItemMeta meta = shards.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(
            ChatColor.translateAlternateColorCodes(
               '&',
               "&x&B&4&2&1&9&4О&x&C&1&1&C&9&Dс&x&C&D&1&6&A&5к&x&D&A&1&1&A&Eо&x&E&6&0&B&B&6л&x&F&3&0&6&B&Fо&x&F&F&0&0&C&7к &x&E&6&0&B&B&6с&x&D&A&1&1&A&Eф&x&C&D&1&6&A&5е&x&C&1&1&C&9&Dр&x&B&4&2&1&9&4ы"
            )
         );
         List<String> lore = Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&f "),
            ChatColor.translateAlternateColorCodes('&', "&8Особенности:"),
            ChatColor.translateAlternateColorCodes('&', "&5&f &x&E&7&E&7&E&7- обменивайте осколки на сферы в &x&F&F&0&0&8&2/spheres&x&E&7&E&7&E&7;"),
            ChatColor.translateAlternateColorCodes('&', "&5&f &x&E&7&E&7&E&7- получена после переплавки сферы в печке."),
            ChatColor.translateAlternateColorCodes('&', "&f ")
         );
         meta.setLore(lore);
         shards.setItemMeta(meta);
      }

      shards = this.applyTextureWithFixedUuid(
         shards,
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY3YmJjZTIzZTgxNjJlNDJkMjA3MDU1YjBjZTkwZjBlZDU3YjAxNWU1MjEyMTM5YWM4ZmM3ZTZkNDVkZGZjYSJ9fX0=",
         UUID.fromString("00000000-0000-0000-0000-000000000000")
      );
      this.addOskolokTag(shards);
      return shards;
   }

   private ItemStack applyTextureWithFixedUuid(ItemStack head, String base64, UUID uuid) {
      if (base64.isEmpty()) {
         return head;
      } else {
         SkullMeta headMeta = (SkullMeta)head.getItemMeta();
         PlayerProfile profile = Bukkit.createProfile(uuid, (String)null);
         profile.setProperty(new ProfileProperty("textures", base64));
         headMeta.setPlayerProfile(profile);
         head.setItemMeta(headMeta);
         return head;
      }
   }

   public void addOskolokTag(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         NamespacedKey key1 = new NamespacedKey(this.plugin, "holysfers");
         NamespacedKey key2 = new NamespacedKey(this.plugin, "oskolok");
         meta.getPersistentDataContainer().set(key1, PersistentDataType.INTEGER, 1);
         meta.getPersistentDataContainer().set(key2, PersistentDataType.INTEGER, 1);
         item.setItemMeta(meta);
      }
   }

   public static enum EffectType {
      DAMAGE,
      ARMOR,
      SPEED,
      HEALTH,
      ATTACK_SPEED,
      HASTE;

      private EffectType() {
      }
   }

   public static enum SphereType {
      DEFAULT,
      EPIC,
      LEGENDARY,
      MYTHIC,
      CUSTOM;

      private SphereType() {
      }
   }
}
