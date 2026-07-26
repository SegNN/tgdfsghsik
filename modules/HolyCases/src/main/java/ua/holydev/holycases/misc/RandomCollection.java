package ua.holydev.holycases.misc;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class RandomCollection<E> {
   private final NavigableMap<Double, E> map = new TreeMap<>();
   private final Random random = new Random();
   private double total = 0.0;

   public RandomCollection() {
   }

   public RandomCollection<E> add(double weight, E result) {
      if (weight <= 0.0) {
         return this;
      } else {
         this.total += weight;
         this.map.put(this.total, result);
         return this;
      }
   }

   public E next() {
      double value = this.random.nextDouble() * this.total;
      return this.map.higherEntry(value).getValue();
   }

   public Set<Entry<Double, E>> getEntries() {
      return this.map.entrySet();
   }

   public Collection<E> getValues() {
      return this.map.values();
   }

   public double getWeight(E element) {
      if (element != null && this.map.containsValue(element)) {
         double weight = 0.0;
         double lastKey = 0.0;

         for (Entry<Double, E> entry : this.map.entrySet()) {
            if (entry.getValue().equals(element)) {
               weight = entry.getKey() - lastKey;
               break;
            }

            lastKey = entry.getKey();
         }

         return weight;
      } else {
         return 0.0;
      }
   }

   public double getTotal() {
      return this.total;
   }
}
