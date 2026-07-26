package ua.fichadev.fichacases.configuration;

import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.jdbi.v3.core.async.JdbiExecutor;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.animation.Animation;
import ua.fichadev.fichacases.animation.CircleAnimation;
import ua.fichadev.fichacases.animation.SpinningAnimation;
import ua.fichadev.fichacases.cases.CaseHolder;
import ua.fichadev.fichacases.cases.ConfigurableCase;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.cases.PurchasableCase;
import ua.fichadev.fichacases.database.dao.PlayerKeysDao;
import ua.fichadev.fichacases.database.dao.PrizeHistoryDao;
import ua.fichadev.fichacases.database.dao.entity.PrizeHistoryEntry;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.menu.Menu;
import ua.fichadev.fichacases.menu.MenuItem;
import ua.fichadev.fichacases.menu.MenuManager;
import ua.fichadev.fichacases.misc.ColorUtil;
import ua.fichadev.fichacases.misc.Messages;
import ua.fichadev.fichacases.opentype.Open;
import ua.fichadev.fichacases.opentype.impls.ClickOpen;
import ua.fichadev.fichacases.opentype.impls.DoubleClickOpen;
import ua.fichadev.fichacases.opentype.impls.ItemOpen;
import ua.fichadev.fichacases.opentype.impls.KeyOpen;
import ua.fichadev.fichacases.opentype.impls.PurchaseOpen;
import ua.fichadev.fichacases.particle.WrappedParticle;
import ua.fichadev.fichacases.reward.Prize;
import ua.fichadev.fichacases.reward.Reward;
import ua.fichadev.fichacases.util.ItemBuilder;

public class CasesConfig {
   private final FichaCases plugin;
   private final LuckManager luckManager;
   private final MenuManager menuManager;
   private JdbiExecutor executor;
   private final File rawConfig;
   private final YamlConfiguration config;

