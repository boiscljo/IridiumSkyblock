package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.*;
import com.iridium.iridiumskyblock.gui.UserMembershipGUI;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Command which invites a user to the Island.
 */
public class ProfileCommand extends Command {
  /**
   * The default constructor.
   */
  public ProfileCommand() {
    super(Arrays.asList("mainisland", "profile", "p"), "Switch the main island",
        "%prefix% &7/is mainisland <islandid>", "",
        true, Duration.ZERO);
  }

  /**
   * Executes the command for the specified {@link CommandSender} with the
   * provided arguments.
   * Not called when the command execution was invalid (no permission, no player
   * or command disabled).
   * Invites a user to the Island.
   *
   * @param sender The CommandSender which executes this command
   * @param args   The arguments used with this command. They contain the
   *               sub-command
   */
  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Player player = (Player) sender;
    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);

    if (args.length == 1) {
      Inventory previousInventory = IridiumSkyblock.getInstance().getConfiguration().backButtons ? player.getOpenInventory().getTopInventory() : null;
      player.openInventory(new UserMembershipGUI(player, previousInventory).getInventory());
      
      return true;
    }

    try {
      int islandId = Integer.valueOf(args[1]);
      if (islandId == 0 || user.getMemberships().stream()
          .anyMatch(membership -> membership.getIslandId() == islandId)) {
        if (islandId == 0) {// Since it technically create a new profile/island
          if (user.getMemberships().size() >= IridiumSkyblock.getInstance()
              .getConfiguration().maxIslandPerPlayer) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance()
                .getMessages().alreadyHaveIsland
                .replace("%prefix%", IridiumSkyblock.getInstance()
                    .getConfiguration().prefix)));
            return false;
          }
          user.setIsland(null);
        } else
          user.setIsland(new Island(islandId));

        player.sendMessage(StringUtils
            .color(IridiumSkyblock.getInstance().getMessages().noIslandFound
                .replace("%prefix%", IridiumSkyblock.getInstance()
                    .getConfiguration().prefix)));
        IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().save(user);
      }
    } catch (Throwable t) {
      player.sendMessage(StringUtils
          .color(IridiumSkyblock.getInstance().getMessages().noIslandFound
              .replace("%prefix%", IridiumSkyblock.getInstance()
                  .getConfiguration().prefix)));
      return false;
    }
    return true;
  }

  /**
   * Handles tab-completion for this command.
   *
   * @param commandSender The CommandSender which tries to tab-complete
   * @param command       The command
   * @param label         The label of the command
   * @param args          The arguments already provided by the sender
   * @return The list of tab completions for this command
   */
  @Override
  public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label,
      String[] args) {
    List<String> possibilities = new ArrayList<>();
    possibilities.add("0");
    Player player = (Player) commandSender;
    IridiumSkyblock.getInstance().getUserManager().getUser(player).getMemberships().forEach(membership -> {
      membership.getIsland().ifPresent(island -> possibilities.add(String.valueOf(island.getId())));
    });
    return possibilities;
  }

}
