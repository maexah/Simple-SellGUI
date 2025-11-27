package com.example.simplesellgui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SellMenuListener implements Listener {

    private final SimpleSellGUIPlugin plugin;

    public SellMenuListener(SimpleSellGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof SellInventoryHolder sellHolder)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        SellSession session = sellHolder.getSession();

        if (!player.getUniqueId().equals(session.getPlayerId())) {
            return;
        }

        if (sellHolder.getType() == SellInventoryHolder.Type.MAIN) {
            handleMainClick(event, session);
        } else {
            handleConfirmClick(event, session);
        }
    }

    private void handleMainClick(InventoryClickEvent event, SellSession session) {
        Inventory clicked = event.getClickedInventory();
        if (clicked != null && clicked.equals(event.getView().getTopInventory()) && event.getSlot() == SellSession.SELL_BUTTON_SLOT) {
            event.setCancelled(true);
            double total = session.calculateTotal(plugin.getPriceResolver());
            if (total <= 0) {
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "Place items in the inventory to sell.");
                return;
            }
            session.preparePendingItems(plugin.getPriceResolver());
            session.openConfirmation(plugin);
            return;
        }

        event.setCancelled(false);
        // Allow item placement and removal, then refresh the sell button
        Bukkit.getScheduler().runTask(plugin, () -> session.updateSellButton(plugin.getPriceResolver()));
    }

    private void handleConfirmClick(InventoryClickEvent event, SellSession session) {
        event.setCancelled(true);
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot == 11) {
            completeSale(player, session);
        } else if (slot == 15) {
            session.returnItemsToPlayer();
            plugin.removeSession(player.getUniqueId());
            player.closeInventory();
        }
    }

    private void completeSale(Player player, SellSession session) {
        double amount = session.getPendingValue();
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Nothing to sell.");
            session.returnItemsToPlayer();
            plugin.removeSession(player.getUniqueId());
            player.closeInventory();
            return;
        }

        session.getPendingItems().clear();
        if (plugin.deposit(player, amount)) {
            player.sendMessage(ChatColor.GREEN + "Sold items for " + ChatColor.GOLD + plugin.getPriceResolver().format(amount));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to deposit funds. Contact an administrator.");
            session.returnItemsToPlayer();
        }

        plugin.removeSession(player.getUniqueId());
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof SellInventoryHolder sellHolder)) {
            return;
        }

        SellSession session = sellHolder.getSession();
        if (!event.getPlayer().getUniqueId().equals(session.getPlayerId())) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (sellHolder.getType() == SellInventoryHolder.Type.MAIN) {
            // Return any items that were left in the sell inventory
            Inventory inv = session.getMainInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (i == SellSession.SELL_BUTTON_SLOT) {
                    continue;
                }
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    MapReturner.giveOrDrop(player, item.clone());
                }
                inv.setItem(i, null);
            }
        } else {
            if (!session.getPendingItems().isEmpty()) {
                session.returnItemsToPlayer();
            }
        }

        plugin.removeSession(player.getUniqueId());
    }
}
