package org.dimasik.playtimeapi.models;

import java.util.UUID;

public class PlayerData {
   private final UUID uuid;
   private long totalPlayTime;
   private long totalActiveTime;
   private String currentServer;
   private long lastLogin;
   private long lastActivity;
   private long sessionPlayTime;
   private long sessionActiveTime;

   public PlayerData(UUID uuid) {
      this.uuid = uuid;
      this.currentServer = "Оффлайн";
      this.lastLogin = System.currentTimeMillis();
      this.lastActivity = System.currentTimeMillis();
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public long getTotalPlayTime() {
      return this.totalPlayTime;
   }

   public void setTotalPlayTime(long totalPlayTime) {
      this.totalPlayTime = totalPlayTime;
   }

   public long getTotalActiveTime() {
      return this.totalActiveTime;
   }

   public void setTotalActiveTime(long totalActiveTime) {
      this.totalActiveTime = totalActiveTime;
   }

   public long getIdleTime() {
      return this.totalPlayTime - this.totalActiveTime;
   }

   public void setIdleTime(long idleTime) {
   }

   public String getCurrentServer() {
      return this.currentServer;
   }

   public void setCurrentServer(String currentServer) {
      this.currentServer = currentServer;
   }

   public long getLastLogin() {
      return this.lastLogin;
   }

   public void setLastLogin(long lastLogin) {
      this.lastLogin = lastLogin;
   }

   public long getLastActivity() {
      return this.lastActivity;
   }

   public void setLastActivity(long lastActivity) {
      this.lastActivity = lastActivity;
   }

   public long getSessionPlayTime() {
      return this.sessionPlayTime;
   }

   public long getSessionActiveTime() {
      return this.sessionActiveTime;
   }

   public long getSessionIdleTime() {
      return this.sessionPlayTime - this.sessionActiveTime;
   }

   public void addPlayTime(long millis) {
      this.totalPlayTime += millis;
      this.sessionPlayTime += millis;
   }

   public void addActiveTime(long millis) {
      this.totalActiveTime += millis;
      this.sessionActiveTime += millis;
      this.lastActivity = System.currentTimeMillis();
   }
}
