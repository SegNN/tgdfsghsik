package ua.fichadev.fichacases.opentype.impls;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.misc.ColorUtil;
import ua.fichadev.fichacases.opentype.Open;

public class ClickOpen extends Open {
   private final FichaCases plugin;
   private final LuckManager luckManager;
   private final int price;
   private final String priceType;
   private final String commandChangePrice;
   private final String failedBuySlot;

   public ClickOpen(Open.Type type, FichaCases plugin, LuckManager luckManager, int price, String priceType, String commandChangePrice, String failedBuySlot) {
      super(type);
      this.plugin = plugin;
      this.luckManager = luckManager;
      this.price = price;
      this.priceType = priceType;
      this.commandChangePrice = commandChangePrice;
      this.failedBuySlot = failedBuySlot;
   }

   @Override
   public void open(GameCase gameCase, ClickType clickType, Player whoClick) {
      if (clickType.isLeftClick() && this.getType() == Open.Type.LEFT || clickType.isRightClick() && this.getType() == Open.Type.RIGHT) {
         whoClick.closeInventory();

         int value;
         try {
            value = Integer.parseInt(PlaceholderAPI.setPlaceholders(whoClick, this.priceType));
         } catch (NumberFormatException var6) {
            this.plugin.getLogger().severe(ColorUtil.parse("&cНе удалось получить число из плейсхолдера: " + this.priceType));
            if (!whoClick.isOp()) {
               whoClick.sendMessage(ColorUtil.parse("&cПроизошла ошибка! Обратитесь к администрации сервера!"));
            } else {
               whoClick.sendMessage(ColorUtil.parse("&cНе удалось получить число из плейсхолдера: " + this.priceType));
            }

            return;
         }

         if (this.price > value) {
            whoClick.sendMessage(ColorUtil.parse(this.failedBuySlot.replace("%amount%", this.price + "")));
         } else {
            Bukkit.dispatchCommand(
               Bukkit.getConsoleSender(), this.commandChangePrice.replace("%player%", whoClick.getName()).replace("%count%", this.price + "")
            );
            gameCase.open(whoClick, this.luckManager.removeLast(whoClick));
         }
      }
   }
}
