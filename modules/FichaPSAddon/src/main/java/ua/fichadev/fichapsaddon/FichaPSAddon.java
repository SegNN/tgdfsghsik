package ua.fichadev.fichapsaddon;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.event.PSRemoveEvent;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class FichaPSAddon extends JavaPlugin implements Listener {
   private FileConfiguration config;
   private FileConfiguration dataConfig;
   private File dataFile;
   private Map<String, Hologram> holograms = new HashMap<>();
   private Map<String, Long> raidTaxExpiry = new HashMap<>();
   private Map<Player, String> pendingUpgrades = new HashMap<>();
   private boolean firstJoin = false;

   public FichaPSAddon() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.config = this.getConfig();
      this.dataFile = new File(this.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         this.saveResource("data.yml", false);
      }

      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.firstJoin = false;
      this.loadRaidTaxData();
      Bukkit.getScheduler().runTaskLater(this, () -> this.restoreHolograms(), 20L);
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      if (!this.firstJoin) {
         Bukkit.getScheduler().runTaskLater(this, () -> this.restoreHolograms(), 40L);
         this.firstJoin = true;
      }
   }

   public void onDisable() {
      for (Hologram hologram : this.holograms.values()) {
         hologram.delete();
      }

      this.holograms.clear();
      this.saveRaidTaxData();

      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!command.getName().equalsIgnoreCase("ps") || args.length <= 0 || !args[0].equalsIgnoreCase("upgrade")) {
         return false;
      } else if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cЭту команду может использовать только игрок"));
         return true;
      } else {
         Player player = (Player)sender;
         PSRegion region = PSRegion.fromLocation(player.getLocation());
         if (region == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&f&0&0&0&0▶ &fВы не находитесь в регионе"));
            return true;
         } else {
            String regionType = region.getType();
            if (!regionType.equals("NETHERITE_BLOCK") && !regionType.equals("ANCIENT_DEBRIS")) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&f&0&0&0&0▶ &fВы не можете улучшить этот регион в данный момент"));
               return true;
            } else {
               this.openUpgradeMenu(player, region);
               return true;
            }
         }
      }
   }

   private void loadRaidTaxData() {
      if (this.dataConfig.contains("raidTax")) {
         for (String regionId : this.dataConfig.getConfigurationSection("raidTax").getKeys(false)) {
            long expiry = this.dataConfig.getLong("raidTax." + regionId);
            this.raidTaxExpiry.put(regionId, expiry);
         }
      }
   }

   private void saveRaidTaxData() {
      this.dataConfig.set("raidTax", null);

      for (Entry<String, Long> entry : this.raidTaxExpiry.entrySet()) {
         this.dataConfig.set("raidTax." + entry.getKey(), entry.getValue());
      }
   }

   public void activateRaidTax(Location location) {
      PSRegion region = PSRegion.fromLocation(location);
      if (region != null) {
         String regionType = region.getType();
         if (regionType.equals("NETHERITE_BLOCK") || regionType.equals("ANCIENT_DEBRIS")) {
            long expiryTime = System.currentTimeMillis() + 10800000L;
            this.raidTaxExpiry.put(region.getId(), expiryTime);
            this.saveRaidTaxData();
         }
      }
   }

   private boolean hasRaidTax(String regionId) {
      if (!this.raidTaxExpiry.containsKey(regionId)) {
         return false;
      } else {
         long expiryTime = this.raidTaxExpiry.get(regionId);
         if (System.currentTimeMillis() >= expiryTime) {
            this.raidTaxExpiry.remove(regionId);
            this.saveRaidTaxData();
            return false;
         } else {
            return true;
         }
      }
   }

   private String getRaidTaxTimeLeft(String regionId) {
      if (!this.raidTaxExpiry.containsKey(regionId)) {
         return "";
      } else {
         long expiryTime = this.raidTaxExpiry.get(regionId);
         long timeLeft = expiryTime - System.currentTimeMillis();
         if (timeLeft <= 0L) {
            return "";
         } else {
            long hours = timeLeft / 3600000L;
            long minutes = timeLeft % 3600000L / 60000L;
            long seconds = timeLeft % 60000L / 1000L;
            return hours + " ч., " + minutes + " м., " + seconds + " сек.";
         }
      }
   }

   private int getUpgradeCost(int targetDurability, int currentDurability, boolean hasRaidTax) {
      int durabilityDifference = targetDurability - currentDurability;
      int baseCost = durabilityDifference * 9;
      return hasRaidTax ? baseCost * 2 : baseCost;
   }

   private void openUpgradeMenu(Player player, PSRegion region) {
      String regionId = region.getId();
      int currentDurability = this.dataConfig.getInt("regions." + regionId + ".durability", this.getStartDurability(region.getType()));
      int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability", currentDurability);
      boolean hasTax = this.hasRaidTax(regionId);
      Inventory menu = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&0Прочность региона"));

      for (int i = 0; i < 9; i++) {
         menu.setItem(i, this.createGlassPane(Material.GRAY_STAINED_GLASS_PANE));
      }

      for (int i = 18; i < 27; i++) {
         menu.setItem(i, this.createGlassPane(Material.GRAY_STAINED_GLASS_PANE));
      }

      menu.setItem(9, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      menu.setItem(11, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      menu.setItem(17, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      ItemStack regionInfo = new ItemStack(Material.NETHERITE_BLOCK);
      ItemMeta infoMeta = regionInfo.getItemMeta();
      infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&f"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(this.translateHexColors("&r &#21BEE9Информация о регионе:&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffПрочность: &#21BEE9" + currentDurability + "&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffУчастников: &#21BEE9" + (region.getMembers().size() + 1) + "&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffНалог: " + (hasTax ? "&#aa0000Есть" : "&#55ff55Нет") + "&r"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
      infoMeta.setLore(infoLore);
      regionInfo.setItemMeta(infoMeta);
      menu.setItem(10, regionInfo);
      if (hasTax) {
         String timeLeft = this.getRaidTaxTimeLeft(regionId);
         player.sendMessage(
            ChatColor.translateAlternateColorCodes(
               '&', "&x&f&f&0&0&0&0▶ &fТекущий регион имеет повышенный налог на улучшение, т.к. находится в зоне рейда. Налог спадёт через " + timeLeft
            )
         );
      }

      int maxUpgradeSlots = 5;

      for (int i = 0; i < Math.min(maxUpgradeSlots, 64 - currentDurability); i++) {
         int targetDurability = currentDurability + i + 1;
         if (targetDurability > 64) {
            break;
         }

         int cost = this.getUpgradeCost(targetDurability, currentDurability, hasTax);
         ItemStack upgradeItem = new ItemStack(Material.NETHERITE_INGOT, Math.min(targetDurability, 64));
         ItemMeta upgradeMeta = upgradeItem.getItemMeta();
         upgradeMeta.setDisplayName(this.translateHexColors("&r &#ff7000Улучшить до &l&#ff7000" + targetDurability + " &#ff7000прочности&r"));
         List<String> upgradeLore = new ArrayList<>();
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeLore.add(
            this.translateHexColors("&r  &#ff7000• &#ffffffПрочность: &#aaaaaa&m" + currentDurability + " &#ffffff → &l&#ff7000" + targetDurability + "&r")
         );
         upgradeLore.add(this.translateHexColors("&r  &#ff7000• &#ffffffНалог: " + (hasTax ? "&#aa0000Есть" : "&#55ff55Нет") + "&r"));
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeLore.add(this.translateHexColors("&r &#ff7000▶ &#ffffffНажмите, чтобы улучшить&r"));
         upgradeLore.add(this.translateHexColors("&r    &#ffffffза &#fb0fd4" + cost + " незеритовых слитков&r"));
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeMeta.setLore(upgradeLore);
         upgradeItem.setItemMeta(upgradeMeta);
         menu.setItem(12 + i, upgradeItem);
      }

      player.openInventory(menu);
   }

   private void openConfirmationMenu(Player player, PSRegion region, int targetDurability, int slot) {
      String regionId = region.getId();
      int currentDurability = this.dataConfig.getInt("regions." + regionId + ".durability", this.getStartDurability(region.getType()));
      int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability", currentDurability);
      boolean hasTax = this.hasRaidTax(regionId);
      int cost = this.getUpgradeCost(targetDurability, currentDurability, hasTax);
      Inventory menu = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&0Прочность региона"));

      for (int i = 0; i < 9; i++) {
         menu.setItem(i, this.createGlassPane(Material.GRAY_STAINED_GLASS_PANE));
      }

      for (int i = 18; i < 27; i++) {
         menu.setItem(i, this.createGlassPane(Material.GRAY_STAINED_GLASS_PANE));
      }

      menu.setItem(9, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      menu.setItem(11, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      menu.setItem(17, this.createGlassPane(Material.BLUE_STAINED_GLASS_PANE));
      ItemStack regionInfo = new ItemStack(Material.NETHERITE_BLOCK);
      ItemMeta infoMeta = regionInfo.getItemMeta();
      infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&f"));
      List<String> infoLore = new ArrayList<>();
      infoLore.add(this.translateHexColors("&r &#21BEE9Информация о регионе:&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffПрочность: &#21BEE9" + currentDurability + "&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffУчастников: &#21BEE9" + (region.getMembers().size() + 1) + "&r"));
      infoLore.add(this.translateHexColors("&r  &#21BEE9• &#ffffffНалог: " + (hasTax ? "&#aa0000Есть" : "&#55ff55Нет") + "&r"));
      infoLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
      infoMeta.setLore(infoLore);
      regionInfo.setItemMeta(infoMeta);
      menu.setItem(10, regionInfo);
      int maxUpgradeSlots = 5;

      for (int i = 0; i < Math.min(maxUpgradeSlots, 64 - currentDurability); i++) {
         int targetDur = currentDurability + i + 1;
         if (targetDur > 64) {
            break;
         }

         int itemCost = this.getUpgradeCost(targetDur, currentDurability, hasTax);
         ItemStack upgradeItem = new ItemStack(Material.NETHERITE_INGOT, Math.min(targetDur, 64));
         ItemMeta upgradeMeta = upgradeItem.getItemMeta();
         upgradeMeta.setDisplayName(this.translateHexColors("&r &#ff7000Улучшить до &l&#ff7000" + targetDur + " &#ff7000прочности&r"));
         List<String> upgradeLore = new ArrayList<>();
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeLore.add(
            this.translateHexColors("&r  &#ff7000• &#ffffffПрочность: &#aaaaaa&m" + currentDurability + " &#ffffff → &l&#ff7000" + targetDur + "&r")
         );
         upgradeLore.add(this.translateHexColors("&r  &#ff7000• &#ffffffНалог: " + (hasTax ? "&#aa0000Есть" : "&#55ff55Нет") + "&r"));
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeLore.add(this.translateHexColors("&r &#ff7000▶ &#ffffffНажмите, чтобы улучшить&r"));
         upgradeLore.add(this.translateHexColors("&r    &#ffffffза &#fb0fd4" + itemCost + " незеритовых слитков&r"));
         upgradeLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
         upgradeMeta.setLore(upgradeLore);
         upgradeItem.setItemMeta(upgradeMeta);
         menu.setItem(12 + i, upgradeItem);
      }

      ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
      ItemMeta confirmMeta = confirmItem.getItemMeta();
      confirmMeta.setDisplayName(this.translateHexColors("&r &#ff7000Улучшить до &l&#ff7000" + targetDurability + " &#ff7000прочности&r"));
      List<String> confirmLore = new ArrayList<>();
      confirmLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
      confirmLore.add(this.translateHexColors("&r  &#ffffffВы уверены, что готовы потратить&r"));
      confirmLore.add(this.translateHexColors("&r  &#fb0fd4" + cost + " незеритовых слитков&#ffffff?&r"));
      confirmLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
      confirmLore.add(this.translateHexColors("&r &#ff7000▶ &#ffffffНажмите, чтобы подтвердить&r"));
      confirmLore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
      confirmMeta.setLore(confirmLore);
      confirmItem.setItemMeta(confirmMeta);
      menu.setItem(slot, confirmItem);
      this.pendingUpgrades.put(player, regionId + ":" + targetDurability + ":" + slot);
      player.openInventory(menu);
   }

   private String translateHexColors(String message) {
      message = ChatColor.translateAlternateColorCodes('&', message);
      Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
      Matcher matcher = hexPattern.matcher(message);
      StringBuffer buffer = new StringBuffer(message.length() + 32);

      while (matcher.find()) {
         String group = matcher.group(1);
         matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + group).toString());
      }

      return matcher.appendTail(buffer).toString();
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         String title = event.getView().getTitle();
         if (title.equals(ChatColor.translateAlternateColorCodes('&', "&0Прочность региона"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
               PSRegion region = PSRegion.fromLocation(player.getLocation());
               if (region == null) {
                  player.closeInventory();
               } else {
                  ItemStack clicked = event.getCurrentItem();
                  if (clicked.getType() == Material.NETHERITE_INGOT) {
                     String displayName = clicked.getItemMeta().getDisplayName();
                     String cleanName = ChatColor.stripColor(displayName);
                     if (cleanName.contains("Улучшить до") && cleanName.contains("прочности")) {
                        try {
                           String[] parts = cleanName.split(" ");

                           for (int i = 0; i < parts.length; i++) {
                              if (parts[i].equals("до") && i + 1 < parts.length) {
                                 int targetDurability = Integer.parseInt(parts[i + 1]);
                                 this.openConfirmationMenu(player, region, targetDurability, event.getSlot());
                                 return;
                              }
                           }
                        } catch (NumberFormatException var19) {
                           var19.printStackTrace();
                        }
                     }
                  } else if (clicked.getType() == Material.LIME_STAINED_GLASS_PANE && this.pendingUpgrades.containsKey(player)) {
                     String data = this.pendingUpgrades.get(player);
                     String[] parts = data.split(":");
                     String regionId = parts[0];
                     int targetDurability = Integer.parseInt(parts[1]);
                     if (!region.getId().equals(regionId)) {
                        player.closeInventory();
                        this.pendingUpgrades.remove(player);
                        return;
                     }

                     int currentDurability = this.dataConfig.getInt("regions." + regionId + ".durability", this.getStartDurability(region.getType()));
                     int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability", currentDurability);
                     boolean hasTax = this.hasRaidTax(regionId);
                     int cost = this.getUpgradeCost(targetDurability, currentDurability, hasTax);
                     int netheritCount = 0;

                     for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.NETHERITE_INGOT) {
                           netheritCount += item.getAmount();
                        }
                     }

                     if (netheritCount < cost) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&f&0&0&0&0▶ &fУ вас недостаточно незеритовых слитков"));
                        player.closeInventory();
                        this.pendingUpgrades.remove(player);
                        return;
                     }

                     int toRemove = cost;

                     for (int ix = 0; ix < player.getInventory().getSize(); ix++) {
                        ItemStack itemx = player.getInventory().getItem(ix);
                        if (itemx != null && itemx.getType() == Material.NETHERITE_INGOT) {
                           int amount = itemx.getAmount();
                           if (amount <= toRemove) {
                              toRemove -= amount;
                              player.getInventory().setItem(ix, null);
                           } else {
                              itemx.setAmount(amount - toRemove);
                              toRemove = 0;
                           }

                           if (toRemove == 0) {
                              break;
                           }
                        }
                     }

                     int newMaxDurability = Math.max(maxDurability, targetDurability);
                     this.dataConfig.set("regions." + regionId + ".durability", targetDurability);
                     this.dataConfig.set("regions." + regionId + ".maxDurability", newMaxDurability);
                     this.saveData();
                     Hologram hologram = this.holograms.get(regionId);
                     if (hologram != null) {
                        this.updateHologramLines(
                           hologram, region.getType(), this.dataConfig.getString("regions." + regionId + ".owner"), targetDurability, newMaxDurability
                        );
                     }

                     player.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                           '&',
                           "&x&0&0&d&8&f&f ▶ &fВы успешно вложили &x&0&0&d&8&f&f"
                              + cost
                              + " незеритовых слитков &fдля повышения прочности региона до &x&0&0&d&8&f&f"
                              + targetDurability
                              + " уровн&f."
                        )
                     );
                     player.closeInventory();
                     this.pendingUpgrades.remove(player);
                     this.openUpgradeMenu(player, region);
                  }
               }
            }
         }
      }
   }

   private ItemStack createGlassPane(Material material) {
      ItemStack pane = new ItemStack(material);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"));
      pane.setItemMeta(meta);
      return pane;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onBlockPlace(PSCreateEvent event) {
      Player player = event.getPlayer();
      if (player.isSneaking()) {
         event.setCancelled(true);
      } else {
         Location location = event.getRegion().getProtectBlock().getLocation();
         String regionId = event.getRegion().getId();
         String regionType = event.getRegion().getType();
         String owner = player.getName();
         int durability = this.getStartDurability(regionType);
         int maxDurability = durability;
         double hologramOffset = this.config.getDouble("regions." + regionType + ".hologram_offset", 2.5);
         PSRegion existingRegion = this.findRegionByWGRegion(event.getRegion());
         if (existingRegion != null) {
            String existingRegionId = existingRegion.getId();
            if (this.dataConfig.contains("regions." + existingRegionId)) {
               durability = this.dataConfig.getInt("regions." + existingRegionId + ".durability", durability);
               maxDurability = this.dataConfig.getInt("regions." + existingRegionId + ".maxDurability", maxDurability);
               owner = this.dataConfig.getString("regions." + existingRegionId + ".owner", owner);
               if (this.hasRaidTax(existingRegionId)) {
                  long expiryTime = this.raidTaxExpiry.get(existingRegionId);
                  this.raidTaxExpiry.put(regionId, expiryTime);
                  this.raidTaxExpiry.remove(existingRegionId);
               }

               this.dataConfig.set("regions." + existingRegionId, null);
               if (this.holograms.containsKey(existingRegionId)) {
                  this.holograms.get(existingRegionId).delete();
                  this.holograms.remove(existingRegionId);
               }
            }
         }

         location.getWorld().strikeLightningEffect(location);
         Location hologramLocation = location.clone().add(0.5, hologramOffset, 0.5);
         if (DHAPI.getHologram("region_" + regionId) != null) {
            DHAPI.getHologram("region_" + regionId).delete();
         }

         Hologram hologram = DHAPI.createHologram("region_" + regionId, hologramLocation);
         this.updateHologramLines(hologram, regionType, owner, durability, maxDurability);
         this.holograms.put(regionId, hologram);
         this.displayRegionBoundaries(event.getRegion(), true);
         event.getRegion()
            .getWorld()
            .playSound(event.getRegion().getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F);
         this.dataConfig.set("regions." + regionId + ".durability", durability);
         this.dataConfig.set("regions." + regionId + ".maxDurability", maxDurability);
         this.dataConfig.set("regions." + regionId + ".type", regionType);
         this.dataConfig.set("regions." + regionId + ".owner", owner);
         this.dataConfig.set("regions." + regionId + ".location.world", location.getWorld().getName());
         this.dataConfig.set("regions." + regionId + ".location.x", location.getX());
         this.dataConfig.set("regions." + regionId + ".location.y", location.getY());
         this.dataConfig.set("regions." + regionId + ".location.z", location.getZ());
         this.saveData();
      }
   }

   private PSRegion findRegionByWGRegion(PSRegion targetRegion) {
      if (this.dataConfig.getConfigurationSection("regions") == null) {
         return null;
      } else {
         for (String regionId : this.dataConfig.getConfigurationSection("regions").getKeys(false)) {
            String worldName = this.dataConfig.getString("regions." + regionId + ".location.world");
            if (worldName != null) {
               World world = this.getServer().getWorld(worldName);
               if (world != null) {
                  double x = this.dataConfig.getDouble("regions." + regionId + ".location.x");
                  double y = this.dataConfig.getDouble("regions." + regionId + ".location.y");
                  double z = this.dataConfig.getDouble("regions." + regionId + ".location.z");
                  Location loc = new Location(world, x, y, z);
                  PSRegion region = PSRegion.fromLocation(loc);
                  if (region != null && region.getWGRegion().equals(targetRegion.getWGRegion())) {
                     return region;
                  }
               }
            }
         }

         return null;
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         PSRegion region = PSRegion.fromLocation(event.getClickedBlock().getLocation());
         if (region != null) {
            Player player = event.getPlayer();
            Location location = event.getClickedBlock().getLocation();
            Location protectBlockLocation = region.getProtectBlock().getLocation();
            if (protectBlockLocation.getBlockX() == location.getBlockX()
               && protectBlockLocation.getBlockY() == location.getBlockY()
               && protectBlockLocation.getBlockZ() == location.getBlockZ()) {
               String regionType = region.getType();
               if (!regionType.equals("NETHERITE_BLOCK") && !regionType.equals("ANCIENT_DEBRIS")) {
                  if (event.getItem() != null && event.getItem().getType() == Material.NETHERITE_INGOT) {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&f&0&0&0&0▶ &fВы не можете улучшить этот регион в данный момент"));
                  }
               } else {
                  if (event.getItem() != null && event.getItem().getType() == Material.NETHERITE_INGOT) {
                     if (!region.isMember(player.getUniqueId()) && !region.isOwner(player.getUniqueId())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&f&0&0&0&0▶ &fВы не можете восстановить чужой регион"));
                        return;
                     }

                     this.openUpgradeMenu(player, region);
                     event.setCancelled(true);
                  } else {
                     this.displayRegionBoundaries(region, true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRgRemove(PSRemoveEvent event) {
      String regionId = event.getRegion().getId();
      if (this.holograms.containsKey(regionId)) {
         int durability = this.dataConfig.getInt("regions." + regionId + ".durability", this.getStartDurability(event.getRegion().getType()));
         int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability", durability);
         if (durability > 1 && event.getPlayer() == null) {
            event.setCancelled(true);
            this.dataConfig.set("regions." + regionId + ".durability", --durability);
            this.saveData();
            event.getRegion()
               .getWorld()
               .playSound(event.getRegion().getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
            Hologram hologram = this.holograms.get(regionId);
            Location location = event.getRegion().getProtectBlock().getLocation();
            location.getBlock().setType(Material.valueOf(event.getRegion().getType()));
            if (hologram != null) {
               this.updateHologramLines(
                  hologram, event.getRegion().getType(), this.dataConfig.getString("regions." + regionId + ".owner"), durability, maxDurability
               );
            }
         } else if (durability == 1 && event.getPlayer() == null) {
            Hologram hologram = this.holograms.get(regionId);
            Location location = event.getRegion().getProtectBlock().getLocation();
            location.getBlock().setType(Material.AIR);
            if (hologram != null) {
               hologram.delete();
               hologram.getLocation().getWorld().strikeLightningEffect(hologram.getLocation());
               this.holograms.remove(regionId);
            }

            this.displayRegionBoundaries(event.getRegion(), false);
            event.getRegion()
               .getWorld()
               .playSound(event.getRegion().getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
            this.dataConfig.set("regions." + regionId, null);
            this.raidTaxExpiry.remove(regionId);
            this.saveData();
         } else {
            Hologram hologram = this.holograms.get(regionId);
            Location location = event.getRegion().getProtectBlock().getLocation();
            location.getBlock().setType(Material.AIR);

            try {
               ItemStack itemStack = ProtectionStones.createProtectBlockItem(event.getRegion().getTypeOptions());
               if (event.getPlayer() == null) {
                  location.getWorld().dropItemNaturally(location, itemStack);
               } else {
                  this.addItemInventory(event.getPlayer().getInventory(), itemStack, event.getPlayer().getLocation());
               }
            } catch (RuntimeException var9) {
               ItemStack fallbackItem = new ItemStack(Material.valueOf(event.getRegion().getType()));
               if (event.getPlayer() == null) {
                  location.getWorld().dropItemNaturally(location, fallbackItem);
               } else {
                  this.addItemInventory(event.getPlayer().getInventory(), fallbackItem, event.getPlayer().getLocation());
               }
            }

            hologram.delete();
            this.displayRegionBoundaries(event.getRegion(), false);
            event.getRegion()
               .getWorld()
               .playSound(event.getRegion().getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
            this.holograms.remove(regionId);
            hologram.getLocation().getWorld().strikeLightningEffect(hologram.getLocation());
            this.dataConfig.set("regions." + regionId, null);
            this.raidTaxExpiry.remove(regionId);
            this.saveData();
         }
      }
   }

   public void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (int slot = 0; slot < inventory.getStorageContents().length; slot++) {
         ItemStack item = inventory.getItem(slot);
         if (item == null || item.getType().isAir()) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int availableSpace = item.getMaxStackSize() - item.getAmount();
            if (availableSpace > 0) {
               if (itemStack.getAmount() <= availableSpace) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack partialStack = itemStack.clone();
               partialStack.setAmount(availableSpace);
               inventory.addItem(new ItemStack[]{partialStack});
               itemStack.setAmount(itemStack.getAmount() - availableSpace);
            }
         }
      }

      Bukkit.getScheduler().runTask(this, () -> location.getWorld().dropItemNaturally(location, itemStack));
   }

   private void restoreHolograms() {
      if (this.dataConfig.getConfigurationSection("regions") != null) {
         for (String regionId : this.dataConfig.getConfigurationSection("regions").getKeys(false)) {
            int durability = this.dataConfig.getInt("regions." + regionId + ".durability");
            int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability");
            String regionType = this.dataConfig.getString("regions." + regionId + ".type");
            String owner = this.dataConfig.getString("regions." + regionId + ".owner");
            String worldName = this.dataConfig.getString("regions." + regionId + ".location.world");
            double x = this.dataConfig.getDouble("regions." + regionId + ".location.x");
            double y = this.dataConfig.getDouble("regions." + regionId + ".location.y");
            double z = this.dataConfig.getDouble("regions." + regionId + ".location.z");
            if (worldName != null) {
               World world = this.getServer().getWorld(worldName);
               if (world != null) {
                  Location location = new Location(world, x, y, z);
                  double hologramOffset = this.config.getDouble("regions." + regionType + ".hologram_offset", 2.5);
                  Location hologramLocation = location.clone().add(0.5, hologramOffset, 0.5);
                  if (DHAPI.getHologram("region_" + regionId) != null) {
                     DHAPI.getHologram("region_" + regionId).delete();
                  }

                  Hologram hologram = DHAPI.createHologram("region_" + regionId, hologramLocation);
                  this.updateHologramLines(hologram, regionType, owner, durability, maxDurability);
                  this.holograms.put(regionId, hologram);
               }
            }
         }
      }
   }

   private void updateHologramLines(Hologram hologram, String regionType, String owner, int durability, int maxDurability) {
      while (!hologram.getPage(0).getLines().isEmpty()) {
         DHAPI.removeHologramLine(hologram, 0);
      }

      List<String> lines = this.config.getStringList("regions." + regionType + ".lines");
      DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', this.getRegionName(regionType)));
      hologram.setDisplayRange(5);

      for (String line : lines) {
         line = line.replace("%player%", owner).replace("%durability%", String.valueOf(durability)).replace("%max_durability%", String.valueOf(maxDurability));
         DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', line));
      }
   }

   private String getRegionName(String regionType) {
      return this.config.getString("regions." + regionType + ".name", "&aНеизвестный регион");
   }

   private int getDurability(String regionType) {
      return this.config.getInt("regions." + regionType + ".durability", 2);
   }

   private int getStartDurability(String regionType) {
      return this.config.getInt("regions." + regionType + ".start_durability", 2);
   }

   private void saveData() {
      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   public boolean breakRegion(Location location) {
      PSRegion region = PSRegion.fromLocation(location);
      if (region == null) {
         return true;
      } else if (region.getProtectBlock().getLocation().equals(location)) {
         region.deleteRegion(false);
         return false;
      } else {
         return true;
      }
   }

   public boolean breakRegionWithDurability(Location location, int durabilityToRemove) {
      PSRegion region = PSRegion.fromLocation(location);
      if (region == null) {
         return true;
      } else {
         String regionId = region.getId();
         Location protectLoc = region.getProtectBlock().getLocation();
         if (protectLoc.getBlockX() == location.getBlockX() && protectLoc.getBlockY() == location.getBlockY() && protectLoc.getBlockZ() == location.getBlockZ()
            )
          {
            if (!this.dataConfig.contains("regions." + regionId)) {
               region.deleteRegion(true);
               return false;
            } else {
               int durability = this.dataConfig.getInt("regions." + regionId + ".durability", this.getStartDurability(region.getType()));
               int maxDurability = this.dataConfig.getInt("regions." + regionId + ".maxDurability", durability);
               if (durability <= durabilityToRemove) {
                  Hologram hologram = this.holograms.get(regionId);
                  if (hologram != null) {
                     hologram.delete();
                     this.holograms.remove(regionId);
                     hologram.getLocation().getWorld().strikeLightningEffect(hologram.getLocation());
                  }

                  this.displayRegionBoundaries(region, false);
                  region.getWorld().playSound(region.getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
                  this.dataConfig.set("regions." + regionId, null);
                  this.raidTaxExpiry.remove(regionId);
                  this.saveData();
                  region.deleteRegion(true);
                  return false;
               } else {
                  durability -= durabilityToRemove;
                  this.dataConfig.set("regions." + regionId + ".durability", durability);
                  this.saveData();
                  region.getWorld()
                     .playSound(region.getProtectBlock().getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                  Hologram hologram = this.holograms.get(regionId);
                  if (hologram != null) {
                     this.updateHologramLines(
                        hologram, region.getType(), this.dataConfig.getString("regions." + regionId + ".owner"), durability, maxDurability
                     );
                  }

                  return false;
               }
            }
         } else {
            return true;
         }
      }
   }

   private void displayRegionBoundaries(PSRegion region, boolean isOpen) {
      Location min = new Location(
         region.getWorld(),
         region.getWGRegion().getMinimumPoint().getX() + 0.5,
         region.getWGRegion().getMinimumPoint().getY() + 0.5,
         region.getWGRegion().getMinimumPoint().getZ() + 0.5
      );
      Location max = new Location(
         region.getWorld(),
         region.getWGRegion().getMaximumPoint().getX() + 0.5,
         region.getWGRegion().getMaximumPoint().getY() + 0.5,
         region.getWGRegion().getMaximumPoint().getZ() + 0.5
      );
      World world = min.getWorld();
      Color[] colors = isOpen ? new Color[]{Color.fromRGB(65280)} : new Color[]{Color.fromRGB(16711680)};
      this.animateRegionBoundaries(world, min, max, colors, isOpen);
   }

   private void animateRegionBoundaries(final World world, Location min, Location max, final Color[] colors, boolean isOpen) {
      final Location center = new Location(world, (min.getX() + max.getX()) / 2.0, (min.getY() + max.getY()) / 2.0, (min.getZ() + max.getZ()) / 2.0);
      double sizeX = max.getX() - min.getX();
      double sizeY = max.getY() - min.getY();
      double sizeZ = max.getZ() - min.getZ();
      final double size = Math.max(sizeX, Math.max(sizeY, sizeZ)) / 2.0;
      this.drawSquare(world, center, size, colors);
      (new BukkitRunnable() {
         public void run() {
            FichaPSAddon.this.drawSquare(world, center, size, colors);
         }
      }).runTaskLater(this, 20L);
   }

   private void drawSquare(World world, Location center, double size, Color[] colors) {
      int colorIndex = 0;

      for (double x = center.getX() - size; x <= center.getX() + size; x += 0.5) {
         world.spawnParticle(
            Particle.DUST,
            new Location(world, x, center.getY() - size, center.getZ() - size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, x, center.getY() - size, center.getZ() + size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, x, center.getY() + size, center.getZ() + size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, x, center.getY() + size, center.getZ() - size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         colorIndex++;
      }

      for (double z = center.getZ() - size; z <= center.getZ() + size; z += 0.5) {
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() - size, center.getY() - size, z),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() - size, center.getY() + size, z),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() + size, center.getY() - size, z),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() + size, center.getY() + size, z),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         colorIndex++;
      }

      for (double y = center.getY() - size; y <= center.getY() + size; y += 0.5) {
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() - size, y, center.getZ() - size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() - size, y, center.getZ() + size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() + size, y, center.getZ() - size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         world.spawnParticle(
            Particle.DUST,
            new Location(world, center.getX() + size, y, center.getZ() + size),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            new DustOptions(colors[colorIndex % colors.length], 1.0F),
            true
         );
         colorIndex++;
      }
   }
}
