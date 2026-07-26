package ru.kotikstasika.holyliteend.api;

import com.sk89q.worldedit.EditSession;
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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.holyliteend.HolyLiteEnd;

public class WorldEdit {
   private EditSession lastEditSession;

   public WorldEdit() {
   }

   public void loadAndPasteSchematic() {
      Location location = HolyLiteEnd.getInstance().getEndSpawnLocation();
      File schematicFile = new File("plugins/WorldEdit/schematics/events/end/", "end.schem");
      if (schematicFile.exists()) {
         ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
         if (format != null) {
            try {
               ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

               try {
                  Clipboard clipboard = reader.read();
                  ClipboardHolder holder = new ClipboardHolder(clipboard);
                  this.lastEditSession = com.sk89q.worldedit.WorldEdit.getInstance()
                     .getEditSessionFactory()
                     .getEditSession(BukkitAdapter.adapt(location.getWorld()), -1);
                  BlockVector3 to = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                  Operation operation = holder.createPaste(this.lastEditSession).to(to).ignoreAirBlocks(true).build();
                  Operations.complete(operation);
                  this.lastEditSession.flushSession();
                  this.asyncRemovePiston(location);
               } catch (Throwable var10) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (reader != null) {
                  reader.close();
               }
            } catch (Exception var11) {
            }
         }
      }
   }

   private void asyncRemovePiston(final Location center) {
      if (center != null && center.getWorld() != null) {
         final World world = center.getWorld();
         (new BukkitRunnable() {
            private int currentX = center.getBlockX() - 40;
            private final int maxX = center.getBlockX() + 40;
            private int currentZ = center.getBlockZ() - 40;
            private final int maxZ = center.getBlockZ() + 40;
            private final int batchSize = 150;
            private int processedInBatch = 0;

            public void run() {
               while (this.currentX <= this.maxX) {
                  for (; this.currentZ <= this.maxZ; this.currentZ++) {
                     if (world.isChunkLoaded(this.currentX >> 4, this.currentZ >> 4)) {
                        Chunk chunk = world.getChunkAt(this.currentX >> 4, this.currentZ >> 4);

                        for (int y = 0; y <= 255; y++) {
                           Block block = chunk.getBlock(this.currentX & 15, y, this.currentZ & 15);
                           Material blockType = block.getType();
                           String materialName = blockType.name();
                           if (materialName.endsWith("PISTON")) {
                              block.setType(Material.AIR, false);
                              this.processedInBatch++;
                              if (this.processedInBatch >= 150) {
                                 this.processedInBatch = 0;
                                 return;
                              }
                           }
                        }

                        this.processedInBatch = 0;
                     }
                  }

                  this.currentZ = center.getBlockZ() - 40;
                  this.currentX++;
               }

               this.cancel();
            }
         }).runTaskTimer(HolyLiteEnd.getInstance(), 1L, 1L);
      }
   }
}
