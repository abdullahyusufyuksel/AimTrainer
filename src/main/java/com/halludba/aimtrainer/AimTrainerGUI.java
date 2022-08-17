package com.halludba.aimtrainer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AimTrainerGUI implements Listener {
    private final Inventory mainInv;
    private final Inventory rankedOrNotInv;
    private final AimTrainer aimTrainer;
    public Inventory getMainInv()
    {
        return this.mainInv;
    }
    public AimTrainerGUI(AimTrainer aimTrainer) {
        this.aimTrainer = aimTrainer;
        // Create a new inventory, with no owner (as this isn't a real inventory)
        this.mainInv = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_RED + "AimTrainer");
        this.rankedOrNotInv = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_RED + "AimTrainer");
        // Put the items into the inventory
        initializeItems();
    }
    // You can call this whenever you want to put the items in
    public void initializeItems() {
        this.mainInv.setItem(0, createGuiItem(Material.DIAMOND_BLOCK, "§cGridshot", "§cThis is very similar to Gridshot from conventional Aim Trainers.", "§cAccurately click as many blocks as possible in the given time period."));
        this.mainInv.setItem(2, createGuiItem(Material.BOW, "§4Tracking", "§4This is very similar to the tracking modes from AimTrainers", "§4Keep your crosshair over the moving target","§4while holding down left click during the given time period to gain points"));
        this.mainInv.setItem(4, createGuiItem(Material.BARRIER, "§gCancel"));

        this.rankedOrNotInv.setItem(0, createGuiItem(Material.DIAMOND_SWORD, "§cRanked", "§fThis session will be recorded on your profile."));
        this.rankedOrNotInv.setItem(2, createGuiItem(Material.WOODEN_SHOVEL, "§7FreePlay", "§fThis session will not be recorded on your profile."));
        this.rankedOrNotInv.setItem(4, createGuiItem(Material.BARRIER, "§gCancel"));
    }

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openMainInventory(Player p) {
        p.openInventory(this.mainInv);
    }

    // Check for clicks on items
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {

        if (!e.getInventory().equals(this.mainInv) && !e.getInventory().equals(this.rankedOrNotInv)) return;

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();
        if(e.getInventory().equals(this.mainInv))
        {
            e.setCancelled(true); // immediately drops item to create button-like effect
            if(e.getSlot() == 0)
            {
                p.closeInventory();
                this.aimTrainer.storeSessionType(1);
                p.openInventory(this.rankedOrNotInv);
            } else if (e.getSlot() == 2) {
                p.closeInventory();

                this.aimTrainer.storeSessionType(2);
                p.openInventory(this.rankedOrNotInv);
            } else if (e.getSlot() == 4)
            {
                p.closeInventory();
            }
        } else if(e.getInventory().equals(this.rankedOrNotInv))
        {
            e.setCancelled(true); // immediately drops item to create button-like effect
            if(e.getSlot() == 0)
            {
                this.aimTrainer.startSession(this.aimTrainer.getSessionTypes().get(this.aimTrainer.getSessionTypes().size() - 1), p, true);
                p.closeInventory();
                return;
            } else if (e.getSlot() == 2) {
                this.aimTrainer.startSession(this.aimTrainer.getSessionTypes().get(this.aimTrainer.getSessionTypes().size() - 1), p, false);
                p.closeInventory();
                return;
            } else if (e.getSlot() == 4)
            {
                this.aimTrainer.removeLastSessionType();
                p.closeInventory();
                return;
            }
            this.aimTrainer.removeLastSessionType();
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory().equals(mainInv) || e.getInventory().equals(rankedOrNotInv)) {
            e.setCancelled(true);
        }
    }
}