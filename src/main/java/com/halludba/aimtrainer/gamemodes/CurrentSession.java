package com.halludba.aimtrainer.gamemodes;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class CurrentSession
{
    protected Location originalLocation;
    protected boolean ranked = false;
    protected boolean terminated = false;
    protected final double rectangleXDimension = 12;
    protected final double rectangleYDimension = 5;
    protected final double rectangleZDimension = 12;
    protected AtomicDouble remainingSeconds;
    protected double originalTimer;
    protected String name;
    protected double startTime;
    protected Player player;
    protected Scoreboard scoreboard;
    protected Objective objective;
    protected HashMap<Integer, Integer> targetIndices = new HashMap<Integer, Integer>(0);
    protected ArrayList<Location> blocksInFront =  new ArrayList<Location>(0);
    protected ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>(0);
    protected AtomicInteger points;
    public CurrentSession(Player player, boolean ranked)
    {
        this.ranked = ranked;
        this.originalTimer = 60;
        this.player = player;
        this.remainingSeconds = new AtomicDouble(originalTimer);
        this.points = new AtomicInteger(0);
        this.startTime = System.currentTimeMillis();
    }
    public Player getPlayer() {
        return player;
    }

    public boolean isTerminated() {
        return terminated;
    }
    public void pushRankedGameToDatabase() throws IOException {
        OkHttpClient client = new OkHttpClient();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", this.player.getName());
        jsonObject.addProperty("points", String.valueOf(this.points.get()));
        jsonObject.addProperty("type", this.name);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://aimtrainer.halludba.com/api/insertGame")
                .post(body)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        response.close();
    }
    public boolean isFrontEmpty()
    {
        this.originalLocation = player.getLocation().clone();
        double cos90 = -Math.cos(Math.toRadians(player.getLocation().getYaw() - 90));
        double sin90 = -Math.sin(Math.toRadians(player.getLocation().getYaw() - 90));
        Location firstLoc = player.getLocation().add(Math.cos(Math.toRadians(player.getLocation().getYaw())) * rectangleXDimension/2, rectangleYDimension, Math.sin(Math.toRadians(player.getLocation().getYaw())) * rectangleZDimension/2);
        firstLoc.add(cos90, 0, sin90);
        firstLoc.add(cos90 * rectangleXDimension, 0, sin90 * rectangleZDimension);

        Location currLocation = firstLoc.clone();
        for(int i = 0; i < this.rectangleZDimension; i++)
        {
            Location rowHead = currLocation.clone();
            for(int j = 0; j < this.rectangleXDimension; j++)
            {
                Location columnHead = currLocation.clone();
                for(int k = 0; k < this.rectangleYDimension; k++)
                {
                    this.blocksInFront.add(currLocation.clone());
                    if(!currLocation.getBlock().isEmpty())
                    {
                        this.blocksInFront.clear();
                        return false;
                    }
                    currLocation = currLocation.clone().add(0,-1, 0);
                }
                currLocation = columnHead;
                currLocation = currLocation.add(-Math.cos(Math.toRadians(this.player.getLocation().getYaw())), 0,  -Math.sin(Math.toRadians(player.getLocation().getYaw())));
            }
            currLocation = rowHead;
            currLocation = currLocation.add(-cos90, 0, -sin90);
        }
        return true;
    }

    public ArrayList<LivingEntity> getTargets()
    {
        return targets;
    }

    public void generateNewTarget()
    {
        Random random = new Random();
        while(true)
        {
            Integer indexGenerated = random.nextInt(this.blocksInFront.size()/3);
            if(!this.targetIndices.containsKey(indexGenerated))
            {
                this.targetIndices.put(indexGenerated, 1);
                LivingEntity e = (LivingEntity)this.player.getWorld().spawnEntity(this.blocksInFront.get(indexGenerated), EntityType.BEE);
                e.setSilent(true);
                e.setAI(false);
                this.targets.add(e);
                break;
            }
        }
    }
    public String getName() {
        return this.name;
    }
    public ArrayList<Location> getBlocksInFront() {
        return blocksInFront;
    }
    public void whiff()
    {
        this.points.getAndAdd(-50);
    }

    public void removeTargets()
    {
        while(!targets.isEmpty())
        {
            this.targets.get(0).remove();
            this.targets.remove(0);
        }
        this.targetIndices.clear();
    }
    public double getRemainingSeconds()
    {
        return remainingSeconds.get();
    }
    abstract public boolean setup();
    abstract public void endSession();
    abstract public void generateTargets(int numTargets);
    abstract public void tick();
    abstract public void shootTarget(int index);

    abstract public void moveTargets();

    abstract public void changeTargetsDestinations();
    abstract public void checkPlayerCrosshair();
    abstract public void terminateSession();

}
