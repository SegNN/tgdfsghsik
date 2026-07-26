package stickdev.siynes.stickhwcustom.listeners.particles;

import java.util.Random;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.holders.ParticleMenuHolder;
import stickdev.siynes.stickhwcustom.items.MenuItems;
import stickdev.siynes.stickhwcustom.menus.MainMenu;
import stickdev.siynes.stickhwcustom.utils.HexUtil;

public class ParticleListener implements Listener {
   private final StickHWCustom plugin;
   private final Random random = new Random();

   public ParticleListener(StickHWCustom plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onParticleMenuClick(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() != null && e.getClickedInventory().getHolder() instanceof ParticleMenuHolder) {
         e.setCancelled(true);
         this.inventoryClick(player, e.getSlot());
      }
   }

   private void inventoryClick(Player player, int slot) {
      MenuItems menuItems = new MenuItems();
      switch (slot) {
         case 45:
            new MainMenu(menuItems).open(player);
            return;
         default:
            if (this.plugin.getConfig().contains("particles.items")) {
               for (String key : this.plugin.getConfig().getConfigurationSection("particles.items").getKeys(false)) {
                  String path = "particles.items." + key;
                  int configSlot = this.plugin.getConfig().getInt(path + ".slot");
                  if (configSlot == slot) {
                     try {
                        Particle particle = Particle.valueOf(this.plugin.getConfig().getString(path + ".particle"));
                        StickHWCustom.playerData.get(player).put("particle_type", particle.name());
                        if (particle == Particle.REDSTONE) {
                           String color = this.plugin.getConfig().getString(path + ".color", "RED");
                           StickHWCustom.playerData.get(player).put("particle_color", color);
                        }

                        player.sendMessage(
                           HexUtil.translate(
                              this.plugin
                                 .getConfig()
                                 .getString("messages.acceptparticle")
                                 .replace("{particle}", this.plugin.getConfig().getString(path + ".name"))
                           )
                        );
                        this.spawnDemoParticles(player, particle);
                     } catch (IllegalArgumentException var10) {
                        player.sendMessage("§cОшибка: частица не найдена!");
                     }

                     return;
                  }
               }
            }
      }
   }

   private void spawnDemoParticles(Player player, Particle particle) {
      Location loc = player.getLocation().add(0.0, 2.0, 0.0);
      if (particle == Particle.REDSTONE) {
         Color color = this.getColorFromConfig(player);
         DustOptions dustOptions = new DustOptions(color, 1.0F);
         player.spawnParticle(particle, loc, 20, 0.5, 0.5, 0.5, 0.0, dustOptions);
      } else {
         player.spawnParticle(particle, loc, 20, 0.5, 0.5, 0.5, 0.0);
      }
   }

   @EventHandler
   public void on(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
         Player damager = (Player)e.getDamager();
         Player target = (Player)e.getEntity();
         if (this.isCriticalHit(damager)
            && StickHWCustom.playerData != null
            && StickHWCustom.playerData.get(damager) != null
            && StickHWCustom.playerData.get(damager).containsKey("particle_type")) {
            try {
               Particle particle = Particle.valueOf(StickHWCustom.playerData.get(damager).get("particle_type"));
               Location loc = target.getLocation().add(0.0, 1.0, 0.0);
               int count = this.random.nextInt(3) + 3;
               if (particle == Particle.REDSTONE) {
                  Color color = this.getColorFromConfig(damager);
                  DustOptions dustOptions = new DustOptions(color, 1.0F);
                  damager.spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.0, dustOptions);
               } else {
                  damager.spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.0);
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }
      }
   }

   private Color getColorFromConfig(Player player) {
      String colorName = StickHWCustom.playerData.get(player).getOrDefault("particle_color", "RED");

      try {
         return (Color)Color.class.getField(colorName).get(null);
      } catch (Exception var4) {
         return Color.RED;
      }
   }

   private boolean isCriticalHit(Player player) {
      return player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && !player.isInsideVehicle();
   }
}
