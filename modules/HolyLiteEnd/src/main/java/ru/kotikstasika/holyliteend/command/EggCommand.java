package ru.kotikstasika.holyliteend.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.utils.ColorUtils;

public class EggCommand implements CommandExecutor, TabCompleter {
   private final HolyLiteEnd plugin;
   private HashMap<Player, Boolean> players = new HashMap<>();

   public EggCommand(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ColorUtils.translateColors("Эта команда только для игроков"));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length == 0) {
            this.sendHelp(player);
            return true;
         } else {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
               case "swap":
                  if (this.players.containsKey(player)) {
                     this.handleSwapConfirm(player);
                  } else {
                     this.handleSwap(player);
                  }
                  break;
               case "price":
                  this.handlePrice(player);
                  break;
               default:
                  this.sendHelp(player);
            }

            return true;
         }
      }
   }

   private void handleSwap(Player player) {
      int eggCount = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && EggItem.isDragonEgg(item)) {
            eggCount += item.getAmount();
         }
      }

      if (eggCount == 0) {
         player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fУ вас нет яиц дракона в инвентаре!"));
      } else {
         player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fВы уверены? Повторите комманду, если хотите обменять яйцо."));
         this.players.put(player, true);
         this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.players.remove(player), 600L);
      }
   }

   private void handleSwapConfirm(Player player) {
      int totalValue = 0;
      int eggCount = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (item != null && EggItem.isDragonEgg(item)) {
            totalValue += EggItem.getEggValue(item) * item.getAmount();
            eggCount += item.getAmount();
         }
      }

      for (ItemStack itemx : player.getInventory().getArmorContents()) {
         if (itemx != null && EggItem.isDragonEgg(itemx)) {
            totalValue += EggItem.getEggValue(itemx) * itemx.getAmount();
            eggCount += itemx.getAmount();
         }
      }

      ItemStack offHand = player.getInventory().getItemInOffHand();
      if (offHand != null && EggItem.isDragonEgg(offHand)) {
         totalValue += EggItem.getEggValue(offHand) * offHand.getAmount();
         eggCount += offHand.getAmount();
      }

      if (eggCount == 0) {
         player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fУ вас нет яиц дракона в инвентаре!"));
      } else {
         for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack armorItem = player.getInventory().getItem(i);
            if (armorItem != null && EggItem.isDragonEgg(armorItem)) {
               player.getInventory().setItem(i, (ItemStack)null);
            }
         }

         for (int ix = 0; ix < player.getInventory().getArmorContents().length; ix++) {
            ItemStack armorItem = player.getInventory().getArmorContents()[ix];
            if (armorItem != null && EggItem.isDragonEgg(armorItem)) {
               ItemStack[] armor = player.getInventory().getArmorContents();
               armor[ix] = null;
               player.getInventory().setArmorContents(armor);
            }
         }

         if (offHand != null && EggItem.isDragonEgg(offHand)) {
            player.getInventory().setItemInOffHand((ItemStack)null);
         }

         this.plugin
            .getServer()
            .broadcastMessage(
               ColorUtils.translateColors(
                  "&x&9&B&3&0&F&F▶ "
                     + player.getName()
                     + " &fобменял &x&9&B&3&0&F&F"
                     + eggCount
                     + " яйцо дракона &fна &x&9&B&3&0&F&F"
                     + totalValue
                     + " сапфиров ☀"
               )
            );
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playerpoints:points give " + player.getName() + " " + totalValue);
         this.players.remove(player);

         for (Player all : Bukkit.getOnlinePlayers()) {
            all.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0F, 1.0F);
         }

         this.plugin.getEggBossBarManager().checkPlayerForEgg(player);
      }
   }

   private void handlePrice(Player player) {
      ItemStack item = player.getInventory().getItemInMainHand();
      if (!EggItem.isDragonEgg(item)) {
         player.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0▶ &fВозьмите яйцо дракона в руку для проверки стоимости!"));
      } else {
         int value = EggItem.getEggValue(item);
         player.sendMessage(ColorUtils.translateColors("&x&5&7&x&f&b&0&0&0&0&D&F&3▶ &fСтоимость яйца дракона: &x&9&A&F&B&6&2" + value));
      }
   }

   private void sendHelp(Player player) {
      player.sendMessage(ColorUtils.translateColors("&x&5&7&x&f&b&0&0&0&0&D&F&3▶ Комманды:"));
      player.sendMessage(ColorUtils.translateColors("&f  &x&5&7&x&f&b&0&0&0&0&D&F&3/egg swap&f - Обменять все яйца на сапфиры"));
      player.sendMessage(ColorUtils.translateColors("&f  &x&5&7&x&f&b&0&0&0&0&D&F&3/egg price&f - Проверить стоимость яйца в руке"));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("swap");
         completions.add("price");
      }

      return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
   }
}
