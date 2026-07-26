package stickdev.dimasik.stickhwtnt.tnt;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TntCommand implements CommandExecutor {
   private StickHWTnT plugin;

   public TntCommand(StickHWTnT plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         List<TnTType> typesList = Arrays.asList(TnTType.values());
         String typesString = typesList.get(0).toString();

         for (int i = 1; i < typesList.size(); i++) {
            typesString = typesString + ", " + typesList.get(i);
         }

         sender.sendMessage("Использование: /holytnt give [никнейм] [" + typesString + "] <количество>");
         return true;
      } else {
         if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
               List<TnTType> typesList = Arrays.asList(TnTType.values());
               String typesString = "explosive";

               for (int i = 0; i < typesList.size(); i++) {
                  typesString = typesString + ", " + typesList.get(i);
               }

               sender.sendMessage("Использование: /holytnt give [никнейм] [" + typesString + "] <количество>");
               return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
               sender.sendMessage("Игрок не найден");
               return true;
            }

            if (args[2].equalsIgnoreCase("explosive")) {
               int count = 1;
               if (args.length > 3) {
                  count = Integer.parseInt(args[3]);
               }

               sender.sendMessage("Взрывчатое вещество выдано");
               player.getInventory().addItem(new ItemStack[]{this.plugin.getHandler().getExplosive(count)});
               return true;
            }

            TnTType tntType;
            try {
               tntType = TnTType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException var13) {
               List<TnTType> typesList = Arrays.asList(TnTType.values());
               String typesString = "explosive";

               for (int i = 0; i < typesList.size(); i++) {
                  typesString = typesString + ", " + typesList.get(i);
               }

               sender.sendMessage("Неизвестный тип динамита. Используйте: " + typesString);
               return true;
            }

            int count = 1;
            if (args.length > 3) {
               count = Integer.parseInt(args[3]);
            }

            sender.sendMessage("Динамит выдан");
            player.getInventory().addItem(new ItemStack[]{this.plugin.getHandler().getTnt(count, tntType)});
         } else if (args[0].equalsIgnoreCase("summon")) {
            if (!(sender instanceof Player)) {
               return true;
            }

            if (args.length < 2) {
               sender.sendMessage("Использование: /holytnt summon [тип динамита] [велосити] [время до взрыва]");
               return true;
            }

            Player playerx = (Player)sender;

            try {
               TnTType tntTypex = TnTType.valueOf(args[1].toUpperCase());
               double velocity = 0.0;
               int fuse = 100;

               try {
                  velocity = Double.parseDouble(args[2]);
                  fuse = Integer.parseInt(args[3]);
               } catch (Exception var11) {
               }

               this.plugin.tntListener.spawnTnT(playerx.getLocation(), tntTypex, velocity, fuse);
            } catch (IllegalArgumentException var12) {
               List<TnTType> typesList = Arrays.asList(TnTType.values());
               String typesString = "explosive";

               for (int i = 0; i < typesList.size(); i++) {
                  typesString = typesString + ", " + typesList.get(i);
               }

               sender.sendMessage("Неизвестный тип динамита. Используйте: " + typesString);
               return true;
            }
         } else {
            List<TnTType> typesList = Arrays.asList(TnTType.values());
            String typesString = "explosive";

            for (int i = 0; i < typesList.size(); i++) {
               typesString = typesString + ", " + typesList.get(i);
            }

            sender.sendMessage("Использование: /holytnt give [никнейм] [" + typesString + "] <количество>");
         }

         return true;
      }
   }
}
