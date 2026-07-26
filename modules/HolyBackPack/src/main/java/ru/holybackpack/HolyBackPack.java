package ru.holybackpack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class HolyBackPack extends JavaPlugin implements Listener {
   private final NamespacedKey backpackLevelKey = new NamespacedKey(this, "backpack-level");
   private final NamespacedKey backpackUuidKey = new NamespacedKey(this, "backpack-uuid");
   private final NamespacedKey protectionKey = new NamespacedKey(this, "protection");
   private final NamespacedKey extraSlotsKey = new NamespacedKey(this, "extra-slots");

   public HolyBackPack() {
   }

   public void onEnable() {
      this.getCommand("holybackpack").setExecutor(new HolyBackPack.BackPackCommand());
      this.getServer().getPluginManager().registerEvents(this, this);
      this.removeRecipes();
      this.registerRecipes();
   }

   public void onDisable() {
      this.removeRecipes();
   }

   @EventHandler
   public void onAnvil(PrepareAnvilEvent event) {
      ItemStack first = event.getInventory().getFirstItem();
      ItemStack second = event.getInventory().getSecondItem();
      if (first != null && first.hasItemMeta() && first.getItemMeta().getPersistentDataContainer().has(this.backpackLevelKey, PersistentDataType.INTEGER)) {
         event.setResult(new ItemStack(Material.AIR));
      }

      if (second != null && second.hasItemMeta() && second.getItemMeta().getPersistentDataContainer().has(this.backpackLevelKey, PersistentDataType.INTEGER)) {
         event.setResult(new ItemStack(Material.AIR));
      }
   }

   @EventHandler
   public void onPrepareCraft(PrepareItemCraftEvent event) {
      Recipe recipe = event.getRecipe();
      if (recipe instanceof ShapedRecipe) {
         ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
         String key = shapedRecipe.getKey().getKey();
         if (key.contains("backpack_level_")) {
            ItemStack[] matrix = event.getInventory().getContents();
            if (key.equals("backpack_level_2")) {
               if (!this.containsBackpack(matrix, 1)) {
                  event.getInventory().setResult(new ItemStack(Material.AIR));
               } else {
                  event.getInventory().setResult(this.get2(1));
               }
            } else if (key.equals("backpack_level_3")) {
               if (!this.containsBackpack(matrix, 2)) {
                  event.getInventory().setResult(new ItemStack(Material.AIR));
               } else {
                  event.getInventory().setResult(this.get3(1));
               }
            } else if (key.equals("backpack_level_4")) {
               if (!this.containsBackpack(matrix, 3)) {
                  event.getInventory().setResult(new ItemStack(Material.AIR));
               } else {
                  event.getInventory().setResult(this.get4(1));
               }
            }
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
         && event.getItem() != null
         && this.isValidBackpack(event.getItem())) {
         event.setCancelled(true);
         this.openShulkerBoxInventory(event.getPlayer(), event.getItem());
      }
   }

   @EventHandler
   public void onDispense(BlockDispenseEvent event) {
      if (event.getItem() != null && this.isValidBackpack(event.getItem())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      ItemStack droppedItem = event.getItemDrop().getItemStack();
      if (droppedItem.getType().name().contains("SHULKER_BOX") && this.isValidBackpack(droppedItem)) {
         Inventory topInventory = player.getOpenInventory().getTopInventory();
         if (topInventory != null && player.hasMetadata("openShulkerBox")) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      Inventory clickedInventory = event.getClickedInventory();
      Inventory topInventory = event.getView().getTopInventory();
      if (clickedInventory != null && topInventory != null) {
         ItemStack currentItem = event.getCurrentItem();
         ItemStack cursorItem = event.getCursor();
         if (player.hasMetadata("openShulkerBox")) {
            List<MetadataValue> metadata = player.getMetadata("openShulkerBox");
            if (metadata.isEmpty()) {
               return;
            }

            if (event.getClick() == ClickType.NUMBER_KEY) {
               int hotbarSlot = event.getHotbarButton();
               ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
               if (hotbarItem != null && this.isValidBackpack(hotbarItem)) {
                  event.setCancelled(true);
                  return;
               }

               if (clickedInventory.equals(topInventory) && currentItem != null && this.isValidBackpack(currentItem)) {
                  event.setCancelled(true);
                  return;
               }
            }

            if (event.isShiftClick() && clickedInventory.getType() == InventoryType.PLAYER && currentItem != null && this.isValidBackpack(currentItem)) {
               event.setCancelled(true);
               return;
            }

            Bukkit.getScheduler()
               .runTaskLater(
                  this,
                  () -> {
                     ItemStack shulkerBox = (ItemStack)metadata.get(0).value();
                     if (shulkerBox != null && this.isValidBackpack(shulkerBox)) {
                        BlockStateMeta blockStateMeta = (BlockStateMeta)shulkerBox.getItemMeta();
                        if (blockStateMeta != null) {
                           BlockState blockState = blockStateMeta.getBlockState();
                           if (blockState instanceof ShulkerBox) {
                              ShulkerBox shulkerBoxState = (ShulkerBox)blockState;
                              PersistentDataContainer pdc = shulkerBox.getItemMeta().getPersistentDataContainer();
                              int level = (Integer)pdc.get(this.backpackLevelKey, PersistentDataType.INTEGER);
                              if (level == 5) {
                                 ItemStack[] first27 = new ItemStack[27];

                                 for (int i = 0; i < 27; i++) {
                                    ItemStack item = topInventory.getItem(i);
                                    if (item != null
                                       && item.hasItemMeta()
                                       && item.getItemMeta().getPersistentDataContainer().has(this.protectionKey, PersistentDataType.STRING)) {
                                       first27[i] = null;
                                    } else {
                                       first27[i] = item;
                                    }
                                 }

                                 shulkerBoxState.getInventory().setContents(first27);
                                 List<ItemStack> extraItems = new ArrayList<>();

                                 for (int ix = 27; ix < 36; ix++) {
                                    ItemStack item = topInventory.getItem(ix);
                                    extraItems.add(item != null ? item : new ItemStack(Material.AIR));
                                 }

                                 byte[] serialized = this.serializeItems(extraItems);
                                 blockStateMeta.getPersistentDataContainer().set(this.extraSlotsKey, PersistentDataType.BYTE_ARRAY, serialized);
                              } else {
                                 Inventory tempInventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);
                                 tempInventory.setContents(topInventory.getContents());

                                 for (int ix = 0; ix < 27; ix++) {
                                    ItemStack item = tempInventory.getItem(ix);
                                    if (item != null
                                       && item.hasItemMeta()
                                       && item.getItemMeta().getPersistentDataContainer().has(this.protectionKey, PersistentDataType.STRING)) {
                                       tempInventory.setItem(ix, null);
                                    }
                                 }

                                 shulkerBoxState.getInventory().setContents(tempInventory.getContents());
                              }

                              blockStateMeta.setBlockState(shulkerBoxState);
                              shulkerBox.setItemMeta(blockStateMeta);
                           }
                        }
                     }
                  },
                  1L
               );
         }

         if (clickedInventory.getType() == InventoryType.PLAYER
            && player.hasMetadata("openShulkerBox")
            && (currentItem != null && this.isValidBackpack(currentItem) || cursorItem != null && this.isValidBackpack(cursorItem))) {
            event.setCancelled(true);
         }

         if (event.getClick().isKeyboardClick() && cursorItem != null && this.isValidBackpack(cursorItem)) {
            event.setCancelled(true);
         }

         if (currentItem != null
            && currentItem.hasItemMeta()
            && currentItem.getItemMeta().getPersistentDataContainer().has(this.protectionKey, PersistentDataType.STRING)
            && "disable_click"
               .equalsIgnoreCase((String)currentItem.getItemMeta().getPersistentDataContainer().get(this.protectionKey, PersistentDataType.STRING))) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      if (player.hasMetadata("openShulkerBox")) {
         ItemStack shulkerBox = (ItemStack)((MetadataValue)player.getMetadata("openShulkerBox").get(0)).value();
         if (!this.isValidBackpack(shulkerBox)) {
            int detectedLevel = this.determineBackpackLevel(shulkerBox);
            if (detectedLevel > 0) {
               shulkerBox = this.restoreBackpack(shulkerBox, detectedLevel);
            }
         }

         BlockStateMeta blockStateMeta = (BlockStateMeta)shulkerBox.getItemMeta();
         if (blockStateMeta == null) {
            player.removeMetadata("openShulkerBox", this);
         } else {
            BlockState blockState = blockStateMeta.getBlockState();
            if (!(blockState instanceof ShulkerBox)) {
               player.removeMetadata("openShulkerBox", this);
            } else {
               ShulkerBox shulkerBoxState = (ShulkerBox)blockState;
               Inventory inventory = event.getInventory();
               ItemMeta originalMeta = shulkerBox.getItemMeta();
               int level = (Integer)originalMeta.getPersistentDataContainer().get(this.backpackLevelKey, PersistentDataType.INTEGER);
               if (level == 5) {
                  for (int i = 0; i < 27; i++) {
                     ItemStack item = inventory.getItem(i);
                     if (item != null
                        && item.hasItemMeta()
                        && item.getItemMeta().getPersistentDataContainer().has(this.protectionKey, PersistentDataType.STRING)) {
                        inventory.setItem(i, null);
                     }
                  }

                  ItemStack[] first27 = new ItemStack[27];

                  for (int ix = 0; ix < 27; ix++) {
                     first27[ix] = inventory.getItem(ix);
                  }

                  shulkerBoxState.getInventory().setContents(first27);
                  List<ItemStack> extraItems = new ArrayList<>();

                  for (int ix = 27; ix < 36; ix++) {
                     ItemStack item = inventory.getItem(ix);
                     extraItems.add(item != null ? item : new ItemStack(Material.AIR));
                  }

                  byte[] serialized = this.serializeItems(extraItems);
                  blockStateMeta.getPersistentDataContainer().set(this.extraSlotsKey, PersistentDataType.BYTE_ARRAY, serialized);
               } else {
                  for (int ix = 0; ix < 27; ix++) {
                     ItemStack item = inventory.getItem(ix);
                     if (item != null
                        && item.hasItemMeta()
                        && item.getItemMeta().getPersistentDataContainer().has(this.protectionKey, PersistentDataType.STRING)) {
                        inventory.setItem(ix, null);
                     }
                  }

                  shulkerBoxState.getInventory().setContents(inventory.getContents());
               }

               shulkerBoxState.setCustomName(null);
               blockStateMeta.setBlockState(shulkerBoxState);
               blockStateMeta.setLore(originalMeta.getLore());
               blockStateMeta.setDisplayName(originalMeta.getDisplayName());
               blockStateMeta.getPersistentDataContainer()
                  .set(
                     this.backpackLevelKey,
                     PersistentDataType.INTEGER,
                     (Integer)originalMeta.getPersistentDataContainer().get(this.backpackLevelKey, PersistentDataType.INTEGER)
                  );
               blockStateMeta.getPersistentDataContainer()
                  .set(
                     this.backpackUuidKey,
                     PersistentDataType.STRING,
                     (String)originalMeta.getPersistentDataContainer().get(this.backpackUuidKey, PersistentDataType.STRING)
                  );

               for (ItemFlag flag : originalMeta.getItemFlags()) {
                  blockStateMeta.addItemFlags(new ItemFlag[]{flag});
               }

               shulkerBox.setItemMeta(blockStateMeta);
               player.removeMetadata("openShulkerBox", this);
               player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 1.0F, 1.0F);
            }
         }
      }
   }

   public void openBackpack(Player player, ItemStack backpack) {
      if (this.isValidBackpack(backpack)) {
         this.openShulkerBoxInventory(player, backpack);
      }
   }

   private void openShulkerBoxInventory(Player player, ItemStack shulkerBox) {
      if (!this.isValidBackpack(shulkerBox)) {
         int level = this.determineBackpackLevel(shulkerBox);
         if (level <= 0) {
            player.sendMessage(ChatColor.RED + "Этот рюкзак поврежден!");
            return;
         }

         shulkerBox = this.restoreBackpack(shulkerBox, level);
      }

      BlockStateMeta blockStateMeta = (BlockStateMeta)shulkerBox.getItemMeta();
      if (blockStateMeta != null) {
         PersistentDataContainer pdc = shulkerBox.getItemMeta().getPersistentDataContainer();
         if (pdc.has(this.backpackLevelKey, PersistentDataType.INTEGER)) {
            int level = (Integer)pdc.get(this.backpackLevelKey, PersistentDataType.INTEGER);
            BlockState blockState = blockStateMeta.getBlockState();
            if (blockState instanceof ShulkerBox) {
               ShulkerBox shulkerBoxState = (ShulkerBox)blockState;
               Inventory shulkerInventory = shulkerBoxState.getInventory();
               Inventory inv;
               if (level == 5) {
                  inv = Bukkit.createInventory(null, 36, shulkerBox.getItemMeta().getDisplayName());
               } else {
                  inv = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, shulkerBox.getItemMeta().getDisplayName());
               }

               inv.setContents(shulkerInventory.getContents());
               if (level == 5 && pdc.has(this.extraSlotsKey, PersistentDataType.BYTE_ARRAY)) {
                  byte[] extraItemsData = (byte[])pdc.get(this.extraSlotsKey, PersistentDataType.BYTE_ARRAY);
                  if (extraItemsData != null && extraItemsData.length > 0) {
                     try {
                        List<ItemStack> extraItems = this.deserializeItems(extraItemsData);

                        for (int i = 0; i < Math.min(extraItems.size(), 9); i++) {
                           inv.setItem(27 + i, extraItems.get(i));
                        }
                     } catch (Exception var14) {
                        Bukkit.getLogger().warning("Ошибка загрузки дополнительных слотов INFINITY: " + var14.getMessage());
                     }
                  }
               }

               ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
               ItemMeta glassMeta = glassPane.getItemMeta();
               glassMeta.setDisplayName(" ");
               glassMeta.getPersistentDataContainer().set(this.protectionKey, PersistentDataType.STRING, "disable_click");
               glassPane.setItemMeta(glassMeta);
               if (level < 5) {
                  int blocked = 4 - level;

                  for (int i = 0; i < blocked; i++) {
                     inv.setItem(i, glassPane);
                     inv.setItem(i + 9, glassPane);
                     inv.setItem(i + 18, glassPane);
                     inv.setItem(8 - i, glassPane);
                     inv.setItem(8 - i + 9, glassPane);
                     inv.setItem(8 - i + 18, glassPane);
                  }
               }

               player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F);
               player.openInventory(inv);
               player.setMetadata("openShulkerBox", new FixedMetadataValue(this, shulkerBox));
            }
         }
      }
   }

   private boolean containsBackpack(ItemStack[] matrix, int requiredLevel) {
      for (ItemStack item : matrix) {
         if (item != null && item.hasItemMeta()) {
            Integer level = (Integer)item.getItemMeta().getPersistentDataContainer().get(this.backpackLevelKey, PersistentDataType.INTEGER);
            if (level != null && level == requiredLevel) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isValidBackpack(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
         return pdc.has(this.backpackLevelKey, PersistentDataType.INTEGER) && pdc.has(this.backpackUuidKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   private ItemStack restoreBackpack(ItemStack item, int level) {
      if (item == null) {
         return null;
      } else {
         switch (level) {
            case 1:
               return this.get1(item.getAmount());
            case 2:
               return this.get2(item.getAmount());
            case 3:
               return this.get3(item.getAmount());
            case 4:
               return this.get4(item.getAmount());
            case 5:
               return this.get5(item.getAmount());
            default:
               return item;
         }
      }
   }

   private int determineBackpackLevel(ItemStack item) {
      if (item == null) {
         return 0;
      } else {
         switch (item.getType()) {
            case PINK_SHULKER_BOX:
               return 1;
            case LIGHT_BLUE_SHULKER_BOX:
               return 2;
            case RED_SHULKER_BOX:
               return 3;
            case MAGENTA_SHULKER_BOX:
               return 4;
            case LIME_SHULKER_BOX:
               return 5;
            default:
               return 0;
         }
      }
   }

   private ItemStack createBackpack(int level, int count, Material material, String name, String colorCode) {
      ItemStack itemStack = new ItemStack(material, count);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "  " + colorCode + "- нельзя поставить на землю;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "  " + colorCode + "- возможность открыть через ПКМ;"));
      int slots = 0;
      switch (level) {
         case 1:
            slots = 9;
            break;
         case 2:
            slots = 15;
            break;
         case 3:
         case 4:
            slots = 21;
            break;
         case 5:
            slots = 36;
      }

      lore.add(ChatColor.translateAlternateColorCodes('&', "  " + colorCode + "- вместимость " + slots + " слотов;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(this.backpackLevelKey, PersistentDataType.INTEGER, level);
      itemMeta.getPersistentDataContainer().set(this.backpackUuidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_DESTROYS});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_PLACED_ON});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
      if (itemMeta instanceof BlockStateMeta) {
         BlockStateMeta blockStateMeta = (BlockStateMeta)itemMeta;
         BlockState blockState = blockStateMeta.getBlockState();
         if (blockState instanceof ShulkerBox) {
            ShulkerBox shulkerBox = (ShulkerBox)blockState;
            shulkerBox.getInventory().clear();
            shulkerBox.setCustomName(null);
            blockStateMeta.setBlockState(shulkerBox);
         }
      }

      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack get1(int count) {
      return this.createBackpack(
         1,
         count,
         Material.PINK_SHULKER_BOX,
         "&x&D&8&7&A&B&8Р&x&E&2&8&0&C&1ю&x&E&D&8&6&C&9к&x&F&7&8&C&D&2з&x&E&8&8&3&C&5а&x&D&8&7&A&B&8к &7(I уровень)",
         "&x&f&7&8&c&d&2"
      );
   }

   public ItemStack get2(int count) {
      return this.createBackpack(
         2,
         count,
         Material.LIGHT_BLUE_SHULKER_BOX,
         "&x&3&5&B&0&C&BР&x&3&9&B&C&D&9ю&x&3&C&C&8&E&7к&x&4&0&D&4&F&5з&x&3&B&C&2&E&0а&x&3&5&B&0&C&Bк &7(II уровень)",
         "&x&4&0&D&4&F&5"
      );
   }

   public ItemStack get3(int count) {
      return this.createBackpack(
         3,
         count,
         Material.RED_SHULKER_BOX,
         "&x&D&3&4&2&4&2Р&x&D&F&4&6&4&6ю&x&E&A&4&9&4&9к&x&F&6&4&D&4&Dз&x&E&5&4&8&4&8а&x&D&3&4&2&4&2к &7(III уровень)",
         "&x&F&6&4&D&4&D"
      );
   }

   public ItemStack get4(int count) {
      return this.createBackpack(
         4,
         count,
         Material.MAGENTA_SHULKER_BOX,
         "&x&C&3&0&B&A&5Р&x&D&3&0&C&B&3ю&x&E&3&0&C&C&0к&x&F&3&0&D&C&Eз&x&D&B&0&C&B&Aа&x&C&3&0&B&A&5к &7(IV уровень)",
         "&x&F&3&0&D&C&E"
      );
   }

   public ItemStack get5(int count) {
      return this.createBackpack(
         5,
         count,
         Material.LIME_SHULKER_BOX,
         "&x&2&B&C&F&0&0&l&k- &x&2&B&C&F&0&0Р&x&2&B&C&F&0&0ю&x&2&B&C&F&0&0к&x&2&B&C&F&0&0з&x&2&B&C&F&0&0а&x&2&B&C&F&0&0к &x&3&0&F&2&0&0&lI&x&2&F&E&A&0&0&lɴ&x&2&D&E&1&0&0&lғ&x&2&B&D&8&0&0&lɪ&x&2&9&C&F&0&0&lɴ&x&2&8&C&7&0&0&lɪ&x&2&6&B&E&0&0&lᴛ&x&2&4&B&5&0&0&lʏ &x&2&B&C&F&0&0&l&k-",
         "&x&2&B&C&F&0&0"
      );
   }

   private void removeRecipes() {
      for (int i = 1; i <= 5; i++) {
         NamespacedKey key = new NamespacedKey(this, "backpack_level_" + i);
         if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
         }
      }
   }

   private void registerRecipes() {
      ShapedRecipe level1 = new ShapedRecipe(new NamespacedKey(this, "backpack_level_1"), this.get1(1));
      level1.shape(new String[]{"NNN", "NSN", "NNN"});
      level1.setIngredient('N', Material.NETHERITE_BLOCK);
      level1.setIngredient(
         'S',
         new MaterialChoice(
            new Material[]{
               Material.SHULKER_BOX,
               Material.WHITE_SHULKER_BOX,
               Material.ORANGE_SHULKER_BOX,
               Material.MAGENTA_SHULKER_BOX,
               Material.LIGHT_BLUE_SHULKER_BOX,
               Material.YELLOW_SHULKER_BOX,
               Material.LIME_SHULKER_BOX,
               Material.PINK_SHULKER_BOX,
               Material.GRAY_SHULKER_BOX,
               Material.LIGHT_GRAY_SHULKER_BOX,
               Material.CYAN_SHULKER_BOX,
               Material.PURPLE_SHULKER_BOX,
               Material.BLUE_SHULKER_BOX,
               Material.BROWN_SHULKER_BOX,
               Material.GREEN_SHULKER_BOX,
               Material.RED_SHULKER_BOX,
               Material.BLACK_SHULKER_BOX
            }
         )
      );
      Bukkit.addRecipe(level1);
      ShapedRecipe level2 = new ShapedRecipe(new NamespacedKey(this, "backpack_level_2"), this.get2(1));
      level2.shape(new String[]{"NNN", "NBN", "NNN"});
      level2.setIngredient('N', Material.NETHERITE_BLOCK);
      level2.setIngredient('B', new MaterialChoice(Material.PINK_SHULKER_BOX));
      Bukkit.addRecipe(level2);
      ShapedRecipe level3 = new ShapedRecipe(new NamespacedKey(this, "backpack_level_3"), this.get3(1));
      level3.shape(new String[]{"NNN", "NBN", "NNN"});
      level3.setIngredient('N', Material.NETHERITE_BLOCK);
      level3.setIngredient('B', new MaterialChoice(Material.LIGHT_BLUE_SHULKER_BOX));
      Bukkit.addRecipe(level3);
      ShapedRecipe level4 = new ShapedRecipe(new NamespacedKey(this, "backpack_level_4"), this.get4(1));
      level4.shape(new String[]{"NNN", "NBN", "NNN"});
      level4.setIngredient('N', Material.NETHERITE_BLOCK);
      level4.setIngredient('B', new MaterialChoice(Material.RED_SHULKER_BOX));
      Bukkit.addRecipe(level4);
   }

   private byte[] serializeItems(List<ItemStack> items) {
      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(out);
         oos.writeInt(items.size());

         for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
               oos.writeObject(item.serializeAsBytes());
            } else {
               oos.writeObject(null);
            }
         }

         oos.close();
         return out.toByteArray();
      } catch (Exception var6) {
         Bukkit.getLogger().warning("Ошибка сериализации предметов: " + var6.getMessage());
         return new byte[0];
      }
   }

   private List<ItemStack> deserializeItems(byte[] data) {
      List<ItemStack> items = new ArrayList<>();

      try {
         ByteArrayInputStream in = new ByteArrayInputStream(data);
         ObjectInputStream ois = new ObjectInputStream(in);
         int size = ois.readInt();

         for (int i = 0; i < size; i++) {
            Object obj = ois.readObject();
            if (obj == null) {
               items.add(new ItemStack(Material.AIR));
            } else {
               byte[] itemData = (byte[])obj;
               ItemStack item = ItemStack.deserializeBytes(itemData);
               items.add(item != null ? item : new ItemStack(Material.AIR));
            }
         }

         ois.close();
      } catch (Exception var10) {
         Bukkit.getLogger().warning("Ошибка десериализации предметов: " + var10.getMessage());
      }

      return items;
   }

   private class BackPackCommand implements CommandExecutor {
      private BackPackCommand() {
      }

      public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length != 4) {
            sender.sendMessage("Использование: /holybackpack give [ник] [1/2/3/4/5] [количество]");
            return true;
         } else {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
               sender.sendMessage("Игрок не найден");
               return true;
            } else {
               int count = Integer.parseInt(args[3]);
               String var7 = args[2];
               switch (var7) {
                  case "1":
                     player.getInventory().addItem(new ItemStack[]{HolyBackPack.this.get1(count)});
                     break;
                  case "2":
                     player.getInventory().addItem(new ItemStack[]{HolyBackPack.this.get2(count)});
                     break;
                  case "3":
                     player.getInventory().addItem(new ItemStack[]{HolyBackPack.this.get3(count)});
                     break;
                  case "4":
                     player.getInventory().addItem(new ItemStack[]{HolyBackPack.this.get4(count)});
                     break;
                  case "5":
                     player.getInventory().addItem(new ItemStack[]{HolyBackPack.this.get5(count)});
               }

               return true;
            }
         }
      }
   }
}
