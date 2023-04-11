package com.iridium.iridiumskyblock.database;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.IslandRank;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents people who are trusted by the island.
 */
@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "island_member")
public final class IslandMember extends IslandData {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, uniqueCombo = true)
    private int id;

    @DatabaseField(columnName = "user", canBeNull = false, uniqueCombo = true)
    private @NotNull UUID user;

    @DatabaseField(columnName = "island_rank")
    private @NotNull IslandRank islandRank;

    @DatabaseField(columnName = "time", canBeNull = false)
    private long time;

    /**
     * The default constructor.
     *
     * @param island  The Island this invite belongs to
     * @param user    The User who is invited
     * @param truster The User who invited the invitee
     */
    public IslandMember(@NotNull Island island, @NotNull User user, @NotNull IslandRank truster) {
        super(island);
        this.user = user.getUuid();
        this.islandRank = truster;
        this.time = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Returns the trusted user.
     *
     * @return The trusted user
     */
    public User getUser() {
        return IridiumSkyblock.getInstance().getUserManager().getUser(Bukkit.getOfflinePlayer(user));
    }
    /**
     * Returns the trusted user.
     *
     * @return The trusted user
     */
    public UUID getUserId() {
        return user;
    }

    /**
     * Returns the time this trust was created.
     *
     * @return The time the trust was created
     */
    public LocalDateTime getTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    public void setIslandRank(@NotNull IslandRank islandRank) {
        this.islandRank = islandRank;
        setChanged(true);
    }
}
