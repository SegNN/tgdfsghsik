package org.dimasik.liteauction.backend.storage.models;

import java.util.Set;
import lombok.Generated;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;

public class GuiData {
   private int id;
   private String player;
   private AuctionType auctionType;
   private CategoryType categoryType;
   private MarketSortingType marketSortingType;
   private BidsSortingType bidsSortingType;
   private Set<String> additionalFilters;

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public AuctionType getAuctionType() {
      return this.auctionType;
   }

   @Generated
   public CategoryType getCategoryType() {
      return this.categoryType;
   }

   @Generated
   public MarketSortingType getMarketSortingType() {
      return this.marketSortingType;
   }

   @Generated
   public BidsSortingType getBidsSortingType() {
      return this.bidsSortingType;
   }

   @Generated
   public Set<String> getAdditionalFilters() {
      return this.additionalFilters;
   }

   @Generated
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   public void setPlayer(String player) {
      this.player = player;
   }

   @Generated
   public void setAuctionType(AuctionType auctionType) {
      this.auctionType = auctionType;
   }

   @Generated
   public void setCategoryType(CategoryType categoryType) {
      this.categoryType = categoryType;
   }

   @Generated
   public void setMarketSortingType(MarketSortingType marketSortingType) {
      this.marketSortingType = marketSortingType;
   }

   @Generated
   public void setBidsSortingType(BidsSortingType bidsSortingType) {
      this.bidsSortingType = bidsSortingType;
   }

   @Generated
   public void setAdditionalFilters(Set<String> additionalFilters) {
      this.additionalFilters = additionalFilters;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GuiData other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getId() != other.getId()) {
         return false;
      } else {
         Object this$player = this.getPlayer();
         Object other$player = other.getPlayer();
         if (this$player == null ? other$player == null : this$player.equals(other$player)) {
            Object this$auctionType = this.getAuctionType();
            Object other$auctionType = other.getAuctionType();
            if (this$auctionType == null ? other$auctionType == null : this$auctionType.equals(other$auctionType)) {
               Object this$categoryType = this.getCategoryType();
               Object other$categoryType = other.getCategoryType();
               if (this$categoryType == null ? other$categoryType == null : this$categoryType.equals(other$categoryType)) {
                  Object this$marketSortingType = this.getMarketSortingType();
                  Object other$marketSortingType = other.getMarketSortingType();
                  if (this$marketSortingType == null ? other$marketSortingType == null : this$marketSortingType.equals(other$marketSortingType)) {
                     Object this$bidsSortingType = this.getBidsSortingType();
                     Object other$bidsSortingType = other.getBidsSortingType();
                     if (this$bidsSortingType == null ? other$bidsSortingType == null : this$bidsSortingType.equals(other$bidsSortingType)) {
                        Object this$additionalFilters = this.getAdditionalFilters();
                        Object other$additionalFilters = other.getAdditionalFilters();
                        return this$additionalFilters == null ? other$additionalFilters == null : this$additionalFilters.equals(other$additionalFilters);
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof GuiData;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getId();
      Object $player = this.getPlayer();
      result = result * 59 + ($player == null ? 43 : $player.hashCode());
      Object $auctionType = this.getAuctionType();
      result = result * 59 + ($auctionType == null ? 43 : $auctionType.hashCode());
      Object $categoryType = this.getCategoryType();
      result = result * 59 + ($categoryType == null ? 43 : $categoryType.hashCode());
      Object $marketSortingType = this.getMarketSortingType();
      result = result * 59 + ($marketSortingType == null ? 43 : $marketSortingType.hashCode());
      Object $bidsSortingType = this.getBidsSortingType();
      result = result * 59 + ($bidsSortingType == null ? 43 : $bidsSortingType.hashCode());
      Object $additionalFilters = this.getAdditionalFilters();
      return result * 59 + ($additionalFilters == null ? 43 : $additionalFilters.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "GuiData(id="
         + this.getId()
         + ", player="
         + this.getPlayer()
         + ", auctionType="
         + this.getAuctionType()
         + ", categoryType="
         + this.getCategoryType()
         + ", marketSortingType="
         + this.getMarketSortingType()
         + ", bidsSortingType="
         + this.getBidsSortingType()
         + ", additionalFilters="
         + this.getAdditionalFilters()
         + ")";
   }

   @Generated
   public GuiData() {
   }

   @Generated
   public GuiData(
      int id,
      String player,
      AuctionType auctionType,
      CategoryType categoryType,
      MarketSortingType marketSortingType,
      BidsSortingType bidsSortingType,
      Set<String> additionalFilters
   ) {
      this.id = id;
      this.player = player;
      this.auctionType = auctionType;
      this.categoryType = categoryType;
      this.marketSortingType = marketSortingType;
      this.bidsSortingType = bidsSortingType;
      this.additionalFilters = additionalFilters;
   }
}
