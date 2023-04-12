package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.gui.PagedGUI;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.PlaceholderBuilder;
import com.iridium.iridiumskyblock.configs.inventories.NoItemGUI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * GUI which displays all members of an Island and allows quick rank management.
 */
public class UserMembershipGUI extends PagedGUI<Island> {

    private final User user;
    @Getter
    private Player player;

    public UserMembershipGUI(Player player, Island island, Inventory previousInventory) {
        super(1,
                IridiumSkyblock.getInstance().getInventories().membershipGUI.size,
                IridiumSkyblock.getInstance().getInventories().membershipGUI.background,
                IridiumSkyblock.getInstance().getInventories().previousPage,
                IridiumSkyblock.getInstance().getInventories().nextPage,
                previousInventory,
                IridiumSkyblock.getInstance().getInventories().backButton);
        this.player = player;
        this.user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = IridiumSkyblock.getInstance().getInventories().membershipGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<Island> getPageObjects() {
        return user.getMemberships().stream().map(x -> x.getIsland()).filter(i -> i.isPresent()).map(i -> i.get())
                .toList();
    }

    @Override
    public ItemStack getItemStack(Island user) {
        return ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getInventories().membershipGUI.item,
                new PlaceholderBuilder()
                        .papi(getPlayer())
                        .applyIslandPlaceholders(user)
                        .applyPlayerPlaceholders(this.user)
                        .build());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
        Island isl = getItem(event.getSlot());
        if (isl == null)
            return;
        switch (event.getClick()) {
            case LEFT:

                IridiumSkyblock.getInstance().getCommands().mainIslandCommand.execute(event.getWhoClicked(),
                        new String[] { "", String.valueOf(isl.getId()) });
                break;
        }
        addContent(event.getInventory());
    }
}