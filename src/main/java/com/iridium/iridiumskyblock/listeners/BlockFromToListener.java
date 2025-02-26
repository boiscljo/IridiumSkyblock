package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.SettingType;
import com.iridium.iridiumskyblock.database.IslandSetting;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (IridiumSkyblock.getInstance().getConfiguration().performance.disableWaterCheck) return;

        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation()).ifPresent(island -> {
            if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.LAVA) {
                if(!island.isInIsland(event.getToBlock().getLocation())){
                    event.setCancelled(true);
                }
                IslandSetting liquidFlowSettings = IridiumSkyblock.getInstance().getIslandManager().getIslandSetting(island, SettingType.LIQUID_FLOW);
                if (!liquidFlowSettings.getBooleanValue()) {
                    event.setCancelled(true);
                }
            }
        });
    }

}
