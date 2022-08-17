package com.halludba.aimtrainer.commands;

import com.halludba.aimtrainer.AimTrainer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandControl implements CommandExecutor
{
    private final AimTrainer aimTrainer;
    public CommandControl (AimTrainer aimTrainer)
    {
        this.aimTrainer = aimTrainer;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(command.getName().equalsIgnoreCase("aimtrainer"))
        {
            try
            {
                if(args[0].equalsIgnoreCase("cancel"))
                {
                    if(sender instanceof Player)
                    {
                        this.aimTrainer.terminatePlayerSession(((Player)sender));
                        return true;
                    }
                } else if(args[0].equalsIgnoreCase("menu"))
                {
                    if(sender instanceof Player)
                    {
                        this.aimTrainer.getUI().openMainInventory((Player) sender);
                        return true;
                    }
                }

            } catch (IndexOutOfBoundsException e){}

        }
        return false;
    }
}
