package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.SettingType;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSetting;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import com.iridium.iridiumskyblock.utils.IslandBlockUtils;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class EntityExplodeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        List<MetadataValue> list = event.getEntity().getMetadata("island_spawned");
        if (list.isEmpty()) return;
        int islandId = list.get(0).asInt();
        IridiumSkyblock.getInstance().getIslandManager().getIslandById(islandId).ifPresent(island -> {
            IslandSetting tntExplosion = IridiumSkyblock.getInstance().getIslandManager().getIslandSetting(island, SettingType.TNT_DAMAGE);
            if (!tntExplosion.getBooleanValue()) {
                event.setCancelled(true);
                return;
            }

            if (!island.isInIsland(event.getLocation())) {
                event.setCancelled(true);
                return;
            }

            event.blockList().removeIf(block -> !island.isInIsland(block.getLocation()));
        });
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void monitorBlockBreak(EntityExplodeEvent event) {
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getEntity().getWorld()))
            return;
        if (!event.isCancelled())
            event.blockList().forEach(explodedBlock -> {
                IslandBlockUtils.blockRemoved(explodedBlock);
            });
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandBlocksTableManager().save();
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandSpawnersTableManager().save();
    }

}
