package ua.fichadev.fichacases.database.dao.argument;

import java.util.UUID;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class UUIDArgumentFactory extends AbstractArgumentFactory<UUID> {
   public UUIDArgumentFactory() {
      super(12);
   }

   protected Argument build(UUID value, ConfigRegistry config) {
      return new UUIDArgument(value);
   }
}
