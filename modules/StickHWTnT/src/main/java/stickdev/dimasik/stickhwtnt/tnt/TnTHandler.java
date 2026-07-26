package stickdev.dimasik.stickhwtnt.tnt;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TnTHandler {
   private StickHWTnT plugin;

   public TnTHandler(StickHWTnT plugin) {
      this.plugin = plugin;
   }

   public ItemStack getExplosive(int count) {
      ItemStack itemStack = new ItemStack(Material.CLAY, count);
      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&D&5&E&B&F&FВ&x&D&2&E&7&F&Bз&x&C&E&E&4&F&7р&x&C&B&E&0&F&3ы&x&C&7&D&C&E&Fв&x&C&4&D&8&E&Bч&x&C&1&D&5&E&7а&x&B&D&D&1&E&3т&x&B&A&C&D&D&Fо&x&B&7&C&A&D&Bе&x&B&3&C&6&D&7 &x&B&0&C&2&D&3в&x&A&C&B&E&C&Fе&x&A&9&B&B&C&Bщ&x&A&6&B&7&C&7е&x&A&2&B&3&C&3с&x&9&F&A&F&B&Fт&x&9&B&A&C&B&Bв&x&9&8&A&8&B&7о"
         )
      );
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt-item"), PersistentDataType.STRING, "explosive");
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8 Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fиспользуется только для крафта"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "   &x&D&5&E&B&F&Fвзрывных предметов"));
      lore.add("");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public ItemStack getTnt(int count, TnTType tntType) {
      if (tntType == TnTType.TNT_GUN) {
         return this.createTntGun(count);
      } else {
         ItemStack itemStack = new ItemStack(Material.TNT, count);
         ItemMeta itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         switch (tntType) {
            case A:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&F&D&8&A&0&0Д&x&F&D&8&1&0&0и&x&F&D&7&7&0&0н&x&F&C&6&E&0&0а&x&F&C&6&4&0&0м&x&F&C&5&B&0&0и&x&F&C&5&1&0&0т &x&F&B&3&E&0&0A"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7имеет в &x&f&d&8&a&0&03 раза &x&e&7&e&7&e&7больший радуc взрыва."));
               lore.add("");
               break;
            case B:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&B&D&0&0&F&DД&x&C&4&0&0&F&Cи&x&C&B&0&0&F&Aн&x&D&2&0&0&F&9а&x&D&9&0&0&F&8м&x&E&1&0&0&F&6и&x&E&8&0&0&F&5т &x&F&6&0&0&F&2B"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7имеет в &x&B&D&0&0&F&D10 раз &x&e&7&e&7&e&7больший радуc взрыва."));
               lore.add("");
               break;
            case B2:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&0&0&0&0Д&x&F&7&0&0&0&0и&x&E&F&0&0&0&0н&x&E&7&0&0&0&0а&x&D&F&0&0&0&0м&x&D&7&0&0&0&0и&x&C&F&0&0&0&0т&x&C&7&0&0&0&0 &x&B&F&0&0&0&0Б&x&B&7&0&0&0&02"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7взрывает практически все блоки"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7   &x&F&F&0&0&0&0в радиусе 12 блоков&x&e&7&e&7&e&7."));
               lore.add("");
               break;
            case C4:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&E&D&4&A&F&BC&x&D&E&5&9&F&A4&x&D&0&6&8&F&A &x&C&1&7&7&F&9В&x&B&3&8&6&F&8з&x&A&4&9&5&F&8р&x&9&6&A&5&F&7ы&x&8&7&B&4&F&6в&x&7&8&C&3&F&6ч&x&6&A&D&2&F&5а&x&5&B&E&1&F&4т&x&4&D&F&0&F&4к&x&3&E&F&F&F&3а"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7разрушает &x&3&E&F&F&F&3блоки незеритового привата;"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7взрывает &x&3&E&F&F&F&3блоки обсидиана."));
               lore.add("");
               break;
            case WAVE:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&D&2&0&0&0&lР&x&F&D&1&E&0&E&lа&x&F&D&1&B&1&C&lз&x&F&D&1&9&2&A&lр&x&F&D&1&7&3&8&lы&x&F&D&1&5&4&6&lв&x&F&D&1&2&5&4&lн&x&F&D&1&0&6&2&lа&x&F&D&0&E&7&0&lя&x&F&D&0&B&7&E&l &x&F&D&0&9&8&C&lв&x&F&D&0&7&9&A&lо&x&F&D&0&5&A&8&lл&x&F&D&0&2&B&6&lн&x&F&D&0&0&C&4&lа"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7разрушает &x&3&E&F&F&F&3блоки в воде;"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7разрушает &x&3&E&F&F&F&3блоки незеритового привата;"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &x&e&7&e&7&e&7взрывает &x&3&E&F&F&F&3блоки обсидиана."));
               lore.add("");
               break;
            case ICE:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&A&4&F&F&F&AЛ&x&A&0&F&8&F&3е&x&9&B&F&2&E&Dд&x&9&7&E&B&E&6я&x&9&3&E&4&E&0н&x&8&E&D&E&D&9а&x&8&A&D&7&D&3я&x&8&6&D&0&C&C &x&8&1&C&A&C&5в&x&7&D&C&3&B&Fо&x&7&9&B&C&B&8л&x&7&4&B&6&B&2н&x&7&0&A&F&A&Bа"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fпри активации, образует на месте взрыва"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "   &fне полую сферу из льда &x&A&4&F&F&F&Aна 5 секунд&f."));
               lore.add("");
               break;
            case STEALER:
               itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&e&f&0&0&f&f&lСтиллер"));
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fпосле взрыва выпадает"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "   &fспавнер с мобом &x&0&b&e&2&f&fшансом в 50%&f."));
               lore.add("");
               break;
            case LUCK_STEALER:
               itemMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&0&B&E&2&F&FН&x&0&B&D&E&F&Aа&x&0&B&D&9&F&5д&x&0&A&D&5&F&1ё&x&0&A&D&1&E&Cж&x&0&A&C&D&E&7н&x&0&A&C&8&E&2ы&x&0&A&C&4&D&Dй&x&0&9&C&0&D&9 &x&0&9&B&C&D&4с&x&0&9&B&7&C&Fт&x&0&9&B&3&C&Aи&x&0&9&A&F&C&5л&x&0&8&A&B&C&1л&x&0&8&A&6&B&Cе&x&0&8&A&2&B&7р"
                  )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, tntType.name().toLowerCase());
               lore.add("");
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 Особенности:"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "&7 • &fпосле взрыва выпадает"));
               lore.add(ChatColor.translateAlternateColorCodes('&', "   &fспавнер с мобом &x&0&b&e&2&f&fшансом в 75%&f."));
               lore.add("");
         }

         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }

   private ItemStack createTntGun(int count) {
      ItemStack itemStack = new ItemStack(Material.DISPENSER, count);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "tnt"), PersistentDataType.STRING, TnTType.TNT_GUN.name().toLowerCase());
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&', "&x&F&B&6&8&6&8Т&x&F&B&5&C&5&Cн&x&F&B&5&0&5&0т&x&F&B&4&4&4&4-&x&F&B&3&8&3&8П&x&F&B&4&4&4&4у&x&F&B&5&0&5&0ш&x&F&B&5&C&5&Cк&x&F&B&6&8&6&8а"
         )
      );
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &x&E&7&E&7&E&7запускает &x&F&B&3&8&3&8летящий динамит"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&0.&7   &x&E&7&E&7&E&7со скоростью &x&F&B&3&8&3&8до 5 блоков за секунду&x&E&7&E&7&E&7;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &x&E&7&E&7&E&7при запуске, сохраняет свойства"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&0.&7   &x&F&B&3&8&3&8особых динамитов и пиротехники."));
      lore.add("");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }
}
