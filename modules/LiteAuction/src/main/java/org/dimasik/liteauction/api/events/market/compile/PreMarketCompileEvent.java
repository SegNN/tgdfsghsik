package org.dimasik.liteauction.api.events.market.compile;

import java.util.HashSet;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;

public class PreMarketCompileEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   private final Player player;
   private int page;
   private String target;
   private MarketSortingType sortingType;
   private CategoryType categoryType;
   private HashSet<String> filters;

   public PreMarketCompileEvent(Player player, int page, String target, MarketSortingType sortingType, CategoryType categoryType, HashSet<String> filters) {
      this.player = player;
      this.page = page;
      this.target = target;
      this.sortingType = sortingType;
      this.categoryType = categoryType;
      this.filters = filters;
   }

   @Generated
   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public int getPage() {
      return this.page;
   }

   @Generated
   public String getTarget() {
      return this.target;
   }

   @Generated
   public MarketSortingType getSortingType() {
      return this.sortingType;
   }

   @Generated
   public CategoryType getCategoryType() {
      return this.categoryType;
   }

   @Generated
   public HashSet<String> getFilters() {
      return this.filters;
   }

   @Generated
   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }

   @Generated
   public void setTarget(String target) {
      this.target = target;
   }

   @Generated
   public void setSortingType(MarketSortingType sortingType) {
      this.sortingType = sortingType;
   }

   @Generated
   public void setCategoryType(CategoryType categoryType) {
      this.categoryType = categoryType;
   }

   @Generated
   public void setFilters(HashSet<String> filters) {
      this.filters = filters;
   }
}
