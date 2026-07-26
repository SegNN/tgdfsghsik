package org.dimasik.shame.modules;

import org.bukkit.event.Listener;
import org.dimasik.shame.Shame;

public abstract class Module implements Listener {
   public Module() {
   }

   public void registerListener() {
      Shame.getInstance().getServer().getPluginManager().registerEvents(this, Shame.getInstance());
   }
}
