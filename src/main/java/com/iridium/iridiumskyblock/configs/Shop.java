package com.iridium.iridiumskyblock.configs;

import com.google.common.collect.ImmutableMap;
import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumcore.Background;
import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnoreProperties;

import com.moyskleytech.obsidian.material.dependencies.xseries.XSound;
import com.iridium.iridiumskyblock.shop.ShopItem;
import com.iridium.iridiumskyblock.shop.ShopItem.BuyCost;
import com.iridium.iridiumskyblock.shop.ShopItem.SellReward;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The shop configuration used by IridiumSkyblock (shop.yml).
 * Is deserialized automatically on plugin startup and reload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shop {

    public Map<String, ShopCategoryConfig> categories = ImmutableMap.<String, ShopCategoryConfig>builder()
            .put("Blocks", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("GRASS_BLOCK"), 12, 1, "&9&lBlocks", Collections.emptyList()), 6))
            .put("Food", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("COOKED_CHICKEN"), 13, 1, "&9&lFood", Collections.emptyList()), 4))
            .put("Ores", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("GOLD_INGOT"), 14, 1, "&9&lOres", Collections.emptyList()), 4))
            .put("Farming", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("WHEAT"), 21, 1, "&9&lFarming", Collections.emptyList()), 5))
            .put("Mob Drops", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("SPIDER_EYE"), 22, 1, "&9&lMob Drops", Collections.emptyList()), 5))
            .put("Miscellaneous", new ShopCategoryConfig(new Item(ObsidianMaterial.valueOf("SADDLE"), 23, 1, "&9&lMiscellaneous", Collections.emptyList()), 4))
            .build();

    public Map<String, List<ShopItem>> items = ImmutableMap.<String, List<ShopItem>>builder()
            .put("Blocks", Arrays.asList(
                    new ShopItem(
                            "&9&lGrass Block",
                            "",
                            ObsidianMaterial.valueOf("GRASS_BLOCK"),
                            1,
                            10,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lDirt Block",
                            "",
                            ObsidianMaterial.valueOf("DIRT"),
                            10,
                            11,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lGravel",
                            "",
                            ObsidianMaterial.valueOf("GRAVEL"),
                            10,
                            12,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lGranite",
                            "",
                            ObsidianMaterial.valueOf("GRANITE"),
                            10,
                            13,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lDiorite",
                            "",
                            ObsidianMaterial.valueOf("DIORITE"),
                            10,
                            14,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lAndesite",
                            "",
                            ObsidianMaterial.valueOf("ANDESITE"),
                            10,
                            15,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lOak Log",
                            "",
                            ObsidianMaterial.valueOf("OAK_LOG"),
                            16,
                            16,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lSpruce Log",
                            "",
                            ObsidianMaterial.valueOf("SPRUCE_LOG"),
                            16,
                            19,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lBirch Log",
                            "",
                            ObsidianMaterial.valueOf("BIRCH_LOG"),
                            16,
                            20,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lJungle Log",
                            "",
                            ObsidianMaterial.valueOf("JUNGLE_LOG"),
                            16,
                            21,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lAcacia Log",
                            "",
                            ObsidianMaterial.valueOf("ACACIA_LOG"),
                            16,
                            22,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lDark Oak Log",
                            "",
                            ObsidianMaterial.valueOf("DARK_OAK_LOG"),
                            16,
                            23,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lSnow Block",
                            "",
                            ObsidianMaterial.valueOf("SNOW_BLOCK"),
                            16,
                            24,
                            new BuyCost(200, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lIce",
                            "",
                            ObsidianMaterial.valueOf("ICE"),
                            8,
                            25,
                            new BuyCost(300, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lPacked Ice",
                            "",
                            ObsidianMaterial.valueOf("PACKED_ICE"),
                            8,
                            28,
                            new BuyCost(300, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lSponge",
                            "",
                            ObsidianMaterial.valueOf("SPONGE"),
                            4,
                            29,
                            new BuyCost(1000, 0),
                            new SellReward(200, 0)
                    ),
                    new ShopItem(
                            "&9&lSand",
                            "",
                            ObsidianMaterial.valueOf("SAND"),
                            8,
                            30,
                            new BuyCost(100, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lSandstone",
                            "",
                            ObsidianMaterial.valueOf("SANDSTONE"),
                            16,
                            31,
                            new BuyCost(80, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lClay Ball",
                            "",
                            ObsidianMaterial.valueOf("CLAY_BALL"),
                            32,
                            32,
                            new BuyCost(70, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lObsidian",
                            "",
                            ObsidianMaterial.valueOf("OBSIDIAN"),
                            4,
                            33,
                            new BuyCost(250, 0),
                            new SellReward(25, 0)
                    ),
                    new ShopItem(
                            "&9&lGlowstone",
                            "",
                            ObsidianMaterial.valueOf("GLOWSTONE"),
                            8,
                            34,
                            new BuyCost(125, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lEnd Stone",
                            "",
                            ObsidianMaterial.valueOf("END_STONE"),
                            4,
                            39,
                            new BuyCost(250, 0),
                            new SellReward(25, 0)
                    ),
                    new ShopItem(
                            "&9&lPrismarine",
                            "",
                            ObsidianMaterial.valueOf("PRISMARINE"),
                            16,
                            40,
                            new BuyCost(200, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lWool",
                            "",
                            ObsidianMaterial.valueOf("WHITE_WOOL"),
                            8,
                            41,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    )
            ))
            .put("Food", Arrays.asList(
                    new ShopItem(
                            "&9&lApple",
                            "",
                            ObsidianMaterial.valueOf("APPLE"),
                            10,
                            11,
                            new BuyCost(50, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lEnchanted Golden Apple",
                            "",
                            ObsidianMaterial.valueOf("ENCHANTED_GOLDEN_APPLE"),
                            3,
                            12,
                            new BuyCost(1000, 0),
                            new SellReward(100, 0)
                    ),
                    new ShopItem(
                            "&9&lCarrot",
                            "",
                            ObsidianMaterial.valueOf("CARROT"),
                            10,
                            13,
                            new BuyCost(100, 0),
                            new SellReward(25, 0)
                    ),
                    new ShopItem(
                            "&9&lBaked Potato",
                            "",
                            ObsidianMaterial.valueOf("BAKED_POTATO"),
                            10,
                            14,
                            new BuyCost(150, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lBread",
                            "",
                            ObsidianMaterial.valueOf("BREAD"),
                            10,
                            15,
                            new BuyCost(50, 0),
                            new SellReward(30, 0)
                    ),
                    new ShopItem(
                            "&9&lCookie",
                            "",
                            ObsidianMaterial.valueOf("COOKIE"),
                            5,
                            20,
                            new BuyCost(130, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lCooked Porkchop",
                            "",
                            ObsidianMaterial.valueOf("COOKED_PORKCHOP"),
                            10,
                            21,
                            new BuyCost(100, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lCooked Beef",
                            "",
                            ObsidianMaterial.valueOf("COOKED_BEEF"),
                            10,
                            22,
                            new BuyCost(100, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lCooked Mutton",
                            "",
                            ObsidianMaterial.valueOf("COOKED_MUTTON"),
                            10,
                            23,
                            new BuyCost(100, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lCooked Rabbit",
                            "",
                            ObsidianMaterial.valueOf("COOKED_RABBIT"),
                            10,
                            24,
                            new BuyCost(100, 0),
                            new SellReward(25, 0)
                    )
                    )
            )
            .put("Ores", Arrays.asList(
                    new ShopItem(
                            "&9&lCoal",
                            "",
                            ObsidianMaterial.valueOf("COAL"),
                            16,
                            11,
                            new BuyCost(100, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lRedstone",
                            "",
                            ObsidianMaterial.valueOf("REDSTONE"),
                            16,
                            12,
                            new BuyCost(150, 0),
                            new SellReward(7, 0)
                    ),
                    new ShopItem(
                            "&9&lLapis Lazuli",
                            "",
                            ObsidianMaterial.valueOf("LAPIS_LAZULI"),
                            16,
                            13,
                            new BuyCost(150, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lIron Ingot",
                            "",
                            ObsidianMaterial.valueOf("IRON_INGOT"),
                            8,
                            14,
                            new BuyCost(200, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lGold Ingot",
                            "",
                            ObsidianMaterial.valueOf("GOLD_INGOT"),
                            8,
                            15,
                            new BuyCost(200, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lDiamond",
                            "",
                            ObsidianMaterial.valueOf("DIAMOND"),
                            8,
                            21,
                            new BuyCost(1000, 0),
                            new SellReward(100, 0)
                    ),
                    new ShopItem(
                            "&9&lEmerald",
                            "",
                            ObsidianMaterial.valueOf("EMERALD"),
                            8,
                            22,
                            new BuyCost(200, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lQuartz",
                            "",
                            ObsidianMaterial.valueOf("QUARTZ"),
                            64,
                            23,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    )
                    )
            )
            .put("Farming", Arrays.asList(
                    new ShopItem(
                            "&9&lWheat Seeds",
                            "",
                            ObsidianMaterial.valueOf("WHEAT_SEEDS"),
                            16,
                            10,
                            new BuyCost(130, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lPumpkin Seeds",
                            "",
                            ObsidianMaterial.valueOf("PUMPKIN_SEEDS"),
                            16,
                            11,
                            new BuyCost(150, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lMelon Seeds",
                            "",
                            ObsidianMaterial.valueOf("MELON_SEEDS"),
                            16,
                            12,
                            new BuyCost(250, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lNether Wart",
                            "",
                            ObsidianMaterial.valueOf("NETHER_WART"),
                            4,
                            13,
                            new BuyCost(100, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lSugar Cane",
                            "",
                            ObsidianMaterial.valueOf("SUGAR_CANE"),
                            16,
                            14,
                            new BuyCost(150, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lWheat",
                            "",
                            ObsidianMaterial.valueOf("WHEAT"),
                            16,
                            15,
                            new BuyCost(50, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lPumpkin",
                            "",
                            ObsidianMaterial.valueOf("PUMPKIN"),
                            16,
                            16,
                            new BuyCost(150, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lMelon Slice",
                            "",
                            ObsidianMaterial.valueOf("MELON_SLICE"),
                            16,
                            19,
                            new BuyCost(150, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lCactus",
                            "",
                            ObsidianMaterial.valueOf("CACTUS"),
                            8,
                            20,
                            new BuyCost(80, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lOak Sapling",
                            "",
                            ObsidianMaterial.valueOf("OAK_SAPLING"),
                            4,
                            21,
                            new BuyCost(20, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lSpruce Sapling",
                            "",
                            ObsidianMaterial.valueOf("SPRUCE_SAPLING"),
                            4,
                            22,
                            new BuyCost(20, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lBirch Sapling",
                            "",
                            ObsidianMaterial.valueOf("BIRCH_SAPLING"),
                            4,
                            23,
                            new BuyCost(20, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lJungle Sapling",
                            "",
                            ObsidianMaterial.valueOf("JUNGLE_SAPLING"),
                            4,
                            24,
                            new BuyCost(150, 0),
                            new SellReward(4, 0)
                    ),
                    new ShopItem(
                            "&9&lAcacia Sapling",
                            "",
                            ObsidianMaterial.valueOf("ACACIA_SAPLING"),
                            4,
                            25,
                            new BuyCost(20, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lDark Oak Sapling",
                            "",
                            ObsidianMaterial.valueOf("DARK_OAK_SAPLING"),
                            4,
                            30,
                            new BuyCost(150, 0),
                            new SellReward(4, 0)
                    ),
                    new ShopItem(
                            "&9&lBrown Mushroom",
                            "",
                            ObsidianMaterial.valueOf("BROWN_MUSHROOM"),
                            8,
                            31,
                            new BuyCost(60, 0),
                            new SellReward(6, 0)
                    ),
                    new ShopItem(
                            "&9&lRed Mushroom",
                            "",
                            ObsidianMaterial.valueOf("RED_MUSHROOM"),
                            8,
                            32,
                            new BuyCost(60, 0),
                            new SellReward(6, 0)
                    )
                    )
            )
            .put("Mob Drops", Arrays.asList(
                    new ShopItem(
                            "&9&lRotten Flesh",
                            "",
                            ObsidianMaterial.valueOf("ROTTEN_FLESH"),
                            16,
                            10,
                            new BuyCost(20, 0),
                            new SellReward(2, 0)
                    ),
                    new ShopItem(
                            "&9&lBone",
                            "",
                            ObsidianMaterial.valueOf("BONE"),
                            16,
                            11,
                            new BuyCost(100, 0),
                            new SellReward(3, 0)
                    ),
                    new ShopItem(
                            "&9&lGunpowder",
                            "",
                            ObsidianMaterial.valueOf("GUNPOWDER"),
                            16,
                            12,
                            new BuyCost(30, 0),
                            new SellReward(3, 0)
                    ),
                    new ShopItem(
                            "&9&lString",
                            "",
                            ObsidianMaterial.valueOf("STRING"),
                            16,
                            13,
                            new BuyCost(80, 0),
                            new SellReward(3, 0)
                    ),
                    new ShopItem(
                            "&9&lArrow",
                            "",
                            ObsidianMaterial.valueOf("ARROW"),
                            16,
                            14,
                            new BuyCost(75, 0),
                            new SellReward(4, 0)
                    ),
                    new ShopItem(
                            "&9&lSpider Eye",
                            "",
                            ObsidianMaterial.valueOf("SPIDER_EYE"),
                            16,
                            15,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lEnder Pearl",
                            "",
                            ObsidianMaterial.valueOf("ENDER_PEARL"),
                            3,
                            16,
                            new BuyCost(75, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lSlime Ball",
                            "",
                            ObsidianMaterial.valueOf("SLIME_BALL"),
                            16,
                            19,
                            new BuyCost(200, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lPrismarine Crystals",
                            "",
                            ObsidianMaterial.valueOf("PRISMARINE_CRYSTALS"),
                            16,
                            20,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lPrismarine Shard",
                            "",
                            ObsidianMaterial.valueOf("PRISMARINE_SHARD"),
                            16,
                            21,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lBlaze Rod",
                            "",
                            ObsidianMaterial.valueOf("BLAZE_ROD"),
                            4,
                            22,
                            new BuyCost(250, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lMagma Cream",
                            "",
                            ObsidianMaterial.valueOf("MAGMA_CREAM"),
                            4,
                            23,
                            new BuyCost(150, 0),
                            new SellReward(15, 0)
                    ),
                    new ShopItem(
                            "&9&lGhast Tear",
                            "",
                            ObsidianMaterial.valueOf("GHAST_TEAR"),
                            4,
                            24,
                            new BuyCost(200, 0),
                            new SellReward(30, 0)
                    ),
                    new ShopItem(
                            "&9&lLeather",
                            "",
                            ObsidianMaterial.valueOf("LEATHER"),
                            8,
                            25,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lRabbit Foot",
                            "",
                            ObsidianMaterial.valueOf("RABBIT_FOOT"),
                            4,
                            30,
                            new BuyCost(250, 0),
                            new SellReward(30, 0)
                    ),
                    new ShopItem(
                            "&9&lInk Sack",
                            "",
                            ObsidianMaterial.valueOf("INK_SAC"),
                            8,
                            31,
                            new BuyCost(50, 0),
                            new SellReward(5, 0)
                    ),
                    new ShopItem(
                            "&9&lFeather",
                            "",
                            ObsidianMaterial.valueOf("FEATHER"),
                            16,
                            32,
                            new BuyCost(30, 0),
                            new SellReward(3, 0)
                    )
                    )
            )
            .put("Miscellaneous", Arrays.asList(
                    new ShopItem(
                            "&9&lBucket",
                            "",
                            ObsidianMaterial.valueOf("BUCKET"),
                            1,
                            12,
                            new BuyCost(100, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lWater Bucket",
                            "",
                            ObsidianMaterial.valueOf("WATER_BUCKET"),
                            1,
                            13,
                            new BuyCost(200, 0),
                            new SellReward(10, 0)
                    ),
                    new ShopItem(
                            "&9&lLava Bucket",
                            "",
                            ObsidianMaterial.valueOf("LAVA_BUCKET"),
                            1,
                            14,
                            new BuyCost(200, 0),
                            new SellReward(20, 0)
                    ),
                    new ShopItem(
                            "&9&lName Tag",
                            "",
                            ObsidianMaterial.valueOf("NAME_TAG"),
                            1,
                            21,
                            new BuyCost(200, 0),
                            new SellReward(30, 0)
                    ),
                    new ShopItem(
                            "&9&lSaddle",
                            "",
                            ObsidianMaterial.valueOf("SADDLE"),
                            1,
                            22,
                            new BuyCost(300, 0),
                            new SellReward(30, 0)
                    ),
                    new ShopItem(
                            "&9&lEnd Portal Frame",
                            "",
                            ObsidianMaterial.valueOf("END_PORTAL_FRAME"),
                            null,
                            Arrays.asList("&5&lVisit the end!", " "),
                            null,
                            1,
                            23,
                            new BuyCost(5000, 50),
                            new SellReward(0, 0),null
                    )
                    )
            )
            .build();

    public String overviewTitle = "&7Island Shop";
    public String categoryTitle = "&7Island Shop | %category_name%";
    public String buyPriceLore = "&aBuy Price: $%buy_price_vault%, %buy_price_crystals% Crystals";
    public String sellRewardLore = "&cSelling Reward: $%sell_reward_vault%, %sell_reward_crystals% Crystals";
    public String notPurchasableLore = "&cThis item cannot be purchased!";
    public String notSellableLore = "&cThis item cannot be sold!";

    public boolean abbreviatePrices = true;
    public boolean dropItemWhenFull = false;

    public int overviewSize = 4 * 9;

    public XSound failSound = XSound.BLOCK_ANVIL_LAND;
    public XSound successSound = XSound.ENTITY_PLAYER_LEVELUP;

    public Background overviewBackground = new Background(ImmutableMap.<Integer, Item>builder().build());
    public Background categoryBackground = new Background(ImmutableMap.<Integer, Item>builder().build());

    public List<String> shopItemLore = Arrays.asList(" ", "&b&l[!] &bLeft-Click to Purchase %amount%, Shift for 64", "&b&l[!] &bRight Click to Sell %amount%, Shift for 64");

    /**
     * Represents configurable options of a {@link com.iridium.iridiumskyblock.shop.ShopCategory}.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopCategoryConfig {
        public Item item;
        public int inventoryRows;

    }

}
