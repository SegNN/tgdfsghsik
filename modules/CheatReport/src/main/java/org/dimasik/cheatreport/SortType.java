package org.dimasik.cheatreport;

public enum SortType {
   NEWEST_FIRST("Сначала новые", "max_date DESC"),
   OLDEST_FIRST("Сначала старые", "max_date ASC"),
   MOST_REPORTS("Сначала большое количество", "report_count DESC"),
   LEAST_REPORTS("Сначала маленькое количество", "report_count ASC"),
   ALPHABETICAL("Сначала по алфавиту", "target ASC"),
   REVERSE_ALPHABETICAL("Сначала не по алфавиту", "target DESC");

   private final String displayName;
   private final String sqlOrder;

   private SortType(String displayName, String sqlOrder) {
      this.displayName = displayName;
      this.sqlOrder = sqlOrder;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getSqlOrder() {
      return this.sqlOrder;
   }
}
