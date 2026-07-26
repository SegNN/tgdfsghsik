package stickdev.siynes.stickhwcustom.menus.skins;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import stickdev.siynes.stickhwcustom.holders.SkinMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;

public class SkinsMenu {
   private final MenuItems items;

   public SkinsMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new SkinMenuHolder(), 54, "Трапки и станы");

      for (int i = 0; i < 54; i++) {
         inventory.setItem(i, this.items.gray());
      }

      inventory.setItem(2, this.items.getTrapSkin(player, Material.STRUCTURE_BLOCK, "Рандомный", "&x&f&b&0&f&d&4", true, "random"));
      inventory.setItem(10, this.items.getTrapSkin(player, Material.NETHER_BRICK_STAIRS, "Незер", "&x&d&3&6&b&7&e", true, "nether"));
      inventory.setItem(11, this.items.getTrapSkin(player, Material.RED_NETHER_BRICK_STAIRS, "Красный незер", "&c", true, "rednether"));
      inventory.setItem(12, this.items.getTrapSkin(player, Material.POLISHED_BLACKSTONE_STAIRS, "Чернит", "&x&0&0&d&8&f&f", true, "blackstone"));
      inventory.setItem(19, this.items.getTrapSkin(player, Material.QUARTZ_STAIRS, "Кварц", "&f", true, "quartz"));
      inventory.setItem(20, this.items.getTrapSkin(player, Material.CRIMSON_STAIRS, "Багровый", "&x&d&d&6&1&c&4", true, "crimson"));
      inventory.setItem(21, this.items.getTrapSkin(player, Material.WARPED_STAIRS, "Искажённый", "&x&4&e&d&8&c&9", true, "warped"));
      inventory.setItem(28, this.items.getTrapSkin(player, Material.RED_SANDSTONE_STAIRS, "Красный песчаник", "&x&e&7&7&e&2&a", true, "redsand"));
      inventory.setItem(29, this.items.getTrapSkin(player, Material.PRISMARINE_STAIRS, "Призмарин", "&x&9&b&d&f&b&e", true, "prismarine"));
      inventory.setItem(30, this.items.getTrapSkin(player, Material.PURPUR_STAIRS, "Пурпур", "&x&e&2&a&2&e&2", true, "purpur"));
      inventory.setItem(37, this.items.getTrapSkin(player, Material.STONE_BRICK_STAIRS, "Каменный кирпич", "&x&c&1&b&f&c&1", false, "stone"));
      inventory.setItem(38, this.items.getTrapSkin(player, Material.SPRUCE_STAIRS, "Ель", "&x&d&5&9&f&5&e", false, "spruce"));
      inventory.setItem(39, this.items.getTrapSkin(player, Material.END_STONE_BRICK_STAIRS, "Эндерняк", "&x&e&0&e&9&a&9", false, "endstone"));
      inventory.setItem(6, this.items.getStanSkin(player, Material.STRUCTURE_BLOCK, "Рандомный", "&x&f&b&0&f&d&4", true, "random"));
      inventory.setItem(14, this.items.getStanSkin(player, Material.BLACK_DYE, "Черный", "&x&0&0&d&8&f&f", true, "black"));
      inventory.setItem(15, this.items.getStanSkin(player, Material.RED_DYE, "Красный", "&c", true, "red"));
      inventory.setItem(16, this.items.getStanSkin(player, Material.WHITE_DYE, "Белый", "&f", true, "white"));
      inventory.setItem(23, this.items.getStanSkin(player, Material.ORANGE_DYE, "Оранжевая", "&6", true, "orange"));
      inventory.setItem(24, this.items.getStanSkin(player, Material.PURPLE_DYE, "Фиолетовый", "&x&b&2&5&7&f&9", true, "purple"));
      inventory.setItem(25, this.items.getStanSkin(player, Material.BLUE_DYE, "Синий", "&x&6&6&9&2&f&f", true, "blue"));
      inventory.setItem(32, this.items.getStanSkin(player, Material.LIME_DYE, "Лаймовый", "&a", true, "lime"));
      inventory.setItem(33, this.items.getStanSkin(player, Material.LIGHT_BLUE_DYE, "Светло-голубой", "&b", true, "light_blue"));
      inventory.setItem(34, this.items.getStanSkin(player, Material.MAGENTA_DYE, "Пурпурный", "&d", true, "magenta"));
      inventory.setItem(41, this.items.getStanSkin(player, Material.YELLOW_DYE, "Жёлтый", "&e", false, "yellow"));
      inventory.setItem(42, this.items.getStanSkin(player, Material.CYAN_DYE, "Голубой", "&x&4&c&c&3&f&4", false, "cyan"));
      inventory.setItem(43, this.items.getStanSkin(player, Material.GREEN_DYE, "Зелёный", "&x&9&a&d&d&3&6", false, "green"));
      inventory.setItem(53, this.items.back());
      player.openInventory(inventory);
   }
}
