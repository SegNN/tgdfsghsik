package org.impldev.holyclans;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holyclans.menu.MenuService;

public final class HolyClans extends JavaPlugin {
   private Economy economy;
   private ClanData data;
   private InviteManager invites;
   private ConfirmManager confirms;
   private MenuService menus;

   /** Игроки, у которых включён клан-чат. */
   private final Set<UUID> clanChatActive = ConcurrentHashMap.newKeySet();
   /** Игроки, ожидающие ввода имени клана в чат. */
   private final Set<UUID> awaitingName = ConcurrentHashMap.newKeySet();

   @Override
   public void onEnable() {
      this.saveDefaultConfig();
      this.economy = this.hookEconomy();
      this.data = new ClanData(this);
      this.invites = new InviteManager();
      this.confirms = new ConfirmManager();
      this.menus = new MenuService(this);

      if (this.economy == null) {
         this.getLogger().warning("Vault не найден: экономика работать не будет.");
      }

      ClanCommand cmd = new ClanCommand(this);
      ClanListener listener = new ClanListener(this);

      this.getServer().getPluginManager().registerEvents(listener, this);
      this.getServer().getPluginManager().registerEvents(this.menus, this);

      if (this.getCommand("clan") != null) {
         this.getCommand("clan").setExecutor(cmd);
         this.getCommand("clan").setTabCompleter(cmd);
      }

      // Очистка просроченных приглашений каждые 60 секунд.
      this.getServer().getScheduler().runTaskTimer(this, this.invites::cleanup, 1200L, 1200L);
      // Сохранение данных каждые 5 минут.
      this.getServer().getScheduler().runTaskTimer(this, this.data::saveAll, 6000L, 6000L);

      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         new ClanPlaceholders(this).register();
      }
   }

   @Override
   public void onDisable() {
      if (this.data != null) {
         this.data.saveAll();
      }
   }

   private Economy hookEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) return null;
      RegisteredServiceProvider<Economy> rsp =
            this.getServer().getServicesManager().getRegistration(Economy.class);
      return rsp == null ? null : rsp.getProvider();
   }

   public Economy economy() { return this.economy; }
   public ClanData data() { return this.data; }
   public InviteManager invites() { return this.invites; }
   public ConfirmManager confirms() { return this.confirms; }
   public MenuService menus() { return this.menus; }
   public Set<UUID> clanChatActive() { return this.clanChatActive; }
   public Set<UUID> awaitingName() { return this.awaitingName; }

   /** Есть ли WorldGuard на сервере. */
   public boolean hasWorldGuard() {
      return this.getServer().getPluginManager().getPlugin("WorldGuard") != null;
   }
}
