package com.example.simplesellgui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SellInventoryHolder implements InventoryHolder {

    public enum Type { MAIN, CONFIRM }

    private final SellSession session;
    private final Type type;

    public SellInventoryHolder(SellSession session, Type type) {
        this.session = session;
        this.type = type;
    }

    public SellSession getSession() {
        return session;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null; // Bukkit will handle inventory reference
    }
}
