package com.iridium.iridiumskyblock.commands;


import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.PermissionType;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandWarp;
import com.iridium.iridiumskyblock.database.User;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditWarpCommand extends Command {

    /**
     * The default constructor.
     */
    public EditWarpCommand() {
        super(Collections.singletonList("editwarp"), "Edits an Island warp", "%prefix% &7/is editwarp <name> <icon/description>", "", true, Duration.ZERO);
    }

    /**
     * Executes the command for the specified {@link CommandSender} with the provided arguments.
     * Not called when the command execution was invalid (no permission, no player or command disabled).
     * Shows an overview over the members of the Island and allows quick rank management.
     *
     * @param sender The CommandSender which executes this command
     * @param args   The arguments used with this command. They contain the sub-command
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(StringUtils.color(syntax.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            return false;
        }

        Player player = (Player) sender;
        User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
        Optional<Island> island = user.getIsland();
        if (!island.isPresent()) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().noIsland.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            return false;
        }

        if (!IridiumSkyblock.getInstance().getIslandManager().getIslandPermission(island.get(), IridiumSkyblock.getInstance().getUserManager().getUser(player), PermissionType.MANAGE_WARPS)) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().cannotManageWarps.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            return false;
        }

        List<IslandWarp> islandWarps = IridiumSkyblock.getInstance().getDatabaseManager().getIslandWarpTableManager().getEntries(island.get());
        Optional<IslandWarp> islandWarp = islandWarps.stream().filter(warp -> warp.getName().equalsIgnoreCase(args[1])).findFirst();
        if (!islandWarp.isPresent()) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().unknownWarp.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            return false;
        }

        //TODO: Use subcommand system
        switch (args[2]) {
            case "icon":
                if (args.length != 4) {
                    sender.sendMessage("/is editwarp <name> icon <icon>");
                }

                ObsidianMaterial xMaterial = ObsidianMaterial.valueOf(args[3]);
                if (xMaterial!= null) {
                    islandWarp.get().setIcon(xMaterial);
                    player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().warpIconSet.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                    return true;
                } else {
                    player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().noSuchMaterial.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                    return false;
                }
            case "description":
                if (args.length < 4) {
                    sender.sendMessage("/is editwarp <name> description <description>");
                    return false;
                }

                String description = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                islandWarp.get().setDescription(description);
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().warpDescriptionSet.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                return true;
        }

        return false;
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
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        Optional<Island> island = IridiumSkyblock.getInstance().getUserManager().getUser((OfflinePlayer) commandSender).getIsland();
        List<IslandWarp> islandWarps = island.isPresent() ? IridiumSkyblock.getInstance().getDatabaseManager().getIslandWarpTableManager().getEntries(island.get()) : Collections.emptyList();

        if (args.length == 2) {
            return islandWarps.stream()
                .map(IslandWarp::getName)
                .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return Arrays.asList("icon", "description");
        }

        if (args.length == 4) {
            if (args[2].equalsIgnoreCase("icon")) {
                return ObsidianMaterial.values().stream()
                    .map(ObsidianMaterial::name)
                    .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

}
