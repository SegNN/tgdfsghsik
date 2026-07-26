package ru.litechat;

import org.bukkit.plugin.java.JavaPlugin;
import ru.litechat.chat.filter.ChatFilter;
import ru.litechat.listeners.BukkitListener;

public class LiteChat extends JavaPlugin {
   private static LiteChat instance;
   private ChatFilter chatFilter;

   public LiteChat() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.chatFilter = new ChatFilter();
      this.getServer().getPluginManager().registerEvents(new BukkitListener(), this);
   }

   public void onDisable() {
   }

   public static LiteChat getInstance() {
      return instance;
   }

   public ChatFilter getChatFilter() {
      return this.chatFilter;
   }
}
