package org.dimasik.mantiitems;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.mantiitems.particles.Particles;
import org.dimasik.mantiitems.particles.UtilParticles;
import ru.holydev.holycustom.api.CustomAPI;

public final class MantiItems extends JavaPlugin implements Listener {
   private static MantiItems main;
   public List<NamespacedKey> recipeKeys = new ArrayList<>();
   private final List<String> trapSkins = Arrays.asList(
      "nether", "rednether", "blackstone", "quartz", "crimson", "warped", "redsand", "prismarine", "purpur", "stone", "spruce", "endstone"
   );
   public static StateFlag FLAG;
   public final List<Location> activeCubes = new ArrayList<>();
   public final HashSet<ExplosiveTrap> traps = new HashSet<>();
   public final HashSet<AlternativeTrap> altTraps = new HashSet<>();
   public final HashSet<Projectile> snowballs = new HashSet<>();
   public final HashMap<Player, Integer> jakes = new HashMap<>();
   private final HashMap<Player, BukkitTask> dustTasks = new HashMap<>();
   private final HashMap<UUID, Long> bottleCooldowns = new HashMap<>();
   private FileConfiguration config;

   public MantiItems() {
   }

   public static MantiItems get() {
      return main;
   }

   private boolean useItems(Location location) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      ProtectedRegion highestPriorityRegion = null;

      for (ProtectedRegion region : regions) {
         if (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority()) {
            highestPriorityRegion = region;
         }
      }

