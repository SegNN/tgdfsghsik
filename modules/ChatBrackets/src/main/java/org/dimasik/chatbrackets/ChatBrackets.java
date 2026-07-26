package org.dimasik.chatbrackets;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatBrackets extends JavaPlugin {
   private ChatBrackets.ChatBracketsExpansion chatBracketsExpansion;

   public ChatBrackets() {
   }

   public void onEnable() {
      this.chatBracketsExpansion = new ChatBrackets.ChatBracketsExpansion();
      if (!this.chatBracketsExpansion.register()) {
         this.getLogger().warning("Не удалось зарегистрировать расширение!");
      }
   }

   public void onDisable() {
      if (this.chatBracketsExpansion != null) {
         this.chatBracketsExpansion.unregister();
      }
   }

   public class ChatBracketsExpansion extends PlaceholderExpansion {
      public ChatBracketsExpansion() {
      }

      public boolean canRegister() {
         return true;
      }

      public String getIdentifier() {
         return "chatbrackets";
      }

      public String getAuthor() {
         return "YourName";
      }

      public String getVersion() {
         return "1.0.0";
      }

      public String onPlaceholderRequest(Player player, String identifier) {
         if (player == null) {
            return "";
         } else {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
               return "";
            } else {
               String prefix = user.getCachedData().getMetaData().getPrefix();
               String prefix2 = "";
               if (prefix != null && !prefix.isEmpty()) {
                  for (int i = 0; i < prefix.length() - 3; i++) {
                     prefix2 = prefix2 + prefix.charAt(i);
                  }

                  if (identifier.equals("begin")) {
                     return this.getFirstColor(prefix2) + "«";
                  } else if (identifier.equals("end")) {
                     return this.getLastColor(prefix2) + "»";
                  } else if (identifier.equals("full")) {
                     return this.getFullString(prefix2, this.getFirstColor(prefix2) + "«", this.getLastColor(prefix2) + "»");
                  } else if (identifier.equals("nobold")) {
                     return this.getNoBold(prefix);
                  } else if (!identifier.equals("color")) {
                     return null;
                  } else if (!player.hasPermission("holyfreeze.use") && !player.hasPermission("chatbrackets.color")) {
                     return "";
                  } else {
                     try {
                        return this.getFirstColor(prefix);
                     } catch (Exception var8) {
                        return "";
                     }
                  }
               } else {
                  return "";
               }
            }
         }
      }

      private String getFirstColor(String prefix) {
         if (prefix.length() == 0) {
            return "";
         } else {
            String ans = "";
            int startindex = 0;
            if (prefix.charAt(1) == 'x') {
               startindex = 14;

               for (int i = 0; i < 14; i++) {
                  char c = prefix.charAt(i);
                  ans = ans + c;
               }
            }

            for (int i = startindex; i < prefix.length(); i++) {
               char c = prefix.charAt(i);
               if (c != '&') {
                  break;
               }

               ans = ans + prefix.charAt(i);
               ans = ans + prefix.charAt(i + 1);
               i++;
            }

            return ans;
         }
      }

      public String getNoBold(String s) {
         String string = s;

         while (string.contains("&l")) {
            string = string.replace("&l", "");
         }

         while (string.contains("&n")) {
            string = string.replace("&n", "");
         }

         return string;
      }

      private String getLastColor(String prefix) {
         try {
            int lastColorIndex = 0;

            for (int i = prefix.length() - 2; i > 0; i--) {
               if (prefix.charAt(i) != '&' && prefix.charAt(i - 1) != '&' && prefix.charAt(i + 1) == '&') {
                  lastColorIndex = i + 1;
               }
            }

            int n = prefix.length();
            String ans = "";

            for (int ix = lastColorIndex; ix < lastColorIndex + 14; ix++) {
               char c = prefix.charAt(ix);
               ans = ans + c;
            }

            for (int ix = lastColorIndex + 14; ix > 0; ix++) {
               char c = prefix.charAt(ix);
               if (c != '&') {
                  break;
               }

               ans = ans + prefix.charAt(ix);
               ans = ans + prefix.charAt(ix + 1);
               ix++;
            }

            return ans;
         } catch (Exception var7) {
            return this.getFirstColor(prefix);
         }
      }

      private String getFullString(String prefix, String begin, String end) {
         String ans = "";
         return begin + prefix + "»&f ";
      }
   }
}
