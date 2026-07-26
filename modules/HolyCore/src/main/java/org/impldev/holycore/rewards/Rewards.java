package org.impldev.holycore.rewards;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.impldev.holycore.HolyCore;
import org.impldev.holycore.database.DatabaseManager;

public class Rewards implements Listener {
   private final HolyCore plugin;
   private final DatabaseManager databaseManager;
   private final Set<UUID> playersWithEndReward = new HashSet<>();
   private final Set<UUID> playersWithNetherReward = new HashSet<>();
   private final Set<UUID> playersWithVillagerTradeReward = new HashSet<>();
   private final Set<UUID> playersWithBeaconReward = new HashSet<>();
   private final Set<UUID> playersWithHeroReward = new HashSet<>();
   private final Set<UUID> playersWithDiamondArmorReward = new HashSet<>();
   private final Set<UUID> playersWithNetheriteArmorReward = new HashSet<>();
   private final Set<UUID> playersWithAdventuringTimeReward = new HashSet<>();
   private final Set<UUID> playersWithTameAnimalReward = new HashSet<>();
   private final Set<UUID> playersWithBalancedDietReward = new HashSet<>();
   private final Set<UUID> playersWithReturnToSenderReward = new HashSet<>();
   private final Set<UUID> playersWithFollowEnderEyeReward = new HashSet<>();
   private final Set<UUID> playersWithLavaBucketReward = new HashSet<>();
   private final Set<UUID> playersWithCureZombieVillagerReward = new HashSet<>();
   private final Set<UUID> playersWithSleepInBedReward = new HashSet<>();
   private static final int END_REWARD_LIMIT = Integer.MAX_VALUE;
   private static final int NETHER_REWARD_LIMIT = 20;
   private static final int BEACON_REWARD_LIMIT = 20;
   private static final int VILLAGER_TRADE_REWARD_LIMIT = 20;
   private static final int HERO_REWARD_LIMIT = 20;
   private static final int DIAMOND_ARMOR_REWARD_LIMIT = 20;
   private static final int NETHERITE_ARMOR_REWARD_LIMIT = 20;
   private static final int ADVENTURING_TIME_REWARD_LIMIT = 20;
   private static final int TAME_ANIMAL_REWARD_LIMIT = 20;
   private static final int BALANCED_DIET_REWARD_LIMIT = 20;
   private static final int RETURN_TO_SENDER_REWARD_LIMIT = 20;
   private static final int FOLLOW_ENDER_EYE_REWARD_LIMIT = 20;
   private static final int LAVA_BUCKET_REWARD_LIMIT = 20;
   private static final int CURE_ZOMBIE_VILLAGER_REWARD_LIMIT = 20;
   private static final int SLEEP_IN_BED_REWARD_LIMIT = 20;

