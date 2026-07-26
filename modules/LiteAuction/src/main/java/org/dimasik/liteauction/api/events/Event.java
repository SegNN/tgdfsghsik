package org.dimasik.liteauction.api.events;

import lombok.Generated;

public abstract class Event {
   private String name;
   private final boolean async;

   public Event() {
      this(false);
   }

   public Event(boolean isAsync) {
      this.async = isAsync;
   }

   public String getEventName() {
      if (this.name == null) {
         this.name = this.getClass().getSimpleName();
      }

      return this.name;
   }

   @Generated
   public boolean isAsync() {
      return this.async;
   }
}
