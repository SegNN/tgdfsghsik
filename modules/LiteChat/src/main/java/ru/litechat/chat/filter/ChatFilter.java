package ru.litechat.chat.filter;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter {
   private static final String[] BLOCKED_DOMAINS = new String[]{
      "su",
      "mc",
      "gomc",
      "mclan",
      "ru",
      "com",
      "net",
      "org",
      "xyz",
      "site",
      "online",
      "fun",
      "gift",
      "pro",
      "top",
      "info",
      "gg",
      "me",
      "io",
      "cc",
      "pw",
      "tk",
      "ml",
      "ga",
      "cf",
      "gq",
      "ws",
      "club",
      "shop",
      "store",
      "space",
      "live",
      "tv",
      "vip",
      "best"
   };
   private static final Pattern IP_PATTERN = Pattern.compile("(?:\\b|(?:[.,\\-_*\\s]))\\d{1,3}(?:[\\s.,\\-_*]+\\d{1,3}){3}(?:\\b|(?:[.,\\-_*\\s]))", 2);
   private static final Pattern URL_PREFIX_PATTERN = Pattern.compile("\\b(?:h(?:ttps?)?\\s*:\\s*/\\s*/|www\\s*\\.)\\s*", 2);
   private final List<Pattern> domainPatterns = new ArrayList<>();

   public ChatFilter() {
      for (String domain : BLOCKED_DOMAINS) {
         this.domainPatterns.add(this.createDomainPattern(domain));
      }
   }

   private Pattern createDomainPattern(String domain) {
      StringBuilder regex = new StringBuilder();
      regex.append("\\b");

      for (int i = 0; i < domain.length(); i++) {
         char c = domain.charAt(i);
         regex.append(this.getCharPattern(c));
         if (i < domain.length() - 1) {
            regex.append("[\\s.,_\\-*]*");
         }
      }

      regex.append("\\b");
      return Pattern.compile(regex.toString(), 2);
   }

   private String getCharPattern(char c) {
      switch (Character.toLowerCase(c)) {
         case 'a':
            return "[aа4@аΑАᴀ]";
         case 'b':
            return "[bб6вΒБᴃ]";
         case 'c':
            return "[cс(¢©ςСᴄ]";
         case 'd':
            return "[dд]";
         case 'e':
            return "[eе3€ёЕЁᴇ]";
         case 'f':
            return "[fф]";
         case 'g':
            return "[gг9]";
         case 'h':
            return "[hн]";
         case 'i':
            return "[iі1!|ІЇїᴉ]";
         case 'j':
            return "[jй]";
         case 'k':
            return "[kк]";
         case 'l':
            return "[lл1|]";
         case 'm':
            return "[mм]";
         case 'n':
            return "[nн]";
         case 'o':
            return "[oо0ОΟᴏ]";
         case 'p':
            return "[pр]";
         case 'q':
            return "[q]";
         case 'r':
            return "[rр]";
         case 's':
            return "[sс5$ѕЅᴤ]";
         case 't':
            return "[tт7+]";
         case 'u':
            return "[uуυцУᴜ]";
         case 'v':
            return "[vв]";
         case 'w':
            return "[wш]";
         case 'x':
            return "[xх]";
         case 'y':
            return "[yуγУ]";
         case 'z':
            return "[zз2]";
         default:
            return Pattern.quote(String.valueOf(c));
      }
   }

   private String normalizeForDomainCheck(String message) {
      String normalized = this.convertStyledSymbols(message);
      normalized = Normalizer.normalize(normalized, Form.NFKC);
      normalized = normalized.replaceAll("[\\u200b-\\u200f\\u202a-\\u202e\\u2060-\\u206f\\ufeff]", "");
      return normalized.toLowerCase();
   }

   private String convertStyledSymbols(String text) {
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c >= '！' && c <= '～') {
            result.append((char)(c - 'ﻠ'));
         } else if (c >= 'Ａ' && c <= 'Ｚ') {
            result.append((char)(c - 'ﻠ'));
         } else if (c >= 'ａ' && c <= 'ｚ') {
            result.append((char)(c - 'ﻠ'));
         } else if (c >= '０' && c <= '９') {
            result.append((char)(c - 'ﻰ'));
         } else if (c >= '\ud835' && i + 1 < text.length()) {
            char next = text.charAt(i + 1);
            int codePoint = Character.toCodePoint(c, next);
            if (codePoint >= 119808 && codePoint <= 120831) {
               if (codePoint >= 119808 && codePoint <= 119859) {
                  result.append((char)(65 + (codePoint - 119808)));
               } else if (codePoint >= 119860 && codePoint <= 119911) {
                  result.append((char)(97 + (codePoint - 119860)));
               } else if (codePoint >= 120782 && codePoint <= 120791) {
                  result.append((char)(48 + (codePoint - 120782)));
               } else {
                  result.append(c).append(next);
               }

               i++;
            } else {
               result.append(c);
            }
         } else if (c >= 7424 && c <= 7551 || c >= 8448 && c <= 8527) {
            switch (c) {
               case 'ǫ':
               case 'ᵠ':
                  result.append('Q');
                  break;
               case 'ɢ':
               case 'ᴳ':
                  result.append('G');
                  break;
               case 'ɪ':
               case 'ᴵ':
                  result.append('I');
                  break;
               case 'ɴ':
               case 'ᴺ':
                  result.append('N');
                  break;
               case 'ʀ':
               case 'ᴿ':
                  result.append('R');
                  break;
               case 'ʏ':
               case 'ᵞ':
                  result.append('Y');
                  break;
               case 'ʙ':
               case 'ᴮ':
                  result.append('B');
                  break;
               case 'ʜ':
               case 'ᴴ':
                  result.append('H');
                  break;
               case 'ʟ':
               case 'ᴸ':
                  result.append('L');
                  break;
               case 'ˢ':
               case 'ѕ':
                  result.append('S');
                  break;
               case 'ˣ':
               case 'ᵡ':
                  result.append('X');
                  break;
               case 'ᴀ':
               case 'ᴬ':
                  result.append('A');
                  break;
               case 'ᴄ':
               case 'ᶜ':
                  result.append('C');
                  break;
               case 'ᴅ':
               case 'ᴰ':
                  result.append('D');
                  break;
               case 'ᴇ':
               case 'ᴱ':
                  result.append('E');
                  break;
               case 'ᴊ':
               case 'ᴶ':
                  result.append('J');
                  break;
               case 'ᴋ':
               case 'ᴷ':
                  result.append('K');
                  break;
               case 'ᴍ':
               case 'ᴹ':
                  result.append('M');
                  break;
               case 'ᴏ':
               case 'ᴼ':
                  result.append('O');
                  break;
               case 'ᴘ':
               case 'ᴾ':
                  result.append('P');
                  break;
               case 'ᴛ':
               case 'ᵀ':
                  result.append('T');
                  break;
               case 'ᴜ':
               case 'ᵁ':
                  result.append('U');
                  break;
               case 'ᴠ':
               case 'ⱽ':
                  result.append('V');
                  break;
               case 'ᴡ':
               case 'ᵂ':
                  result.append('W');
                  break;
               case 'ᴢ':
               case 'ᶻ':
                  result.append('Z');
                  break;
               case 'ᶠ':
               case 'ꜰ':
                  result.append('F');
                  break;
               default:
                  result.append(c);
            }
         } else {
            result.append(c);
         }
      }

      return result.toString();
   }

   public boolean isMessageBlocked(String message) {
      if (message != null && !message.isEmpty()) {
         String latinCheck = message.replaceAll("[^a-zA-Z0-9\\s\\p{Punct}]", "");
         if (this.containsStyledSymbols(message) && latinCheck.length() > 5) {
            return true;
         } else {
            String normalized = this.normalizeForDomainCheck(message);
            if (!IP_PATTERN.matcher(message).find() && !IP_PATTERN.matcher(normalized).find()) {
               if (!URL_PREFIX_PATTERN.matcher(message).find() && !URL_PREFIX_PATTERN.matcher(normalized).find()) {
                  for (Pattern pattern : this.domainPatterns) {
                     if (pattern.matcher(normalized).find()) {
                        String matchText = normalized.toLowerCase();
                        if (this.isLikelyDomain(matchText)) {
                           return true;
                        }
                     }
                  }

                  return false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean containsStyledSymbols(String text) {
      Pattern styledPattern = Pattern.compile("[\\uff01-\\uff5e]|[\\ud835\\udc00-\\ud835\\udfff]|[\\ud835\\udd00-\\ud835\\uddff]|[ᴀ-ᴢ]|[ᴬ-ᶻ]");
      return styledPattern.matcher(text).find();
   }

   private boolean isLikelyDomain(String text) {
      Pattern domainPattern = Pattern.compile("(?:\\b|\\.|\\/)(?:[a-zа-я0-9]+\\.)+[a-zа-я]{2,}|\\b(?:www|http|https|ftp)\\b|\\/[^\\s\\/]+\\.[^\\s\\/]+\\b");
      return domainPattern.matcher(text).find();
   }

   private void logCheck(String message, String normalized, boolean blocked) {
      System.out.println("Message: " + message);
      System.out.println("Normalized: " + normalized);
      System.out.println("Blocked: " + blocked);
      System.out.println("---");
   }
}
