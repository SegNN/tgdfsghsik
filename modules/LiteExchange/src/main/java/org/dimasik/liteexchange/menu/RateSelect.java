package org.dimasik.liteexchange.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.ContainerAccess;
import net.minecraft.server.v1_16_R3.ContainerAnvil;
import net.minecraft.server.v1_16_R3.Containers;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindow;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.Native;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.RateData;
import org.dimasik.liteexchange.util.Parser;

@Native
public class RateSelect implements InventoryHolder {
   public final Player player;
   public int rate;
   public int sourceRate;
   public int coins;
   private AnvilInventory anvilInventory;

   public RateSelect(Player player, int rate, int coins) {
      this.player = player;
      this.rate = rate;
      this.sourceRate = rate;
      this.coins = coins;
      this.openAnvil();
      LiteExchange.rateDatas.put(player, new RateData(rate, coins));
   }

   private void openAnvil() {
      EntityPlayer nmsPlayer = ((CraftPlayer)this.player).getHandle();
      ContainerAnvil container = new RateSelect.CustomAnvilContainer(nmsPlayer, this);
      this.anvilInventory = (AnvilInventory)container.getBukkitView().getTopInventory();
      this.anvilInventory.setItem(0, this.createBlackPane());
      this.anvilInventory.setItem(1, this.createRedPane());
      this.anvilInventory.setItem(2, this.createLimePane());
      nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, Containers.ANVIL, new ChatMessage("Введите курс обмена")));
      nmsPlayer.activeContainer = container;
      nmsPlayer.activeContainer.addSlotListener(nmsPlayer);
   }

   private ItemStack createBlackPane() {
      ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(String.valueOf(this.rate));
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Сбросить на начальное значение"));
      lore.add(Parser.color(""));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createRedPane() {
      ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Отменить ввод курса"));
      lore.add(Parser.color(""));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createLimePane() {
      ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Утвердить новый курс &x&0&5&F&B&0&0[ЛКМ]"));
      lore.add(Parser.color(""));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   public Inventory getInventory() {
      return this.anvilInventory;
   }

   private static class CustomAnvilContainer extends ContainerAnvil {
      private final RateSelect holder;

      public CustomAnvilContainer(EntityPlayer player, RateSelect holder) {
         super(player.nextContainerCounter(), player.inventory, ContainerAccess.at(player.world, new BlockPosition(0, 0, 0)));
         this.holder = holder;
         this.checkReachable = false;
      }

      public InventoryHolder getOwner() {
         return this.holder;
      }
   }
}
