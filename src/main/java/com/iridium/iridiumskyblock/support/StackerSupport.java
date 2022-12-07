package com.iridium.iridiumskyblock.support;


import com.iridium.iridiumskyblock.database.Island;
import com.moyskleytech.obsidian.material.ObsidianMaterial;
import org.bukkit.entity.EntityType;

public interface StackerSupport {
    int getExtraBlocks(Island island, ObsidianMaterial material);

    int getExtraSpawners(Island island, EntityType entityType);
}
