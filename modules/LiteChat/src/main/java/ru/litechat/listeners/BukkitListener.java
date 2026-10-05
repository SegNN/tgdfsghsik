package ru.litechat.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Statistic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.litechat.LiteChat;
import ru.litechat.utils.ConsoleMSG;
import ru.litechat.utils.HexUtil;

public class BukkitListener implements Listener {
   private final Map<UUID, Long> cooldown = new HashMap<>();
   private final Map<UUID, Integer> messageCount = new HashMap<>();
   private final Map<UUID, Long> lastMessageTime = new HashMap<>();
   private final Map<UUID, Integer> warningCount = new HashMap<>();
   public static int time = LiteChat.getInstance().getConfig().getInt("settings.cooldown", 2);

   public BukkitListener() {
   }

   @EventHandler(ignoreCancelled = true)
   public void onChat(AsyncPlayerChatEvent event) {
      String bypassPermission = LiteChat.getInstance().getConfig().getString("settings.bypass-permissions", "litechat.bypass");
      String disablePermission = LiteChat.getInstance().getConfig().getString("settings.disablechat-permissions", "litechat.disable");
      String message = event.getMessage();
      Player player = event.getPlayer();
      if (player.hasPermission(disablePermission)) {
         event.setCancelled(true);
      } else {
         if (!player.hasPermission(bypassPermission) && this.isNewbie(player, event)) {
            event.setCancelled(true);
         } else if (this.containsBlockedWords(player, message, bypassPermission)) {
            event.setCancelled(true);
         } else if (!this.handleFloodCheck(player, message, event, bypassPermission) && !this.handleCooldown(player, message, event, bypassPermission)) {
            this.processRegularMessage(event, player, message, bypassPermission);
         }
      }
   }


