package org.dimasik.eventmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EventVote implements Listener, CommandExecutor {
   private final EventManager plugin;
   private HashMap<String, EventVote.Event> allEvents = new HashMap<>();
   private HashMap<String, EventVote.Event> events = new HashMap<>();
   private HashMap<UUID, String> playerVotes = new HashMap<>();
   public boolean isVoting = false;
   private Inventory voteMenu;
   private BukkitTask bukkitTask;
   private BukkitTask timerUpdateTask;
   private String invName = "Голосование";
   private int timer;
   private int votingTimeLeft = 120;

   public EventVote(EventManager plugin) {
      this.plugin = plugin;
      this.loadEvents();
   }

   private void loadEvents() {
      FileConfiguration config = this.plugin.getConfig();
      ConfigurationSection itemsSection = config.getConfigurationSection("items");
      if (itemsSection != null) {
         this.allEvents.clear();

         for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection eventSection = itemsSection.getConfigurationSection(key);
            if (eventSection != null) {
               EventVote.Event event = new EventVote.Event(
                  eventSection.getString("material"),
                  eventSection.getString("displayname"),
                  eventSection.getStringList("lore"),
                  eventSection.getBoolean("glow"),
                  eventSection.getStringList("commands"),
                  eventSection.getString("name")
               );
               this.allEvents.put(key, event);
            }
         }
      }
   }

   public void fillEvents() {
      this.events.clear();
      List<String> eventKeys = new ArrayList<>(this.allEvents.keySet());
      if (eventKeys.size() < 2) {
         throw new IllegalStateException("Недостаточно событий для голосования. Нужно как минимум 2 события.");
      } else {
         int numberOfEvents = Math.min(4, eventKeys.size());
         numberOfEvents = Math.min(new Random().nextInt(3) + 3, numberOfEvents);
         Collections.shuffle(eventKeys);

         for (int i = 0; i < numberOfEvents; i++) {
            String eventKey = eventKeys.get(i);
            this.events.put(eventKey, this.allEvents.get(eventKey));
         }
      }
   }

   public void startVoting() {
      if (!this.isVoting) {
         this.isVoting = true;
         this.votingTimeLeft = 120;

         for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&c&6&f&0&0 &n◢&r &fЗапущено голосование за следующее мероприятие."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&c&6&f&0&0 &l◤&f &x&f&c&6&f&0&0Прописывай /vote, &fчтобы принять участие."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
         }

         this.loadEvents();
         this.fillEvents();
         this.playerVotes.clear();
         this.createVoteMenu();
         this.timerUpdateTask = (new BukkitRunnable() {
            public void run() {
               if (EventVote.this.isVoting && EventVote.this.votingTimeLeft > 0) {
                  EventVote.this.votingTimeLeft = EventVote.this.votingTimeLeft - 1;
                  EventVote.this.updateVoteMenu();
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 20L);
         this.bukkitTask = (new BukkitRunnable() {
            public void run() {
               EventVote.this.finishVoting();
            }
         }).runTaskLater(this.plugin, 2400L);
      }
   }

   private void stopVoting() {
      if (this.isVoting) {
         this.isVoting = false;
         this.playerVotes.clear();
         if (this.timerUpdateTask != null) {
            this.timerUpdateTask.cancel();
            this.timerUpdateTask = null;
         }

         Iterator<HumanEntity> iterator = this.voteMenu.getViewers().iterator();

         while (iterator.hasNext()) {
            HumanEntity humanEntity = iterator.next();
            humanEntity.closeInventory();
            iterator.remove();
         }
      }
   }

   private void finishVoting() {
      if (this.isVoting) {
         this.isVoting = false;
         this.bukkitTask = null;
         if (this.timerUpdateTask != null) {
            this.timerUpdateTask.cancel();
            this.timerUpdateTask = null;
         }

         if (this.voteMenu != null) {
            for (HumanEntity player : new ArrayList(this.voteMenu.getViewers())) {
               player.closeInventory();
            }
         }

         HashMap<String, Integer> voteCounts = new HashMap<>();
         Map<String, EventVote.Event> eventsCopy = new HashMap<>(this.events);

         for (Entry<String, EventVote.Event> event : eventsCopy.entrySet()) {
            voteCounts.put(event.getKey(), event.getValue().vote);
         }

         String winningEvent = voteCounts.entrySet().stream().max(Entry.comparingByValue()).map(Entry::getKey).orElse(null);
         if (winningEvent != null) {
            final EventVote.Event event = this.events.get(winningEvent);
            if (event != null) {
               String color = event.displayName.substring(0, 14);
               if (!color.substring(0, 3).equalsIgnoreCase("&x&")) {
                  color = "&x&F&F&B&C&3&B";
               }

               String message;
               label136: {
                  String nameevent = event.getName();
                  switch (nameevent.hashCode()) {
                     case -1112711993:
                        if (nameevent.equals("deathmine")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           break label136;
                        }
                        break;
                     case -836759239:
                        if (nameevent.equals("svetpolana")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           break label136;
                        }
                        break;
                     case -410956671:
                        if (nameevent.equals("container")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           break label136;
                        }
                        break;
                     case -391526899:
                        if (nameevent.equals("posilka")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           Iterator var26 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var26.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var26.next();
                              player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0F, 1.0F);
                           }
                        }
                        break;
                     case 3029869:
                        if (nameevent.equals("boss")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&',
                              color
                                 + " ▶ "
                                 + event.displayName
                                 + " &fпоявится через "
                                 + color
                                 + "минуту &fна пвп арене &x&9&F&0&0&F&F(&x&9&B&0&1&F&5/&x&9&6&0&2&E&Bw&x&9&2&0&2&E&1a&x&8&E&0&3&D&7r&x&8&9&0&4&C&Dp &x&8&0&0&6&B&Ap&x&7&C&0&7&B&0v&x&7&8&0&7&A&6p&x&7&3&0&8&9&C)&x&6&F&0&9&9&2."
                           );
                           Iterator var25 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var25.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var25.next();
                              player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 1.0F);
                           }
                        }
                        break;
                     case 3181776:
                        if (nameevent.equals("gruz")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           Iterator var24 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var24.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var24.next();
                              player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0F, 1.0F);
                           }
                        }
                        break;
                     case 3529276:
                        if (nameevent.equals("ship")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           Iterator var23 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var23.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var23.next();
                              player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0F, 1.0F);
                           }
                        }
                        break;
                     case 110367596:
                        if (nameevent.equals("tipo ")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                           );
                           Iterator var22 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var22.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var22.next();
                              player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 1.0F, 1.0F);
                           }
                        }
                        break;
                     case 1476540602:
                        if (nameevent.equals("zamokgoldrush")) {
                           message = ChatColor.translateAlternateColorCodes(
                              '&', color + " ▶ " + event.displayName + " &fначнется на координатах " + color + "0 63 0 &fуже через " + color + "минуту."
                           );
                           Iterator var10 = Bukkit.getOnlinePlayers().iterator();

                           while (true) {
                              if (!var10.hasNext()) {
                                 break label136;
                              }

                              Player player = (Player)var10.next();
                              player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
                           }
                        }
                  }

                  message = ChatColor.translateAlternateColorCodes(
                     '&', color + " ▶ " + event.displayName + " &fпоявится через " + color + "минуту &fна рандомных координатах."
                  );

                  for (Player player : Bukkit.getOnlinePlayers()) {
                     player.playSound(player.getLocation(), Sound.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 1.0F, 1.0F);
                  }
               }

               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ""));
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ""));
               this.plugin.events.put("wait_event", new org.dimasik.eventmanager.Event(event.displayName, message));
               (new BukkitRunnable() {
                  public void run() {
                     EventVote.this.plugin.events.remove("wait_event");
                     String command = event.getCommands().get(new Random().nextInt(event.getCommands().size()));
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                  }
               }).runTaskLater(this.plugin, 1200L);
            }
         }
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() == EventManager.eventMenyHolder) {
         if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
            return;
         }

         event.setCancelled(true);
         ItemStack itemStack = event.getCurrentItem();
         if (itemStack.getItemMeta() == null) {
            return;
         }

         PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
         if (pdc == null) {
            return;
         }

         String eventName = (String)pdc.get(new NamespacedKey(this.plugin, "event-name"), PersistentDataType.STRING);
         if (eventName == null) {
            return;
         }

         EventVote.Event clickedEvent = this.events.get(eventName);
         if (clickedEvent == null) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         if (this.playerVotes.get(player.getUniqueId()) != null) {
            String previousVote = this.playerVotes.get(player.getUniqueId());
            EventVote.Event previousEvent = this.events.get(previousVote);
            if (previousEvent != null) {
               previousEvent.vote--;
            }
         }

         this.playerVotes.put(player.getUniqueId(), eventName);
         clickedEvent.vote++;
         this.updateVoteMenu();
      }
   }

   private void createVoteMenu() {
      this.voteMenu = Bukkit.createInventory(EventManager.eventMenyHolder, 27, this.invName + " [" + this.formatTime(this.votingTimeLeft) + "]");
      this.updateVoteMenu();
   }

   private void updateVoteMenu() {
      String newTitle = this.invName + " [" + this.formatTime(this.votingTimeLeft) + "]";
      Inventory newMenu = Bukkit.createInventory(EventManager.eventMenyHolder, 27, newTitle);
      int slot;
      if (this.events.size() == 2) {
         slot = 12;
      } else if (this.events.size() == 3) {
         slot = 11;
      } else {
         slot = 10;
      }

      for (Entry<String, EventVote.Event> entry : this.events.entrySet()) {
         EventVote.Event event = entry.getValue();

         try {
            ItemStack item = new ItemStack(Material.valueOf(event.getMaterial().toUpperCase()));
            ItemMeta meta = item.getItemMeta();
            if (event.glow) {
               meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               meta.addEnchant(Enchantment.LUCK, 1, false);
            }

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', event.getDisplayName()));
            List<String> lore = new ArrayList<>();

            for (String line : event.getLore()) {
               lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%amount%", String.valueOf(event.vote))));
            }

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "event-name"), PersistentDataType.STRING, entry.getKey());
            item.setItemMeta(meta);
            newMenu.setItem(slot, item);
            slot += 2;
         } catch (IllegalArgumentException var12) {
            this.plugin.getLogger().info(event.getMaterial().toUpperCase());
         }
      }

      List<Player> viewers = new ArrayList<>();
      if (this.voteMenu != null) {
         for (HumanEntity viewer : this.voteMenu.getViewers()) {
            if (viewer instanceof Player) {
               viewers.add((Player)viewer);
            }
         }
      }

      this.voteMenu = newMenu;

      for (Player player : viewers) {
         player.openInventory(this.voteMenu);
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("vote")) {
         if (args.length == 0) {
            if (sender instanceof Player) {
               Player player = (Player)sender;
               if (this.isVoting) {
                  player.openInventory(this.voteMenu);
               } else {
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&8&0&e&0&1▶ &fВ настоящее время голосование не проводится."));
               }
            }

            return true;
         }

         if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
               if (sender.hasPermission("mantivote.admin")) {
                  this.startVoting();
                  sender.sendMessage("Голосование начато.");
               } else {
                  Player player = (Player)sender;
                  if (this.isVoting) {
                     player.openInventory(this.voteMenu);
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&8&0&e&0&1▶ &fВ настоящее время голосование не проводится."));
                  }
               }

               return true;
            }

            if (args[0].equalsIgnoreCase("stop")) {
               if (sender.hasPermission("mantivote.admin")) {
                  this.stopVoting();
                  sender.sendMessage("Голосование остановлено.");
               } else {
                  Player player = (Player)sender;
                  if (this.isVoting) {
                     player.openInventory(this.voteMenu);
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&8&0&e&0&1▶ &fВ настоящее время голосование не проводится."));
                  }
               }

               return true;
            }

            if (args[0].equalsIgnoreCase("finish")) {
               if (sender.hasPermission("mantivote.admin")) {
                  this.finishVoting();
                  sender.sendMessage("Голосование завершено.");
               } else {
                  Player player = (Player)sender;
                  if (this.isVoting) {
                     player.openInventory(this.voteMenu);
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&8&0&e&0&1▶ &fВ настоящее время голосование не проводится."));
                  }
               }

               return true;
            }
         }
      }

      return true;
   }

   private String formatTime(int seconds) {
      int minutes = seconds / 60;
      int remainingSeconds = seconds % 60;
      return minutes + " мин, " + remainingSeconds + " сек.";
   }

   private class Event {
      private String material;
      private String displayName;
      private List<String> lore;
      private boolean glow;
      private List<String> commands;
      public int vote = 0;
      private String name;

      public Event(String material, String displayName, List<String> lore, boolean glow, List<String> commands, String name) {
         this.material = material;
         this.displayName = displayName;
         this.lore = lore;
         this.glow = glow;
         this.commands = commands;
         this.name = name;
      }

      public String getMaterial() {
         return this.material;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public List<String> getLore() {
         return this.lore;
      }

      public boolean isGlow() {
         return this.glow;
      }

      public List<String> getCommands() {
         return this.commands;
      }

      public String getName() {
         return this.name;
      }
   }
}
