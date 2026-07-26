package org.dimasik.liteexchange.util;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.Native;

@Native
public class PacketSender {
   public PacketSender() {
   }

   public static void sendAnvilSlotUpdate(Player player, int slot, ItemStack itemStack) {
      net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
      EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
      PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(nmsPlayer.activeContainer.windowId, slot, nmsItem);
      nmsPlayer.playerConnection.sendPacket(packet);
   }
}
