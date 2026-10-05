package org.impldev.holyclans;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ClanCommand implements CommandExecutor, TabCompleter {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9]+$");
   private static final List<String> SUBCOMMANDS = List.of(
         "help", "top", "money", "invest", "withdraw", "take", "members",
         "invite", "kick", "delete", "newleader", "glow", "pvp", "sethome",
         "home", "list", "chat", "leave", "accept", "decline", "confirm", "cancel");

   private final HolyClans plugin;

   public ClanCommand(HolyClans plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("Команда только для игроков.");
         return true;
      }

      String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
      Clan clan = this.plugin.data().byPlayer(player.getUniqueId());

      switch (sub) {
         case "help" -> this.help(player);
         case "top" -> this.top(player);
         case "money" -> this.money(player, clan);
         case "invest" -> this.invest(player, clan, args);
         case "withdraw", "take" -> this.withdraw(player, clan, args);
         case "members" -> this.members(player, clan);
         case "invite" -> this.invite(player, clan, args);
         case "kick" -> this.kick(player, clan, args);
         case "delete" -> this.delete(player, clan);
         case "newleader" -> this.newleader(player, clan, args);
         case "glow" -> this.glow(player, clan);
         case "pvp" -> this.pvp(player, clan);
         case "sethome" -> this.sethome(player, clan);
         case "home" -> this.home(player, clan);
         case "list" -> this.list(player);
         case "chat" -> this.chat(player, clan);
         case "leave" -> this.leave(player, clan);
         case "accept" -> this.accept(player);
         case "decline" -> this.decline(player);
         case "confirm" -> this.confirm(player, clan);
         case "cancel" -> this.cancel(player);
         default -> this.openMenu(player, clan);
      }
      return true;
   }

   // ── Открыть меню ────────────────────────────────────────────────────────────

   private void openMenu(Player player, Clan clan) {
      if (clan == null) {
         this.plugin.menus().openCreate(player);
      } else {
         this.plugin.menus().openMain(player, clan);
      }
   }

   // ── /clan help ──────────────────────────────────────────────────────────────

   private void help(Player player) {
      String msg = "&r\n"
            + "&r &r&x&0&0&d&8&f&fПомощь по клановой системе:&r\n"
            + "&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan money &r&f– баланс клана по монетам&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan invest <сумма> &r&f– пополнить казну клана&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan withdraw <сумма> &r&f– снять деньги с казны клана&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan take <сумма> &r&f– снять деньги с казны клана&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan members &r&f– участники клана&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan invite <ник> &r&f– пригласить игрока в клан&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan kick <ник> &r&f– удалить игрока из клана&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan delete &r&f– удалить клан&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan newleader <ник> &r&f– передать лидера клан&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan glow &r&f– открыть меню выбора цвета вашей брони&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan pvp &r&f– вкл./выкл. урон по союзникам&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan sethome &r&f– установить точку клан хома&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan home &r&f– телепортироваться на клановый хом&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan list &r&f– просмотр списка кланов&r\n"
            + "&r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan chat &r&f– переключить свой чат на клановый чат&r\n"
            + "&r&x&0&0&d&8&f&f&l▍&r&x&0&0&d&8&f&f /clan leave &r&f– покинуть клан&r";
      player.sendMessage(Texts.color(msg));
   }

   // ── /clan top ───────────────────────────────────────────────────────────────

   private void top(Player player) {
      List<Clan> top = this.plugin.data().top(10);
      StringBuilder sb = new StringBuilder("&r&fТоп кланов:\n");
      for (int i = 0; i < top.size(); i++) {
         Clan c = top.get(i);
         sb.append("&r&6#").append(i + 1).append(" &r&7| &r&x&f&d&c&6&5&3")
               .append(c.name()).append(" &r&f").append(c.points()).append(" &r&7очков\n");
      }
      player.sendMessage(Texts.color(sb.toString().trim()));
   }

   // ── /clan money ─────────────────────────────────────────────────────────────

   private void money(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fНа балансе клана &r&x&f&f&c&9&0&0"
            + Texts.num(clan.treasury()) + "&r&x&f&f&c&9&0&0 &r&fмонет."));
   }

   // ── /clan invest ────────────────────────────────────────────────────────────

   private void invest(Player player, Clan clan, String[] args) {
      if (this.requireClan(player, clan)) return;
      if (args.length < 2) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИспользование: /clan invest <сумма>"));
         return;
      }
      long amount = this.parseLong(args[1]);
      if (amount <= 0) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУкажите корректную сумму."));
         return;
      }
      if (!this.hasMoney(player, amount)) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вас недостаточно средств."));
         return;
      }
      this.plugin.economy().withdrawPlayer(player, amount);
      clan.setTreasury(clan.treasury() + amount);
      this.plugin.data().save(clan);
      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fВы инвестировали монеты в казну клана."));
   }

   // ── /clan withdraw ──────────────────────────────────────────────────────────

   private void withdraw(Player player, Clan clan, String[] args) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может снимать деньги."));
         return;
      }
      if (args.length < 2) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИспользование: /clan withdraw <сумма>"));
         return;
      }
      long amount = this.parseLong(args[1]);
      if (amount <= 0) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУкажите корректную сумму."));
         return;
      }
      if (clan.treasury() < amount) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вашего клана &r&x&f&f&2&2&2&2недостаточно средств &r&fв казне."));
         return;
      }
      clan.setTreasury(clan.treasury() - amount);
      this.plugin.economy().depositPlayer(player, amount);
      this.plugin.data().save(clan);
      player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВы списали монеты с казны клана."));
   }

   // ── /clan members ───────────────────────────────────────────────────────────

   private void members(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      this.plugin.menus().openMembers(player, clan);
   }

   // ── /clan invite ────────────────────────────────────────────────────────────

   private void invite(Player player, Clan clan, String[] args) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может приглашать."));
         return;
      }
      if (args.length < 2) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИспользование: /clan invite <ник>"));
         return;
      }
      Player target = Bukkit.getPlayerExact(args[1]);
      if (target == null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИгрок не найден в сети."));
         return;
      }
      if (this.plugin.data().byPlayer(target.getUniqueId()) != null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fЭтот игрок уже состоит в клане."));
         return;
      }
      if (this.plugin.invites().hasFromClan(target.getUniqueId(), clan)) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fЭтот игрок уже приглашен в клан."));
         return;
      }
      if (clan.members().size() >= clan.slots()) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВ клане нет свободных слотов."));
         return;
      }

      long timeout = this.plugin.getConfig().getLong("invite-timeout-seconds", 300) * 1000L;
      this.plugin.invites().add(target.getUniqueId(), clan, player.getUniqueId(), timeout);

      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fИгрок приглашен в клан."));

      // Кликабельное сообщение приглашённому
      Component msg = Texts.color("&r&x&f&f&7&0&0&0▶ &r&x&f&f&7&0&0&0" + player.getName()
            + "&r&x&f&f&7&0&0&0, &r&fвас пригласили в клан &r&x&f&f&7&0&0&0" + clan.name() + "&r&x&f&f&7&0&0&0!&r\n"
            + "&r\n"
            + "&r   &r&fВыберите действий ниже и нажмите по нему:&r\n");

      Component accept = Texts.color("&r    &r&x&0&5&f&b&0&0[Принять приглашение]")
            .clickEvent(ClickEvent.runCommand("/clan accept"))
            .hoverEvent(HoverEvent.showText(Component.text("Принять")));
      Component decline = Texts.color("&r    &r&x&f&f&2&2&2&2[Отменить приглашение]")
            .clickEvent(ClickEvent.runCommand("/clan decline"))
            .hoverEvent(HoverEvent.showText(Component.text("Отклонить")));
      Component footer = Texts.color("&r\n&r   &r&fПриглашение будет отклонено&r\n"
            + "&r   &r&fчерез &r&x&f&f&7&0&0&05 минут.");

      target.sendMessage(msg.append(accept).append(Component.text("\n")).append(decline).append(footer));
   }

   // ── /clan accept ────────────────────────────────────────────────────────────

   private void accept(Player player) {
      InviteManager.Invite inv = this.plugin.invites().remove(player.getUniqueId());
      if (inv == null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вас нет активных приглашений."));
         return;
      }
      Clan clan = inv.clan();
      if (clan.members().size() >= clan.slots()) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВ клане нет свободных слотов."));
         return;
      }
      clan.members().put(player.getUniqueId(),
            new ClanMember(player.getUniqueId(), player.getName(), ClanRole.MEMBER));
      this.plugin.data().registerPlayer(player.getUniqueId(), clan);
      this.plugin.data().save(clan);

      // Сообщение клану
      Component joinMsg = Texts.color("&r&x&0&0&d&8&f&f▶ &r&x&0&0&d&8&f&f" + player.getName()
            + "&r&x&0&0&d&8&f&f &r&fприсоединился к клану.");
      for (UUID uuid : clan.members().keySet()) {
         Player online = Bukkit.getPlayer(uuid);
         if (online != null) {
            online.sendMessage(joinMsg);
         }
      }
   }

   // ── /clan decline ───────────────────────────────────────────────────────────

   private void decline(Player player) {
      InviteManager.Invite inv = this.plugin.invites().remove(player.getUniqueId());
      if (inv == null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вас нет активных приглашений."));
         return;
      }
      player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fПриглашение отклонено."));
   }

   // ── /clan kick ──────────────────────────────────────────────────────────────

   private void kick(Player player, Clan clan, String[] args) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может исключать."));
         return;
      }
      if (args.length < 2) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИспользование: /clan kick <ник>"));
         return;
      }
      String targetName = args[1];
      UUID targetUuid = this.findMemberByName(clan, targetName);
      if (targetUuid == null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fЭтот игрок не состоит в вашем клане."));
         return;
      }
      if (clan.isLeader(targetUuid)) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fНельзя исключить лидера клана."));
         return;
      }
      this.removeMember(clan, targetUuid);
      player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИгрок &r&x&f&f&2&2&2&2" + targetName
            + "&r&x&f&f&2&2&2&2 &r&fбыл изгнан из клана."));
   }

   // ── /clan delete ────────────────────────────────────────────────────────────

   private void delete(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeader(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер может удалить клан."));
         return;
      }
      this.plugin.confirms().add(player.getUniqueId(), ConfirmManager.Action.DELETE, null, 30_000L);
      this.spamConfirm(player,
            "&r&x&f&f&7&0&0&0▶ &r&fВы действительно желаете удалить клан?");
   }

   // ── /clan newleader ─────────────────────────────────────────────────────────

   private void newleader(Player player, Clan clan, String[] args) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeader(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер может передать лидерство."));
         return;
      }
      if (args.length < 2) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fИспользование: /clan newleader <ник>"));
         return;
      }
      UUID targetUuid = this.findMemberByName(clan, args[1]);
      if (targetUuid == null || clan.isLeader(targetUuid)) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fЭтот игрок не состоит в вашем клане."));
         return;
      }
      this.plugin.confirms().add(player.getUniqueId(), ConfirmManager.Action.NEWLEADER, targetUuid, 30_000L);
      this.spamConfirm(player,
            "&r&x&f&f&7&0&0&0▶ &r&fВы действительно желаете передать лидерство клан?");
   }

   // ── /clan confirm ───────────────────────────────────────────────────────────

   private void confirm(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      ConfirmManager.Pending pending = this.plugin.confirms().remove(player.getUniqueId());
      if (pending == null) return;

      switch (pending.action()) {
         case DELETE -> {
            this.plugin.data().delete(clan);
            for (UUID uuid : clan.members().keySet()) {
               Player online = Bukkit.getPlayer(uuid);
               if (online != null) {
                  online.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВаш клан был удалён."));
               }
               this.plugin.clanChatActive().remove(uuid);
            }
         }
         case NEWLEADER -> {
            UUID target = pending.target();
            ClanMember old = clan.members().get(clan.leader());
            if (old != null) old.setRole(ClanRole.MEMBER);
            clan.setLeader(target);
            ClanMember newLeader = clan.members().get(target);
            if (newLeader != null) newLeader.setRole(ClanRole.LEADER);
            this.plugin.data().save(clan);
            player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fЛидерство передано."));
         }
      }
   }

   // ── /clan cancel ────────────────────────────────────────────────────────────

   private void cancel(Player player) {
      this.plugin.confirms().remove(player.getUniqueId());
      player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fДействие отменено."));
   }

   // ── /clan glow ──────────────────────────────────────────────────────────────

   private void glow(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может менять цвет."));
         return;
      }
      this.plugin.menus().openGlow(player, clan);
   }

   // ── /clan pvp ───────────────────────────────────────────────────────────────

   private void pvp(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может переключать PvP."));
         return;
      }
      clan.setPvp(!clan.pvp());
      this.plugin.data().save(clan);
      if (clan.pvp()) {
         player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fУрон по своим &r&x&0&5&f&b&0&0включен&r&f."));
      } else {
         player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fУрон по своим &r&x&f&f&2&2&2&2выключен&r&f."));
      }
   }

   // ── /clan sethome ───────────────────────────────────────────────────────────

   private void sethome(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (!clan.isLeader(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер может устанавливать точки."));
         return;
      }

      // Проверка WorldGuard
      if (this.plugin.hasWorldGuard() && RegionCheck.isInForeignRegion(player, player.getLocation())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&x&e&7&e&7&e&7Эту команду &r&x&f&f&2&2&2&2запрещено использовать &r&x&e&7&e&7&e&7в чужом регионе"));
         return;
      }

      // Определяем доступное количество точек
      int available = this.getAvailableHomes(clan);
      if (available <= 0) {
         int nextThreshold = this.getNextHomeThreshold(clan);
         int leaderHours = this.getLeaderPlaytimeHours(clan);
         int remaining = nextThreshold - leaderHours;
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВашему клану доступно &r&x&f&f&2&2&2&2" + clan.homes().size()
               + "&r&x&f&f&2&2&2&2 из &r&x&f&f&2&2&2&23&r&f точек дома. Вашему лидеру нужно отыграть еще &r&x&f&f&2&2&2&2"
               + remaining + "&r&x&f&f&2&2&2&2 часов&r&f, чтобы получить новую точку."));
         return;
      }

      // Ищем первый свободный слот
      int slot = -1;
      for (int i = 0; i < 3; i++) {
         if (!clan.homes().containsKey(i)) {
            slot = i;
            break;
         }
      }
      if (slot == -1) {
         // Перезаписываем последний
         slot = 2;
      }

      clan.homes().put(slot, player.getLocation());
      this.plugin.data().save(clan);
      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fКлановая точка установлена."));
   }

   // ── /clan home ──────────────────────────────────────────────────────────────

   private void home(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (clan.homes().isEmpty()) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вашего клана нет установленных точек."));
         return;
      }
      // Телепортируем на первую доступную точку
      Location home = clan.homes().values().iterator().next();
      player.teleport(home);
      player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fТелепортация на клановый хом."));
   }

   // ── /clan list ──────────────────────────────────────────────────────────────

   private void list(Player player) {
      this.plugin.menus().openClanList(player);
   }

   // ── /clan chat ──────────────────────────────────────────────────────────────

   private void chat(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      UUID uuid = player.getUniqueId();
      if (this.plugin.clanChatActive().contains(uuid)) {
         this.plugin.clanChatActive().remove(uuid);
         player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fКлановый чат &r&x&f&f&2&2&2&2выключен&r&f."));
      } else {
         this.plugin.clanChatActive().add(uuid);
         player.sendMessage(Texts.color("&r&x&0&0&d&8&f&f▶ &r&fКлановый чат &r&x&0&5&f&b&0&0включен&r&f."));
      }
   }

   // ── /clan leave ─────────────────────────────────────────────────────────────

   private void leave(Player player, Clan clan) {
      if (this.requireClan(player, clan)) return;
      if (clan.isLeader(player.getUniqueId())) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fЛидер не может покинуть клан. Передайте лидерство или удалите клан."));
         return;
      }
      this.removeMember(clan, player.getUniqueId());
      player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВы покинули клан."));
   }

   // ── Утилиты ─────────────────────────────────────────────────────────────────

   private boolean requireClan(Player player, Clan clan) {
      if (clan == null) {
         player.sendMessage(Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВы не состоите в клане."));
         return true;
      }
      return false;
   }

   private UUID findMemberByName(Clan clan, String name) {
      for (ClanMember m : clan.members().values()) {
         if (m.name().equalsIgnoreCase(name)) return m.uuid();
      }
      return null;
   }

   private void removeMember(Clan clan, UUID uuid) {
      clan.members().remove(uuid);
      this.plugin.data().unregisterPlayer(uuid);
      this.plugin.clanChatActive().remove(uuid);
      this.plugin.data().save(clan);
   }

   private boolean hasMoney(Player player, long amount) {
      return this.plugin.economy() != null
            && this.plugin.economy().getBalance(player) >= amount;
   }

   private long parseLong(String s) {
      try {
         return Long.parseLong(s);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   private void spamConfirm(Player player, String header) {
      int count = this.plugin.getConfig().getInt("confirm-spam-count", 10);
      for (int i = 0; i < count; i++) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Component msg = Texts.color(header + "&r\n"
                  + "&r\n"
                  + "&r   &r&fВыберите действие ниже и нажмите по нему:&r\n");
            Component confirm = Texts.color("&r   &r&x&0&5&f&b&0&0[ПОДТВЕРДИТЬ]")
                  .clickEvent(ClickEvent.runCommand("/clan confirm"))
                  .hoverEvent(HoverEvent.showText(Component.text("Подтвердить")));
            Component cancel = Texts.color("&r   &r&x&f&f&2&2&2&2[ОТМЕНА]")
                  .clickEvent(ClickEvent.runCommand("/clan cancel"))
                  .hoverEvent(HoverEvent.showText(Component.text("Отменить")));
            player.sendMessage(msg.append(confirm).append(Component.text("\n")).append(cancel));
         }, i * 2L); // каждые 2 тика = 0.1 секунды, 10 за секунду
      }
   }

   private int getLeaderPlaytimeHours(Clan clan) {
      OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.leader());
      try {
         int ticks = leader.getStatistic(Statistic.PLAY_ONE_MINUTE);
         return ticks / 72000;
      } catch (Exception e) {
         return 0;
      }
   }

   private int getAvailableHomes(Clan clan) {
      int leaderHours = this.getLeaderPlaytimeHours(clan);
      int unlocked = 0;
      for (int i = 1; i <= 3; i++) {
         int threshold = this.plugin.getConfig().getInt("home-points." + i, i * 10);
         if (leaderHours >= threshold) unlocked++;
      }
      return unlocked - clan.homes().size();
   }

   private int getNextHomeThreshold(Clan clan) {
      int current = clan.homes().size();
      int idx = current + 1;
      return this.plugin.getConfig().getInt("home-points." + idx, idx * 10);
   }

   /** Валидация имени клана при создании (вызывается из ClanListener при вводе в чат). */
   public boolean isValidName(String name) {
      int maxLen = this.plugin.getConfig().getInt("max-name-length", 12);
      return name.length() <= maxLen && NAME_PATTERN.matcher(name).matches();
   }

   @Override
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String label, @NotNull String[] args) {
      if (args.length == 1) {
         String prefix = args[0].toLowerCase(Locale.ROOT);
         List<String> visible = List.of("help", "top", "money", "invest", "withdraw", "take",
               "members", "invite", "kick", "delete", "newleader", "glow", "pvp",
               "sethome", "home", "list", "chat", "leave");
         return visible.stream().filter(s -> s.startsWith(prefix)).toList();
      }
      if (args.length == 2) {
         String sub = args[0].toLowerCase(Locale.ROOT);
         if (sub.equals("invite")) {
            return null; // дефолт — онлайн игроки
         }
         if (sub.equals("kick") || sub.equals("newleader")) {
            if (sender instanceof Player p) {
               Clan clan = this.plugin.data().byPlayer(p.getUniqueId());
               if (clan != null) {
                  String prefix = args[1].toLowerCase(Locale.ROOT);
                  return clan.members().values().stream()
                        .map(ClanMember::name)
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                        .toList();
               }
            }
         }
      }
      return List.of();
   }
}
