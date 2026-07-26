package ru.lolodimo4ka.holygoldspawner.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesManager {
   private final JavaPlugin plugin;
   private File messagesFile;
   private FileConfiguration messages;
   public String mustUseJackhammerMessage;
   public String placementDisabledMessage;
   public String coinReceivedMessage;
   public String protectionNearSpawnerMessage;
   public String spawnerProtectedMessage;
   public String inventoryFullMessage;
   public String noPermissionMessage;
   public String playerNotFoundMessage;
   public String reloadSuccessMessage;
   public String commandUsageGive;
   public String commandUsageGiveSpawner;
   public String commandUsageGivePickaxe;
   public String commandUnknownItem;
   public String commandHelpHeader;
   public String commandHelpGiveSpawner;
   public String commandHelpGivePickaxe;
   public String commandHelpReload;
   public String commandHelpFooter;
   public String spawnerGivenMessage;
   public String jackhammerGivenMessage;
   public String durabilityNumberError;
   public String amountNumberError;
   public String coordsMessageFormat;
   public String economyDisabledMessage;
   public String wrongWorldMessage;

   public MessagesManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.setupMessages();
   }

   private void setupMessages() {
      this.messagesFile = new File(this.plugin.getDataFolder(), "messages.yml");
      if (!this.messagesFile.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messages = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.loadMessages();
   }

   private void loadMessages() {
      this.mustUseJackhammerMessage = this.translateHexColors(
         this.messages.getString("player.must-use-jackhammer", "&cЭтот спавнер можно сломать только специальной киркой Джейка!")
      );
      this.placementDisabledMessage = this.translateHexColors(
         this.messages.getString("player.placement-disabled", "&cУстановка золотых спавнеров временно отключена!")
      );
      this.coinReceivedMessage = this.translateHexColors(this.messages.getString("player.coin-received", "&6&l▶&f Ваш баланс пополнен на &6%coins%"));
      this.protectionNearSpawnerMessage = this.translateHexColors(
         this.messages.getString("player.protection-near-spawner", "&cНельзя устанавливать приват рядом с золотым спавнером!")
      );
      this.spawnerProtectedMessage = this.translateHexColors(
         this.messages.getString("player.spawner-protected", "&cЭта территория защищена золотым спавнером!")
      );
      this.inventoryFullMessage = this.translateHexColors(this.messages.getString("player.inventory-full", "&cИнвентарь полон!"));
      this.noPermissionMessage = this.translateHexColors(this.messages.getString("player.no-permission", "&cУ вас нет прав на эту команду!"));
      this.playerNotFoundMessage = this.translateHexColors(this.messages.getString("player.not-found", "&cИгрок не найден!"));
      this.reloadSuccessMessage = this.translateHexColors(this.messages.getString("player.reload-success", "&aКонфигурация плагина перезагружена!"));
      this.commandUsageGive = this.translateHexColors(
         this.messages.getString("command.usage-give", "&cИспользование: /holygoldspawner give <spawner|pickaxe> [игрок] [параметры]")
      );
      this.commandUsageGiveSpawner = this.translateHexColors(
         this.messages.getString("command.usage-give-spawner", "&cИспользование: /holygoldspawner give spawner <игрок> [прочность] [количество]")
      );
      this.commandUsageGivePickaxe = this.translateHexColors(
         this.messages.getString("command.usage-give-pickaxe", "&cИспользование: /holygoldspawner give pickaxe <игрок> [количество]")
      );
      this.commandUnknownItem = this.translateHexColors(this.messages.getString("command.unknown-item", "&cНеизвестный предмет: %item%"));
      this.commandHelpHeader = this.translateHexColors(this.messages.getString("command.help-header", "&6=== HolyGoldSpawner Помощь ==="));
      this.commandHelpGiveSpawner = this.translateHexColors(
         this.messages.getString("command.help-give-spawner", "&e/holygoldspawner give spawner <игрок> [прочность] [количество]")
      );
      this.commandHelpGivePickaxe = this.translateHexColors(
         this.messages.getString("command.help-give-pickaxe", "&e/holygoldspawner give pickaxe <игрок> [количество]")
      );
      this.commandHelpReload = this.translateHexColors(this.messages.getString("command.help-reload", "&e/holygoldspawner reload §7- перезагрузить конфиг"));
      this.commandHelpFooter = this.translateHexColors(this.messages.getString("command.help-footer", "&6============================="));
      this.spawnerGivenMessage = this.translateHexColors(
         this.messages.getString("give.spawner-given", "&aЗолотой спавнер выдан игроку %player% в количестве %amount% шт.")
      );
      this.jackhammerGivenMessage = this.translateHexColors(
         this.messages.getString("give.jackhammer-given", "&aКирка Джейка выдана игроку %player% в количестве %amount% шт.")
      );
      this.durabilityNumberError = this.translateHexColors(this.messages.getString("give.durability-number-error", "&cПрочность должна быть числом!"));
      this.amountNumberError = this.translateHexColors(this.messages.getString("give.amount-number-error", "&cКоличество должно быть числом!"));
      this.coordsMessageFormat = this.translateHexColors(
         this.messages
            .getString(
               "coords.message-format",
               "&x&F&F&C&F&0&0▶\n&x&F&F&C&F&0&0▶&fНа координатах: &x&F&F&C&F&0&0X: %x_mask%, Y: %y_mask%\n&x&F&F&C&F&0&0▶&fбыл&x&F&F&C&F&0&0 установлен Золотой Спавнер\n&x&F&F&C&F&0&0▶"
            )
      );
      this.economyDisabledMessage = this.translateHexColors(
         this.messages.getString("player.economy-disabled", "&cПлагин Vault не найден! Плагин будет отключен.")
      );
      this.wrongWorldMessage = this.translateHexColors(
         this.messages.getString("player.wrong-world", "&cЗолотые спавнеры можно устанавливать только в мире &eworld&c!")
      );
   }

   public String translateHexColors(String text) {
      return text == null
         ? ""
         : text.replace("&x", "§x")
            .replace("&", "§")
            .replaceAll("§x([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])", "§x$1$2$3$4$5$6");
   }

   public List<String> translateHexColors(List<String> list) {
      if (list == null) {
         return new ArrayList<>();
      } else {
         List<String> translated = new ArrayList<>();

         for (String line : list) {
            translated.add(this.translateHexColors(line));
         }

         return translated;
      }
   }

   public void reloadMessages() {
      this.messages = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.loadMessages();
   }

   public FileConfiguration getMessages() {
      return this.messages;
   }
}
