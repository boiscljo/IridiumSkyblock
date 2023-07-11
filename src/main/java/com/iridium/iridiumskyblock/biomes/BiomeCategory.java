package com.iridium.iridiumskyblock.biomes;

import com.iridium.iridiumcore.Item;
import lombok.AllArgsConstructor;
import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Represents a category of items in the shop.
 */
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BiomeCategory {

    public String name;
    public Item item;
    public List<BiomeItem> items;
    public int size;

}