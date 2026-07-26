package ru.nezxenka.holypvp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PvpList {
   private final HolyPvP plugin;
   private final Player player;

   public PvpList(HolyPvP plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.open();
   }

   private void open() {
      Inventory inventory = Bukkit.createInventory(
         this.player,
         54,
         ChatColor.translateAlternateColorCodes(
            '&',
            "&0Доступные игры                                                                                                                                                                                                                                                                            [holypvp:list]"
         )
      );
      ItemStack borderItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
      ItemMeta borderMeta = borderItem.getItemMeta();
      borderMeta.setDisplayName(" ");
      borderItem.setItemMeta(borderMeta);
      inventory.setItem(0, borderItem);
      inventory.setItem(1, borderItem);
      inventory.setItem(2, borderItem);
      inventory.setItem(6, borderItem);
      inventory.setItem(7, borderItem);
      inventory.setItem(8, borderItem);
      inventory.setItem(45, borderItem);
      inventory.setItem(46, borderItem);
      inventory.setItem(47, borderItem);
      inventory.setItem(51, borderItem);
      inventory.setItem(52, borderItem);
      inventory.setItem(53, borderItem);
      borderItem.setType(Material.ORANGE_STAINED_GLASS_PANE);
      inventory.setItem(3, borderItem);
      inventory.setItem(4, borderItem);
      inventory.setItem(5, borderItem);
      inventory.setItem(48, borderItem);
      inventory.setItem(49, borderItem);
      inventory.setItem(50, borderItem);

      try {
         List<PlayerProfile> list = this.plugin.databaseManager.getAllPlayerProfiles().get();
         int slot = 9;

         for (PlayerProfile profile : list) {
            ItemStack profileItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
            ItemMeta profileMeta = profileItem.getItemMeta();
            List<String> loreList = new ArrayList<>();
            loreList.add(ChatColor.translateAlternateColorCodes('&', ""));
            if (profile.getFightType() == PlayerProfile.FightType.DUEL) {
               profileItem.setType(Material.LIME_CONCRETE);
               profileMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6● &x&E&7&E&7&E&7Поединок с: &6" + profile.getNickname()));
               loreList.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F● &fПоединок состоится на: &x&0&0&D&8&F&F" + profile.getServer()));
            } else {
               profileItem.setType(Material.PURPLE_CONCRETE);
               profileMeta.setDisplayName(
                  ChatColor.translateAlternateColorCodes('&', "&x&F&B&0&F&D&4● &x&E&7&E&7&E&7Смертельная схватка с: &x&F&B&0&F&D&4" + profile.getNickname())
               );
               loreList.add(
                  ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F● &fСмертельная схватка состоится на: &x&0&0&D&8&F&F" + profile.getServer())
               );
            }

            loreList.add(ChatColor.translateAlternateColorCodes('&', ""));
            loreList.add(ChatColor.translateAlternateColorCodes('&', "&6▶ &x&E&7&E&7&E&7Левый клик - &6начать поединок"));
            loreList.add(ChatColor.translateAlternateColorCodes('&', "&x&0&0&D&8&F&F▶ &x&E&7&E&7&E&7Правый клик - &x&0&0&D&8&F&Fинвентарь игрока"));
            profileMeta.setLore(loreList);
            profileMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "name"), PersistentDataType.STRING, profile.getNickname());
            profileItem.setItemMeta(profileMeta);
            inventory.setItem(slot, profileItem);
            slot++;
         }

         this.player.openInventory(inventory);
      } catch (Exception var11) {
         var11.printStackTrace();
      }
   }
}
