package org.patryk3211.hungergames.loot;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.patryk3211.hungergames.HungerGamesPlugin;

import java.io.File;
import java.util.*;

public class LootConfig {
    private final FileConfiguration configuration;

    private static class ItemDef {
        public final ItemStack stack;
        public final int cost;

        public ItemDef(ItemStack stack, int cost) {
            this.stack = stack;
            this.cost = cost;
        }
    }

    private final List<ItemDef> items = new ArrayList<>();

    public LootConfig(File config) {
        configuration = YamlConfiguration.loadConfiguration(config);
        configuration.getConfigurationSection("items");
        final List<Map<?, ?>> defs = configuration.getMapList("items");
        for(Map<?, ?> map : defs) {
            try {
                String itemName = (String) map.get("item");
                int itemAmount = (int) map.get("amount");
                ItemStack stack = new ItemStack(Material.getMaterial(itemName), itemAmount);
                int rarity = (int) map.get("rarity");
                int cost = (int) map.get("cost");

                ItemDef entry = new ItemDef(stack, cost);
                for(int i = 0; i < rarity; ++i)
                    items.add(entry);
            } catch (ClassCastException | NullPointerException e) {
                HungerGamesPlugin.LOG.error(e.getMessage());
            }
        }

        HungerGamesPlugin.LOG.info("Loot table size: " + items.size());
    }

    public void fillChest(Chest chest, int targetCost) {
        Inventory inv = chest.getInventory();

        int triesLeft = 15;
        int currentCost = 0;

        List<ItemStack> fillItems = new LinkedList<>();
        while(currentCost < targetCost && triesLeft-- > 0) {
            ItemDef def = items.get(HungerGamesPlugin.manager.random.nextInt(items.size()));
            if(targetCost - currentCost >= def.cost) {
                fillItems.add(def.stack);
                currentCost += def.cost;
            }
        }

        Collections.shuffle(fillItems);
        while(!fillItems.isEmpty()) {
            ItemStack item = fillItems.get(0);
            int slotIdx = HungerGamesPlugin.manager.random.nextInt(3 * 9);
            ItemStack currentItem = inv.getItem(slotIdx);
            if(currentItem == null || currentItem.isEmpty()) {
                inv.setItem(slotIdx, item.clone());
                fillItems.remove(0);
            }
        }
    }
}
