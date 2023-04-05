package com.iridium.iridiumskyblock.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

public class IslandBlockUtils {
    public static void blockRemoved(Block explodedBlock) {
        blockRemoved(explodedBlock.getState());
    }
    public static void blockRemoved(BlockState block) {
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(block.getLocation())
                .ifPresent(island -> {
                    ObsidianMaterial material = ObsidianMaterial.wrap(block.getType());
                    IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(
                            island,
                            material);
                    if (islandBlocks.getAmount() > 0) {
                        islandBlocks.setAmount(islandBlocks.getAmount() - 1);
                    }
                    if (block instanceof CreatureSpawner) {
                        CreatureSpawner creatureSpawner = (CreatureSpawner) block;
                        try {
                            IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager()
                                    .getIslandSpawners(island, creatureSpawner.getSpawnedType());
                            if (islandSpawners.getAmount() > 0) {
                                islandSpawners.setAmount(islandSpawners.getAmount() - 1);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        if (IridiumSkyblock.getInstance().getConfiguration().dropSpawners) {
                            block.getWorld().dropItem(block.getLocation(), ObsidianMaterial
                                    .valueOf(creatureSpawner.getSpawnedType() + "_SPAWNER").toItem());
                        }
                    }
                });
    }

    public static void blockAdded(BlockData blockData, Location l) {
        BlockState bs = l.getBlock().getState();
        bs.setType(blockData.getMaterial());
        blockAdded(bs);
    }
    public static void blockAdded(BlockState block) {
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(block.getLocation())
                .ifPresent(island -> {
                    ObsidianMaterial material = ObsidianMaterial.wrap(block.getType());
                    IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(
                            island,
                            material);
                    if (islandBlocks.getAmount() > 0) {
                        islandBlocks.setAmount(islandBlocks.getAmount() - 1);
                    }
                    if (block instanceof CreatureSpawner) {
                        CreatureSpawner creatureSpawner = (CreatureSpawner) block;
                        try {
                            IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager()
                                    .getIslandSpawners(island, creatureSpawner.getSpawnedType());
                            if (islandSpawners.getAmount() > 0) {
                                islandSpawners.setAmount(islandSpawners.getAmount() - 1);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        if (IridiumSkyblock.getInstance().getConfiguration().dropSpawners) {
                            block.getWorld().dropItem(block.getLocation(), ObsidianMaterial
                                    .valueOf(creatureSpawner.getSpawnedType() + "_SPAWNER").toItem());
                        }
                    }
                });
    }
}