   public CasesConfig(FichaCases plugin, LuckManager luckManager, MenuManager menuManager, JdbiExecutor executor) {
      this.plugin = plugin;
      this.luckManager = luckManager;
      this.menuManager = menuManager;
      this.executor = executor;
      this.rawConfig = new File(plugin.getDataFolder(), "CasesConfig.yml");
      if (!this.rawConfig.exists()) {
         plugin.saveResource("CasesConfig.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(this.rawConfig);
   }

   public CasesConfig(FichaCases plugin, LuckManager luckManager, MenuManager menuManager) {
      this.plugin = plugin;
      this.luckManager = luckManager;
      this.menuManager = menuManager;
      this.rawConfig = new File(plugin.getDataFolder(), "CasesConfig.yml");
      if (!this.rawConfig.exists()) {
         plugin.saveResource("CasesConfig.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(this.rawConfig);
   }

   public Map<String, CaseHolder> getCases() {
      Map<String, CaseHolder> cases = new HashMap<>();
      ConfigurationSection casesSection = this.config.getConfigurationSection("cases");
      if (casesSection == null) {
         return cases;
      } else {
         Iterator var3 = casesSection.getKeys(false).iterator();

         while (true) {
            String caseName;
            ConfigurationSection caseSection;
            String caseType;
            Object open;
            label86:
            while (true) {
               if (!var3.hasNext()) {
                  return cases;
               }

               caseName = (String)var3.next();
               caseSection = this.config.getConfigurationSection("cases." + caseName);
               if (caseSection != null) {
                  caseType = caseSection.getString("case-type", "normal");
                  open = null;
                  if ("purchase".equalsIgnoreCase(caseType)) {
                     open = new PurchaseOpen(this.luckManager);
                     break;
                  }

                  ConfigurationSection openSection = caseSection.getConfigurationSection("open");
                  if (openSection == null) {
                     break;
                  }

                  try {
                     Open.Type openType = Open.Type.valueOf(openSection.getString("type").toUpperCase());
                     switch (openType) {
                        case KEY:
                           open = new KeyOpen(this.plugin, this.executor, this.luckManager);
                           break label86;
                        case RIGHT:
                        case LEFT:
                           open = this.getClickOpen(openSection, openType);
                           break label86;
                        case LEFTRIGHT:
                           ClickOpen leftClick = this.getClickOpen(openSection, Open.Type.LEFT);
                           ClickOpen rightClick = this.getClickOpen(openSection, Open.Type.RIGHT);
                           open = new DoubleClickOpen(leftClick, rightClick);
                           break label86;
                        case ITEM:
                           int amount = openSection.getInt("amount");
                           String failedBuySlot = openSection.getString("item-failed-buy-slot");
                           open = new ItemOpen(this.plugin, this.luckManager, amount, failedBuySlot);
                        default:
                           break label86;
                     }
                  } catch (Exception var74) {
                     this.plugin.getLogger().warning("Ошибка при обработке типа открытия для кейса: " + caseName);
                  }
               }
            }

            if (open == null) {
               this.plugin.getLogger().warning("Не удалось создать открывалку для кейса: " + caseName);
            } else {
               String titleAboveCase = ColorUtil.parse(caseSection.getString("title-above-case", ""));
               double titleAboveCaseYOffset = caseSection.getDouble("title-above-case-y-offset", 0.0);
               boolean luckWork = caseSection.getBoolean("luck_work", true);
               String animationType = caseSection.getString("animation.type", "circle");
               boolean randomRewards = caseSection.getBoolean("circle-animation.random-rewards", false);
               double rewardRadius = caseSection.getDouble("circle-animation.reward-radius", 0.0);
               Sound chestOpenSound = this.parseSound(caseSection.getString("circle-animation.chest-open-sound"));
               Sound chestCloseSound = this.parseSound(caseSection.getString("circle-animation.chest-close-sound"));
               int startDelay = caseSection.getInt("circle-animation.start.delay", 0);
               int spawnInterval = caseSection.getInt("circle-animation.start.spawn-interval", 0);
               int rewardUpTicks = caseSection.getInt("circle-animation.open.reward-up-ticks", 0);
               double openRotateIntoPlaceAngle = caseSection.getDouble("circle-animation.open.rotate-into-place-angle", 0.0);
               double spinStartAnglePerTick = caseSection.getDouble("circle-animation.spin.start-angle-per-tick", 0.0);
               double spinMinimalAnglePerTick = caseSection.getDouble("circle-animation.spin.minimal-angle-per-tick", 0.0);
               int minimumSlowdownTicks = caseSection.getInt("circle-animation.spin.minimum-slowdown-ticks", 0);
               int maximumSlowdownTicks = caseSection.getInt("circle-animation.spin.maximum-slowdown-ticks", 0);
               int minimumSpinIdleTicks = caseSection.getInt("circle-animation.spin.minimum-idle-ticks", 0);
               int maximumSpinIdleTicks = caseSection.getInt("circle-animation.spin.maximum-idle-ticks", 0);
               int endDelay = caseSection.getInt("circle-animation.end.delay", 0);
               int rewardDownTicks = caseSection.getInt("circle-animation.end.down-ticks", 0);
               double endRotateIntoPlaceAngle = caseSection.getDouble("circle-animation.end.rotate-into-place-angle", 0.0);
               CircleAnimation circleAnimation = new CircleAnimation(
                  this.plugin,
                  randomRewards,
                  rewardRadius,
                  startDelay,
                  spawnInterval,
                  rewardUpTicks,
                  openRotateIntoPlaceAngle,
                  spinStartAnglePerTick,
                  spinMinimalAnglePerTick,
                  minimumSlowdownTicks,
                  maximumSlowdownTicks,
                  minimumSpinIdleTicks,
                  maximumSpinIdleTicks,
                  endDelay,
                  rewardDownTicks,
                  endRotateIntoPlaceAngle,
                  chestOpenSound,
                  chestCloseSound
               );
               double liftHeight = caseSection.getDouble("spinning-animation.lift-height", 0.0);
               int liftDuration = caseSection.getInt("spinning-animation.lift-duration", 0);
               int spinDuration = caseSection.getInt("spinning-animation.spin-duration", 0);
               int changeInterval = caseSection.getInt("spinning-animation.change-interval", 0);
               int dropDuration = caseSection.getInt("spinning-animation.drop-duration", 0);
               Sound liftSound = this.parseSound(caseSection.getString("spinning-animation.lift-sound"));
               Sound dropSound = this.parseSound(caseSection.getString("spinning-animation.drop-sound"));
               SpinningAnimation spinningAnimation = new SpinningAnimation(
                  this.plugin, liftHeight, liftDuration, spinDuration, changeInterval, dropDuration, liftSound, dropSound
               );
               Object animation;
               if ("circle".equalsIgnoreCase(animationType)) {
                  animation = circleAnimation;
               } else if ("spinning".equalsIgnoreCase(animationType)) {
                  animation = spinningAnimation;
               } else {
                  animation = circleAnimation;
               }

               Messages messages = new Messages();
               ConfigurationSection messagesSection = caseSection.getConfigurationSection("messages");
               if (messagesSection != null) {
                  for (String key : messagesSection.getKeys(false)) {
                     messages.addMessage(key, messagesSection.getStringList(key));
                  }
               }

               List<Reward> rewards = new ArrayList<>();
               List<String> prizeMessage = caseSection.getStringList("prize-messages").stream().map(ColorUtil::parse).collect(Collectors.toList());
               ConfigurationSection rewardsSection = caseSection.getConfigurationSection("rewards");
               if (rewardsSection != null) {
                  for (String rewardName : rewardsSection.getKeys(false)) {
                     Material material = this.parseMaterial(rewardsSection.getString(rewardName + ".material"));
                     Color color = rewardsSection.getColor(rewardName + ".color");
                     boolean glow = rewardsSection.getBoolean(rewardName + ".glow", false);
                     String name = ColorUtil.parse(rewardsSection.getString(rewardName + ".name", ""));
                     Map<String, Double> chances = new HashMap<>();
                     if (rewardsSection.contains(rewardName + ".chances")) {
                        ConfigurationSection chancesSection = rewardsSection.getConfigurationSection(rewardName + ".chances");
                        if (chancesSection != null) {
                           for (String luck : chancesSection.getKeys(false)) {
                              double weight = rewardsSection.getDouble(rewardName + ".chances." + luck);
                              chances.put(luck, weight);
                           }
                        }
                     }

                     boolean sendTitle = rewardsSection.getBoolean(rewardName + ".send-title", false);
                     boolean addToHistory = rewardsSection.getBoolean(rewardName + ".add-to-history", false);
                     Reward reward = new Reward(name, chances, material, color, glow, sendTitle, addToHistory);
                     ConfigurationSection prizesSection = rewardsSection.getConfigurationSection(rewardName + ".prizes");
                     if (prizesSection != null) {
                        for (String prize : prizesSection.getKeys(false)) {
                           String prizeName = ColorUtil.parse(prizesSection.getString(prize + ".name", ""));
                           String title = ColorUtil.parse(prizesSection.getString(prize + ".title.title", ""));
                           String subtitle = ColorUtil.parse(prizesSection.getString(prize + ".title.subtitle", ""));
                           List<String> commands = prizesSection.getStringList(prize + ".commands");
                           Map<String, Double> prizeChances = new HashMap<>();
                           if (prizesSection.contains(prize + ".chances")) {
                              ConfigurationSection prizeChancesSection = prizesSection.getConfigurationSection(prize + ".chances");
                              if (prizeChancesSection != null) {
                                 for (String luck : prizeChancesSection.getKeys(false)) {
                                    double weight = prizesSection.getDouble(prize + ".chances." + luck);
                                    prizeChances.put(luck, weight);
                                 }
                              }
                           }

                           reward.addPrize(new Prize(prizeName, title, subtitle, commands), prizeChances);
                        }
                     }

                     rewards.add(reward);
                  }
               }

               List<WrappedParticle> particles = new ArrayList<>();
               ConfigurationSection particlesSection = caseSection.getConfigurationSection("particles");
               if (particlesSection != null) {
                  for (String particlesKey : particlesSection.getKeys(false)) {
                     Particle particle = this.parseParticle(particlesSection.getString(particlesKey + ".particle"));
                     int count = particlesSection.getInt(particlesKey + ".count", 0);
                     double speed = particlesSection.getDouble(particlesKey + ".speed", 0.0);
                     double xOffset = particlesSection.getDouble(particlesKey + ".xOffset", 0.0);
                     double yOffset = particlesSection.getDouble(particlesKey + ".yOffset", 0.0);
                     double zOffset = particlesSection.getDouble(particlesKey + ".zOffset", 0.0);
                     int red = particlesSection.getInt(particlesKey + ".r", 0);
                     int green = particlesSection.getInt(particlesKey + ".g", 0);
                     int blue = particlesSection.getInt(particlesKey + ".b", 0);
                     WrappedParticle wrappedParticle = new WrappedParticle(particle, count, xOffset, yOffset, zOffset, speed, red, green, blue);
                     particles.add(wrappedParticle);
                  }
               }

               int expPrice = caseSection.getInt("purchase-settings.exp-price", 100);
               int sapphirePrice = caseSection.getInt("purchase-settings.sapphire-price", 10);
               String notEnoughExpMessage = ColorUtil.parse(
                  caseSection.getString("purchase-settings.not-enough-exp-message", "&cУ вас недостаточно опыта! Нужно: %price%")
               );
               String notEnoughSapphireMessage = ColorUtil.parse(
                  caseSection.getString("purchase-settings.not-enough-sapphire-message", "&cУ вас недостаточно сапфиров! Нужно: %price%")
               );
               CaseHolder caseHolder = new CaseHolder(this.plugin, caseName);

               for (String wrapped : caseSection.getStringList("locations")) {
                  String[] args = wrapped.split(" ");
                  if (args.length >= 5) {
                     Supplier<World> world = () -> Bukkit.getWorld(args[0]);
                     int x = Integer.parseInt(args[1]);
                     int y = Integer.parseInt(args[2]);
                     int z = Integer.parseInt(args[3]);
                     Supplier<Location> location = () -> new Location(world.get(), x, y, z);
                     BlockFace blockFace = BlockFace.valueOf(args[4]);
                     Pair<String, Pair<Supplier<Location>, BlockFace>> position = Pair.of(args[0], Pair.of(location, blockFace));
                     Object finalOpen = open;
                     Object finalOpen1 = open;
                     ConfigurationSection finalCaseSection = caseSection;
                     String finalCaseName = caseName;
                     Function<Player, Menu> menuCreator = player -> {
                        ConfigurationSection menuSection = finalCaseSection.getConfigurationSection("menu");
                        if (menuSection == null) {
                           return null;
                        } else {
                           String menuTitle = ColorUtil.parse(menuSection.getString("title", ""));
                           int menuSize = menuSection.getInt("size", 54);
                           String cooldownType = menuSection.getString("cooldown.type", "GLOBAL");
                           int cooldownTime = menuSection.getInt("cooldown.time", 10);
                           Menu caseMenu = this.menuManager.createMenu(menuTitle, menuSize);
                           caseMenu.setCooldown(cooldownType, cooldownTime);
                           caseMenu.onClick(e -> e.setCancelled(true));
                           Map<String, ItemStack> itemTypes = new HashMap<>();
                           if (this.plugin.isSQL()) {
                              this.executor
                                 .useExtension(
                                    PlayerKeysDao.class,
                                    dao -> {
                                       ConfigurationSection itemTypesSectionx = menuSection.getConfigurationSection("item-types");
                                       if (itemTypesSectionx != null) {
                                          List<CompletableFuture<Void>> futures = new ArrayList<>();

                                          for (String typeKeyx : itemTypesSectionx.getKeys(false)) {
                                             UUID playerUUID = player.getUniqueId();
                                             CompletableFuture<Integer> keysFuture = CompletableFuture.supplyAsync(() -> dao.getKeys(playerUUID, finalCaseName));
                                             CompletableFuture<String> luckFuture = CompletableFuture.supplyAsync(() -> this.luckManager.getLuckLines(player));
                                             CompletableFuture<Void> combinedFuture = keysFuture.thenCombine(
                                                luckFuture,
                                                (keysx, lucksx) -> {
                                                   ItemStack itemStackx = ItemBuilder.fromConfig(
                                                         itemTypesSectionx.getConfigurationSection(typeKeyx),
                                                         text -> PlaceholderAPI.setPlaceholders(
                                                            player,
                                                            ColorUtil.parse(text.replace("%player_keys%", String.valueOf(keysx)).replace("%lucks%", lucksx))
                                                         )
                                                      )
                                                      .build();
                                                   itemTypes.put(typeKeyx, itemStackx);
                                                   return null;
                                                }
                                             );
                                             futures.add(combinedFuture);
                                          }

                                          CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                                       }
                                    }
                                 )
                                 .whenComplete((v, ex) -> this.setupMenuInventory(menuSection, caseMenu, itemTypes, position, finalOpen, finalCaseName));
                           } else {
                              ConfigurationSection itemTypesSection = menuSection.getConfigurationSection("item-types");
                              if (itemTypesSection != null) {
                                 for (String typeKey : itemTypesSection.getKeys(false)) {
                                    String keys = String.valueOf(this.plugin.getKeys(player, finalCaseName));
                                    String lucks = this.luckManager.getLuckLines(player);
                                    ItemStack itemStack = ItemBuilder.fromConfig(
                                          itemTypesSection.getConfigurationSection(typeKey),
                                          text -> PlaceholderAPI.setPlaceholders(
                                             player, ColorUtil.parse(text.replace("%player_keys%", keys).replace("%lucks%", lucks))
                                          )
                                       )
                                       .build();
                                    itemTypes.put(typeKey, itemStack);
                                 }
                              }

                              this.setupMenuInventory(menuSection, caseMenu, itemTypes, position, finalOpen1, finalCaseName);
                           }

                           return caseMenu;
                        }
                     };
                     Supplier<GameCase> configurableCase;
                     if ("purchase".equalsIgnoreCase(caseType)) {
                        configurableCase = () -> new PurchasableCase(
                           this.plugin,
                           this.executor,
                           finalCaseName,
                           titleAboveCase,
                           titleAboveCaseYOffset,
                           luckWork,
                           location.get(),
                           blockFace,
                           (Animation)animation,
                           Material.ENDER_CHEST,
                           particles,
                           rewards,
                           menuCreator,
                           prizeMessage,
                           messages,
                           expPrice,
                           sapphirePrice,
                           notEnoughExpMessage,
                           notEnoughSapphireMessage
                        );
                     } else {
                        configurableCase = () -> new ConfigurableCase(
                           this.plugin,
                           this.executor,
                           finalCaseName,
                           titleAboveCase,
                           titleAboveCaseYOffset,
                           luckWork,
                           location.get(),
                           blockFace,
                           (Animation)animation,
                           Material.ENDER_CHEST,
                           particles,
                           rewards,
                           menuCreator,
                           prizeMessage,
                           messages
                        );
                     }

                     caseHolder.addCreator(args[0], configurableCase);
                  }
               }

               cases.put(caseName, caseHolder);
            }
         }
      }
   }

   private void setupMenuInventory(
      ConfigurationSection menuSection,
      Menu caseMenu,
      Map<String, ItemStack> itemTypes,
      Pair<String, Pair<Supplier<Location>, BlockFace>> position,
      Object open,
      String caseName
   ) {
      List<String> wrappedInventory = menuSection.getStringList("inventory");

      for (int inventoryRow = 0; inventoryRow < wrappedInventory.size(); inventoryRow++) {
         String[] wrappedSlotItem = wrappedInventory.get(inventoryRow).split(" ");

         for (int index = 0; index < wrappedSlotItem.length; index++) {
            int inventorySlot = index + inventoryRow * 9;
            String itemType = wrappedSlotItem[index];
            ItemStack icon = itemTypes.get(itemType);
            MenuItem menuItem;
            if (itemType.equals("C")) {
               menuItem = this.menuManager
                  .createItem(
                     icon,
                     (whoClick, clickType) -> {
                        GameCase gameCase = (GameCase)((MetadataValue)((Location)((Supplier)((Pair)position.getSecond()).getFirst()).get())
                              .getBlock()
                              .getMetadata("case")
                              .get(0))
                           .value();
                        if (gameCase != null) {
                           if (gameCase.isOpening()) {
                              whoClick.closeInventory();
                              gameCase.getMessages().sendMessage(whoClick, "case-already-opening");
                           } else if (gameCase instanceof PurchasableCase purchasableCase) {
                              LuckManager luckManager = this.luckManager;
                              whoClick.closeInventory();
                              purchasableCase.openWithPurchase(whoClick, clickType, luckManager.removeLast(whoClick));
                           } else {
                              ((Open)open).open(gameCase, clickType, whoClick);
                           }
                        }
                     }
                  );
            } else {
               if (itemType.equals("W")) {
                  caseMenu.setItem(inventorySlot, this.menuManager.createItem(icon));
                  int historyCount = (inventoryRow + 1) * 9 - inventorySlot - 1;
                  if (this.plugin.isSQL()) {
                     this.executor
                        .withExtension(PrizeHistoryDao.class, dao -> dao.getLastPrizes(caseName, historyCount))
                        .whenComplete(
                           (historyx, ex1) -> {
                              int counterx = 1;
                              String prizeItemNamex = ColorUtil.parse(menuSection.getString("last-rewards.item-name", ""));
                              List<String> prizeItemLorex = menuSection.getStringList("last-rewards.item-lore")
                                 .stream()
                                 .map(ColorUtil::parse)
                                 .collect(Collectors.toList());
                              SimpleDateFormat fmtx = new SimpleDateFormat("dd MMM HH:mm:ss", new Locale("ru"));
                              fmtx.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));

                              for (PrizeHistoryEntry prizeHistoryEntryx : historyx) {
                                 String[] argsx = prizeHistoryEntryx.prizeMaterial().split(":");
                                 Color colorx = this.getColor(argsx);
                                 ItemStack prizeIconx = new ItemBuilder(Material.valueOf(argsx[0].toUpperCase()))
                                    .name(
                                       prizeItemNamex.replace("%player_name%", prizeHistoryEntryx.opener())
                                          .replace("%prize_name%", prizeHistoryEntryx.prizeName())
                                          .replace("%server_name%", prizeHistoryEntryx.serverName())
                                          .replace("%date%", fmtx.format(new Date(prizeHistoryEntryx.timestamp())))
                                          .replace("%luck%", prizeHistoryEntryx.luckName())
                                    )
                                    .lore(
                                       prizeItemLorex.stream()
                                          .map(
                                             line -> line.replace("%player_name%", prizeHistoryEntryx.opener())
                                                .replace("%prize_name%", prizeHistoryEntryx.prizeName())
                                                .replace("%server_name%", prizeHistoryEntryx.serverName())
                                                .replace("%date%", fmtx.format(new Date(prizeHistoryEntryx.timestamp())))
                                                .replace("%luck%", prizeHistoryEntryx.luckName())
                                          )
                                          .collect(Collectors.toList())
                                    )
                                    .color(colorx)
                                    .removeAttributes()
                                    .build();
                                 caseMenu.setItem(inventorySlot + counterx, this.menuManager.createItem(prizeIconx));
                                 counterx++;
                              }
                           }
                        );
                     continue;
                  }

                  Collection<PrizeHistoryEntry> history = this.plugin.getLastPrizes(caseName, historyCount);
                  int counter = 1;
                  String prizeItemName = ColorUtil.parse(menuSection.getString("last-rewards.item-name", ""));
                  List<String> prizeItemLore = menuSection.getStringList("last-rewards.item-lore").stream().map(ColorUtil::parse).collect(Collectors.toList());
                  SimpleDateFormat fmt = new SimpleDateFormat("dd MMM HH:mm:ss");
                  fmt.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));

                  for (PrizeHistoryEntry prizeHistoryEntry : history) {
                     String[] args = prizeHistoryEntry.prizeMaterial().split(":");
                     Color color = this.getColor(args);
                     ItemStack prizeIcon = new ItemBuilder(Material.valueOf(args[0].toUpperCase()))
                        .name(
                           prizeItemName.replace("%player_name%", prizeHistoryEntry.opener())
                              .replace("%prize_name%", prizeHistoryEntry.prizeName())
                              .replace("%server_name%", prizeHistoryEntry.serverName())
                              .replace("%date%", fmt.format(new Date(prizeHistoryEntry.timestamp())))
                              .replace("%luck%", prizeHistoryEntry.luckName())
                        )
                        .lore(
                           prizeItemLore.stream()
                              .map(
                                 line -> line.replace("%player_name%", prizeHistoryEntry.opener())
                                    .replace("%prize_name%", prizeHistoryEntry.prizeName())
                                    .replace("%server_name%", prizeHistoryEntry.serverName())
                                    .replace("%date%", fmt.format(new Date(prizeHistoryEntry.timestamp())))
                                    .replace("%luck%", prizeHistoryEntry.luckName())
                              )
                              .collect(Collectors.toList())
                        )
                        .color(color)
                        .removeAttributes()
                        .build();
                     caseMenu.setItem(inventorySlot + counter, this.menuManager.createItem(prizeIcon));
                     counter++;
                  }
                  continue;
               }

               menuItem = this.menuManager.createItem(icon);
            }

            if (caseMenu.getItem(inventorySlot) == null) {
               caseMenu.setItem(inventorySlot, menuItem);
            }
         }
      }
   }

