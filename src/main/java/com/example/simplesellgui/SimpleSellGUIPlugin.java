package com.example.simplesellgui;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleSellGUIPlugin extends JavaPlugin implements TabExecutor {

    private Object economy;
    private Class<?> economyClass;
    private PriceResolver priceResolver;
    private final ConcurrentMap<UUID, SellSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault economy provider not found. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        priceResolver = new PriceResolver(this);

        getCommand("sellgui").setExecutor(this);
        getCommand("sellgui").setTabCompleter(this);

        Bukkit.getPluginManager().registerEvents(new SellMenuListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("simplesellgui.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        openSellMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    public void openSellMenu(Player player) {
        SellSession session = new SellSession(player.getUniqueId());
        sessions.put(player.getUniqueId(), session);
        session.openMainInventory(this, priceResolver);
    }

    public SellSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public PriceResolver getPriceResolver() {
        return priceResolver;
    }

    public boolean deposit(Player player, double amount) {
        if (economy == null || economyClass == null) {
            return false;
        }

        try {
            var method = economyClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class);
            Object response = method.invoke(economy, player, amount);

            if (response != null) {
                try {
                    var successMethod = response.getClass().getMethod("transactionSuccess");
                    Object result = successMethod.invoke(response);
                    if (result instanceof Boolean bool) {
                        return bool;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Treat missing method as success fallback.
                }
            }

            return true;
        } catch (ReflectiveOperationException ex) {
            getLogger().warning("Failed to call Vault depositPlayer: " + ex.getMessage());
            return false;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        try {
            economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            return false;
        }

        @SuppressWarnings("unchecked")
        RegisteredServiceProvider<?> rsp = getServer().getServicesManager().getRegistration((Class) economyClass);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
