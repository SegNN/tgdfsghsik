package org.dimasik.mantiexpkeeper;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class MantiExpItemUtil implements Listener {
   private MantiExpKeeper plugin;
   private static final Map<String, String> ABILITY_NAMES = new HashMap<>();

   public MantiExpItemUtil(MantiExpKeeper plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onIterationRecieved(PlayerInteractEvent event) {
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem() != null) {
         String[] items = new String[]{"hunter", "snowman", "illuminator", "enderman", "gravity", "antifantom", "telekines", "vampire", "justice"};
         ItemStack itemStack = event.getItem();
         if (itemStack.getItemMeta() != null) {
            for (String itemType : items) {
               if (itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .has(new NamespacedKey(this.plugin, "expkeeper-" + itemType), PersistentDataType.INTEGER)) {
                  event.setCancelled(true);
                  return;
               }
            }
         }
      }
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      if (event.getInventory().getFirstItem() != null && event.getInventory().getSecondItem() != null) {
         ItemStack first = event.getInventory().getFirstItem();
         ItemStack second = event.getInventory().getSecondItem();
         ItemMeta firstMeta = first.getItemMeta();
         ItemMeta secondMeta = second.getItemMeta();
         if (!firstMeta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-artefact"), PersistentDataType.INTEGER)) {
            return;
         }

         if (first.getAmount() != 1 || second.getAmount() != 1) {
            return;
         }

         ItemStack result = first.clone();
         if (!result.getItemMeta().getLore().contains(ChatColor.translateAlternateColorCodes('&', "&7 Совмещённые эффекты:"))) {
            result = this.getNamedArtefact();
         }

         ItemMeta resultMeta = result.getItemMeta();
         String[] items = new String[]{"hunter", "snowman", "illuminator", "enderman", "gravity", "antifantom", "telekines", "vampire", "justice"};

         for (String itemType : items) {
            if (secondMeta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-" + itemType), PersistentDataType.INTEGER)
               && !firstMeta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-" + itemType), PersistentDataType.INTEGER)) {
               resultMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-" + itemType), PersistentDataType.INTEGER, 1);
               List<String> lore = resultMeta.getLore() != null ? new ArrayList<>(resultMeta.getLore()) : new ArrayList<>();
               int lastIndex = lore.size() - 1;
               String abilityName = this.getName(itemType);
               lore.set(lastIndex, ChatColor.translateAlternateColorCodes('&', "&x&a&b&c&9&d&b - " + abilityName));
               lore.add("");
               resultMeta.setLore(lore);
               result.setItemMeta(resultMeta);
               event.getInventory().setRepairCost(10);
               event.setResult(result);
            }
         }
      }
   }

   @EventHandler
   public void onKill(PlayerDeathEvent event) {
      if (event.getEntity().getKiller() != null && !event.getKeepLevel() && !event.isCancelled()) {
         Player player = event.getEntity().getKiller();
         boolean hasHunterAbility = false;

         for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-hunter"), PersistentDataType.INTEGER)) {
               hasHunterAbility = true;
               break;
            }
         }

         if (hasHunterAbility) {
            player.giveExp(this.plugin.getPlayerExp(event.getEntity()) / 2);
            player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F▶ &fТолько что сработал эффект &x&E&1&5&5&F&F«Охотник»"));
         }
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (!MantiExpKeeper.allowForAll && event.getEntity() instanceof Player) {
         Player killedPlayer = (Player)event.getEntity();
         Player killer = killedPlayer.getKiller();
         if (killer == null) {
            return;
         }

         boolean hasTelekines = false;

         for (ItemStack itemStack : killer.getInventory().getContents()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-telekines"), PersistentDataType.INTEGER)) {
               hasTelekines = true;
               break;
            }
         }

         if (hasTelekines) {
            for (ItemStack drop : event.getDrops()) {
               Item item = killedPlayer.getWorld().dropItem(killedPlayer.getLocation(), drop);
               item.setPickupDelay(60);
            }

            event.getDrops().clear();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
         Player player = (Player)event.getEntity();

         for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-gravity"), PersistentDataType.INTEGER)) {
               event.setCancelled(true);
               player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F▶ &fТолько что сработал эффект &x&E&1&5&5&F&F«Гравитация»"));
               return;
            }
         }
      }
   }

   @EventHandler
   public void onDamage2(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager().getType() == EntityType.PHANTOM) {
         Player player = (Player)event.getEntity();

         for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-antifantom"), PersistentDataType.INTEGER)) {
               ((Phantom)event.getDamager()).setHealth(0.0);
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler
   public void onDamage1(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && !event.isCancelled()) {
         Player player = (Player)event.getEntity();

         for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-justice"), PersistentDataType.INTEGER)) {
               player.removePotionEffect(PotionEffectType.BLINDNESS);
               player.removePotionEffect(PotionEffectType.JUMP_BOOST);
               player.removePotionEffect(PotionEffectType.POISON);
               player.removePotionEffect(PotionEffectType.WITHER);
               player.removePotionEffect(PotionEffectType.WEAKNESS);
               return;
            }
         }
      }
   }

   @EventHandler
   public void onKill(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Player && !event.isCancelled()) {
         Player attacker = (Player)event.getDamager();
         Player target = (Player)event.getEntity();
         boolean hasSnowman = false;
         boolean hasIlluminator = false;
         boolean hasVampire = false;

         for (ItemStack itemStack : attacker.getInventory()) {
            if (itemStack != null && itemStack.hasItemMeta()) {
               if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-snowman"), PersistentDataType.INTEGER)) {
                  hasSnowman = true;
               }

               if (itemStack.getItemMeta()
                  .getPersistentDataContainer()
                  .has(new NamespacedKey(this.plugin, "expkeeper-illuminator"), PersistentDataType.INTEGER)) {
                  hasIlluminator = true;
               }

               if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-vampire"), PersistentDataType.INTEGER)) {
                  hasVampire = true;
               }
            }
         }

         if (hasSnowman && ThreadLocalRandom.current().nextInt(100) < 2) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0, false, false, true));
         }

         if (hasIlluminator && ThreadLocalRandom.current().nextInt(100) < 5) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false, true));
         }

         if (hasVampire && ThreadLocalRandom.current().nextInt(100) < 2) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 50, 1, false, false, true));
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerLaunchProjectileEvent event) {
      if (event.getItemStack().getType().equals(Material.ENDER_PEARL)) {
         if (event.isCancelled()) {
            return;
         }

         Player player = event.getPlayer();
         if (player.getCooldown(Material.ENDER_PEARL) != 0) {
            return;
         }

         for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null
               && itemStack.hasItemMeta()
               && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "expkeeper-enderman"), PersistentDataType.INTEGER)) {
               if (ThreadLocalRandom.current().nextInt(100) < 50) {
                  ItemStack pearl = event.getItemStack().clone();
                  Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                     if (pearl.getAmount() > 0) {
                        player.getInventory().addItem(new ItemStack[]{pearl.asOne()});
                     }

                     player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&x&F&0&0&0&F&F▶ &fТолько что сработал эффект &x&E&1&5&5&F&F«Эндермен»"));
                     player.updateInventory();
                  }, 1L);
               }

               return;
            }
         }
      }
   }

   public String getName(String type) {
      return ABILITY_NAMES.getOrDefault(type.toLowerCase(), null);
   }

   public ItemStack getHunter() {
      ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 Охотник"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 - когда предмет в инвентаре"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0   возможность забрать 50% опыта"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0   с соперника, при его убийстве."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-hunter"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getSnowman() {
      ItemStack itemStack = new ItemStack(Material.SNOW_BLOCK);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " &x&A&E&F&0&F&FСнеговик"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&A&E&F&0&F&F - при ударе, есть шанс 2% дать игроку,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&A&E&F&0&F&F   по которому совершен удар,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&A&E&F&0&F&F   эффект замедления на 5 секунд"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-snowman"), PersistentDataType.INTEGER, 1);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getIlluminator() {
      ItemStack itemStack = new ItemStack(Material.SEA_LANTERN);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " &x&E&7&E&7&E&7Иллюминатор"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&7&E&7&E&7 - при ударе, есть шанс 5% дать игроку,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&7&E&7&E&7   по которому совершен удар,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&7&E&7&E&7   эффект свечения на 5 секунд."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-illuminator"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getEnderman() {
      ItemStack itemStack = new ItemStack(Material.ENDER_PEARL);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " &x&F&F&7&0&0&0Эндермен"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 - когда предмет в инвентаре, с шансом в 50%"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0   не потратится эндер-жемчуг, при его броске"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-enderman"), PersistentDataType.INTEGER, 1);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getAntifantom() {
      ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&D&A&9&D&0&0АнтиФантом"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&4&B&0&0&0 - когда предмет в инвентаре, умирают"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&4&B&0&0&0   все фантомы, при попытке"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&4&B&0&0&0   атаковать вас."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-antifantom"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getTelekines() {
      ItemStack itemStack = new ItemStack(Material.HONEY_BLOCK);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&C&B&3&4&D&DТелекинез"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&A&3&C&F&F - при убийстве другого игрока,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&A&3&C&F&F   блокирует выпавшие с него предметы для"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&E&A&3&C&F&F   их сбора с земли, сроком на 3 секунды."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-telekines"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getGravity() {
      ItemStack itemStack = new ItemStack(Material.FEATHER);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 Гравитация"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 - когда предмет в инвентаре, ваш персонаж"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0   не получает урон от падения."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-gravity"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getVampire() {
      ItemStack itemStack = new ItemStack(Material.WITHER_SKELETON_SKULL);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c Вампиризм"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&c - при ударе, есть шанс 2%"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&c   получить регенерацию II."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-vampire"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getJustice() {
      ItemStack itemStack = new ItemStack(Material.POTION);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0 Справедливость"));
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&8&E&4&9 - когда предмет в инвентаре, вы получаете"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&8&E&4&9   защиту от дебафов - слепота, прыгучесть,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&F&8&E&4&9   отравление, иссушение, медлительность и слабость."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-justice"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getArtefact() {
      ItemStack itemStack = new ItemStack(Material.CONDUIT);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&8&9&A&1&A&FАртефакт"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 Может активировать сразу несколько"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 разных эффектов Хранителя Опыта."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-artefact"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getNamedArtefact() {
      ItemStack itemStack = new ItemStack(Material.CONDUIT);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&8&9&A&1&A&FАртефакт"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 Может активировать сразу несколько"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 разных эффектов Хранителя Опыта."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Совмещённые эффекты:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-artefact"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getFullArtefact() {
      ItemStack itemStack = new ItemStack(Material.CONDUIT);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&8&9&A&1&A&FАртефакт"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 Может активировать сразу несколько"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&e&7&e&7&e&7 разных эффектов Хранителя Опыта."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Совмещённые эффекты:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      String[] items = new String[]{"hunter", "snowman", "illuminator", "enderman", "gravity", "antifantom", "telekines", "vampire", "justice"};

      for (String itemType : items) {
         itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-" + itemType), PersistentDataType.INTEGER, 1);
         int lastIndex = lore.size() - 1;
         String abilityName = this.getName(itemType);
         lore.set(lastIndex, ChatColor.translateAlternateColorCodes('&', "&x&a&b&c&9&d&b - " + abilityName));
         lore.add("");
      }

      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper-artefact"), PersistentDataType.INTEGER, 1);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getBottle(int count) {
      ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&e&e&1&c Пузырь опыта &8&o(предмет)"));
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&6&E&E&E&6&5 - при нажатие ПКМ, полностью ремонтирует"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&6&E&E&E&6&5   надетую броню с зачарованием починка."));
      lore.add("");
      itemMeta.setLore(lore);
      itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getJake(int count) {
      ItemStack item = new ItemStack(Material.JACK_O_LANTERN);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&b&b&9&0&0 Светильник Джейка &8&o(предмет)"));
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&C&4&5&2- при нажатие ПКМ, всем игрокам в радиусе"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&C&4&5&2   15 блоков, на которых отсутствует шлем,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&C&4&5&2   надевается Светильник Джека (тыква)"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&C&4&5&2   который нельзя снять, на протяжении 15 секунд."));
      lore.add("");
      itemMeta.setLore(lore);
      itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getExplosive(int count) {
      ItemStack item = new ItemStack(Material.FIRE_CHARGE);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&B&3&1&0&0Взрывная штучка"));
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', " &7Особенности и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A - при нажатие ПКМ, вызывает взрыв, с силой"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   способной убить игрока без брони, на расстоянии"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   3 блоков от точки активации;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A - урон наносится, в том числе игроку, который"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   активировал Взрывную Штучку."));
      lore.add("");
      itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getSnowball(int count) {
      ItemStack stan = new ItemStack(Material.SNOWBALL);
      stan.setAmount(count);
      ItemMeta stanMeta = stan.getItemMeta();
      stanMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&0&e&b&f&f Ком снега &8(предмет)"));
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&4&9&F&F&F&7 - при попадании таким снежком "));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&4&9&F&F&F&7   игроку он получает эффект"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&4&9&F&F&F&7   Медлительности VI и слепоту на 10 секунд."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      stanMeta.addEnchant(Enchantment.LOOTING, 1, true);
      stanMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      stanMeta.setLore(lore);
      stan.setItemMeta(stanMeta);
      return stan;
   }

   public ItemStack getJumpThree(int count) {
      ItemStack stan = new ItemStack(Material.POTION);
      stan.setAmount(count);
      ItemMeta stanMeta = stan.getItemMeta();
      PotionMeta potionMeta = (PotionMeta)stanMeta;
      potionMeta.setBasePotionData(new PotionData(PotionType.LEAPING, false, false));
      stanMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&8&6&f&f&9&2 Прыгучесть III &8&o(предмет)"));
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенность:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&8&6&f&f&9&2 - зелье на эффект прыгучести III уровня"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      stanMeta.setLore(lore);
      stanMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "expkeeper"), PersistentDataType.STRING, "jump_three");
      stanMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      stan.setItemMeta(stanMeta);
      return stan;
   }

   static {
      ABILITY_NAMES.put("hunter", "Охотник");
      ABILITY_NAMES.put("snowman", "Снеговик");
      ABILITY_NAMES.put("illuminator", "Иллюминатор");
      ABILITY_NAMES.put("enderman", "Эндермен");
      ABILITY_NAMES.put("gravity", "Гравитация");
      ABILITY_NAMES.put("antifantom", "АнтиФантом");
      ABILITY_NAMES.put("telekines", "Телекинез");
      ABILITY_NAMES.put("vampire", "Вампиризм");
      ABILITY_NAMES.put("justice", "Справедливость");
   }
}
