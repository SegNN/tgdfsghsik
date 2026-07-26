package frozenpon.frozenpotionstacker;

import frozenpon.frozenpotionstacker.Utils.InventoryListener;
import frozenpon.frozenpotionstacker.Utils.StackDebugger;
import frozenpon.frozenpotionstacker.Utils.ReflectionUtils;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   private int stackPotion;
   private int stackSplash;
   private InventoryListener listener;
   private int sweeperId = -1;

   public Main() {
   }

   public void onEnable() {
      this.loadConfiguration();
      this.changePotionStackSizes();
      if (this.getConfig().getBoolean("debug", false)) {
         new StackDebugger(this).runTaskTimer(this, 20L, 20L);
         this.getLogger().warning("DEBUG включён: слежу за слотами с зельями раз в секунду");
      }
   }

   public void loadConfiguration() {
      this.getConfig().options().copyDefaults(true);
      this.saveConfig();
      this.stackPotion = this.getConfig().getInt("stack.potion");
      this.stackSplash = this.getConfig().getInt("stack.splash-potion");
      if (this.stackPotion == 1 && this.stackSplash == 1) {
         if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
         }
      } else if (this.listener == null) {
         this.getServer().getPluginManager().registerEvents(this.listener = new InventoryListener(this, this.stackPotion, this.stackSplash), this);
         this.startSweeper();
      } else {
         this.listener.setStacks(this.stackPotion, this.stackSplash);
      }
   }


   /**
    * Раз в секунду проставляет компонент max_stack_size всем зельям в инвентарях онлайна.
    *
    * <p>Событий (вход, подбор, закрытие инвентаря) недостаточно: зелья приходят и другими
    * путями - из китов, магазинов, наковальни HolyPotion, команд, - и такое зелье остаётся
    * без компонента, то есть с реальным лимитом 1. Стак из 1.16.5 в слоте с лимитом 1
    * сервер считает нелегальным и уничтожает - именно так зелья и пропадали через пару секунд.
    * Периодическое подметание делает инвариант безусловным, каким бы плагином зелье ни выдали.
    */
   private void startSweeper() {
      if (this.sweeperId != -1 || !ReflectionUtils.isModern()) {
         return;
      }

      this.sweeperId = this.getServer().getScheduler().runTaskTimer(this, () -> {
         if (this.listener == null) {
            return;
         }

         for (org.bukkit.entity.Player player : this.getServer().getOnlinePlayers()) {
            if (this.listener.sweep(player.getInventory()) > 0) {
               player.updateInventory();
            }
         }
      }, 20L, 20L).getTaskId();
   }

   private void changePotionStackSizes() {
      String potionName = "POTION";
      boolean potionReg = ReflectionUtils.setFor(potionName, this.stackPotion);
      boolean potionNMS = ReflectionUtils.setForNMS("POTION", this.stackPotion);
      if (potionReg && potionNMS) {
         this.getLogger().info("Максимальный размер стака " + potionName + " равно " + this.stackPotion);
      } else {
         this.getLogger()
            .warning(
               "Проблема с максимальным размером стака "
                  + potionName
                  + ".\norg.bukkit.Material:"
                  + (potionReg ? "WORKED" : "FAILED")
                  + "\nNMS.Items:"
                  + (potionNMS ? "WORKED" : "FAILED")
            );
      }

      String splashPotionName = "SPLASH_POTION";
      boolean splashReg = ReflectionUtils.setFor(splashPotionName, this.stackSplash);
      boolean splashNMS = ReflectionUtils.setForNMS(splashPotionName, this.stackSplash);
      if (splashReg && splashNMS) {
         this.getLogger().info("Установлен максимальный размер стака (NMS)" + splashPotionName + " равно " + this.stackSplash);
      } else {
         this.getLogger()
            .warning(
               "Problem setting maxStack size of "
                  + splashPotionName
                  + ".\norg.bukkit.Material:"
                  + (splashReg ? "WORKED" : "FAILED")
                  + "\nNMS.Items:"
                  + (splashNMS ? "WORKED" : "FAILED")
            );
         this.getLogger().info("The above warning is normal when on 1.8");
      }
   }
}
