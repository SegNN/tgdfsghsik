package org.dimasik.eventmanager;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class EventManager extends JavaPlugin {
   public static EventManager instance;
   public EventVote vote;
   private int timer = this.getConfig().getInt("timer") * 60;
   private BukkitTask eventTimer;
   public HashMap<String, Event> events = new HashMap<>();
   public static EventMenyHolder eventMenyHolder;

   public EventManager() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.vote = new EventVote(this);
      this.getServer().getPluginManager().registerEvents(this.vote, this);
      this.getCommand("vote").setExecutor(this.vote);
      this.getCommand("event").setExecutor(new EventCommand(this));
      this.updateTimerTask();
      eventMenyHolder = new EventMenyHolder();
   }

   public void sendEventMessage(Player player) {
      if (this.vote.isVoting) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&c&6&f&0&0 &n◢&r &fЗапущено голосование за следующее мероприятие."));
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&c&6&f&0&0 &l◤&f &x&f&c&6&f&0&0Прописывай /vote, &fчтобы принять участие."));
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
      } else {
         if (this.events.isEmpty()) {
            int min = this.timer / 60;
            int sec = this.timer % 60;
            player.sendMessage(
               ChatColor.translateAlternateColorCodes(
                  '&', "&x&0&0&D&4&F&B▶ &fБлижайшее голосование за мероприятие будет проводиться через &x&0&0&D&4&F&B" + min + " мин., " + sec + " сек."
               )
            );
         } else {
            for (Event event : this.events.values()) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', event.message));
            }
         }
      }
   }

   public void updateTimerTask() {
      this.eventTimer = (new BukkitRunnable() {
         public void run() {
            if (EventManager.this.timer <= 0) {
               EventManager.this.timer = EventManager.this.getConfig().getInt("timer") * 60;
               EventManager.this.vote.startVoting();
            }

            EventManager.this.timer = EventManager.this.timer - 1;
         }
      }).runTaskTimer(this, 0L, 20L);
   }

   public void setTimer(int newtimer) {
      this.timer = newtimer;
      this.updateTimerTask();
   }

   public static EventManager getInstance() {
      return instance;
   }
}
