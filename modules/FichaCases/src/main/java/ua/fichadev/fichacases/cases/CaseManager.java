package ua.fichadev.fichacases.cases;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichacases.configuration.CasesConfig;

public class CaseManager {
   private Plugin plugin;
   private CasesConfig casesConfig;
   private Map<String, CaseHolder> holders;

   public CaseManager(Plugin plugin, CasesConfig casesConfig) {
      this.plugin = plugin;
      this.casesConfig = casesConfig;
      this.holders = casesConfig.getCases();
      this.startParticlesTask();
   }

   public void loadCases() {
      this.holders = this.casesConfig.getCases();
      this.getHolders().forEach(CaseHolder::onReload);
   }

   public Collection<CaseHolder> getHolders() {
      return Collections.unmodifiableCollection(this.holders.values());
   }

   public Collection<String> getCaseNames() {
      return Collections.unmodifiableCollection(this.holders.keySet());
   }

   public CaseHolder getCaseHolder(String name) {
      return this.holders.get(name);
   }

   private void startParticlesTask() {
      (new BukkitRunnable() {
            public void run() {
               CaseManager.this.holders
                  .values()
                  .forEach(
                     caseHolder -> caseHolder.getCases()
                        .forEach(
                           gameCase -> {
                              if (!gameCase.isOpening()) {
                                 Location caseLocation = gameCase.getLocation();
                                 gameCase.getWrappedParticles()
                                    .forEach(
                                       wrappedParticle -> caseLocation.getWorld()
                                          .spawnParticle(
                                             wrappedParticle.particle(),
                                             caseLocation.clone().add(0.5, 0.5, 0.5),
                                             wrappedParticle.count(),
                                             wrappedParticle.offX(),
                                             wrappedParticle.offY(),
                                             wrappedParticle.offZ(),
                                             wrappedParticle.speed(),
                                             wrappedParticle.getData()
                                          )
                                    );
                              }
                           }
                        )
                  );
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
   }
}
