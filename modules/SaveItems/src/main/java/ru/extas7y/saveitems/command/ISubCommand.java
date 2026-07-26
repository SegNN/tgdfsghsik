package ru.extas7y.saveitems.command;

import org.bukkit.command.CommandSender;

public interface ISubCommand {
   String Argument();

   boolean execute(CommandSender var1, String[] var2);
}
