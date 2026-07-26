package ru.kotikstasika.kotiktotemrune.item;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.kotiktotemrune.KotikTotemRune;

public class Item {
   public Item() {
   }

   public ItemStack giveImmortalityRune(Player player, Integer amount) {
      ItemStack item = new ItemStack(Material.ORANGE_DYE, amount);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&A&2&6&9&0&0Р&x&A&E&6&F&0&0у&x&B&9&7&6&0&0н&x&C&5&7&C&0&0а &x&D&C&8&8&0&0«&x&E&8&8&F&0&0Б&x&F&3&9&5&0&0е&x&F&F&9&B&0&0с&x&F&3&9&5&0&0с&x&E&8&8&F&0&0м&x&D&C&8&8&0&0е&x&D&1&8&2&0&0р&x&C&5&7&C&0&0т&x&B&9&7&6&0&0и&x&A&E&6&F&0&0е&x&A&2&6&9&0&0»"
         )
      );
      itemMeta.addEnchant(Enchantment.LUCK, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(KotikTotemRune.getInstance(), "bessmertie"), PersistentDataType.INTEGER, 1);
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8Эффект руны"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " "));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &7Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &x&F&F&9&B&0&0 - после активации тотема с этим эффектом,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &0.&x&F&F&9&B&0&0   Вы получите неуязвимость к урону"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &0.&x&F&F&9&B&0&0   продолжительностью 3 секунды;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &x&F&F&9&B&0&0 - возможность наложить данный эффект"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &0.&x&F&F&9&B&0&0   на тотем через наковальню;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " "));
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack giveRecoveryRune(Player player, Integer amount) {
      ItemStack item = new ItemStack(Material.RED_DYE, amount);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&0&0&5&3«Восстановление»"));
      itemMeta.addEnchant(Enchantment.LUCK, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(KotikTotemRune.getInstance(), "vosstanovlenie"), PersistentDataType.INTEGER, 1);
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&8Эффект руны"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " "));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &7Особенности:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &x&F&F&0&0&5&3 - после активации тотема с этим эффектом,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &0.&x&F&F&0&0&5&3   У вас полностью восстановиться здоровье;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &x&F&F&0&0&5&3 - возможность наложить данный эффект"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5 &0.&x&F&F&0&0&5&3   на тотем через наковальню;"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " "));
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
      return item;
   }

   public ItemStack giveRune(Player player, Item.RuneType type, Integer amount) {
      switch (type) {
         case IMMORTALITY:
            return this.giveImmortalityRune(player, amount);
         case RECOVERY:
            return this.giveRecoveryRune(player, amount);
         default:
            return null;
      }
   }

   public static enum RuneType {
      IMMORTALITY,
      RECOVERY;

      private RuneType() {
      }
   }
}
