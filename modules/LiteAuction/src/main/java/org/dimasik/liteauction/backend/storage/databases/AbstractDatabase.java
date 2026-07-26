package org.dimasik.liteauction.backend.storage.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.dimasik.liteauction.backend.storage.tables.impl.BidItems;
import org.dimasik.liteauction.backend.storage.tables.impl.Bids;
import org.dimasik.liteauction.backend.storage.tables.impl.GuiDatas;
import org.dimasik.liteauction.backend.storage.tables.impl.HistoryItems;
import org.dimasik.liteauction.backend.storage.tables.impl.SellItems;
import org.dimasik.liteauction.backend.storage.tables.impl.Sounds;
import org.dimasik.liteauction.backend.storage.tables.impl.UnsoldItems;

public abstract class AbstractDatabase {
   protected HikariDataSource dataSource;
   protected SellItems sellItemsManager;
   protected UnsoldItems unsoldItemsManager;
   protected Sounds soundsManager;
   protected BidItems bidItemsManager;
   protected Bids bidsManager;
   protected GuiDatas guiDatasManager;
   protected HistoryItems historyItems;

   public void initialize(HikariConfig config) {
      this.dataSource = new HikariDataSource(config);
      this.sellItemsManager = new SellItems(this.dataSource);
      this.unsoldItemsManager = new UnsoldItems(this.dataSource);
      this.soundsManager = new Sounds(this.dataSource);
      this.bidItemsManager = new BidItems(this.dataSource);
      this.bidsManager = new Bids(this.dataSource);
      this.guiDatasManager = new GuiDatas(this.dataSource);
      this.historyItems = new HistoryItems(this.dataSource);
   }

   public CompletableFuture<Void> createTables() {
      return CompletableFuture.allOf(
         this.sellItemsManager.createTable(),
         this.unsoldItemsManager.createTable(),
         this.soundsManager.createTable(),
         this.bidItemsManager.createTable(),
         this.bidsManager.createTable(),
         this.guiDatasManager.createTable(),
         this.historyItems.createTable()
      );
   }

   public void moveExpiredItems() {
      this.sellItemsManager.moveExpiredItems();
      this.unsoldItemsManager.deleteExpiredItems();
      this.bidItemsManager.moveExpiredItems();
   }

   public void close() {
      this.dataSource.close();
   }

   public String editQuery(String sql) {
      return sql;
   }

   @Generated
   public HikariDataSource getDataSource() {
      return this.dataSource;
   }

   @Generated
   public SellItems getSellItemsManager() {
      return this.sellItemsManager;
   }

   @Generated
   public UnsoldItems getUnsoldItemsManager() {
      return this.unsoldItemsManager;
   }

   @Generated
   public Sounds getSoundsManager() {
      return this.soundsManager;
   }

   @Generated
   public BidItems getBidItemsManager() {
      return this.bidItemsManager;
   }

   @Generated
   public Bids getBidsManager() {
      return this.bidsManager;
   }

   @Generated
   public GuiDatas getGuiDatasManager() {
      return this.guiDatasManager;
   }

   @Generated
   public HistoryItems getHistoryItems() {
      return this.historyItems;
   }

   @Generated
   public AbstractDatabase() {
   }
}
