package com.iridium.iridiumskyblock.upgrades;


import com.moyskleytech.obsidian.material.ObsidianMaterial;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class BlockLimitUpgrade extends UpgradeData {
    public Map<ObsidianMaterial, Integer> limits;

    public BlockLimitUpgrade(int money, int crystals, Map<ObsidianMaterial, Integer> limits) {
        super(money, crystals);
        this.limits = limits;
    }
}
