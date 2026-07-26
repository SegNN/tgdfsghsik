package org.dimasik.liteexchange.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.Native;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.Lot;
import org.dimasik.liteexchange.util.HeadUtil;
import org.dimasik.liteexchange.util.Parser;
import org.jetbrains.annotations.NotNull;

@Native
public class Main implements InventoryHolder {
   public HashMap<Integer, Lot> lots = new HashMap<>();
   public int page;
   public String playerName;
   private final LiteExchange plugin = LiteExchange.getInstance();

   public Main(Player player, int page) {
      this.playerName = player.getName();
      this.page = page;
      Inventory inventory = Bukkit.createInventory(this, 54, "Биржа");

      try {
         int slotIndex = 0;
         int startIndex = 36 * (page - 1);
         List<Lot> lots = this.plugin.dbCore.getLots().getAllLots().get();

         for (int i = startIndex; i < lots.size() && slotIndex < 36; i++) {
            Lot lot = lots.get(i);
            this.lots.put(slotIndex, lot);
            int count = lot.getCoins() / 5 + (lot.getCoins() % 5 == 0 ? 0 : 1);
            count = Math.min(count, 64);
            if (LiteExchange.selectedIds.getOrDefault(player, -1) == lot.getId()) {
               ItemStack selectedItem = new ItemStack(Material.LIME_WOOL);
               ItemMeta selectedMeta = selectedItem.getItemMeta();
               selectedMeta.setDisplayName(" ");
               ArrayList<String> selectedLore = new ArrayList<>();
               selectedLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
               selectedLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f Вы уверены, что хотите"));
               selectedLore.add(
                  Parser.color(
                     " &x&0&0&D&8&F&F&l&n▍&f потратить &x&F&2&F&F&0&0"
                        + Parser.formatNumber(lot.getCoins())
                        + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"
                  )
               );
               String var10001 = Parser.formatNumber(lot.getCoins() * lot.getRate());
               selectedLore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&f на &x&F&F&C&9&0&0" + var10001 + " &x&F&F&A&2&0&0¤ (монеток)&f?"));
               selectedLore.add(Parser.color(" &x&0&0&D&8&F&F&l▍"));
               selectedLore.add(Parser.color(""));
               selectedLore.add(Parser.color(" &x&0&5&F&B&0&0[ЛКМ]&x&E&7&E&7&E&7 — Подтвердить покупку"));
               selectedLore.add(Parser.color(""));
               selectedMeta.setLore(selectedLore);
               selectedItem.setItemMeta(selectedMeta);
               inventory.setItem(slotIndex, selectedItem);
            } else {
               ItemStack headItem = HeadUtil.setTexture(
                  new ItemStack(Material.PLAYER_HEAD, count),
                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVmNDg2MWFhNWIyMmVlMjhhOTBlNzVkYWI0NWQyMjFlZmQxNGMwYjFlY2M4ZWU5OThmYjY3ZTQzYmI4ZjNkZSJ9fX0="
               );
               ItemMeta headMeta = headItem.getItemMeta();
               String var10001 = Parser.color("&x&F&2&F&F&0&0 ▶ &fЗаявка игрока &x&F&2&F&F&0&0");
               headMeta.setDisplayName(var10001 + lot.getPlayerName());
               ArrayList<String> headLore = new ArrayList<>();
               headLore.add(Parser.color(""));
               headLore.add(Parser.color(" &x&F&2&F&F&0&0&l&n▍&f Курс: &x&F&F&C&9&0&0" + Parser.formatNumber(lot.getRate()) + " &x&F&F&A&2&0&0¤ (за 1 коин)"));
               headLore.add(
                  Parser.color(
                     " &x&F&2&F&F&0&0&l&n▍&f Ожидается: &x&F&2&F&F&0&0"
                        + Parser.formatNumber(lot.getCoins())
                        + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"
                  )
               );
               var10001 = Parser.formatNumber(lot.getCoins() * lot.getRate());
               headLore.add(Parser.color(" &x&F&2&F&F&0&0&l▍&f Доступно: &x&F&F&C&9&0&0" + var10001 + " &x&F&F&A&2&0&0¤ (монеток)"));
               headLore.add(Parser.color(""));
               headLore.add(Parser.color("&5&f &x&0&5&F&B&0&0[ЛКМ]&x&E&7&E&7&E&7 — Выкупить все монеты"));
               headLore.add(Parser.color("&5&f &x&0&0&D&8&F&F[ПКМ]&x&E&7&E&7&E&7 — Выкупить часть монеток"));
               headMeta.setLore(headLore);
               headItem.setItemMeta(headMeta);
               inventory.setItem(slotIndex, headItem);
            }

            slotIndex++;
         }
      } catch (Exception var26) {
      }

      for (int j = 36; j < 45; j++) {
         ItemStack orangePane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
         ItemMeta orangeMeta = orangePane.getItemMeta();
         orangeMeta.setDisplayName(" ");
         orangePane.setItemMeta(orangeMeta);
         inventory.setItem(j, orangePane);
      }

      for (int k = 45; k < 54; k++) {
         ItemStack bluePane = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
         ItemMeta blueMeta = bluePane.getItemMeta();
         blueMeta.setDisplayName(" ");
         bluePane.setItemMeta(blueMeta);
         inventory.setItem(k, bluePane);
      }

      ItemStack enderChest = new ItemStack(Material.ENDER_CHEST);
      ItemMeta enderMeta = enderChest.getItemMeta();
      ArrayList<String> enderLore = new ArrayList<>();

      try {
         Lot playerLot = this.plugin.dbCore.getLots().getPlayerLot(this.playerName).get();
         if (playerLot != null) {
            enderMeta.setDisplayName(Parser.color(" &x&F&2&F&F&0&0▶&f Ваша заявка"));
            enderLore.add(Parser.color(""));
            enderLore.add(
               Parser.color(" &x&F&2&F&F&0&0&l&n▍&f Курс: &x&F&F&C&9&0&0" + Parser.formatNumber(playerLot.getRate()) + " &x&F&F&A&2&0&0¤ (за 1 коин)")
            );
            enderLore.add(
               Parser.color(
                  " &x&F&2&F&F&0&0&l&n▍&f Ожидается: &x&F&2&F&F&0&0"
                     + Parser.formatNumber(playerLot.getCoins())
                     + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)"
               )
            );
            String var10001 = Parser.formatNumber(playerLot.getCoins() * playerLot.getRate());
            enderLore.add(Parser.color(" &x&F&2&F&F&0&0&l▍&f Доступно: &x&F&F&C&9&0&0" + var10001 + " &x&F&F&A&2&0&0¤ (монеток)"));
            enderLore.add(Parser.color(""));
            enderLore.add(Parser.color(" &c[ЛКМ]&x&E&7&E&7&E&7 — Снять заявку"));
         } else {
            enderMeta.setDisplayName(Parser.color(" "));
            enderLore.add(Parser.color(" &c▶&f Нет активной заявки на продажу монеток"));
            enderLore.add(Parser.color(""));
         }
      } catch (Exception var25) {
         enderMeta.setDisplayName(Parser.color(" "));
         enderLore.add(Parser.color(" &c▶&f Нет активной заявки на продажу монеток"));
         enderLore.add(Parser.color(""));
      }

