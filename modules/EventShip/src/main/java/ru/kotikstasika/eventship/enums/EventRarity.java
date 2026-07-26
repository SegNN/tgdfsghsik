package ru.kotikstasika.eventship.enums;

public enum EventRarity {
   DEFAULT("default"),
   PROSPEROUS("prosperous"),
   LUXURIOUS("luxurious");

   private final String displayName;

   private EventRarity(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
