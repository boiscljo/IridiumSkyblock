package com.iridium.iridiumskyblock.generators;


import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.utils.LocationUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import java.util.Objects;
import java.util.Random;

/**
 * Class which handles the {@link World} generation of IridiumSkyblock.
 * Creates an ocean world.
 */
public class OceanGenerator extends IridiumChunkGenerator {

    /**
     * Generates an ocean.
     *
     * @param world     The world that we try to generate
     * @param random    A random which should be used. We don't need this
     * @param chunkX    The x position of the chunk
     * @param chunkZ    The y position of the chunk
     * @param biomeGrid The biome grid of the chunk
     * @return The data of this chunk. None all the time to generate void
     */
    @Override
    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biomeGrid) {
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        final ChunkData chunkData = createChunkData(world);
        generator.setScale(0.005D);

        ObsidianMaterial bottomMaterial = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.oceanFloorBottomMaterial;
        ObsidianMaterial topMaterial = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.oceanFloorTopMaterial;
        int waterHeight = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.waterHeight;
        int maxOceanFloorLevel = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.maxOceanFloorLevel;
        int minOceanFloorLevel = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.minOceanFloorLevel;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int currentFloorHeight = (int) ((generator.noise(chunkX * 16 + x, chunkZ * 16 + z, 1.5D, 0.5D, true) + 1) * (maxOceanFloorLevel - minOceanFloorLevel) + minOceanFloorLevel);

                // Generate layer of bedrock
                chunkData.setBlock(x, LocationUtils.getMinHeight(world), z,
                        Objects.requireNonNull(Material.BEDROCK)
                );

                // Generate gravel layer
                for (int y = LocationUtils.getMinHeight(world) + 1; y < currentFloorHeight; y++) {
                    chunkData.setBlock(x, y, z,
                            Objects.requireNonNull(bottomMaterial.toMaterial())
                    );
                }

                // Generate sand on top of gravel
                chunkData.setBlock(x, currentFloorHeight, z,
                        Objects.requireNonNull(topMaterial.toMaterial())
                );

                // Generate water or lava on top of the floor
                ObsidianMaterial oceanMaterial = world.getEnvironment() == Environment.NETHER ? ObsidianMaterial.valueOf("LAVA") : ObsidianMaterial.valueOf("WATER");
                for (int y = currentFloorHeight + 1; y <= waterHeight; y++) {
                    chunkData.setBlock(x, y, z, Objects.requireNonNull(oceanMaterial.toMaterial()));
                }
            }
        }

        return chunkData;
    }

    public void generateWater(World world, int x, int z) {
        if(world==null) return;
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        generator.setScale(0.005D);

        ObsidianMaterial bottomMaterial = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.oceanFloorBottomMaterial;
        ObsidianMaterial topMaterial = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.oceanFloorTopMaterial;
        int waterHeight = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.waterHeight;
        int maxOceanFloorLevel = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.maxOceanFloorLevel;
        int minOceanFloorLevel = IridiumSkyblock.getInstance().getConfiguration().generatorSettings.minOceanFloorLevel;

        int currentFloorHeight = (int) ((generator.noise(x, z, 1.5D, 0.5D, true) + 1) * (maxOceanFloorLevel - minOceanFloorLevel) + minOceanFloorLevel);
        int minHeightWorld = LocationUtils.getMinHeight(world);

        // Generate layer of bedrock
        if (world.getBlockAt(x, minHeightWorld, z).getType() != ObsidianMaterial.valueOf("BEDROCK").toMaterial()) {
            if (world.getBlockAt(x, minHeightWorld, z).getState() instanceof InventoryHolder) {
                ((InventoryHolder) world.getBlockAt(x, minHeightWorld, z).getState()).getInventory().clear();
            }
            world.getBlockAt(x, minHeightWorld, z).setType(Material.BEDROCK, false);
        }

        // Generate gravel layer
        for (int y = minHeightWorld + 1; y < currentFloorHeight; y++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != bottomMaterial.toMaterial() && bottomMaterial.toMaterial() != null) {
                if (block.getState() instanceof InventoryHolder) {
                    ((InventoryHolder) block.getState()).getInventory().clear();
                }
                block.setType(bottomMaterial.toMaterial(), false);
            }
        }

        // Generate sand on top of gravel
        if (world.getBlockAt(x, currentFloorHeight, z).getType() != topMaterial.toMaterial() && topMaterial.toMaterial() != null) {
            if (world.getBlockAt(x, currentFloorHeight, z).getState() instanceof InventoryHolder) {
                ((InventoryHolder) world.getBlockAt(x, currentFloorHeight, z).getState()).getInventory().clear();
            }
            world.getBlockAt(x, currentFloorHeight, z).setType(topMaterial.toMaterial(), false);
        }

        // Generate water or lava on top of the floor
        ObsidianMaterial oceanMaterial = world.getEnvironment() == Environment.NETHER ? ObsidianMaterial.valueOf("LAVA") : ObsidianMaterial.valueOf("WATER");
        for (int y = currentFloorHeight + 1; y <= waterHeight; y++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != oceanMaterial.toMaterial() && oceanMaterial.toMaterial() != null) {
                if (block.getState() instanceof InventoryHolder) {
                    ((InventoryHolder) block.getState()).getInventory().clear();
                }
                block.setType(oceanMaterial.toMaterial(), false);
            }
        }

        // Replace everything else with air
        for (int y = waterHeight + 1; y < world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != Material.AIR) {
                if (block.getState() instanceof InventoryHolder) {
                    ((InventoryHolder) block.getState()).getInventory().clear();
                }
                block.setType(Material.AIR, false);
            }
        }
    }

    /**
     * Tells Bukkit that we always want entities to spawn.
     *
     * @param world The world the entity wants to spawn in
     * @param x     The x location of the spawning attempt
     * @param z     The z location of the spawning attempt
     * @return true because we always want entities to spawn
     */
    @Override
    public boolean canSpawn(@NotNull World world, int x, int z) {
        return true;
    }

    /**
     * Returns what a made with this generator is mainly consisting of.<p>
     * Used for performance improvements.
     *
     * @param world the world that should be checked
     * @return the most used material of the chunk generator in this generator
     */
    @Override
    public ObsidianMaterial getMainMaterial(@Nullable World world) {
        if (world == null) return ObsidianMaterial.valueOf("WATER");

        return world.getEnvironment() == Environment.NETHER ? ObsidianMaterial.valueOf("LAVA") : ObsidianMaterial.valueOf("WATER");
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return null;
    }

}
