package com.halludba.aimtrainer.events;

import com.halludba.aimtrainer.AimTrainer;
import com.halludba.aimtrainer.gamemodes.CurrentSession;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Objects;

public class InteractEvent implements Listener
{
    private final AimTrainer aimTrainer;

    public InteractEvent(AimTrainer aimTrainer)
    {
        this.aimTrainer = aimTrainer;
    }

    private boolean getLookingAt(Player player, Entity e)
    {
        Location playerLoc = player.getEyeLocation();
        Vector playerDirection = player.getEyeLocation().getDirection();
        return (e.getBoundingBox().rayTrace(playerLoc.toVector(), playerDirection, 20) != null);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event){
        Player p = event.getPlayer();
        Action click  = event.getAction();
        Location TargetLocation = p.getLocation();
        CurrentSession cr = this.aimTrainer.getCurrentSession(this.aimTrainer.getPlayers().indexOf(p));
        if(cr != null)
        {
            if(Objects.equals(cr.getName(), "Gridshot"))
            {
                if (click == Action.LEFT_CLICK_AIR)
                {
                    for(int i = 0; i < cr.getTargets().size(); i++)
                    {
                        if(getLookingAt(p, cr.getTargets().get(i)))
                        {
                            cr.shootTarget(i);
                            cr.generateNewTarget();
                            return;
                        }
                    }
                    cr.whiff();
                }
            }
        }


    }
}
