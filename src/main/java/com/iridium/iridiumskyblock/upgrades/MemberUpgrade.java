package com.iridium.iridiumskyblock.upgrades;

import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnore;
import com.iridium.iridiumcore.utils.Placeholder;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class MemberUpgrade extends UpgradeData {

    public int amount;

    public MemberUpgrade(int money, int crystals, int amount) {
        super(money, crystals, null, null);
        this.amount = amount;
    }

    public MemberUpgrade(int money, int crystals, int amount, String permission, String permissionMessage) {
        super(money, crystals, permission, permissionMessage);
        this.amount = amount;
    }

    @JsonIgnore
    @Override
    public List<Placeholder> getPlaceholders() {
        return Collections.singletonList(new Placeholder("amount", String.valueOf(amount)));
    }
}