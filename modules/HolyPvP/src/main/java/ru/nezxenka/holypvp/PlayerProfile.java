package ru.nezxenka.holypvp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class PlayerProfile {
   private final String nickname;
   private String server;
   private PlayerProfile.FightType fightType;
   private Inventory inventory;
   private String hash;

   public PlayerProfile(String nickname, String server, PlayerProfile.FightType fightType, Inventory inventory) {
      this.nickname = nickname;
      this.server = server;
      this.fightType = fightType;
      this.inventory = inventory;
      this.hash = inventoryToBase64(inventory);
   }

   public String getNickname() {
      return this.nickname;
   }

   public String getServer() {
      return this.server;
   }

   public void setServer(String server) {
      this.server = server;
   }

   public PlayerProfile.FightType getFightType() {
      return this.fightType;
   }

   public void setFightType(PlayerProfile.FightType fightType) {
      this.fightType = fightType;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
      this.hash = inventoryToBase64(inventory);
   }

   public String getHash() {
      return this.hash;
   }

   public static String inventoryToBase64(Inventory inventory) throws IllegalStateException {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeInt(41);

         for (int i = 0; i <= 40; i++) {
            ItemStack item = inventory.getItem(i);
            dataOutput.writeObject(item);
         }

         dataOutput.close();
         return Base64Coder.encodeLines(outputStream.toByteArray());
      } catch (IOException var51) {
         throw new IllegalStateException("Unable to save inventory to base64.", var51);
      }
   }

   public static Inventory base64ToInventory(String base64) throws IOException {
      if (base64 != null && !base64.isEmpty()) {
         try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory((InventoryHolder)null, 54);
            int size = dataInput.readInt();
            if (size != 41) {
               throw new IOException("Invalid inventory data size: expected 41, got " + size);
            } else {
               for (int i = 0; i <= 40; i++) {
                  try {
                     ItemStack item = (ItemStack)dataInput.readObject();
                     inventory.setItem(i, item);
                  } catch (ClassNotFoundException var71) {
                     throw new IOException("Unable to decode item class type.", var71);
                  }
               }

               dataInput.close();
               return inventory;
            }
         } catch (Exception var81) {
            throw new IOException("Failed to decode inventory: " + var81.getMessage(), var81);
         }
      } else {
         return Bukkit.createInventory((InventoryHolder)null, 54);
      }
   }

   public static enum FightType {
      DUEL,
      DEADLY_FIGHT;

      private FightType() {
      }
   }
}
