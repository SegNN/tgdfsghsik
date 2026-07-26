package ua.fichatime.fichaspawneroved.Utils;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.fichatime.fichaspawneroved.FichaSpawneroved;

public class RandomEggs {
   private final FichaSpawneroved plugin;
   private final Random random = new Random();

   public RandomEggs(FichaSpawneroved plugin) {
      this.plugin = plugin;
   }

   public void executeRandomCommand(String category, Player player) {
      List<String> commands = this.plugin.getConfig().getStringList("spawn-eggs." + category);
      if (!commands.isEmpty()) {
         String randomCmd = commands.get(this.random.nextInt(commands.size()));
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.replace("%player", player.getName()));
      }
   }
}
