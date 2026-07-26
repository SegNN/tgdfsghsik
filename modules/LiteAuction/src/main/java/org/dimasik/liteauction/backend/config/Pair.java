package org.dimasik.liteauction.backend.config;

import java.util.Objects;
import lombok.Generated;

public class Pair<L, R> {
   private L left;
   private R right;

   public Pair(L left, R right) {
      this.left = left;
      this.right = right;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Pair<?, ?> pair = (Pair<?, ?>)o;
         return Objects.equals(this.left, pair.left) && Objects.equals(this.right, pair.right);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.left, this.right);
   }

   @Override
   public String toString() {
      return "Pair{" + this.left + ", " + this.right + "}";
   }

   @Generated
   public L getLeft() {
      return this.left;
   }

   @Generated
   public R getRight() {
      return this.right;
   }

   @Generated
   public void setLeft(L left) {
      this.left = left;
   }

   @Generated
   public void setRight(R right) {
      this.right = right;
   }
}
