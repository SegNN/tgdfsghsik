package org.impldev.holybuyer;

import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * Один товар скупщика.
 *
 * @param id       ключ в items.yml, он же ключ во всех счётчиках
 * @param category к какой категории относится
 * @param name     как подписан в меню
 * @param price    начальная цена за штуку из вики
 * @param points   очки скупщика за штуку
 * @param icon     что показывать в меню
 * @param materials все предметы, которые принимаются по этой цене
 * @param potion   тип зелья, если товар - зелье; иначе null
 * @param also     строки "А также доступно для продажи"
 */
public record BuyerItem(String id, String category, String name, double price, double points,
                        Material icon, Set<Material> materials, PotionType potion, List<String> also) {

   /**
    * Подходит ли предмет из инвентаря под этот товар.
    *
    * <p>Зелья отличаются только типом эффекта, поэтому одного материала мало: зелье силы и
    * обычная вода - это оба {@code POTION}, а цены у них разные. Предмет с чужим именем или
    * зачарованием не принимается: иначе именное оружие ушло бы скупщику по цене материала.
    */
   public boolean matches(ItemStack stack) {
      if (stack == null || !this.materials.contains(stack.getType())) {
         return false;
      }

      if (this.potion != null) {
         return stack.getItemMeta() instanceof PotionMeta meta && meta.getBasePotionType() == this.potion;
      }

      return !isSpecial(stack);
   }

   /** Предмет, который явно не является обычным ресурсом. */
   private static boolean isSpecial(ItemStack stack) {
      if (!stack.hasItemMeta()) {
         return false;
      }

      var meta = stack.getItemMeta();
      return meta.hasDisplayName() || meta.hasEnchants() || meta.hasLore() || meta.hasCustomModelData();
   }
}
