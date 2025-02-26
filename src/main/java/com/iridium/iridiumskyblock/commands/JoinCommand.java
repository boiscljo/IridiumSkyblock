package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.IslandRank;
import com.iridium.iridiumskyblock.LogAction;
import com.iridium.iridiumskyblock.api.UserJoinEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandInvite;
import com.iridium.iridiumskyblock.database.IslandLog;
import com.iridium.iridiumskyblock.database.IslandMember;
import com.iridium.iridiumskyblock.database.IslandUpgrade;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.utils.PlayerUtils;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command which enables users to join other Islands.
 */
public class JoinCommand extends Command {

    /**
     * The default constructor.
     */
    public JoinCommand() {
        super(Collections.singletonList("join"), "Join an Island", "%prefix% &7/is join <player>", "", true,
                Duration.ZERO);
    }

    /**
     * Executes the command for the specified {@link CommandSender} with the
     * provided arguments.
     * Not called when the command execution was invalid (no permission, no player
     * or command disabled).
     * Enables users to join other Islands.
     *
     * @param sender The CommandSender which executes this command
     * @param args   The arguments used with this command. They contain the
     *               sub-command
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(StringUtils
                    .color(syntax.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            return false;
        }

        Player player = (Player) sender;
        User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
        List<IslandMember> memberships = user.getMemberships();
        if (memberships.size() > IridiumSkyblock.getInstance().getConfiguration().maxIslandPerPlayer || 
            (memberships.size() == IridiumSkyblock.getInstance().getConfiguration().maxIslandPerPlayer && user.getIsland().isPresent() )
        ) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().alreadyHaveIsland
                    .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
                    .replace("%membership%", String.valueOf(memberships.size()))
                    .replace("%max%", String.valueOf(IridiumSkyblock.getInstance().getConfiguration().maxIslandPerPlayer))));
            return false;
        }

        User offlinePlayerUser = IridiumSkyblock.getInstance().getUserManager().getUser(args[1]);
        if (offlinePlayerUser != null) {
            Optional<Island> island = offlinePlayerUser.getIsland();
            if (!island.isPresent()) {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().userNoIsland
                        .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                return false;
            }

            Optional<IslandInvite> islandInvite = IridiumSkyblock.getInstance().getIslandManager()
                    .getIslandInvite(island.get(), user);
            if (!islandInvite.isPresent() && !user.isBypassing()) {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().noInvite
                        .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                return false;
            }

            IslandUpgrade islandUpgrade = IridiumSkyblock.getInstance().getIslandManager()
                    .getIslandUpgrade(island.get(), "member");
            int memberLimit = IridiumSkyblock.getInstance().getUpgrades().memberUpgrade.upgrades
                    .get(islandUpgrade.getLevel()).amount;
            if (!user.isBypassing() && island.get().getMembers().size() >= memberLimit) {
                player.sendMessage(
                        StringUtils.color(IridiumSkyblock.getInstance().getMessages().islandMemberLimitReached
                                .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                return false;
            }

            Optional<User> inviter = islandInvite.map(IslandInvite::getInviter);
            UserJoinEvent userJoinEvent = new UserJoinEvent(island.get(), user, inviter.orElse(null));
            Bukkit.getPluginManager().callEvent(userJoinEvent);
            if (userJoinEvent.isCancelled())
                return false;

            // Send a message to all other members
            for (IslandMember member : island.get().getMembers()) {
                Player islandMember = Bukkit.getPlayer(member.getUserId());
                if (islandMember != null) {
                    islandMember.sendMessage(
                            StringUtils.color(IridiumSkyblock.getInstance().getMessages().playerJoinedYourIsland
                                    .replace("%player%", player.getName())
                                    .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                }
            }
            IslandMember membership = new IslandMember(island.get(),user,IslandRank.MEMBER);
            IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager().addEntry(membership);
            IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager().save(membership);
            user.setIsland(island.get());
            IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().save(user);


            islandInvite.ifPresent(invite -> IridiumSkyblock.getInstance().getDatabaseManager()
                    .getIslandInviteTableManager().delete(invite));
            IridiumSkyblock.getInstance().getIslandManager().teleportHome(player, island.get(), 0);

            IslandLog islandLog = new IslandLog(island.get(), LogAction.USER_JOINED, user, null, 0, "");
            IridiumSkyblock.getInstance().getDatabaseManager().getIslandLogTableManager().addEntry(islandLog);
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
        return PlayerUtils.getOnlinePlayerNames();
    }

}
