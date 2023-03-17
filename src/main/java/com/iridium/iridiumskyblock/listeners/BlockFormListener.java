package com.iridium.iridiumskyblock.listeners;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.upgrades.OresUpgrade;
import com.iridium.iridiumskyblock.utils.RandomAccessList;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlockFormListener implements Listener {

    private static final Map<Integer, RandomAccessList<ObsidianMaterial>> normalOreLevels = new HashMap<>();
    private static final Map<Integer, RandomAccessList<ObsidianMaterial>> netherOreLevels = new HashMap<>();

    public static void generateOrePossibilities() {
        for (Map.Entry<Integer, OresUpgrade> oreUpgrade : IridiumSkyblock.getInstance()
                .getUpgrades().oresUpgrade.upgrades.entrySet()) {
            normalOreLevels.put(oreUpgrade.getKey(), new RandomAccessList<>(oreUpgrade.getValue().ores));
            netherOreLevels.put(oreUpgrade.getKey(), new RandomAccessList<>(oreUpgrade.getValue().netherOres));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getBlock().getWorld())) return;
        if (IridiumSkyblock.getInstance().getConfiguration().performance.disableGenerator) return;

        ObsidianMaterial newMaterial = ObsidianMaterial.valueOf(event.getNewState().getType());

        boolean overworld_gen = newMaterial == ObsidianMaterial.valueOf("COBBLESTONE") || newMaterial == ObsidianMaterial.valueOf("STONE");
        boolean nether_gen = newMaterial == ObsidianMaterial.valueOf("BASALT") ;
        // Custom basalt generators should only work in nether, unless it's not forced
        if (IridiumSkyblock.getInstance().getConfiguration().forceNetherGeneratorInNether)
            nether_gen = nether_gen&& event.getBlock().getLocation().getWorld().getEnvironment() == World.Environment.NETHER;
        
        if (overworld_gen || nether_gen) {
            Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getNewState().getLocation());
            if (island.isPresent()) {
                int upgradeLevel = IridiumSkyblock.getInstance().getIslandManager().getIslandUpgrade(island.get(), "generator").getLevel();
                RandomAccessList<ObsidianMaterial> randomMaterialList = newMaterial == ObsidianMaterial.valueOf("BASALT") ? netherOreLevels.get(upgradeLevel) : normalOreLevels.get(upgradeLevel);
                if (randomMaterialList == null) return;

                Optional<ObsidianMaterial> ObsidianMaterialOptional = randomMaterialList.nextElement();
                if (!ObsidianMaterialOptional.isPresent()) return;

                Material material = ObsidianMaterialOptional.get().toMaterial();
                if (material == Material.COBBLESTONE && newMaterial == ObsidianMaterial.valueOf("STONE")) material = Material.STONE;
                if (material != null) event.getNewState().setType(material);
            }
        }
    }

}
