package ru.kotikstasika.kotikdang.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.DangType;

public class WorldGuardUtils {
   public WorldGuardUtils() {
   }

   public void createRegion(String id, Location loc, DangType type) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager != null) {
         if (regionManager.hasRegion(id)) {
            regionManager.removeRegion(id);
         }

         BlockVector3 pos1 = BlockVector3.at(loc.getX() + 28.0, 0.0, loc.getZ() + 28.0);
         BlockVector3 pos2 = BlockVector3.at(loc.getX() - 28.0, 255.0, loc.getZ() - 28.0);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, pos1, pos2);
         switch (type.getDangType()) {
            case GRIEF:
               this.setGriefFlags(region, type);
               break;
            case UNIK:
               this.setUnikFlags(region);
         }

         regionManager.addRegion(region);
      }
   }

   public void removeRegion(String id, Location loc) {
      try {
         RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
         if (regionManager == null) {
            return;
         }

         if (regionManager.hasRegion(id)) {
            regionManager.removeRegion(id);

            try {
               regionManager.save();
            } catch (Exception var5) {
            }
         }
      } catch (Exception var6) {
      }
   }

   private void setGriefFlags(ProtectedCuboidRegion region, DangType type) {
      StateFlag holyItemsFlag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
      if (holyItemsFlag != null) {
         region.setFlag(holyItemsFlag, State.DENY);
      }

      region.setFlag(Flags.BUILD, State.DENY);
      region.setFlag(Flags.WITHER_DAMAGE, State.ALLOW);
      region.setFlag(Flags.BLOCK_PLACE, State.DENY);
      region.setFlag(Flags.CHEST_ACCESS, State.ALLOW);
      region.setFlag(Flags.GHAST_FIREBALL, State.ALLOW);
      region.setFlag(Flags.INTERACT, State.ALLOW);
      region.setFlag(Flags.TNT, State.ALLOW);
      region.setFlag(Flags.OTHER_EXPLOSION, State.ALLOW);
      region.setFlag(Flags.CREEPER_EXPLOSION, State.ALLOW);
      region.setFlag(Flags.PVP, State.ALLOW);
      Material anchorMaterial = type.getAnchorType();
      String greetMessage = anchorMaterial == Material.LODESTONE
         ? "&6▶ &fВы попали на территорию &6Сокровища&f. Вам необходимо &6найти блок-привет &7(Магнетит) &6и загрифферить его&f, чтобы заполучить доступ к драгоценным ресурсам."
         : "&6▶ &fВы попали на территорию &6Сокровища&f. Вам необходимо &6найти блок-привет &7(Якорь возрождения) &6и загрифферить его&f, чтобы заполучить доступ к драгоценным ресурсам.";
      region.setFlag(Flags.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', greetMessage));
   }

   public void setFlagsGriefKill(ProtectedCuboidRegion region) {
      try {
         StateFlag holyItemsFlag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         if (holyItemsFlag != null) {
            region.setFlag(holyItemsFlag, State.ALLOW);
         }

         region.setFlag(Flags.BUILD, State.ALLOW);
         region.setFlag(Flags.WITHER_DAMAGE, State.ALLOW);
         region.setFlag(Flags.BLOCK_PLACE, State.ALLOW);
         region.setFlag(Flags.BLOCK_BREAK, State.ALLOW);
         region.setFlag(Flags.CHEST_ACCESS, State.ALLOW);
         region.setFlag(Flags.GHAST_FIREBALL, State.ALLOW);
         region.setFlag(Flags.INTERACT, State.ALLOW);
         region.setFlag(Flags.TNT, State.ALLOW);
         region.setFlag(Flags.OTHER_EXPLOSION, State.ALLOW);
         region.setFlag(Flags.CREEPER_EXPLOSION, State.ALLOW);
         region.setFlag(Flags.PVP, State.ALLOW);
      } catch (Exception var3) {
         KotikDang.plugin.getLogger().severe("Ошибка при установке флагов для региона " + region.getId() + ": " + var3.getMessage());
      }
   }

   private void setUnikFlags(ProtectedCuboidRegion region) {
      StateFlag holyItemsFlag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
      if (holyItemsFlag != null) {
         region.setFlag(holyItemsFlag, State.DENY);
      }

      region.setFlag(Flags.BUILD, State.DENY);
      region.setFlag(Flags.WITHER_DAMAGE, State.DENY);
      region.setFlag(Flags.BLOCK_PLACE, State.DENY);
      region.setFlag(Flags.CHEST_ACCESS, State.ALLOW);
      region.setFlag(Flags.GHAST_FIREBALL, State.DENY);
      region.setFlag(Flags.INTERACT, State.ALLOW);
      region.setFlag(Flags.TNT, State.DENY);
      region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
      region.setFlag(Flags.CREEPER_EXPLOSION, State.DENY);
      region.setFlag(
         Flags.GREET_MESSAGE,
         ChatColor.translateAlternateColorCodes(
            '&',
            "&6▶ &fВы попали на территорию &6Уникального данжа&f. Вам необходимо &6найти шалкера &6и открыть его универсальным ключом&f, чтобы заполучить доступ к драгоценным ресурсам."
         )
      );
   }
}
