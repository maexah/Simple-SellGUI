package com.example.simplesellgui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class PriceResolver {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");

    private final Map<Material, Double> prices = new HashMap<>();
    private final Logger logger;

    public PriceResolver(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        File worthFile = new File(plugin.getDataFolder().getParentFile(), "Essentials/worth.yml");
        loadPrices(worthFile);
    }

    private void loadPrices(File worthFile) {
        if (!worthFile.exists()) {
            logger.warning("Essentials worth.yml was not found at " + worthFile.getPath());
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(worthFile);
        ConfigurationSection section = config.getConfigurationSection("worth");
        if (section == null) {
            section = config;
        }

        for (String key : section.getKeys(false)) {
            double price = section.getDouble(key, Double.NaN);
            if (Double.isNaN(price)) {
                continue;
            }

            Material material = matchMaterial(key);
            if (material != null) {
                prices.put(material, price);
            }
        }
        logger.info("Loaded " + prices.size() + " item prices from Essentials worth.yml");
    }

    private Material matchMaterial(String key) {
        String normalized = key;
        if (key.contains(":")) {
            normalized = key.substring(0, key.indexOf(":"));
        }
        Material material = Material.matchMaterial(normalized.toUpperCase(Locale.ENGLISH));
        if (material == null) {
            material = Material.matchMaterial(normalized.toLowerCase(Locale.ENGLISH));
        }
        return material;
    }

    public double getPrice(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return 0.0;
        }
        Double base = prices.get(stack.getType());
        if (base == null) {
            return 0.0;
        }
        return base * stack.getAmount();
    }

    public String format(double amount) {
        return PRICE_FORMAT.format(amount);
    }
}
