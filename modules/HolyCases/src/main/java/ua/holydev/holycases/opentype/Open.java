package ua.holydev.holycases.opentype;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ua.holydev.holycases.cases.GameCase;

public abstract class Open {
   private final Open.Type type;

   public Open(Open.Type type) {
      this.type = type;
   }

   public abstract void open(GameCase var1, ClickType var2, Player var3);

   public Open.Type getType() {
      return this.type;
   }

   public static enum Type {
      KEY,
      LEFT,
      RIGHT,
      LEFTRIGHT,
      ITEM,
      PURCHASE;

      private Type() {
      }

      private static Open.Type[] $values() {
         return new Open.Type[]{KEY, LEFT, RIGHT, LEFTRIGHT, ITEM, PURCHASE};
      }
   }
}
