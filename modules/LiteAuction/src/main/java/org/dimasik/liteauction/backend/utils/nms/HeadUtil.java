package org.dimasik.liteauction.backend.utils.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class HeadUtil {
   public static ItemStack setTexture(ItemStack head, String base64) {
      if (base64.isEmpty()) {
         return head;
      } else {
         SkullMeta headMeta = (SkullMeta)head.getItemMeta();
         GameProfile profile = new GameProfile(UUID.randomUUID(), null);
         profile.getProperties().put("textures", new Property("textures", base64));

         try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
         } catch (IllegalAccessException | NoSuchFieldException var5) {
            throw new RuntimeException(var5);
         }

         head.setItemMeta(headMeta);
         return head;
      }
   }

   @Generated
   private HeadUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
