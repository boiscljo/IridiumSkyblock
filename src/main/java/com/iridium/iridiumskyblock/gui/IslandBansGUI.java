package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.gui.PagedGUI;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.PlaceholderBuilder;
import com.iridium.iridiumskyblock.configs.inventories.NoItemGUI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBan;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * Gui which displays all banned users of an Island
 */
public class IslandBansGUI extends PagedGUI<IslandBan> {

    private final Island island;
    @Getter
    private Player player;

    public IslandBansGUI(Player player,Island island, Inventory previousInventory) {
        super(1,
                IridiumSkyblock.getInstance().getInventories().bansGUI.size,
                IridiumSkyblock.getInstance().getInventories().bansGUI.background,
                IridiumSkyblock.getInstance().getInventories().previousPage,
                IridiumSkyblock.getInstance().getInventories().nextPage,
                previousInventory,
                IridiumSkyblock.getInstance().getInventories().backButton
        );
        this.player = player;
        this.island = island;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = IridiumSkyblock.getInstance().getInventories().bansGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<IslandBan> getPageObjects() {
        return IridiumSkyblock.getInstance().getDatabaseManager().getIslandBanTableManager().getEntries(island);
    }

    @Override
    public ItemStack getItemStack(IslandBan islandBan) {
        List<Placeholder> placeholderList = new PlaceholderBuilder().applyPlayerPlaceholders(islandBan.getBannedUser()).papi(islandBan.getBannedUser().getPlayer())
                .applyIslandPlaceholders(island)
                .build();
        placeholderList.add(new Placeholder("ban_time", islandBan.getBanTime().format(DateTimeFormatter.ofPattern(IridiumSkyblock.getInstance().getConfiguration().dateTimeFormat))));
        placeholderList.add(new Placeholder("banned_by", islandBan.getBanner().getName()));
        placeholderList.add(new PlaceholderBuilder.PapiPlacheolder(player));
        return ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getInventories().bansGUI.item, placeholderList);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
        IslandBan islandBan = getItem(event.getSlot());
        if (islandBan == null) return;
        IridiumSkyblock.getInstance().getCommands().promoteCommand.execute(event.getWhoClicked(), new String[]{"", islandBan.getBannedUser().getName()});
        addContent(event.getInventory());
    }
    
}