   private Color getColor(String[] args) {
      Color color;
      if (args.length >= 4) {
         int red = Integer.parseInt(args[1]);
         int green = Integer.parseInt(args[2]);
         int blue = Integer.parseInt(args[3]);
         color = Color.fromRGB(red, green, blue);
      } else {
         color = null;
      }

      return color;
   }

   private Sound parseSound(String soundName) {
      if (soundName != null && !soundName.isEmpty()) {
         try {
            return Sound.valueOf(soundName);
         } catch (IllegalArgumentException var3) {
            return Sound.BLOCK_NOTE_BLOCK_PLING;
         }
      } else {
         return Sound.BLOCK_NOTE_BLOCK_PLING;
      }
   }

   private Material parseMaterial(String materialName) {
      if (materialName != null && !materialName.isEmpty()) {
         try {
            return Material.valueOf(materialName);
         } catch (IllegalArgumentException var3) {
            return Material.STONE;
         }
      } else {
         return Material.STONE;
      }
   }

   private Particle parseParticle(String particleName) {
      if (particleName != null && !particleName.isEmpty()) {
         try {
            return Particle.valueOf(particleName);
         } catch (IllegalArgumentException var3) {
            return Particle.CRIT;
         }
      } else {
         return Particle.CRIT;
      }
   }

