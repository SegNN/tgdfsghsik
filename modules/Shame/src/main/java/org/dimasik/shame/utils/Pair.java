package org.dimasik.shame.utils;

import lombok.Generated;

public class Pair<F, S> {
   private F first;
   private S second;

   @Generated
   public void setFirst(F first) {
      this.first = first;
   }

   @Generated
   public void setSecond(S second) {
      this.second = second;
   }

   @Generated
   public Pair(F first, S second) {
      this.first = first;
      this.second = second;
   }

   @Generated
   public F getFirst() {
      return this.first;
   }

   @Generated
   public S getSecond() {
      return this.second;
   }
}