   public Rewards(HolyCore plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.END_PORTAL_FRAME && player.getInventory().getItemInMainHand().getType() == Material.ENDER_EYE) {
               EndPortalFrame frame = (EndPortalFrame)block.getBlockData();
               if (!frame.hasEye()) {
                  Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                     if (this.isEndPortalFullyActivated(block)) {
                        this.databaseManager.getRewardCountAsync("END").thenAcceptAsync(rewardCount -> (new BukkitRunnable() {
                           public void run() {
                              if (rewardCount < Integer.MAX_VALUE && !Rewards.this.playersWithEndReward.contains(player.getUniqueId())) {
                                 Rewards.this.giveEndReward(player);
                              }
                           }
                        }).runTask(this.plugin));
                     }
                  }, 2L);
               }
            }
         }
      }
   }

   @EventHandler
   public void onAdvancement(PlayerAdvancementDoneEvent event) {
      Player player = event.getPlayer();
      String advancementKey = event.getAdvancement().getKey().getKey();
      switch (advancementKey) {
         case "story/enter_the_nether":
            if (!this.playersWithNetherReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "NETHER", 20, this.playersWithNetherReward, this::giveNetherReward);
            }
            break;
         case "adventure/trade":
            if (!this.playersWithVillagerTradeReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "VILLAGER_TRADE", 20, this.playersWithVillagerTradeReward, this::giveVillagerTradeReward);
            }
            break;
         case "nether/create_beacon":
            if (!this.playersWithBeaconReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "BEACON", 20, this.playersWithBeaconReward, this::giveBeaconReward);
            }
            break;
         case "adventure/hero_of_the_village":
            if (!this.playersWithHeroReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "HERO", 20, this.playersWithHeroReward, this::giveHeroReward);
            }
            break;
         case "story/shiny_gear":
            if (!this.playersWithDiamondArmorReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "DIAMOND_ARMOR", 20, this.playersWithDiamondArmorReward, this::giveDiamondArmorReward);
            }
            break;
         case "nether/netherite_armor":
            if (!this.playersWithNetheriteArmorReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "NETHERITE_ARMOR", 20, this.playersWithNetheriteArmorReward, this::giveNetheriteArmorReward);
            }
            break;
         case "adventure/adventuring_time":
            if (!this.playersWithAdventuringTimeReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "ADVENTURING_TIME", 20, this.playersWithAdventuringTimeReward, this::giveAdventuringTimeReward);
            }
            break;
         case "husbandry/tame_an_animal":
            if (!this.playersWithTameAnimalReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "TAME_ANIMAL", 20, this.playersWithTameAnimalReward, this::giveTameAnimalReward);
            }
            break;
         case "husbandry/balanced_diet":
            if (!this.playersWithBalancedDietReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "BALANCED_DIET", 20, this.playersWithBalancedDietReward, this::giveBalancedDietReward);
            }
            break;
         case "nether/return_to_sender":
            if (!this.playersWithReturnToSenderReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "RETURN_TO_SENDER", 20, this.playersWithReturnToSenderReward, this::giveReturnToSenderReward);
            }
            break;
         case "story/follow_ender_eye":
            if (!this.playersWithFollowEnderEyeReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "FOLLOW_ENDER_EYE", 20, this.playersWithFollowEnderEyeReward, this::giveFollowEnderEyeReward);
            }
            break;
         case "story/lava_bucket":
            if (!this.playersWithLavaBucketReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "LAVA_BUCKET", 20, this.playersWithLavaBucketReward, this::giveLavaBucketReward);
            }
            break;
         case "story/cure_zombie_villager":
            if (!this.playersWithCureZombieVillagerReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "CURE_ZOMBIE_VILLAGER", 20, this.playersWithCureZombieVillagerReward, this::giveCureZombieVillagerReward);
            }
            break;
         case "adventure/sleep_in_bed":
            if (!this.playersWithSleepInBedReward.contains(player.getUniqueId())) {
               this.checkAndGiveReward(player, "SLEEP_IN_BED", 20, this.playersWithSleepInBedReward, this::giveSleepInBedReward);
            }
      }
   }

   private void checkAndGiveReward(Player player, String rewardType, int limit, Set<UUID> rewardSet, Rewards.RewardGiver rewardGiver) {
      this.databaseManager.getRewardCountAsync(rewardType).thenAccept(rewardCount -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         if (rewardCount < limit) {
            rewardGiver.give(player);
            rewardSet.add(player.getUniqueId());
         }
      }));
   }

   private boolean isEndPortalFullyActivated(Block frameBlock) {
      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            Block possibleCenter = frameBlock.getRelative(x, 0, z);
            if (this.checkPortalFromCenter(possibleCenter)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkPortalFromCenter(Block center) {
      int framesWithEyes = 0;
      int totalFrames = 0;
      int[][] framePositions = new int[][]{
         {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1}, {-2, 2}, {-1, -2}, {-1, 2}, {0, -2}, {0, 2}, {1, -2}, {1, 2}, {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}
      };

      for (int[] pos : framePositions) {
         Block frameBlock = center.getRelative(pos[0], 0, pos[1]);
         if (frameBlock.getType() == Material.END_PORTAL_FRAME) {
            totalFrames++;
            EndPortalFrame frameData = (EndPortalFrame)frameBlock.getBlockData();
            if (frameData.hasEye()) {
               framesWithEyes++;
            }
         }
      }

      return framesWithEyes >= 12;
   }

   private void giveEndReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 500 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§B§0§0§F§8▶ §fИгрок §x§F§B§0§0§F§8"
            + player.getName()
            + " §fактивировал портал в §x§F§B§0§0§F§8енд §fи получил награду в виде §x§F§B§0§0§F§8500 сапфиров ☀"
      );
      this.playersWithEndReward.add(player.getUniqueId());
      this.databaseManager.saveRewardAsync("END", player.getUniqueId().toString(), player.getName());
   }

   private void giveNetherReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 50 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§B§0§0§0§0▶ §fИгрок §x§F§B§0§0§0§0"
            + player.getName()
            + " §fактивировал портал в §x§F§B§0§0§0§0ад §fи получил награду в виде §x§F§B§0§0§0§050 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("NETHER", player.getUniqueId().toString(), player.getName());
   }

   private void giveVillagerTradeReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 100 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§B§9§5§0§0▶ §fИгрок §x§F§B§9§5§0§0"
            + player.getName()
            + " §fсовершил первую торговлю с жителем и получил §x§F§B§9§5§0§0100 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("VILLAGER_TRADE", player.getUniqueId().toString(), player.getName());
   }

   private void giveBeaconReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 200 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§0§0§F§F§0§0▶ §fИгрок §x§0§0§F§F§0§0" + player.getName() + " §fактивировал маяк и получил §x§0§0§F§F§0§0200 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("BEACON", player.getUniqueId().toString(), player.getName());
   }

   private void giveHeroReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 300 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§F§D§7§0§0▶ §fИгрок §x§F§F§D§7§0§0" + player.getName() + " §fстал Героем деревни и получил §x§F§F§D§7§0§0300 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("HERO", player.getUniqueId().toString(), player.getName());
   }

   private void giveDiamondArmorReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 150 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§0§0§F§F§F§F▶ §fИгрок §x§0§0§F§F§F§F"
            + player.getName()
            + " §fполучил полную алмазную броню и награду §x§0§0§F§F§F§F150 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("DIAMOND_ARMOR", player.getUniqueId().toString(), player.getName());
   }

   private void giveNetheriteArmorReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 400 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§4§4§4§4§4§4▶ §fИгрок §x§4§4§4§4§4§4"
            + player.getName()
            + " §fполучил полную незеритовую броню и награду §x§4§4§4§4§4§4400 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("NETHERITE_ARMOR", player.getUniqueId().toString(), player.getName());
   }

   private void giveAdventuringTimeReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 350 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§3§2§C§D§3§2▶ §fИгрок §x§3§2§C§D§3§2" + player.getName() + " §fисследовал все биомы и получил §x§3§2§C§D§3§2350 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("ADVENTURING_TIME", player.getUniqueId().toString(), player.getName());
   }

   private void giveTameAnimalReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 80 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§F§6§A§3§B▶ §fИгрок §x§F§F§6§A§3§B" + player.getName() + " §fприручил животное и получил §x§F§F§6§A§3§B80 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("TAME_ANIMAL", player.getUniqueId().toString(), player.getName());
   }

   private void giveBalancedDietReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 250 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§9§A§C§8§2§3▶ §fИгрок §x§9§A§C§8§2§3" + player.getName() + " §fпопробовал всю еду и получил §x§9§A§C§8§2§3250 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("BALANCED_DIET", player.getUniqueId().toString(), player.getName());
   }

   private void giveReturnToSenderReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 180 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§F§B§1§B§1▶ §fИгрок §x§F§F§B§1§B§1"
            + player.getName()
            + " §fубил гаста его же снарядом и получил §x§F§F§B§1§B§1180 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("RETURN_TO_SENDER", player.getUniqueId().toString(), player.getName());
   }

   private void giveFollowEnderEyeReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 120 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§9§5§0§0§F§F▶ §fИгрок §x§9§5§0§0§F§F"
            + player.getName()
            + " §fнашел крепость с помощью ока эндера и получил §x§9§5§0§0§F§F120 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("FOLLOW_ENDER_EYE", player.getUniqueId().toString(), player.getName());
   }

   private void giveLavaBucketReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 90 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§F§5§5§0§0▶ §fИгрок §x§F§F§5§5§0§0" + player.getName() + " §fсоздал ведро с лавой и получил §x§F§F§5§5§0§090 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("LAVA_BUCKET", player.getUniqueId().toString(), player.getName());
   }

   private void giveCureZombieVillagerReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 280 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§D§5§B§A§5§8▶ §fИгрок §x§D§5§B§A§5§8" + player.getName() + " §fвылечил зомби-жителя и получил §x§D§5§B§A§5§8280 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("CURE_ZOMBIE_VILLAGER", player.getUniqueId().toString(), player.getName());
   }

   private void giveSleepInBedReward(Player player) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "p give " + player.getName() + " 60 -s");
      Bukkit.dispatchCommand(
         Bukkit.getConsoleSender(),
         "litebans broadcast §x§F§F§F§F§0§0▶ §fИгрок §x§F§F§F§F§0§0" + player.getName() + " §fвпервые поспал в кровати и получил §x§F§F§F§F§0§060 сапфиров ☀"
      );
      this.databaseManager.saveRewardAsync("SLEEP_IN_BED", player.getUniqueId().toString(), player.getName());
   }

   @FunctionalInterface
   private interface RewardGiver {
      void give(Player var1);
   }
}
