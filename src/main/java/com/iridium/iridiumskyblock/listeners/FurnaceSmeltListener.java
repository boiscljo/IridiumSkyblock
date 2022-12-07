package com.iridium.iridiumskyblock.listeners;


import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

import java.util.Optional;

public class FurnaceSmeltListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorFurnaceSmelt(FurnaceSmeltEvent event) {
        Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation());
        ObsidianMaterial material = ObsidianMaterial.valueOf(event.getSource().getType());

        island.ifPresent(value -> IridiumSkyblock.getInstance().getMissionManager().handleMissionUpdates(value, "SMELT", material.name(), 1));
    }

}
