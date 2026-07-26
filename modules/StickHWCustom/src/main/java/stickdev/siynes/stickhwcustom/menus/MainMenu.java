package stickdev.siynes.stickhwcustom.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.items.StarItem;
import stickdev.siynes.stickhwcustom.menus.colorname.ColorNameMenu;
import stickdev.siynes.stickhwcustom.menus.particle.ParticleMenu;
import stickdev.siynes.stickhwcustom.menus.prem.PremPearlMenu;
import stickdev.siynes.stickhwcustom.menus.skins.SkinsMenu;
import stickdev.siynes.stickhwcustom.menus.sounds.SoundHitMenu;
import stickdev.siynes.stickhwcustom.menus.star.StarMenu;

public class MainMenu extends Menu {
   private final MenuItems items;
   private Inventory inventory;

   public MainMenu(MenuItems items) {
      this.items = items;
      this.setupButtons();
   }

   private void setupButtons() {
      this.buttons.add(new Button(11, this.items.sounds(), e -> {
         SoundHitMenu soundMenu = new SoundHitMenu(this.items);
         soundMenu.open((Player)e.getWhoClicked());
      }));
      this.buttons.add(new Button(33, this.items.traps(), e -> {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
         if (plugin != null) {
            SkinsMenu skinsMenu = new SkinsMenu(this.items);
            skinsMenu.open((Player)e.getWhoClicked());
         } else {
            e.getWhoClicked().sendMessage("Данный раздел недоступен");
            Bukkit.getLogger().info("HolyLiteItems не найден!");
         }
      }));
      this.buttons.add(new Button(15, this.items.particle(), e -> {
         ParticleMenu particleMenu = new ParticleMenu(this.items);
         particleMenu.open((Player)e.getWhoClicked());
      }));
      this.buttons.add(new Button(13, this.items.premka(), e -> {
         if (e.getWhoClicked().hasPermission("stickhwcustom.prem")) {
            PremPearlMenu premPearlMenu = new PremPearlMenu(this.items);
            premPearlMenu.open((Player)e.getWhoClicked());
         }
      }));
      this.buttons.add(new Button(29, this.items.nickcolor(), e -> {
         if (e.getWhoClicked().hasPermission("stickhwcustom.prem")) {
            ColorNameMenu colorNameMenu = new ColorNameMenu(this.items);
            colorNameMenu.open((Player)e.getWhoClicked());
         }
      }));
      this.buttons.add(new Button(31, this.items.star(), e -> {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("StickHWStar");
         if (plugin != null) {
            StarItem starItem = new StarItem();
            StarMenu starMenu = new StarMenu(this.items, starItem);
            starMenu.open((Player)e.getWhoClicked());
         } else {
            e.getWhoClicked().sendMessage("Данный раздел недоступен");
            Bukkit.getLogger().info("StickHWStar не найден!");
         }
      }));
   }

   @Override
   public void open(Player player) {
      this.inventory = Bukkit.createInventory(this, 45, "Кастомизация");
      this.buttons.forEach(button -> this.inventory.setItem(button.getSlot(), button.getItem()));
      player.openInventory(this.inventory);
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
