package org.dimasik.liteexchange.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import org.bukkit.Bukkit;
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

         try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), (String)null);
            profile.setProperty(new ProfileProperty("textures", base64));
            headMeta.setPlayerProfile(profile);
         } catch (RuntimeException var4) {
            // the 1.16 original swallowed reflection failures here; keep the head untextured
            // rather than letting a profile failure break the calling menu
            var4.printStackTrace();
         }

         head.setItemMeta(headMeta);
         return head;
      }
   }
}
