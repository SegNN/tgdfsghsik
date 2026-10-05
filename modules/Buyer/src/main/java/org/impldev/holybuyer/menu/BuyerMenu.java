package org.impldev.holybuyer.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.impldev.holybuyer.Texts;
import org.jetbrains.annotations.NotNull;

/**
 * Общий каркас меню скупщика.
 *
 * <p>Меню узнаётся по владельцу инвентаря, а не по заголовку: заголовок игрок видит и может
 * повторить в бочке с таким же названием, а владельца подделать нельзя.
 */
public abstract class BuyerMenu implements InventoryHolder {
   protected final Player player;
   protected final Inventory inventory;

   protected BuyerMenu(Player player, int size, String title) {
      this.player = player;
      this.inventory = Bukkit.createInventory(this, size, Texts.color(title));
   }

   @NotNull
   @Override
   public Inventory getInventory() {
      return this.inventory;
   }

   public void open() {
      this.render();
      this.player.openInventory(this.inventory);
   }

   /** Заполнить меню. Вызывается перед открытием и после каждого изменения. */
   public abstract void render();

   public abstract void click(InventoryClickEvent event);

   public void close(InventoryCloseEvent event) {
   }
}
