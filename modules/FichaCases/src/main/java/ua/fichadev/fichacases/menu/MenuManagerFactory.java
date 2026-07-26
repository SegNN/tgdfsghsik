package ua.fichadev.fichacases.menu;

import org.bukkit.Bukkit;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.menu.impl.version.v1_16_5.MenuManagerImpl;

public class MenuManagerFactory {
   public MenuManagerFactory() {
   }

   public MenuManager createMenuManager(FichaCases plugin) {
      String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
      // MenuManagerImpl no longer touches any CraftBukkit internals, so the same implementation
      // serves both the legacy 1.16.5 server and the current 1.21.4 one.
      return switch (version) {
         case "1.16.5", "1.21.4" -> new MenuManagerImpl(plugin);
         default -> null;
      };
   }
}
