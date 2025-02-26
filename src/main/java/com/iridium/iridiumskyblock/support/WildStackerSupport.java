package com.iridium.iridiumskyblock.support;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import org.bukkit.entity.EntityType;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

public class WildStackerSupport implements StackerSupport {

    @Override
    public int getExtraBlocks(Island island, ObsidianMaterial material) {
        //IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(island, material);
        int stackedBlocks = 0;
        for (StackedBarrel stackedBarrel : WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrels()) {
            if (!island.isInIsland(stackedBarrel.getLocation())) continue;
            if (material != ObsidianMaterial.valueOf(stackedBarrel.getType())) continue;

            if (material == ObsidianMaterial.valueOf(stackedBarrel.getType())) {
                stackedBlocks += stackedBarrel.getStackAmount();
            }
        }
        //islandBlocks.setExtraAmount(stackedBlocks);
        return stackedBlocks;
    }

    @Override
    public int getExtraSpawners(Island island, EntityType entityType) {
        //IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager().getIslandSpawners(island, entityType);
        int stackedSpawners = 0;
        for (StackedSpawner stackedSpawner : WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners()) {
            if (!island.isInIsland(stackedSpawner.getLocation())) continue;
            if (stackedSpawner.getSpawnedType() != entityType) continue;
            stackedSpawners += stackedSpawner.getStackAmount();
        }
        //islandSpawners.setExtraAmount(stackedSpawners);
        return stackedSpawners;
    }
}
