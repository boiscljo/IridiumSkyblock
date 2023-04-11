package com.iridium.iridiumskyblock;

import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandUpgrade;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.placeholders.Placeholders;

import me.clip.placeholderapi.PlaceholderAPI;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

public class PlaceholderBuilder {
    private final List<Placeholder> placeholderList = new ArrayList<>();

    public PlaceholderBuilder() {
        placeholderList.add(new Placeholder("prefix", IridiumSkyblock.getInstance().getConfiguration().prefix));
    }

    private static class PlaceholderAdapter extends Placeholder {
        private com.iridium.iridiumskyblock.placeholders.Placeholders.Placeholder placeholder;

        public PlaceholderAdapter(String key, Placeholders.Placeholder value) {
            super(key, null);
            this.placeholder = value;
        }

        @Override
        public String process(String line) {
            return new Placeholder(getKey(), placeholder.placeholderProcess(null)).process(line);
        }

    }

    public PlaceholderBuilder applyIslandPlaceholders(Island island) {
        IslandUpgrade islandUpgrade = IridiumSkyblock.getInstance().getIslandManager().getIslandUpgrade(island,
                "member");
        int memberLimit = IridiumSkyblock.getInstance().getUpgrades().memberUpgrade.upgrades
                .get(islandUpgrade.getLevel()).amount;

        placeholderList.addAll(Placeholders.getIslandPlaceholders("island", new Placeholders.IslandGetter() {
            @Override
            public Optional<Island> getIsland(Player player) {
                return Optional.ofNullable(island);
            }
        }).entrySet().stream().map(p -> new PlaceholderAdapter(p.getKey(), p.getValue())).toList());
        placeholderList.add(new Placeholder("island_members_limit",
                IridiumSkyblock.getInstance().getNumberFormatter().format(memberLimit)));
        placeholderList.add(new Placeholder("island_create", island.getCreateTime()
                .format(DateTimeFormatter.ofPattern(IridiumSkyblock.getInstance().getConfiguration().dateTimeFormat))));

        IridiumSkyblock.getInstance().getBlockValues().blockValues.keySet().stream()
                .map(material -> new Placeholder(material.name() + "_AMOUNT",
                        IridiumSkyblock.getInstance().getNumberFormatter()
                                .format(IridiumSkyblock.getInstance().getIslandManager().getIslandBlockAmount(island,
                                        material))))
                .forEach(placeholderList::add);

        IridiumSkyblock.getInstance().getBlockValues().spawnerValues.keySet().stream()
                .map(entity -> new Placeholder(entity.name() + "_AMOUNT",
                        IridiumSkyblock.getInstance().getNumberFormatter()
                                .format(IridiumSkyblock.getInstance().getIslandManager().getIslandSpawnerAmount(island,
                                        entity))))
                .forEach(placeholderList::add);
        return this;
    }

    public static class PapiPlacheolder extends Placeholder {

        private Player player;

        public PapiPlacheolder(Player p) {
            super("papi", null);
            this.player = p;
        }

        @Override
        public String process(String line) {
            try{
                return executePapi(line);
            }
            catch(Throwable t)
            {
                return line;
            }
        }

        private String executePapi(String line){
            return PlaceholderAPI.setPlaceholders(player, line);
        }

    }

    public PlaceholderBuilder papi(Player p) {
        placeholderList.add(new PapiPlacheolder(p));
        return this;
    }

    public PlaceholderBuilder applyPlayerPlaceholders(User user) {
        placeholderList.add(new Placeholder("player_name", user.getName()));
        placeholderList.add(new Placeholder("has_island",
                user.getIsland().isPresent() ? IridiumSkyblock.getInstance().getMessages().yes
                        : IridiumSkyblock.getInstance().getMessages().no));
        placeholderList.add(new Placeholder("player_rank", user.getCurrentIslandRank().getDisplayName()));
        placeholderList.add(new Placeholder("player_join", user.getJoinTime()
                .format(DateTimeFormatter.ofPattern(IridiumSkyblock.getInstance().getConfiguration().dateTimeFormat))));
        return this;
    }

    public List<Placeholder> build() {
        return placeholderList;
    }
}
