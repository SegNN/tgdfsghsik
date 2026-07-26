package org.dimasik.liteauction.api.events;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.plugin.java.JavaPlugin;

public class EventManager {
   private final Map<JavaPlugin, Set<Object>> listeners = new HashMap<>();
   private final Map<Class<? extends Event>, Map<Object, List<Method>>> handlerCache = new HashMap<>();

   public EventManager() {
   }

   public void register(JavaPlugin plugin, Object listener) {
      if (plugin != null && plugin.isEnabled()) {
         if (!listener.getClass().isAnnotationPresent(Subscribed.class)) {
            throw new IllegalArgumentException("Listener must be annotated with @Subscribed");
         } else {
            this.unregister(plugin, listener);
            this.listeners.computeIfAbsent(plugin, k -> new HashSet<>()).add(listener);
            this.buildHandlerCache(listener);
         }
      }
   }

   public void unregister(JavaPlugin plugin) {
      if (plugin != null) {
         Set<Object> pluginListeners = this.listeners.remove(plugin);
         if (pluginListeners != null) {
            for (Object listener : pluginListeners) {
               this.removeFromHandlerCache(listener);
            }
         }
      }
   }

   public void unregister(JavaPlugin plugin, Object listener) {
      if (plugin != null) {
         Set<Object> pluginListeners = this.listeners.get(plugin);
         if (pluginListeners != null) {
            pluginListeners.remove(listener);
            this.removeFromHandlerCache(listener);
         }
      }
   }

   public void triggerEvent(Event event) {
      Class<? extends Event> eventClass = (Class<? extends Event>)event.getClass();
      Map<Object, List<Method>> handlers = this.handlerCache.get(eventClass);
      if (handlers != null) {
         for (Entry<Object, List<Method>> entry : handlers.entrySet()) {
            Object listener = entry.getKey();
            JavaPlugin plugin = this.getPluginForListener(listener);
            if (plugin != null && plugin.isEnabled()) {
               for (Method method : entry.getValue()) {
                  try {
                     method.invoke(listener, event);
                  } catch (Exception var11) {
                     var11.printStackTrace();
                  }
               }
            }
         }
      }
   }

   private JavaPlugin getPluginForListener(Object listener) {
      for (Entry<JavaPlugin, Set<Object>> entry : this.listeners.entrySet()) {
         if (entry.getValue().contains(listener)) {
            return entry.getKey();
         }
      }

      return null;
   }

   private void buildHandlerCache(Object listener) {
      for (Method method : listener.getClass().getDeclaredMethods()) {
         if (method.isAnnotationPresent(Subscribe.class)) {
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
               Class<? extends Event> eventClass = (Class<? extends Event>)parameters[0];
               this.handlerCache.computeIfAbsent(eventClass, k -> new HashMap<>()).computeIfAbsent(listener, k -> new ArrayList<>()).add(method);
            }
         }
      }
   }

   private void removeFromHandlerCache(Object listener) {
      for (Map<Object, List<Method>> handlers : this.handlerCache.values()) {
         handlers.remove(listener);
      }
   }
}
