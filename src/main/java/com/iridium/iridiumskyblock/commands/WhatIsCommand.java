package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command which display plugin information to the user.
 */
public class WhatIsCommand extends Command {

  /*
   * Please don't add yourself to this list, if you contribute enough I (Peaches)
   * will add you.
   */
  private final List<String> contributors = Arrays.asList("boiscljo", "das_", "SlashRemix", "DoctaEnkoda");

  /**
   * The default constructor.
   */
  public WhatIsCommand() {
    super(Collections.singletonList("whatis"), "Display plugin info", "", false, Duration.ZERO);
  }

  /**
   * Executes the command for the specified {@link CommandSender} with the
   * provided arguments.
   * Not called when the command execution was invalid (no permission, no player
   * or command disabled).
   * Display plugin information to the user.
   *
   * @param sender The CommandSender which executes this command
   * @param args   The arguments used with this command. They contain the
   *               sub-command
   */
  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Player player = (Player) sender;
    Block b = player.getTargetBlock(null, 10);
    if (b != null) {
      ObsidianMaterial m = ObsidianMaterial.match(b);
      if (m != null)
        sender.sendMessage(m.getKey());
      else
        sender.sendMessage("Unknown bock");
    } else
      sender.sendMessage("You're not looking at a block");

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
    // We currently don't want to tab-completion here
    // Return a new List, so it isn't a list of online players
    return Collections.emptyList();
  }

}
