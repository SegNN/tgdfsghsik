package ua.holydev.holysalary.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ua.holydev.holysalary.handler.SalaryHandler;
import ua.holydev.holysalary.managers.DataManager;

public class PlayerListener implements Listener {
   private final SalaryHandler salaryHandler;
   private final DataManager dataManager;

   public PlayerListener(SalaryHandler salaryHandler, DataManager dataManager) {
      this.salaryHandler = salaryHandler;
      this.dataManager = dataManager;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player p = event.getPlayer();
      String uuid = p.getUniqueId().toString();
      if (!this.dataManager.getData().contains(uuid + ".lastSalaryTime")) {
         this.salaryHandler.checkAndGiveSalary(p);
      } else {
         this.salaryHandler.checkAndGiveSalary(p);
      }
   }
}
