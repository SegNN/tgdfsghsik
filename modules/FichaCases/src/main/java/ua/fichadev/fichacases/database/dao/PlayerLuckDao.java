package ua.fichadev.fichacases.database.dao;

import java.util.UUID;
import shaded.org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import shaded.org.jdbi.v3.sqlobject.customizer.Bind;
import shaded.org.jdbi.v3.sqlobject.statement.SqlQuery;
import shaded.org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.fichadev.fichacases.database.dao.argument.UUIDArgumentFactory;

@RegisterArgumentFactory(UUIDArgumentFactory.class)
public interface PlayerLuckDao {
   @SqlUpdate("INSERT IGNORE INTO player_luck(player, luck_name, `count`) VALUES (:player, :luck_name, :count) ON DUPLICATE KEY UPDATE count = count + :count")
   void addLuck(@Bind("player") UUID var1, @Bind("luck_name") String var2, @Bind("count") int var3);

   @SqlUpdate("INSERT IGNORE INTO player_luck(player, luck_name, `count`) VALUES (:player, :luck_name, :count) ON DUPLICATE KEY UPDATE count = count - :count")
   void withdrawLuck(@Bind("player") UUID var1, @Bind("luck_name") String var2, @Bind("count") int var3);

   @SqlQuery("SELECT COALESCE(SUM(`count`), 0) FROM player_luck WHERE player = :player AND luck_name = :luck_name")
   int getLuck(@Bind("player") UUID var1, @Bind("luck_name") String var2);
}
