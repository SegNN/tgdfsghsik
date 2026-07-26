package ru.lolodimo4ka.holygoldspawner.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holygoldspawner.HolyGoldSpawner;
import ru.lolodimo4ka.holygoldspawner.utils.Item;

public class SpawnerCraft implements Listener {
   private final JavaPlugin plugin;
   private final Config configManager;
   private final Item itemManager;
   private final NamespacedKey goldSpawnerRecipeKey;
   private final NamespacedKey reverseRecipeKey;
   private final NamespacedKey jakePickaxeRecipeKey;

   public SpawnerCraft(JavaPlugin plugin, Config configManager, Item itemManager) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.itemManager = itemManager;
      this.goldSpawnerRecipeKey = new NamespacedKey(plugin, "gold_spawner_craft");
      this.reverseRecipeKey = new NamespacedKey(plugin, "gold_spawner_reverse");
      this.jakePickaxeRecipeKey = new NamespacedKey(plugin, "jake_pickaxe_craft");
      this.registerRecipes();
   }

   private void registerRecipes() {
      Bukkit.removeRecipe(this.goldSpawnerRecipeKey);
      Bukkit.removeRecipe(this.reverseRecipeKey);
      Bukkit.removeRecipe(this.jakePickaxeRecipeKey);
      this.registerGoldSpawnerRecipe();
      this.registerReverseRecipe();
      this.registerJakePickaxeRecipe();
   }

   private void registerGoldSpawnerRecipe() {
      try {
         ItemStack result = this.itemManager.createGoldenSpawner(this.configManager.maxDurability);
         ShapedRecipe recipe = new ShapedRecipe(this.goldSpawnerRecipeKey, result);
         recipe.shape(new String[]{"SSS", "SNS", "SSS"});
         recipe.setIngredient('S', Material.SPAWNER);
         recipe.setIngredient('N', Material.NETHER_STAR);
         Bukkit.addRecipe(recipe);
      } catch (Exception var3) {
      }
   }

   private void registerReverseRecipe() {
      try {
         ItemStack result = new ItemStack(Material.SPAWNER, 8);
         ShapedRecipe recipe = new ShapedRecipe(this.reverseRecipeKey, result);
         recipe.shape(new String[]{"G"});
         recipe.setIngredient('G', Material.SPAWNER);
         Bukkit.addRecipe(recipe);
      } catch (Exception var3) {
      }
   }

   private void registerJakePickaxeRecipe() {
      try {
         HolyGoldSpawner mainPlugin = (HolyGoldSpawner)this.plugin;
         if (!mainPlugin.isCustomEggEnabled()) {
            this.plugin.getLogger().info("HolyCustomEgg не подключен, крафт кирки Джейка не зарегистрирован");
            return;
         }

         ItemStack result = mainPlugin.getCustomEggPlugin().getJakePickaxe();
         ShapedRecipe recipe = new ShapedRecipe(this.jakePickaxeRecipeKey, result);
         recipe.shape(new String[]{"ABA", "BCB", "ABA"});
         recipe.setIngredient('A', Material.REDSTONE_BLOCK);
         recipe.setIngredient('C', Material.GOLDEN_PICKAXE);
         recipe.setIngredient('B', Material.TNT);
         Bukkit.addRecipe(recipe);
      } catch (Exception var4) {
         this.plugin.getLogger().warning("Не удалось зарегистрировать рецепт кирки Джейка: " + var4.getMessage());
      }
   }

   @EventHandler
   public void onPrepareCraft(PrepareItemCraftEvent event) {
      CraftingInventory inventory = event.getInventory();
      Recipe recipe = event.getRecipe();
      if (recipe != null) {
         if (recipe.getResult().getType() == Material.SPAWNER && recipe.getResult().getAmount() == 8) {
            boolean hasGoldenSpawner = false;

            for (ItemStack item : inventory.getMatrix()) {
               if (item != null && this.itemManager.isGoldenSpawner(item)) {
                  hasGoldenSpawner = true;
                  break;
               }
            }

            if (!hasGoldenSpawner) {
               inventory.setResult(null);
            }
         }

         HolyGoldSpawner mainPlugin = (HolyGoldSpawner)this.plugin;
         if (mainPlugin.isCustomEggEnabled()) {
            try {
               ItemStack recipeResult = recipe.getResult();
               ItemStack jakePickaxe = mainPlugin.getCustomEggPlugin().getJakePickaxe();
               if (recipeResult != null
                  && jakePickaxe != null
                  && recipeResult.getType() == jakePickaxe.getType()
                  && recipeResult.hasItemMeta()
                  && jakePickaxe.hasItemMeta()
                  && recipeResult.getItemMeta().getDisplayName().equals(jakePickaxe.getItemMeta().getDisplayName())) {
                  ItemStack[] matrix = inventory.getMatrix();
                  int validTntCount = 0;

                  for (ItemStack itemx : matrix) {
                     if (itemx != null && itemx.getType() == Material.TNT && itemx.hasItemMeta()) {
                        try {
                           NamespacedKey tntKey = new NamespacedKey("holytnt", "tnt");
                           if (itemx.getItemMeta().getPersistentDataContainer().has(tntKey, PersistentDataType.STRING)) {
                              String tntType = (String)itemx.getItemMeta().getPersistentDataContainer().get(tntKey, PersistentDataType.STRING);
                              if ("stealer".equals(tntType)) {
                                 validTntCount++;
                              }
                           }
                        } catch (Exception var15) {
                        }
                     }
                  }

                  if (validTntCount != 4) {
                     inventory.setResult(null);
                  }
               }
            } catch (Exception var16) {
            }
         }
      }
   }

   public void reloadRecipes() {
      this.registerRecipes();
   }
}
