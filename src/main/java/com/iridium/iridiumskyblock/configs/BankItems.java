package com.iridium.iridiumskyblock.configs;

import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnoreProperties;

import com.iridium.iridiumskyblock.bank.CrystalsBankItem;
import com.iridium.iridiumskyblock.bank.ExperienceBankItem;
import com.iridium.iridiumskyblock.bank.MoneyBankItem;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import java.util.Arrays;

/**
 * The bank item configuration used by IridiumSkyblock (bankitems.yml).
 * Is deserialized automatically on plugin startup and reload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankItems {

    public CrystalsBankItem crystalsBankItem = new CrystalsBankItem(10, new Item(ObsidianMaterial.valueOf("NETHER_STAR"), 13, 1, "&b&lIsland Crystals", Arrays.asList("&7%amount% Crystals", "&b&l[!] &bLeft click to withdraw", "&b&l[!] &bRight click to deposit")));
    public ExperienceBankItem experienceBankItem = new ExperienceBankItem(10, new Item(ObsidianMaterial.valueOf("EXPERIENCE_BOTTLE"), 15, 1, "&b&lIsland Experience", Arrays.asList("&7%amount% Experience", "&b&l[!] &bLeft click to withdraw", "&b&l[!] &bRight click to deposit")));
    public MoneyBankItem moneyBankItem = new MoneyBankItem(1000, new Item(ObsidianMaterial.valueOf("PAPER"), 11, 1, "&b&lIsland Money", Arrays.asList("&7$%amount%", "&b&l[!] &bLeft click to withdraw", "&b&l[!] &bRight click to deposit")));

}
