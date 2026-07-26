package ru.lolodimo4ka.holygoldspawner;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holycustomegg.HolyCustomEgg;
import ru.lolodimo4ka.holygoldspawner.commands.SpawnerCommand;
import ru.lolodimo4ka.holygoldspawner.database.Database;
import ru.lolodimo4ka.holygoldspawner.events.Config;
import ru.lolodimo4ka.holygoldspawner.events.SpawnerCraft;
import ru.lolodimo4ka.holygoldspawner.managers.MessagesManager;
import ru.lolodimo4ka.holygoldspawner.managers.SpawnerManager;
import ru.lolodimo4ka.holygoldspawner.protection.WorldGuardManager;
import ru.lolodimo4ka.holygoldspawner.utils.Item;
import ru.lolodimo4ka.holygoldspawner.utils.Sound;

public class HolyGoldSpawner extends JavaPlugin implements Listener, CommandExecutor {
   private Config configManager;
   private MessagesManager messagesManager;
   private Item itemManager;
   private Sound soundManager;
   private SpawnerManager spawnerManager;
   private SpawnerCommand commandManager;
   private WorldGuardManager worldGuardManager;
   private Economy economy;
   private HolyCustomEgg customEggPlugin;
   private boolean customEggEnabled;
   private Database database;

   public HolyGoldSpawner() {
   }

   public void onEnable() {
      this.configManager = new Config(this);
      this.messagesManager = new MessagesManager(this);
      this.worldGuardManager = new WorldGuardManager(this);
      this.setupCustomEggIntegration();
      if (!this.setupEconomy()) {
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.database = new Database(this);
         this.database.initialize();
         this.itemManager = new Item(this, this.configManager);
         this.soundManager = new Sound(this, this.configManager);
         this.spawnerManager = new SpawnerManager(this, this.configManager, this.itemManager, this.messagesManager, this.database);
         this.commandManager = new SpawnerCommand(this, this.itemManager);
         SpawnerCraft craftManager = new SpawnerCraft(this, this.configManager, this.itemManager);
         this.getServer().getPluginManager().registerEvents(craftManager, this);
         this.getServer().getPluginManager().registerEvents(this, this);
         Bukkit.getScheduler().runTaskLater(this, () -> this.spawnerManager.loadData(), 60L);
      }
   }

