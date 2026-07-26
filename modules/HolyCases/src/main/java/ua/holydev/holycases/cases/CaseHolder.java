package ua.holydev.holycases.cases;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class CaseHolder {
   private Plugin plugin;
   private String caseName;
   private Set<GameCase> cases;
   private Map<String, Set<Supplier<GameCase>>> caseCreators;

   public CaseHolder(Plugin plugin, String caseName) {
      this.plugin = plugin;
      this.caseName = caseName;
      this.cases = new HashSet<>();
      this.caseCreators = new HashMap<>();
   }

   public String getCaseName() {
      return this.caseName;
   }

   public GameCase getCaseBy(Location location) {
      for (GameCase gameCase : this.getCases()) {
         Location loc = gameCase.getLocation();
         if (loc.getX() == location.getX() && loc.getY() == location.getY() && loc.getZ() == location.getZ()) {
            return gameCase;
         }
      }

      return null;
   }

   public void addCase(GameCase gameCase) {
      this.cases.add(gameCase);
      gameCase.getLocation().getBlock().setMetadata("case", new FixedMetadataValue(this.plugin, gameCase));
   }

   public void removeCase(GameCase gameCase) {
      this.cases.remove(gameCase);
      gameCase.deleteTitle();
      gameCase.getLocation().getBlock().removeMetadata("case", this.plugin);
      this.caseCreators.forEach((worldName, creators) -> {
         if (Bukkit.getWorld(worldName) != null) {
            creators.forEach(creator -> {
               if (creator.get() == gameCase) {
                  creators.remove(creator);
               }
            });
         }
      });
   }

   public void addCreator(String worldName, Supplier<GameCase> creator) {
      if (!this.caseCreators.containsKey(worldName)) {
         this.caseCreators.put(worldName, new HashSet<>());
      }

      this.caseCreators.get(worldName).add(creator);
   }

   public Set<Supplier<GameCase>> getCreators(String worldName) {
      return this.caseCreators.get(worldName);
   }

   public Collection<GameCase> getCases() {
      return Collections.unmodifiableCollection(this.cases);
   }

   public void onReload() {
      this.caseCreators.forEach((worldName, creators) -> {
         if (Bukkit.getWorld(worldName) != null) {
            long loaded = this.cases.stream().filter(gameCase -> gameCase.getLocation().getWorld().getName().equals(worldName)).count();
            if (loaded == 0L) {
               creators.forEach(creator -> this.addCase(creator.get()));
            }
         }
      });
   }
}
