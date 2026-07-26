package org.dimasik.liteauction.backend.utils.nms;

import java.lang.reflect.Method;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public final class NBTUtil {
   public static String getNBTAsString(ItemStack itemStack) {
      if (itemStack != null && !itemStack.getType().isAir()) {
         try {
            Class<?> craftItemStackClass = VersionUtil.getCraftItemStackClass();
            Method asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopyMethod.invoke(null, itemStack);
            Method getTagMethod = nmsItem.getClass().getMethod("getTag");
            Object tag = getTagMethod.invoke(nmsItem);
            return tag == null ? "" : tag.toString();
         } catch (Exception var6) {
            return "";
         }
      } else {
         return "";
      }
   }

   @Generated
   private NBTUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
