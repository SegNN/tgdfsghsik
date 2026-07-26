package ru.kotikstasika.holyliteend.end.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dimasik.lootmanager.LootAPI;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.utils.ColorUtils;

public class Dragon {
   private final HolyLiteEnd plugin;
   private BossBar dragonBossBar;
   private BukkitTask updateTask;
   private BukkitTask messageTask;
   private final Map<String, Double> dragonDamage = new HashMap<>();
   private EnderDragon currentDragon;

   public Dragon(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public void onDragonSpawn(EnderDragon dragon) {
      this.currentDragon = dragon;
      double maxHealth = 6000.0;

      try {
         dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
      } catch (Throwable var6) {
         dragon.setMaxHealth(maxHealth);
      }

      dragon.setHealth(maxHealth);
      dragon.setCustomName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&9&F&0&0&F&FЭ&x&9&D&0&1&F&8н&x&9&B&0&3&F&2д&x&9&A&0&4&E&Bе&x&9&8&0&6&E&4р&x&9&6&0&7&D&D-&x&9&4&0&9&D&7Д&x&9&2&0&A&D&0р&x&9&0&0&x&f&b&0&0&0&0&x&f&b&0&0&0&0&9а&x&8&F&0&D&x&f&b&0&0&0&0&2к&x&8&D&0&F&B&x&f&b&0&0&0&0о&x&8&B&1&0&B&5н"
         )
      );
      dragon.setCustomNameVisible(true);

      try {
         dragon.setPhase(Phase.LAND_ON_PORTAL);
      } catch (Throwable var5) {
      }

