package com.iridium.iridiumskyblock.upgrades;


import com.moyskleytech.obsidian.material.ObsidianMaterial;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class OresUpgrade extends UpgradeData {
    public Map<ObsidianMaterial, Integer> ores;
    public Map<ObsidianMaterial, Integer> netherOres;
    public Map<ObsidianMaterial, Integer> endOres;

    public OresUpgrade(int money, int crystals, Map<ObsidianMaterial, Integer> ores, Map<ObsidianMaterial, Integer> netherOres, Map<ObsidianMaterial, Integer> endOres) {
        super(money, crystals,null);
        this.ores = ores;
        this.netherOres = netherOres;
    }
}
