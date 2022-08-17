package com.halludba.aimtrainer.events;

import com.halludba.aimtrainer.AimTrainer;
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
    public void onQuit(PlayerQuitEvent e)
    {
        this.aimTrainer.terminatePlayerSession(e.getPlayer());
    }

}
