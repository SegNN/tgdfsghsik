package org.dimasik.liteexchange.menu;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.dimasik.Native;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.RateData;
import org.dimasik.liteexchange.util.Parser;

@Native
public class RateSelect implements InventoryHolder {
   public static final String TITLE = "Введите курс обмена";
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
      AnvilView view = MenuType.ANVIL.builder().title(Component.text(TITLE)).checkReachable(false).build(this.player);
      this.anvilInventory = view.getTopInventory();
      this.anvilInventory.setItem(0, this.createBlackPane());
      this.anvilInventory.setItem(1, this.createRedPane());
      this.anvilInventory.setItem(2, this.createLimePane());
      this.player.openInventory(view);
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
}
