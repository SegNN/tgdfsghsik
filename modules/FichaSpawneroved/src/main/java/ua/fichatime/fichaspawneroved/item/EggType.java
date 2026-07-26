package ua.fichatime.fichaspawneroved.item;

public enum EggType {
   SHARD_EGG("Shard-Egg", "Shard-Egg"),
   END_SPLINTER_EGG("End-Splinter-Egg", "End-Splinter-Egg");

   private final String commandName;
   private final String configKey;

   private EggType(String commandName, String configKey) {
      this.commandName = commandName;
      this.configKey = configKey;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public String getConfigKey() {
      return this.configKey;
   }

   public static EggType fromCommandName(String name) {
      for (EggType type : values()) {
         if (type.commandName.equalsIgnoreCase(name)) {
            return type;
         }
      }

      return null;
   }

   public static EggType fromConfigKey(String key) {
      for (EggType type : values()) {
         if (type.configKey.equalsIgnoreCase(key)) {
            return type;
         }
      }

      return null;
   }
}
