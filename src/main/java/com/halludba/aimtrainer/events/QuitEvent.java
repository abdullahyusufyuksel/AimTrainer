package com.halludba.aimtrainer.events;

import com.halludba.aimtrainer.AimTrainer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener
{
    private final AimTrainer aimTrainer;

    public QuitEvent(AimTrainer aimTrainer)
    {
        this.aimTrainer = aimTrainer;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        int playerIndex = this.aimTrainer.getPlayers().indexOf(e.getPlayer());
        if(playerIndex != -1)
        {
            this.aimTrainer.getCurrentSession(playerIndex).terminateSession();
            this.aimTrainer.getPlayers().remove(playerIndex);
            this.aimTrainer.getCurrentSessions().remove(playerIndex);
            this.aimTrainer.getSessionTypes().remove(playerIndex);
        }
    }

}
