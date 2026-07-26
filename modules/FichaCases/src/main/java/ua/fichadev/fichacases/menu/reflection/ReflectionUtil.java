package ua.fichadev.fichacases.menu.reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
   public ReflectionUtil() {
   }

   public static Object getFieldValue(String fieldName, Object o) {
      Class clazz = o.getClass();

      List<Class> classes;
      for (classes = new ArrayList<>(); clazz != null && !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
         classes.add(clazz);
      }

      Field field = classes.stream().map(clz -> {
         try {
            return clz.getDeclaredField(fieldName);
         } catch (NoSuchFieldException var3x) {
            return null;
         }
      }).filter(f -> f != null).findFirst().orElse(null);
      if (field == null) {
         return null;
      } else {
         field.setAccessible(true);
         Object value = null;

         try {
            value = field.get(o);
         } catch (IllegalAccessException var7) {
            throw new RuntimeException(var7);
         }

         field.setAccessible(false);
         return value;
      }
   }
}
