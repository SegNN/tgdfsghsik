package ru.kotikstasika.eventdeathmine.service;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicService {
   private final JavaPlugin plugin;

   public SchematicService(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public CompletableFuture<Clipboard> readClipboardAsync(String schematicName) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            File schematicFile = new File(this.plugin.getDataFolder(), schematicName);
            if (!schematicFile.exists()) {
               schematicFile = new File("plugins/WorldEdit/schematics/" + schematicName);
               if (!schematicFile.exists()) {
                  return null;
               }
            }

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
               return null;
            } else {
               ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

               Clipboard var5;
               try {
                  var5 = reader.read();
               } catch (Throwable var8) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (reader != null) {
                  reader.close();
               }

               return var5;
            }
         } catch (Exception var9) {
            return null;
         }
      });
   }
}
