package org.dimasik.liteauction.backend.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class ItemEncryptUtil {
   public static String encodeItem(ItemStack item) throws IOException {
      if (item == null) {
         return null;
      } else {
         String var3;
         try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            try {
               dataOutput.writeObject(item);
               dataOutput.flush();
               var3 = Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (Throwable var7) {
               try {
                  dataOutput.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            dataOutput.close();
         }

         return var3;
      }
   }

   public static ItemStack decodeItem(String base64) throws IOException, ClassNotFoundException {
      if (base64 != null && !base64.isEmpty()) {
         ItemStack var3;
         try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64))) {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
               var3 = (ItemStack)dataInput.readObject();
            } catch (Throwable var7) {
               try {
                  dataInput.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            dataInput.close();
         }

         return var3;
      } else {
         return null;
      }
   }

   @Generated
   private ItemEncryptUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
