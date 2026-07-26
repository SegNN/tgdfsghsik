package ua.holydev.holycases.database.dao;

import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.holydev.holycases.database.dao.argument.UUIDArgumentFactory;

@RegisterArgumentFactory(UUIDArgumentFactory.class)
public interface PlayerKeysDao {
   @SqlUpdate("INSERT IGNORE INTO player_keys(player, key_name, `count`) VALUES (:player, :key_name, :count) ON DUPLICATE KEY UPDATE count = count + :count")
   void addKeys(@Bind("player") UUID var1, @Bind("key_name") String var2, @Bind("count") int var3);

   @SqlUpdate("INSERT IGNORE INTO player_keys(player, key_name, `count`) VALUES (:player, :key_name, :count) ON DUPLICATE KEY UPDATE count = count - :count")
   void withdrawKeys(@Bind("player") UUID var1, @Bind("key_name") String var2, @Bind("count") int var3);

   @SqlQuery("SELECT COALESCE(SUM(`count`), 0) FROM player_keys WHERE player = :player AND key_name = :key_name")
   int getKeys(@Bind("player") UUID var1, @Bind("key_name") String var2);
}
