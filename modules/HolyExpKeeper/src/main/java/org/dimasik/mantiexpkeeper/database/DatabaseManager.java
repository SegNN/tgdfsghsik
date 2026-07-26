package org.dimasik.mantiexpkeeper.database;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface DatabaseManager {
   void init() throws Exception;

   void close();

   CompletableFuture<Void> cleanExpiredAbilities(Executor var1);

   CompletableFuture<Void> addTime(Executor var1, String var2, String var3, long var4);

   CompletableFuture<Void> addInf(Executor var1, String var2, String var3);

   CompletableFuture<Void> removePlayer(Executor var1, String var2, String var3);

   CompletableFuture<Boolean> hasAbility(Executor var1, String var2, String var3);

   CompletableFuture<String> getAbilityTime(Executor var1, String var2, String var3);
}