      this.startBossBarUpdate();
      this.startMessageTask();
      this.dragonDamage.clear();
   }

   public void onDragonDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player && EndEvent.isEventActive()) {
         Player player = (Player)event.getDamager();
         double damage = event.getFinalDamage();
         String playerName = player.getName();
         double newDamage = this.dragonDamage.getOrDefault(playerName, 0.0) + damage;
         this.dragonDamage.put(playerName, newDamage);
      }
   }

   public void onDragonDeath(EntityDeathEvent event) {
      if (this.currentDragon != null && EndEvent.isEventActive()) {
         event.getDrops().removeIf(item -> item.getType() == Material.DRAGON_EGG);
         List<Entry<String, Double>> sortedDamage = new ArrayList<>(this.dragonDamage.entrySet());
         sortedDamage.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
         if (!sortedDamage.isEmpty()) {
            String topPlayerName = sortedDamage.get(0).getKey();
            Player topPlayer = Bukkit.getPlayer(topPlayerName);
            if (topPlayer != null) {
               topPlayer.getInventory().addItem(new ItemStack[]{EggItem.createDragonEggFromKill()});
               this.callLootManager();
               Bukkit.broadcastMessage(ColorUtils.translateColors("&e▶ &fИгрок &b" + topPlayer.getName() + " &fУничтожил Дракона Энда и &eзаполучил его яйцо!"));
            }
         }

         for (Entry<String, Double> entry : this.dragonDamage.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
               int damage = entry.getValue().intValue();
               player.giveExp(damage);
            }
         }

         this.stopDragonBossBar();
         this.dragonDamage.clear();
         this.currentDragon = null;
         (new BukkitRunnable() {
            public void run() {
               Dragon.this.removeEgg();
            }
         }).runTaskLater(this.plugin, 1L);
      }
   }

   void removeEgg() {
      World endWorld = Bukkit.getWorld("world_the_end");
      int x = 0;
      int z = 0;

      for (int y = 0; y <= 255; y++) {
         Location blockLoc = new Location(endWorld, x, y, z);
         Material blockType = blockLoc.getBlock().getType();
         if (blockType == Material.DRAGON_EGG) {
            blockLoc.getBlock().setType(Material.AIR);
         }
      }
   }

   private void startBossBarUpdate() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
      }

      this.updateTask = (new BukkitRunnable() {
         public void run() {
            if (Dragon.this.currentDragon == null || Dragon.this.currentDragon.isDead()) {
               Dragon.this.stopDragonBossBar();
            }
         }
      }).runTaskTimer(this.plugin, 100L, 100L);
   }

   private void startMessageTask() {
      if (this.messageTask != null) {
         this.messageTask.cancel();
      }

      if (EndEvent.isEventActive()) {
         this.messageTask = (new BukkitRunnable() {
               private final Random random = new Random();
               private final String[] headerMessages = new String[]{
                  "Бесстрашные воины не побоялись вступить в схватку\nжизни и смерти против Дракона, вот они наши герои:",
                  "Могущественный Дракон скоро будет повержен, и всё\nблагодаря этим воинам:",
                  "Эти герои не побоялись напрячь свою уверенность\nи уже сделали наибольший вклад в убийство Дракона:",
                  "В схватке против дракона, ящерица проявляет слабину,\nнаибольший вклад в его убийство вносят эти рыцари:"
               };

               public void run() {
                  if (Dragon.this.currentDragon != null && !Dragon.this.currentDragon.isDead()) {
                     List<Entry<String, Double>> sortedDamage = new ArrayList<>(Dragon.this.dragonDamage.entrySet());
                     sortedDamage.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
                     String headerMessage = this.headerMessages[this.random.nextInt(this.headerMessages.length)];
                     Bukkit.broadcastMessage(ColorUtils.translateColors(""));

                     for (String playerName : headerMessage.split("\n")) {
                        Bukkit.broadcastMessage(ColorUtils.translateColors("&f" + playerName));
                     }

                     for (int i = 0; i < 3; i++) {
                        String place = i == 0 ? "1 место" : (i == 1 ? "2 место" : "3 место");
                        if (i < sortedDamage.size()) {
                           Entry<String, Double> entry = sortedDamage.get(i);
                           String playerName = entry.getKey();
                           double damageValue = entry.getValue();
                           String damageStr = String.format("%.2f", damageValue);
                           Bukkit.broadcastMessage(
                              ColorUtils.translateColors(" &e• " + place + " - &b" + playerName + " &7| &x&F&F&7&0&0&0" + damageStr + " урона")
                           );
                        } else {
                           Bukkit.broadcastMessage(ColorUtils.translateColors(" &e• " + place + " - &b--- &7| &x&F&F&7&0&0&00 урона"));
                        }
                     }

                     Bukkit.broadcastMessage(ColorUtils.translateColors(""));
                     Bukkit.broadcastMessage(ColorUtils.translateColors("&bИгрок, &fзанявший &bпервое место, &e&lполучит Яйцо дракона."));
                     Bukkit.broadcastMessage(ColorUtils.translateColors("&bТакже всем игрокам &fначислется &bопыт &fза &bурон."));
                     Bukkit.broadcastMessage(ColorUtils.translateColors(""));
                  }
               }
            })
            .runTaskTimer(this.plugin, 600L, 600L);
      }
   }

   private void callLootManager() {
      if (this.currentDragon != null) {
         LootAPI lootAPI = new LootAPI();
         int count = new Random().nextInt(31) + 30;

         for (ItemStack itemStack : lootAPI.getRandomItems(count, 3)) {
            if (itemStack != null && !itemStack.getType().isAir()) {
               double angle = Math.random() * 2.0 * Math.PI;
               double distance = 3.0 + Math.random() * 7.0;
               double x = this.currentDragon.getLocation().getX() + Math.cos(angle) * distance;
               double z = this.currentDragon.getLocation().getZ() + Math.sin(angle) * distance;
               double y = this.currentDragon.getLocation().getY() + 2.0;
               Location dropLocation = new Location(this.currentDragon.getWorld(), x, y, z);
               this.currentDragon.getWorld().dropItemNaturally(dropLocation, itemStack);
            }
         }
      }
   }

   public void stopDragonBossBar() {
      if (this.dragonBossBar != null) {
         this.dragonBossBar.removeAll();
         this.dragonBossBar = null;
      }

      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }

      if (this.messageTask != null) {
         this.messageTask.cancel();
         this.messageTask = null;
      }
   }

   public void onEventEnd() {
      this.stopDragonBossBar();
      this.dragonDamage.clear();
      this.currentDragon = null;
   }

   public void addPlayerToDragonBossBar(Player player) {
      if (this.dragonBossBar != null && !this.dragonBossBar.getPlayers().contains(player)) {
         this.dragonBossBar.addPlayer(player);
      }
   }
}
