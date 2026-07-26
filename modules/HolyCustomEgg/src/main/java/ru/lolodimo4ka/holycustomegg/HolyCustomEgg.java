package ru.lolodimo4ka.holycustomegg;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner;

public class HolyCustomEgg extends JavaPlugin implements Listener, TabCompleter {
   private static HolyCustomEgg instance;
   private Map<String, ManagerEgg> eggs = new HashMap<>();
   private Random random = new Random();
   private Map<Location, HolyCustomEgg.ActiveSpawner> activeSpawners = new ConcurrentHashMap<>();
   private FileConfiguration config;
   private ItemStack jakePickaxe;
   private ConfigEgg configEgg;
   private Database database;

   public HolyCustomEgg() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.loadConfig();
      this.database = new Database(this);
      this.getServer().getPluginManager().registerEvents(new ListenerEgg(this, this.eggs), this);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.getCommand("holycustomegg").setTabCompleter(this);
      this.loadSpawnersFromDatabase();
   }

   public Database getDatabase() {
      return this.database;
   }

   private void loadSpawnersFromDatabase() {
      this.database.loadAllActiveSpawners().thenAccept(spawners -> this.getServer().getScheduler().runTask(this, () -> {
         this.activeSpawners.clear();
         this.activeSpawners.putAll((Map<? extends Location, ? extends HolyCustomEgg.ActiveSpawner>)spawners);
         this.getLogger().info("Активные спавнеры загружены из БД: " + this.activeSpawners.size());

         for (Entry<Location, HolyCustomEgg.ActiveSpawner> entry : this.activeSpawners.entrySet()) {
            Location location = entry.getKey();
            HolyCustomEgg.ActiveSpawner spawner = entry.getValue();
            if (location.getChunk().isLoaded() && location.getBlock().getType() == Material.SPAWNER) {
               CreatureSpawner creatureSpawner = (CreatureSpawner)location.getBlock().getState();
               creatureSpawner.setSpawnedType(spawner.getEgg().getRandomMob());
               creatureSpawner.update(false);
            }
         }
      }));
   }

   private void saveAllSpawnersToDatabase() {
      for (Entry<Location, HolyCustomEgg.ActiveSpawner> entry : this.activeSpawners.entrySet()) {
         Location location = entry.getKey();
         HolyCustomEgg.ActiveSpawner spawner = entry.getValue();
         if (location.getChunk().isLoaded() && location.getBlock().getType() == Material.SPAWNER) {
            this.database.saveActiveSpawner(location, spawner.getEgg().getId(), spawner.getLastChangeTime());
         }
      }

      this.getLogger().info("Все активные спавнеры сохранены в БД");
   }

   public void onDisable() {
      this.saveAllSpawnersToDatabase();
      this.activeSpawners.clear();
      if (this.database != null) {
         this.database.close();
      }
   }

   public static HolyCustomEgg getInstance() {
      return instance;
   }

   private void loadConfig() {
      this.config = this.getConfig();
      this.eggs.clear();
      this.configEgg = new ConfigEgg(this.config);
      this.eggs = this.configEgg.getEggs();
      this.loadJakePickaxe();
   }

   private void loadJakePickaxe() {
      String name = this.config.getString("jake_pickaxe.name");
      List<String> lore = this.config.getStringList("jake_pickaxe.lore");
      if (name != null && lore != null) {
         this.jakePickaxe = new ItemStack(Material.GOLDEN_PICKAXE);
         ItemMeta meta = this.jakePickaxe.getItemMeta();
         if (meta != null) {
            meta.setDisplayName(Utils.formatColors(name));
            meta.setLore(Utils.formatColors(lore));
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            NamespacedKey key = new NamespacedKey(this, "jake_pickaxe");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");
            this.jakePickaxe.setItemMeta(meta);
         }
      } else {
         this.jakePickaxe = new ItemStack(Material.GOLDEN_PICKAXE);
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("holycustomegg.admin")) {
         sender.sendMessage("§x§f§f§0§0§0§0▶§f У вас нет прав на использование этой команды");
         return true;
      } else if (args.length < 2) {
         sender.sendMessage("§6Использование: /hce give <eggs|spawner|pickaxe> [тип] [количество]");
         return true;
      } else if (!args[0].equalsIgnoreCase("give")) {
         sender.sendMessage("§6Использование: /hce give <eggs|spawner|pickaxe> [тип] [количество]");
         return true;
      } else if (!(sender instanceof Player)) {
         sender.sendMessage("§x§f§f§0§0§0§0▶§f Эта команда только для игроков");
         return true;
      } else {
         Player player = (Player)sender;
         String subCommand = args[1].toLowerCase();
         switch (subCommand) {
            case "pickaxe":
               this.givePickaxe(player, args);
               break;
            case "eggs":
               this.giveEggs(player, args);
               break;
            case "spawner":
               this.giveSpawner(player, args);
               break;
            default:
               player.sendMessage("§x§f§f§0§0§0§0▶§f Неизвестная подкоманда: " + subCommand);
         }

         return true;
      }
   }

   private void givePickaxe(Player player, String[] args) {
      int amount = 1;
      if (args.length >= 3) {
         try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1) {
               player.sendMessage("§x§f§f§0§0§0§0▶§f Количество должно быть положительным");
               return;
            }
         } catch (NumberFormatException var7) {
            player.sendMessage("§x§f§f§0§0§0§0▶§f Неверное количество");
            return;
         }
      }

      for (int i = 0; i < amount; i++) {
         ItemStack pickaxe = this.jakePickaxe.clone();
         pickaxe.setAmount(1);
         HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{pickaxe});
         if (!leftover.isEmpty()) {
            player.sendMessage("§x§f§f§0§0§0§0▶§f Инвентарь заполнен, не все кирки были выданы");
            break;
         }
      }

      player.sendMessage("§x§0§0§f§f§0§0▶§f Выдана кирка Джейка");
   }

   private void giveEggs(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage("§x§f§f§0§0§0§0▶§f Использование: /hce give eggs <тип> [количество]");
         player.sendMessage("§x§f§f§0§0§0§0▶§f Доступные типы: " + String.join(", ", this.eggs.keySet()));
      } else {
         String eggType = args[2].toLowerCase();
         if (!this.eggs.containsKey(eggType)) {
            player.sendMessage("§x§f§f§0§0§0§0▶§f Неизвестный тип яйца: " + eggType);
            player.sendMessage("§x§f§f§0§0§0§0▶§f Доступные типы: " + String.join(", ", this.eggs.keySet()));
         } else {
            int amount = 1;
            if (args.length >= 4) {
               try {
                  amount = Integer.parseInt(args[3]);
                  if (amount < 1 || amount > 64) {
                     player.sendMessage("§x§f§f§0§0§0§0▶§f Количество должно быть от 1 до 64");
                     return;
                  }
               } catch (NumberFormatException var7) {
                  player.sendMessage("§x§f§f§0§0§0§0▶§f Неверное количество");
                  return;
               }
            }

            ItemStack egg = this.eggs.get(eggType).getEgg();
            egg.setAmount(amount);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{egg});
            if (!leftover.isEmpty()) {
               player.sendMessage("§x§f§f§0§0§0§0▶§f Инвентарь заполнен, не все яйца были выданы");
            } else {
               player.sendMessage("§x§0§0§f§f§0§0▶§f Выдано загадочное яйцо призыва");
            }
         }
      }
   }

   private void giveSpawner(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage("§x§f§f§0§0§0§0▶§f Использование: /hce give spawner <тип> [количество]");
         player.sendMessage("§x§f§f§0§0§0§0▶§f Доступные типы: " + String.join(", ", this.eggs.keySet()));
      } else {
         String eggType = args[2].toLowerCase();
         if (!this.eggs.containsKey(eggType)) {
            player.sendMessage("§x§f§f§0§0§0§0▶§f Неизвестный тип спавнера: " + eggType);
            player.sendMessage("§x§f§f§0§0§0§0▶§f Доступные типы: " + String.join(", ", this.eggs.keySet()));
         } else {
            int amount = 1;
            if (args.length >= 4) {
               try {
                  amount = Integer.parseInt(args[3]);
                  if (amount < 1 || amount > 64) {
                     player.sendMessage("§x§f§f§0§0§0§0▶§f Количество должно быть от 1 до 64");
                     return;
                  }
               } catch (NumberFormatException var7) {
                  player.sendMessage("§x§f§f§0§0§0§0▶§f Неверное количество");
                  return;
               }
            }

            ItemStack spawner = this.eggs.get(eggType).getSpawner();
            spawner.setAmount(amount);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{spawner});
            if (!leftover.isEmpty()) {
               player.sendMessage("§x§f§f§0§0§0§0▶§f Инвентарь заполнен, не все спавнеры были выданы");
            } else {
               player.sendMessage("§x§0§0§f§f§0§0▶§f Выдан загадочный спанер");
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("give");
      } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
         completions.add("eggs");
         completions.add("spawner");
         completions.add("pickaxe");
      } else if (args.length == 3 && args[0].equalsIgnoreCase("give") && (args[1].equalsIgnoreCase("eggs") || args[1].equalsIgnoreCase("spawner"))) {
         completions.addAll(this.eggs.keySet());
      }

      return completions;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockDamage(BlockDamageEvent event) {
      Block block = event.getBlock();
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (block.getType() != Material.SPAWNER && this.isJakePickaxe(tool)) {
         event.setCancelled(true);
         player.sendMessage("§x§f§f§0§0§0§0▶§f Эта кирка предназначена только для спавнеров!");
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Block block = event.getBlock();
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (block.getType() != Material.SPAWNER && this.isJakePickaxe(tool)) {
         event.setCancelled(true);
         player.sendMessage("§x§f§f§0§0§0§0▶§f Эта кирка предназначена только для спавнеров!");
      } else {
         if (block.getType() == Material.SPAWNER) {
            if (this.isGoldenSpawner(block.getLocation()) && !this.isJakePickaxe(tool)) {
               event.setCancelled(true);
               player.sendMessage("§x§f§f§0§0§0§0▶§f Этот спавнер можно сломать только специальной киркой Джейка!");
               return;
            }

            if (this.isJakePickaxe(tool)) {
               event.setCancelled(true);
               this.handleSpawnerBreak(player, block);
            }
         }
      }
   }

   public boolean isJakePickaxe(ItemStack item) {
      if (item != null && item.getType() == Material.GOLDEN_PICKAXE && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         NamespacedKey key = new NamespacedKey(this, "jake_pickaxe");
         return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)
            ? true
            : meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public ItemStack getJakePickaxeItem() {
      return this.jakePickaxe.clone();
   }

   public boolean isCustomSpawner(ItemStack spawnerItem) {
      return Utils.findEggBySpawnerNBT(spawnerItem, this.eggs, this) != null;
   }

   public boolean isCustomSpawner(Block block) {
      if (block.getType() != Material.SPAWNER) {
         return false;
      } else {
         Location location = block.getLocation();
         return this.activeSpawners.containsKey(location);
      }
   }

   public ManagerEgg getEggById(String eggId) {
      return this.eggs.get(eggId);
   }

   public Map<String, ManagerEgg> getAllEggs() {
      return new HashMap<>(this.eggs);
   }

   public ItemStack createCustomSpawner(String eggId) {
      ManagerEgg egg = this.eggs.get(eggId);
      return egg != null ? egg.getSpawner() : null;
   }

   public ItemStack createCustomEgg(String eggId) {
      ManagerEgg egg = this.eggs.get(eggId);
      return egg != null ? egg.getEgg() : null;
   }

   private void handleSpawnerBreak(Player player, Block spawnerBlock) {
      Location location = spawnerBlock.getLocation();
      CreatureSpawner spawner = (CreatureSpawner)spawnerBlock.getState();
      ItemStack dropSpawner;
      if (this.isGoldenSpawner(location)) {
         dropSpawner = this.handleGoldenSpawnerBreak(player, location);
         if (dropSpawner == null) {
            spawnerBlock.setType(Material.AIR);
            this.breakJakePickaxe(player);
            return;
         }
      } else if (this.activeSpawners.containsKey(location)) {
         HolyCustomEgg.ActiveSpawner activeSpawner = this.activeSpawners.get(location);
         dropSpawner = activeSpawner.getEgg().getSpawner();
         this.activeSpawners.remove(location);
         this.database.removeActiveSpawner(location);
      } else {
         dropSpawner = this.createSpawnerWithHolySpawnersData(spawner.getSpawnedType());
      }

      spawnerBlock.setType(Material.AIR);
      spawnerBlock.getWorld().dropItemNaturally(location, dropSpawner);
      this.breakJakePickaxe(player);
   }

   private ItemStack createSpawnerWithHolySpawnersData(EntityType entityType) {
      Plugin holySpawnersPlugin = this.getServer().getPluginManager().getPlugin("HolySpawners");
      if (holySpawnersPlugin != null) {
         try {
            Class<?> holySpawnersClass = holySpawnersPlugin.getClass();
            Method getInstanceMethod = holySpawnersClass.getMethod("getInstance");
            Object holySpawnersInstance = getInstanceMethod.invoke(null);
            Method getConfigManagerMethod = holySpawnersClass.getMethod("getConfigManager");
            Object configManager = getConfigManagerMethod.invoke(holySpawnersInstance);
            Class<?> configManagerClass = configManager.getClass();
            Method getSpawnerNameMethod = configManagerClass.getMethod("getSpawnerName", String.class);
            Method getSpawnerLoreMethod = configManagerClass.getMethod("getSpawnerLore", String.class);
            String entityName = entityType.name();
            String name = (String)getSpawnerNameMethod.invoke(configManager, entityName);
            List<String> lore = (List<String>)getSpawnerLoreMethod.invoke(configManager, entityName);
            return this.createSpawnerItemWithCustomData(entityType, name, lore);
         } catch (Exception var14) {
            this.getLogger().warning("Failed to use HolySpawners: " + var14.getMessage());
         }
      }

      ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
      BlockStateMeta meta = (BlockStateMeta)spawnerItem.getItemMeta();
      if (meta != null) {
         CreatureSpawner spawnerMeta = (CreatureSpawner)meta.getBlockState();
         spawnerMeta.setSpawnedType(entityType);
         meta.setBlockState(spawnerMeta);
         meta.setDisplayName("§f" + entityType.name());
         spawnerItem.setItemMeta(meta);
      }

      return spawnerItem;
   }

   private ItemStack createSpawnerItemWithCustomData(EntityType entityType, String name, List<String> lore) {
      ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
      ItemMeta meta = spawnerItem.getItemMeta();
      if (meta instanceof BlockStateMeta) {
         BlockStateMeta blockMeta = (BlockStateMeta)meta;
         CreatureSpawner spawner = (CreatureSpawner)blockMeta.getBlockState();
         spawner.setSpawnedType(entityType);
         blockMeta.setBlockState(spawner);
         if (name != null) {
            blockMeta.setDisplayName(this.translateColors(name));
         }

         if (lore != null && !lore.isEmpty()) {
            List<String> translatedLore = new ArrayList<>();

            for (String loreLine : lore) {
               translatedLore.add(this.translateColors(loreLine));
            }

            blockMeta.setLore(translatedLore);
         }

         spawnerItem.setItemMeta(blockMeta);
      }

      return spawnerItem;
   }

   private String translateColors(String text) {
      Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
      Matcher matcher = HEX_PATTERN.matcher(text);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hexCode = matcher.group();

         try {
            ChatColor color = ChatColor.of(hexCode);
            matcher.appendReplacement(buffer, color.toString());
         } catch (Exception var7) {
            matcher.appendReplacement(buffer, hexCode);
         }
      }

      matcher.appendTail(buffer);
      return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
   }

   private boolean isGoldenSpawner(Location location) {
      try {
         Plugin goldSpawnerPlugin = this.getServer().getPluginManager().getPlugin("HolyGoldSpawner");
         if (goldSpawnerPlugin != null && goldSpawnerPlugin.isEnabled()) {
            HolyGoldSpawner goldSpawner = (HolyGoldSpawner)goldSpawnerPlugin;
            return goldSpawner.getSpawnerManager().containsSpawner(location);
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private ItemStack handleGoldenSpawnerBreak(Player player, Location location) {
      try {
         Plugin goldSpawnerPlugin = this.getServer().getPluginManager().getPlugin("HolyGoldSpawner");
         if (goldSpawnerPlugin != null && goldSpawnerPlugin.isEnabled()) {
            HolyGoldSpawner goldSpawner = (HolyGoldSpawner)goldSpawnerPlugin;
            int currentDurability = goldSpawner.getSpawnerManager().getSpawnerDurability(location);
            int newDurability = currentDurability / 2;
            goldSpawner.getSpawnerManager().removeSpawner(location);
            if (newDurability > 0) {
               return goldSpawner.getItemManager().createGoldenSpawner(newDurability);
            }

            player.sendMessage("§x§f§f§0§0§0§0▶ §fЗолотой спавнер полностью сломался");
            return null;
         }
      } catch (Exception var7) {
      }

      return null;
   }

   private void breakJakePickaxe(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (this.isJakePickaxe(mainHand)) {
         if (mainHand.getAmount() > 1) {
            mainHand.setAmount(mainHand.getAmount() - 1);
         } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
         }
      } else if (this.isJakePickaxe(offHand)) {
         if (offHand.getAmount() > 1) {
            offHand.setAmount(offHand.getAmount() - 1);
         } else {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
         }
      }
   }

   public void handleVanillaEggOnSpawner(Player player, Block spawnerBlock, ItemStack eggItem) {
      CreatureSpawner spawner = (CreatureSpawner)spawnerBlock.getState();
      EntityType entityType = this.getEntityTypeFromEgg(eggItem.getType());
      if (entityType != null) {
         Location location = spawnerBlock.getLocation();
         if (this.activeSpawners.containsKey(location)) {
            this.activeSpawners.remove(location);
            this.database.removeActiveSpawner(location);
         }

         spawner.setSpawnedType(entityType);
         spawner.update(false);
         if (player.getGameMode() != GameMode.CREATIVE) {
            this.reduceItemAmount(player, eggItem);
         }
      }
   }

   private EntityType getEntityTypeFromEgg(Material eggMaterial) {
      switch (eggMaterial) {
         case BAT_SPAWN_EGG:
            return EntityType.BAT;
         case BEE_SPAWN_EGG:
            return EntityType.BEE;
         case BLAZE_SPAWN_EGG:
            return EntityType.BLAZE;
         case CAT_SPAWN_EGG:
            return EntityType.CAT;
         case CAVE_SPIDER_SPAWN_EGG:
            return EntityType.CAVE_SPIDER;
         case CHICKEN_SPAWN_EGG:
            return EntityType.CHICKEN;
         case COD_SPAWN_EGG:
            return EntityType.COD;
         case COW_SPAWN_EGG:
            return EntityType.COW;
         case CREEPER_SPAWN_EGG:
            return EntityType.CREEPER;
         case DOLPHIN_SPAWN_EGG:
            return EntityType.DOLPHIN;
         case DONKEY_SPAWN_EGG:
            return EntityType.DONKEY;
         case DROWNED_SPAWN_EGG:
            return EntityType.DROWNED;
         case ELDER_GUARDIAN_SPAWN_EGG:
            return EntityType.ELDER_GUARDIAN;
         case ENDERMAN_SPAWN_EGG:
            return EntityType.ENDERMAN;
         case ENDERMITE_SPAWN_EGG:
            return EntityType.ENDERMITE;
         case EVOKER_SPAWN_EGG:
            return EntityType.EVOKER;
         case FOX_SPAWN_EGG:
            return EntityType.FOX;
         case GHAST_SPAWN_EGG:
            return EntityType.GHAST;
         case GUARDIAN_SPAWN_EGG:
            return EntityType.GUARDIAN;
         case HOGLIN_SPAWN_EGG:
            return EntityType.HOGLIN;
         case HORSE_SPAWN_EGG:
            return EntityType.HORSE;
         case HUSK_SPAWN_EGG:
            return EntityType.HUSK;
         case LLAMA_SPAWN_EGG:
            return EntityType.LLAMA;
         case MAGMA_CUBE_SPAWN_EGG:
            return EntityType.MAGMA_CUBE;
         case MOOSHROOM_SPAWN_EGG:
            return EntityType.MOOSHROOM;
         case MULE_SPAWN_EGG:
            return EntityType.MULE;
         case OCELOT_SPAWN_EGG:
            return EntityType.OCELOT;
         case PANDA_SPAWN_EGG:
            return EntityType.PANDA;
         case PARROT_SPAWN_EGG:
            return EntityType.PARROT;
         case PHANTOM_SPAWN_EGG:
            return EntityType.PHANTOM;
         case PIG_SPAWN_EGG:
            return EntityType.PIG;
         case PIGLIN_SPAWN_EGG:
            return EntityType.PIGLIN;
         case PILLAGER_SPAWN_EGG:
            return EntityType.PILLAGER;
         case POLAR_BEAR_SPAWN_EGG:
            return EntityType.POLAR_BEAR;
         case PUFFERFISH_SPAWN_EGG:
            return EntityType.PUFFERFISH;
         case RABBIT_SPAWN_EGG:
            return EntityType.RABBIT;
         case RAVAGER_SPAWN_EGG:
            return EntityType.RAVAGER;
         case SALMON_SPAWN_EGG:
            return EntityType.SALMON;
         case SHEEP_SPAWN_EGG:
            return EntityType.SHEEP;
         case SHULKER_SPAWN_EGG:
            return EntityType.SHULKER;
         case SILVERFISH_SPAWN_EGG:
            return EntityType.SILVERFISH;
         case SKELETON_SPAWN_EGG:
            return EntityType.SKELETON;
         case SKELETON_HORSE_SPAWN_EGG:
            return EntityType.SKELETON_HORSE;
         case SLIME_SPAWN_EGG:
            return EntityType.SLIME;
         case SPIDER_SPAWN_EGG:
            return EntityType.SPIDER;
         case SQUID_SPAWN_EGG:
            return EntityType.SQUID;
         case STRAY_SPAWN_EGG:
            return EntityType.STRAY;
         case STRIDER_SPAWN_EGG:
            return EntityType.STRIDER;
         case TRADER_LLAMA_SPAWN_EGG:
            return EntityType.TRADER_LLAMA;
         case TROPICAL_FISH_SPAWN_EGG:
            return EntityType.TROPICAL_FISH;
         case TURTLE_SPAWN_EGG:
            return EntityType.TURTLE;
         case VEX_SPAWN_EGG:
            return EntityType.VEX;
         case VILLAGER_SPAWN_EGG:
            return EntityType.VILLAGER;
         case VINDICATOR_SPAWN_EGG:
            return EntityType.VINDICATOR;
         case WANDERING_TRADER_SPAWN_EGG:
            return EntityType.WANDERING_TRADER;
         case WITCH_SPAWN_EGG:
            return EntityType.WITCH;
         case WITHER_SKELETON_SPAWN_EGG:
            return EntityType.WITHER_SKELETON;
         case WOLF_SPAWN_EGG:
            return EntityType.WOLF;
         case ZOGLIN_SPAWN_EGG:
            return EntityType.ZOGLIN;
         case ZOMBIE_SPAWN_EGG:
            return EntityType.ZOMBIE;
         case ZOMBIE_HORSE_SPAWN_EGG:
            return EntityType.ZOMBIE_HORSE;
         case ZOMBIE_VILLAGER_SPAWN_EGG:
            return EntityType.ZOMBIE_VILLAGER;
         case ZOMBIFIED_PIGLIN_SPAWN_EGG:
            return EntityType.ZOMBIFIED_PIGLIN;
         default:
            return null;
      }
   }

   private void reduceItemAmount(Player player, ItemStack item) {
      if (item.getAmount() > 1) {
         item.setAmount(item.getAmount() - 1);
      } else {
         player.getInventory().removeItem(new ItemStack[]{item});
      }
   }

   public Map<Location, HolyCustomEgg.ActiveSpawner> getActiveSpawners() {
      return this.activeSpawners;
   }

   public Map<String, ManagerEgg> getEggs() {
      return this.eggs;
   }

   public ItemStack getJakePickaxe() {
      return this.jakePickaxe.clone();
   }

   public ItemStack getMysterySpawnerFromBlock(Block block) {
      if (block.getType() != Material.SPAWNER) {
         return null;
      } else {
         Location location = block.getLocation();
         if (this.activeSpawners.containsKey(location)) {
            HolyCustomEgg.ActiveSpawner activeSpawner = this.activeSpawners.get(location);
            return activeSpawner.getEgg().getSpawner();
         } else {
            ItemStack tempSpawner = new ItemStack(Material.SPAWNER);
            BlockStateMeta meta = (BlockStateMeta)tempSpawner.getItemMeta();
            if (meta != null) {
               CreatureSpawner spawnerMeta = (CreatureSpawner)meta.getBlockState();
               spawnerMeta.setSpawnedType(((CreatureSpawner)block.getState()).getSpawnedType());
               meta.setBlockState(spawnerMeta);
               tempSpawner.setItemMeta(meta);
               ManagerEgg egg = Utils.findEggBySpawnerNBT(tempSpawner, this.eggs, this);
               if (egg != null) {
                  return egg.getSpawner();
               }
            }

            return null;
         }
      }
   }

   public boolean isMysterySpawner(Location location) {
      return this.activeSpawners.containsKey(location);
   }

   public ManagerEgg getMysterySpawnerEgg(Location location) {
      HolyCustomEgg.ActiveSpawner activeSpawner = this.activeSpawners.get(location);
      return activeSpawner != null ? activeSpawner.getEgg() : null;
   }

   public void removeMysterySpawner(Location location) {
      if (this.activeSpawners.containsKey(location)) {
         this.activeSpawners.remove(location);
         this.database.removeActiveSpawner(location);
      }
   }

   public static HolyCustomEgg getPluginInstance() {
      return instance;
   }

   public class ActiveSpawner {
      private Location location;
      private ManagerEgg egg;
      private long lastChangeTime;

      public ActiveSpawner(Location location, ManagerEgg egg, long lastChangeTime) {
         this.location = location;
         this.egg = egg;
         this.lastChangeTime = lastChangeTime;
      }

      public Location getLocation() {
         return this.location;
      }

      public ManagerEgg getEgg() {
         return this.egg;
      }

      public long getLastChangeTime() {
         return this.lastChangeTime;
      }

      public void setLastChangeTime(long lastChangeTime) {
         this.lastChangeTime = lastChangeTime;
      }
   }
}
