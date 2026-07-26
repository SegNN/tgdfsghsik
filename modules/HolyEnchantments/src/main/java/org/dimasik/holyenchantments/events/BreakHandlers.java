package org.dimasik.holyenchantments.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;
import org.dimasik.holyenchantments.enchantscore.MEnchant;
import org.dimasik.holyenchantments.enchantscore.levels.AbstractLevel;
import org.dimasik.holyenchantments.enchantscore.levels.DoubleLevel;
import org.dimasik.holyenchantments.enchantscore.levels.IntLevel;
import org.dimasik.holyenchantments.utils.EncoderSound;
import org.dimasik.holyenchantments.utils.Schedulers;

public class BreakHandlers implements Listener {
   private static final Set<Material> ORES = EnumSet.of(
      Material.COAL_ORE,
      Material.IRON_ORE,
      Material.GOLD_ORE,
      Material.REDSTONE_ORE,
      Material.EMERALD_ORE,
      Material.LAPIS_ORE,
      Material.DIAMOND_ORE,
      Material.NETHER_QUARTZ_ORE,
      Material.NETHER_GOLD_ORE
   );
   private static final Set<Material> STONE_TYPES = EnumSet.of(Material.STONE, Material.COBBLESTONE);
   private final EnchantsMap enchantsMap;
   private final NamespacedKey spawnerKey;
   private final List<Block> brokenBlocksCache = new ArrayList<>();
   private final HashMap<Player, Boolean> boorToggleMap = new HashMap<>();

   public BreakHandlers(EnchantsMap enchantsMap, NamespacedKey spawnerKey) {
      this.enchantsMap = enchantsMap;
      this.spawnerKey = spawnerKey;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (this.hasRelevantEnchantments(tool)) {
         this.handleExpBoost(event, tool);
         this.handleBoorEnchantment(event, player, tool);
      }
   }

   private boolean hasRelevantEnchantments(ItemStack tool) {
      if (!tool.hasItemMeta()) {
         return false;
      } else {
         PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
         return !data.isEmpty()
            && (
               data.has(this.enchantsMap.expBoost.getNamespacedKey(), PersistentDataType.INTEGER)
                  || data.has(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER)
                  || data.has(this.enchantsMap.magnet.getNamespacedKey(), PersistentDataType.INTEGER)
                  || data.has(this.enchantsMap.melting.getNamespacedKey(), PersistentDataType.INTEGER)
                  || data.has(this.enchantsMap.filter.getNamespacedKey(), PersistentDataType.INTEGER)
            );
      }
   }

   private void handleExpBoost(BlockBreakEvent event, ItemStack tool) {
      PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
      if (data.has(this.enchantsMap.expBoost.getNamespacedKey(), PersistentDataType.INTEGER)) {
         AbstractLevel level = this.enchantsMap.expBoost.getLevel(data);
         if (level != null && level.random()) {
            event.setExpToDrop((int)(event.getExpToDrop() * ((DoubleLevel)level).getValue()));
            this.playEnchantmentSounds(event.getPlayer(), this.enchantsMap.expBoost);
         }
      }
   }

   private void handleBoorEnchantment(BlockBreakEvent event, Player player, ItemStack tool) {
      if (!event.isCancelled() && !this.brokenBlocksCache.contains(event.getBlock())) {
         PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
         boolean hasBoor = data.has(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER);
         int radius = hasBoor ? this.getBoorRadius(player, data) : 0;
         boolean hasMelting = data.has(this.enchantsMap.melting.getNamespacedKey(), PersistentDataType.INTEGER);
         boolean hasMagnet = data.has(this.enchantsMap.magnet.getNamespacedKey(), PersistentDataType.INTEGER);
         boolean hasFilter = data.has(this.enchantsMap.filter.getNamespacedKey(), PersistentDataType.INTEGER);
         if (hasMelting || hasMagnet) {
            this.playEnchantmentSounds(player, hasMelting, hasMagnet);
         }

         Collection<ItemStack> collectedItems = new ArrayList<>();
         int brokenBlocksCount = 0;
         if (radius > 0) {
            brokenBlocksCount = this.breakBlocksInRadius(
               event.getBlock(), tool, player, radius, this.getBoorLevel(data), hasMelting, hasMagnet, hasFilter, collectedItems
            );
         } else {
            Collection<ItemStack> drops = this.breakBlock(tool, player, event.getBlock(), hasMelting, hasFilter);
            if (drops != null) {
               brokenBlocksCount = 1;
               if (hasMagnet) {
                  collectedItems.addAll(drops);
               }
            }
         }

         this.applyToolDamage(tool, brokenBlocksCount, data);
         this.dropItems(player, collectedItems);
      } else {
         this.brokenBlocksCache.remove(event.getBlock());
      }
   }

