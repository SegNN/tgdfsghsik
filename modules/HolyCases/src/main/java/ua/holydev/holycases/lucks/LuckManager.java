package ua.holydev.holycases.lucks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.database.dao.PlayerLuckDao;

public class LuckManager {
   private final List<Luck> lucks = new ArrayList<>();
   private HolyCases plugin;
   private FileConfiguration luckConfig;
   private static Luck NO_LUCK = null;

   public static Luck getNoLuck() {
      return NO_LUCK;
   }

   public LuckManager(HolyCases plugin) {
      this.plugin = plugin;
      this.loadLucks();
      NO_LUCK = new Luck(plugin, "no_luck", this.luckConfig.getString("lucks.no_luck.history"), this.luckConfig.getString("lucks.no_luck.prize_message"));
   }

   public void loadLucks() {
      this.lucks.clear();
      this.loadFile();
      if (this.luckConfig.contains("lucks")) {
         for (String name : this.luckConfig.getConfigurationSection("lucks").getKeys(false)) {
            if (!name.equalsIgnoreCase("no_luck")) {
               ConfigurationSection luckSection = this.luckConfig.getConfigurationSection("lucks." + name);
               if (luckSection != null) {
                  Luck luck = new Luck(this.plugin, name, luckSection.getString("history", name), luckSection.getString("prize_message", name));
                  this.lucks.add(luck);
               }
            }
         }
      }
   }

   public boolean hasLuck(String name) {
      return this.getLuckNames().stream().anyMatch(luckName -> luckName.equalsIgnoreCase(name));
   }

   public String getLuckLines(OfflinePlayer player) {
      ConcurrentHashMap<Integer, String> results = new ConcurrentHashMap<>();
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (int i = 0; i < this.lucks.size(); i++) {
         int index = i;
         Luck luck = this.lucks.get(index);
         String replace = this.getReplace(luck.getName());
         CompletableFuture<Void> future = luck.getLuckFor(player).thenAccept(lucksValue -> {
            if (lucksValue > 0) {
               String resultLine = replace.replace("%count%", lucksValue + "");
               results.put(index, resultLine);
            }
         });
         futures.add(future);
      }

      CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
      allOf.join();
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < this.lucks.size(); i++) {
         if (results.containsKey(i)) {
            if (!result.isEmpty()) {
               result.append("\n");
            }

            result.append(results.get(i));
         }
      }

      if (result.isEmpty() && this.luckConfig.contains("lucks.no_luck.replace")) {
         result.append(this.luckConfig.getString("lucks.no_luck.replace"));
      }

      return result.toString();
   }

   public List<Luck> getLucks() {
      return this.lucks;
   }

   public List<String> getLuckNames() {
      List<String> result = new ArrayList<>();

      for (Luck luck : this.lucks) {
         result.add(luck.getName());
      }

      return result;
   }

   public Luck removeLast(OfflinePlayer player) {
      AtomicReference<Luck> result = new AtomicReference<>(null);
      AtomicReference<Integer> maxLuckValue = new AtomicReference<>(Integer.MIN_VALUE);
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (int i = 0; i < this.lucks.size(); i++) {
         int index = i;
         Luck luck = this.lucks.get(index);
         CompletableFuture<Void> future = luck.getLuckFor(player).thenAccept(luckValue -> {
            if (luckValue > 0 && index > maxLuckValue.get()) {
               maxLuckValue.set(index);
               result.set(luck);
            }
         });
         futures.add(future);
      }

      CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
      allOf.join();
      Luck highestLuck = result.get();
      if (highestLuck != null) {
         if (this.plugin.isSQL()) {
            this.plugin.getExecutor().useExtension(PlayerLuckDao.class, dao -> dao.withdrawLuck(player.getUniqueId(), highestLuck.getName(), 1));
         } else {
            this.plugin.removeLuck(player, highestLuck.getName(), 1);
         }
      }

      return highestLuck;
   }

   public Luck getLuckByName(String name) {
      for (Luck luck : this.lucks) {
         if (luck.getName().equalsIgnoreCase(name)) {
            return luck;
         }
      }

      return null;
   }

   private String getReplace(String luck) {
      return this.luckConfig.getString("lucks." + luck + ".replace");
   }

   private void loadFile() {
      File luckFile = new File(this.plugin.getDataFolder(), "LuckConfig.yml");
      if (!luckFile.exists()) {
         this.plugin.saveResource("LuckConfig.yml", false);
      }

      this.luckConfig = YamlConfiguration.loadConfiguration(luckFile);
   }
}
