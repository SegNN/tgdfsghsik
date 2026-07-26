package org.dimasik.litebinds.database;

import lombok.Generated;

public class PlayerActions {
   private String player;
   private ActionType actionDrop;
   private ActionType actionSwap;
   private ActionType actionInteract;

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public ActionType getActionDrop() {
      return this.actionDrop;
   }

   @Generated
   public ActionType getActionSwap() {
      return this.actionSwap;
   }

   @Generated
   public ActionType getActionInteract() {
      return this.actionInteract;
   }

   @Generated
   public void setPlayer(String player) {
      this.player = player;
   }

   @Generated
   public void setActionDrop(ActionType actionDrop) {
      this.actionDrop = actionDrop;
   }

   @Generated
   public void setActionSwap(ActionType actionSwap) {
      this.actionSwap = actionSwap;
   }

   @Generated
   public void setActionInteract(ActionType actionInteract) {
      this.actionInteract = actionInteract;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof PlayerActions other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$player = this.getPlayer();
         Object other$player = other.getPlayer();
         if (this$player == null ? other$player == null : this$player.equals(other$player)) {
            Object this$actionDrop = this.getActionDrop();
            Object other$actionDrop = other.getActionDrop();
            if (this$actionDrop == null ? other$actionDrop == null : this$actionDrop.equals(other$actionDrop)) {
               Object this$actionSwap = this.getActionSwap();
               Object other$actionSwap = other.getActionSwap();
               if (this$actionSwap == null ? other$actionSwap == null : this$actionSwap.equals(other$actionSwap)) {
                  Object this$actionInteract = this.getActionInteract();
                  Object other$actionInteract = other.getActionInteract();
                  return this$actionInteract == null ? other$actionInteract == null : this$actionInteract.equals(other$actionInteract);
               } else {
                  return false;
               }
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
      return other instanceof PlayerActions;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $player = this.getPlayer();
      result = result * 59 + ($player == null ? 43 : $player.hashCode());
      Object $actionDrop = this.getActionDrop();
      result = result * 59 + ($actionDrop == null ? 43 : $actionDrop.hashCode());
      Object $actionSwap = this.getActionSwap();
      result = result * 59 + ($actionSwap == null ? 43 : $actionSwap.hashCode());
      Object $actionInteract = this.getActionInteract();
      return result * 59 + ($actionInteract == null ? 43 : $actionInteract.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerActions(player="
         + this.getPlayer()
         + ", actionDrop="
         + this.getActionDrop()
         + ", actionSwap="
         + this.getActionSwap()
         + ", actionInteract="
         + this.getActionInteract()
         + ")";
   }

   @Generated
   public PlayerActions() {
   }

   @Generated
   public PlayerActions(String player, ActionType actionDrop, ActionType actionSwap, ActionType actionInteract) {
      this.player = player;
      this.actionDrop = actionDrop;
      this.actionSwap = actionSwap;
      this.actionInteract = actionInteract;
   }
}
