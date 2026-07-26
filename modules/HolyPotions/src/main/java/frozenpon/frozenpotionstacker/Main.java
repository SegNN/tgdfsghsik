package frozenpon.frozenpotionstacker;

import frozenpon.frozenpotionstacker.Utils.InventoryListener;
import frozenpon.frozenpotionstacker.Utils.ReflectionUtils;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   private int stackPotion;
   private int stackSplash;
   private InventoryListener listener;

   public Main() {
   }

   public void onEnable() {
      this.loadConfiguration();
      this.changePotionStackSizes();
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
      } else {
         this.listener.setStacks(this.stackPotion, this.stackSplash);
      }
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
