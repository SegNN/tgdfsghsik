package stickdev.siynes.stickhwcustom.items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import stickdev.siynes.stickhwcustom.StickHWCustom;
import stickdev.siynes.stickhwcustom.manager.ConfigManager;
import stickdev.siynes.stickhwcustom.utils.HexUtil;

public class MenuItems {
   public MenuItems() {
   }

   public ItemStack nickcolor() {
      ItemStack nickcolor = new ItemStack(Material.RED_DYE);
      ItemMeta itemMeta = nickcolor.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#FF0000 ❏ Цвет никнейма ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FF0000&n▍&f В данном разделе можно настроить"));
      lore.add(HexUtil.translate(" &#FF0000▍&f цвет вашего &6ᴘʀᴇᴍɪᴜᴍ-градиента&f."));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FF0000▶&f Нажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      nickcolor.setItemMeta(itemMeta);
      return nickcolor;
   }

   public ItemStack sounds() {
      ItemStack sounds = new ItemStack(Material.MUSIC_DISC_STRAD);
      ItemMeta itemMeta = sounds.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#FF7000 ❏ Звуки ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FF7000&n▍&f В данном разделе можно настроить"));
      lore.add(HexUtil.translate(" &#FF7000&n▍&f проигрывание звуков при совершении"));
      lore.add(HexUtil.translate(" &#FF7000▍&f вами определённых действий"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FF7000▶&f Нажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      sounds.setItemMeta(itemMeta);
      return sounds;
   }

   public ItemStack particle() {
      ItemStack particle = new ItemStack(Material.MELON_SEEDS);
      ItemMeta itemMeta = particle.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Частицы ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f В данном разделе можно настроить"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f отображение частиц при совершении"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f вами определённых действий"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      particle.setItemMeta(itemMeta);
      return particle;
   }

   public ItemStack premka() {
      ItemStack premka = new ItemStack(Material.END_CRYSTAL);
      ItemMeta itemMeta = premka.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Премиум ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f В данном разделе можно настроить"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f отображение при совершении"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f вами определённых действий"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      premka.setItemMeta(itemMeta);
      return premka;
   }

   public ItemStack blue() {
      ItemStack blue = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
      ItemMeta itemMeta = blue.getItemMeta();
      itemMeta.setDisplayName(" ");
      blue.setItemMeta(itemMeta);
      return blue;
   }

   public ItemStack orange() {
      ItemStack orange = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
      ItemMeta itemMeta = orange.getItemMeta();
      itemMeta.setDisplayName(" ");
      orange.setItemMeta(itemMeta);
      return orange;
   }

   public ItemStack gray() {
      ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta itemMeta = gray.getItemMeta();
      itemMeta.setDisplayName(" ");
      gray.setItemMeta(itemMeta);
      return gray;
   }

   public ItemStack green(double volume, Player player) {
      ItemStack gray = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
      ItemMeta itemMeta = gray.getItemMeta();
      itemMeta.setDisplayName(" ");
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Высота звука: " + String.format("%.1f", volume)));
      lore.add(HexUtil.translate(" &#00D8FF▍&f"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&l▶&f Нажмите, чтобы изменить высоту звука."));
      itemMeta.setLore(lore);
      StickHWCustom.getInstance();
      String pitchValue = StickHWCustom.getPlayerData(player, "sound_pitch");
      if (!pitchValue.equalsIgnoreCase("null")) {
         try {
            double playerVolume = Double.parseDouble(pitchValue);
            if (Math.abs(playerVolume - volume) < 1.0E-4) {
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            }
         } catch (NumberFormatException var10) {
         }
      }

      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "double"), PersistentDataType.DOUBLE, volume);
      gray.setItemMeta(itemMeta);
      return gray;
   }

   public ItemStack saveSound() {
      ItemStack gray = new ItemStack(Material.GREEN_CONCRETE);
      ItemMeta itemMeta = gray.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#05FB00▶ Сохранить"));
      gray.setItemMeta(itemMeta);
      return gray;
   }

   public ItemStack particleItem(String particleName, String displayName, Material material) {
      ConfigManager configManager = new ConfigManager(StickHWCustom.getInstance());
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(HexUtil.translate(configManager.getString("items.particle.name", " &6{name}").replace("{name}", displayName)));
      List<String> lore = configManager.getStringList("items.particle.lore");
      List<String> translatedLore = new ArrayList<>();

      for (String line : lore) {
         translatedLore.add(HexUtil.translate(line));
      }

      meta.setLore(translatedLore);
      item.setItemMeta(meta);
      return item;
   }

   public ItemStack soundItem(Player player, String cathegory, String soundName, String displayName, Material material) {
      ConfigManager configManager = new ConfigManager(StickHWCustom.getInstance());
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(HexUtil.translate(configManager.getString("items.sound.name", " &x&f&f&7&0&0&0{name}").replace("{name}", displayName)));
      List<String> translatedLore = new ArrayList<>();
      StickHWCustom.getInstance();
      if (StickHWCustom.getPlayerData(player, cathegory).equalsIgnoreCase(soundName)) {
         for (String line : configManager.getStringList("items.sound.lore")) {
            translatedLore.add(HexUtil.translate(line));
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
      } else {
         for (String line : configManager.getStringList("items.sound.lore2")) {
            translatedLore.add(HexUtil.translate(line));
         }
      }

      meta.setLore(translatedLore);
      item.setItemMeta(meta);
      return item;
   }

   public ItemStack banner_crit() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Критический удар ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - нанесение критического удара"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы настроить действие."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner_kill() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Убийство игрока ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - убийство другого игрока"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner_lowheal() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Остаётся мало здоровья ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - низкий уровень (развития) здоровья ( < 2.5 )"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner_death_me() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ При вашей смерти ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - смерть игрока"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶&f Нажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Телепортация жемчугом края ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - телепортация жемчугом края"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶ &fНажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner1() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ Убийство игрока ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - убийство другого игрока"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶ &fНажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack banner2() {
      ItemStack banner = new ItemStack(Material.CREEPER_BANNER_PATTERN);
      ItemMeta itemMeta = banner.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#00D8FF ❏ При вашей смерти ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF&n▍&f Действие при сробатывании события:"));
      lore.add(HexUtil.translate(" &#00D8FF▍&f - смерть игрока"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#00D8FF▶ &fНажмите, чтобы настроить действия."));
      itemMeta.setLore(lore);
      banner.setItemMeta(itemMeta);
      return banner;
   }

   public ItemStack star() {
      ItemStack star = new ItemStack(Material.NETHER_STAR);
      ItemMeta itemMeta = star.getItemMeta();
      itemMeta.setDisplayName(HexUtil.translate("&#FFC900 ❏ Звезда статуса ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FFC900&n▍&f В данном разделе можно настроить"));
      lore.add(HexUtil.translate(" &#FFC900▍&f возможности звезды статуса"));
      lore.add(HexUtil.translate(" &f"));
      lore.add(HexUtil.translate(" &#FFC900▶ &fНажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      star.setItemMeta(itemMeta);
      return star;
   }

   public ItemStack getNameItem(Material material, String color, String color2, int level, String playerName) {
      ItemStack shulker = new ItemStack(material);
      ItemMeta itemMeta = shulker.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + convertInt(level) + " Звезда статуса (" + level + " ур.)"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &n▍" + color + " Предпросмотр:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " ▍&r " + this.colorizeNickname(playerName, color2, "#FFFFFF", true)));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &l▶ &fНажмите, чтобы выбрать цвет &6&l(ᴘʀᴇᴍɪᴜᴍ)&f."));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "color"), PersistentDataType.STRING, color2);
      shulker.setItemMeta(itemMeta);
      return shulker;
   }

   public static String convertInt(int i) {
      if (i >= 1 && i <= 3999) {
         int[] arabicValues = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] romanSymbols = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for (int j = 0; j < arabicValues.length; j++) {
            while (i >= arabicValues[j]) {
               result.append(romanSymbols[j]);
               i -= arabicValues[j];
            }
         }

         return result.toString();
      } else {
         throw new IllegalArgumentException("Число должно быть в диапазоне от 1 до 3999");
      }
   }

   private String colorizeNickname(String playerName, String color1, String color2, Boolean bold) {
      int length = playerName.length();
      net.md_5.bungee.api.ChatColor startColor = net.md_5.bungee.api.ChatColor.of(color1);
      net.md_5.bungee.api.ChatColor endColor = net.md_5.bungee.api.ChatColor.of(color2);
      TextComponent coloredNickname = new TextComponent();

      for (int i = 0; i < length; i++) {
         double ratio = (double)i / (length - 1);
         int red = (int)(startColor.getColor().getRed() * (1.0 - ratio) + endColor.getColor().getRed() * ratio);
         int green = (int)(startColor.getColor().getGreen() * (1.0 - ratio) + endColor.getColor().getGreen() * ratio);
         int blue = (int)(startColor.getColor().getBlue() * (1.0 - ratio) + endColor.getColor().getBlue() * ratio);
         net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(new Color(red, green, blue));
         TextComponent letter = new TextComponent(String.valueOf(playerName.charAt(i)));
         letter.setColor(color);
         if (bold) {
            letter.setBold(true);
         }

         coloredNickname.addExtra(letter);
      }

      return TextComponent.toLegacyText(new BaseComponent[]{coloredNickname});
   }

   public ItemStack back() {
      ItemStack dragon = new ItemStack(Material.ARROW);
      ItemMeta itemMeta = dragon.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&F&F&7&0&0&0◀ Назад"));
      dragon.setItemMeta(itemMeta);
      return dragon;
   }

   public ItemStack traps() {
      ItemStack dye = new ItemStack(Material.POPPED_CHORUS_FRUIT);
      ItemMeta itemMeta = dye.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&f&d&4 ❏ Скины на пиротехнику ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&f&d&4 &n▍&f В данном разделе можно настроить скины"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&f&d&4 ▍&f для различной пиротехники (трапки, станы)."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&f&d&4 &l▶ &fНажмите, чтобы открыть раздел."));
      itemMeta.setLore(lore);
      dye.setItemMeta(itemMeta);
      return dye;
   }

   public ItemStack getTrapSkin(Player player, Material material, String skinName, String color, Boolean premium, String tag) {
      ItemStack dye = new ItemStack(material);
      ItemMeta itemMeta = dye.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + "❏ Скин: " + skinName + " ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &n▍&f " + color + "Цель:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " ▍&f &fТрапки"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &l▶ &fНажмите, чтобы выбрать скин." + (premium ? " &6&l(ᴘʀᴇᴍɪᴜᴍ)" : "")));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "trap-type"), PersistentDataType.STRING, tag);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "premium"), PersistentDataType.STRING, premium.toString());
      if (StickHWCustom.playerData.get(player).getOrDefault("trap_skin", "none").equalsIgnoreCase(tag)) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
      }

      dye.setItemMeta(itemMeta);
      return dye;
   }

   public ItemStack getStanSkin(Player player, Material material, String skinName, String color, Boolean premium, String tag) {
      ItemStack dye = new ItemStack(material);
      ItemMeta itemMeta = dye.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + "❏ " + skinName + " цвет ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &n▍&f " + color + "Цель:"));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " ▍&f &fСтаны"));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', color + " &l▶ &fНажмите, чтобы выбрать скин." + (premium ? " &6&l(ᴘʀᴇᴍɪᴜᴍ)" : "")));
      itemMeta.setLore(lore);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "stan-type"), PersistentDataType.STRING, tag);
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(StickHWCustom.getInstance(), "premium"), PersistentDataType.STRING, premium.toString());
      if (StickHWCustom.playerData.get(player).getOrDefault("stan_skin", "none").equalsIgnoreCase(tag)) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
      }

      dye.setItemMeta(itemMeta);
      return dye;
   }

   public ItemStack getStarHelp() {
      ItemStack itemStack = new ItemStack(Material.BOOK);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&FПомощь по системе звёзд статуса"));
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Что такое Звёзды Статуса?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f "));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f Звёзды статуса - префикс перед привилегией,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f который определяет статусность игрока в Табе."));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f "));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Как получить звезду статуса?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f "));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f Звёзды статуса выдаются автоматически,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f в зависимости от того, сколько вы пожертвовали"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f средств за все время существования Лайт анархии."));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f "));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F Какие преимущества у звёзд статуса?"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f "));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f Звезды придают статусности вашему никнейму,"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l&n▍&f а также прибавляют количество голосов за ивент"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&0&D&8&F&F&l▍&f за который вы проголосовали (через /vote)."));
      lore.add(ChatColor.translateAlternateColorCodes('&', ""));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }
}
