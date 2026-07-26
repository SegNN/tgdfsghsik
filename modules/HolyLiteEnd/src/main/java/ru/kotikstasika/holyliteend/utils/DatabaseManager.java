package ru.kotikstasika.holyliteend.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;

public class DatabaseManager {
   private final HolyLiteEnd plugin;

   public DatabaseManager(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public DatabaseManager.EndSettings getEndSettings() {
      try {
         label138: {
            Connection connection = this.plugin.getDataSource().getConnection();

            DatabaseManager.EndSettings var7;
            label133: {
               try {
                  PreparedStatement stmt;
                  label140: {
                     String sql = "SELECT id, start_hour, start_minute, day_of_week, duration, timezone FROM end_settings WHERE id = 1";
                     stmt = connection.prepareStatement(sql);

                     try {
                        ResultSet rs = stmt.executeQuery();

                        label118: {
                           try {
                              if (rs.next()) {
                                 DatabaseManager.EndSettings endSettings = new DatabaseManager.EndSettings();
                                 endSettings.setId(rs.getInt("id"));
                                 endSettings.setStartHour(rs.getInt("start_hour"));
                                 endSettings.setStartMinute(rs.getInt("start_minute"));
                                 endSettings.setDayOfWeek(rs.getInt("day_of_week"));
                                 endSettings.setDuration(rs.getInt("duration"));
                                 String tz = rs.getString("timezone");
                                 endSettings.setTimezone(tz != null && !tz.isEmpty() ? tz : "Europe/Moscow");
                                 var7 = endSettings;
                                 break label118;
                              }
                           } catch (Throwable var13) {
                              if (rs != null) {
                                 try {
                                    rs.close();
                                 } catch (Throwable var11) {
                                    var13.addSuppressed(var11);
                                 }
                              }

                              throw var13;
                           }

                           if (rs != null) {
                              rs.close();
                           }
                           break label140;
                        }

                        if (rs != null) {
                           rs.close();
                        }
                     } catch (Throwable var14) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var10) {
                              var14.addSuppressed(var10);
                           }
                        }

                        throw var14;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                     break label133;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }

                  String insert = "INSERT INTO end_settings (id, start_hour, start_minute, day_of_week, duration, timezone) VALUES (1, 16, 0, 1, 90, 'Europe/Moscow')";
                  PreparedStatement ins = connection.prepareStatement(insert);

                  try {
                     ins.executeUpdate();
                  } catch (Throwable var121) {
                     if (ins != null) {
                        try {
                           ins.close();
                        } catch (Throwable var91) {
                           var121.addSuppressed(var91);
                        }
                     }

                     throw var121;
                  }

                  if (ins != null) {
                     ins.close();
                  }
               } catch (Throwable var151) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var81) {
                        var151.addSuppressed(var81);
                     }
                  }

                  throw var151;
               }

