package com.iridium.iridiumskyblock.configs;

import com.google.common.collect.ImmutableMap;
import com.iridium.iridiumcore.Background;
import com.iridium.iridiumcore.Item;
import com.iridium.iridiumskyblock.biomes.BiomeCategory;
import com.iridium.iridiumskyblock.biomes.BiomeItem;
import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.moyskleytech.obsidian.material.dependencies.xseries.XBiome;
import com.moyskleytech.obsidian.material.dependencies.xseries.XMaterial;
import com.moyskleytech.obsidian.material.dependencies.xseries.XSound;
import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Biomes {

    public Map<String, BiomeCategoryConfig> categories = ImmutableMap.<String, BiomeCategoryConfig>builder()
            .put("Overworld", new BiomeCategoryConfig(new Item(ObsidianMaterial.wrap(XMaterial.GRASS_BLOCK), 11, 1, "&9&lOverworld", Collections.emptyList()), 6))
            .put("Nether", new BiomeCategoryConfig(new Item(ObsidianMaterial.wrap(XMaterial.CRIMSON_HYPHAE), 13, 1, "&9&lNether", Collections.emptyList()), 6))
            .put("End", new BiomeCategoryConfig(new Item(ObsidianMaterial.wrap(XMaterial.END_STONE), 15, 1, "&9&lEnd", Collections.emptyList()), 6))
            .build();

    public Map<String, List<BiomeItem>> biomes = ImmutableMap.<String, List<BiomeItem>>builder()
            .put("Overworld", Arrays.asList(
                    new BiomeItem(
                            "&9&lPlains",
                            "",
                            XBiome.PLAINS,
                            ObsidianMaterial.wrap(XMaterial.GRASS_BLOCK),
                            1,
                            1,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lForest",
                            "",
                            XBiome.FOREST,
                            ObsidianMaterial.wrap(XMaterial.OAK_SAPLING),
                            1,
                            4,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lSnowy Taiga",
                            "",
                            XBiome.SNOWY_TAIGA,
                            ObsidianMaterial.wrap(XMaterial.SPRUCE_WOOD),
                            1,
                            7,
                            new BiomeItem.BuyCost(1000, 15))))
            .put("Nether", Arrays.asList(
                    new BiomeItem(
                            "&9&lNether Wastes",
                            "",
                            XBiome.NETHER_WASTES,
                            ObsidianMaterial.wrap(XMaterial.NETHERRACK),
                            1,
                            1,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lCrimson Forest",
                            "",
                            XBiome.CRIMSON_FOREST,
                            ObsidianMaterial.wrap(XMaterial.CRIMSON_NYLIUM),
                            1,
                            4,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lWarped Forest",
                            "",
                            XBiome.WARPED_FOREST,
                            ObsidianMaterial.wrap(XMaterial.WARPED_NYLIUM),
                            1,
                            7,
                            new BiomeItem.BuyCost(1000, 15))))
            .put("End", Arrays.asList(
                    new BiomeItem(
                            "&9&lThe End",
                            "",
                            XBiome.THE_END,
                            ObsidianMaterial.wrap(XMaterial.END_CRYSTAL),
                            1,
                            1,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lEnd Barrens",
                            "",
                            XBiome.END_BARRENS,
                            ObsidianMaterial.wrap(XMaterial.END_STONE),
                            1,
                            4,
                            new BiomeItem.BuyCost(1000, 15)),
                    new BiomeItem(
                            "&9&lEnd Highlands",
                            "",
                            XBiome.END_HIGHLANDS,
                            ObsidianMaterial.wrap(XMaterial.CHORUS_FRUIT),
                            1,
                            7,
                            new BiomeItem.BuyCost(1000, 15))))
            .build();

    public XSound failSound = XSound.BLOCK_ANVIL_LAND;
    public XSound successSound = XSound.ENTITY_PLAYER_LEVELUP;

    public Background overviewBackground = new Background(ImmutableMap.<Integer, Item>builder().build());
    public Background categoryBackground = new Background(ImmutableMap.<Integer, Item>builder().build());

    public List<String> biomeItemLore = Arrays.asList(" ", "&b&l[!] &bLeft-Click to Purchase");

    public String overviewTitle = "&7Island Biome Shop";
    public String categoryTitle = "&7Island Biome Shop | %biomecategory_name%";
    public String buyPriceLore = "&aBuy Price: $%buy_price_vault%, %buy_price_crystals% Crystals";
    public String notPurchasableLore = "&cThis biome cannot be purchased!";

    public boolean abbreviatePrices = true;

    public int overviewSize = 3 * 9;

    /**
     * Represents configurable options of a {@link BiomeCategory}.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BiomeCategoryConfig {
        public Item item;
        public int inventoryRows;

    }

}