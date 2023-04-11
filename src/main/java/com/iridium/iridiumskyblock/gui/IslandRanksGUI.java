package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.IslandRank;
import com.iridium.iridiumskyblock.PlaceholderBuilder;
import com.iridium.iridiumskyblock.configs.inventories.IslandRanksInventoryConfig;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandMember;
import com.iridium.iridiumskyblock.database.User;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI which allows users to select ranks to edit in the
 * {@link IslandPermissionsGUI}.
 */
public class IslandRanksGUI extends IslandGUI {

    /**
     * The default constructor.
     *
     * @param island The Island this GUI belongs to
     */
    public IslandRanksGUI(Player player, @NotNull Island island, Inventory previousInventory) {
        super(player, IridiumSkyblock.getInstance().getInventories().islandRanksGUI, previousInventory, island);
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        IslandRanksInventoryConfig islandRanks = IridiumSkyblock.getInstance().getInventories().islandRanksGUI;
        InventoryUtils.fillInventory(inventory, islandRanks.background);
        List<IslandMember> members = IridiumSkyblock.getInstance().getIslandManager().getIslandMembers(getIsland());
        PlaceholderBuilder.PapiPlacheolder papi = new PlaceholderBuilder.PapiPlacheolder(getPlayer());

        inventory.setItem(islandRanks.owner.slot, ItemStackUtils.makeItem(islandRanks.owner,
                List.of(papi, new Placeholder("members", getIsland().getOwner().getName()))));
        inventory.setItem(islandRanks.coOwner.slot, ItemStackUtils.makeItem(islandRanks.coOwner,
                List.of(papi,
                        new Placeholder("members",
                                members.stream().filter(member -> member.getIslandRank().equals(IslandRank.CO_OWNER))
                                        .map(member->member.getUser().getName()).collect(Collectors.joining(", "))))));
        inventory.setItem(islandRanks.moderator.slot, ItemStackUtils.makeItem(islandRanks.moderator,
                List.of(papi,
                        new Placeholder("members",
                                members.stream().filter(member -> member.getIslandRank().equals(IslandRank.MODERATOR))
                                        .map(member->member.getUser().getName()).collect(Collectors.joining(", "))))));
        inventory.setItem(islandRanks.member.slot, ItemStackUtils.makeItem(islandRanks.member,
                List.of(papi,
                        new Placeholder("members",
                                members.stream().filter(member -> member.getIslandRank().equals(IslandRank.MEMBER))
                                        .map(member->member.getUser().getName()).collect(Collectors.joining(", "))))));
        inventory.setItem(islandRanks.visitor.slot,
                ItemStackUtils.makeItem(islandRanks.visitor, new PlaceholderBuilder().papi(getPlayer()).build()));

        if (IridiumSkyblock.getInstance().getConfiguration().backButtons && getPreviousInventory() != null) {
            inventory.setItem(inventory.getSize() + IridiumSkyblock.getInstance().getInventories().backButton.slot,
                    ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getInventories().backButton,
                            new PlaceholderBuilder().papi(getPlayer()).build()));
        }
    }

    /**
     * Called when there is a click in this GUI.
     * Cancelled automatically.
     *
     * @param event The InventoryClickEvent provided by Bukkit
     */

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        IslandRanksInventoryConfig islandRanks = IridiumSkyblock.getInstance().getInventories().islandRanksGUI;
        if (event.getSlot() == islandRanks.owner.slot)
            event.getWhoClicked().openInventory(
                    new IslandPermissionsGUI(getPlayer(), getIsland(), IslandRank.OWNER, event.getClickedInventory(), 1)
                            .getInventory());
        else if (event.getSlot() == islandRanks.coOwner.slot)
            event.getWhoClicked().openInventory(new IslandPermissionsGUI(getPlayer(), getIsland(), IslandRank.CO_OWNER,
                    event.getClickedInventory(), 1).getInventory());
        else if (event.getSlot() == islandRanks.moderator.slot)
            event.getWhoClicked().openInventory(new IslandPermissionsGUI(getPlayer(), getIsland(), IslandRank.MODERATOR,
                    event.getClickedInventory(), 1).getInventory());
        else if (event.getSlot() == islandRanks.member.slot)
            event.getWhoClicked().openInventory(new IslandPermissionsGUI(getPlayer(), getIsland(), IslandRank.MEMBER,
                    event.getClickedInventory(), 1).getInventory());
        else if (event.getSlot() == islandRanks.visitor.slot)
            event.getWhoClicked().openInventory(new IslandPermissionsGUI(getPlayer(), getIsland(), IslandRank.VISITOR,
                    event.getClickedInventory(), 1).getInventory());

    }

}