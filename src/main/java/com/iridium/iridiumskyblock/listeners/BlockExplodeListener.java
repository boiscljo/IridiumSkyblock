package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.SettingType;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSetting;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import com.iridium.iridiumskyblock.database.User;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation()).ifPresent(island -> {
            IslandSetting tntExplosion = IridiumSkyblock.getInstance().getIslandManager().getIslandSetting(island, SettingType.TNT_DAMAGE);
            if (!tntExplosion.getBooleanValue()) {
                event.setCancelled(true);
                return;
            }
            event.blockList().removeIf(block -> !island.isInIsland(block.getLocation()));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockBreak(BlockExplodeEvent event) {
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getBlock().getWorld()))
            return;

        Block block = event.getBlock();
        ObsidianMaterial material = ObsidianMaterial.wrap(block.getType());
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation())
                .ifPresent(island -> {
                    IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(island,
                            material);
                    if (islandBlocks.getAmount() > 0) {
                        islandBlocks.setAmount(islandBlocks.getAmount() - 1);
                    }
                    if (event.getBlock().getState() instanceof CreatureSpawner) {
                        CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();
                        try {
                            IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager()
                                    .getIslandSpawners(island, creatureSpawner.getSpawnedType());
                            if (islandSpawners.getAmount() > 0) {
                                islandSpawners.setAmount(islandSpawners.getAmount() - 1);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        if(IridiumSkyblock.getInstance().getConfiguration().dropSpawners)
                        {
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(),ObsidianMaterial.valueOf(creatureSpawner.getSpawnedType()+"_SPAWNER").toItem());
                        }
                    }
                });
    }

}
