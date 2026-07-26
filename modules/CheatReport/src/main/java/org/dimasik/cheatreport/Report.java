package org.dimasik.cheatreport;

import java.sql.Timestamp;

public class Report {
   public int id;
   public String sender;
   public String target;
   public Timestamp date;
   public String reason;
   public String type;
   public String admin;
   public String mode;

   public Report(int id, String sender, String target, Timestamp date, String reason, String type, String admin, String mode) {
      this.id = id;
      this.sender = sender;
      this.target = target;
      this.date = date;
      this.reason = reason;
      this.type = type;
      this.admin = admin;
      this.mode = mode;
   }

   public int getId() {
      return this.id;
   }

   public String getSender() {
      return this.sender;
   }

   public String getTarget() {
      return this.target;
   }

   public Timestamp getDate() {
      return this.date;
   }

   public String getReason() {
      return this.reason;
   }

   public String getType() {
      return this.type;
   }

   public String getAdmin() {
      return this.admin;
   }

   public String getMode() {
      return this.mode;
   }
}
