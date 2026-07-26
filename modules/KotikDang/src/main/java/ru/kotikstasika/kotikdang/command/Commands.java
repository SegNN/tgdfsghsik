package ru.kotikstasika.kotikdang.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.DangType;
import ru.kotikstasika.kotikdang.dang.items.Items;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;
import ru.kotikstasika.kotikdang.dang.types.DangNoRg;
import ru.kotikstasika.kotikdang.dang.types.DangUniversal;
import ru.kotikstasika.kotikdang.model.GriefDangData;
import ru.kotikstasika.kotikdang.service.GriefDangService;
import ru.kotikstasika.kotikdang.util.ColorUtil;
import ru.kotikstasika.kotikdang.util.LocationAndSpawnUtil;
import ru.kotikstasika.kotikdang.util.WorldGuardUtils;

public class Commands implements CommandExecutor, TabCompleter {
   private final KotikDang pluginInstance;
   private final Items items;
   private final DangUniversal dangUniversal;
   private final DangGrief dangGrief;
   private final DangNoRg dangNoRg;
   private final Random random;
   private final WorldGuardUtils worldGuardUtils = new WorldGuardUtils();
   private final GriefDangService griefDangService;

   public Commands(KotikDang pluginInstance, Items items, DangUniversal dangUniversal, DangGrief dangGrief) {
      this.pluginInstance = pluginInstance;
      this.items = items;
      this.random = new Random();
      this.dangNoRg = new DangNoRg(KotikDang.plugin);
      this.dangUniversal = dangUniversal;
      this.dangGrief = dangGrief;
      this.griefDangService = new GriefDangService(dangGrief);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("kotikdang.admin")) {
         sender.sendMessage("§6▶ §cУ Вас нет прав!");
         return true;
      } else if (!command.getName().equalsIgnoreCase("kotikdang")) {
         return true;
      } else if (args.length == 0) {
         this.sendMessageHelp(sender);
         return true;
      } else {
         String subcommand = args[0].toLowerCase();

         return switch (subcommand) {
            case "spawn" -> this.spawnCommand(sender, args);
            case "spawnhere" -> this.spawnHereCommand(sender, args);
            case "give" -> this.handleGiveCommand(sender, args);
            case "remove" -> this.removeCommand(sender, args);
            case "restore" -> this.restoreCommand(sender, args);
            case "list" -> this.listCommand(sender, args);
            case "tp" -> this.tpCommand(sender, args);
            case "addloc" -> this.addUniqueLocation(sender, args);
            case "respawn" -> this.respawnAllUnique(sender);
            case "stop" -> this.stopUniqueSession(sender, args);
            case "help" -> {
               this.sendMessageHelp(sender);
               yield true;
            }
            default -> {
               this.sendMessageHelp(sender);
               yield true;
            }
         };
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("kotikdang.admin")) {
         return Collections.emptyList();
      } else if (!command.getName().equalsIgnoreCase("kotikdang")) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         String currentArg = args[0].toLowerCase();
         return (List<String>)StringUtil.copyPartialMatches(
            currentArg, Arrays.asList("spawn", "spawnhere", "give", "remove", "restore", "list", "tp", "addloc", "respawn", "stop", "help"), new ArrayList()
         );
      } else {
         String subcommand = args[0].toLowerCase();
         String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

         return (List<String>)(switch (subcommand) {
            case "spawn" -> this.handleSpawnTabComplete(args, currentArg);
            case "spawnhere" -> this.handleSpawnHereTabComplete(args, currentArg);
            case "give" -> this.handleGiveTabComplete(args, currentArg);
            case "remove" -> this.handleRemoveTabComplete(args, currentArg);
            case "restore" -> this.handleRestoreTabComplete(args, currentArg);
            case "list" -> this.handleListTabComplete(args, currentArg);
            case "tp" -> this.handleTpTabComplete(args, currentArg);
            case "stop" -> args.length == 2
               ? (ArrayList)StringUtil.copyPartialMatches(
                  currentArg, Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList()), new ArrayList()
               )
               : Collections.emptyList();
            default -> Collections.emptyList();
         });
      }
   }

   private List<String> handleSpawnTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("norg", "grief"), new ArrayList());
      } else if (args.length == 3) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("1", "5", "10"), new ArrayList());
      } else {
         return args.length == 4 ? (List)StringUtil.copyPartialMatches(currentArg, Bukkit.getWorlds().stream().map(w -> {
            String name = w.getName();
            if (w.getEnvironment() == Environment.NETHER) {
               return name + " (АД)";
            } else {
               return w.getEnvironment() == Environment.THE_END ? name + " (ЭНД)" : name;
            }
         }).collect(Collectors.toList()), new ArrayList()) : Collections.emptyList();
      }
   }

   private List<String> handleSpawnHereTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("norg", "grief"), new ArrayList());
      } else {
         if (args.length == 3) {
            String type = args[1].toLowerCase();
            ConfigurationSection section = null;
            if ("grief".equals(type)) {
               section = KotikDang.plugin.getConfig().getConfigurationSection("dangs.grief");
            } else if ("norg".equals(type)) {
               section = KotikDang.plugin.getConfig().getConfigurationSection("dangs.no_rg");
            }

            if (section != null) {
               return (List<String>)StringUtil.copyPartialMatches(currentArg, new ArrayList(section.getKeys(false)), new ArrayList());
            }
         }

         return Collections.emptyList();
      }
   }

   private List<String> handleGiveTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("unic", "compass"), new ArrayList());
      } else {
         return args.length == 3
            ? (List)StringUtil.copyPartialMatches(
               currentArg, Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList()), new ArrayList()
            )
            : Collections.emptyList();
      }
   }

   private List<String> handleRemoveTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("grief", "allgrief"), new ArrayList());
      } else {
         return args.length == 3 && "grief".equalsIgnoreCase(args[1])
            ? (List)StringUtil.copyPartialMatches(currentArg, Arrays.asList("all", "loc", "restore"), new ArrayList())
            : Collections.emptyList();
      }
   }

   private List<String> handleRestoreTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("grief"), new ArrayList());
      } else if (args.length == 3 && "grief".equalsIgnoreCase(args[1])) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("loc", "id"), new ArrayList());
      } else {
         return args.length == 4 && "grief".equalsIgnoreCase(args[1]) && "id".equalsIgnoreCase(args[2])
            ? (List)StringUtil.copyPartialMatches(currentArg, new ArrayList<>(this.pluginInstance.getDangGriefInstance().getLoadedDangIds()), new ArrayList())
            : Collections.emptyList();
      }
   }

   private List<String> handleListTabComplete(String[] args, String currentArg) {
      return args.length == 2 ? (List)StringUtil.copyPartialMatches(currentArg, Arrays.asList("grief"), new ArrayList()) : Collections.emptyList();
   }

   private List<String> handleTpTabComplete(String[] args, String currentArg) {
      if (args.length == 2) {
         return (List<String>)StringUtil.copyPartialMatches(currentArg, Arrays.asList("grief"), new ArrayList());
      } else {
         return args.length == 3 && "grief".equalsIgnoreCase(args[1])
            ? (List)StringUtil.copyPartialMatches(currentArg, new ArrayList<>(this.pluginInstance.getDangGriefInstance().getLoadedDangIds()), new ArrayList())
            : Collections.emptyList();
      }
   }

   private boolean addUniqueLocation(CommandSender sender, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("§6▶ §fКоманда только для игроков!");
         return true;
      } else {
         Location location = player.getLocation().getBlock().getLocation();
         if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &f/kotikdang addloc <название_уника>"));
            return true;
         } else {
            String unicalName = args[1];
            this.dangUniversal.addLocation(location, unicalName);
            sender.sendMessage(ColorUtil.colorize("&#00FF00▶ &fЛокация уника добавлена и шалкер создан!"));
            return true;
         }
      }
   }

   private boolean respawnAllUnique(CommandSender sender) {
      this.dangUniversal.respawnAllShulkers();
      sender.sendMessage(ColorUtil.colorize("&#00FF00▶ &fВсе уники зареспавнены!"));
      return true;
   }

   private boolean stopUniqueSession(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &f/kotikdang stop <игрок>"));
         return true;
      } else {
         Player target = Bukkit.getPlayer(args[1]);
         if (target == null) {
            sender.sendMessage(ColorUtil.colorize("&#FB1F1F▶ &fИгрок не найден!"));
            return true;
         } else {
            this.dangUniversal.stopSession(target);
            sender.sendMessage(ColorUtil.colorize("&#00FF00▶ &fОткрытие остановлено для игрока " + target.getName() + "!"));
            return true;
         }
      }
   }

   private boolean spawnCommand(final CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang spawn <norg|grief> <кол-во> [мир]");
         return true;
      } else {
         final String type = args[1].toLowerCase();
         if (!Arrays.asList("norg", "grief").contains(type)) {
            sender.sendMessage("§6▶ §fНеверный §cтип §fданжа! §bДоступно: norg, grief");
            return true;
         } else {
            final int count;
            try {
               count = Integer.parseInt(args[2]);
               if (count <= 0 || count > 100) {
                  sender.sendMessage("§6▶ §fКоличество §bдолжно§f быть от §c1 §fдо §c100§f!");
                  return true;
               }
            } catch (NumberFormatException var6) {
               sender.sendMessage("§6▶ §fКоличество §bдолжно§f быть числом!");
               return true;
            }

            final World world = args.length >= 4
               ? this.parseWorld(args[3])
               : (sender instanceof Player ? ((Player)sender).getWorld() : Bukkit.getWorld("world"));
            if (world == null) {
               sender.sendMessage("§6▶ §fМир не найден!");
               return true;
            } else {
               sender.sendMessage("§6▶ §fПоиск подходящих мест...");
               (new BukkitRunnable() {
                  int spawned = 0;

                  public void run() {
                     if (this.spawned >= count) {
                        this.cancel();
                     } else {
                        String var1 = type;
                        switch (var1) {
                           case "norg":
                              Commands.this.spawnNoRgAsync(sender, world, this.spawned == count - 1);
                              break;
                           case "grief":
                              Commands.this.spawnGriefAsync(sender, world, this.spawned == count - 1);
                        }

                        this.spawned++;
                     }
                  }
               }).runTaskTimer(KotikDang.plugin, 0L, 5L);
               return true;
            }
         }
      }
   }

   private boolean removeCommand(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang remove grief <all|loc|restore> §7или §b/kotikdang remove allgrief");
         return true;
      } else if ("allgrief".equalsIgnoreCase(args[1])) {
         return this.handleRemoveAllGrief(sender);
      } else if (!"grief".equalsIgnoreCase(args[1])) {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang remove grief <all|loc|restore> §7или §b/kotikdang remove allgrief");
         return true;
      } else {
         String mode = args.length >= 3 ? args[2].toLowerCase() : "all";
         return this.handleRemoveGrief(sender, mode);
      }
   }

   private boolean handleRemoveAllGrief(CommandSender sender) {
      if (!this.dangGrief.isDatabaseConnected()) {
         sender.sendMessage("§6▶ §cБаза данных недоступна!");
         return true;
      } else {
         int removed = 0;

         for (String id : new ArrayList<>(this.pluginInstance.getDangGriefInstance().getLoadedDangIds())) {
            GriefDangData data = this.pluginInstance.getDangGriefInstance().getDangData(id);
            if (data != null) {
               try {
                  this.worldGuardUtils.removeRegion("kotikdang_grief_" + id, data.getLocation());
               } catch (Exception var8) {
               }

               try {
                  this.pluginInstance.getDangGriefInstance().deleteDangAndRegion(id);
               } catch (Exception var7) {
               }

               removed++;
            }
         }

         sender.sendMessage("§6▶ §fУдалено grief-данжей (без отката): §b" + removed);
         return true;
      }
   }

   private boolean handleRemoveGrief(CommandSender sender, String mode) {
      return switch (mode) {
         case "all" -> {
            int removed = this.pluginInstance.removeGrifAlready();
            sender.sendMessage("§6▶ §fУдалено зарейженных данжей: §b" + removed);
            yield true;
         }
         case "loc" -> {
            if (!(sender instanceof Player player)) {
               sender.sendMessage("§6▶ §fТолько игрок может использовать режим loc");
               yield true;
            } else {
               boolean ok = this.griefDangService.deleteNearest(player.getLocation());
               if (!ok) {
                  sender.sendMessage("§6▶ §fРядом нет Grief данжей.");
                  yield true;
               } else {
                  sender.sendMessage("§6▶ §fБлижайший данж удален.");
                  yield true;
               }
            }
         }
         case "restore" -> {
            sender.sendMessage("§6▶ §fФункция восстановления территории пока не реализована.");
            yield true;
         }
         default -> {
            sender.sendMessage("§6▶ §fИспользование: §b/kotikdang remove grief <all|loc|restore>");
            yield true;
         }
      };
   }

   private boolean listCommand(CommandSender sender, String[] args) {
      if (args.length >= 2 && "grief".equalsIgnoreCase(args[1])) {
         Set<String> ids = this.pluginInstance.getDangGriefInstance().getLoadedDangIds();
         if (ids.isEmpty()) {
            sender.sendMessage("§6▶ §fНет активных гриффф-данжей");
            return true;
         } else {
            sender.sendMessage("§6▶ §fСписок активных гриффф-данжей (id): §b" + String.join(", ", ids));
            return true;
         }
      } else {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang list grief");
         return true;
      }
   }

   private boolean tpCommand(CommandSender sender, String[] args) {
      if (args.length < 3 || !"grief".equalsIgnoreCase(args[1])) {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang tp grief <id>");
         return true;
      } else if (!(sender instanceof Player)) {
         sender.sendMessage("§6▶ §fКоманда только для игроков");
         return true;
      } else {
         String id = args[2];
         Location loc = this.pluginInstance.getDangGriefInstance().getDangLocation(id);
         if (loc == null) {
            sender.sendMessage("§6▶ §fДанж с id §b" + id + " §fне найден");
            return true;
         } else {
            ((Player)sender).teleport(loc);
            sender.sendMessage("§6▶ §fТелепортировано к §b" + id + " §7(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
            return true;
         }
      }
   }

   private boolean restoreCommand(CommandSender sender, String[] args) {
      if (args.length >= 2 && "grief".equalsIgnoreCase(args[1])) {
         if (args.length == 2 || "loc".equalsIgnoreCase(args[2])) {
            return this.handleRestoreByLocation(sender);
         } else if ("id".equalsIgnoreCase(args[2])) {
            return this.handleRestoreById(sender, args);
         } else {
            sender.sendMessage("§6▶ §fИспользование: §b/kotikdang restore grief <loc|id> [id]");
            return true;
         }
      } else {
         sender.sendMessage("§6▶ §fИспользование: §b/kotikdang restore grief <loc|id> [id]");
         return true;
      }
   }

   private boolean handleRestoreByLocation(CommandSender sender) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("§6▶ §fРежим loc доступен только игроку");
         return true;
      } else {
         String nearest = this.pluginInstance.getDangGriefInstance().findNearestDangId(player.getLocation());
         if (nearest == null) {
            sender.sendMessage("§6▶ §fРядом нет данных для восстановления.");
            return true;
         } else {
            boolean ok = this.pluginInstance.getDangGriefInstance().restoreDangById(nearest);
            sender.sendMessage(ok ? "§6▶ §fУчасток восстановлен." : "§6▶ §cНе удалось восстановить участок.");
            return true;
         }
      }
   }

   private boolean handleRestoreById(CommandSender sender, String[] args) {
      if (args.length < 4) {
         sender.sendMessage("§6▶ §fУкажите id: §b/kotikdang restore grief id <id>");
         return true;
      } else {
         String id = args[3];
         boolean ok = this.pluginInstance.getDangGriefInstance().restoreDangById(id);
         sender.sendMessage(ok ? "§6▶ §fУчасток восстановлен: §b" + id : "§6▶ §cНе удалось восстановить участок.");
         return true;
      }
   }

   private boolean spawnHereCommand(CommandSender sender, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("§6▶ §fКоманда только для игроков!");
         return true;
      } else {
         Location location = player.getLocation();
         if (args.length < 2) {
            sender.sendMessage("§6▶ §fИспользование: §b/kotikdang spawnhere <norg|grief> [название]");
            return true;
         } else {
            String typeStr = args[1].toLowerCase();
            if (!Arrays.asList("norg", "grief").contains(typeStr)) {
               sender.sendMessage("§6▶ §fНеверный §cтип §fданжа! §bДоступно: norg, grief");
               return true;
            } else {
               return switch (typeStr) {
                  case "norg" -> this.handleSpawnHereNoRg(sender, location, args);
                  case "grief" -> this.handleSpawnHereGrief(sender, location, args);
                  default -> true;
               };
            }
         }
      }
   }

   private boolean handleSpawnHereNoRg(CommandSender sender, Location location, String[] args) {
      ConfigurationSection norgSection = KotikDang.plugin.getConfig().getConfigurationSection("dangs.no_rg");
      if (norgSection != null && !norgSection.getKeys(false).isEmpty()) {
         String norgDang = args.length >= 3 ? args[2] : new ArrayList<>(norgSection.getKeys(false)).get(this.random.nextInt(norgSection.getKeys(false).size()));
         if (!norgSection.contains(norgDang)) {
            sender.sendMessage("§6▶ §cNoRg данж '" + norgDang + "' не найден в конфиге!");
            return true;
         } else {
            if (this.dangNoRg.spawnNoRgDang(location, true, norgDang)) {
               sender.sendMessage("§6▶ §fNoRg данж §b" + norgDang + " §fсоздан на вашем месте!");
            } else {
               sender.sendMessage("§6▶ §cНе удалось создать NoRg данж!");
            }

            return true;
         }
      } else {
         sender.sendMessage("§6▶ §cНет настроенных NoRg данжей в конфиге!");
         return true;
      }
   }

   private boolean handleSpawnHereGrief(CommandSender sender, Location location, String[] args) {
      ConfigurationSection griefSection = KotikDang.plugin.getConfig().getConfigurationSection("dangs.grief");
      if (griefSection != null && !griefSection.getKeys(false).isEmpty()) {
         String griefDang = args.length >= 3
            ? args[2]
            : new ArrayList<>(griefSection.getKeys(false)).get(this.random.nextInt(griefSection.getKeys(false).size()));
         if (!griefSection.contains(griefDang)) {
            sender.sendMessage("§6▶ §cGrief данж '" + griefDang + "' не найден в конфиге!");
            return true;
         } else {
            int level = griefSection.getConfigurationSection(griefDang).getInt("lvlgrief", 1);
            if (this.dangGrief.spawnGriefDang(location, true, griefDang)) {
               sender.sendMessage("§6▶ §fGrief данж §b" + griefDang + " §f(уровень " + level + ") создан на вашем месте!");
            } else {
               sender.sendMessage("§6▶ §cНе удалось создать Grief данж!");
            }

            return true;
         }
      } else {
         sender.sendMessage("§6▶ §cНет настроенных Grief данжей в конфиге!");
         return true;
      }
   }

   private boolean handleGiveCommand(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.sendGiveUsage(sender);
         return true;
      } else {
         String itemType = args[1].toLowerCase();
         if (!Arrays.asList("unic", "compass").contains(itemType)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &cНеверный &fтип предмета! &bДоступно: unic, compass&f!\n "));
            return true;
         } else {
            Player target;
            if (args.length >= 3) {
               target = Bukkit.getPlayer(args[2]);
               if (target == null) {
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &cИгрок не найден&f!\n "));
                  return true;
               }
            } else if (!(sender instanceof Player)) {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &cУкажите игрока&f!\n "));
               return true;
            } else {
               target = (Player)sender;
            }

            if ("unic".equals(itemType)) {
               this.items.giveUnick(target);
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &b" + target.getName() + " &fполучил &cуник&f!\n "));
            } else if ("compass".equals(itemType)) {
               this.items.giveCompass(target);
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &b" + target.getName() + " &fполучил &cкомпаа&f!\n "));
            }

            return true;
         }
      }
   }

   private void sendGiveUsage(CommandSender sender) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&6▶ &fИспользование: &b/kotikdang give <unic/compass> [игрок]&f!\n "));
   }

   private void sendMessageHelp(CommandSender sender) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang give <unic/compass> [player] &8- &fВыдать предмет игроку"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang spawn <norg|grief> <count> [world] &8- &fСоздать данжи"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang spawnhere <norg|grief> [level] &8- &fСоздать данж здесь"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang addloc <уник> &8- &fДобавить локацию шалкера"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang respawn &8- &fЗареспавнить все уники"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang stop <игрок> &8- &fОстановить открытие"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang remove grief <all|loc|restore> &8- &fУдалить гриффф-данжи"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang list grief &8- &fСписок данжей"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang tp grief <id> &8- &fТелепортироваться"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &b/kotikdang help &8- &fПомощь"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
   }

   private World parseWorld(String worldName) {
      String cleanName = worldName.split("\\s+")[0];
      World world = Bukkit.getWorld(cleanName);
      if (world == null) {
         if (worldName.toUpperCase().contains("АД") || worldName.toUpperCase().contains("NETHER")) {
            return Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.NETHER).findFirst().orElse(null);
         }

         if (worldName.toUpperCase().contains("ЭНД") || worldName.toUpperCase().contains("END")) {
            return Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END).findFirst().orElse(null);
         }
      }

      return world;
   }

   private void spawnNoRgAsync(CommandSender sender, World world, boolean isLast) {
      LocationAndSpawnUtil.findSuitableLocation(world, DangType.NO_RG, 100)
         .thenAccept(
            loc -> {
               if (loc == null) {
                  sender.sendMessage("§6▶ §fНе удалось найти место для NoRg данжа");
               } else {
                  ConfigurationSection noRgSection = KotikDang.plugin.getConfig().getConfigurationSection("dangs.no_rg");
                  if (noRgSection != null && !noRgSection.getKeys(false).isEmpty()) {
                     List<String> dangKeys = new ArrayList<>(noRgSection.getKeys(false));
                     final String selectedDang = dangKeys.get(this.random.nextInt(dangKeys.size()));
                     (new BukkitRunnable() {
                           public void run() {
                              if (Commands.this.dangNoRg.spawnNoRgDang(loc, false, selectedDang)) {
                                 String message = "§6▶ §fСоздан NoRg данж §b"
                                    + selectedDang
                                    + " §fв мире §b"
                                    + world.getName()
                                    + " §fна координатах §b"
                                    + loc.getBlockX()
                                    + " "
                                    + loc.getBlockY()
                                    + " "
                                    + loc.getBlockZ();
                                 if (isLast) {
                                    message = message + "\n§6▶ §fВсе данжи успешно созданы!";
                                 }

                                 sender.sendMessage(message);
                              }
                           }
                        })
                        .runTask(KotikDang.plugin);
                  } else {
                     sender.sendMessage("§6▶ §cНет настроенных NoRg данжей в конфиге!");
                  }
               }
            }
         );
   }

   private void spawnGriefAsync(CommandSender sender, World world, boolean isLast) {
      LocationAndSpawnUtil.findSuitableLocation(world, DangType.GRIEF, 5)
         .thenAccept(
            loc -> {
               if (loc == null) {
                  sender.sendMessage("§6▶ §fНе удалось найти место для Grief данжа");
               } else {
                  ConfigurationSection griefConfig = KotikDang.plugin.getConfig().getConfigurationSection("dangs.grief");
                  if (griefConfig != null && !griefConfig.getKeys(false).isEmpty()) {
                     List<String> dangKeys = new ArrayList<>(griefConfig.getKeys(false));
                     final String selectedDang = dangKeys.get(this.random.nextInt(dangKeys.size()));
                     final int level = griefConfig.getConfigurationSection(selectedDang).getInt("lvlgrief", 1);
                     (new BukkitRunnable() {
                           public void run() {
                              if (Commands.this.dangGrief.spawnGriefDang(loc, false, selectedDang)) {
                                 String message = "§6▶ §fСоздан Grief данж §b"
                                    + selectedDang
                                    + " §f(уровень §b"
                                    + level
                                    + "§f) в мире §b"
                                    + world.getName()
                                    + " §fна координатах §b"
                                    + loc.getBlockX()
                                    + " "
                                    + loc.getBlockY()
                                    + " "
                                    + loc.getBlockZ();
                                 if (isLast) {
                                    message = message + "\n§6▶ §fВсе данжи успешно созданы!";
                                 }

                                 sender.sendMessage(message);
                              }
                           }
                        })
                        .runTask(KotikDang.plugin);
                  } else {
                     sender.sendMessage("§6▶ §cНет настроенных Grief данжей в конфиге!");
                  }
               }
            }
         );
   }
}
