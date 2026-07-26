package ua.fichadev.fichacases.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;

public class Messages {
   private Map<String, List<String>> messages = new HashMap<>();

   public Messages() {
   }

   public Messages(Entry<String, List<String>>... entries) {
      for (Entry<String, List<String>> entry : entries) {
         this.messages.put(entry.getKey(), entry.getValue());
      }
   }

   public void addMessage(String key, List<String> messageList) {
      this.messages.put(key, messageList);
   }

   public void sendMessage(Player player, String key) {
      List<String> messageList = this.messages.get(key);
      if (messageList != null) {
         for (String message : messageList) {
            player.sendMessage(ColorUtil.parse(message));
         }
      }
   }

   public List<String> getMessage(String key) {
      return this.messages.get(key);
   }

   public boolean hasMessage(String key) {
      return this.messages.containsKey(key);
   }
}
