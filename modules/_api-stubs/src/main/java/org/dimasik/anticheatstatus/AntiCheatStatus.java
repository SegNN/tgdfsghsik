package org.dimasik.anticheatstatus;

import org.dimasik.anticheatstatus.manager.ConfigManager;
import org.dimasik.anticheatstatus.manager.DatabaseManager;

/** Compile-time stub for the AntiCheatStatus plugin, which is not shipped in this build. */
public class AntiCheatStatus {
   public ConfigManager configManager;
   public DatabaseManager databaseManager;

   public static AntiCheatStatus getInstance() {
      throw new UnsupportedOperationException("AntiCheatStatus is not installed");
   }
}
