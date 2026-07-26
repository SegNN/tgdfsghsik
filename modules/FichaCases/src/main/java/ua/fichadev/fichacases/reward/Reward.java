package ua.fichadev.fichacases.reward;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import ua.fichadev.fichacases.lucks.Luck;
import ua.fichadev.fichacases.misc.RandomCollection;

public class Reward {
   private String name;
   private Map<String, Double> chances;
   private Material icon;
   private Color color;
   private boolean glow;
   private Map<Prize, Map<String, Double>> prizes;
   private boolean sendTitle;
   private boolean addInHistory;

   public Reward(String name, Map<String, Double> chances, Material icon, Color color, boolean glow, boolean sendTitle, boolean addInHistory) {
      this.name = name;
      this.chances = chances;
      this.icon = icon;
      this.color = color;
      this.glow = glow;
      this.prizes = new HashMap<>();
      this.sendTitle = sendTitle;
      this.addInHistory = addInHistory;
   }

   public ItemStack getIcon() {
      ItemStack itemStack = new ItemStack(this.icon);
      if (itemStack.getItemMeta() instanceof LeatherArmorMeta meta && this.color != null) {
         meta.setColor(this.color);
         itemStack.setItemMeta(meta);
      } else if (itemStack.getItemMeta() instanceof PotionMeta meta && this.color != null) {
         meta.setColor(this.color);
         itemStack.setItemMeta(meta);
      }

      if (this.glow) {
         ItemMeta meta = itemStack.getItemMeta();
         meta.addEnchant(Enchantment.DURABILITY, 1, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         itemStack.setItemMeta(meta);
      }

      return itemStack;
   }

   public double getChance(String luck) {
      return this.chances.get(luck) == null ? this.chances.get("no_luck") : this.chances.get(luck);
   }

   public String getName() {
      return this.name;
   }

   public void addPrize(Prize prize, Map<String, Double> chances) {
      this.prizes.put(prize, chances);
   }

   public Prize getRandomPrize(Luck luck) {
      RandomCollection<Prize> randomPrizes = new RandomCollection<>();
      String luckName = luck != null ? luck.getName() : "no_luck";

      for (Entry<Prize, Map<String, Double>> entry : this.prizes.entrySet()) {
         randomPrizes.add(entry.getValue().get(luckName), entry.getKey());
      }

      return randomPrizes.next();
   }

   public boolean isSendTitle() {
      return this.sendTitle;
   }

   public boolean isAddInHistory() {
      return this.addInHistory;
   }
}
