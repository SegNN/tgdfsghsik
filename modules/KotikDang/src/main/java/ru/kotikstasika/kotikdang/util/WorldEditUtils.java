package ru.kotikstasika.kotikdang.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bukkit.Location;

public class WorldEditUtils {
   private final File schematicsDir = new File("plugins/WorldEdit/schematics/dangs");

   public WorldEditUtils() {
      if (!this.schematicsDir.exists()) {
         this.schematicsDir.mkdirs();
      }
   }

   public boolean pasteSchematic(Location loc, String schematicName) {
      if (schematicName != null && !schematicName.isEmpty()) {
         File schematicFile = this.findSchematicFile(schematicName);
         if (schematicFile != null && schematicFile.exists()) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
               System.err.println("Неизвестный формат схематики: " + schematicFile.getName());
               return false;
            } else {
               try {
                  boolean var10;
                  try (FileInputStream fis = new FileInputStream(schematicFile)) {
                     ClipboardReader reader = format.getReader(fis);

                     try {
                        EditSession editSession = WorldEdit.getInstance()
                           .newEditSessionBuilder()
                           .world(BukkitAdapter.adapt(loc.getWorld()))
                           .maxBlocks(-1)
                           .build();

                        try {
                           Clipboard clipboard = reader.read();
                           Operation operation = new ClipboardHolder(clipboard)
                              .createPaste(editSession)
                              .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                              .ignoreAirBlocks(true)
                              .build();
                           Operations.complete(operation);
                           editSession.flushSession();
                           System.out.println("Схема " + schematicName + " успешно вставлена");
                           var10 = true;
                        } catch (Throwable var14) {
                           if (editSession != null) {
                              try {
                                 editSession.close();
                              } catch (Throwable var13) {
                                 var14.addSuppressed(var13);
                              }
                           }

                           throw var14;
                        }

                        if (editSession != null) {
                           editSession.close();
                        }
                     } catch (Throwable var15) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var12) {
                              var15.addSuppressed(var12);
                           }
                        }

                        throw var15;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                  }

                  return var10;
               } catch (IOException | WorldEditException var17) {
                  System.err.println("Ошибка при вставке схематики:");
                  var17.printStackTrace();
                  return false;
               }
            }
         } else {
            System.err.println("Схематика не найдена: " + schematicName);
            return false;
         }
      } else {
         System.err.println("Имя схематики не указано!");
         return false;
      }
   }

   public File makeRegionBackup(Location center, int radius, String backupFileName) {
      try {
         File backupDir = new File("plugins/KotikDang/backups");
         if (!backupDir.exists()) {
            backupDir.mkdirs();
         }

         File out = new File(backupDir, backupFileName.endsWith(".schem") ? backupFileName : backupFileName + ".schem");
         int minX = center.getBlockX() - radius;
         int minY = 0;
         int minZ = center.getBlockZ() - radius;
         int maxX = center.getBlockX() + radius;
         int maxY = 255;
         int maxZ = center.getBlockZ() + radius;
         Region region = new CuboidRegion(BukkitAdapter.adapt(center.getWorld()), BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
         BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
         EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(center.getWorld())).build();

         try {
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(false);
            Operations.complete(copy);
         } catch (Throwable var22) {
            if (editSession != null) {
               try {
                  editSession.close();
               } catch (Throwable var20) {
                  var22.addSuppressed(var20);
               }
            }

            throw var22;
         }

         if (editSession != null) {
            editSession.close();
         }

         try (FileOutputStream fos = new FileOutputStream(out)) {
            ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(fos);

            try {
               writer.write(clipboard);
            } catch (Throwable var21) {
               if (writer != null) {
                  try {
                     writer.close();
                  } catch (Throwable var19) {
                     var21.addSuppressed(var19);
                  }
               }

               throw var21;
            }

            if (writer != null) {
               writer.close();
            }
         }

         return out;
      } catch (Exception var24) {
         var24.printStackTrace();
         return null;
      }
   }

   public boolean restoreFromBackup(Location center, int radius, String backupPath) {
      try {
         File file = new File(backupPath);
         if (!file.exists()) {
            return false;
         } else {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
               return false;
            } else {
               Clipboard clipboard;
               try (FileInputStream fis = new FileInputStream(file)) {
                  ClipboardReader reader = format.getReader(fis);

                  try {
                     clipboard = reader.read();
                  } catch (Throwable var17) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var15) {
                           var17.addSuppressed(var15);
                        }
                     }

                     throw var17;
                  }

                  if (reader != null) {
                     reader.close();
                  }
               }

               int var20 = center.getBlockX() - radius;
               byte var21 = 0;
               int pasteZ = center.getBlockZ() - radius;
               EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(center.getWorld())).maxBlocks(-1).build();

               try {
                  Operation operation = new ClipboardHolder(clipboard)
                     .createPaste(editSession)
                     .to(BlockVector3.at(var20, var21, pasteZ))
                     .ignoreAirBlocks(false)
                     .build();
                  Operations.complete(operation);
                  editSession.flushSession();
               } catch (Throwable var16) {
                  if (editSession != null) {
                     try {
                        editSession.close();
                     } catch (Throwable var13) {
                        var16.addSuppressed(var13);
                     }
                  }

                  throw var16;
               }

               if (editSession != null) {
                  editSession.close();
               }

               return true;
            }
         }
      } catch (IOException | WorldEditException var19) {
         var19.printStackTrace();
         return false;
      }
   }

   private File findSchematicFile(String name) {
      if (name == null) {
         return null;
      } else {
         File exact = new File(this.schematicsDir, name);
         if (exact.exists()) {
            return exact;
         } else {
            String[] extensions = new String[]{".schem", ".schematic"};

            for (String ext : extensions) {
               if (name.toLowerCase().endsWith(ext)) {
                  File file = new File(this.schematicsDir, name);
                  if (file.exists()) {
                     return file;
                  }
               } else {
                  File file = new File(this.schematicsDir, name + ext);
                  if (file.exists()) {
                     return file;
                  }
               }
            }

            File[] files = this.schematicsDir
               .listFiles((dir, filename) -> filename.equalsIgnoreCase(name) || filename.toLowerCase().startsWith(name.toLowerCase()));
            return files != null && files.length > 0 ? files[0] : null;
         }
      }
   }
}
