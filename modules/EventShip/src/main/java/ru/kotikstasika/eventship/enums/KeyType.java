package ru.kotikstasika.eventship.enums;

public enum KeyType {
   DEFAULT("def"),
   RARE("red"),
   UNIQUE("unik"),
   SECRET("secret");

   private final String configKey;

   private KeyType(String configKey) {
      this.configKey = configKey;
   }

   public String getConfigKey() {
      return this.configKey;
   }
}
