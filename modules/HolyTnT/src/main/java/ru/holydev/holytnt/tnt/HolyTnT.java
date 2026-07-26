package ru.holydev.holytnt.tnt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.types.DangGrief;

public final class HolyTnT extends JavaPlugin {
   private TntCommand tntCommand;
   public TnTListener tntListener;
   private TnTHandler tntHandler;
   private TnTData tntData;
   private List<NamespacedKey> recipeKeys = new ArrayList<>();
   public HashSet<Location> ices = new HashSet<>();
   private DangGrief dangGrief;

   public HolyTnT() {
   }

   public void onEnable() {
      this.tntCommand = new TntCommand(this);
      this.tntListener = new TnTListener(this);
      this.tntHandler = new TnTHandler(this);
      this.tntData = new TnTData(this);
      Plugin kotikDangPlugin = Bukkit.getPluginManager().getPlugin("KotikDang");
      if (kotikDangPlugin instanceof KotikDang) {
         KotikDang kotikDang = (KotikDang)kotikDangPlugin;
         this.dangGrief = kotikDang.getDangGriefInstance();
         this.getLogger().info("KotikDang подключен: защита сундуков активна.");
      } else {
         this.getLogger().warning("KotikDang не найден! Защита сундуков данжей не будет работать.");
      }

      this.getCommand("holytnt").setExecutor(this.tntCommand);
      this.getServer().getPluginManager().registerEvents(this.tntListener, this);
      this.registerRecipes();
   }

   public TnTHandler getHandler() {
      return this.tntHandler;
   }

   public TnTData getData() {
      return this.tntData;
   }

   public HolyTnT getInstance() {
      return this;
   }

   public DangGrief getDangGrief() {
      return this.dangGrief;
   }

   public void onDisable() {
      this.removeRecipes();
   }

   private void registerRecipes() {
      NamespacedKey tntAKey = new NamespacedKey(this, "tnt_a");
      ShapedRecipe tntARecipe = new ShapedRecipe(tntAKey, this.tntHandler.getTnt(1, TnTType.A));
      tntARecipe.shape(new String[]{"GSG", "TGT", "GSG"});
      tntARecipe.setIngredient('G', Material.GUNPOWDER);
      tntARecipe.setIngredient('T', Material.TNT);
      tntARecipe.setIngredient('S', Material.SAND);
      Bukkit.addRecipe(tntARecipe);
      this.recipeKeys.add(tntAKey);
      NamespacedKey tntBKey = new NamespacedKey(this, "tnt_b");
      ShapedRecipe tntBRecipe = new ShapedRecipe(tntBKey, this.tntHandler.getTnt(1, TnTType.B));
      tntBRecipe.shape(new String[]{"GSG", "TGT", "GSG"});
      tntBRecipe.setIngredient('G', Material.GUNPOWDER);
      tntBRecipe.setIngredient('T', this.tntHandler.getTnt(1, TnTType.A));
      tntBRecipe.setIngredient('S', Material.SAND);
      Bukkit.addRecipe(tntBRecipe);
      this.recipeKeys.add(tntBKey);
      NamespacedKey tntC4Key = new NamespacedKey(this, "tnt_c4");
      ShapedRecipe tntC4Recipe = new ShapedRecipe(tntC4Key, this.tntHandler.getTnt(1, TnTType.C4));
      tntC4Recipe.shape(new String[]{"GSG", "TGT", "GSG"});
      tntC4Recipe.setIngredient('G', Material.GUNPOWDER);
      tntC4Recipe.setIngredient('T', this.tntHandler.getTnt(1, TnTType.B));
      tntC4Recipe.setIngredient('S', Material.SAND);
      Bukkit.addRecipe(tntC4Recipe);
      this.recipeKeys.add(tntC4Key);
      NamespacedKey tntStealerKey = new NamespacedKey(this, "tnt_stealer");
      ShapedRecipe tntStealerRecipe = new ShapedRecipe(tntStealerKey, this.tntHandler.getTnt(1, TnTType.STEALER));
      tntStealerRecipe.shape(new String[]{"GSG", "TGT", "GSG"});
      tntStealerRecipe.setIngredient('G', Material.GUNPOWDER);
      tntStealerRecipe.setIngredient('T', this.tntHandler.getTnt(1, TnTType.C4));
      tntStealerRecipe.setIngredient('S', Material.SAND);
      Bukkit.addRecipe(tntStealerRecipe);
      this.recipeKeys.add(tntStealerKey);
      NamespacedKey tntLuckStealerKey = new NamespacedKey(this, "tnt_luck_stealer");
      ShapedRecipe tntLuckStealerRecipe = new ShapedRecipe(tntLuckStealerKey, this.tntHandler.getTnt(1, TnTType.LUCK_STEALER));
      tntLuckStealerRecipe.shape(new String[]{"GSG", "TGT", "GSG"});
      tntLuckStealerRecipe.setIngredient('G', Material.GUNPOWDER);
      tntLuckStealerRecipe.setIngredient('T', this.tntHandler.getTnt(1, TnTType.STEALER));
      tntLuckStealerRecipe.setIngredient('S', Material.SAND);
      Bukkit.addRecipe(tntLuckStealerRecipe);
      this.recipeKeys.add(tntLuckStealerKey);
      NamespacedKey tntIceKey = new NamespacedKey(this, "tnt_ice");
      ShapedRecipe tntIceRecipe = new ShapedRecipe(tntIceKey, this.tntHandler.getTnt(1, TnTType.ICE));
      tntIceRecipe.shape(new String[]{"TIT", "ITI", "TIT"});
      tntIceRecipe.setIngredient('I', Material.BLUE_ICE);
      tntIceRecipe.setIngredient('T', Material.TNT);
      Bukkit.addRecipe(tntIceRecipe);
      this.recipeKeys.add(tntIceKey);
      NamespacedKey tntB2Key = new NamespacedKey(this, "tnt_b2");
      ShapedRecipe tntB2Recipe = new ShapedRecipe(tntB2Key, this.tntHandler.getTnt(1, TnTType.B2));
      tntB2Recipe.shape(new String[]{"ETE", "TET", "ETE"});
      tntB2Recipe.setIngredient('E', this.tntHandler.getExplosive(1));
      tntB2Recipe.setIngredient('T', this.tntHandler.getTnt(1, TnTType.A));
      Bukkit.addRecipe(tntB2Recipe);
      this.recipeKeys.add(tntB2Key);
      NamespacedKey gunpowderBlockKey = new NamespacedKey(this, "gunpowderBlock");
      ShapedRecipe gunpowderBlockRecipe = new ShapedRecipe(gunpowderBlockKey, this.tntHandler.getExplosive(1));
      gunpowderBlockRecipe.shape(new String[]{"AAA", "AAA", "AAA"});
      gunpowderBlockRecipe.setIngredient('A', Material.GUNPOWDER);
      Bukkit.addRecipe(gunpowderBlockRecipe);
      this.recipeKeys.add(gunpowderBlockKey);
      NamespacedKey gunpowderKey = new NamespacedKey(this, "gunpowder");
      ShapelessRecipe gunpowderRecipe = new ShapelessRecipe(gunpowderKey, new ItemStack(Material.GUNPOWDER, 9));
      gunpowderRecipe.addIngredient(1, this.tntHandler.getExplosive(1));
      Bukkit.addRecipe(gunpowderRecipe);
      this.recipeKeys.add(gunpowderKey);
   }

   private void removeRecipes() {
      for (NamespacedKey key : this.recipeKeys) {
         Bukkit.removeRecipe(key);
      }

      this.recipeKeys.clear();
   }
}
