package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Mission;
import com.iridium.iridiumskyblock.PlaceholderBuilder;
import com.iridium.iridiumskyblock.database.Island;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DailyIslandMissionsGUI extends IslandGUI {

  /**
   * The default constructor.
   *
   * @param island The Island this GUI belongs to
   */
  public DailyIslandMissionsGUI(Player player, @NotNull Island island, Inventory previousInventory) {
    super(player, IridiumSkyblock.getInstance().getInventories().dailyMissionGUI, previousInventory, island);
  }

  @Override
  public void addContent(Inventory inventory) {
    inventory.clear();
    InventoryUtils.fillInventory(inventory, IridiumSkyblock.getInstance().getInventories().missionsGUI.background);

    Map<String, Mission> missions = IridiumSkyblock.getInstance().getIslandManager()
        .getDailyIslandMissions(getIsland());
    int i = 0;

    for (Map.Entry<String, Mission> entry : missions.entrySet()) {
      List<Placeholder> placeholders = new ArrayList<>(IntStream.range(0, entry.getValue().getMissions().size())
          .boxed()
          .map(integer -> IridiumSkyblock.getInstance().getIslandManager().getIslandMission(getIsland(),
              entry.getValue(), entry.getKey(), integer))
          .map(islandMission -> new Placeholder("progress_" + (islandMission.getMissionIndex() + 1),
              String.valueOf(islandMission.getProgress())))
          .collect(Collectors.toList()));
      placeholders.add(new PlaceholderBuilder.PapiPlacheolder(getPlayer()));

      if (IridiumSkyblock.getInstance().getMissions().dailySlots.size() > i) {
        Integer slot = IridiumSkyblock.getInstance().getMissions().dailySlots.get(i);
        if (entry.getValue().getItem().slot == null)
          inventory.setItem(slot, ItemStackUtils.makeItem(entry.getValue().getItem(), placeholders));
        i++;
      }
      if (entry.getValue().getItem().slot != null)
        inventory.setItem(entry.getValue().getItem().slot.intValue(),
            ItemStackUtils.makeItem(entry.getValue().getItem(), placeholders));
    }

    if (IridiumSkyblock.getInstance().getConfiguration().backButtons && getPreviousInventory() != null) {
      inventory.setItem(inventory.getSize() + IridiumSkyblock.getInstance().getInventories().backButton.slot,
          ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getInventories().backButton,
              new PlaceholderBuilder().papi(getPlayer()).build()));
    }
  }

  @Override
  public boolean needRefresh() {
    return true;
  }

  /**
   * Called when there is a click in this GUI.
   * Cancelled automatically.
   *
   * @param event The InventoryClickEvent provided by Bukkit
   */
  @Override
  public void onInventoryClick(InventoryClickEvent event) {
    // Do nothing here
  }

}