   /**
    * Вешает подсказку со статистикой на ник автора сообщения.
    *
    * <p>Сообщение уже собрано в строку с §-кодами, поэтому ник ищется в ней по его отрисованному
    * значению и оборачивается отдельным компонентом. Хвост после ника получает явный цвет,
    * унаследованный от предыдущего куска: {@code fromLegacyText} разбирает каждый отрезок
    * независимо, и без этого текст после ника вернулся бы к белому.
    *
    * <p>Если ник в строке не нашёлся (ник переопределён, формат изменён), сообщение уходит как
    * было - лучше без подсказки, чем сломанное.
    */
   private BaseComponent[] withHover(String formatted, Player author) {
      List<String> lines = LiteChat.getInstance().getConfig().getStringList("hover");
      if (lines.isEmpty()) {
         return TextComponent.fromLegacyText(formatted);
      }

      String nick = HexUtil.color(PlaceholderAPI.setPlaceholders(author, "%holycustom_name%"));
      int at = formatted.indexOf(nick);
      if (nick.isEmpty() || at < 0) {
         nick = author.getName();
         at = formatted.indexOf(nick);
      }

      if (at < 0) {
         return TextComponent.fromLegacyText(formatted);
      }

      int end = at + nick.length();
      String head = formatted.substring(0, at);
      String tail = org.bukkit.ChatColor.getLastColors(formatted.substring(0, end)) + formatted.substring(end);

      HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
            TextComponent.fromLegacyText(this.hoverText(author))));

      TextComponent out = new TextComponent();
      for (BaseComponent part : TextComponent.fromLegacyText(head)) {
         out.addExtra(part);
      }
      for (BaseComponent part : TextComponent.fromLegacyText(nick)) {
         part.setHoverEvent(hover);
         out.addExtra(part);
      }
      for (BaseComponent part : TextComponent.fromLegacyText(tail)) {
         out.addExtra(part);
      }

      return new BaseComponent[]{out};
   }

   private String hoverText(Player author) {
      String body = String.join("\n", LiteChat.getInstance().getConfig().getStringList("hover"));
      return HexUtil.color(PlaceholderAPI.setPlaceholders(author, body.replace("{playtime}", playtime(author))));
   }

   /**
    * "0ч., 51м." - наигранное берётся из статистики, а не из времени первого входа: последнее
    * растёт даже когда игрок не заходит, и через месяц показывало бы месяц игры.
    */
   private static String playtime(Player player) {
      long minutes = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20L / 60L;
      return minutes / 60L + "ч., " + minutes % 60L + "м.";
   }

   private boolean isNewbie(Player player, AsyncPlayerChatEvent event) {
      long firstPlayed = player.getFirstPlayed();
      long currentTime = System.currentTimeMillis();
      long timePlayed = currentTime - firstPlayed;
      long newbieCooldownMillis = LiteChat.getInstance().getConfig().getInt("settings.newbie-cooldown-minutes", 5) * 60 * 1000L;
      if (timePlayed < newbieCooldownMillis) {
         long timePlayedSeconds = timePlayed / 1000L;
         long minutes = timePlayedSeconds / 60L;
         long seconds = timePlayedSeconds % 60L;
         String var10000 = minutes > 0L ? minutes + " мин. " : "";
         String timePlayedFormatted = var10000 + seconds + " сек.";
         String newbieMessage = LiteChat.getInstance()
            .getConfig()
            .getString("settings.newbie-cooldown-message", "&x&1&E&C&D&F&B▶ &fВам нужно отыграть 5 минут, вы пока что отыграли &6{time_played}&f!");
         player.sendMessage(HexUtil.color(newbieMessage.replace("{time_played}", timePlayedFormatted)));
         var10000 = player.getName();
         ConsoleMSG.color("&7[Чат] &cНовичок " + var10000 + " пытался писать в чат до истечения 5 минут: " + event.getMessage());
         return true;
      } else {
         return false;
      }
   }

   private boolean containsBlockedWords(Player player, String message, String bypassPermission) {
      if (player.hasPermission(bypassPermission)) {
         return false;
      } else {
         String lowerMessage = message.toLowerCase();
         List<String> blockedPhrases = LiteChat.getInstance().getConfig().getStringList("blocks-text");
         String blockType = LiteChat.getInstance().getConfig().getString("block-types.swear.message", "запрещённое слово");
         boolean isBlocked = false;

         for (String phrase : blockedPhrases) {
            if (lowerMessage.contains(phrase.toLowerCase())) {
               isBlocked = true;
               break;
            }
         }

         if (!isBlocked && LiteChat.getInstance().getChatFilter() != null && LiteChat.getInstance().getChatFilter().isMessageBlocked(message)) {
            isBlocked = true;
            blockType = LiteChat.getInstance().getConfig().getString("block-types.ads.message", "реклама");
         }

         if (isBlocked) {
            UUID uuid = player.getUniqueId();
            int warnings = this.warningCount.getOrDefault(uuid, 0) + 1;
            int maxWarnings = LiteChat.getInstance().getConfig().getInt("settings.max-warnings", 3);
            this.warningCount.put(uuid, warnings);
            String playerMsg = LiteChat.getInstance()
               .getConfig()
               .getString(
                  "messages.block-message-for-player", "&x&1&E&C&D&F&B▶ &fСообщение не было отправлено&x&1&E&C&D&F&B, в нем содержится нецензурный контент!"
               )
               .replace("{warning_count}", String.valueOf(warnings))
               .replace("{max_warnings}", String.valueOf(maxWarnings));
            player.sendMessage(HexUtil.color(playerMsg));
            String warningMsg = LiteChat.getInstance()
               .getConfig()
               .getString("messages.warning-notify", "&6▶ &fНарушение &6#{warning_count}/{max_warnings}&f. При &6{max_warnings}&f - мут!")
               .replace("{warning_count}", String.valueOf(warnings))
               .replace("{max_warnings}", String.valueOf(maxWarnings));
            player.sendMessage(HexUtil.color(warningMsg));
            String adminMsg = LiteChat.getInstance().getConfig().getString("admin.block-message-for-admin");
            if (adminMsg != null) {
               String muteCommand = HexUtil.color(
                  adminMsg.replace("{player}", player.getName()).replace("{message}", message).replace("{block_type}", blockType)
               );
               Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("litechat.alert.admin")).forEach(p -> p.sendMessage(muteCommand));
            }

            if (warnings >= maxWarnings) {
               String muteCommand = LiteChat.getInstance()
                  .getConfig()
                  .getString("settings.mute-command", "mute {player} 5m Автоматический мут за нарушения чата")
                  .replace("{player}", player.getName());
               String muteMessage = LiteChat.getInstance().getConfig().getString("messages.mute-message", "&cВы получили мут на 5 минут за нарушения в чате!");
               Bukkit.getScheduler().runTask(LiteChat.getInstance(), () -> {
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), muteCommand);
                  String var10000 = player.getName();
                  ConsoleMSG.color("&7[Чат] &cИгрок " + var10000 + " получил мут за достижение максимума предупреждений (" + maxWarnings + ")");
               });
               player.sendMessage(HexUtil.color(muteMessage));
               this.warningCount.remove(uuid);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private boolean handleFloodCheck(Player player, String message, AsyncPlayerChatEvent event, String bypassPermission) {
      if (player.hasPermission(bypassPermission)) {
         return false;
      } else {
         UUID uuid = player.getUniqueId();
         long now = System.currentTimeMillis();
         if (this.lastMessageTime.containsKey(uuid) && now - this.lastMessageTime.get(uuid) < 2000L) {
            int count = this.messageCount.getOrDefault(uuid, 0) + 1;
            this.messageCount.put(uuid, count);
            if (count >= 3) {
               String cooldownMsg = LiteChat.getInstance().getConfig().getString("messages.cooldown-for-chat", "&6▶ &fЧат будет &6доступен &fчерез &6{sec}");
               if (cooldownMsg != null) {
                  player.sendMessage(HexUtil.color(cooldownMsg.replace("{sec}", "2")));
               }

               this.sendAlertFlood(player, message);
               event.setCancelled(true);
               return true;
            }
         } else {
            this.messageCount.put(uuid, 1);
         }

         this.lastMessageTime.put(uuid, now);
         return false;
      }
   }

   private boolean handleCooldown(Player player, String message, AsyncPlayerChatEvent event, String bypassPermission) {
      if (player.hasPermission(bypassPermission)) {
         return false;
      } else {
         UUID uuid = player.getUniqueId();
         long currentTime = System.currentTimeMillis();
         if (this.cooldown.containsKey(uuid)) {
            long lastTime = this.cooldown.get(uuid);
            long timeLeft = (lastTime + time * 1000L - currentTime) / 1000L;
            if (timeLeft > 0L) {
               String cooldownMsg = LiteChat.getInstance().getConfig().getString("messages.cooldown-for-chat", "&6▶ &fЧат будет &6доступен &fчерез &6{sec}");
               if (cooldownMsg != null) {
                  player.sendMessage(HexUtil.color(cooldownMsg.replace("{sec}", String.valueOf(timeLeft))));
               }

               this.sendAlertFlood(player, message);
               event.setCancelled(true);
               return true;
            }
         }

         this.cooldown.put(uuid, currentTime);
         return false;
      }
   }

   private void processRegularMessage(AsyncPlayerChatEvent event, Player player, String message, String bypassPermission) {
      String colorPermission = LiteChat.getInstance().getConfig().getString("settings.color-permissions", "");
      if (player.hasPermission(colorPermission) || !message.contains("&") && !message.contains("&#") && !message.contains("<#")) {
         if (player.hasPermission(colorPermission)) {
            message = HexUtil.color(message);
         }
      } else {
         player.sendMessage(HexUtil.color("&cУ вас нет разрешения для использования цветов в чате!"));
         String var10000 = player.getName();
         ConsoleMSG.color("&7[Чат] &cИгрок " + var10000 + " пытался использовать цвета без разрешения: " + message);
         message = HexUtil.removeColors(message);
      }

      String lg = PlaceholderAPI.setPlaceholders(player, "%luckperms_primary_group_name%");
      String suffix = PlaceholderAPI.setPlaceholders(player, "%luckperms_suffix%");
      String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
      String color = LiteChat.getInstance()
         .getConfig()
         .getString("color." + lg, LiteChat.getInstance().getConfig().getString("color." + (message.startsWith("!") ? "global" : "local"), "&f"));
      if (message.startsWith("!")) {
         String gcFormat = LiteChat.getInstance().getConfig().getString("format.global", "&7{player}: {message}");
         String formattedMessage = HexUtil.color(PlaceholderAPI.setPlaceholders(player, gcFormat))
            .replace("{player}", HexUtil.color(player.getName()))
            .replace("{prefix}", HexUtil.color(prefix))
            .replace("{suffix}", HexUtil.translate(suffix))
            .replace("{color}", HexUtil.color(color))
            .replace("{message}", message.substring(1));
         event.setCancelled(true);
         BaseComponent[] withHover = this.withHover(formattedMessage, player);
         Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(withHover));
         ConsoleMSG.color("&7[Глобальный чат] " + formattedMessage);
      } else {
         String gcFormat = LiteChat.getInstance().getConfig().getString("format.local", "&7{player}: {message}");
         String formattedMessage = HexUtil.color(PlaceholderAPI.setPlaceholders(player, gcFormat))
            .replace("{player}", HexUtil.color(player.getName()))
            .replace("{prefix}", HexUtil.color(prefix))
            .replace("{suffix}", HexUtil.translate(suffix))
            .replace("{color}", HexUtil.color(color))
            .replace("{message}", message);
         event.getRecipients().clear();
         event.getRecipients().add(player);
         int radius = LiteChat.getInstance().getConfig().getInt("format.radius", 100);
         Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.getWorld().equals(player.getWorld()) && p.getLocation().distance(player.getLocation()) <= radius) {
               event.getRecipients().add(p);
            }
         });
         event.setCancelled(true);
         BaseComponent[] withHover = this.withHover(formattedMessage, player);
         event.getRecipients().forEach(p -> p.spigot().sendMessage(withHover));
         ConsoleMSG.color("&7[Локальный чат] " + formattedMessage);
      }

      if (!player.hasPermission(bypassPermission)) {
         this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());
      }
   }

   public void sendAlertFlood(Player player, String message) {
      String alertPermission = LiteChat.getInstance().getConfig().getString("settings.alert-permissions", "litechat.alert");
      String floodAlert = LiteChat.getInstance().getConfig().getString("messages.flood-alert");
      if (floodAlert != null) {
         String formatted = HexUtil.color(floodAlert.replace("{playerName}", player.getName()).replace("{msg}", message));
         Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(alertPermission)).forEach(p -> p.sendMessage(formatted));
      }
   }
}
