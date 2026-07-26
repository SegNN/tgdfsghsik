package org.impldev.holycore.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.utils.Parser;
import org.jetbrains.annotations.NotNull;

/**
 * Топ по балансу: {@code /baltop} - места 1-10, {@code /baltop2} - 11-20, {@code /baltop3} - 21-30.
 *
 * <p>Баланс берётся через Vault, то есть из той же экономики, что и везде на сервере.
 *
 * <p>Считается это по всем, кто когда-либо заходил, поэтому перебор идёт вне основного потока и
 * результат живёт {@link #CACHE_MS} миллисекунд: иначе каждая команда подвешивала бы сервер, а
 * три страницы подряд перечитывали бы одно и то же по три раза.
 */
public class BalTop implements CommandExecutor {
   private static final int PER_PAGE = 10;
   private static final int MAX_PLACES = 30;
   private static final long CACHE_MS = 60_000L;

   private final JavaPlugin plugin;
   private volatile List<Entry> cache = List.of();
   private volatile long cachedAt = 0L;
   private volatile boolean building = false;

   public BalTop(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   private record Entry(String name, long balance) {
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      int page = switch (command.getName().toLowerCase(Locale.ROOT)) {
         case "baltop2" -> 2;
         case "baltop3" -> 3;
         default -> 1;
      };

      if (System.currentTimeMillis() - this.cachedAt < CACHE_MS && !this.cache.isEmpty()) {
         this.send(sender, page);
         return true;
      }

      if (this.building) {
         sender.sendMessage(Parser.color("&fТоп считается, повторите через пару секунд."));
         return true;
      }

      this.building = true;
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            List<Entry> fresh = this.build();
            this.cache = fresh;
            this.cachedAt = System.currentTimeMillis();
         } catch (Throwable t) {
            this.plugin.getLogger().warning("Не удалось посчитать топ по балансу: " + t);
         } finally {
            this.building = false;
         }

         Bukkit.getScheduler().runTask(this.plugin, () -> this.send(sender, page));
      });

      return true;
   }

   private List<Entry> build() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
      if (rsp == null) {
         return List.of();
      }

      Economy economy = rsp.getProvider();
      List<Entry> all = new ArrayList<>();
      for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
         String name = player.getName();
         if (name == null) {
            continue;
         }

         double balance;
         try {
            balance = economy.getBalance(player);
         } catch (Throwable ignored) {
            // у части старых записей нет данных экономики - такие просто пропускаем
            continue;
         }

         if (balance > 0.0) {
            all.add(new Entry(name, (long)balance));
         }
      }

      all.sort(Comparator.comparingLong(Entry::balance).reversed());
      return all.size() > MAX_PLACES ? new ArrayList<>(all.subList(0, MAX_PLACES)) : all;
   }

   private void send(CommandSender sender, int page) {
      List<Entry> top = this.cache;
      if (top.isEmpty()) {
         sender.sendMessage(Parser.color("&fТоп богачей пуст."));
         return;
      }

      int from = (page - 1) * PER_PAGE;
      if (from >= top.size()) {
         sender.sendMessage(Parser.color("&fНа этой странице никого нет."));
         return;
      }

      int to = Math.min(from + PER_PAGE, top.size());
      sender.sendMessage(Parser.color("&fТоп богачей:"));
      for (int i = from; i < to; i++) {
         Entry entry = top.get(i);
         sender.sendMessage(Parser.color(
               "&r&x&0&0&d&8&f&f#" + (i + 1)
                     + " &r&7| &r&x&a&3&e&d&f&e" + entry.name()
                     + " &r&f" + money(entry.balance()) + "&r&7¤"));
      }
   }

   /** Немецкая локаль ставит точку разделителем тысяч - ровно тот формат, что на сервере. */
   private static String money(long value) {
      return String.format(Locale.GERMAN, "%,d", value);
   }
}
