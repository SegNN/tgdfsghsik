package ua.fichadev.fichacases.cases;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import shaded.org.jdbi.v3.core.async.JdbiExecutor;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.animation.Animation;
import ua.fichadev.fichacases.database.dao.PrizeHistoryDao;
import ua.fichadev.fichacases.lucks.Luck;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.menu.Menu;
import ua.fichadev.fichacases.misc.ColorUtil;
import ua.fichadev.fichacases.misc.Messages;
import ua.fichadev.fichacases.misc.RandomCollection;
import ua.fichadev.fichacases.nms.CustomArmorStand;
import ua.fichadev.fichacases.particle.WrappedParticle;
import ua.fichadev.fichacases.reward.Prize;
import ua.fichadev.fichacases.reward.Reward;

public abstract class GameCase {
   private static final Map<Player, GameCase> openedCases = new ConcurrentHashMap<>();
   protected FichaCases plugin;
   protected JdbiExecutor executor;
   protected String name;
   protected boolean isLuckWork;
   protected Location location;
   protected BlockFace direction;
   protected Animation animation;
   protected Material blockMaterial;
   protected List<WrappedParticle> wrappedParticles;
   protected List<Reward> rewards;
   protected Player opening;
   protected Luck openingLuck;
   protected CustomArmorStand titleAboveCase;
   protected Function<Player, Menu> menuCreator;
   protected List<String> prizeMessage;
   protected Messages messages;

   public GameCase() {
   }

   public GameCase(
      FichaCases plugin,
      JdbiExecutor executor,
      String name,
      String titleAboveCase,
      double titleAboveCaseYOffset,
      boolean isLuckWork,
      Location location,
      BlockFace direction,
      Animation animation,
      Material blockMaterial,
      List<WrappedParticle> wrappedParticles,
      List<Reward> rewards,
      Function<Player, Menu> menuCreator,
      List<String> prizeMessage,
      Messages messages
   ) {
      this.plugin = plugin;
      this.executor = executor;
      this.name = name;
      this.isLuckWork = isLuckWork;
      this.location = location;
      this.direction = direction;
      this.animation = animation;
      this.blockMaterial = blockMaterial;
      this.wrappedParticles = wrappedParticles;
      this.rewards = rewards;
      this.menuCreator = menuCreator;
      this.prizeMessage = prizeMessage;
      this.messages = messages;
      this.titleAboveCase = new CustomArmorStand(location);
      this.titleAboveCase.spawn(location.clone().add(0.5, titleAboveCaseYOffset, 0.5));
      this.titleAboveCase.setCustomName(titleAboveCase);
      this.titleAboveCase.setMarker(true);
   }

   public String getName() {
      return this.name;
   }

   public Location getLocation() {
      return this.location;
   }

   public BlockFace getDirection() {
      return this.direction;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public List<WrappedParticle> getWrappedParticles() {
      return this.wrappedParticles;
   }

   public List<Reward> getRewards() {
      return this.rewards;
   }

   public Luck getLuck() {
      return this.openingLuck;
   }

   public Reward getRandomReward() {
      RandomCollection<Reward> newRewards = new RandomCollection<>();
      if (this.isLuckWork) {
         String luckName = this.openingLuck != null ? this.openingLuck.getName() : "no_luck";

         for (Reward reward : this.rewards) {
            newRewards.add(reward.getChance(luckName), reward);
         }
      } else {
         for (Reward reward : this.rewards) {
            newRewards.add(reward.getChance("no_luck"), reward);
         }
      }

      return newRewards.next();
   }

   public abstract void open(Player var1, Luck var2);

   public Player getOpening() {
      return this.opening;
   }

   public Luck getOpeningLuck() {
      return this.openingLuck;
   }

   public Messages getMessages() {
      return this.messages;
   }

   public boolean isOpening() {
      return this.opening != null;
   }

   public abstract void onOpenEnd();

   public void setTitleVisible(boolean visible) {
      this.titleAboveCase.setNameVisible(visible);
   }

   public void deleteTitle() {
      this.setTitleVisible(false);
      this.titleAboveCase.remove();
   }

   public void openMenu(Player player) {
      this.menuCreator.apply(player).open(player);
      put(player, this);
   }

   public void givePrize(OfflinePlayer player, Reward reward) {
      Prize prize;
      if (this.isLuckWork) {
         prize = reward.getRandomPrize(this.openingLuck);
      } else {
         prize = reward.getRandomPrize(null);
      }

      if (reward.isSendTitle() && player.isOnline()) {
         player.getPlayer().sendTitle(prize.title(), prize.subtitle(), 20, 100, 10);
      }

      String prizeLuck;
      if (this.isLuckWork) {
         if (this.getOpeningLuck() != null) {
            prizeLuck = this.getOpeningLuck().getPrizeMessage();
         } else {
            prizeLuck = LuckManager.getNoLuck() == null ? "" : LuckManager.getNoLuck().getPrizeMessage();
         }
      } else {
         prizeLuck = "";
      }

      this.prizeMessage
         .forEach(
            prizeLine -> {
               String message = prizeLine.replace("%player_name%", player.getName())
                  .replace("%prize_name%", prize.name())
                  .replace("%luck%", ColorUtil.parse(prizeLuck));
               Bukkit.broadcastMessage(message);
            }
         );
      prize.commands()
         .forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%luck%", prizeLuck).replace("%player_name%", player.getName())));
      this.plugin.removePlayer(player);
      if (reward.isAddInHistory()) {
         String color;
         if (reward.getIcon().getItemMeta() instanceof LeatherArmorMeta armorMeta) {
            color = ":" + armorMeta.getColor().getRed() + ":" + armorMeta.getColor().getGreen() + ":" + armorMeta.getColor().getBlue();
         } else {
            color = "";
         }

         String luck;
         if (this.isLuckWork) {
            if (this.getOpeningLuck() != null) {
               luck = this.getOpeningLuck().getHistory();
            } else {
               luck = LuckManager.getNoLuck() == null ? "Без удачи" : LuckManager.getNoLuck().getHistory();
            }
         } else {
            luck = LuckManager.getNoLuck() == null ? "Без удачи" : LuckManager.getNoLuck().getHistory();
         }

         String serverName = this.plugin.getConfig().getString("server_name");
         if (this.plugin.isSQL()) {
            this.executor
               .useExtension(
                  PrizeHistoryDao.class,
                  dao -> dao.addEntry(
                     player.getName(), this.name, luck, prize.name(), reward.getIcon().getType().name() + color, System.currentTimeMillis(), serverName
                  )
               );
         } else {
            this.plugin
               .addPrizeHistory(
                  player.getName(), this.name, luck, prize.name(), reward.getIcon().getType().name() + color, System.currentTimeMillis(), serverName
               );
         }
      }
   }

   public static void put(Player player, GameCase gameCase) {
      openedCases.put(player, gameCase);
   }

   public static void remove(Player player) {
      openedCases.remove(player);
   }

   public static void removeAll(GameCase gameCase) {
      for (Player player : openedCases.keySet().stream().toList()) {
         if (player != null && openedCases.get(player) == gameCase) {
            player.closeInventory();
         }
      }
   }
}
