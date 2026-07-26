package ua.fichadev.fichacases.menu;

import org.bukkit.Bukkit;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.menu.impl.version.v1_16_5.MenuManagerImpl;

public class MenuManagerFactory {
   public MenuManagerFactory() {
   }

   public MenuManager createMenuManager(FichaCases plugin) {
      String var2 = Bukkit.getServer().getBukkitVersion().split("-")[0];
      byte var3 = -1;
      switch (var2.hashCode()) {
         case 1446823497:
            if (var2.equals("1.16.5")) {
               var3 = 0;
            }
         default:
            switch (var3) {
               case 0:
                  return new MenuManagerImpl(plugin);
               default:
                  return null;
            }
      }
   }
}
