package org.patryk3211.hungergames.loot;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.patryk3211.hungergames.HungerGamesPlugin;

import java.io.File;
import java.util.*;

public class LootConfig {
    private final FileConfiguration configuration;

    private record ItemDef(ItemStack stack, int cost) { }

    private final List<ItemDef> items = new ArrayList<>();

    public LootConfig(File config) {
        configuration = YamlConfiguration.loadConfiguration(config);
        configuration.getConfigurationSection("items");
        final List<Map<?, ?>> defs = configuration.getMapList("items");
        for(Map<?, ?> map : defs) {
            try {
                String itemName = (String) map.get("item");
                int itemAmount = (int) map.get("amount");
                final Material mat = Material.getMaterial(itemName);
                if(mat == null) {
                    HungerGamesPlugin.LOG.error("Material is null, provided name: " + itemName);
                    continue;
                }
                ItemStack stack = new ItemStack(mat, itemAmount);
                int rarity = (int) map.get("rarity");
                int cost = (int) map.get("cost");
                if(map.containsKey("damage")) {
                    ItemMeta meta = stack.getItemMeta();
                    if(meta instanceof Damageable damageable) {
                        damageable.setDamage((int) map.get("damage"));
                        stack.setItemMeta(meta);
                    } else {
                        HungerGamesPlugin.LOG.warn("Item " + itemName + " is not damageable");
                    }
                }

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
        fillInventory(chest.getInventory(), targetCost);
    }

    public void fillContainer(Container container, int targetCost) {
        fillInventory(container.getInventory(), targetCost);
    }

    public void fillInventory(Inventory inv, int targetCost) {
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
