package ua.fichadev.fichaautocraft.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import ua.fichadev.fichaautocraft.FichaAutoCraft;

public class CraftingService {
   public CraftingService() {
   }

   public static void executeCrafting(Block block) {
      Dispenser dispenser = (Dispenser)block.getState();
      Block inputContainer = getAdjacentContainer(block, true);
      Block outputContainer = getAdjacentContainer(block, false);
      if (inputContainer != null && outputContainer != null) {
         Inventory matrixInv = dispenser.getInventory();
         Inventory sourceInv = ((Container)inputContainer.getState()).getInventory();
         Inventory targetInv = ((Container)outputContainer.getState()).getInventory();
         if (hasValidPattern(matrixInv)) {
            ItemStack[] pattern = extractPattern(matrixInv);
            ItemStack craftResult = CraftingService.RecipeResolver.findResult(pattern);
            if (craftResult != null) {
               if (CraftingService.MaterialChecker.canCraft(pattern, sourceInv)) {
                  ItemStack output = craftResult.clone();
                  output.setAmount(1);
                  HashMap<Integer, ItemStack> overflow = targetInv.addItem(new ItemStack[]{output});
                  if (overflow.isEmpty()) {
                     CraftingService.MaterialConsumer.consume(pattern, sourceInv);
                  }
               }
            }
         }
      }
   }

   private static Block getAdjacentContainer(Block dispenser, boolean opposite) {
      if (!(dispenser.getBlockData() instanceof Directional)) {
         return null;
      } else {
         Directional dir = (Directional)dispenser.getBlockData();
         BlockFace face = opposite ? dir.getFacing().getOppositeFace() : dir.getFacing();
         Block adjacent = dispenser.getRelative(face);
         if (!(adjacent.getState() instanceof Container)) {
            return null;
         } else {
            Set<Material> allowed = FichaAutoCraft.getInstance().getConfigManager().getAllowedContainers();
            return allowed.contains(adjacent.getType()) ? adjacent : null;
         }
      }
   }

   private static boolean hasValidPattern(Inventory inv) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = inv.getItem(i);
         if (stack != null && stack.getType() != Material.AIR) {
            return true;
         }
      }

      return false;
   }

   private static ItemStack[] extractPattern(Inventory inv) {
      ItemStack[] pattern = new ItemStack[9];

      for (int i = 0; i < 9; i++) {
         pattern[i] = inv.getItem(i);
      }

      return pattern;
   }

   private static class MaterialChecker {
      private MaterialChecker() {
      }

      public static boolean canCraft(ItemStack[] pattern, Inventory source) {
         Map<Material, Integer> required = new HashMap<>();

         for (ItemStack stack : pattern) {
            if (stack != null && stack.getType() != Material.AIR) {
               required.merge(stack.getType(), 1, Integer::sum);
            }
         }

         if (required.isEmpty()) {
            return false;
         } else {
            Map<Material, Integer> available = new HashMap<>();

            for (ItemStack stackx : source.getContents()) {
               if (stackx != null && stackx.getType() != Material.AIR) {
                  available.merge(stackx.getType(), stackx.getAmount(), Integer::sum);
               }
            }

            for (Entry<Material, Integer> entry : required.entrySet()) {
               int need = entry.getValue();
               int have = available.getOrDefault(entry.getKey(), 0);
               if (have < need) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static class MaterialConsumer {
      private MaterialConsumer() {
      }

      public static void consume(ItemStack[] pattern, Inventory source) {
         for (ItemStack required : pattern) {
            if (required != null && required.getType() != Material.AIR) {
               Material mat = required.getType();
               int left = 1;

               for (int slot = 0; slot < source.getSize() && left > 0; slot++) {
                  ItemStack current = source.getItem(slot);
                  if (current != null && current.getType() == mat) {
                     int take = Math.min(current.getAmount(), left);
                     current.setAmount(current.getAmount() - take);
                     left -= take;
                     if (current.getAmount() <= 0) {
                        source.setItem(slot, null);
                     } else {
                        source.setItem(slot, current);
                     }
                  }
               }
            }
         }
      }
   }

   private static class RecipeResolver {
      private RecipeResolver() {
      }

      public static ItemStack findResult(ItemStack[] pattern) {
         Iterator<Recipe> recipes = Bukkit.recipeIterator();

         while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (recipe instanceof ShapedRecipe) {
               if (matchesShaped((ShapedRecipe)recipe, pattern)) {
                  return recipe.getResult();
               }
            } else if (recipe instanceof ShapelessRecipe && matchesShapeless((ShapelessRecipe)recipe, pattern)) {
               return recipe.getResult();
            }
         }

         return null;
      }

      private static boolean matchesShaped(ShapedRecipe recipe, ItemStack[] grid) {
         try {
            String[] shape = recipe.getShape();
            int w = shape[0].length();
            int h = shape.length;

            for (int xOff = 0; xOff <= 3 - w; xOff++) {
               for (int yOff = 0; yOff <= 3 - h; yOff++) {
                  if (testShapedAt(recipe, grid, xOff, yOff, false)) {
                     return true;
                  }

                  if (testShapedAt(recipe, grid, xOff, yOff, true)) {
                     return true;
                  }
               }
            }
         } catch (Exception var7) {
         }

         return false;
      }

      private static boolean testShapedAt(ShapedRecipe recipe, ItemStack[] grid, int xOff, int yOff, boolean mirror) {
         String[] shape = recipe.getShape();
         Map<Character, RecipeChoice> choices = recipe.getChoiceMap();

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               int rX = x - xOff;
               int rY = y - yOff;
               ItemStack gridSlot = grid[y * 3 + x];
               if (rY >= 0 && rY < shape.length && rX >= 0 && rX < shape[rY].length()) {
                  char symbol = shape[rY].charAt(mirror ? shape[rY].length() - 1 - rX : rX);
                  RecipeChoice choice = choices.get(symbol);
                  if (choice != null) {
                     if (gridSlot == null || !choice.test(gridSlot)) {
                        return false;
                     }
                  } else if (gridSlot != null) {
                     return false;
                  }
               } else if (gridSlot != null) {
                  return false;
               }
            }
         }

         return true;
      }

      private static boolean matchesShapeless(ShapelessRecipe recipe, ItemStack[] grid) {
         try {
            List<RecipeChoice> required = new ArrayList<>(recipe.getChoiceList());
            List<ItemStack> provided = new ArrayList<>();

            for (ItemStack stack : grid) {
               if (stack != null) {
                  provided.add(stack);
               }
            }

            if (required.size() != provided.size()) {
               return false;
            } else {
               for (RecipeChoice choice : required) {
                  boolean matched = false;
                  Iterator<ItemStack> it = provided.iterator();

                  while (it.hasNext()) {
                     ItemStack stackx = it.next();
                     if (choice.test(stackx)) {
                        it.remove();
                        matched = true;
                        break;
                     }
                  }

                  if (!matched) {
                     return false;
                  }
               }

               return provided.isEmpty();
            }
         } catch (Exception var9) {
            return false;
         }
      }
   }
}
