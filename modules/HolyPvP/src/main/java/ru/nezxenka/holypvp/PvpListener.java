package ru.nezxenka.holypvp;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PvpListener implements Listener {
   private final HolyPvP plugin;

   public PvpListener(HolyPvP plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      if (this.plugin.pvpHashMap.containsKey(event.getEntity())) {
         this.plugin.pvpHashMap.get(event.getEntity()).onDeath(event.getEntity());
      }
   }

   @EventHandler
   public void onDeath(PlayerKickEvent event) {
      if (!event.isCancelled()) {
         if (this.plugin.pvpHashMap.containsKey(event.getPlayer())) {
            Pvp pvp = this.plugin.pvpHashMap.get(event.getPlayer());
            this.plugin.pvpHashMap.remove(pvp.player1);
            this.plugin.pvpHashMap.remove(pvp.player2);
         }

         this.plugin.databaseManager.deletePlayerProfile(event.getPlayer().getName());
      }
   }

   @EventHandler
   public void onDeath(PlayerQuitEvent event) {
      if (this.plugin.pvpHashMap.containsKey(event.getPlayer())) {
         Pvp pvp = this.plugin.pvpHashMap.get(event.getPlayer());
         pvp.onDeath(event.getPlayer());
         this.plugin.pvpHashMap.remove(pvp.player1);
         this.plugin.pvpHashMap.remove(pvp.player2);
      }

      this.plugin.databaseManager.deletePlayerProfile(event.getPlayer().getName());
   }

   @EventHandler
   public void onAttack(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (this.plugin.pvpHashMap.containsKey(player) && !this.plugin.pvpHashMap.get(player).canPvp) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      if (event.getPlayer().getWorld().getName().equalsIgnoreCase(this.plugin.getConfig().getString("settings.settings.worlds.smertka", "deadly_fight"))) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + event.getPlayer().getName());
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView()
         .getTitle()
         .equalsIgnoreCase(
            "Поиск PvP                                                                                                                                                                                                                                                                            [holypvp:pvp]"
         )) {
         event.setCancelled(true);
         if (event.getCurrentItem() == null) {
            return;
         }

         if (event.getClickedInventory() == null) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING)) {
            if (event.getCurrentItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .get(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING)
               .equalsIgnoreCase("pvp")) {
               int cnt = 0;
               boolean hasHelmet = false;
               boolean hasChestplate = false;
               boolean hasLeggings = false;
               boolean hasBoots = false;

               for (ItemStack itemStack : player.getInventory()) {
                  if (itemStack != null) {
                     if (itemStack.getType() == Material.GOLDEN_APPLE) {
                        cnt += itemStack.getAmount();
                     } else if (itemStack.getType() == Material.NETHERITE_HELMET
                        || itemStack.getType() == Material.DIAMOND_HELMET
                        || itemStack.getType() == Material.GOLDEN_HELMET) {
                        hasHelmet = true;
                     } else if (itemStack.getType() == Material.NETHERITE_CHESTPLATE || itemStack.getType() == Material.DIAMOND_CHESTPLATE) {
                        hasChestplate = true;
                     } else if (itemStack.getType() == Material.NETHERITE_LEGGINGS || itemStack.getType() == Material.DIAMOND_LEGGINGS) {
                        hasLeggings = true;
                     } else if (itemStack.getType() == Material.NETHERITE_BOOTS || itemStack.getType() == Material.DIAMOND_BOOTS) {
                        hasBoots = true;
                     }
                  }
               }

               if (cnt < 15) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', "&c▶ &fВступить в игру можно, только имея в инвентаре, &cкак минимум, 15 золотых яблок.")
                  );
                  return;
               }

               if (!hasHelmet || !hasChestplate || !hasLeggings || !hasBoots) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&c▶ &fВступить в поединок можно только а настоящих доспехах! (нужна &6алмазная или незеритовая&f броня)"
                     )
                  );
                  return;
               }

               event.getWhoClicked().closeInventory();
               this.plugin
                  .databaseManager
                  .createPlayerProfile(
                     new PlayerProfile(player.getName(), this.plugin.configManager.getServerName(), PlayerProfile.FightType.DUEL, player.getInventory())
                  );
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(),
                  "litebans broadcast " + this.translateSearching(false, player.getName(), this.plugin.configManager.getServerName())
               );
            }

            if (event.getCurrentItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .get(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING)
               .equalsIgnoreCase("smert")) {
               int cnt = 0;
               boolean hasHelmet = false;
               boolean hasChestplate = false;
               boolean hasLeggings = false;
               boolean hasBoots = false;

               for (ItemStack itemStackx : player.getInventory()) {
                  if (itemStackx != null) {
                     if (itemStackx.getType() == Material.GOLDEN_APPLE) {
                        cnt += itemStackx.getAmount();
                     } else if (itemStackx.getType() == Material.NETHERITE_HELMET || itemStackx.getType() == Material.GOLDEN_HELMET) {
                        hasHelmet = true;
                     } else if (itemStackx.getType() == Material.NETHERITE_CHESTPLATE) {
                        hasChestplate = true;
                     } else if (itemStackx.getType() == Material.NETHERITE_LEGGINGS) {
                        hasLeggings = true;
                     } else if (itemStackx.getType() == Material.NETHERITE_BOOTS) {
                        hasBoots = true;
                     }
                  }
               }

               if (cnt < 15) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', "&c▶ &fВступить в игру можно, только имея в инвентаре, &cкак минимум, 15 золотых яблок.")
                  );
                  return;
               }

               if (!hasHelmet || !hasChestplate || !hasLeggings || !hasBoots) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&c▶ &fВступить в смертельную схватку можно только а настоящих доспехах! (нужна &6незеритовая&f броня)"
                     )
                  );
                  return;
               }

               event.getWhoClicked().closeInventory();
               this.plugin
                  .databaseManager
                  .createPlayerProfile(
                     new PlayerProfile(player.getName(), this.plugin.configManager.getServerName(), PlayerProfile.FightType.DEADLY_FIGHT, player.getInventory())
                  );
               Bukkit.dispatchCommand(
                  Bukkit.getConsoleSender(), "litebans broadcast " + this.translateSearching(true, player.getName(), this.plugin.configManager.getServerName())
               );
            }

            if (event.getCurrentItem()
               .getItemMeta()
               .getPersistentDataContainer()
               .get(new NamespacedKey(this.plugin, "update"), PersistentDataType.STRING)
               .equalsIgnoreCase("cancel")) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &fПоиск поединка отменен"));
               this.plugin.databaseManager.deletePlayerProfile(player.getName());
               player.closeInventory();
            }
         }
      } else if (event.getView()
         .getTitle()
         .equalsIgnoreCase(
            ChatColor.translateAlternateColorCodes(
               '&',
               "&0Доступные игры                                                                                                                                                                                                                                                                            [holypvp:list]"
            )
         )) {
         event.setCancelled(true);
         if (event.getCurrentItem() == null) {
            return;
         }

         if (event.getClickedInventory() == null) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "name"), PersistentDataType.STRING)) {
            try {
               PlayerProfile profile = this.plugin
                  .databaseManager
                  .getPlayerProfile(
                     event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this.plugin, "name"), PersistentDataType.STRING)
                  )
                  .get();
               if (profile.getNickname().equalsIgnoreCase(player.getName())) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        this.plugin.getConfig().getString("messages.deny.self", "&x&f&b&0&0&0&0 ▶ &fВы не можете начать &6поединок &x&f&b&0&0&0&0с собой!")
                     )
                  );
                  return;
               }

               if (!profile.getServer().equalsIgnoreCase(this.plugin.configManager.getServerName())) {
                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&x&0&0&D&8&F&F ▶ &fДля начала поединка перейдите на сервер: &x&0&0&D&8&F&F" + profile.getServer()
                     )
                  );
                  player.closeInventory();
                  return;
               }

               if (profile == null) {
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&0&0&0 ▶ &fНевозможно загрузить данные этого поединка"));
                  player.closeInventory();
                  return;
               }

               if (Bukkit.getPlayer(profile.getNickname()) == null) {
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&0&0&0 ▶ &fНевозможно загрузить данные этого поединка"));
                  player.closeInventory();
                  return;
               }

               if (event.getClick().isLeftClick()) {
                  if (profile.getFightType() != PlayerProfile.FightType.DUEL) {
                     int cnt = 0;
                     boolean hasHelmet = false;
                     boolean hasChestplate = false;
                     boolean hasLeggings = false;
                     boolean hasBoots = false;

                     for (ItemStack itemStackxx : player.getInventory()) {
                        if (itemStackxx != null) {
                           if (itemStackxx.getType() == Material.GOLDEN_APPLE) {
                              cnt += itemStackxx.getAmount();
                           } else if (itemStackxx.getType() == Material.NETHERITE_HELMET || itemStackxx.getType() == Material.GOLDEN_HELMET) {
                              hasHelmet = true;
                           } else if (itemStackxx.getType() == Material.NETHERITE_CHESTPLATE) {
                              hasChestplate = true;
                           } else if (itemStackxx.getType() == Material.NETHERITE_LEGGINGS) {
                              hasLeggings = true;
                           } else if (itemStackxx.getType() == Material.NETHERITE_BOOTS) {
                              hasBoots = true;
                           }
                        }
                     }

                     if (cnt < 15) {
                        player.sendMessage(
                           ChatColor.translateAlternateColorCodes('&', "&c▶ &fВступить в игру можно, только имея в инвентаре, &cкак минимум, 15 золотых яблок.")
                        );
                        return;
                     }

                     if (hasHelmet && hasChestplate && hasLeggings && hasBoots) {
                        event.getWhoClicked().closeInventory();
                        Player target = Bukkit.getPlayer(profile.getNickname());
                        Pvp pvp = new Pvp(this.plugin, player, target, true);
                        this.plugin.pvpHashMap.put(player, pvp);
                        this.plugin.pvpHashMap.put(target, pvp);
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &fСоперник найден, &6ожидайте..."));
                        return;
                     }

                     player.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                           '&', "&c▶ &fВступить в смертельную схватку можно только а настоящих доспехах! (нужна &6незеритовая&f броня)"
                        )
                     );
                     return;
                  }

                  int cnt = 0;
                  boolean hasHelmet = false;
                  boolean hasChestplate = false;
                  boolean hasLeggings = false;
                  boolean hasBoots = false;

                  for (ItemStack itemStackxxx : player.getInventory()) {
                     if (itemStackxxx != null) {
                        if (itemStackxxx.getType() == Material.GOLDEN_APPLE) {
                           cnt += itemStackxxx.getAmount();
                        } else if (itemStackxxx.getType() == Material.NETHERITE_HELMET
                           || itemStackxxx.getType() == Material.DIAMOND_HELMET
                           || itemStackxxx.getType() == Material.GOLDEN_HELMET) {
                           hasHelmet = true;
                        } else if (itemStackxxx.getType() == Material.NETHERITE_CHESTPLATE || itemStackxxx.getType() == Material.DIAMOND_CHESTPLATE) {
                           hasChestplate = true;
                        } else if (itemStackxxx.getType() == Material.NETHERITE_LEGGINGS || itemStackxxx.getType() == Material.DIAMOND_LEGGINGS) {
                           hasLeggings = true;
                        } else if (itemStackxxx.getType() == Material.NETHERITE_BOOTS || itemStackxxx.getType() == Material.DIAMOND_BOOTS) {
                           hasBoots = true;
                        }
                     }
                  }

                  if (cnt < 15) {
                     player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', "&c▶ &fВступить в игру можно, только имея в инвентаре, &cкак минимум, 15 золотых яблок.")
                     );
                     return;
                  }

                  if (hasHelmet && hasChestplate && hasLeggings && hasBoots) {
                     event.getWhoClicked().closeInventory();
                     Player target = Bukkit.getPlayer(profile.getNickname());
                     Pvp pvp = new Pvp(this.plugin, player, target, false);
                     this.plugin.pvpHashMap.put(player, pvp);
                     this.plugin.pvpHashMap.put(target, pvp);
                     target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &fСоперник найден, &6ожидайте..."));
                     return;
                  }

                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes(
                        '&', "&c▶ &fВступить в поединок можно только а настоящих доспехах! (нужна &6алмазная или незеритовая&f броня)"
                     )
                  );
                  return;
               }

               if (event.getClick().isRightClick()) {
                  Inventory inventory = Bukkit.createInventory(
                     (InventoryHolder)null,
                     54,
                     ChatColor.translateAlternateColorCodes(
                        '&',
                        "Просмотр инвентаря                                                                                                                                                                                                                                                                            [holypvp:invsee]"
                     )
                  );
                  if (profile.getInventory() == null) {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&0&0&0 ▶ &fДанные инвентаря не загрузились вовремя."));
                     player.closeInventory();
                     return;
                  }

                  inventory.setContents(profile.getInventory().getContents());
                  ItemStack itemStackxxxx = new ItemStack(Material.ARROW);
                  ItemMeta itemMeta = itemStackxxxx.getItemMeta();
                  itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&5&F&B&0&0▶ Назад"));
                  itemStackxxxx.setItemMeta(itemMeta);
                  inventory.setItem(53, itemStackxxxx);
                  player.openInventory(inventory);
               }
            } catch (Exception var16) {
            }
         }
      } else if (event.getView()
         .getTitle()
         .equalsIgnoreCase(
            ChatColor.translateAlternateColorCodes(
               '&',
               "Просмотр инвентаря                                                                                                                                                                                                                                                                            [holypvp:invsee]"
            )
         )) {
         event.setCancelled(true);
         if (event.getCurrentItem() == null) {
            return;
         }

         if (event.getClickedInventory() == null) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         if (event.getSlot() == 53) {
            new PvpList(this.plugin, player);
         }
      }
   }

   @EventHandler
   public void onTp(PlayerTeleportEvent event) {
      if (this.plugin.pvpHashMap.containsKey(event.getPlayer())) {
         Pvp pvp = this.plugin.pvpHashMap.get(event.getPlayer());
         if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            if (pvp.teleportTask != null && !pvp.teleportTask.isCancelled()) {
               pvp.teleportTask.cancel();
            } else {
               pvp.stopTp(event.getPlayer());
            }

            this.plugin.pvpHashMap.remove(event.getPlayer());
            return;
         }

         if (!pvp.death) {
            return;
         }

         Location location = pvp.center;
         Location to = event.getTo();
         Location teleport = to.clone();
         Boolean flag = false;
         if (Math.abs(to.getX() - location.getX()) > 50.0) {
            flag = true;
            teleport.setX(to.getX() - location.getX() > 0.0 ? location.getX() + 49.5 : location.getX() - 49.5);
         }

         if (Math.abs(to.getZ() - location.getZ()) > 50.0) {
            flag = true;
            teleport.setZ(to.getZ() - location.getZ() > 0.0 ? location.getZ() + 49.5 : location.getZ() - 49.5);
         }

         if (flag) {
            double y = teleport.getWorld().getHighestBlockYAt(teleport.getBlockX(), teleport.getBlockZ());
            teleport.setY(y + 1.0);
            event.setTo(teleport);
         }
      }
   }

   @EventHandler
   public void onTp(PlayerMoveEvent event) {
      if (this.plugin.pvpHashMap.containsKey(event.getPlayer())) {
         Pvp pvp = this.plugin.pvpHashMap.get(event.getPlayer());
         if (!pvp.death) {
            return;
         }

         Location location = pvp.center;
         Location to = event.getTo();
         Location teleport = to.clone();
         Boolean flag = false;
         if (Math.abs(to.getX() - location.getX()) > 50.0) {
            flag = true;
            teleport.setX(to.getX() - location.getX() > 0.0 ? location.getX() + 49.5 : location.getX() - 49.5);
         }

         if (Math.abs(to.getZ() - location.getZ()) > 50.0) {
            flag = true;
            teleport.setZ(to.getZ() - location.getZ() > 0.0 ? location.getZ() + 49.5 : location.getZ() - 49.5);
         }

         if (flag) {
            double y = teleport.getWorld().getHighestBlockYAt(teleport.getBlockX(), teleport.getBlockZ());
            teleport.setY(y + 1.0);
            event.setTo(teleport);
         }
      }
   }

   public String translateSearching(Boolean smertka, String player, String server) {
      List<String> list = this.plugin.getConfig().getStringList("messages.searching.pvp");
      if (smertka) {
         list = this.plugin.getConfig().getStringList("messages.searching.smertka");
      }

      String random = list.get(new Random().nextInt(list.size()));

      while (random.contains("%player%")) {
         random = random.replace("%player%", player);
      }

      while (random.contains("%server%")) {
         random = random.replace("%server%", server);
      }

      return random;
   }
}
