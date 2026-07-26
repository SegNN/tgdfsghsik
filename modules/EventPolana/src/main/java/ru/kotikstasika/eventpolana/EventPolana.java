package ru.kotikstasika.eventpolana;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.eventpolana.command.PolanaCommand;
import ru.kotikstasika.eventpolana.config.PluginConfig;
import ru.kotikstasika.eventpolana.listener.PolanaListeners;
import ru.kotikstasika.eventpolana.manager.FlowerManager;
import ru.kotikstasika.eventpolana.manager.LootManagerBridge;
import ru.kotikstasika.eventpolana.manager.PolanaManager;
import ru.kotikstasika.eventpolana.manager.RegionBarrierCleaner;
import ru.kotikstasika.eventpolana.model.PolanaRarity;
import ru.kotikstasika.eventpolana.model.PolanaState;
import ru.kotikstasika.eventpolana.service.HologramService;
import ru.kotikstasika.eventpolana.service.SchematicService;
import ru.kotikstasika.eventpolana.service.ShearsService;
import ru.kotikstasika.eventpolana.service.WorldGuardService;

public final class EventPolana extends JavaPlugin {
   private static EventPolana plugin;
   private PluginConfig cfg;
   private ShearsService shearsService;
   private HologramService hologramService;
   private SchematicService schematicService;
   private WorldGuardService wgService;
   private FlowerManager flowerManager;
   private RegionBarrierCleaner barrierCleaner;
   private PolanaManager polanaManager;
   private final Random random = new Random();

   public EventPolana() {
   }

   public void onEnable() {
      plugin = this;
      this.saveDefaultConfig();
      this.cfg = new PluginConfig(this);
      this.shearsService = new ShearsService(this);
      this.hologramService = new HologramService();
      this.schematicService = new SchematicService();
      this.wgService = new WorldGuardService();
      this.flowerManager = new FlowerManager(this);
      this.barrierCleaner = new RegionBarrierCleaner(this);
      this.polanaManager = new PolanaManager(
         this, this.cfg, this.schematicService, this.wgService, this.flowerManager, this.barrierCleaner, this.hologramService
      );
      this.getCommand("polana").setExecutor(new PolanaCommand(this));
      this.getCommand("polana").setTabCompleter(new PolanaCommand(this));
      this.getServer()
         .getPluginManager()
         .registerEvents(new PolanaListeners(this, this.cfg, this.shearsService, new LootManagerBridge(this.cfg), this.polanaManager, this.flowerManager), this);
   }

   public void onDisable() {
      this.removePolana();
   }

   public void spawnPolana(PolanaRarity rarity) {
      this.polanaManager.spawnPolana(rarity);
   }

   public void removePolana() {
      this.polanaManager.removePolana();
   }

   public PolanaState getState() {
      return this.polanaManager.getState();
   }

   public Location getPolanaLocation() {
      return this.polanaManager.getPolanaLocation();
   }

   public PolanaRarity getRarity() {
      return this.polanaManager.getRarity();
   }

   public boolean isSearchingLocation() {
      return this.polanaManager.isSearchingLocation();
   }

   public PolanaManager getPolanaManager() {
      return this.polanaManager;
   }

   public void giveShears(CommandSender sender, String playerName) {
      Player target = this.getServer().getPlayer(playerName);
      if (target == null) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&l▶ &fИгрок &6" + playerName + "&f не найден!"));
      } else {
         ItemStack shears = this.shearsService.createCustomShears();
         target.getInventory().addItem(new ItemStack[]{shears});
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l▶ &fВы успешно выдали ножницы игроку &6" + target.getName() + "&f!"));
      }
   }
}
