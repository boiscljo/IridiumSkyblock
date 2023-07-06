package com.iridium.iridiumskyblock.database;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Mission;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a mission of an Island.
 */
@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "island_mission")
public class IslandMission extends IslandData {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, uniqueCombo = true)
    private int id;

    @DatabaseField(columnName = "mission_name", uniqueCombo = true)
    private String missionName;

    @DatabaseField(columnName = "mission_index")
    private int missionIndex;

    @DatabaseField(columnName = "progress")
    private int progress;

    @DatabaseField(columnName = "type")
    private Mission.MissionType type;

    @DatabaseField(columnName = "last_update")
    private long lastUpdate;

    /**
     * The default constructor.
     *
     * @param island       The Island that has this mission
     * @param mission      The mission that is represented in the database
     * @param missionKey   The key of the mission
     * @param missionIndex The index of the mission
     */
    public IslandMission(@NotNull Island island, @NotNull Mission mission, @NotNull String missionKey, int missionIndex) {
        super(island);
        this.missionName = missionKey;
        this.type = mission.getMissionType();
        this.missionIndex = missionIndex;
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public @NotNull String getUniqueKey() {
        return missionName + "-" + missionIndex + "-" + getIslandId();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.lastUpdate = System.currentTimeMillis();
        setChanged(true);
    }

    public void setType(Mission.MissionType type) {
        this.type = type;
        this.lastUpdate = System.currentTimeMillis();
        setChanged(true);
    }

    public Mission getMission()
    {
      return IridiumSkyblock.getInstance().getMissionsList().get(missionName);
    }
}
