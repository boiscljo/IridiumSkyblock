package com.iridium.iridiumskyblock.upgrades;

import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class OresUpgrade extends UpgradeData {
    public Map<XMaterial, Integer> ores;
    public Map<XMaterial, Integer> netherOres;

    public OresUpgrade(int money, int crystals, Map<XMaterial, Integer> ores, Map<XMaterial, Integer> netherOres) {
        super(money, crystals, null, null);
        this.ores = ores;
        this.netherOres = netherOres;
    }

    public OresUpgrade(int money, int crystals, Map<XMaterial, Integer> ores, Map<XMaterial, Integer> netherOres,
            String permission, String permissionMessage) {
        super(money, crystals, permission, permissionMessage);
        this.ores = ores;
        this.netherOres = netherOres;
    }
}
