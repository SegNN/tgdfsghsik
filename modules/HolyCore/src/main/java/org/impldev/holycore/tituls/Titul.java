package org.impldev.holycore.tituls;

/**
 * Один титул.
 *
 * @param permission право LuckPerms, которым владение и определяется (tituls.summer2024.1)
 * @param text       сам титул со всеми цветами - он же суффикс, который вешается на игрока
 * @param season     ключ сезона (summer2024), из него берётся дата появления
 * @param order      порядок внутри сезона, чтобы сортировка была устойчивой
 */
public record Titul(String permission, String text, String season, int order) {

   /** Даты сезонов взяты из подписей в меню /titul: там у каждого сезона указаны месяцы. */
   public String date() {
      String year = this.season.replaceAll("\\D+", "");
      String part = this.season.replaceAll("\\d+", "");
      String day = switch (part) {
         case "spring" -> "01.03.";
         case "summer" -> "01.08.";
         case "autumn" -> "01.10.";
         case "winter" -> "01.12.";
         default -> "01.01.";
      };

      return day + year;
   }

   /** Сезоны идут по календарю - на этом строится сортировка "сначала новые". */
   public int seasonRank() {
      int year = Integer.parseInt(this.season.replaceAll("\\D+", "0").isEmpty()
            ? "0" : this.season.replaceAll("\\D+", ""));
      int part = switch (this.season.replaceAll("\\d+", "")) {
         case "spring" -> 1;
         case "summer" -> 2;
         case "autumn" -> 3;
         case "winter" -> 4;
         default -> 0;
      };

      return year * 10 + part;
   }
}
