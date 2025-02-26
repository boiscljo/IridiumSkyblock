package com.iridium.iridiumskyblock.support;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class RoseStackerSupport implements StackerSupport {

  @Override
  public int getExtraBlocks(Island island, ObsidianMaterial material) {
    //IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(island, material);
    int stackedBlocks = 0;
    List<Location> locations = new ArrayList<>();
    for (StackedBlock stackedBlock : RoseStackerAPI.getInstance().getStackedBlocks().values()) {
      if (!island.isInIsland(stackedBlock.getLocation()))
        continue;
      if (locations.contains(stackedBlock.getLocation()))
        continue;
      if (material != ObsidianMaterial.valueOf(stackedBlock.getBlock().getType()))
        continue;
      locations.add(stackedBlock.getLocation());
      if (material == ObsidianMaterial.valueOf(stackedBlock.getBlock().getType())) {
        stackedBlocks += (stackedBlock.getStackSize() - 1);
      }
    }
    //islandBlocks.setExtraAmount(stackedBlocks);
    return stackedBlocks;
  }

  @Override
  public int getExtraSpawners(Island island, EntityType entityType) {
    //IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager().getIslandSpawners(island,
    //    entityType);
    int stackedSpawners = 0;
    for (StackedSpawner stackedSpawner : RoseStackerAPI.getInstance().getStackedSpawners().values()) {
      if (!island.isInIsland(stackedSpawner.getLocation()))
        continue;
      if (stackedSpawner.getSpawner().getSpawnedType() != entityType)
        continue;
      stackedSpawners += (stackedSpawner.getStackSize() - 1);
    }
    //islandSpawners.setExtraAmount(stackedSpawners);
    return stackedSpawners;
  }
}