   private int getBoorRadius(Player player, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER) && !this.boorToggleMap.containsKey(player)) {
         AbstractLevel level = this.enchantsMap.boor.getLevel(data);
         if (level != null && level.random()) {
            this.playEnchantmentSounds(player, this.enchantsMap.boor);
            return ((IntLevel)level).getValue();
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private int getBoorLevel(PersistentDataContainer data) {
      return (Integer)data.getOrDefault(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER, -1);
   }

   private int breakBlocksInRadius(
      Block center,
      ItemStack tool,
      Player player,
      int radius,
      int boorLevel,
      boolean melting,
      boolean magnet,
      boolean filter,
      Collection<ItemStack> collectedItems
   ) {
      int brokenBlocks = 0;
      BreakHandlers.DiggingPlane plane = this.determineDiggingPlane(player);
      int minX = -radius;
      int maxX = radius;
      int minY = -radius;
      int maxY = radius;
      int minZ = -radius;
      int maxZ = radius;
      if (boorLevel == 1) {
         switch (plane) {
            case XY:
               maxZ = 0;
               minZ = 0;
               break;
            case XZ:
            default:
               maxY = 0;
               minY = 0;
               break;
            case YZ:
               maxX = 0;
               minX = 0;
         }
      }

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               Block target = center.getRelative(x, y, z);
               if (this.isBreakable(target, tool)) {
                  Collection<ItemStack> drops = this.breakBlock(tool, player, target, melting, filter);
                  if (drops != null) {
                     brokenBlocks++;
                     if (magnet) {
                        collectedItems.addAll(drops);
                     }
                  }
               }
            }
         }
      }

      return brokenBlocks;
   }

   private BreakHandlers.DiggingPlane determineDiggingPlane(Player player) {
      float pitch = player.getLocation().getPitch();
      if (Math.abs(pitch) > 45.0F) {
         return BreakHandlers.DiggingPlane.XZ;
      } else {
         BlockFace face = player.getFacing();
         switch (face) {
            case NORTH:
            case SOUTH:
               return BreakHandlers.DiggingPlane.XY;
            case EAST:
            case WEST:
               return BreakHandlers.DiggingPlane.YZ;
            default:
               return BreakHandlers.DiggingPlane.XZ;
         }
      }
   }

   private Block getRelativeBlock(Block center, int x, int y, int z, BreakHandlers.DiggingPlane plane, int boorLevel) {
      if (boorLevel == 1) {
         switch (plane) {
            case XY:
               return center.getRelative(x, y, 0);
            case XZ:
            default:
               return center.getRelative(x, 0, z);
            case YZ:
               return center.getRelative(0, y, z);
         }
      } else {
         return center.getRelative(x, y, z);
      }
   }

   private boolean isBreakable(Block block, ItemStack tool) {
      return !block.isEmpty() && block.getType() != Material.BEDROCK && block.getType() != Material.BARRIER && block.isPreferredTool(tool);
   }

   private Collection<ItemStack> breakBlock(ItemStack tool, Player player, Block block, boolean melting, boolean filter) {
      this.brokenBlocksCache.add(block);
      BlockBreakEvent event = new BlockBreakEvent(block, player);
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         return null;
      } else if (block.getType() != Material.TNT && block.getType() != Material.DISPENSER) {
         World world = block.getWorld();
         Location location = block.getLocation();
         if (!event.isDropItems()) {
            block.setType(Material.AIR);
            return Collections.emptyList();
         } else {
            Collection<ItemStack> originalDrops = block.getDrops(tool);
            Collection<ItemStack> processedDrops = new ArrayList<>();
            if (melting) {
               processedDrops.addAll(this.getSmeltedDrops(originalDrops, filter));
            } else if (filter) {
               processedDrops.addAll(this.getFilteredDrops(originalDrops, filter));
            } else {
               processedDrops.addAll(originalDrops);
            }

            if (!player.getInventory()
               .getItemInMainHand()
               .getItemMeta()
               .getPersistentDataContainer()
               .has(this.enchantsMap.magnet.getNamespacedKey(), PersistentDataType.INTEGER)) {
               processedDrops.forEach(item -> world.dropItemNaturally(location, item));
            }

            this.dropExperience(world, location, block);
            block.setType(Material.AIR);
            return processedDrops;
         }
      } else {
         return null;
      }
   }

   private Collection<ItemStack> getSmeltedDrops(Collection<ItemStack> drops, boolean filter) {
      return drops.stream()
         .filter(item -> !filter || !STONE_TYPES.contains(item.getType()))
         .map(this::smeltIfOre)
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   private Collection<ItemStack> getFilteredDrops(Collection<ItemStack> drops, boolean filter) {
      return (Collection<ItemStack>)(!filter ? drops : drops.stream().filter(item -> !STONE_TYPES.contains(item.getType())).collect(Collectors.toList()));
   }

   private ItemStack smeltIfOre(ItemStack item) {
      if (!ORES.contains(item.getType())) {
         return item;
      } else {
         Iterator<Recipe> recipes = Bukkit.recipeIterator();

         while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (recipe instanceof FurnaceRecipe && ((FurnaceRecipe)recipe).getInput().getType() == item.getType()) {
               ItemStack result = recipe.getResult().clone();
               result.setAmount(item.getAmount());
               return result;
            }
         }

         return item;
      }
   }

   private void dropExperience(World world, Location location, Block block) {
      int experience = this.getExperienceForBlock(block);
      if (experience > 0) {
         ((ExperienceOrb)world.spawn(location, ExperienceOrb.class)).setExperience(experience);
      }
   }

   private int getExperienceForBlock(Block block) {
      Random random = new Random();
      switch (block.getType()) {
         case NETHER_GOLD_ORE:
            return random.nextInt(2);
         case COAL_ORE:
            return random.nextInt(3);
         case REDSTONE_ORE:
            return 1 + random.nextInt(5);
         case LAPIS_ORE:
         case NETHER_QUARTZ_ORE:
            return 2 + random.nextInt(4);
         case DIAMOND_ORE:
         case EMERALD_ORE:
            return 3 + random.nextInt(5);
         case SPAWNER:
            return 15 + random.nextInt(29);
         default:
            return 0;
      }
   }

   private void applyToolDamage(ItemStack tool, int brokenBlocks, PersistentDataContainer data) {
      if (!tool.getItemMeta().isUnbreakable()) {
         int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);
         int durabilityLoss = this.calculateDurabilityLoss(unbreakingLevel, brokenBlocks);
         tool.setDurability((short)(tool.getDurability() + durabilityLoss));
         if (tool.getDurability() >= tool.getType().getMaxDurability() && data.has(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER)) {
            tool.setAmount(0);
         }
      }
   }

   private int calculateDurabilityLoss(int unbreakingLevel, int brokenBlocks) {
      int loss = 0;

      for (int i = 0; i < brokenBlocks; i++) {
         if (new Random().nextInt(unbreakingLevel + 1) == 0) {
            loss++;
         }
      }

      return loss;
   }

   private void playEnchantmentSounds(Player player, MEnchant enchantment) {
      EncoderSound.playSound(player, enchantment.getSoundAlone());
      EncoderSound.playSound(player.getLocation(), enchantment.getSoundAround());
   }

   private void playEnchantmentSounds(Player player, boolean melting, boolean magnet) {
      if (melting) {
         this.playEnchantmentSounds(player, this.enchantsMap.melting);
      }

      if (magnet) {
         this.playEnchantmentSounds(player, this.enchantsMap.magnet);
      }
   }

   private void dropItems(Player player, Collection<ItemStack> items) {
      if (!items.isEmpty()) {
         Schedulers.async(() -> {
            PlayerInventory inventory = player.getInventory();
            Location location = player.getLocation();
            items.forEach(item -> this.addToInventoryOrDrop(inventory, item, location));
         });
      }
   }

   private void addToInventoryOrDrop(Inventory inventory, ItemStack item, Location dropLocation) {
      HashMap<Integer, ItemStack> remaining = inventory.addItem(new ItemStack[]{item});
      if (!remaining.isEmpty()) {
         Schedulers.sync(() -> remaining.values().forEach(i -> dropLocation.getWorld().dropItemNaturally(dropLocation, i)));
      }
   }

   @EventHandler
   public void onCropBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (tool.hasItemMeta()) {
         PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
         if (!data.isEmpty()) {
            this.handleDeliciousEnchantment(event, data);
            this.handleSowingEnchantment(event, data);
            this.handleFermerEnchantment(event, tool);
         }
      }
   }

   private void handleDeliciousEnchantment(BlockBreakEvent event, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.delicious.getNamespacedKey(), PersistentDataType.INTEGER) && !event.isCancelled()) {
         AbstractLevel level = this.enchantsMap.delicious.getLevel(data);
         if (level != null && level.random()) {
            Block block = event.getBlock();
            if (this.isCrop(block)) {
               int levelValue = (Integer)data.get(this.enchantsMap.delicious.getNamespacedKey(), PersistentDataType.INTEGER);
               if (block.getBlockData() instanceof Ageable) {
                  Ageable ageable = (Ageable)block.getBlockData();
                  switch (levelValue) {
                     case 1:
                        if (ageable.getAge() == 0) {
                           event.setCancelled(true);
                        }
                        break;
                     case 2:
                        if (ageable.getAge() == 0) {
                           event.setCancelled(true);
                        } else if (ageable.getAge() != ageable.getMaximumAge()) {
                           ageable.setAge(ageable.getAge() - 1);
                           block.setBlockData(ageable);
                           event.setCancelled(true);
                        }
                        break;
                     default:
                        if (ageable.getAge() != ageable.getMaximumAge()) {
                           event.setCancelled(true);
                        }
                  }
               }
            }
         }
      }
   }

   private void handleSowingEnchantment(BlockBreakEvent event, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.sowing.getNamespacedKey(), PersistentDataType.INTEGER) && !event.isCancelled()) {
         AbstractLevel level = this.enchantsMap.sowing.getLevel(data);
         if (level != null && level.random()) {
            Block block = event.getBlock();
            if (this.isMatureCrop(block)) {
               final Material cropType = block.getType();
               final Location location = block.getLocation();
               (new BukkitRunnable() {
                  public void run() {
                     Block targetBlock = location.getBlock();
                     targetBlock.setType(cropType);
                     if (targetBlock.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable)targetBlock.getBlockData();
                        ageable.setAge(0);
                        targetBlock.setBlockData(ageable);
                     }
                  }
               }).runTaskLater(HolyEnchantments.getInstance(), 1L);
            }
         }
      }
   }

   private void handleFermerEnchantment(BlockBreakEvent event, ItemStack tool) {
      PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
      if (data.has(this.enchantsMap.fermer.getNamespacedKey(), PersistentDataType.INTEGER) && !event.isCancelled()) {
         AbstractLevel level = this.enchantsMap.fermer.getLevel(data);
         if (level != null && level.random()) {
            double multiplier = ((DoubleLevel)level).getValue();

            for (ItemStack drop : event.getBlock().getDrops(tool)) {
               drop.setAmount((int)Math.ceil(drop.getAmount() * multiplier));
            }
         }
      }
   }

   private boolean isCrop(Block block) {
      if (block == null) {
         return false;
      } else {
         Material type = block.getType();
         return type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES || type == Material.BEETROOTS || type == Material.NETHER_WART;
      }
   }

   private boolean isMatureCrop(Block block) {
      if (!this.isCrop(block)) {
         return false;
      } else if (block.getBlockData() instanceof Ageable) {
         Ageable ageable = (Ageable)block.getBlockData();
         return ageable.getAge() == ageable.getMaximumAge();
      } else {
         return false;
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (event.getPlayer().isSneaking()) {
            ItemStack item = event.getItem();
            if (item != null) {
               PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
               if (!data.isEmpty()) {
                  this.toggleBoor(event.getPlayer(), data);
               }
            }
         }
      }
   }

   private void toggleBoor(Player player, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.boor.getNamespacedKey(), PersistentDataType.INTEGER)) {
         if (this.boorToggleMap.containsKey(player)) {
            player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "Бур активирован"));
            this.boorToggleMap.remove(player);
         } else {
            player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "Бур деактивирован"));
            this.boorToggleMap.put(player, true);
         }
      }
   }

   @EventHandler
   public void onBlockDrops(BlockDropItemEvent event) {
      ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
      if (tool.hasItemMeta()) {
         PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
         if (!data.isEmpty()) {
            this.handleMeltingOnDrop(event, data);
            this.handleMagnetOnDrop(event, data);
         }
      }
   }

   private void handleMeltingOnDrop(BlockDropItemEvent event, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.melting.getNamespacedKey(), PersistentDataType.INTEGER) && this.enchantsMap.melting.getLevel(data).random()) {
         this.playEnchantmentSounds(event.getPlayer(), this.enchantsMap.melting);
         event.getItems().forEach(item -> item.setItemStack(this.smeltIfOre(item.getItemStack())));
      }
   }

   private void handleMagnetOnDrop(BlockDropItemEvent event, PersistentDataContainer data) {
      if (data.has(this.enchantsMap.magnet.getNamespacedKey(), PersistentDataType.INTEGER)) {
         this.playEnchantmentSounds(event.getPlayer(), this.enchantsMap.magnet);
         event.setCancelled(true);
         this.dropItems(event.getPlayer(), event.getItems().stream().<ItemStack>map(Item::getItemStack).collect(Collectors.toList()));
      }
   }

   private ItemStack createSpawnerItem(EntityType entityType) {
      ItemStack item = new ItemStack(Material.SPAWNER);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(entityType.name());
      meta.getPersistentDataContainer().set(this.spawnerKey, PersistentDataType.STRING, entityType.name());
      item.setItemMeta(meta);
      return item;
   }

   private static enum DiggingPlane {
      XY,
      XZ,
      YZ;

      private DiggingPlane() {
      }
   }
}
