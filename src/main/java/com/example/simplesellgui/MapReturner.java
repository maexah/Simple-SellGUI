package com.example.simplesellgui;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class MapReturner {

    private MapReturner() {
    }

    public static void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        if (!leftovers.isEmpty()) {
            Location loc = player.getLocation();
            leftovers.values().forEach(remaining -> player.getWorld().dropItemNaturally(loc, remaining));
        }
    }
}
