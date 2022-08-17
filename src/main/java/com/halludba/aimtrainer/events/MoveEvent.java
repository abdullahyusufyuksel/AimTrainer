package com.halludba.aimtrainer.events;

import com.halludba.aimtrainer.AimTrainer;
import com.halludba.aimtrainer.gamemodes.CurrentSession;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveEvent implements Listener
{
    private final AimTrainer aimTrainer;

    public MoveEvent(AimTrainer aimTrainer)
    {
        this.aimTrainer = aimTrainer;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        CurrentSession cr = this.aimTrainer.getCurrentSession(this.aimTrainer.getPlayers().indexOf(e.getPlayer()));
        if(cr != null)
        {
            if(!e.getFrom().getBlock().equals(e.getTo().getBlock()))
            {
                e.getPlayer().teleport(e.getFrom());
                e.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot move from this position once the game has started!");
            }
        }
    }

}
