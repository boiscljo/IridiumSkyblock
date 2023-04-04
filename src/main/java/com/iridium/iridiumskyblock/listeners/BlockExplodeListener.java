package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.SettingType;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSetting;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import com.iridium.iridiumskyblock.database.User;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation())
                .ifPresent(island -> {
                    IslandSetting tntExplosion = IridiumSkyblock.getInstance().getIslandManager()
                            .getIslandSetting(island, SettingType.TNT_DAMAGE);
                    if (!tntExplosion.getBooleanValue()) {
                        event.setCancelled(true);
                        return;
                    }
                    event.blockList().removeIf(block -> !island.isInIsland(block.getLocation()));
                });
    }

    private Map<BlockExplodeEvent, Map<Location, BlockState>> state = new HashMap<>();

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onBlockPreExplode(BlockExplodeEvent event) {
        Map<Location, BlockState> explodeState = new HashMap<>();
        event.blockList().forEach(block -> {
            System.out.println("[Preexplode]Block " + block.getLocation() + " exploded");
            explodeState.put(block.getLocation(), block.getState());
        });
        state.put(event, explodeState);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void monitorBlockBreak(BlockExplodeEvent event) {
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getBlock().getWorld()))
            return;
        if (!event.isCancelled())
            event.blockList().forEach(explodedBlock -> {
                BlockState block = state.get(event).get(explodedBlock.getLocation());
                System.out.println("Block " + block.getLocation() + " exploded");
                IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(block.getLocation())
                        .ifPresent(island -> {
                            ObsidianMaterial material = ObsidianMaterial.wrap(block.getType());
                            System.out.println("Block " + material + " exploded in island");
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
            });
        state.remove(event);
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandBlocksTableManager().save();
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandSpawnersTableManager().save();
    }

}
