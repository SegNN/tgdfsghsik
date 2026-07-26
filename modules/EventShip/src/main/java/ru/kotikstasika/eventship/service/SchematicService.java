package ru.kotikstasika.eventship.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SchematicService {
   private EditSession lastEditSession;
   private final Logger logger = Bukkit.getLogger();

   public SchematicService() {
   }

   public boolean loadAndPasteSchematic(File dataFolder, String schematicName, Location location) {
      File schematicFile = new File(dataFolder, schematicName);
      if (!schematicFile.exists()) {
         schematicFile = new File("plugins/WorldEdit/schematics/" + schematicName);
         if (!schematicFile.exists()) {
            return false;
         }
      }

      ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
      if (format == null) {
         return false;
      } else {
         try {
            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

            boolean var11;
            try {
               Clipboard clipboard = reader.read();
               ClipboardHolder holder = new ClipboardHolder(clipboard);
               this.lastEditSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(location.getWorld()), -1);
               this.lastEditSession.setFastMode(true);
               this.lastEditSession.setMask(null);
               BlockVector3 to = BlockVector3.at(location.getX(), location.getY(), location.getZ());
               Operation operation = holder.createPaste(this.lastEditSession).to(to).ignoreAirBlocks(true).build();
               Operations.complete(operation);
               this.lastEditSession.flushSession();
               var11 = true;
            } catch (Throwable var13) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (reader != null) {
               reader.close();
            }

            return var11;
         } catch (Exception var14) {
            this.logger.severe("SchematicService: Ошибка при загрузке схемы: " + var14.getMessage());
            return false;
         }
      }
   }

   public void undoLastPaste() {
      if (this.lastEditSession != null) {
         try {
            this.lastEditSession.undo(this.lastEditSession);
            this.lastEditSession.close();
         } catch (Exception var2) {
            this.logger.warning("Не удалось отменить изменения: " + var2.getMessage());
         }

         this.lastEditSession = null;
      }
   }
}
