package ua.fichadev.fichacases.database.dao;

import java.util.Collection;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.fichadev.fichacases.database.dao.entity.PrizeHistoryEntry;

public interface PrizeHistoryDao {
   @SqlUpdate("insert ignore into prize_history(opener, case_name, luck_name, prize_name, prize_material, timestamp, server_name) values(?,?,?,?,?,?,?)")
   void addEntry(String var1, String var2, String var3, String var4, String var5, long var6, String var8);

   @SqlQuery("select * from prize_history where case_name = :caseName order by timestamp desc limit :limit")
   @RegisterConstructorMapper(PrizeHistoryEntry.class)
   Collection<PrizeHistoryEntry> getLastPrizes(@Bind("caseName") String var1, @Bind("limit") int var2);
}