   public void addCaseBlock(Block blockCase, String caseName) {
      Location pos = blockCase.getLocation();
      BlockFace dir = BlockFace.EAST;
      if (blockCase.getBlockData() instanceof Directional) {
         dir = ((Directional)blockCase.getBlockData()).getFacing();
      }

      String wrapped = pos.getWorld().getName() + " " + (int)pos.getX() + " " + (int)pos.getY() + " " + (int)pos.getZ() + " " + dir.name();
      List<String> locs = this.config.getStringList("cases." + caseName + ".locations");
      if (!locs.contains(wrapped)) {
         locs.add(wrapped);
      }

      this.config.set("cases." + caseName + ".locations", locs);
      this.save();
   }

   public void removeCaseBlock(Location location, String caseName) {
      List<String> locations = this.config.getStringList("cases." + caseName + ".locations");

      for (String wrapped : new ArrayList<>(locations)) {
         String[] args = wrapped.split(" ");
         if (args[0].equalsIgnoreCase(location.getWorld().getName())
            && args[1].equalsIgnoreCase(String.valueOf((int)location.getX()))
            && args[2].equalsIgnoreCase(String.valueOf((int)location.getY()))
            && args[3].equalsIgnoreCase(String.valueOf((int)location.getZ()))) {
            locations.remove(wrapped);
         }
      }

      this.config.set("cases." + caseName + ".locations", locations);
      this.save();
   }

   private void save() {
      try {
         this.config.save(this.rawConfig);
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   private void sync(Runnable runnable) {
      Bukkit.getScheduler().runTask(this.plugin, runnable);
   }

   private ClickOpen getClickOpen(ConfigurationSection openSection, Open.Type openType) {
      String click = openType.name().toLowerCase();
      int price = openSection.getInt(click + "-price");
      String priceType = openSection.getString(click + "-price-type");
      String commandChangePrice = openSection.getString(click + "-command-change-price");
      String failedBuySlot = openSection.getString(click + "-failed-buy-slot");
      return new ClickOpen(openType, this.plugin, this.luckManager, price, priceType, commandChangePrice, failedBuySlot);
   }

   public LuckManager getLuckManager() {
      return this.luckManager;
   }
}
