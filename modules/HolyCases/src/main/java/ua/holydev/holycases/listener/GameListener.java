package ua.holydev.holycases.listener;

import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.cases.CaseHolder;
import ua.holydev.holycases.cases.CaseManager;
import ua.holydev.holycases.cases.GameCase;
import ua.holydev.holycases.configuration.CasesConfig;
import ua.holydev.holycases.util.ItemBuilder;

public class GameListener implements Listener {
   private HolyCases plugin;
   private CaseManager caseManager;
   private CasesConfig casesConfig;
   private final Object lock = new Object();

   public GameListener(HolyCases plugin, CaseManager caseManager, CasesConfig casesConfig) {
      this.plugin = plugin;
      this.caseManager = caseManager;
      this.casesConfig = casesConfig;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      GameCase.remove(player);
   }

   @EventHandler
   public void onInt(final PlayerInteractEvent e) {
      if (e.getClickedBlock() != null && e.getClickedBlock().hasMetadata("case")) {
         e.setCancelled(true);
         if (((MetadataValue)e.getClickedBlock().getMetadata("case").get(0)).value() instanceof GameCase gameCase && gameCase != null) {
            (new BukkitRunnable() {
               public void run() {
                  synchronized (GameListener.this.lock) {
                     if (!gameCase.isOpening()) {
                        gameCase.openMenu(e.getPlayer());
                     } else if (!gameCase.getOpening().equals(e.getPlayer())) {
                        gameCase.getMessages().sendMessage(e.getPlayer(), "case-already-opening");
                     }
                  }
               }
            }).runTaskLater(this.plugin, 1L);
         }
      }
   }

   @EventHandler
   public void onCaseAdmin(PlayerInteractEvent e) {
      if (e.getItem() != null && e.getItem().getType() == Material.ENDER_CHEST) {
         if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName()) {
            if (e.getClickedBlock() != null) {
               if (e.getPlayer().hasPermission("holycases.command.place")) {
                  String caseName = new ItemBuilder(e.getItem()).name();
                  CaseHolder caseHolder = this.caseManager.getCaseHolder(caseName);
                  if (caseHolder != null) {
                     Player player = e.getPlayer();
                     e.setCancelled(true);
                     if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        CaseHolder aleradyExists = this.caseManager.getHolders().stream().filter(holder -> {
                           for (GameCase gameCase : holder.getCases()) {
                              if (gameCase.getLocation().getBlock().equals(e.getClickedBlock())) {
                                 return true;
                              }
                           }

                           return false;
                        }).findAny().orElse(null);
                        if (aleradyExists != null) {
                           player.sendMessage("На этом месте уже стоит кейс " + aleradyExists.getCaseName());
                           return;
                        }

                        this.casesConfig.addCaseBlock(e.getClickedBlock(), caseName);
                        player.sendMessage("Локация добавлена в конфиг");
                        this.caseManager.loadCases();
                     }

                     if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        CaseHolder aleradyExists = this.caseManager.getHolders().stream().filter(holder -> {
                           for (GameCase gameCase : holder.getCases()) {
                              if (gameCase.getLocation().getBlock().equals(e.getClickedBlock()) && gameCase.getName().equalsIgnoreCase(caseName)) {
                                 return true;
                              }
                           }

                           return false;
                        }).findAny().orElse(null);
                        if (aleradyExists == null) {
                           player.sendMessage("На этом месте нет кейса " + caseName);
                           return;
                        }

                        caseHolder.removeCase(caseHolder.getCaseBy(e.getClickedBlock().getLocation()));
                        e.getClickedBlock().removeMetadata("case", this.plugin);
                        this.casesConfig.removeCaseBlock(e.getClickedBlock().getLocation(), caseName);
                        player.sendMessage("Локация удалена из конфига");
                        this.caseManager.loadCases();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.caseManager.getHolders().forEach(holder -> {
         Set<Supplier<GameCase>> creators = holder.getCreators(e.getWorld().getName());
         if (creators != null) {
            creators.forEach(creator -> holder.addCase(creator.get()));
         }
      });
   }
}
