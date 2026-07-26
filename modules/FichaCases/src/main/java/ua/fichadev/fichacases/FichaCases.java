package ua.fichadev.fichacases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.async.JdbiExecutor;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import ua.fichadev.fichacases.cases.CaseHolder;
import ua.fichadev.fichacases.cases.CaseManager;
import ua.fichadev.fichacases.cases.GameCase;
import ua.fichadev.fichacases.command.CaseCommand;
import ua.fichadev.fichacases.configuration.CasesConfig;
import ua.fichadev.fichacases.configuration.DatabaseConfig;
import ua.fichadev.fichacases.database.ConnectionInfo;
import ua.fichadev.fichacases.database.dao.PlayerKeysDao;
import ua.fichadev.fichacases.database.dao.entity.PrizeHistoryEntry;
import ua.fichadev.fichacases.listener.GameListener;
import ua.fichadev.fichacases.lucks.LuckManager;
import ua.fichadev.fichacases.menu.MenuManager;
import ua.fichadev.fichacases.menu.MenuManagerFactory;
import ua.fichadev.fichacases.nms.CustomArmorStandHandler;
import ua.fichadev.fichacases.placeholder.CasePlaceholder;
import ua.fichadev.fichacases.reward.Reward;

public class FichaCases extends JavaPlugin {
   private Map<OfflinePlayer, GameCase> opens = new HashMap<>();
   private DatabaseConfig databaseConfig;
   private Jdbi jdbi;
   private JdbiExecutor jdbiExecutor;
   private CasesConfig casesConfig;
   private LuckManager luckManager;
   private MenuManager menuManager;
   private CaseManager caseManager;
   private CustomArmorStandHandler customArmorStandHandler;
   private YamlConfiguration config;
   private YamlConfiguration database;
   private static FichaCases instance;
   private boolean isSQL = false;

   public FichaCases() {
   }

