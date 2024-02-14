package com.iridium.iridiumskyblock.listeners;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.PermissionType;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBlocks;
import com.iridium.iridiumskyblock.database.IslandSpawners;
import com.iridium.iridiumskyblock.database.User;
import org.apache.commons.lang.WordUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.BlockStateMeta;
import com.iridium.iridiumskyblock.upgrades.BlockLimitUpgrade;
import java.util.HashMap;

import java.util.Objects;
import java.util.Optional;

public class BlockPlaceListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlaceEvent(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager()
        .getIslandViaLocation(event.getBlock().getLocation());
    if (!island.isPresent()) {
      World world = event.getBlock().getLocation().getWorld();
      if (IridiumSkyblockAPI.getInstance().isIslandWorld(world)) {
        if (!user.isBypassing())
          event.setCancelled(true);
      }
      return;
    }

    ObsidianMaterial material = ObsidianMaterial.wrap(event.getBlock().getType());
    if (!IridiumSkyblock.getInstance().getIslandManager().getIslandPermission(island.get(), user,
        PermissionType.BLOCK_PLACE)) {
      event.setCancelled(true);
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().cannotPlaceBlocks
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
      return;
    }

    int limitUpgradeLevel = IridiumSkyblock.getInstance().getIslandManager()
        .getIslandUpgrade(island.get(), "blocklimit").getLevel();
    int blockLimit = IridiumSkyblock.getInstance().getUpgrades().blockLimitUpgrade.upgrades
        .getOrDefault(limitUpgradeLevel, new BlockLimitUpgrade(0, 0, new HashMap<>())).limits
        .getOrDefault(material, 0);

    if (blockLimit != 0 && IridiumSkyblock.getInstance().getIslandManager().getIslandBlockAmount(island.get(),
        material) >= blockLimit) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().blockLimitReached
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
          .replace("%limit%", String.valueOf(blockLimit < 0 ? 0 : blockLimit))
          .replace("%block%", WordUtils.capitalizeFully(material.name().toLowerCase().replace("_", " ")))));
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void monitorBlockPlace(BlockPlaceEvent event) {
    if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getBlock().getWorld()))
      return;

    Player player = event.getPlayer();
    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    ObsidianMaterial material = ObsidianMaterial.valueOf(event.getBlock().getType());

    user.getIsland().ifPresent(island -> {
      IridiumSkyblock.getInstance().getMissionManager().handleMissionUpdates(island, "PLACE", material.name(), 1);
    });

    IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getBlock().getLocation())
        .ifPresent(island -> {
          IslandBlocks islandBlocks = IridiumSkyblock.getInstance().getIslandManager().getIslandBlock(island,
              material);
          islandBlocks.setAmount(islandBlocks.getAmount() + 1);

          if (event.getBlock().getState() instanceof CreatureSpawner) {

            CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();

            if (IridiumSkyblock.getInstance().getConfiguration().basicSpawnerSupport)
              try {
                BlockStateMeta blockStateMeta = (BlockStateMeta) event.getItemInHand().getItemMeta();
                CreatureSpawner creatureSpawner_ = (CreatureSpawner) blockStateMeta.getBlockState();
                if (creatureSpawner.getSpawnedType() != creatureSpawner_.getSpawnedType()) {
                  creatureSpawner.setSpawnedType(creatureSpawner_.getSpawnedType());
                  creatureSpawner.update();
                }
              } catch (Throwable t) {
                t.printStackTrace();
              }

            try {
              IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager()
                  .getIslandSpawners(island, creatureSpawner.getSpawnedType());
              islandSpawners.setAmount(islandSpawners.getAmount() + 1);
            } catch (Throwable t) {
              t.printStackTrace();
            }
            IridiumSkyblock.getInstance().getIslandManager().getIslandSpawnerAmount(island,
                creatureSpawner.getSpawnedType());

          }
          IridiumSkyblock.getInstance().getIslandManager().getIslandBlockAmount(island, material);
        });
  }

}
