package org.dimasik.mantiexpkeeper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MantiExpKeeperListener implements Listener {
   private MantiExpKeeper plugin;
   private final Map<UUID, UUID> lastDamagerMap = new HashMap<>();
   private final HashSet<Player> thornsPlayers = new HashSet<>();
   private final HashSet<Player> unduyingPlayers = new HashSet<>();
   public BukkitTask task;

   public MantiExpKeeperListener(MantiExpKeeper plugin) {
      this.plugin = plugin;
      this.setupTask();
   }

   private void setupTask() {
      this.task = (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               MantiExpKeeperListener.this.checkEffects(player);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 100L);
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView().getTitle().equalsIgnoreCase(this.plugin.effectsTitle)
         || event.getView().getTitle().equalsIgnoreCase(this.plugin.itemsTitle)
         || event.getView().getTitle().equalsIgnoreCase(this.plugin.effectsAbilities)) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
            return;
         }

         if (event.getClickedInventory().getType() != InventoryType.CHEST) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         PersistentDataContainer pdc = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
         if (!pdc.has(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING)) {
            return;
         }

         String updateType = (String)pdc.get(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING);
         if (event.getView().getTitle().equalsIgnoreCase(this.plugin.effectsTitle)) {
            this.handleEffectsMenu(event, player, updateType, pdc);
         } else if (event.getView().getTitle().equalsIgnoreCase(this.plugin.effectsAbilities)) {
            this.handleAbilitiesMenu(event, player, updateType);
         } else if (event.getView().getTitle().equalsIgnoreCase(this.plugin.itemsTitle)) {
            this.handleItemsMenu(event, player, updateType);
         }
      }
   }

   private void handleEffectsMenu(InventoryClickEvent event, Player player, String updateType, PersistentDataContainer pdc) {
      if (updateType.equalsIgnoreCase("cathegory_abilities")) {
         new MantiExpAbilities(this.plugin, player);
      } else if (updateType.equalsIgnoreCase("cathegory_items")) {
         new MantiExpItems(this.plugin, player);
      } else {
         String[] effects = new String[]{
            "haste", "fire_resistance", "water_breath", "night_vision", "saturation", "speed3", "regeneration", "resistance", "health_boost"
         };

         for (String effect : effects) {
            if (updateType.equalsIgnoreCase(effect)) {
               Integer pricet = (Integer)pdc.get(new NamespacedKey(this.plugin, "pricet"), PersistentDataType.INTEGER);
               Integer pricee = (Integer)pdc.get(new NamespacedKey(this.plugin, "pricee"), PersistentDataType.INTEGER);
               if (pricet == null || pricee == null) {
                  return;
               }

               int finalPricee = pricee;
               int finalPricet = pricet;
               if (event.isLeftClick()) {
                  if (this.plugin.getPlayerExp(player) >= calculateXPForLevel(finalPricee)) {
                     this.plugin.addTime(player.getName(), updateType, 24).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        player.giveExp(-calculateXPForLevel(finalPricee));
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        this.checkEffects(player);
                        new MantiExpEffects(this.plugin, player);
                     }));
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Опыта &fдля совершения покупки."));
                  }
               } else if (event.isRightClick()) {
                  if (Long.parseLong(PlaceholderAPI.setPlaceholders(player, "%playerpoints_points%")) >= finalPricet) {
                     this.plugin.addTime(player.getName(), updateType, 24).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p take " + player.getName() + " " + finalPricet);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        this.checkEffects(player);
                        new MantiExpEffects(this.plugin, player);
                     }));
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Сапфиров &fдля совершения покупки."));
                  }
               }
               break;
            }
         }
      }
   }

   private void handleAbilitiesMenu(InventoryClickEvent event, Player player, String updateType) {
      if (updateType.equalsIgnoreCase("cathegory_effects")) {
         new MantiExpEffects(this.plugin, player);
      } else if (updateType.equalsIgnoreCase("cathegory_items")) {
         new MantiExpItems(this.plugin, player);
      } else {
         String[] abilities = new String[]{"hunter", "snowman", "illuminator", "enderman", "antifantom", "telekines", "gravity", "vampire", "justice"};
         int[] abilityPricesT = new int[]{275, 300, 300, 400, 400, 600, 600, 1000, 1000};
         int[] abilityPricesE = new int[]{165, 175, 175, 200, 200, 250, 250, 300, 300};

         for (int i = 0; i < abilities.length; i++) {
            if (updateType.equalsIgnoreCase(abilities[i])) {
               int pricet = abilityPricesT[i];
               int pricee = abilityPricesE[i];
               String abilityType = abilities[i];
               if (event.isLeftClick()) {
                  if (this.plugin.getPlayerExp(player) >= calculateXPForLevel(pricee)) {
                     this.plugin.addInf(player.getName(), abilityType).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        player.giveExp(-calculateXPForLevel(pricee));
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        this.checkEffects(player);
                        new MantiExpAbilities(this.plugin, player);
                     }));
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Опыта &fдля совершения покупки."));
                  }
               } else if (event.isRightClick()) {
                  if (Long.parseLong(PlaceholderAPI.setPlaceholders(player, "%playerpoints_points%")) >= pricet) {
                     this.plugin.addInf(player.getName(), abilityType).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p take " + player.getName() + " " + pricet);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        this.checkEffects(player);
                        new MantiExpAbilities(this.plugin, player);
                     }));
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Сапфиров &fдля совершения покупки."));
                  }
               }
               break;
            }
         }

         String[] itemAbilities = new String[]{"hunter", "snowman", "illuminator", "enderman", "antifantom", "telekines", "gravity", "vampire", "justice"};
         int[] itemPricesT = new int[]{100, 110, 110, 150, 150, 225, 225, 300, 300};
         int[] itemPricesE = new int[]{100, 100, 100, 125, 125, 150, 150, 175, 175};

         for (int ix = 0; ix < itemAbilities.length; ix++) {
            if (updateType.equalsIgnoreCase("item_" + itemAbilities[ix])) {
               int pricet = itemPricesT[ix];
               int pricee = itemPricesE[ix];
               String itemType = itemAbilities[ix];
               if (event.isLeftClick()) {
                  if (this.plugin.getPlayerExp(player) >= calculateXPForLevel(pricee)) {
                     Bukkit.getScheduler().runTask(this.plugin, () -> {
                        this.plugin.addItemInventory(player.getInventory(), this.getAbilityItem(itemType), player.getLocation());
                        player.giveExp(-calculateXPForLevel(pricee));
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        new MantiExpAbilities(this.plugin, player);
                     });
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Опыта &fдля совершения покупки."));
                  }
               } else if (event.isRightClick()) {
                  if (Long.parseLong(PlaceholderAPI.setPlaceholders(player, "%playerpoints_points%")) >= pricet) {
                     Bukkit.getScheduler().runTask(this.plugin, () -> {
                        this.plugin.addItemInventory(player.getInventory(), this.getAbilityItem(itemType), player.getLocation());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p take " + player.getName() + " " + pricet);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        new MantiExpAbilities(this.plugin, player);
                     });
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Сапфиров &fдля совершения покупки."));
                  }
               }
               break;
            }
         }
      }
   }

   private void handleItemsMenu(InventoryClickEvent event, Player player, String updateType) {
      if (updateType.equalsIgnoreCase("cathegory_abilities")) {
         new MantiExpAbilities(this.plugin, player);
      } else if (updateType.equalsIgnoreCase("cathegory_effects")) {
         new MantiExpEffects(this.plugin, player);
      } else {
         String[] items = new String[]{"explosive", "jake", "bottle", "snowball", "jump_three", "artefact"};
         int[] itemPrices = new int[]{20, 25, 50, 25, 30, 150};

         for (int i = 0; i < items.length; i++) {
            if (updateType.equalsIgnoreCase(items[i])) {
               int pricet = itemPrices[i];
               String itemName = items[i];
               if (event.isLeftClick()) {
                  if (this.plugin.getPlayerExp(player) >= calculateXPForLevel(pricet)) {
                     Bukkit.getScheduler().runTask(this.plugin, () -> {
                        if (itemName.equals("explosive") || itemName.equals("jake") || itemName.equals("bottle") || itemName.equals("snowball")) {
                           Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holyliteitems give " + player.getName() + " " + itemName + " 1");
                        } else if (itemName.equals("jump_three")) {
                           this.plugin.addItemInventory(player.getInventory(), this.plugin.mantiExpItemUtil.getJumpThree(1), player.getLocation());
                        } else if (itemName.equals("artefact")) {
                           this.plugin.addItemInventory(player.getInventory(), this.plugin.mantiExpItemUtil.getArtefact(), player.getLocation());
                        }

                        player.giveExp(-calculateXPForLevel(pricet));
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
                        new MantiExpItems(this.plugin, player);
                     });
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c ▶ &fУ вас &cнедостаточно Опыта &fдля совершения покупки."));
                  }
               }
               break;
            }
         }
      }
   }

   private ItemStack getAbilityItem(String ability) {
      String var2 = ability.toLowerCase();
      switch (var2) {
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

   public static int calculateXPForLevel(int level) {
      if (level < 0) {
         throw new IllegalArgumentException("Уровень не может быть отрицательным");
      } else if (level <= 15) {
         return level * level + 6 * level;
      } else {
         return level <= 30 ? (int)(2.5 * level * level - 40.5 * level + 360.0) : (int)(4.5 * level * level - 162.5 * level + 2220.0);
      }
   }

   public void checkEffects(Player player) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         this.checkAndApplyEffect(player, "haste", PotionEffectType.HASTE, 0, true);
         this.checkAndApplyEffect(player, "fire_resistance", PotionEffectType.FIRE_RESISTANCE, 0, false);
         this.checkAndApplyEffect(player, "water_breath", PotionEffectType.WATER_BREATHING, 0, false);
         this.checkAndApplyEffect(player, "night_vision", PotionEffectType.NIGHT_VISION, 0, false);
         this.checkAndApplyEffect(player, "saturation", PotionEffectType.SATURATION, 0, true);
         this.checkAndApplyEffect(player, "speed3", PotionEffectType.SPEED, 2, false);
         this.checkAndApplyEffect(player, "regeneration", PotionEffectType.REGENERATION, 0, false);
         this.checkAndApplyEffect(player, "resistance", PotionEffectType.RESISTANCE, 0, false);
         this.checkHealthBoostEffect(player);
      }, 1L);
   }

   private void checkAndApplyEffect(Player player, String ability, PotionEffectType effectType, int amplifier, boolean pvpCheck) {
      this.plugin
         .hasAbility(player.getName(), ability)
         .thenAccept(
            hasAbility -> Bukkit.getScheduler()
               .runTask(
                  this.plugin,
                  () -> {
                     try {
                        boolean inPvp = PlaceholderAPI.setPlaceholders(player, "%antirelog_isinpvp%").equalsIgnoreCase("yes");
                        if (!hasAbility || pvpCheck && inPvp) {
                           if (player.hasPotionEffect(effectType)
                              && player.getPotionEffect(effectType).getDuration() >= 1998180
                              && player.getPotionEffect(effectType).getAmplifier() == amplifier) {
                              player.removePotionEffect(effectType);
                           } else if (player.hasPotionEffect(effectType) && pvpCheck && inPvp) {
                              player.removePotionEffect(effectType);
                           }
                        } else {
                           player.addPotionEffect(new PotionEffect(effectType, 1999980, amplifier, false, true, true));
                        }
                     } catch (Exception var6) {
                     }
                  }
               )
         );
   }

   private void checkHealthBoostEffect(Player player) {
      this.plugin
         .hasAbility(player.getName(), "health_boost")
         .thenAccept(
            hasAbility -> Bukkit.getScheduler()
               .runTask(
                  this.plugin,
                  () -> {
                     try {
                        if (hasAbility) {
                           if (player.hasPotionEffect(PotionEffectType.HEALTH_BOOST)
                              && player.getPotionEffect(PotionEffectType.HEALTH_BOOST).getDuration() <= 20000) {
                              player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 1999980, 0, false, false, true));
                           } else if (!player.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
                              player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 1999980, 0, false, false, true));
                           }
                        } else if (player.hasPotionEffect(PotionEffectType.HEALTH_BOOST)
                           && player.getPotionEffect(PotionEffectType.HEALTH_BOOST).getDuration() >= 20000
                           && player.getPotionEffect(PotionEffectType.HEALTH_BOOST).getAmplifier() == 0) {
                           player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
                        }
                     } catch (Exception var3) {
                     }
                  }
               )
         );
   }

   @EventHandler
   public void onRespawn(PlayerRespawnEvent event) {
      this.checkEffects(event.getPlayer());
   }

   @EventHandler
   public void onRespawn(PlayerJoinEvent event) {
      this.checkEffects(event.getPlayer());
   }

   @EventHandler
   public void onRespawn(PlayerDeathEvent event) {
      this.checkEffects(event.getEntity());
   }

   @EventHandler
   public void onRespawn(EntityResurrectEvent event) {
      if (event.getEntity() instanceof Player) {
         this.checkEffects((Player)event.getEntity());
      }
   }

   @EventHandler
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player damaged = (Player)event.getEntity();
         if (this.unduyingPlayers.contains(damaged)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
         final Player damaged = (Player)event.getEntity();
         final Player damager = (Player)event.getDamager();
         if (!event.isCancelled() && this.thornsPlayers.contains(damaged)) {
            damager.damage(event.getDamage() / 2.0);
         }

         this.lastDamagerMap.put(damaged.getUniqueId(), damager.getUniqueId());
         (new BukkitRunnable() {
            public void run() {
               MantiExpKeeperListener.this.lastDamagerMap.remove(damaged.getUniqueId(), damager.getUniqueId());
            }
         }).runTaskLater(this.plugin, 60L);
         this.checkEffects(damaged);
         this.checkEffects(damager);
      }
   }

   @EventHandler(
      ignoreCancelled = false
   )
   public void onRespawn(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (event.getItem() != null && event.getItem().getType() == Material.PHANTOM_MEMBRANE) {
         this.checkEffects(player);
      }
   }

   @EventHandler
   public void onRespawn(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      if (event.getItem() != null && event.getItem().getType() == Material.MILK_BUCKET) {
         this.checkEffects(player);
      }
   }
}
