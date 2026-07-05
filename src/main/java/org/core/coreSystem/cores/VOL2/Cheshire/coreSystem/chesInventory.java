package org.core.coreSystem.cores.VOL2.Cheshire.coreSystem;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.core.coreSystem.absInventorySystem.InventoryWrapper;
import org.core.coreSystem.absInventorySystem.absInventory;
import org.core.main.coreConfig;

import java.util.List;

public class chesInventory extends absInventory {
    public chesInventory(coreConfig tag) {
        super(tag);
    }

    @Override
    protected Plugin getPlugin() {
        return null;
    }

    @Override
    protected boolean contains(Player player) {
        return false;
    }

    @Override
    protected Material getMainTotem(Player player) {
        return null;
    }

    @Override
    protected Long getSkillLevel(Player player, String skill) {
        return 0L;
    }

    @Override
    protected Component getName(Player player, String skill) {
        return null;
    }

    @Override
    protected Material getTotem(Player player, String skill) {
        return null;
    }

    @Override
    protected List<Component> getTotemLore(Player player, String skill) {
        return List.of();
    }

    @Override
    protected void reinforceSkill(Player player, String skill, Long skillLevel, Inventory customInv) {

    }

    @Override
    protected InventoryWrapper getInventoryWrapper() {
        return null;
    }
}
