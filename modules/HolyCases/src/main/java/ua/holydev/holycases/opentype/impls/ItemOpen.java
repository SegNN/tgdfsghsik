package ua.holydev.holycases.opentype.impls;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.cases.GameCase;
import ua.holydev.holycases.lucks.LuckManager;
import ua.holydev.holycases.misc.ColorUtil;
import ua.holydev.holycases.opentype.Open;

public class ItemOpen extends Open {
   private final HolyCases plugin;
   private final LuckManager luckManager;
   private final int amount;
   private final String failedBuySlot;

   public ItemOpen(HolyCases plugin, LuckManager luckManager, int amount, String failedBuySlot) {
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
