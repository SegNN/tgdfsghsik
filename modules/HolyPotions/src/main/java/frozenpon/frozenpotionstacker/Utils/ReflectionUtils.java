package frozenpon.frozenpotionstacker.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ReflectionUtils {
   private static String version = null;
   private static boolean post1dot17 = false;
   private static boolean post1dot18 = false;
   private static boolean post1dot18dot2 = false;
   private static boolean post1dot19 = false;
   private static boolean modern = false;
   private static Class<?> itemClass = null;
   private static Class<?> itemsClass = null;
   private static Method itemGetNameMethod = null;
   private static Class<?> craftPlayerClass = null;
   private static Class<?> entityPlayerClass = null;
   private static Method getHandleMethod = null;
   private static Class<?> containerInterface = null;
   private static Method updateInventoryMethod = null;
   private static Field activeInventoryField = null;

   private ReflectionUtils() {
   }

   public static boolean setForClass(Class<?> clazz, Object instance, String fieldName, int value) {
      Field curField;
      try {
         curField = clazz.getDeclaredField(fieldName);
      } catch (SecurityException | NoSuchFieldException var7) {
         var7.printStackTrace();
         return false;
      }

      curField.setAccessible(true);

      try {
         curField.set(instance, value);
      } catch (IllegalAccessException | IllegalArgumentException var6) {
         var6.printStackTrace();
         return false;
      }

      curField.setAccessible(false);
      return true;
   }

   public static boolean setFor(String materialName, int stack) {
      Material potion;
      try {
         potion = Material.valueOf(materialName);
      } catch (IllegalArgumentException var4) {
         return false;
      }

      return setForClass(Material.class, potion, "maxStack", stack);
   }

   private static void initiateNMSClasses() {
      try {
         itemClass = Class.forName(post1dot17 ? "net.minecraft.world.item.Item" : "net.minecraft.server." + version + ".Item");
         itemsClass = Class.forName(post1dot17 ? "net.minecraft.world.item.Items" : "net.minecraft.server." + version + ".Items");
         itemGetNameMethod = itemClass.getMethod(post1dot18 ? "a" : "getName");
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException var1) {
         var1.printStackTrace();
      }
   }

   /**
    * Since 1.20.5 Paper ships unmapped, so the server package is plain
    * {@code org.bukkit.craftbukkit} with no {@code vX_Y_RZ} element. Detect that instead of
    * indexing blindly, which is what used to throw ArrayIndexOutOfBounds on 1.21.4.
    */
   private static void detectVersion() {
      if (version != null || modern) {
         return;
      }

      String[] parts = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
      if (parts.length < 4) {
         modern = true;
         return;
      }

      version = parts[3];
      post1dot17 = version.startsWith("v1_17") || version.startsWith("v1_18") || version.startsWith("v1_19");
      post1dot18 = version.startsWith("v1_18") || version.startsWith("v1_19");
      post1dot18dot2 = version.startsWith("v1_18_R2") || version.startsWith("v1_19");
      post1dot19 = version.startsWith("v1_19");
   }

   public static boolean isModern() {
      detectVersion();
      return modern;
   }

   public static boolean setForNMS(String materialName, int stack) {
      detectVersion();
      if (modern) {
         // 1.20.5+ drives the stack limit from the item's max_stack_size data component, so there
         // is no static NMS field to patch. Material.maxStack (setFor) plus the per item component
         // applied in InventoryListener cover it.
         return true;
      }

      if (itemClass == null) {
         initiateNMSClasses();
      }

      Field potionField;
      try {
         potionField = itemsClass.getDeclaredField(materialName);
      } catch (NoSuchFieldException var6) {
         if (post1dot17) {
            potionField = find1dot17PotionField(materialName);
         } else {
            potionField = null;
         }

         if (!post1dot17 || potionField == null) {
            var6.printStackTrace();
            return false;
         }
      } catch (SecurityException var7) {
         var7.printStackTrace();
         return false;
      }

      Object potion;
      try {
         potion = potionField.get(null);
      } catch (IllegalAccessException | IllegalArgumentException var5) {
         var5.printStackTrace();
         return false;
      }

      String fieldName;
      if (post1dot17 && !post1dot18dot2) {
         fieldName = "c";
      } else if (post1dot18dot2 && !post1dot19) {
         fieldName = "d";
      } else if (post1dot19) {
         fieldName = "d";
      } else {
         fieldName = "maxStackSize";
      }

      return setForClass(itemClass, potion, fieldName, stack);
   }

   private static Field find1dot17PotionField(String materialName) {
      Field[] var1 = itemsClass.getFields();

      for (Field field : var1) {
         if (Modifier.isStatic(field.getModifiers())) {
            Object got;
            try {
               got = field.get(null);
            } catch (IllegalAccessException | IllegalArgumentException var10) {
               var10.printStackTrace();
               continue;
            }

            if (itemClass.isInstance(got)) {
               String name;
               try {
                  name = (String)itemGetNameMethod.invoke(got);
               } catch (ClassCastException | IllegalAccessException | IllegalArgumentException | InvocationTargetException var9) {
                  var9.printStackTrace();
                  continue;
               }

               String[] split = name.split("\\.");
               name = split[split.length - 1];
               if (name.equalsIgnoreCase(materialName)) {
                  return field;
               }
            }
         }
      }

      return null;
   }

   private static void initiateClassesForInventoryUpdate() {
      try {
         craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
         entityPlayerClass = Class.forName(post1dot17 ? "net.minecraft.server.level.EntityPlayer" : "net.minecraft.server." + version + ".EntityPlayer");
         getHandleMethod = craftPlayerClass.getMethod("getHandle");
         containerInterface = Class.forName(post1dot17 ? "net.minecraft.world.inventory.Container" : "net.minecraft.server." + version + ".Container");
         updateInventoryMethod = entityPlayerClass.getMethod("updateInventory", containerInterface);
         activeInventoryField = entityPlayerClass.getField("activeContainer");
      } catch (NoSuchMethodException | ClassNotFoundException | NoSuchFieldException | SecurityException var1) {
         var1.printStackTrace();
      }
   }

   public static void updateInventory(Player bukkitPlayer) {
      detectVersion();
      if (modern) {
         bukkitPlayer.updateInventory();
         return;
      }

      if (!post1dot17) {
         if (craftPlayerClass == null) {
            initiateClassesForInventoryUpdate();
         }

         Object craftPlayer = craftPlayerClass.cast(bukkitPlayer);

         try {
            Object player = getHandleMethod.invoke(craftPlayer);
            Object activeContainer = activeInventoryField.get(player);
            updateInventoryMethod.invoke(player, activeContainer);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException var41) {
            var41.printStackTrace();
         }
      }
   }
}
