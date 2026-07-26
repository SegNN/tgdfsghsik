package ua.holydev.holycases.opentype.impls;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ua.holydev.holycases.cases.GameCase;
import ua.holydev.holycases.cases.PurchasableCase;
import ua.holydev.holycases.lucks.LuckManager;
import ua.holydev.holycases.opentype.Open;

public class PurchaseOpen extends Open {
   private final LuckManager luckManager;

   public PurchaseOpen(LuckManager luckManager) {
      super(Open.Type.PURCHASE);
      this.luckManager = luckManager;
   }

   @Override
   public void open(GameCase gameCase, ClickType clickType, Player whoClick) {
      if (gameCase instanceof PurchasableCase purchasableCase) {
         purchasableCase.openWithPurchase(whoClick, clickType, this.luckManager.removeLast(whoClick));
      } else {
         whoClick.sendMessage("§cЭтот кейс не является покупным!");
      }
   }
}
