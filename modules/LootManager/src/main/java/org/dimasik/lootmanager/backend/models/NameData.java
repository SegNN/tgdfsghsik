package org.dimasik.lootmanager.backend.models;

import lombok.Generated;
import org.dimasik.lootmanager.frontend.menus.Configs;

public class NameData {
   private Configs back;
   private String name;
   private Boolean forceClose;

   @Generated
   public Configs getBack() {
      return this.back;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Boolean getForceClose() {
      return this.forceClose;
   }

   @Generated
   public void setBack(Configs back) {
      this.back = back;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setForceClose(Boolean forceClose) {
      this.forceClose = forceClose;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof NameData other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$forceClose = this.getForceClose();
         Object other$forceClose = other.getForceClose();
         if (this$forceClose == null ? other$forceClose == null : this$forceClose.equals(other$forceClose)) {
            Object this$back = this.getBack();
            Object other$back = other.getBack();
            if (this$back == null ? other$back == null : this$back.equals(other$back)) {
               Object this$name = this.getName();
               Object other$name = other.getName();
               return this$name == null ? other$name == null : this$name.equals(other$name);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof NameData;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $forceClose = this.getForceClose();
      result = result * 59 + ($forceClose == null ? 43 : $forceClose.hashCode());
      Object $back = this.getBack();
      result = result * 59 + ($back == null ? 43 : $back.hashCode());
      Object $name = this.getName();
      return result * 59 + ($name == null ? 43 : $name.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "NameData(back=" + this.getBack() + ", name=" + this.getName() + ", forceClose=" + this.getForceClose() + ")";
   }

   @Generated
   public NameData(Configs back, String name, Boolean forceClose) {
      this.back = back;
      this.name = name;
      this.forceClose = forceClose;
   }
}