   public void onDisable() {
      if (this.spawnerManager != null) {
         this.spawnerManager.saveDataSync();
      }

      if (this.database != null) {
         this.database.closeConnection();
      }
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         this.getLogger().severe("Плагин Vault не найден");
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            this.getLogger().severe("Не удалось подключиться к экономике через Vault");
            return false;
         } else {
            this.economy = (Economy)rsp.getProvider();
            this.getLogger().info("Vault подключена");
            return this.economy != null;
         }
      }
   }

   private void setupCustomEggIntegration() {
      this.customEggPlugin = (HolyCustomEgg)this.getServer().getPluginManager().getPlugin("HolyCustomEgg");
      this.customEggEnabled = this.customEggPlugin != null && this.customEggPlugin.isEnabled();
      if (!this.customEggEnabled) {
         Bukkit.getScheduler().runTaskLater(this, () -> {
            this.customEggPlugin = (HolyCustomEgg)this.getServer().getPluginManager().getPlugin("HolyCustomEgg");
            this.customEggEnabled = this.customEggPlugin != null && this.customEggPlugin.isEnabled();
         }, 40L);
      }
   }

   public boolean isJakePickaxe(ItemStack item) {
      if (this.customEggEnabled && item != null) {
         try {
            return this.customEggPlugin.isJakePickaxe(item);
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block clickedBlock = event.getClickedBlock();
         if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
            Location location = clickedBlock.getLocation();
            if (this.spawnerManager.containsSpawner(location)) {
               Player player = event.getPlayer();
               ItemStack item = event.getItem();
               if (item != null) {
                  String itemType = item.getType().name();
                  boolean isEgg = itemType.toLowerCase().endsWith("_spawn_egg");
                  if (isEgg) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      return this.commandManager.onCommand(sender, command, label, args);
   }

   public boolean isEconomyEnabled() {
      return this.economy != null;
   }

   public void giveRandomCoins(Player player, double penalty, double minCoins, double maxCoins) {
      if (this.economy == null) {
         this.getLogger().warning("Vault экономика не найдена, монет не будет");
      } else {
         double coins = ThreadLocalRandom.current().nextDouble(minCoins, maxCoins + 1.0E-4);
         coins = Math.floor(coins * 100.0) / 100.0;
         if (penalty > 0.0) {
            double penaltyMultiplier = 1.0 - penalty;
            coins *= penaltyMultiplier;
            coins = Math.floor(coins * 100.0) / 100.0;
         }

         double finalCoins = coins;
         Bukkit.getScheduler().runTask(this, () -> {
            this.economy.depositPlayer(player, finalCoins);
            if (this.configManager.isShowMessages()) {
               double currentBalance = this.economy.getBalance(player);
               DecimalFormat decimalFormat = this.configManager.getDecimalFormat();
               String formattedCoins = decimalFormat.format(finalCoins);
               String formattedBalance = decimalFormat.format(currentBalance);
               String message = this.messagesManager.coinReceivedMessage.replace("%coins%", formattedCoins).replace("%balance%", formattedBalance);
               player.sendMessage(message);
            }
         });
      }
   }

   public Economy getEconomy() {
      return this.economy;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
         ItemStack item = event.getItemInHand();
         if (this.itemManager.isGoldenSpawner(item)) {
            Location location = event.getBlock().getLocation();
            Player player = event.getPlayer();
            if (!location.getWorld().getName().equals("world")) {
               event.setCancelled(true);
               player.sendMessage(this.messagesManager.wrongWorldMessage);
               return;
            }

            if (!this.configManager.isSpawnerPlacementEnabled()) {
               event.setCancelled(true);
               player.sendMessage(this.messagesManager.placementDisabledMessage);
               return;
            }

            if (!this.canBuild(player, location)) {
               event.setCancelled(true);
               return;
            }

            int durability = this.itemManager.getSpawnerDurability(item);
            if (!this.spawnerManager.addSpawner(location, player.getUniqueId(), durability, player)) {
               event.setCancelled(true);
               return;
            }

            Bukkit.getScheduler()
               .runTaskLater(
                  this,
                  () -> {
                     if (location.getBlock().getType() == Material.SPAWNER) {
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                           this.soundManager.playPlacementSound(onlinePlayer);
                           this.soundManager.playWitherSound(onlinePlayer);
                        }

                        if (this.configManager.isCoordsEncryptionEnabled() && !this.messagesManager.coordsMessageFormat.isEmpty()) {
                           int x = location.getBlockX();
                           int y = location.getBlockY();
                           int z = location.getBlockZ();
                           String encryptedX = this.spawnerManager.maskCoordinates(x, "x", false);
                           String encryptedZ = this.spawnerManager.maskCoordinates(z, "z", false);
                           String plainY = String.valueOf(y);
                           String message = this.messagesManager
                              .coordsMessageFormat
                              .replace("%x_mask%", encryptedX)
                              .replace("%x_full%", String.valueOf(x))
                              .replace("%y_mask%", plainY)
                              .replace("%y_full%", plainY)
                              .replace("%z_mask%", encryptedZ)
                              .replace("%z_full%", String.valueOf(z));

                           for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                              onlinePlayer.sendMessage(message);
                           }
                        }
                     }
                  },
                  1L
               );
         }
      }
   }

   private boolean canBuild(Player player, Location location) {
      return this.worldGuardManager.canPlaceSpawner(player, location);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      if (this.spawnerManager.containsSpawner(location)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onSpawnerSpawn(SpawnerSpawnEvent event) {
      Location spawnerLocation = event.getSpawner().getLocation();
      if (this.spawnerManager.containsSpawner(spawnerLocation)) {
         if (this.configManager.isPreventMobSpawning()) {
            event.setCancelled(true);
         }

         if (event.getSpawner() instanceof CreatureSpawner) {
            this.spawnerManager.resetSpawnerTimer(event.getSpawner());
         }
      }
   }

   public Config getConfigManager() {
      return this.configManager;
   }

   public MessagesManager getMessagesManager() {
      return this.messagesManager;
   }

   public Item getItemManager() {
      return this.itemManager;
   }

   public Sound getSoundManager() {
      return this.soundManager;
   }

   public SpawnerManager getSpawnerManager() {
      return this.spawnerManager;
   }

   public WorldGuardManager getWorldGuardManager() {
      return this.worldGuardManager;
   }

   public boolean isCustomEggEnabled() {
      return this.customEggEnabled;
   }

   public HolyCustomEgg getCustomEggPlugin() {
      return this.customEggPlugin;
   }

   public static HolyGoldSpawner getInstance() {
      return (HolyGoldSpawner)getPlugin(HolyGoldSpawner.class);
   }

   public ItemStack createSpawnerWithFichaSpawnersData(EntityType entityType) {
      Plugin fichaSpawnersPlugin = this.getServer().getPluginManager().getPlugin("FichaSpawners");
      if (fichaSpawnersPlugin != null) {
         try {
            Class<?> fichaSpawnersClass = fichaSpawnersPlugin.getClass();
            Method getInstanceMethod = fichaSpawnersClass.getMethod("getInstance");
            Object fichaSpawnersInstance = getInstanceMethod.invoke(null);
            Method getConfigManagerMethod = fichaSpawnersClass.getMethod("getConfigManager");
            Object configManager = getConfigManagerMethod.invoke(fichaSpawnersInstance);
            Class<?> configManagerClass = configManager.getClass();
            Method getSpawnerNameMethod = configManagerClass.getMethod("getSpawnerName", String.class);
            Method getSpawnerLoreMethod = configManagerClass.getMethod("getSpawnerLore", String.class);
            String entityName = entityType.name();
            String name = (String)getSpawnerNameMethod.invoke(configManager, entityName);
            List<String> lore = (List<String>)getSpawnerLoreMethod.invoke(configManager, entityName);
            return this.createSpawnerItemWithCustomData(entityType, name, lore);
         } catch (Exception var14) {
            this.getLogger().warning("Failed to use FichaSpawners: " + var14.getMessage());
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
}