               if (connection != null) {
                  connection.close();
               }
               break label138;
            }

            if (connection != null) {
               connection.close();
            }

            return var7;
         }
      } catch (SQLException var16) {
         this.plugin.getLogger().severe("Ошибка при получении end_settings: " + var16.getMessage());
      }

      DatabaseManager.EndSettings def = new DatabaseManager.EndSettings();
      def.setId(1);
      def.setStartHour(16);
      def.setStartMinute(0);
      def.setDayOfWeek(1);
      def.setDuration(90);
      def.setTimezone("Europe/Moscow");
      return def;
   }

   public boolean updateEndSettings(DatabaseManager.EndSettings settings) {
      if (settings == null) {
         return false;
      } else {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            boolean var5;
            try {
               String sql = "UPDATE end_settings SET start_hour = ?, start_minute = ?, day_of_week = ?, duration = ?, timezone = ? WHERE id = 1";
               PreparedStatement stmt = connection.prepareStatement(sql);

               try {
                  stmt.setInt(1, settings.getStartHour());
                  stmt.setInt(2, settings.getStartMinute());
                  stmt.setInt(3, settings.getDayOfWeek());
                  stmt.setInt(4, settings.getDuration());
                  stmt.setString(5, settings.getTimezone() != null && !settings.getTimezone().isEmpty() ? settings.getTimezone() : "Europe/Moscow");
                  var5 = stmt.executeUpdate() > 0;
               } catch (Throwable var101) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var9) {
                        var101.addSuppressed(var9);
                     }
                  }

                  throw var101;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var111) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var111.addSuppressed(var8);
                  }
               }

               throw var111;
            }

            if (connection != null) {
               connection.close();
            }

            return var5;
         } catch (SQLException var12) {
            this.plugin.getLogger().severe("Ошибка при обновлении end_settings: " + var12.getMessage());
            return false;
         }
      }
   }

   public void storeDragonEgg(Player player, Location location, int baseValue) {
      if (location != null) {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            try {
               String sql = "INSERT INTO dragon_eggs (x, y, z, world, server, base_value, current_value, last_increase_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
               PreparedStatement stmt = connection.prepareStatement(sql);

               try {
                  stmt.setInt(1, location.getBlockX());
                  stmt.setInt(2, location.getBlockY());
                  stmt.setInt(3, location.getBlockZ());
                  stmt.setString(4, location.getWorld().getName());
                  stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
                  stmt.setInt(6, baseValue);
                  stmt.setInt(7, baseValue);
                  stmt.setLong(8, System.currentTimeMillis());
                  stmt.executeUpdate();
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     try {
                        if (this.plugin.getHologramManager() != null) {
                           this.plugin.getHologramManager().updateTopHologram();
                        }
                     } catch (Exception var2x) {
                     }
                  });
               } catch (Throwable var11) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var121) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var91) {
                     var121.addSuppressed(var91);
                  }
               }

               throw var121;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var131) {
            this.plugin.getLogger().severe("Ошибка при сохранении яйца дракона: " + var131.getMessage());
         }
      }
   }

   public void syncChestEggCount(Location location, int actualEggCount) {
      if (location != null) {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            try {
               String countSql = "SELECT COUNT(*) FROM dragon_eggs WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ?";
               int dbCount = 0;
               PreparedStatement countStmt = connection.prepareStatement(countSql);

               try {
                  countStmt.setInt(1, location.getBlockX());
                  countStmt.setInt(2, location.getBlockY());
                  countStmt.setInt(3, location.getBlockZ());
                  countStmt.setString(4, location.getWorld().getName());
                  countStmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
                  ResultSet rs = countStmt.executeQuery();

                  try {
                     if (rs.next()) {
                        dbCount = rs.getInt(1);
                     }
                  } catch (Throwable var13) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var12) {
                           var13.addSuppressed(var12);
                        }
                     }

                     throw var13;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var14) {
                  if (countStmt != null) {
                     try {
                        countStmt.close();
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }
                  }

                  throw var14;
               }

               if (countStmt != null) {
                  countStmt.close();
               }

               if (actualEggCount > dbCount) {
                  int eggsToRemove = actualEggCount - dbCount;

                  for (int i = 0; i < eggsToRemove; i++) {
                     this.addEggToChest(location, 250);
                  }
               } else if (actualEggCount < dbCount) {
                  int eggsToRemove = dbCount - actualEggCount;

                  for (int i = 0; i < eggsToRemove; i++) {
                     this.removeEggFromChest(location);
                  }
               }
            } catch (Throwable var151) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var101) {
                     var151.addSuppressed(var101);
                  }
               }

               throw var151;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var161) {
            this.plugin.getLogger().severe("Ошибка при синхронизации количества яиц: " + var161.getMessage());
         }
      }
   }

   private void addEggToChest(Location location, int baseValue) {
      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "INSERT INTO dragon_eggs (x, y, z, world, server, base_value, current_value, last_increase_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);

            try {
               stmt.setInt(1, location.getBlockX());
               stmt.setInt(2, location.getBlockY());
               stmt.setInt(3, location.getBlockZ());
               stmt.setString(4, location.getWorld().getName());
               stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
               stmt.setInt(6, baseValue);
               stmt.setInt(7, baseValue);
               stmt.setLong(8, System.currentTimeMillis());
               stmt.executeUpdate();
               this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                  try {
                     if (this.plugin.getHologramManager() != null) {
                        this.plugin.getHologramManager().updateTopHologram();
                     }
                  } catch (Exception var2x) {
                  }
               });
            } catch (Throwable var10) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var111) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var81) {
                  var111.addSuppressed(var81);
               }
            }

            throw var111;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var121) {
         this.plugin.getLogger().severe("Ошибка при добавлении яйца в сундук: " + var121.getMessage());
      }
   }

   private void removeEggFromChest(Location location) {
      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "DELETE FROM dragon_eggs WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(sql);

            try {
               stmt.setInt(1, location.getBlockX());
               stmt.setInt(2, location.getBlockY());
               stmt.setInt(3, location.getBlockZ());
               stmt.setString(4, location.getWorld().getName());
               stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
               stmt.executeUpdate();
               this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                  try {
                     if (this.plugin.getHologramManager() != null) {
                        this.plugin.getHologramManager().updateTopHologram();
                     }
                  } catch (Exception var2x) {
                  }
               });
            } catch (Throwable var9) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var101) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var71) {
                  var101.addSuppressed(var71);
               }
            }

            throw var101;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var111) {
         this.plugin.getLogger().severe("Ошибка при удалении яйца из сундука: " + var111.getMessage());
      }
   }

   public void removeDragonEgg(Location location) {
      if (location != null) {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            try {
               String sql = "DELETE FROM dragon_eggs WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ?";
               PreparedStatement stmt = connection.prepareStatement(sql);

               try {
                  stmt.setInt(1, location.getBlockX());
                  stmt.setInt(2, location.getBlockY());
                  stmt.setInt(3, location.getBlockZ());
                  stmt.setString(4, location.getWorld().getName());
                  stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
                  stmt.executeUpdate();
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     try {
                        if (this.plugin.getHologramManager() != null) {
                           this.plugin.getHologramManager().updateTopHologram();
                        }
                     } catch (Exception var2x) {
                     }
                  });
               } catch (Throwable var9) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var101) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var71) {
                     var101.addSuppressed(var71);
                  }
               }

               throw var101;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var111) {
            this.plugin.getLogger().severe("Ошибка при удалении яйца дракона: " + var111.getMessage());
         }
      }
   }

   public List<DatabaseManager.DragonEggData> getGroupedDragonEggs() {
      ArrayList groupedEggs = new ArrayList();

      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "SELECT x, y, z, world, server, COUNT(*) as egg_count, SUM(current_value) as total_value FROM dragon_eggs GROUP BY x, y, z, world, server ORDER BY total_value DESC";
            Statement stmt = connection.createStatement();

            try {
               ResultSet rs = stmt.executeQuery(sql);

               try {
                  while (rs.next()) {
                     int x = rs.getInt("x");
                     int y = rs.getInt("y");
                     int z = rs.getInt("z");
                     String world = rs.getString("world");
                     String server = rs.getString("server");
                     int dbCount = rs.getInt("egg_count");
                     World bWorld = this.plugin.getServer().getWorld(world);
                     if (bWorld != null && bWorld.isChunkLoaded(x >> 4, z >> 4)) {
                        Block block = new Location(bWorld, x, y, z).getBlock();
                        if (block.getType() != Material.CHEST) {
                           this.removeDragonEgg(new Location(bWorld, x, y, z));
                        } else {
                           Chest chest = (Chest)block.getState();
                           int actualEggCount = 0;

                           for (ItemStack item : chest.getInventory().getContents()) {
                              if (item != null && EggItem.isDragonEgg(item)) {
                                 actualEggCount += item.getAmount();
                              }
                           }

                           if (actualEggCount > 0) {
                              if (actualEggCount != dbCount) {
                                 this.syncChestEggCount(new Location(bWorld, x, y, z), actualEggCount);
                              }

                              DatabaseManager.DragonEggData egg = new DatabaseManager.DragonEggData(x, y, z, world, server, 0, rs.getInt("total_value"));
                              egg.setEggCount(actualEggCount);
                              groupedEggs.add(egg);
                           } else {
                              this.removeDragonEgg(new Location(bWorld, x, y, z));
                           }
                        }
                     } else {
                        DatabaseManager.DragonEggData egg = new DatabaseManager.DragonEggData(x, y, z, world, server, 0, rs.getInt("total_value"));
                        egg.setEggCount(dbCount);
                        groupedEggs.add(egg);
                     }
                  }
               } catch (Throwable var23) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var221) {
                        var23.addSuppressed(var221);
                     }
                  }

                  throw var23;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var241) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var211) {
                     var241.addSuppressed(var211);
                  }
               }

               throw var241;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var251) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var201) {
                  var251.addSuppressed(var201);
               }
            }

            throw var251;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var261) {
         this.plugin.getLogger().severe("Ошибка при получении сгруппированных яиц дракона: " + var261.getMessage());
      }

      return groupedEggs;
   }

   public void updateEggValue(Location location, int newValue) {
      if (location != null) {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            try {
               String sql = "UPDATE dragon_eggs SET current_value = ?, last_increase_time = ? WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ?";
               PreparedStatement stmt = connection.prepareStatement(sql);

               try {
                  stmt.setInt(1, newValue);
                  stmt.setLong(2, System.currentTimeMillis());
                  stmt.setInt(3, location.getBlockX());
                  stmt.setInt(4, location.getBlockY());
                  stmt.setInt(5, location.getBlockZ());
                  stmt.setString(6, location.getWorld().getName());
                  stmt.setString(7, this.plugin.getConfig().getString("server-number", "server"));
                  stmt.executeUpdate();
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     try {
                        if (this.plugin.getHologramManager() != null) {
                           this.plugin.getHologramManager().updateTopHologram();
                        }
                     } catch (Exception var2x) {
                     }
                  });
               } catch (Throwable var10) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var111) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var81) {
                     var111.addSuppressed(var81);
                  }
               }

               throw var111;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var121) {
            this.plugin.getLogger().severe("Ошибка при обновлении стоимости яйца: " + var121.getMessage());
         }
      }
   }

   public boolean isEggStored(Location location) {
      if (location == null) {
         return false;
      } else {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            label117: {
               boolean var6;
               try {
                  PreparedStatement stmt;
                  label119: {
                     String sql = "SELECT COUNT(*) FROM dragon_eggs WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ?";
                     stmt = connection.prepareStatement(sql);

                     try {
                        stmt.setInt(1, location.getBlockX());
                        stmt.setInt(2, location.getBlockY());
                        stmt.setInt(3, location.getBlockZ());
                        stmt.setString(4, location.getWorld().getName());
                        stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
                        ResultSet rs = stmt.executeQuery();

                        label97: {
                           try {
                              if (!rs.next()) {
                                 break label97;
                              }

                              var6 = rs.getInt(1) > 0;
                           } catch (Throwable var121) {
                              if (rs != null) {
                                 try {
                                    rs.close();
                                 } catch (Throwable var11) {
                                    var121.addSuppressed(var11);
                                 }
                              }

                              throw var121;
                           }

                           if (rs != null) {
                              rs.close();
                           }
                           break label119;
                        }

                        if (rs != null) {
                           rs.close();
                        }
                     } catch (Throwable var131) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var10) {
                              var131.addSuppressed(var10);
                           }
                        }

                        throw var131;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                     break label117;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable var141) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var9) {
                        var141.addSuppressed(var9);
                     }
                  }

                  throw var141;
               }

               if (connection != null) {
                  connection.close();
               }

               return var6;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var15) {
            this.plugin.getLogger().severe("Ошибка при проверке яйца дракона: " + var15.getMessage());
         }

         return false;
      }
   }

   public void loadAllEggsOnReload() {
      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "SELECT x, y, z, world, server, base_value, current_value, last_increase_time FROM dragon_eggs";
            Statement stmt = connection.createStatement();

            try {
               ResultSet rs = stmt.executeQuery(sql);

               try {
                  HashMap eggsByLocation = new HashMap();

                  while (rs.next()) {
                     int x = rs.getInt("x");
                     int y = rs.getInt("y");
                     int z = rs.getInt("z");
                     String world = rs.getString("world");
                     String server = rs.getString("server");
                     if (server.equals(this.plugin.getConfig().getString("server-number", "server"))) {
                        World bWorld = this.plugin.getServer().getWorld(world);
                        if (bWorld != null) {
                           Location loc = new Location(bWorld, x, y, z);
                           DatabaseManager.EggData eggData = new DatabaseManager.EggData();
                           eggData.x = x;
                           eggData.y = y;
                           eggData.z = z;
                           eggData.world = world;
                           eggData.baseValue = rs.getInt("base_value");
                           eggData.currentValue = rs.getInt("current_value");
                           eggData.lastIncreaseTime = rs.getLong("last_increase_time");
                           eggsByLocation.computeIfAbsent(loc, k -> new ArrayList<>()).add(eggData);
                        }
                     }
                  }

                  for (Entry<Location, List<DatabaseManager.EggData>> entry : eggsByLocation.entrySet()) {
                     Location loc = entry.getKey();
                     List eggsData = entry.getValue();
                     if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        Block block = loc.getBlock();
                        if (block.getType() == Material.CHEST) {
                           Chest chest = (Chest)block.getState();
                           Inventory inventory = chest.getInventory();
                           boolean hasEggs = false;

                           for (ItemStack item : inventory.getContents()) {
                              if (item != null && EggItem.isDragonEgg(item)) {
                                 hasEggs = true;
                                 int itemBaseValue = EggItem.getBaseValue(item);
                                 int itemCurrentValue = EggItem.getEggValue(item);

                                 for (DatabaseManager.EggData eggData : eggsData) {
                                    if (eggData.baseValue == itemBaseValue) {
                                       if (eggData.currentValue != itemCurrentValue) {
                                          EggItem.updateEggValue(item, eggData.currentValue);
                                       }

                                       ItemMeta meta = item.getItemMeta();
                                       if (meta != null) {
                                          meta.getPersistentDataContainer().set(EggItem.getPlaceTimeKey(), PersistentDataType.LONG, eggData.lastIncreaseTime);
                                          item.setItemMeta(meta);
                                       }
                                       break;
                                    }
                                 }
                              }
                           }

                           if (!hasEggs) {
                              this.removeDragonEgg(loc);
                           }
                        }
                     }
                  }
               } catch (Throwable var251) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var241) {
                        var251.addSuppressed(var241);
                     }
                  }

                  throw var251;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var26) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var231) {
                     var26.addSuppressed(var231);
                  }
               }

               throw var26;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var271) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var22) {
                  var271.addSuppressed(var22);
               }
            }

            throw var271;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var281) {
         this.plugin.getLogger().severe("Ошибка при загрузке яиц при перезагрузке: " + var281.getMessage());
      }
   }

   public List<DatabaseManager.EggData> getAllEggs() {
      ArrayList eggs = new ArrayList();

      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "SELECT x, y, z, world, server, base_value, current_value, last_increase_time FROM dragon_eggs WHERE world = ? AND server = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);

            try {
               stmt.setString(1, "world");
               stmt.setString(2, this.plugin.getConfig().getString("server-number", "server"));
               ResultSet rs = stmt.executeQuery();

               try {
                  while (rs.next()) {
                     DatabaseManager.EggData egg = new DatabaseManager.EggData();
                     egg.x = rs.getInt("x");
                     egg.y = rs.getInt("y");
                     egg.z = rs.getInt("z");
                     egg.world = rs.getString("world");
                     egg.baseValue = rs.getInt("base_value");
                     egg.currentValue = rs.getInt("current_value");
                     egg.lastIncreaseTime = rs.getLong("last_increase_time");
                     eggs.add(egg);
                  }
               } catch (Throwable var11) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var121) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var91) {
                     var121.addSuppressed(var91);
                  }
               }

               throw var121;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var131) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var81) {
                  var131.addSuppressed(var81);
               }
            }

            throw var131;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var141) {
         this.plugin.getLogger().severe("Ошибка при получении всех яиц: " + var141.getMessage());
      }

      return eggs;
   }

   public void updateEggValueInDatabase(int x, int y, int z, String world, int baseValue, int oldValue, int newValue) {
      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "UPDATE dragon_eggs SET current_value = ?, last_increase_time = ? WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ? AND base_value = ? AND current_value = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);

            try {
               stmt.setInt(1, newValue);
               stmt.setLong(2, System.currentTimeMillis());
               stmt.setInt(3, x);
               stmt.setInt(4, y);
               stmt.setInt(5, z);
               stmt.setString(6, world);
               stmt.setString(7, this.plugin.getConfig().getString("server-number", "server"));
               stmt.setInt(8, baseValue);
               stmt.setInt(9, oldValue);
               stmt.executeUpdate();
            } catch (Throwable var15) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }
               }

               throw var15;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var161) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var131) {
                  var161.addSuppressed(var131);
               }
            }

            throw var161;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var171) {
         this.plugin.getLogger().severe("Ошибка при обновлении стоимости яйца в БД: " + var171.getMessage());
      }
   }

   public void saveEggValueToDatabase(int x, int y, int z, String world, int baseValue, int currentValue) {
      try {
         Connection connection = this.plugin.getDataSource().getConnection();

         try {
            String sql = "UPDATE dragon_eggs SET current_value = ?, last_increase_time = ? WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ? AND base_value = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);

            try {
               stmt.setInt(1, currentValue);
               stmt.setLong(2, System.currentTimeMillis());
               stmt.setInt(3, x);
               stmt.setInt(4, y);
               stmt.setInt(5, z);
               stmt.setString(6, world);
               stmt.setString(7, this.plugin.getConfig().getString("server-number", "server"));
               stmt.setInt(8, baseValue);
               stmt.executeUpdate();
            } catch (Throwable var14) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }
               }

               throw var14;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var151) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var121) {
                  var151.addSuppressed(var121);
               }
            }

            throw var151;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var161) {
         this.plugin.getLogger().severe("Ошибка при сохранении стоимости яйца в БД: " + var161.getMessage());
      }
   }

   public List<DatabaseManager.EggData> getEggsData(Location location) {
      List<DatabaseManager.EggData> eggs = new ArrayList<>();
      if (location == null) {
         return eggs;
      } else {
         try {
            Connection connection = this.plugin.getDataSource().getConnection();

            try {
               String sql = "SELECT x, y, z, world, server, base_value, current_value, last_increase_time FROM dragon_eggs WHERE x = ? AND y = ? AND z = ? AND world = ? AND server = ?";
               PreparedStatement stmt = connection.prepareStatement(sql);

               try {
                  stmt.setInt(1, location.getBlockX());
                  stmt.setInt(2, location.getBlockY());
                  stmt.setInt(3, location.getBlockZ());
                  stmt.setString(4, location.getWorld().getName());
                  stmt.setString(5, this.plugin.getConfig().getString("server-number", "server"));
                  ResultSet rs = stmt.executeQuery();

                  try {
                     while (rs.next()) {
                        DatabaseManager.EggData egg = new DatabaseManager.EggData();
                        egg.x = rs.getInt("x");
                        egg.y = rs.getInt("y");
                        egg.z = rs.getInt("z");
                        egg.world = rs.getString("world");
                        egg.baseValue = rs.getInt("base_value");
                        egg.currentValue = rs.getInt("current_value");
                        egg.lastIncreaseTime = rs.getLong("last_increase_time");
                        eggs.add(egg);
                     }
                  } catch (Throwable var12) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var11) {
                           var12.addSuppressed(var11);
                        }
                     }

                     throw var12;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var131) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var101) {
                        var131.addSuppressed(var101);
                     }
                  }

                  throw var131;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var141) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var91) {
                     var141.addSuppressed(var91);
                  }
               }

               throw var141;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var151) {
            this.plugin.getLogger().severe("Ошибка при получении данных яиц: " + var151.getMessage());
         }

         return eggs;
      }
   }

   public static class DragonEggData {
      private String playerName;
      private int x;
      private int y;
      private int z;
      private String world;
      private String server;
      private int baseValue;
      private int currentValue;
      private int eggCount;
      private long lastIncreaseTime;

      public DragonEggData(int x, int y, int z, String world, String server, int baseValue, int currentValue) {
         this.playerName = "";
         this.x = x;
         this.y = y;
         this.z = z;
         this.world = world;
         this.server = server;
         this.baseValue = baseValue;
         this.currentValue = currentValue;
         this.eggCount = 1;
         this.lastIncreaseTime = 0L;
      }

      @Generated
      public String getPlayerName() {
         return this.playerName;
      }

      @Generated
      public int getX() {
         return this.x;
      }

      @Generated
      public int getY() {
         return this.y;
      }

      @Generated
      public int getZ() {
         return this.z;
      }

      @Generated
      public String getWorld() {
         return this.world;
      }

      @Generated
      public String getServer() {
         return this.server;
      }

      @Generated
      public int getBaseValue() {
         return this.baseValue;
      }

      @Generated
      public int getCurrentValue() {
         return this.currentValue;
      }

      @Generated
      public int getEggCount() {
         return this.eggCount;
      }

      @Generated
      public long getLastIncreaseTime() {
         return this.lastIncreaseTime;
      }

      @Generated
      public void setPlayerName(String playerName) {
         this.playerName = playerName;
      }

      @Generated
      public void setX(int x) {
         this.x = x;
      }

      @Generated
      public void setY(int y) {
         this.y = y;
      }

      @Generated
      public void setZ(int z) {
         this.z = z;
      }

      @Generated
      public void setWorld(String world) {
         this.world = world;
      }

      @Generated
      public void setServer(String server) {
         this.server = server;
      }

      @Generated
      public void setBaseValue(int baseValue) {
         this.baseValue = baseValue;
      }

      @Generated
      public void setCurrentValue(int currentValue) {
         this.currentValue = currentValue;
      }

      @Generated
      public void setEggCount(int eggCount) {
         this.eggCount = eggCount;
      }

      @Generated
      public void setLastIncreaseTime(long lastIncreaseTime) {
         this.lastIncreaseTime = lastIncreaseTime;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof DatabaseManager.DragonEggData)) {
            return false;
         } else {
            DatabaseManager.DragonEggData other = (DatabaseManager.DragonEggData)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.getX() != other.getX()) {
               return false;
            } else if (this.getY() != other.getY()) {
               return false;
            } else if (this.getZ() != other.getZ()) {
               return false;
            } else if (this.getBaseValue() != other.getBaseValue()) {
               return false;
            } else if (this.getCurrentValue() != other.getCurrentValue()) {
               return false;
            } else if (this.getEggCount() != other.getEggCount()) {
               return false;
            } else if (this.getLastIncreaseTime() != other.getLastIncreaseTime()) {
               return false;
            } else {
               Object this$playerName = this.getPlayerName();
               Object other$playerName = other.getPlayerName();
               if (this$playerName == null) {
                  if (other$playerName != null) {
                     return false;
                  }
               } else if (!this$playerName.equals(other$playerName)) {
                  return false;
               }

               Object this$world = this.getWorld();
               Object other$world = other.getWorld();
               if (this$world == null ? other$world == null : this$world.equals(other$world)) {
                  this$world = this.getServer();
                  other$world = other.getServer();
                  if (this$world == null) {
                     if (other$world != null) {
                        return false;
                     }
                  } else if (!this$world.equals(other$world)) {
                     return false;
                  }

                  return true;
               } else {
                  return false;
               }
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof DatabaseManager.DragonEggData;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getX();
         result = result * 59 + this.getY();
         result = result * 59 + this.getZ();
         result = result * 59 + this.getBaseValue();
         result = result * 59 + this.getCurrentValue();
         result = result * 59 + this.getEggCount();
         long $lastIncreaseTime = this.getLastIncreaseTime();
         result = result * 59 + (int)($lastIncreaseTime >>> 32 ^ $lastIncreaseTime);
         Object $playerName = this.getPlayerName();
         result = result * 59 + ($playerName == null ? 43 : $playerName.hashCode());
         Object $world = this.getWorld();
         result = result * 59 + ($world == null ? 43 : $world.hashCode());
         Object $server = this.getServer();
         return result * 59 + ($server == null ? 43 : $server.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "DatabaseManager.DragonEggData(playerName="
            + this.getPlayerName()
            + ", x="
            + this.getX()
            + ", y="
            + this.getY()
            + ", z="
            + this.getZ()
            + ", world="
            + this.getWorld()
            + ", server="
            + this.getServer()
            + ", baseValue="
            + this.getBaseValue()
            + ", currentValue="
            + this.getCurrentValue()
            + ", eggCount="
            + this.getEggCount()
            + ", lastIncreaseTime="
            + this.getLastIncreaseTime()
            + ")";
      }

      @Generated
      public DragonEggData(
         String playerName, int x, int y, int z, String world, String server, int baseValue, int currentValue, int eggCount, long lastIncreaseTime
      ) {
         this.playerName = playerName;
         this.x = x;
         this.y = y;
         this.z = z;
         this.world = world;
         this.server = server;
         this.baseValue = baseValue;
         this.currentValue = currentValue;
         this.eggCount = eggCount;
         this.lastIncreaseTime = lastIncreaseTime;
      }
   }

   public static class EggData {
      public int x;
      public int y;
      public int z;
      public String world;
      public int baseValue;
      public int currentValue;
      public long lastIncreaseTime;

      public EggData() {
      }
   }

   public static class EndSettings {
      private int id;
      private int startHour;
      private int startMinute;
      private int dayOfWeek;
      private int duration;
      private String timezone;

      public EndSettings() {
      }

      @Generated
      public int getId() {
         return this.id;
      }

      @Generated
      public int getStartHour() {
         return this.startHour;
      }

      @Generated
      public int getStartMinute() {
         return this.startMinute;
      }

      @Generated
      public int getDayOfWeek() {
         return this.dayOfWeek;
      }

      @Generated
      public int getDuration() {
         return this.duration;
      }

      @Generated
      public String getTimezone() {
         return this.timezone;
      }

      @Generated
      public void setId(int id) {
         this.id = id;
      }

      @Generated
      public void setStartHour(int startHour) {
         this.startHour = startHour;
      }

      @Generated
      public void setStartMinute(int startMinute) {
         this.startMinute = startMinute;
      }

      @Generated
      public void setDayOfWeek(int dayOfWeek) {
         this.dayOfWeek = dayOfWeek;
      }

      @Generated
      public void setDuration(int duration) {
         this.duration = duration;
      }

      @Generated
      public void setTimezone(String timezone) {
         this.timezone = timezone;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof DatabaseManager.EndSettings)) {
            return false;
         } else {
            DatabaseManager.EndSettings other = (DatabaseManager.EndSettings)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.getId() != other.getId()) {
               return false;
            } else if (this.getStartHour() != other.getStartHour()) {
               return false;
            } else if (this.getStartMinute() != other.getStartMinute()) {
               return false;
            } else if (this.getDayOfWeek() != other.getDayOfWeek()) {
               return false;
            } else if (this.getDuration() != other.getDuration()) {
               return false;
            } else {
               Object this$timezone = this.getTimezone();
               Object other$timezone = other.getTimezone();
               if (this$timezone == null) {
                  if (other$timezone != null) {
                     return false;
                  }
               } else if (!this$timezone.equals(other$timezone)) {
                  return false;
               }

               return true;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof DatabaseManager.EndSettings;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getId();
         result = result * 59 + this.getStartHour();
         result = result * 59 + this.getStartMinute();
         result = result * 59 + this.getDayOfWeek();
         result = result * 59 + this.getDuration();
         Object $timezone = this.getTimezone();
         return result * 59 + ($timezone == null ? 43 : $timezone.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "DatabaseManager.EndSettings(id="
            + this.getId()
            + ", startHour="
            + this.getStartHour()
            + ", startMinute="
            + this.getStartMinute()
            + ", dayOfWeek="
            + this.getDayOfWeek()
            + ", duration="
            + this.getDuration()
            + ", timezone="
            + this.getTimezone()
            + ")";
      }
   }
}
