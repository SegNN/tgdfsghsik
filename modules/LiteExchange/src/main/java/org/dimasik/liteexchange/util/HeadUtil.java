package org.dimasik.liteexchange.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.dimasik.Native;

@Native
public class HeadUtil {
   public HeadUtil() {
   }

   public static ItemStack setTexture(ItemStack head, String base64) {
      if (base64.isEmpty()) {
         return head;
      } else {
         SkullMeta headMeta = (SkullMeta)head.getItemMeta();
         GameProfile profile = new GameProfile(UUID.randomUUID(), (String)null);
         profile.getProperties().put("textures", new Property("textures", base64));

         try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
         } catch (NoSuchFieldException | IllegalAccessException var51) {
            var51.printStackTrace();
         }

         head.setItemMeta(headMeta);
         return head;
      }
   }
}