   public void onEnable() {
      this.initConfig();
      if (Objects.equals(this.config.getString("database"), "sql")) {
         this.databaseConfig = new DatabaseConfig(this);

         try {
            this.initJDBI(this.databaseConfig.getConnectionInfo());
         } catch (Exception var2) {
            this.getLogger().severe("Настройте данные для подключения к базе данных в DatabaseConfig!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.isSQL = true;
      } else {
         if (!Objects.equals(this.config.getString("database"), "yaml")) {
            this.getLogger().severe("Укажите верное значение в config! (sql или yaml)");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.initDatabaseYaml();
         this.isSQL = false;
      }

      this.menuManager = new MenuManagerFactory().createMenuManager(this);
      this.luckManager = new LuckManager(this);
      if (this.isSQL) {
         this.casesConfig = new CasesConfig(this, this.luckManager, this.menuManager, this.jdbiExecutor);
      } else {
         this.casesConfig = new CasesConfig(this, this.luckManager, this.menuManager);
      }

      this.caseManager = new CaseManager(this, this.casesConfig);
      new CaseCommand(this, this.caseManager, this.luckManager, this.jdbiExecutor);
      this.customArmorStandHandler = new CustomArmorStandHandler(this);
      new GameListener(this, this.caseManager, this.casesConfig);
      this.caseManager.getHolders().forEach(CaseHolder::onReload);
      this.getLogger().info(" ");
      this.getLogger().info(" ");
      this.getLogger().info("                                   ⢱⡙⡞⡺⠶⢶⣤⣤⢶⢶⡷⣳⢷⢶⡦⣤⣀⠀⢀⠀⡀");
      this.getLogger().info("                                   ⢘⠮⣍⡉⣮⣻⢮⣯⣻⡳⣟⣽⣻⣳⡻⣷⢯⣟⡋⠛⠫⣻⢹⡙⡎");
      this.getLogger().info("                                     ⢏⢞⡌⣟⡾⣽⣳⢽⣫⡿⣮⢷⣝⣽⢞⣷⡻⣟⣭⢘⡧⣣⢫⠂");
      this.getLogger().info("                                     ⢸⣞⣟⢮⣿⣳⢯⣻⣵⣟⢾⢟⣎⡷⣟⣽⣝⣟⣾⣠⢵⣱⡁");
      this.getLogger().info("                                   ⢠⢿⡾⣭⢷⢯⡞⣟⣮⡷⣯⠃⣹⢾⣝⣿⢼⣗⣽⢮⣧⣷⢿⣻⠄");
      this.getLogger().info("                                   ⣸⣟⢷⣫⡇⣸⠂⢿⣪⡿⡇⢀⠘⣷⠇⣿⡳⣽⢞⣯⢾⣯⡿⣽⠧");
      this.getLogger().info("                                   ⢼⣿⣝⢷⡋⣻⣦⠹⣳⠇⠂⢠⡠⣿⣤⣿⣝⣯⣻⣳⢿⣺⣯⣻⣻⡀");
      this.getLogger().info(" §bПлагин от студии§r             ⣾⢾⣵⢟⠀⢫⣟⠀⠙⠀⠐⠀⢰⣾⠟⣾⢮⡷⣽⠋⢛⣯⡷⣿⢵⡃");
      this.getLogger().info(" §dFichaDev (t.me/FichaDev)§r     ⣼⢫⢿⣽⣿⠀⡂⠁⠄⡈⢀⠁⠂⠘⠓⢫⣻⢗⣟⢾⠀⠎⣼⣯⢿⣝⡇");
      this.getLogger().info("                                 ⢰⡯⠀⣟⢷⡿⡆⠈⡀⠂⢰⠤⠀⡁⠌⠑⣸⣟⢽⣞⠃⠀⠀⡻⣗⣯⢿⡅");
      this.getLogger().info("                               ⢀⡟⢁⢠⣯⣻⢿⣇⣁⣠⣐⠀⠁⠄⠠⡐⢴⢻⣭⢷⣇⣀⣄⣀⣇⢿⣞⢯⣿");
      this.getLogger().info("                               ⢸⠀⠈⣸⣵⣟⣽⣝⡯⠯⠻⣷⠗⡪⠱⡌⢏⡼⣷⡿⣫⡿⣽⢯⣻⣺⡽⣳⣽⡆");
      this.getLogger().info("                               ⠸⡄⢠⣟⣾⢵⡛⠀⠠⠀⢢⠛⢄⣽⣜⢄⣿⡽⣯⣻⡽⣞⣷⣻⡳⣇⢻⣗⡽⣿⣄");
      this.getLogger().info("                                 ⢳⡜⣷⢿⢍⠠⣄⢁⣖⢼⠢⢣⡟⣼⣻⡳⣟⣽⢾⣝⡷⣽⣞⣽⡏⢾⣳⡻⣮⢿⣷⣄");
      this.getLogger().info("                                   ⡿⣝⡯⠀⠁⢈⡠⠀⢀⠀⠄⠀⡀⢹⣽⣫⣷⣻⢞⣟⢾⡮⣷⡃⠀⠉⣿⡺⡽⣾⣯⠳⡄");
      this.getLogger().info("                                   ⣿⢽⣝⣟⢿⣲⣦⣐⡄⠲⡤⠢⡄⠈⢱⡿⣮⣟⣽⣫⣟⢾⣳⠁⠀⠀⠘⣿⢽⢮⣻⣷⡙⣦⣤⢶⢶⣲⢦⣄⣀");
      this.getLogger().info("                                   ⢻⣗⣟⢾⡯⣷⢽⡽⡇⣴⡇⡱⡀⣠⣾⡻⣞⣮⢷⢯⡾⣯⡻⠀⠀⠀⠀⠈⢻⢷⡵⣻⢿⡕⢫⠉⠁⠉⠙⠹⡽⡶⡄");
      this.getLogger().info("                                   ⠘⡷⣽⣫⡿⣞⣯⣻⡳⢻⣟⣴⣖⢻⡮⣿⢽⣺⢯⣟⣽⢞⠇⠀⠀⠀⠀⠀⠀⣽⢾⣝⡽⣧⢸⠀⠀⠀⠀⠀⠈⢫⣟");
      this.getLogger().info("                                     ⢸⣟⣮⢿⣝⣷⡽⣻⣨⡷⣽⣽⡫⣟⣾⣫⣟⢷⡻⣞⢿⠀⠀⠀⠀⠀⠀⣼⢯⠃⢷⣛⢾⡗⠀⠀⠀⠀⠀⢀⡿⡾");
      this.getLogger().info("                                     ⣼⢷⣽⣳⢟⣮⢿⣽⣝⣿⣺⢞⡷⣻⢮⣷⣫⡿⣝⣯⡏⠀⠀⠀⠀⠀⣸⣯⠃⠀⢸⣯⢳⣧⠀⠀⠀⠀⠀⠈⠛");
      this.getLogger().info("                                     ⣟⣷⣳⢯⡿⣝⡷⣷⢯⡾⣯⣟⣽⢯⡷⣯⢾⣝⣿⣺⡇⠀⠀⠀⠀⠀⣿⡚⠀⠀⠈⡷⡯⣞⡇");
      this.getLogger().info("                                     ⠻⣮⣻⡳⣿⢽⣯⣟⣿⡽⣷⣻⣗⢿⢾⣝⡷⣯⣯⣿⡅⠀⠀⠀⠀⢸⣯⡃⠀⠀⢀⠃⣿⡺⣧");
      this.getLogger().info("                                       ⠹⣾⣫⣷⠟⢱⣿⣳⣿⡼⣷⢿⣝⡿⣾⣝⣿⢾⣞⣟⡇⠀⠀⢀⠿⡚⠀⠀⠀⠸⠀⢱⣟⡮");
      this.getLogger().info("                                         ⠈⠉⠀⠀⢈⣿⢷⣯⣟⡿⣯⣿⣽⡾⣾⢯⡿⣽⣾⣻⣦⢴⢋⢆⠳⡠⠀⠀⠂⠀⢸⣽⠃");
      this.getLogger().info("                                                   ⢸⣯⢿⣾⣝⡻⣷⣻⣞⣿⡽⣿⣽⢷⢯⡷⣽⣻⣴⠡⡣⡱⣩⠆⠀⠀⢸⠎");
      this.getLogger().info("                                                   ⢸⣷⡻⣷⣟⡿⣯⣿⣽⣾⣻⢷⡯⣿⢯⣿⣳⢟⡾⣗⡱⡼⣕⠷");
      this.getLogger().info("                                                   ⣸⣯⡿⣾⣝⡿⡻⣞⣷⢯⣟⣯⢿⣽⣳⢷⡯⣿⢽⣯⣻⡞⡜⡱⠢");
      this.getLogger().info("                                             ⢀⣸⢯⣯⣻⢾⣳⣿⣻⣟⡾⣟⣾⣽⣳⢯⣷⣻⡽⣞⣷⡻⣮⢿⣮⡘⢕⢕⡀⡀");
      this.getLogger().info("                                       ⣀⢖⢛⠝⠷⣫⢯⡿⣵⢯⣟⢾⠽⠻⢞⢮⢟⡷⣯⢾⣝⣿⣺⡽⣯⢷⡯⣳⡕⡱⢏⡇");
      this.getLogger().info("                                 ⢀⡤⡚⠤⡣⣑⢝⢪⠢⡕⣌⠳⡡⡪⢢⠣⣋⢆⢇⢪⠢⡪⣉⠞⡕⡫⡚⣫⡣⡙⣜⡦⡱⡑⡅");
      this.getLogger().info("                           ⢀⠴⠋⠔⠩⢪⡢⡋⢆⡣⡱⠪⠒⠝⠶⢑⢕⡑⣎⢔⢅⢣⠓⡜⣄⢻⠨⡪⠸⡄⡷⢨⢪⢾⢔⠕⡜⡀");
      this.getLogger().info("                           ⠂⠀⠀⠄⠠⠀⠈⠘⠌⠂⠁⡀⠐⠀⠄⠠⠀⠈⠁⠕⠥⣃⠳⡨⡲⡑⡕⡩⡪⢢⣫⢢⢛⢅⡻⣳⢜⠄");
      this.getLogger().info("                           ⢀⠈⡀⠐⠀⠄⢁⠐⢀⠈⠀⠄⠈⠀⠂⠐⠀⡁⠐⡀⠈⢢⠣⡵⢱⡘⡌⢎⢜⢢⢟⡆⡣⣵⣼⢷⣯⣥⣀");
      this.getLogger().info("                         ⠄⠠⠀⠠⠀⢁⠀⠂⠀⠄⢀⠁⠄⢈⢀⠁⠔⠀⠂⠲⣞⢿⢶⡷⡶⡷⣮⢾⣼⣢⣳⣫⣎⢶⡿⣝⣿⡺⣯⣟⣿⣻⢷⣷⣶⣦⣤⡀");
      this.getLogger().info("                       ⠐⠀⠐⠀⠂⠐⠠⠀⡁⠐⠀⠄⠀⠂⠀⠄⠀⠂⡀⢁⠀⠈⣿⡳⣟⣽⡻⣞⢷⡯⣞⡷⣽⣝⣟⣾⣫⡾⣽⢞⣷⢯⣿⣻⣞⣷⣻⣞⣿⣄");
      this.getLogger().info("                     ⡈⢀⠁⡈⢀⠈⠀⠂⠠⠀⡈⠀⠂⢈⠀⠂⡀⠁⠄⠠⠀⠐⠀⢘⣯⣻⢮⡿⣝⣟⢾⣝⣟⡾⠵⠟⠼⠳⠝⠯⠻⣽⣟⣾⣷⣻⢾⡯⣾⣗⣿");
      this.getLogger().info("                   ⠠⡀⠠⠀⠠⠀⠠⣁⢜⠀⠠⠀⠄⢁⠀⠂⠐⢀⠈⡀⡁⣰⣯⡻⠗⠟⠙⠉⠉⠈");
      this.getLogger().info("                     ⠁⠑⠁⠉⠈⠀⠀⠈⠢⠄⠢⢤⠠⡅⠢⠂⠳⠘⠔⠑⠈⠈⠁");
      instance = this;
      new CasePlaceholder(this, this.luckManager, this.caseManager).register();
   }

   public String getKeysForAllCases(Player player) {
      StringBuilder builder = new StringBuilder();
      this.caseManager.getCaseNames().forEach(caseName -> {
         int keys = this.getKeys(player, caseName);
         builder.append(caseName).append(": ").append(keys).append(" ключей\n");
      });
      return builder.toString().trim();
   }

   public void onDisable() {
      for (OfflinePlayer player : this.opens.keySet().stream().toList()) {
         if (player != null) {
            GameCase gameCase = this.opens.get(player);
            if (gameCase != null) {
               Reward winner = gameCase.getRandomReward();
               gameCase.givePrize(player, winner);
            }
         }
      }

      try {
         if (this.customArmorStandHandler != null) {
            this.customArmorStandHandler.onDisable();
         }

         this.caseManager
            .getHolders()
            .forEach(holder -> holder.getCases().forEach(gameCasex -> gameCasex.getLocation().getBlock().removeMetadata("case", this)));
      } catch (Exception var5) {
      }

      this.getLogger().info(" ");
      this.getLogger().info(" ");
      this.getLogger().info("                                   ⢱⡙⡞⡺⠶⢶⣤⣤⢶⢶⡷⣳⢷⢶⡦⣤⣀⠀⢀⠀⡀");
      this.getLogger().info("                                   ⢘⠮⣍⡉⣮⣻⢮⣯⣻⡳⣟⣽⣻⣳⡻⣷⢯⣟⡋⠛⠫⣻⢹⡙⡎");
      this.getLogger().info("                                     ⢏⢞⡌⣟⡾⣽⣳⢽⣫⡿⣮⢷⣝⣽⢞⣷⡻⣟⣭⢘⡧⣣⢫⠂");
      this.getLogger().info("                                     ⢸⣞⣟⢮⣿⣳⢯⣻⣵⣟⢾⢟⣎⡷⣟⣽⣝⣟⣾⣠⢵⣱⡁");
      this.getLogger().info("                                   ⢠⢿⡾⣭⢷⢯⡞⣟⣮⡷⣯⠃⣹⢾⣝⣿⢼⣗⣽⢮⣧⣷⢿⣻⠄");
      this.getLogger().info("                                   ⣸⣟⢷⣫⡇⣸⠂⢿⣪⡿⡇⢀⠘⣷⠇⣿⡳⣽⢞⣯⢾⣯⡿⣽⠧");
      this.getLogger().info("                                   ⢼⣿⣝⢷⡋⣻⣦⠹⣳⠇⠂⢠⡠⣿⣤⣿⣝⣯⣻⣳⢿⣺⣯⣻⣻⡀");
      this.getLogger().info(" §bПлагин от студии§r              ⣾⢾⣵⢟⠀⢫⣟⠀⠙⠀⠐⠀⢰⣾⠟⣾⢮⡷⣽⠋⢛⣯⡷⣿⢵⡃");
      this.getLogger().info(" §dFichaDev (t.me/FichaDev)§r      ⣼⢫⢿⣽⣿⠀⡂⠁⠄⡈⢀⠁⠂⠘⠓⢫⣻⢗⣟⢾⠀⠎⣼⣯⢿⣝⡇");
      this.getLogger().info("                                 ⢰⡯⠀⣟⢷⡿⡆⠈⡀⠂⢰⠤⠀⡁⠌⠑⣸⣟⢽⣞⠃⠀⠀⡻⣗⣯⢿⡅");
      this.getLogger().info("                               ⢀⡟⢁⢠⣯⣻⢿⣇⣁⣠⣐⠀⠁⠄⠠⡐⢴⢻⣭⢷⣇⣀⣄⣀⣇⢿⣞⢯⣿");
      this.getLogger().info("                               ⢸⠀⠈⣸⣵⣟⣽⣝⡯⠯⠻⣷⠗⡪⠱⡌⢏⡼⣷⡿⣫⡿⣽⢯⣻⣺⡽⣳⣽⡆");
      this.getLogger().info("                               ⠸⡄⢠⣟⣾⢵⡛⠀⠠⠀⢢⠛⢄⣽⣜⢄⣿⡽⣯⣻⡽⣞⣷⣻⡳⣇⢻⣗⡽⣿⣄");
      this.getLogger().info("                                 ⢳⡜⣷⢿⢍⠠⣄⢁⣖⢼⠢⢣⡟⣼⣻⡳⣟⣽⢾⣝⡷⣽⣞⣽⡏⢾⣳⡻⣮⢿⣷⣄");
      this.getLogger().info("                                   ⡿⣝⡯⠀⠁⢈⡠⠀⢀⠀⠄⠀⡀⢹⣽⣫⣷⣻⢞⣟⢾⡮⣷⡃⠀⠉⣿⡺⡽⣾⣯⠳⡄");
      this.getLogger().info("                                   ⣿⢽⣝⣟⢿⣲⣦⣐⡄⠲⡤⠢⡄⠈⢱⡿⣮⣟⣽⣫⣟⢾⣳⠁⠀⠀⠘⣿⢽⢮⣻⣷⡙⣦⣤⢶⢶⣲⢦⣄⣀");
      this.getLogger().info("                                   ⢻⣗⣟⢾⡯⣷⢽⡽⡇⣴⡇⡱⡀⣠⣾⡻⣞⣮⢷⢯⡾⣯⡻⠀⠀⠀⠀⠈⢻⢷⡵⣻⢿⡕⢫⠉⠁⠉⠙⠹⡽⡶⡄");
      this.getLogger().info("                                   ⠘⡷⣽⣫⡿⣞⣯⣻⡳⢻⣟⣴⣖⢻⡮⣿⢽⣺⢯⣟⣽⢞⠇⠀⠀⠀⠀⠀⠀⣽⢾⣝⡽⣧⢸⠀⠀⠀⠀⠀⠈⢫⣟");
      this.getLogger().info("                                     ⢸⣟⣮⢿⣝⣷⡽⣻⣨⡷⣽⣽⡫⣟⣾⣫⣟⢷⡻⣞⢿⠀⠀⠀⠀⠀⠀⣼⢯⠃⢷⣛⢾⡗⠀⠀⠀⠀⠀⢀⡿⡾");
      this.getLogger().info("                                     ⣼⢷⣽⣳⢟⣮⢿⣽⣝⣿⣺⢞⡷⣻⢮⣷⣫⡿⣝⣯⡏⠀⠀⠀⠀⠀⣸⣯⠃⠀⢸⣯⢳⣧⠀⠀⠀⠀⠀⠈⠛");
      this.getLogger().info("                                     ⣟⣷⣳⢯⡿⣝⡷⣷⢯⡾⣯⣟⣽⢯⡷⣯⢾⣝⣿⣺⡇⠀⠀⠀⠀⠀⣿⡚⠀⠀⠈⡷⡯⣞⡇");
      this.getLogger().info("                                     ⠻⣮⣻⡳⣿⢽⣯⣟⣿⡽⣷⣻⣗⢿⢾⣝⡷⣯⣯⣿⡅⠀⠀⠀⠀⢸⣯⡃⠀⠀⢀⠃⣿⡺⣧");
      this.getLogger().info("                                       ⠹⣾⣫⣷⠟⢱⣿⣳⣿⡼⣷⢿⣝⡿⣾⣝⣿⢾⣞⣟⡇⠀⠀⢀⠿⡚⠀⠀⠀⠸⠀⢱⣟⡮");
      this.getLogger().info("                                         ⠈⠉⠀⠀⢈⣿⢷⣯⣟⡿⣯⣿⣽⡾⣾⢯⡿⣽⣾⣻⣦⢴⢋⢆⠳⡠⠀⠀⠂⠀⢸⣽⠃");
      this.getLogger().info("                                                   ⢸⣯⢿⣾⣝⡻⣷⣻⣞⣿⡽⣿⣽⢷⢯⡷⣽⣻⣴⠡⡣⡱⣩⠆⠀⠀⢸⠎");
      this.getLogger().info("                                                   ⢸⣷⡻⣷⣟⡿⣯⣿⣽⣾⣻⢷⡯⣿⢯⣿⣳⢟⡾⣗⡱⡼⣕⠷");
      this.getLogger().info("                                                   ⣸⣯⡿⣾⣝⡿⡻⣞⣷⢯⣟⣯⢿⣽⣳⢷⡯⣿⢽⣯⣻⡞⡜⡱⠢");
      this.getLogger().info("                                             ⢀⣸⢯⣯⣻⢾⣳⣿⣻⣟⡾⣟⣾⣽⣳⢯⣷⣻⡽⣞⣷⡻⣮⢿⣮⡘⢕⢕⡀⡀");
      this.getLogger().info("                                       ⣀⢖⢛⠝⠷⣫⢯⡿⣵⢯⣟⢾⠽⠻⢞⢮⢟⡷⣯⢾⣝⣿⣺⡽⣯⢷⡯⣳⡕⡱⢏⡇");
      this.getLogger().info("                                 ⢀⡤⡚⠤⡣⣑⢝⢪⠢⡕⣌⠳⡡⡪⢢⠣⣋⢆⢇⢪⠢⡪⣉⠞⡕⡫⡚⣫⡣⡙⣜⡦⡱⡑⡅");
      this.getLogger().info("                           ⢀⠴⠋⠔⠩⢪⡢⡋⢆⡣⡱⠪⠒⠝⠶⢑⢕⡑⣎⢔⢅⢣⠓⡜⣄⢻⠨⡪⠸⡄⡷⢨⢪⢾⢔⠕⡜⡀");
      this.getLogger().info("                           ⠂⠀⠀⠄⠠⠀⠈⠘⠌⠂⠁⡀⠐⠀⠄⠠⠀⠈⠁⠕⠥⣃⠳⡨⡲⡑⡕⡩⡪⢢⣫⢢⢛⢅⡻⣳⢜⠄");
      this.getLogger().info("                           ⢀⠈⡀⠐⠀⠄⢁⠐⢀⠈⠀⠄⠈⠀⠂⠐⠀⡁⠐⡀⠈⢢⠣⡵⢱⡘⡌⢎⢜⢢⢟⡆⡣⣵⣼⢷⣯⣥⣀");
      this.getLogger().info("                         ⠄⠠⠀⠠⠀⢁⠀⠂⠀⠄⢀⠁⠄⢈⢀⠁⠔⠀⠂⠲⣞⢿⢶⡷⡶⡷⣮⢾⣼⣢⣳⣫⣎⢶⡿⣝⣿⡺⣯⣟⣿⣻⢷⣷⣶⣦⣤⡀");
      this.getLogger().info("                       ⠐⠀⠐⠀⠂⠐⠠⠀⡁⠐⠀⠄⠀⠂⠀⠄⠀⠂⡀⢁⠀⠈⣿⡳⣟⣽⡻⣞⢷⡯⣞⡷⣽⣝⣟⣾⣫⡾⣽⢞⣷⢯⣿⣻⣞⣷⣻⣞⣿⣄");
      this.getLogger().info("                     ⡈⢀⠁⡈⢀⠈⠀⠂⠠⠀⡈⠀⠂⢈⠀⠂⡀⠁⠄⠠⠀⠐⠀⢘⣯⣻⢮⡿⣝⣟⢾⣝⣟⡾⠵⠟⠼⠳⠝⠯⠻⣽⣟⣾⣷⣻⢾⡯⣾⣗⣿");
      this.getLogger().info("                   ⠠⡀⠠⠀⠠⠀⠠⣁⢜⠀⠠⠀⠄⢁⠀⠂⠐⢀⠈⡀⡁⣰⣯⡻⠗⠟⠙⠉⠉⠈");
      this.getLogger().info("                     ⠁⠑⠁⠉⠈⠀⠀⠈⠢⠄⠢⢤⠠⡅⠢⠂⠳⠘⠔⠑⠈⠈⠁");
   }

   public static FichaCases getInstance() {
      return instance;
   }

   private void initConfig() {
      File configFile = new File(this.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         this.saveResource("config.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(configFile);
   }

   private void initDatabaseYaml() {
      File databaseFile = new File(this.getDataFolder(), "database.yml");
      if (!databaseFile.exists()) {
         this.getLogger().info("Файл database.yml не найден");
         this.getDataFolder().mkdirs();

         try {
            databaseFile.createNewFile();
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      }

      this.database = YamlConfiguration.loadConfiguration(databaseFile);
   }

   private void initJDBI(ConnectionInfo info) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://" + info.host() + ":" + info.port() + "/" + info.database() + "?autoReconnect=true&useSSL=false&characterEncoding=utf8");
      config.setUsername(info.user());
      config.setPassword(info.password());

      HikariDataSource dataSource;
      try {
         dataSource = new HikariDataSource(config);
      } catch (Exception var8) {
         throw new RuntimeException(var8);
      }

      this.jdbi = Jdbi.create(dataSource);
      this.jdbi.installPlugin(new CaffeineCachePlugin());
      this.jdbi.installPlugin(new SqlObjectPlugin());

      try (InputStream inputStream = this.getClass().getResourceAsStream("/template.sql")) {
         if (inputStream == null) {
            throw new RuntimeException("SQL template file not found: /template.sql");
         }

         this.jdbi.useHandle(handle -> handle.createScript(new String(inputStream.readAllBytes())).execute());
      } catch (IOException var10) {
         throw new RuntimeException(var10);
      }

      Executor executor = Executors.newFixedThreadPool(8);
      this.jdbiExecutor = JdbiExecutor.create(this.jdbi, executor);
   }

   public void reloadConfig() {
      if (this.isSQL) {
         this.casesConfig = new CasesConfig(this, this.luckManager, this.menuManager, this.jdbiExecutor);
      } else {
         this.casesConfig = new CasesConfig(this, this.luckManager, this.menuManager);
      }

      this.initConfig();
   }

   @Nonnull
   public YamlConfiguration getConfig() {
      return this.config;
   }

   public JdbiExecutor getExecutor() {
      return this.jdbiExecutor;
   }

   public void setItemStack(String caseName, ItemStack itemStack) {
      this.database.set("items." + caseName, itemStack);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public ItemStack getItemStack(String caseName) {
      return !this.database.contains("items." + caseName) ? null : this.database.getItemStack("items." + caseName);
   }

   public void addKeys(OfflinePlayer player, String key, int value) {
      int keys = this.getKeys(player, key);
      this.database.set("keys." + key + "." + player.getUniqueId(), keys + value);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var6) {
         this.getLogger().severe("Не удалось сохранить ключи в database.yml: " + var6.getMessage());
         var6.printStackTrace();
      }
   }

   public void removeKeys(OfflinePlayer player, String key, int value) {
      int keys = this.getKeys(player, key);
      int newValue = Math.max(keys - value, 0);
      this.database.set("keys." + key + "." + player.getUniqueId(), newValue);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var7) {
         throw new RuntimeException(var7);
      }
   }

   public void addLuck(OfflinePlayer player, String key, int value) {
      int lucks = this.getLucks(player, key);
      this.database.set("lucks." + key + "." + player.getUniqueId(), lucks + value);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }
   }

   public void removeLuck(OfflinePlayer player, String key, int value) {
      int lucks = this.getLucks(player, key);
      int newValue = Math.max(lucks - value, 0);
      this.database.set("lucks." + key + "." + player.getUniqueId(), newValue);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var7) {
         throw new RuntimeException(var7);
      }
   }

   public void addPrizeHistory(String opener, String caseName, String luckName, String prizeName, String prizeMaterial, long timestamp, String serverName) {
      UUID uuid = UUID.randomUUID();
      this.database.set("history." + uuid + ".opener", opener);
      this.database.set("history." + uuid + ".caseName", caseName);
      this.database.set("history." + uuid + ".luckName", luckName);
      this.database.set("history." + uuid + ".prizeName", prizeName);
      this.database.set("history." + uuid + ".prizeMaterial", prizeMaterial);
      this.database.set("history." + uuid + ".timestamp", timestamp);
      this.database.set("history." + uuid + ".serverName", serverName);

      try {
         this.database.save(new File(this.getDataFolder(), "database.yml"));
      } catch (IOException var11) {
         throw new RuntimeException(var11);
      }
   }

   public Collection<PrizeHistoryEntry> getLastPrizes(String caseName, int limit) {
      Collection<PrizeHistoryEntry> lastPrizes = new ArrayList<>();
      int amount = 0;
      if (this.database.contains("history")) {
         for (String uuid : this.database.getConfigurationSection("history").getKeys(false)) {
            ConfigurationSection section = this.database.getConfigurationSection("history." + uuid);
            if (Objects.equals(section.getString("caseName"), caseName)) {
               lastPrizes.add(
                  new PrizeHistoryEntry(
                     section.getString("opener"),
                     section.getString("caseName"),
                     section.getString("luckName"),
                     section.getString("prizeName"),
                     section.getString("prizeMaterial"),
                     section.getLong("timestamp"),
                     section.getString("serverName")
                  )
               );
               if (++amount >= limit) {
                  break;
               }
            }
         }
      }

      return lastPrizes;
   }

   public int getKeys(OfflinePlayer player, String key) {
      if (!this.isSQL) {
         if (this.database == null) {
            this.getLogger().severe("Ошибка: database.yml не загружен");
            return 0;
         } else {
            return this.database.getInt("keys." + key + "." + player.getUniqueId(), 0);
         }
      } else {
         return 0;
      }
   }

   public CompletableFuture<Integer> getKeysC(OfflinePlayer player, String key) {
      if (!this.isSQL) {
         if (this.database == null) {
            this.getLogger().severe("Ошибка: database.yml не загружен!");
            return CompletableFuture.completedFuture(0);
         } else {
            int keys = this.database.getInt("keys." + key + "." + player.getUniqueId(), 0);
            return CompletableFuture.completedFuture(keys);
         }
      } else {
         return this.jdbiExecutor.withExtension(PlayerKeysDao.class, dao -> dao.getKeys(player.getUniqueId(), key)).toCompletableFuture().thenApply(count -> {
            try {
               return (Integer)count;
            } catch (NumberFormatException var3x) {
               this.getLogger().warning("Некорректный формат данных в базе: " + count);
               return 0;
            }
         });
      }
   }

   public int getLucks(OfflinePlayer player, String key) {
      return this.database.getInt("lucks." + key + "." + player.getUniqueId(), 0);
   }

   public CasesConfig getCasesConfig() {
      return this.casesConfig;
   }

   public boolean isSQL() {
      return this.isSQL;
   }

   public void addPlayer(Player player, GameCase gameCase) {
      this.opens.put(player, gameCase);
   }

   public void removePlayer(OfflinePlayer player) {
      this.opens.remove(player);
   }
}
