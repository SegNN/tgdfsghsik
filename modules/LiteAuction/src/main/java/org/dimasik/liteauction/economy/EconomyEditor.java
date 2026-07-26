package org.dimasik.liteauction.economy;

public abstract class EconomyEditor {
   public EconomyEditor() {
   }

   public abstract double getBalance(String var1);

   public abstract void addBalance(String var1, double var2);

   public abstract void subtractBalance(String var1, double var2);
}
