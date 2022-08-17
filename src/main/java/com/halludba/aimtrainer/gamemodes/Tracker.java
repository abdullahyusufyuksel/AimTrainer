package com.halludba.aimtrainer.gamemodes;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class Tracker extends CurrentSession
{
    HashMap<Integer, Location> whereIsTargetHeaded = new HashMap<Integer, Location>(0); //key = index
    private final String targetHealth = "||||||||||";
    public Tracker(Player player, boolean ranked)
    {
        super(player, ranked);
        this.name = "Tracker";
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
                this.runAway(i);
                LivingEntity e = (LivingEntity)this.player.getWorld().spawnEntity(this.blocksInFront.get(indexGenerated), EntityType.BEE);
                e.setSilent(true);
                e.setAI(false);
                e.setHealth(10);
                e.setGravity(false);
                e.setCustomName(ChatColor.GREEN + Double.toString(e.getHealth()));
                e.setCustomNameVisible(true);
                this.targets.add(e);
                i++;
            }
        }
        this.changeTargetsDestinations();
    }
    public boolean setup()
    {
        this.points.set(0);
        if(!this.isFrontEmpty())
        {
            return false;
        }
        this.generateTargets(3);
        this.setupScoreboard();
        this.player.sendMessage(ChatColor.GREEN + "Starting Tracker!");
        return true;
    }
    public void setupScoreboard()
    {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("\u00A7b" + "AimTrainer", "\u00A7b" + "Tracker", "\u00A7b" + "AimTrainer");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.objective.getScore(ChatColor.AQUA + "Tracker").setScore(3);

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
    public void checkPlayerCrosshair()
    {
        for(int i = 0; i < this.targets.size(); i++)
        {
            if(getLookingAt(this.player, this.targets.get(i)))
            {
                this.shootTarget(i);
                return;
            }
        }
    }
    public void changeTargetsDestinations()
    {
        for(int i = 0; i < this.targets.size(); i++)
        {
            this.runAway(i);
        }
    }
    private boolean getLookingAt(Player player, Entity e)
    {
        Location playerLoc = player.getEyeLocation();
        Vector playerDirection = player.getEyeLocation().getDirection();
        return (e.getBoundingBox().rayTrace(playerLoc.toVector(), playerDirection, 20) != null);
    }
    public void shootTarget(int index)
    {
        this.points.getAndAdd(5);
        this.targets.get(index).setHealth(this.targets.get(index).getHealth() - 0.5);
        if(this.targets.get(index).getHealth() == 0)
        {
            this.targetIndices.remove(index);
            this.targets.get(index).remove();
            this.targets.remove(index);
            this.generateTargets(1);
        }
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

    @Override
    public void terminateSession()
    {
        this.terminated = true;
        this.removeTargets();
        this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }
    public void runAway(int index)
    {
//        BoundingBox b = new BoundingBox(this.blocksInFront.get(0).getX(), this.blocksInFront.get(0).getY(), this.blocksInFront.get(0).getZ(), this.blocksInFront.get(this.blocksInFront.size() - 1).getX(), this.blocksInFront.get(this.blocksInFront.size() - 1).getY(), this.blocksInFront.get(this.blocksInFront.size() - 1).getZ());
        if(index >= this.targets.size()) return;
        double xOrigin = Math.min(this.blocksInFront.get(0).getX(), this.blocksInFront.get(this.blocksInFront.size() - 1).getX());
        double zOrigin = Math.min(this.blocksInFront.get(0).getZ(), this.blocksInFront.get(this.blocksInFront.size() - 1).getZ());
        double xDest = Math.max(this.blocksInFront.get(0).getX(), this.blocksInFront.get(this.blocksInFront.size() - 1).getX());
        double zDest = Math.max(this.blocksInFront.get(0).getZ(), this.blocksInFront.get(this.blocksInFront.size() - 1).getZ());
        Random rand = new Random();
        Location newLoc = new Location(this.player.getWorld(), rand.nextDouble(xOrigin, xDest), this.targets.get(index).getLocation().getY(), rand.nextDouble(zOrigin, zDest));
//        -Math.cos(Math.toRadians(this.player.getLocation().getYaw())), 0,  -Math.sin(Math.toRadians(player.getLocation().getYaw()))
        this.whereIsTargetHeaded.put(index, newLoc);
    }
    public void moveTargets()
    {
        Random r = new Random();
        for(int i = 0; i < this.targets.size(); i++)
        {
            Location entityLocation = this.targets.get(i).getLocation().clone();
            Location locToGoTo =this.whereIsTargetHeaded.get(i).clone().subtract(this.targets.get(i).getLocation());
            locToGoTo.setY(0);
            entityLocation = entityLocation.add(locToGoTo.toVector().normalize().multiply(0.3));
            this.targets.get(i).teleport(entityLocation);
            this.targets.get(i).setCustomName(ChatColor.GREEN + this.targetHealth.substring(0, (int) Math.ceil(this.targets.get(i).getHealth() - 0.5)));
        }
    }
}
