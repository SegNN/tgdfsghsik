package org.impldev.holycore.invest;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Применяет то, что можно применить прямо здесь: опыт, порох и границы миров.
 *
 * <p>Скупщик, автошахты, голосование и сокровища живут в других плагинах, поэтому здесь для них
 * считается только величина эффекта - её отдают плейсхолдеры {@link InvestPlaceholders}, а уже
 * те плагины могут её использовать в своих конфигах.
 */
public final class InvestEffects implements Listener {
   /** радиус границы на нулевом уровне и прибавка за уровень */
   private static final double NETHER_BASE = 1602.0;
   private static final double NORMAL_BASE = 2884.0;
   private static final double END_BASE = 4166.0;
   private static final double BORDER_STEP = 10.0;

   private final JavaPlugin plugin;
   private final InvestData data;

   public InvestEffects(JavaPlugin plugin, InvestData data) {
      this.plugin = plugin;
      this.data = data;
   }

   /** Множитель вида 1.7 для уровня 14 при 5% за уровень. */
   public double bonus(String id) {
      Category category = Categories.all().get(id);
      if (category == null) {
         return 1.0;
      }

      return 1.0 + this.data.level(id) * category.perLevel() / 100.0;
   }

   /** Сколько секунд занимает действие с учётом прокачки. */
   public double timeFor(Category category) {
      double cut = this.data.level(category.id()) * category.perLevel() / 100.0;
      return category.baseValue() * (1.0 - Math.min(0.95, cut));
   }

   public double borderRadius(String kind, int level) {
      double base = switch (kind) {
         case "nether" -> NETHER_BASE;
         case "end" -> END_BASE;
         default -> NORMAL_BASE;
      };

      return base + level * BORDER_STEP;
   }

   /** Ставит границы мирам по текущему уровню. Вызывается при старте и после каждого уровня. */
   public void apply() {
      int level = this.data.level("borders");
      for (World world : this.plugin.getServer().getWorlds()) {
         String kind = switch (world.getEnvironment()) {
            case NETHER -> "nether";
            case THE_END -> "end";
            default -> "normal";
         };

         double radius = this.borderRadius(kind, level);
         if (Math.abs(world.getWorldBorder().getSize() / 2.0 - radius) > 0.5) {
            world.getWorldBorder().setSize(radius * 2.0);
         }
      }
   }

   @EventHandler
   public void onDeath(EntityDeathEvent event) {
      double xp = this.bonus("experience");
      if (xp > 1.0) {
         event.setDroppedExp((int)Math.round(event.getDroppedExp() * xp));
      }

      if (event.getEntityType() != EntityType.CREEPER) {
         return;
      }

      // из спавнера порох не усиливаем: это отдельно оговорено в описании направления
      if (event.getEntity() instanceof Creeper creeper && creeper.fromMobSpawner()) {
         return;
      }

      double bonus = this.bonus("gunpowder");
      if (bonus <= 1.0) {
         return;
      }

      for (ItemStack drop : event.getDrops()) {
         if (drop.getType() == org.bukkit.Material.GUNPOWDER) {
            int amount = (int)Math.round(drop.getAmount() * bonus);
            drop.setAmount(Math.max(1, Math.min(amount, drop.getMaxStackSize())));
         }
      }
   }
}