      if (highestPriorityRegion == null) {
         return true;
      } else {
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("pvp");
         if (flag == null) {
            return true;
         } else {
            State flagState = (State)highestPriorityRegion.getFlag(flag);
            return flagState == null || flagState == State.ALLOW;
         }
      }
   }

   private boolean useTrap(Location location) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery query = container.createQuery();
      Collection<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
      ProtectedRegion highestPriorityRegion = null;

      for (ProtectedRegion region : regions) {
         if (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority()) {
            highestPriorityRegion = region;
         }
      }

      if (highestPriorityRegion == null) {
         return true;
      } else {
         StateFlag flag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         if (flag == null) {
            return true;
         } else {
            State flagState = (State)highestPriorityRegion.getFlag(flag);
            return flagState == null || flagState == State.ALLOW;
         }
      }
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      if (this.dustTasks.containsKey(event.getEntity())) {
         this.dustTasks.get(event.getEntity()).cancel();
      }
   }

   public void onLoad() {
      if (WorldGuard.getInstance().getFlagRegistry().get("holyliteitems") == null) {
         try {
            StateFlag var1 = new StateFlag("holyliteitems", true);
            WorldGuard.getInstance().getFlagRegistry().register(var1);
            FLAG = var1;
            Bukkit.getLogger().severe("Flag registered: " + FLAG.getName());
         } catch (FlagConflictException var31) {
            Flag<?> var2 = WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
            if (var2 instanceof StateFlag) {
               FLAG = (StateFlag)var2;
               Bukkit.getLogger().severe("Flag already exists: " + FLAG.getName());
            } else {
               Bukkit.getLogger().severe("Flag conflict: " + var31.getMessage());
            }
         }
      } else {
         Bukkit.getLogger().severe("Flag already registered: holyliteitems");
      }
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.config = this.getConfig();
      NamespacedKey explosivetrapKey = new NamespacedKey(this, "explosivetrap");
      ShapedRecipe explosivetrapRecipe = new ShapedRecipe(explosivetrapKey, this.getTrapka(1));
      explosivetrapRecipe.shape(new String[]{"ABA", "CDC", "ABA"});
      explosivetrapRecipe.setIngredient('A', Material.BLAZE_POWDER);
      explosivetrapRecipe.setIngredient('B', this.getTnTBlock(1));
      explosivetrapRecipe.setIngredient('C', Material.SAND);
      explosivetrapRecipe.setIngredient('D', Material.REDSTONE);
      Bukkit.addRecipe(explosivetrapRecipe);
      this.recipeKeys.add(explosivetrapKey);
      NamespacedKey trapkaKey = new NamespacedKey(this, "trapka");
      ShapedRecipe trapkaRecipe = new ShapedRecipe(trapkaKey, this.getPurpleTrapka(1));
      trapkaRecipe.shape(new String[]{"ABA", "BCB", "ABA"});
      trapkaRecipe.setIngredient('B', Material.NETHERITE_SCRAP);
      trapkaRecipe.setIngredient('C', this.getTnTBlock(1));
      Bukkit.addRecipe(trapkaRecipe);
      this.recipeKeys.add(trapkaKey);
      NamespacedKey stanKey = new NamespacedKey(this, "stan");
      ShapedRecipe stanRecipe = new ShapedRecipe(stanKey, this.getStan(1));
      stanRecipe.shape(new String[]{"AAA", "BCB", "AAA"});
      stanRecipe.setIngredient('A', Material.SPIDER_EYE);
      stanRecipe.setIngredient('B', Material.BLAZE_POWDER);
      stanRecipe.setIngredient('C', this.getTnTBlock(1));
      Bukkit.addRecipe(stanRecipe);
      this.recipeKeys.add(stanKey);
      this.getCommand("holyliteitems").setExecutor(new MantiItems.MantiItemsCommand());
      this.getServer().getPluginManager().registerEvents(this, this);
   }

   private void removeRecipes() {
      for (NamespacedKey key : this.recipeKeys) {
         Bukkit.removeRecipe(key);
      }

      this.recipeKeys.clear();
   }

   @EventHandler
   public void onProjectileLaunch(PlayerLaunchProjectileEvent event) {
      if (event.getItemStack() != null
         && event.getItemStack().getItemMeta() != null
         && ((String)event.getItemStack()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("snowball")) {
         Player player = event.getPlayer();
         if (player.getCooldown(Material.SNOWBALL) == 0) {
            Projectile projectile = event.getProjectile();
            this.snowballs.add(projectile);
            player.setCooldown(Material.SNOWBALL, 600);
         } else {
            int cd = player.getCooldown(Material.SNOWBALL) / 20 + 1;
            player.sendMessage(
               ChatColor.translateAlternateColorCodes('&', "&x&f&b&6&f&0&0▶ &fПовторно активировать Ком снега возможно &x&f&b&6&f&0&0через " + cd + " секунд.")
            );
            event.setCancelled(true);
            event.getProjectile().remove();
         }
      }
   }

   @EventHandler
   public void onProjectileAttack(ProjectileHitEvent event) {
      if (this.snowballs.contains(event.getEntity()) && event.getEntity() != null && event.getHitEntity() instanceof Player) {
         Player player = (Player)event.getHitEntity();
         player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 5, false, false, true));
         player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 5, false, false, true));
      }
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
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "bottle");
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
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "jake");
      itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getTrapka(int count) {
      ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&c&4&B&D&F&FВ&x&B&D&B&6&F&6з&x&B&6&B&0&E&Dр&x&A&F&A&9&E&4ы&x&A&9&A&3&D&cв&x&A&2&9&c&D&3н&x&9&B&9&5&c&Aа&x&9&4&8&F&c&1я &x&8&D&8&8&B&8т&x&8&6&8&1&A&Fр&x&8&0&7&B&A&7а&x&7&9&7&4&9&Eп&x&7&2&6&E&9&5к&x&6&B&6&7&8&cа"
         )
      );
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&3&D&3&D&3&DОсобенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&f • &fпри взрыве наносит &x&c&4&B&D&F&Fурон в радиусе 3 блоков"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&f   и &x&c&4&B&D&F&Fсоздаёт воронку &fв земле."));
      lore.add("");
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "explosive_trap");
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getPurpleTrapka(int count) {
      ItemStack item = new ItemStack(Material.POPPED_CHORUS_FRUIT);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes('&', "&x&C&1&5&B&D&3Т&x&C&6&6&8&D&6р&x&C&B&7&4&D&Aа&x&D&0&8&1&D&Dп&x&C&9&6&E&D&8к&x&C&1&5&B&D&3а")
      );
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&f • &fпри взрыве наносит &x&e&9&6&e&f&dурон в радиусе 3 блоков"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&f   и &x&e&9&6&e&f&dсоздаёт воронку &fв месте активации."));
      lore.add("");
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "alternative_trap");
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getTnTBlock(int count) {
      ItemStack itemStack = new ItemStack(new ItemStack(Material.CLAY, count));
      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&D&5&E&B&F&FВ&x&D&2&E&7&F&Bз&x&C&E&E&4&F&7р&x&C&B&E&0&F&3ы&x&C&7&D&C&E&Fв&x&C&4&D&8&E&Bч&x&C&1&D&5&E&7а&x&B&D&D&1&E&3т&x&B&A&C&D&D&Fо&x&B&7&C&A&D&Bе&x&B&3&C&6&D&7 &x&B&0&C&2&D&3в&x&A&C&B&E&C&Fе&x&A&9&B&B&C&Bщ&x&A&6&B&7&C&7е&x&A&2&B&3&C&3с&x&9&F&A&F&B&Fт&x&9&B&A&C&B&Bв&x&9&8&A&8&B&7о"
         )
      );
      itemMeta.getPersistentDataContainer()
         .set(new NamespacedKey((JavaPlugin)Bukkit.getPluginManager().getPlugin("HolyTnT"), "tnt-item"), PersistentDataType.STRING, "explosive");
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8 Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fиспользуется только для крафта"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "   &x&D&5&E&B&F&Fвзрывных предметов"));
      lore.add("");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getExplosive(int count) {
      ItemStack item = new ItemStack(Material.FIRE_CHARGE);
      item.setAmount(count);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&F&B&3&1&0&0В&x&F&B&3&3&0&0з&x&F&B&3&5&0&0р&x&F&B&3&7&0&0ы&x&F&B&3&9&0&0в&x&F&B&3&B&0&0н&x&F&B&3&D&0&0а&x&F&B&3&F&0&0я &x&F&B&4&3&0&0ш&x&F&B&4&5&0&0т&x&F&B&4&7&0&0у&x&F&B&4&9&0&0ч&x&F&B&4&B&0&0к&x&F&B&4&D&0&0а"
         )
      );
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', " &7Особенности и применение:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A - при нажатие ПКМ, вызывает взрыв, с силой"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   способной убить игрока без брони, на расстоянии"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   3 блоков от точки активации;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A - урон наносится, в том числе игроку, который"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&F&B&6&2&4&A   активировал Взрывную Штучку."));
      lore.add("");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "explosive_stuff");
      itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack getStan(int count) {
      ItemStack stan = new ItemStack(Material.NETHER_STAR);
      stan.setAmount(count);
      ItemMeta stanMeta = stan.getItemMeta();
      stanMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&f&e&f&e&fС&x&e&6&e&6&e&6т&x&d&c&d&c&d&cа&x&d&3&d&3&d&3н"));
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fсоздает &7куб (30х30х30) &fна 15 секунд"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7   &fигроки в нем &7не могут использовать"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7   &fэндер-жемчуги и хорусы и &7накладывает"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7   &7эффект Замедление 1 &fна всех, кроме"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7   &fактиватора."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      stanMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "stan");
      stanMeta.addEnchant(Enchantment.LOOTING, 1, true);
      stanMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      stanMeta.setLore(lore);
      stan.setItemMeta(stanMeta);
      return stan;
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
      stanMeta.getPersistentDataContainer().set(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "snowball");
      stanMeta.addEnchant(Enchantment.LOOTING, 1, true);
      stanMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      stanMeta.setLore(lore);
      stan.setItemMeta(stanMeta);
      return stan;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (event.getItem() == null) {
            return;
         }

         Player player = event.getPlayer();
         Location loc = player.getLocation();
         if (!event.getItem().hasItemMeta()) {
            return;
         }

         if (((String)event.getItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("stan")) {
            event.setCancelled(true);
            if (player.getCooldown(event.getItem().getType()) == 0) {
               if (!this.useItems(player.getLocation())) {
                  return;
               }

               event.getItem().setAmount(event.getItem().getAmount() - 1);
               Location playerLocation = player.getLocation();
               World world = playerLocation.getWorld();
               if (world == null) {
                  player.sendMessage(ChatColor.RED + "Ошибка: мир недоступен. Попробуйте снова.");
                  return;
               }

               this.createStan(player, playerLocation, 30, 15L);
               playerLocation.getWorld().playSound(playerLocation, Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 1.0F);
               player.setCooldown(Material.NETHER_STAR, 600);
            } else {
               int cd = player.getCooldown(Material.NETHER_STAR) / 20 + 1;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&f&2&2&2&2▶ &fВы сможете повторно использовать этот предмет только через &x&f&f&2&2&2&2" + cd + " секунд."
                  )
               );
            }
         } else if (((String)event.getItem()
                  .getItemMeta()
                  .getPersistentDataContainer()
                  .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
               .equalsIgnoreCase("alternative_trap")
            && !this.config.getBoolean("oldmeta", false)) {
            event.setCancelled(true);
            if (player.getCooldown(event.getItem().getType()) == 0) {
               if (!this.useTrap(player.getLocation())) {
                  return;
               }

               event.getItem().setAmount(event.getItem().getAmount() - 1);
               Location playerLocation = player.getLocation();
               World world = playerLocation.getWorld();
               if (world == null) {
                  player.sendMessage(ChatColor.RED + "Ошибка: мир недоступен. Попробуйте снова.");
                  return;
               }

               String trapSkin = new CustomAPI().getPlayerData(player, "trap_skin");
               if (trapSkin == null) {
                  trapSkin = "endstone";
               }

               if (trapSkin.equals("random")) {
                  Random random = new Random();
                  trapSkin = this.trapSkins.get(random.nextInt(this.trapSkins.size()));
               }

               Material baseMaterial;
               switch (trapSkin) {
                  case "nether":
                     baseMaterial = Material.NETHER_BRICKS;
                     break;
                  case "rednether":
                     baseMaterial = Material.RED_NETHER_BRICKS;
                     break;
                  case "blackstone":
                     baseMaterial = Material.POLISHED_BLACKSTONE;
                     break;
                  case "quartz":
                     baseMaterial = Material.QUARTZ_BLOCK;
                     break;
                  case "crimson":
                     baseMaterial = Material.CRIMSON_PLANKS;
                     break;
                  case "warped":
                     baseMaterial = Material.WARPED_PLANKS;
                     break;
                  case "redsand":
                     baseMaterial = Material.RED_SANDSTONE;
                     break;
                  case "prismarine":
                     baseMaterial = Material.PRISMARINE;
                     break;
                  case "purpur":
                     baseMaterial = Material.PURPUR_BLOCK;
                     break;
                  case "stone":
                     baseMaterial = Material.STONE_BRICKS;
                     break;
                  case "spruce":
                     baseMaterial = Material.SPRUCE_PLANKS;
                     break;
                  case "endstone":
                  default:
                     baseMaterial = Material.END_STONE_BRICKS;
               }

               player.setCooldown(Material.PRISMARINE_SHARD, 500);
               player.setCooldown(Material.POPPED_CHORUS_FRUIT, 500);
               this.altTraps
                  .add(new AlternativeTrap(this, playerLocation, player, baseMaterial, 2, playerLocation.getWorld().getName().equalsIgnoreCase("spawn")));
            } else {
               int cd = player.getCooldown(Material.PRISMARINE_SHARD) / 20 + 1;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&f&2&2&2&2▶ &fВы сможете повторно использовать этот предмет только через &x&f&f&2&2&2&2" + cd + " секунд."
                  )
               );
            }
         } else if (((String)event.getItem()
                  .getItemMeta()
                  .getPersistentDataContainer()
                  .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
               .equalsIgnoreCase("explosive_trap")
            && !this.config.getBoolean("oldmeta", false)) {
            event.setCancelled(true);
            if (player.getCooldown(event.getItem().getType()) == 0) {
               if (!this.useTrap(player.getLocation())) {
                  return;
               }

               event.getItem().setAmount(event.getItem().getAmount() - 1);
               Location playerLocationx = player.getLocation();
               World worldx = playerLocationx.getWorld();
               if (worldx == null) {
                  player.sendMessage(ChatColor.RED + "Ошибка: мир недоступен. Попробуйте снова.");
                  return;
               }

               player.setCooldown(Material.PRISMARINE_SHARD, 500);
               player.setCooldown(Material.POPPED_CHORUS_FRUIT, 500);
               String zamokFile = "trap";
               Boolean jump = true;
               Boolean onlyAir = false;
               Location under1 = playerLocationx.clone();
               under1.setY(under1.getY() - 1.0);
               Location under2 = playerLocationx.clone();
               under2.setY(under2.getY() - 2.0);
               if (playerLocationx.getBlock().getType() == Material.WATER
                  || playerLocationx.getBlock().getType() == Material.LAVA
                  || playerLocationx.getBlock().getType() == Material.SEAGRASS
                  || playerLocationx.getBlock().getType() == Material.TALL_SEAGRASS) {
                  zamokFile = "trapair";
                  jump = false;
               } else if (playerLocationx.getWorld().getName().equals("world_the_end")) {
                  zamokFile = "trapend";
                  jump = true;
               } else if (playerLocationx.getWorld().getName().equals("spawn") || playerLocationx.getWorld().getName().equals("deatharea")) {
                  zamokFile = "traprg";
                  jump = false;
                  onlyAir = true;
               } else if (under1.getBlock().getType().toString().contains("SAND") || under2.getBlock().getType().toString().contains("SAND")) {
                  zamokFile = "trapsand";
                  jump = true;
               } else if (under1.getBlock().getBlockData().getAsString().contains("snowy=true")
                  || under1.getBlock().getType().toString().contains("SNOW")
                  || under1.getBlock().getType().toString().contains("ICE")
                  || under2.getBlock().getBlockData().getAsString().contains("minecraft:grass_block[snowy=true]")
                  || under2.getBlock().getType().toString().contains("SNOW")
                  || under2.getBlock().getType().toString().contains("ICE")) {
                  zamokFile = "trapsnow";
                  jump = true;
               } else if (under1.getBlock().getType().toString().contains("STONE")
                  || under1.getBlock().getType().toString().contains("ORE")
                  || under1.getBlock().getType().toString().contains("DIORITE")
                  || under1.getBlock().getType().toString().contains("ANDESITE")
                  || under1.getBlock().getType().toString().contains("GRANITE")
                  || under1.getBlock().getType().toString().contains("GRAVEL")
                  || under2.getBlock().getType().toString().contains("STONE")
                  || under2.getBlock().getType().toString().contains("ORE")
                  || under2.getBlock().getType().toString().contains("DIORITE")
                  || under2.getBlock().getType().toString().contains("ANDESITE")
                  || under2.getBlock().getType().toString().contains("GRANITE")
                  || under2.getBlock().getType().toString().contains("GRAVEL")) {
                  zamokFile = "trapstone";
                  jump = true;
               } else if (playerLocationx.getBlock().getType().toString().contains("AIR")
                  && under1.getBlock().getType().toString().contains("AIR")
                  && under2.getBlock().getType().toString().contains("AIR")) {
                  zamokFile = "trapair";
                  jump = false;
               }

               this.traps.add(new ExplosiveTrap(this, playerLocationx, player, zamokFile, jump, onlyAir));
            } else {
               int cd = player.getCooldown(Material.PRISMARINE_SHARD) / 20 + 1;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&f&2&2&2&2▶ &fВы сможете повторно использовать этот предмет только через &x&f&f&2&2&2&2" + cd + " секунд."
                  )
               );
            }
         } else if (((String)event.getItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("explosive_stuff")) {
            event.setCancelled(true);
            if (player.getCooldown(event.getItem().getType()) == 0) {
               if (!this.useItems(player.getLocation())) {
                  return;
               }

               event.getItem().setAmount(event.getItem().getAmount() - 1);
               Location playerLocationxx = player.getLocation();
               World worldxx = playerLocationxx.getWorld();
               playerLocationxx.getWorld().createExplosion(playerLocationxx, 3.0F, false, false);
               player.setCooldown(Material.FIRE_CHARGE, 900);
               Location explosionCenter = playerLocationxx.clone().add(0.0, -3.0, 0.0);

               for (Player p : playerLocationxx.getNearbyPlayers(3.0)) {
                  Vector direction = p.getLocation().toVector().subtract(explosionCenter.toVector());
                  p.setVelocity(direction.normalize().multiply(1.2).setY(direction.getY() > 0.0 ? direction.getY() : 0.5));
               }
            } else {
               int cd = player.getCooldown(Material.FIRE_CHARGE) / 20 + 1;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&b&6&f&0&0▶ &fПовторно активировать Взрывную штучку возможно &x&f&b&6&f&0&0через " + cd + " секунд."
                  )
               );
            }
         } else if (((String)event.getItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("bottle")) {
            event.setCancelled(true);
            if (this.bottleCooldowns.getOrDefault(player.getUniqueId(), 0L) < System.currentTimeMillis()) {
               for (int i = 36; i < 40; i++) {
                  if (player.getInventory().getItem(i) != null) {
                     player.getInventory().getItem(i).setDurability((short)0);
                  }
               }

               this.bottleCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 600000L);
               event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
               long cd = this.bottleCooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
               cd /= 1000L;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&b&6&f&0&0▶ &fПовторно активировать Пузырь опыта возможно &x&f&b&6&f&0&0через " + ++cd + " секунд."
                  )
               );
            }
         } else if (((String)event.getItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("jake")) {
            event.setCancelled(true);
            if (player.getCooldown(event.getItem().getType()) == 0) {
               if (!this.useItems(player.getLocation())) {
                  return;
               }

               event.getItem().setAmount(event.getItem().getAmount() - 1);
               player.setCooldown(Material.JACK_O_LANTERN, 900);
               Location center = player.getLocation();
               center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

               for (Location location : getCircle(center, 0.25, 80)) {
                  Vector direction = location.toVector().subtract(center.toVector()).normalize();
                  UtilParticles.display(Particles.EXPLOSION_NORMAL, direction, center, 0.5F);
               }

               final HashSet<Player> players = new HashSet<>();

               for (Player player1 : center.getWorld().getPlayers()) {
                  if (player1.getLocation().distance(center) <= 15.0 && player1 != player) {
                     if (player1.getInventory().getItem(39) == null) {
                        player1.getInventory().setItem(39, new ItemStack(Material.JACK_O_LANTERN));
                        players.add(player1);
                     }

                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + player1.getName() + " " + player.getName());
                     player1.setFireTicks(10);
                     int distance = (int)(15.0 - player1.getLocation().distance(player.getLocation()));
                     player1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, distance * 2, 0, false, false, true));
                     player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, distance * 4, distance / 5, false, false, true));
                     player1.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, distance * 4, distance / 3 + 1, false, false, true));
                     player1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, distance * 4, 0, false, false, true));
                     player1.setCooldown(Material.NETHER_STAR, player.getCooldown(Material.NETHER_STAR) + distance * 4);
                  }
               }

               if (!players.isEmpty()) {
                  (new BukkitRunnable() {
                     int seconds = 16;

                     public void run() {
                        this.seconds--;
                        if (this.seconds <= 0) {
                           for (Player player1x : players) {
                              MantiItems.this.jakes.remove(player1x);
                           }

                           this.cancel();
                        } else {
                           for (Player player1x : players) {
                              MantiItems.this.jakes.put(player1x, this.seconds);
                           }
                        }
                     }
                  }).runTaskTimer(this, 0L, 20L);
               }
            } else {
               int cd = player.getCooldown(Material.JACK_O_LANTERN) / 20 + 1;
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&f&b&6&f&0&0▶ &fПовторно активировать Светильник Джейка возможно &x&f&b&6&f&0&0через " + cd + " секунд."
                  )
               );
            }
         }
      }
   }

   public static List<Location> getCircle(Location location, double radius, int points) {
      List<Location> locations = new ArrayList<>();
      double increment = (Math.PI * 2) / points;

      for (int i = 0; i < points; i++) {
         double angle = i * increment;
         double x = location.getX() + Math.cos(angle) * radius;
         double z = location.getZ() + Math.sin(angle) * radius;
         locations.add(new Location(location.getWorld(), x, location.getY(), z));
      }

      return locations;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
         && event.getItem() != null
         && event.getItem().getType() == Material.ENDER_PEARL
         && !this.config.getBoolean("oldmeta", false)
         && this.isInsideCube(event.getPlayer().getLocation())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getSlot() == 39
         && event.getCurrentItem() != null
         && event.getCurrentItem().getType() == Material.JACK_O_LANTERN
         && this.jakes.containsKey((Player)event.getWhoClicked())) {
         event.setCancelled(true);
         event.getWhoClicked()
            .sendMessage(
               ChatColor.translateAlternateColorCodes(
                  '&', "&x&F&B&6&F&0&0▶ &fВы не можете снять светильник еще &x&F&B&6&F&0&0" + this.jakes.get((Player)event.getWhoClicked()) + " секунд."
               )
            );
      }
   }

   @EventHandler
   public void onConsume(PlayerItemConsumeEvent event) {
      if (this.isInsideCube(event.getPlayer().getLocation())
         && !this.config.getBoolean("oldmeta", false)
         && event.getItem() != null
         && event.getItem().getType() == Material.CHORUS_FRUIT) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onDispence(BlockPreDispenseEvent event) {
      if (event.getItemStack() != null
         && event.getItemStack().getItemMeta() != null
         && ((String)event.getItemStack()
               .getItemMeta()
               .getPersistentDataContainer()
               .getOrDefault(new NamespacedKey(this, "item-type"), PersistentDataType.STRING, "none"))
            .equalsIgnoreCase("stan")) {
         event.getItemStack().setAmount(event.getItemStack().getAmount() - 1);
         this.createStan(null, event.getBlock().getLocation(), 30, 15L);
         event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2.0F, 1.0F);
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         event.setDamage(event.getDamage() * 1.25);
      }
   }

   public void createStan(final Player player, final Location center, int size, final long duration) {
      final World world = center.getWorld();
      final double minX = center.getX() - size / 2.0;
      final double minY = center.getY() - size / 2.0;
      final double minZ = center.getZ() - size / 2.0;
      final double maxX = center.getX() + size / 2.0;
      final double maxY = center.getY() + size / 2.0;
      final double maxZ = center.getZ() + size / 2.0;
      this.activeCubes.add(center);
      if (player == null) {
         for (Player player1 : world.getPlayers()) {
            Location location = player1.getLocation();
            if (location.getX() >= minX
               && location.getX() <= maxX
               && location.getY() >= minY
               && location.getY() <= maxY
               && location.getZ() >= minZ
               && location.getZ() <= maxZ) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + player1.getName() + " " + 30);
            }
         }
      } else {
         for (Player player1x : world.getPlayers()) {
            Location location = player1x.getLocation();
            if (location.getX() >= minX
               && location.getX() <= maxX
               && location.getY() >= minY
               && location.getY() <= maxY
               && location.getZ() >= minZ
               && location.getZ() <= maxZ) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "updatepvp " + player1x.getName() + " " + player.getName());
            }
         }
      }

      final double step = 0.75;
      Color color;
      final String finalStanSkin;
      if (player == null) {
         color = Color.YELLOW;
         finalStanSkin = "yellow";
      } else {
         String stanSkin = new CustomAPI().getPlayerData(player, "stan_skin");
         if (stanSkin == null) {
            stanSkin = "yellow";
         }

         Map<String, Color> colors = new HashMap<>();
         colors.put("black", Color.BLACK);
         colors.put("red", Color.RED);
         colors.put("white", Color.WHITE);
         colors.put("orange", Color.ORANGE);
         colors.put("purple", Color.PURPLE);
         colors.put("blue", Color.BLUE);
         colors.put("lime", Color.LIME);
         colors.put("light_blue", Color.AQUA);
         colors.put("magenta", Color.FUCHSIA);
         colors.put("cyan", Color.TEAL);
         colors.put("green", Color.GREEN);
         colors.put("yellow", Color.YELLOW);
         if (stanSkin.equals("random")) {
            List<Color> colorList = new ArrayList<>(colors.values());
            Random random = new Random();
            color = colorList.get(random.nextInt(colorList.size()));
         } else {
            color = colors.getOrDefault(stanSkin, Color.YELLOW);
         }

         finalStanSkin = stanSkin;
      }

      DustOptions dustOptions = new DustOptions(color, 1.0F);
      final Color finalColor = color;
      final DustOptions finalDustOptions = dustOptions;
      (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            public void run() {
               if (System.currentTimeMillis() - this.startTime >= duration * 1000L) {
                  MantiItems.this.activeCubes.remove(center);
                  this.cancel();
               } else {
                  if (player == null) {
                     for (Player player1xx : world.getPlayers()) {
                        Location location = player1xx.getLocation();
                        if (location.getX() >= minX
                           && location.getX() <= maxX
                           && location.getY() >= minY
                           && location.getY() <= maxY
                           && location.getZ() >= minZ
                           && location.getZ() <= maxZ) {
                           player1xx.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, false, false, true));
                        }
                     }
                  } else {
                     for (Player player1x : world.getPlayers()) {
                        if (player != player1x) {
                           Location location = player1x.getLocation();
                           if (location.getX() >= minX
                              && location.getX() <= maxX
                              && location.getY() >= minY
                              && location.getY() <= maxY
                              && location.getZ() >= minZ
                              && location.getZ() <= maxZ) {
                              player1x.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, false, false, true));
                           }
                        }
                     }
                  }

                  Color colorx = finalColor;
                  DustOptions dustOptionsx = finalDustOptions;
                  if (finalStanSkin != null && finalStanSkin.equals("random")) {
                     Map<String, Color> colors = new HashMap<>();
                     colors.put("black", Color.BLACK);
                     colors.put("red", Color.RED);
                     colors.put("white", Color.WHITE);
                     colors.put("orange", Color.ORANGE);
                     colors.put("purple", Color.PURPLE);
                     colors.put("blue", Color.BLUE);
                     colors.put("lime", Color.LIME);
                     colors.put("light_blue", Color.AQUA);
                     colors.put("magenta", Color.FUCHSIA);
                     colors.put("cyan", Color.TEAL);
                     colors.put("green", Color.GREEN);
                     colors.put("yellow", Color.YELLOW);
                     List<Color> colorList = new ArrayList<>(colors.values());
                     Random random = new Random();
                     colorx = colorList.get(random.nextInt(colorList.size()));
                     dustOptionsx = new DustOptions(colorx, 1.0F);
                  }

                  for (double x = minX; x <= maxX; x += step) {
                     world.spawnParticle(Particle.DUST, x, minY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, x, minY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, x, maxY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, x, maxY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                  }

                  for (double y = minY; y <= maxY; y += step) {
                     world.spawnParticle(Particle.DUST, minX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, minX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, maxX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, maxX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                  }

                  for (double z = minZ; z <= maxZ; z += step) {
                     world.spawnParticle(Particle.DUST, minX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, minX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, maxX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                     world.spawnParticle(Particle.DUST, maxX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptionsx, true);
                  }
               }
            }
         })
         .runTaskTimer(this, 0L, 20L);
   }

   private boolean isInsideCube(Location location) {
      for (Location cubeCenter : this.activeCubes) {
         double minX = cubeCenter.getX() - 15.0;
         double minY = cubeCenter.getY() - 15.0;
         double minZ = cubeCenter.getZ() - 15.0;
         double maxX = cubeCenter.getX() + 15.0;
         double maxY = cubeCenter.getY() + 15.0;
         double maxZ = cubeCenter.getZ() + 15.0;
         if (location.getX() >= minX
            && location.getX() <= maxX
            && location.getY() >= minY
            && location.getY() <= maxY
            && location.getZ() >= minZ
            && location.getZ() <= maxZ) {
            return true;
         }
      }

      return false;
   }

   public void onDisable() {
      this.removeRecipes();

      for (ExplosiveTrap trap : this.traps) {
         trap.delete();
      }

      for (AlternativeTrap trap : this.altTraps) {
         trap.delete();
      }
   }

   private class MantiItemsCommand implements CommandExecutor {
      private MantiItemsCommand() {
      }

      public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length != 4) {
            sender.sendMessage("Использование: /holyliteitems give [ник] [stan/trap/alternativetrap/snowball/explosive/bottle/jake] [количество]");
            return true;
         } else {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
               sender.sendMessage("Игрок не найден");
               return true;
            } else {
               if (args[2].equals("stan")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getStan(Integer.parseInt(args[3]))});
               } else if (args[2].equals("trap")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getTrapka(Integer.parseInt(args[3]))});
               } else if (args[2].equals("alternativetrap")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getPurpleTrapka(Integer.parseInt(args[3]))});
               } else if (args[2].equals("snowball")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getSnowball(Integer.parseInt(args[3]))});
               } else if (args[2].equals("explosive")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getExplosive(Integer.parseInt(args[3]))});
               } else if (args[2].equals("bottle")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getBottle(Integer.parseInt(args[3]))});
               } else if (args[2].equals("jake")) {
                  player.getInventory().addItem(new ItemStack[]{MantiItems.this.getJake(Integer.parseInt(args[3]))});
               }

               return true;
            }
         }
      }
   }
}
