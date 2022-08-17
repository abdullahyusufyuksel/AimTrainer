package com.halludba.aimtrainer.gamemodes;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.IOException;
import java.util.Random;

public class GridShot extends CurrentSession
{

    public GridShot(Player player, boolean ranked)
    {
        super(player, ranked);
        this.name = "Gridshot";
    }

    public boolean setup()
    {
        this.points.set(0);
        if(!this.isFrontEmpty())
        {
            return false;
        }
        this.generateTargets(6);
        this.setupScoreboard();
        this.player.sendMessage(ChatColor.GREEN + "Starting GridShot!");
        return true;
    }
    public void setupScoreboard()
    {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("\u00A7b" + "AimTrainer", "\u00A7b" + "GridShot", "\u00A7b" + "AimTrainer");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.objective.getScore(ChatColor.AQUA + "GridShot").setScore(3);

        this.scoreboard.registerNewTeam("timeRemaining");
        this.scoreboard.getTeam("timeRemaining").addEntry(ChatColor.AQUA + "");
        this.scoreboard.getTeam("timeRemaining").setPrefix(ChatColor.AQUA + "Time Remaining:  " + (int) this.originalTimer);
        this.objective.getScore(ChatColor.AQUA + "").setScore(2);

        this.scoreboard.registerNewTeam("targetsHit");
        this.scoreboard.getTeam("targetsHit").addEntry(ChatColor.GREEN + "");
        this.scoreboard.getTeam("targetsHit").setPrefix(ChatColor.GREEN + "Points:  " + this.points.get());
        this.objective.getScore(ChatColor.GREEN + "").setScore(1);

        this.player.setScoreboard(this.scoreboard);
    }
    public void generateTargets(int numTargets)
    {
        Random random = new Random();

        for(int i = 0; i < numTargets;)
        {
            Integer indexGenerated = random.nextInt(this.blocksInFront.size()/3);
            if(!this.targetIndices.containsKey(indexGenerated))
            {
                this.targetIndices.put(indexGenerated, 1);
                LivingEntity e = (LivingEntity)this.player.getWorld().spawnEntity(this.blocksInFront.get(indexGenerated), EntityType.BEE);
                e.setSilent(true);
                e.setAI(false);
                this.targets.add(e);
                i++;
            }
        }
    }
    public void changeTargetsDestinations() {}
    public void tick()
    {
        if(this.remainingSeconds.get() > 0)
        {
            this.remainingSeconds.set(60 - (System.currentTimeMillis() - this.startTime)/1000);
            this.scoreboard.getTeam("timeRemaining").setPrefix(ChatColor.AQUA + "Time Remaining:  " + (int) this.remainingSeconds.get());
            this.scoreboard.getTeam("targetsHit").setPrefix(ChatColor.GREEN + "Points:  " + this.points.get());
            this.player.setScoreboard(this.scoreboard);
        }
    }
    public void shootTarget(int index)
    {
        this.targetIndices.remove(index);
        this.targets.get(index).remove();
        this.targets.remove(index);
        this.points.getAndAdd(250);
    }
    public void checkPlayerCrosshair() {}

    @Override
    public void terminateSession()
    {
        this.terminated = true;
        this.removeTargets();
        this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void endSession()
    {
        if(this.ranked)
        {
            try {
                this.pushRankedGameToDatabase();
            } catch (IOException e) {
                this.player.getServer().getLogger().info("Failed to push game into DB!");
            }
        }
        this.removeTargets();
        this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        this.player.sendTitle(ChatColor.GREEN + "You scored " + ChatColor.BLUE + this.points.get() + " points.", null, 10, 40, 7);
    }
    public void moveTargets() {}
}
