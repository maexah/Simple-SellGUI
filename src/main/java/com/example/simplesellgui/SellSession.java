package com.example.simplesellgui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SellSession {

    public static final int SELL_BUTTON_SLOT = 49;

    private final UUID playerId;
    private Inventory mainInventory;
    private List<ItemStack> pendingItems = new ArrayList<>();
    private double pendingValue;

    public SellSession(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Inventory getMainInventory() {
        return mainInventory;
    }

    public List<ItemStack> getPendingItems() {
        return pendingItems;
    }

    public double getPendingValue() {
        return pendingValue;
    }

    public void openMainInventory(SimpleSellGUIPlugin plugin, PriceResolver priceResolver) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        this.mainInventory = Bukkit.createInventory(new SellInventoryHolder(this, SellInventoryHolder.Type.MAIN), 54, ChatColor.GREEN + "Sell Items");
        updateSellButton(priceResolver);
        player.openInventory(mainInventory);
    }

    public void updateSellButton(PriceResolver resolver) {
        if (mainInventory == null) {
            return;
        }
        double total = calculateTotal(resolver);
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Sell Items");
            meta.setLore(List.of(ChatColor.YELLOW + "Value: " + ChatColor.GREEN + resolver.format(total),
                    ChatColor.GRAY + "Click to confirm sale"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            button.setItemMeta(meta);
        }
        mainInventory.setItem(SELL_BUTTON_SLOT, button);
    }

    public double calculateTotal(PriceResolver resolver) {
        double total = 0.0;
        if (mainInventory == null) {
            return 0.0;
        }
        for (int slot = 0; slot < mainInventory.getSize(); slot++) {
            if (slot == SELL_BUTTON_SLOT) {
                continue;
            }
            ItemStack stack = mainInventory.getItem(slot);
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            total += resolver.getPrice(stack);
        }
        return total;
    }

    public void preparePendingItems(PriceResolver resolver) {
        pendingItems = new ArrayList<>();
        pendingValue = 0.0;
        if (mainInventory == null) {
            return;
        }
        for (int slot = 0; slot < mainInventory.getSize(); slot++) {
            if (slot == SELL_BUTTON_SLOT) {
                continue;
            }
            ItemStack stack = mainInventory.getItem(slot);
            if (stack != null && !stack.getType().isAir()) {
                pendingItems.add(stack.clone());
                pendingValue += resolver.getPrice(stack);
                mainInventory.setItem(slot, null);
            }
        }
    }

    public void openConfirmation(SimpleSellGUIPlugin plugin) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        Inventory confirmInventory = Bukkit.createInventory(new SellInventoryHolder(this, SellInventoryHolder.Type.CONFIRM), 27, ChatColor.DARK_GREEN + "Confirm Sale");

        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
            confirmMeta.setLore(List.of(ChatColor.YELLOW + "Sell for: " + ChatColor.GREEN + plugin.getPriceResolver().format(pendingValue)));
            confirm.setItemMeta(confirmMeta);
        }

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
            cancelMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Return items"));
            cancel.setItemMeta(cancelMeta);
        }

        confirmInventory.setItem(11, confirm);
        confirmInventory.setItem(15, cancel);

        player.openInventory(confirmInventory);
    }

    public void returnItemsToPlayer() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        for (ItemStack item : pendingItems) {
            MapReturner.giveOrDrop(player, item);
        }
        pendingItems.clear();
    }
}
