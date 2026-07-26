package org.dimasik.liteauction.backend.utils.tags;

import java.io.IOException;
import lombok.Generated;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;
import org.dimasik.liteauction.backend.utils.ItemNameUtil;
import org.dimasik.liteauction.backend.utils.nms.NBTUtil;

public final class ItemHoverUtil {
   public static void sendHoverItemMessage(Player player, String message, ItemStack hoverItem) {
      if (!message.contains("%item%")) {
         player.sendMessage(message);
      } else {
         String[] parts = message.split("%item%", 2);
         TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(parts[0]));
         TextComponent itemComponent = new TextComponent(TextComponent.fromLegacyText(getItemDisplayName(hoverItem)));
         itemComponent.setHoverEvent(new HoverEvent(Action.SHOW_ITEM, new Content[]{createHoverContent(hoverItem)}));
         if (textComponent.getExtra() != null && !textComponent.getExtra().isEmpty()) {
            BaseComponent last = (BaseComponent)textComponent.getExtra().get(textComponent.getExtra().size() - 1);
            itemComponent.setColor(last.getColorRaw());
            itemComponent.setBold(last.isBoldRaw());
            itemComponent.setItalic(last.isItalicRaw());
            itemComponent.setUnderlined(last.isUnderlinedRaw());
            itemComponent.setStrikethrough(last.isStrikethroughRaw());
            itemComponent.setObfuscated(last.isObfuscatedRaw());
         }

         textComponent.addExtra(itemComponent);
         if (parts.length > 1) {
            textComponent.addExtra(new TextComponent(TextComponent.fromLegacyText(parts[1])));
         }

         player.spigot().sendMessage(textComponent);
      }
   }

   private static Item createHoverContent(ItemStack item) {
      String nbtString = NBTUtil.getNBTAsString(item);
      return new Item(item.getType().getKey().toString(), item.getAmount(), ItemTag.ofNbt(nbtString.isEmpty() ? "{}" : nbtString));
   }

   private static String getItemDisplayName(ItemStack item) {
      return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
         ? item.getItemMeta().getDisplayName()
         : "[" + ItemNameUtil.getLocalizedItemName(item.getType()) + "]";
   }

   public static String getHoverItemMessage(String message, ItemStack hoverItem) {
      try {
         return ItemEncryptUtil.encodeItem(hoverItem) + " " + message;
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   @Generated
   private ItemHoverUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
