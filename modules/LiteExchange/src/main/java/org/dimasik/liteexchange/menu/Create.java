package org.dimasik.liteexchange.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.Native;
import org.dimasik.liteexchange.util.HeadUtil;
import org.dimasik.liteexchange.util.Parser;
import org.jetbrains.annotations.NotNull;

@Native
public class Create implements InventoryHolder {
   public Player player;
   public int rate;
   public int coins;
   public boolean forceClose = false;

   public Create(Player player, int rate, int coins) {
      this.player = player;
      this.rate = rate;
      this.coins = coins;
      Inventory inventory = Bukkit.createInventory(this, 27, "Покупка");
      ItemStack redPane50 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 10);
      ItemMeta redMeta50 = redPane50.getItemMeta();
      redMeta50.setDisplayName(Parser.color(" &c▶&f Вычесть &x&F&2&F&F&0&050 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      redPane50.setItemMeta(redMeta50);
      inventory.setItem(9, redPane50);
      ItemStack redPane25 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 5);
      ItemMeta redMeta25 = redPane25.getItemMeta();
      redMeta25.setDisplayName(Parser.color(" &c▶&f Вычесть &x&F&2&F&F&0&025 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      redPane25.setItemMeta(redMeta25);
      inventory.setItem(10, redPane25);
      ItemStack redPane10 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 2);
      ItemMeta redMeta10 = redPane10.getItemMeta();
      redMeta10.setDisplayName(Parser.color(" &c▶&f Вычесть &x&F&2&F&F&0&010 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      redPane10.setItemMeta(redMeta10);
      inventory.setItem(11, redPane10);
      ItemStack redPane5 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
      ItemMeta redMeta5 = redPane5.getItemMeta();
      redMeta5.setDisplayName(Parser.color(" &c▶&f Вычесть &x&F&2&F&F&0&05 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      redPane5.setItemMeta(redMeta5);
      inventory.setItem(12, redPane5);
      int count = coins / 5 + (coins % 5 == 0 ? 0 : 1);
      count = Math.min(count, 64);
      ItemStack headItem = HeadUtil.setTexture(
         new ItemStack(Material.PLAYER_HEAD, count),
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVmNDg2MWFhNWIyMmVlMjhhOTBlNzVkYWI0NWQyMjFlZmQxNGMwYjFlY2M4ZWU5OThmYjY3ZTQzYmI4ZjNkZSJ9fX0="
      );
      ItemMeta headMeta = headItem.getItemMeta();
      headMeta.setDisplayName(Parser.color(" &x&F&2&F&F&0&0▶&f Ваша заявка"));
      List<String> headLore = new ArrayList<>();
      headLore.add(Parser.color(""));
      headLore.add(
         Parser.color(
            " &x&F&2&F&F&0&0&l&n▍&f Вы получите: &x&F&2&F&F&0&0" + Parser.formatNumber(coins) + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"
         )
      );
      headLore.add(Parser.color(" &x&F&2&F&F&0&0&l&n▍&f Вы отдадите: &x&F&F&C&9&0&0" + Parser.formatNumber(coins * rate) + " &x&F&F&A&2&0&0¤ (монеток)"));
      headLore.add(Parser.color(" &x&F&2&F&F&0&0&l▍&f Курс: &x&F&F&C&9&0&0" + Parser.formatNumber(rate) + " &x&F&F&A&2&0&0¤ (за 1 коин)"));
      headLore.add(Parser.color(""));
      headLore.add(Parser.color(" &x&0&5&F&B&0&0[ЛКМ]&x&E&7&E&7&E&7 — Создать заявку"));
      headLore.add(Parser.color(" &c[ПКМ]&x&E&7&E&7&E&7 — Изменить курс"));
      headMeta.setLore(headLore);
      headItem.setItemMeta(headMeta);
      inventory.setItem(13, headItem);
      ItemStack limePane5 = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
      ItemMeta limeMeta5 = limePane5.getItemMeta();
      limeMeta5.setDisplayName(Parser.color(" &x&0&5&F&B&0&0▶&f Добавить &x&F&2&F&F&0&05 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      limePane5.setItemMeta(limeMeta5);
      inventory.setItem(14, limePane5);
      ItemStack limePane10 = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 2);
      ItemMeta limeMeta10 = limePane10.getItemMeta();
      limeMeta10.setDisplayName(Parser.color(" &x&0&5&F&B&0&0▶&f Добавить &x&F&2&F&F&0&010 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      limePane10.setItemMeta(limeMeta10);
      inventory.setItem(15, limePane10);
      ItemStack limePane25 = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 5);
      ItemMeta limeMeta25 = limePane25.getItemMeta();
      limeMeta25.setDisplayName(Parser.color(" &x&0&5&F&B&0&0▶&f Добавить &x&F&2&F&F&0&025 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      limePane25.setItemMeta(limeMeta25);
      inventory.setItem(16, limePane25);
      ItemStack limePane50 = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 10);
      ItemMeta limeMeta50 = limePane50.getItemMeta();
      limeMeta50.setDisplayName(Parser.color(" &x&0&5&F&B&0&0▶&f Добавить &x&F&2&F&F&0&050 &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"));
      limePane50.setItemMeta(limeMeta50);
      inventory.setItem(17, limePane50);
      ItemStack bookItem = new ItemStack(Material.WRITABLE_BOOK, 1);
      ItemMeta bookMeta = bookItem.getItemMeta();
      bookMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F▶&f Хочешь &x&0&0&D&8&F&Fобменять монетки&f на Коины?"));
      List<String> bookLore = new ArrayList<>();
      bookLore.add(Parser.color(""));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f Выбери желаемое &x&0&0&D&8&F&Fколичество Коинов"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f и нажми &x&0&0&D&8&F&F\"Создать заявку\"&f!"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f Вы &x&0&0&D&8&F&Fполучите коины&f,"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f как только &x&0&0&D&8&F&Fкупят монетки&f у Вас!"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f Для отмены - закройте инвентарь."));
      bookLore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&f Срок жизни заявки - &x&0&0&D&8&F&F12 часов&f."));
      bookLore.add(Parser.color(""));
      bookMeta.setLore(bookLore);
      bookItem.setItemMeta(bookMeta);
      inventory.setItem(18, bookItem);
      player.openInventory(inventory);
   }

   @NotNull
   public Inventory getInventory() {
      return null;
   }

}
