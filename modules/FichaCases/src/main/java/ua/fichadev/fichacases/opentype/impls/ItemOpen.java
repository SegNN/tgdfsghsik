package ua.fichadev.fichacases.opentype.impls;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.misc.ColorUtil;
import ua.fichadev.fichacases.opentype.Open;

public class ItemOpen extends Open {
   private final FichaCases plugin;
   private final LuckManager luckManager;
   private final int amount;
   private final String failedBuySlot;

   public ItemOpen(FichaCases plugin, LuckManager luckManager, int amount, String failedBuySlot) {
      super(Open.Type.ITEM);
      this.plugin = plugin;
      this.luckManager = luckManager;
      this.amount = amount;
      this.failedBuySlot = failedBuySlot;
   }

   @Override
   public void open(GameCase gameCase, ClickType type, Player whoClick) {
      ItemStack itemStack = this.plugin.getItemStack(gameCase.getName());
      whoClick.closeInventory();
      if (itemStack == null) {
         this.plugin.getLogger().severe(ColorUtil.parse("&cНе удалось получить предмет для кейса: " + gameCase.getName()));
         if (!whoClick.isOp()) {
            whoClick.sendMessage(ColorUtil.parse("&cПроизошла ошибка! Обратитесь к администрации сервера!"));
         } else {
            whoClick.sendMessage(ColorUtil.parse("&cНе удалось получить предмет для кейса: " + gameCase.getName()));
         }
      } else {
         int value = 0;
         ItemStack finalItemStack = itemStack.clone();
         finalItemStack.setAmount(1);

         for (ItemStack item : whoClick.getInventory().getContents()) {
            if (item != null && !item.getType().isAir() && item.isSimilar(finalItemStack)) {
               value += item.getAmount();
            }
         }

         if (this.amount > value) {
            whoClick.sendMessage(ColorUtil.parse(this.failedBuySlot.replace("%amount%", this.amount + "")));
         } else {
            finalItemStack.setAmount(this.amount);
            whoClick.getInventory().removeItem(new ItemStack[]{finalItemStack});
            gameCase.open(whoClick, this.luckManager.removeLast(whoClick));
         }
      }
   }
}
