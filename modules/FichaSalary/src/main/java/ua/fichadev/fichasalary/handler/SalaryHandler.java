package ua.fichadev.fichasalary.handler;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.fichadev.fichasalary.FichaSalary;
import ua.fichadev.fichasalary.managers.ConfigManager;
import ua.fichadev.fichasalary.managers.DataManager;
import ua.fichadev.fichasalary.utils.ColorUtils;

public class SalaryHandler {
   private final FichaSalary plugin;
   private final ConfigManager config;
   private final DataManager dataManager;
   private final LuckPerms luckPerms;

   public SalaryHandler(FichaSalary plugin, ConfigManager config, DataManager dataManager, LuckPerms luckPerms) {
      this.plugin = plugin;
      this.config = config;
      this.dataManager = dataManager;
      this.luckPerms = luckPerms;
   }

   public void checkAndGiveSalary(Player p) {
      String uuid = p.getUniqueId().toString();
      long lastSalaryTime = this.dataManager.getData().getLong(uuid + ".lastSalaryTime", 0L);
      int salaryCount = this.dataManager.getData().getInt(uuid + ".salaryCount", 0);
      long currentTime = System.currentTimeMillis();
      long cooldown = this.config.getCooldownSeconds() * 1000L;
      if (currentTime - lastSalaryTime >= cooldown) {
         this.giveSalary(p, salaryCount);
         this.dataManager.getData().set(uuid + ".lastSalaryTime", currentTime);
         this.dataManager.getData().set(uuid + ".salaryCount", salaryCount + 1);
         this.dataManager.getData().set(uuid + ".playerName", p.getName());
         this.dataManager.saveData();
      }
   }

   private void giveSalary(Player p, int salaryCount) {
      String uuid = p.getUniqueId().toString();
      String nextType = this.dataManager.getData().getString(uuid + ".nextType", "money");
      String group = this.getPrimaryGroup(p);
      int base = this.config.getRankRewards().getOrDefault(group, this.config.getBaseReward());
      if (nextType.equalsIgnoreCase("sapphire")) {
         int amount = this.config.getSapphireBaseAmount() + salaryCount * this.config.getSapphireGrowth();
         this.executeCommand(p, this.config.getSapphireCommand(), amount);
         ColorUtils.sendMessage(p, this.config.getConfig().getString("messages.sapphire-received").replace("%amount%", ColorUtils.formatNumber(amount)));
      } else {
         int amount = base + salaryCount * this.config.getRewardGrowth();
         this.executeCommand(p, this.config.getMoneyCommand(), amount);
         ColorUtils.sendMessage(p, this.config.getConfig().getString("messages.money-received").replace("%amount%", ColorUtils.formatNumber(amount)));
      }

      String newNextType = Math.random() * 100.0 < this.config.getSapphireChance() ? "sapphire" : "money";
      this.dataManager.getData().set(uuid + ".nextType", newNextType);
   }

   public String getPrimaryGroup(Player player) {
      if (this.luckPerms == null) {
         return "default";
      } else {
         User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
         if (user != null && user.getPrimaryGroup() != null) {
            String primaryGroup = user.getPrimaryGroup().toLowerCase();
            return this.config.getRankOrder().contains(primaryGroup) ? primaryGroup : "default";
         } else {
            return "default";
         }
      }
   }

   private void executeCommand(Player p, String template, int amount) {
      String command = template.replace("%player%", p.getName()).replace("%amount%", String.valueOf(amount));
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
   }

   public int getRemainingTime(Player p) {
      String uuid = p.getUniqueId().toString();
      long lastSalaryTime = this.dataManager.getData().getLong(uuid + ".lastSalaryTime", 0L);
      return (int)((lastSalaryTime + this.config.getCooldownSeconds() * 1000L - System.currentTimeMillis()) / 1000L);
   }

   public String getNextSalaryType(Player p) {
      return this.dataManager.getData().getString(p.getUniqueId().toString() + ".nextType", "money");
   }

   public String getPrimaryGroupPublic(Player p) {
      return this.getPrimaryGroup(p);
   }
}