      enderMeta.setLore(enderLore);
      enderChest.setItemMeta(enderMeta);
      inventory.setItem(45, enderChest);
      ItemStack sunflower = new ItemStack(Material.SUNFLOWER);
      ItemMeta sunflowerMeta = sunflower.getItemMeta();
      sunflowerMeta.setDisplayName(Parser.color(" "));
      ArrayList<String> sunflowerLore = new ArrayList<>();
      sunflowerLore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Заявка на продажу монеток &x&0&5&F&B&0&0[ЛКМ]"));
      sunflowerLore.add(Parser.color(""));
      sunflowerMeta.setLore(sunflowerLore);
      sunflower.setItemMeta(sunflowerMeta);
      inventory.setItem(40, sunflower);
      ItemStack prevArrow = new ItemStack(Material.ARROW);
      ItemMeta prevMeta = prevArrow.getItemMeta();
      prevMeta.setDisplayName(Parser.color(" &x&0&0&D&8&F&F▶&f Предыдущая страница"));
      prevArrow.setItemMeta(prevMeta);
      inventory.setItem(48, prevArrow);
      String coins = "Неизвестно";

      try {
         coins = Parser.formatNumber(this.plugin.dbCore.getCoins().getBalance(this.playerName).get());
      } catch (Exception var24) {
      }

      ItemStack headBalance = HeadUtil.setTexture(
         new ItemStack(Material.PLAYER_HEAD),
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVmNDg2MWFhNWIyMmVlMjhhOTBlNzVkYWI0NWQyMjFlZmQxNGMwYjFlY2M4ZWU5OThmYjY3ZTQzYmI4ZjNkZSJ9fX0="
      );
      ItemMeta headBalanceMeta = headBalance.getItemMeta();
      headBalanceMeta.setDisplayName(
         Parser.color(" &x&0&0&D&8&F&F▶&f Ваш баланс: &x&F&2&F&F&0&0" + coins + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0| (коинов)")
      );
      List<String> headBalanceLore = new ArrayList<>();
      headBalanceLore.add(Parser.color(""));
      headBalanceLore.add(Parser.color(" &x&0&5&F&B&0&0[ЛКМ]&x&E&7&E&7&E&7 — Обновить страницу"));
      headBalanceMeta.setLore(headBalanceLore);
      headBalance.setItemMeta(headBalanceMeta);
      inventory.setItem(49, headBalance);
      ItemStack nextArrow = new ItemStack(Material.ARROW);
      ItemMeta nextMeta = nextArrow.getItemMeta();
      nextMeta.setDisplayName(Parser.color(" &x&0&0&D&8&F&F▶&f Следующая страница"));
      nextArrow.setItemMeta(nextMeta);
      inventory.setItem(50, nextArrow);
      player.openInventory(inventory);
      Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
         if (inventory.getViewers().contains(player)) {
            new Main(player, page);
         }
      }, 40L);
   }

   @NotNull
   public Inventory getInventory() {
      return null;
   }

}
