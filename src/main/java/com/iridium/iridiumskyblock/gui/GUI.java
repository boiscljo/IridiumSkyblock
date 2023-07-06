package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.configs.inventories.NoItemGUI;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a clickable GUI.
 * Base for all other classes in this package.
 */
@NoArgsConstructor
@Getter
public abstract class GUI implements InventoryHolder {

    private NoItemGUI noItemGUI;
    private Inventory previousInventory;
    @Getter
    private Player player;

    public GUI(Player player,Inventory previousInventory){
        this.player= player;
        if (previousInventory != null && previousInventory.getHolder() != null && previousInventory.getHolder() instanceof GUI) {
            this.previousInventory = previousInventory;
        }
    }

    /**
     * The default constructor.
     *
     * @param noItemGUI         The NoItemGUI of this GUI
     * @param previousInventory The previous Inventory
     */
    public GUI(Player player,@NotNull NoItemGUI noItemGUI, Inventory previousInventory) {
        this.player = player;
        this.noItemGUI = noItemGUI;
        if (previousInventory != null && previousInventory.getHolder() != null && previousInventory.getHolder() instanceof GUI) {
            this.previousInventory = previousInventory;
        }
    }

    /**
     * The default constructor.
     *
     * @param noItemGUI The NoItemGUI of this GUI
     */
    public GUI(Player player,@NotNull NoItemGUI noItemGUI) {
        this.player = player;
        this.noItemGUI = noItemGUI;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));

        Bukkit.getScheduler().runTaskAsynchronously(IridiumSkyblock.getInstance(), () -> addContent(inventory));

        return inventory;
    }

    /**
     * Called when there is a click in this GUI.
     * Cancelled automatically.
     *
     * @param event The InventoryClickEvent provided by Bukkit
     */
    public abstract void onInventoryClick(InventoryClickEvent event);

    /**
     * Called when updating the Inventories contents
     */
    public abstract void addContent(Inventory inventory);

    public boolean needRefresh() {
      return false;
    }
}
