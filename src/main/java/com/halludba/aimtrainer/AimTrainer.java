package com.halludba.aimtrainer;

import com.halludba.aimtrainer.commands.CommandControl;
import com.halludba.aimtrainer.events.InteractEvent;
import com.halludba.aimtrainer.events.MoveEvent;
import com.halludba.aimtrainer.events.QuitEvent;
import com.halludba.aimtrainer.gamemodes.CurrentSession;
import com.halludba.aimtrainer.gamemodes.GridShot;
import com.halludba.aimtrainer.gamemodes.Tracker;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.ArrayList;


public class AimTrainer extends JavaPlugin
{
    private final AimTrainerGUI UI;
    private final ArrayList<Integer> sessionTypes = new ArrayList<Integer>(0);
    private final ArrayList<Player> players = new ArrayList<Player>(0);
    private final ArrayList<CurrentSession> currentSessions = new ArrayList<CurrentSession>(0);
    public AimTrainer()
    {
        this.UI = new AimTrainerGUI(this);
    }
    @Override
    public void onEnable() {
        getLogger().info("AimTrainer is enabled.");
        this.getCommand("aimtrainer").setExecutor(new CommandControl(this));
        getServer().getPluginManager().registerEvents(this.UI, this);
        getServer().getPluginManager().registerEvents(new InteractEvent(this), this);
        getServer().getPluginManager().registerEvents(new MoveEvent(this), this);
        getServer().getPluginManager().registerEvents(new QuitEvent(this), this);
    }
    @Override
    public void onDisable()
    {
        getLogger().info("AimTrainer is disabled.");
        for(int i = 0; i < this.currentSessions.size(); i++)
        {
            this.terminatePlayerSession(this.players.get(i));
        }
    }
    public ArrayList<CurrentSession> getCurrentSessions() {
        return currentSessions;
    }
    public CurrentSession getCurrentSession(int index)
    {
        if(index == -1)
        {
            return null;
        }
        return this.currentSessions.get(index);
    }
    public ArrayList<Integer> getSessionTypes() {
        return this.sessionTypes;
    }
    public AimTrainerGUI getUI()
    {
        return this.UI;
    }

    public void endCurrentSession(int index)
    {
        try {
            if(this.currentSessions.get(index) != null)
            {
                this.currentSessions.get(index).endSession();
                this.currentSessions.remove(index);
                this.players.remove(index);
            }
        } catch (IndexOutOfBoundsException e) {}
    }
    public void terminateSession(int index)
    {
        String name = this.currentSessions.get(index).getName();
        this.currentSessions.get(index).terminateSession();
        this.currentSessions.remove(index);
        this.players.get(index).sendMessage(ChatColor.RED + "Cancelled " + name);
        this.players.remove(index);
        this.sessionTypes.remove(index);
    }
    public void removeLastSessionType()
    {
        this.sessionTypes.remove(this.sessionTypes.size() - 1);
    }
    public void storeSessionType(int sessionType)
    {
        this.sessionTypes.add(sessionType);
    }
    public void terminatePlayerSession(Player p)
    {
        if(this.players.contains(p))
        {
            this.terminateSession(this.players.indexOf(p));
        }
    }
     public void startSession(int MODE, Player p, boolean ranked)
     {
         AimTrainer aimTrainer = this;
         aimTrainer.terminatePlayerSession(p);
         if(MODE == 1) // Gridshot
        {
            this.currentSessions.add(new GridShot(p, ranked));
            int index = this.currentSessions.size() - 1;
            CurrentSession cr = this.currentSessions.get(index);
            this.players.add(p);
            if(!cr.setup())
            {
                p.sendMessage(ChatColor.DARK_RED + "The front of you must have a clear area to start GridShot!");
                this.currentSessions.remove(index);
                this.players.remove(index);
                this.removeLastSessionType();
                return;
            }
            new BukkitRunnable()
            {
                @Override
                public void run() {
                    if(cr.isTerminated())
                    {
                        this.cancel();
                    }
                    cr.tick();
                    if(cr.getRemainingSeconds() <= 0)
                    {
                        aimTrainer.endCurrentSession(aimTrainer.players.indexOf(cr.getPlayer()));
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0, 1);
        } else if (MODE == 2) // Tracker
        {
            this.currentSessions.add(new Tracker(p, ranked));
            int index = this.currentSessions.size() - 1;
            CurrentSession cr = this.currentSessions.get(index);
            this.players.add(p);
            if(!cr.setup())
            {
                p.sendMessage(ChatColor.DARK_RED + "The front of you must have a clear area to start Tracker!");
                this.currentSessions.remove(index);
                this.players.remove(index);
                this.removeLastSessionType();
                return;
            }
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if(cr.isTerminated())
                    {
                        this.cancel();
                    }
                    cr.tick();
                    if(cr.getRemainingSeconds() <= 0)
                    {
                        aimTrainer.endCurrentSession(aimTrainer.players.indexOf(cr.getPlayer()));
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0, 1);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if(cr.isTerminated())
                    {
                        this.cancel();
                    }
                    cr.changeTargetsDestinations();
                    if(cr.getRemainingSeconds() <= 0)
                    {
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0, 20);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if(cr.isTerminated())
                    {
                        this.cancel();
                    }
                    cr.checkPlayerCrosshair();
                    cr.moveTargets();
                    if(cr.getRemainingSeconds() <= 0)
                    {
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0, 1);

        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
