package ua.fichadev.fichacases.opentype.impls;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import shaded.org.jdbi.v3.core.async.JdbiExecutor;
import ua.fichadev.fichacases.FichaCases;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.database.dao.PlayerKeysDao;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.opentype.Open;

public class KeyOpen extends Open {
   private final FichaCases plugin;
   private final JdbiExecutor executor;
   private final LuckManager luckManager;

   public KeyOpen(FichaCases plugin, JdbiExecutor executor, LuckManager luckManager) {
      super(Open.Type.KEY);
      this.plugin = plugin;
      this.executor = executor;
      this.luckManager = luckManager;
   }

   @Override
   public void open(GameCase gameCase, ClickType clickType, Player whoClick) {
      String caseName = gameCase.getName();
      if (this.plugin.isSQL()) {
         this.executor.withExtension(PlayerKeysDao.class, dao -> dao.getKeys(whoClick.getUniqueId(), caseName)).whenComplete((keysx, ex1) -> {
            if (keysx < 1) {
               whoClick.closeInventory();
               gameCase.getMessages().sendMessage(whoClick, "not-have-keys-for-case");
            } else {
               this.executor.useExtension(PlayerKeysDao.class, dao -> dao.withdrawKeys(whoClick.getUniqueId(), caseName, 1));
               whoClick.closeInventory();
               this.sync(() -> gameCase.open(whoClick, this.luckManager.removeLast(whoClick)));
            }
         }).exceptionally(ex2 -> {
            ex2.printStackTrace();
            return null;
         });
      } else {
         int keys = this.plugin.getKeys(whoClick, caseName);
         if (keys < 1) {
            whoClick.closeInventory();
            gameCase.getMessages().sendMessage(whoClick, "not-have-keys-for-case");
         } else {
            this.plugin.removeKeys(whoClick, caseName, 1);
            whoClick.closeInventory();
            this.sync(() -> gameCase.open(whoClick, this.luckManager.removeLast(whoClick)));
         }
      }
   }

   private void sync(Runnable runnable) {
      Bukkit.getScheduler().runTask(this.plugin, runnable);
   }
}
