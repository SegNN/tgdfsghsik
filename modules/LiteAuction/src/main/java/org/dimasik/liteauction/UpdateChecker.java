package org.dimasik.liteauction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {
   private final JavaPlugin plugin;
   private final File pluginFile;
   private final boolean testMode = false;
   private final String currentVersion = "v3.4.7";
   private final String changeLogUrl;
   private final String pluginUrl;

   public UpdateChecker(JavaPlugin plugin, File pluginFile) {
      this.plugin = plugin;
      this.pluginFile = pluginFile;
      this.changeLogUrl = "https://raw.githubusercontent.com/Dimasik201O/LiteAuction/master/change.log";
      this.pluginUrl = "https://github.com/Dimasik201O/LiteAuction/releases/latest/download/LiteAuction.jar";
   }

   public void checkForUpdates() {
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            String latestVersion = this.getLatestVersion();
            this.plugin.getLogger().info("   [   пᴘᴏʙᴇᴘᴋᴀ ᴏбʜᴏʙлᴇʜий   ]   ");
            if (this.isNewerVersion(latestVersion, "v3.4.7")) {
               this.plugin.getLogger().info("   |   ʜᴀйдᴇʜᴀ ʜᴏʙᴀя ʙᴇᴘᴄия: " + latestVersion + " | тᴇᴋущᴀя: v3.4.7");
               this.plugin.getLogger().info("   |   зᴀгᴘузᴋᴀ пᴏᴄлᴇдʜᴇй ʙᴇᴘᴄии...");
               if (this.downloadUpdate()) {
                  this.plugin.getLogger().info("   |   ᴏбʜᴏʙлᴇʜиᴇ ᴄᴋᴀчᴀʜᴏ");
                  this.installUpdate();
               } else {
                  this.plugin.getLogger().warning("   |   ᴏшибᴋᴀ ᴄᴋᴀчиʙᴀʜия ᴏбʜᴏʙлᴇʜия");
               }
            } else {
               this.plugin.getLogger().info("   |   у ʙᴀᴄ ужᴇ пᴏᴄлᴇдʜяя ʙᴇᴘᴄия (v3.4.7)");
            }
         } catch (Exception var2) {
            this.plugin.getLogger().warning("   |   ᴏшибᴋᴀ ᴏбʜᴏʙлᴇʜия: " + var2.getMessage());
         }
      });
   }

   private String getLatestVersion() throws IOException {
      URL url = new URL(this.changeLogUrl);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
         String firstLine = reader.readLine();
         if (firstLine != null && firstLine.startsWith("LATEST_VERSION=")) {
            return firstLine.substring("LATEST_VERSION=".length()).trim();
         }
      }

      throw new IOException("Could not find LATEST_VERSION in change.log");
   }

   private boolean isNewerVersion(String newVersion, String currentVersion) {
      return newVersion != null && currentVersion != null ? newVersion.compareTo(currentVersion) > 0 : false;
   }

   private boolean downloadUpdate() {
      try {
         URL website = new URL(this.pluginUrl);
         ReadableByteChannel rbc = Channels.newChannel(website.openStream());
         File updateFile = new File(this.plugin.getDataFolder().getParent(), "LiteAuction-updated.jar");
         FileOutputStream fos = new FileOutputStream(updateFile);
         fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
         fos.close();
         return true;
      } catch (IOException var5) {
         this.plugin.getLogger().warning("   |   ᴏшибᴋᴀ ᴄᴋᴀчиʙᴀʜия ᴏбʜᴏʙлᴇʜия: " + var5.getMessage());
         var5.printStackTrace();
         return false;
      }
   }

   private void installUpdate() {
      try {
         File updateFile = new File(this.plugin.getDataFolder().getParent(), "LiteAuction-updated.jar");
         if (!updateFile.exists()) {
            this.plugin.getLogger().warning("   |   пᴏᴄлᴇдʜяя ʙᴇᴘᴄия ʜᴇ ʜᴀйдᴇʜᴀ");
            return;
         }

         Files.move(updateFile.toPath(), this.pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         this.plugin.getLogger().info("   |   плᴀгиʜ уᴄпᴇшʜᴏ ᴏбʜᴏʙлᴇʜ");
         this.plugin.getLogger().info("   |   пᴇᴘᴇзᴀпуᴄтитᴇ ᴄᴇᴘʙᴇᴘ, чтᴏбы ᴏбʜᴏʙлᴇʜиᴇ пᴘимᴇʜилᴏᴄь");
      } catch (Exception var2) {
         this.plugin.getLogger().warning("   |   ᴏшибᴋᴀ уᴄтᴀʜᴏʙᴋи ᴏбʜᴏʙлᴇʜия: " + var2.getMessage());
         var2.printStackTrace();
      }
   }
}
