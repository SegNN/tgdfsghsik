package ua.holydev.holycases.opentype.impls;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ua.holydev.holycases.cases.GameCase;
import ua.holydev.holycases.opentype.Open;

public class DoubleClickOpen extends Open {
   private final ClickOpen leftClick;
   private final ClickOpen rightClick;

   public DoubleClickOpen(ClickOpen leftClick, ClickOpen rightClick) {
      super(Open.Type.LEFTRIGHT);
      this.leftClick = leftClick;
      this.rightClick = rightClick;
   }

   @Override
   public void open(GameCase gameCase, ClickType type, Player whoClick) {
      if (type.isRightClick()) {
         this.rightClick.open(gameCase, type, whoClick);
      } else if (type.isLeftClick()) {
         this.leftClick.open(gameCase, type, whoClick);
      }
   }
}
