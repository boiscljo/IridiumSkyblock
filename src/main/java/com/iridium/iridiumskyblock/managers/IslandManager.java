package com.iridium.iridiumskyblock.managers;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.google.common.collect.ImmutableMap;
import com.iridium.iridiumcore.dependencies.paperlib.PaperLib;
import com.moyskleytech.obsidian.material.dependencies.xseries.XBiome;

import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.*;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import com.iridium.iridiumskyblock.api.IslandRegenEvent;
import com.iridium.iridiumskyblock.bank.BankItem;
import com.iridium.iridiumskyblock.configs.Configuration.IslandRegenSettings;
import com.iridium.iridiumskyblock.configs.Configuration;
import com.iridium.iridiumskyblock.configs.Schematics;
import com.iridium.iridiumskyblock.database.*;
import com.iridium.iridiumskyblock.generators.GeneratorType;
import com.iridium.iridiumskyblock.generators.OceanGenerator;
import com.iridium.iridiumskyblock.support.StackerSupport;
import com.iridium.iridiumskyblock.utils.LocationUtils;
import com.iridium.iridiumskyblock.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class which handles islands and their worlds.
 */
public class IslandManager {

  public final Cache<List<Island>> islandValueSortCache = new Cache<>(5000);
  public final Cache<List<Island>> islandLevelSortCache = new Cache<>(5000);
  public World overworld, nether, the_end;

  public void clearIslandCache() {
    islandLevelSortCache.clearCache();
    islandValueSortCache.clearCache();
  }

  private World move(World w) {
    if (w.getEnvironment() == Environment.NORMAL)
      overworld = w;
    else if (w.getEnvironment() == Environment.NETHER)
      nether = w;
    else if (w.getEnvironment() == Environment.THE_END)
      the_end = w;
    return w;
    // IridiumSkyblock.getInstance().getConfiguration().worldName;
  }

  /**
   * Creates a new world using the current skyblock generator.
   *
   * @param environment The world's Environment
   * @param name        The World's Name
   */
  public World createWorld(World.Environment environment, String name) {

    Optional<World> w_ = Bukkit.getWorlds().stream().filter(w -> w.getName().equalsIgnoreCase(name)).findFirst();
    if (w_.isPresent()) {
      return move(w_.get());
    }
    long begin = System.nanoTime();
    WorldCreator worldCreator = null;
    if (IridiumSkyblock.getInstance().getConfiguration().generatorSettings.generatorType == GeneratorType.NORMAL) {
      worldCreator = new WorldCreator(name)
          .environment(environment);
    } else {
      worldCreator = new WorldCreator(name)
          .generator(IridiumSkyblock.getInstance().getDefaultWorldGenerator(name, null))
          .environment(environment);
    }
    World w = Bukkit.createWorld(worldCreator);

    System.out.println("Created world in " + (System.nanoTime() - begin) + " nanoseconds");
    return move(w);
  }

  /**
   * Returns the invite for a User to an Island.
   * Empty if there is none.
   *
   * @param island The island to which the user might have been invited to
   * @param user   The user which might have been invited
   * @return The invite of the user to this island, might be empty
   */
  public Optional<IslandInvite> getIslandInvite(@NotNull Island island, @NotNull User user) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandInviteTableManager()
        .getEntry(new IslandInvite(island, user, user));
  }

  /**
   * Sets an island's biome
   *
   * @param island The specified Island
   * @param xBiome The specified Biome
   */
  public void setIslandBiome(@NotNull Island island, @NotNull XBiome xBiome) {
    World.Environment environment = xBiome.getEnvironment();
    World world;
    switch (environment) {
      case NETHER:
        world = getNetherWorld();
        break;
      case THE_END:
        world = getEndWorld();
        break;
      default:
        world = getWorld();
        break;
    }
    if (world == null)
      return;

    getIslandChunks(island, world).thenAccept(chunks -> {
      Location pos1 = island.getPos1(world);
      Location pos2 = island.getPos2(world);
      xBiome.setBiome(pos1, pos2).thenRun(() -> {
        for (Chunk chunk : chunks) {
          chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }
      });
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  /**
   * Teleports a player to the Island's home
   *
   * @param player The player we are teleporting
   * @param island The island we are teleporting them to
   * @param delay  How long the player should stand still for before teleporting
   */
  public void teleportHome(@NotNull Player player, @NotNull Island island, int delay) {
    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    if (isBannedOnIsland(island, user)) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().youHaveBeenBanned
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
          .replace("%owner%", island.getOwner().getName())
          .replace("%name%", island.getName())));
      return;
    }
    boolean trusted = getIslandTrusted(island, user).isPresent();
    boolean inIsland = island.getMembership(user).getIslandRank() != IslandRank.VISITOR;
    if (!island.isVisitable() && !inIsland && !trusted && !user.isBypassing()) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().islandIsPrivate
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
      return;
    }
    if (inIsland) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().teleportingHome
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
    } else {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().teleportingHomeOther
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
          .replace("%owner%", island.getOwner().getName())));
    }
    if (delay < 1) {
      teleportHome(player, island);
      return;
    }
    BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> {
      teleportHome(player, island);
      user.setTeleportingTask(null);
    }, 20L * delay);
    user.setTeleportingTask(bukkitTask);
  }

  /**
   * Teleports a player to the Island's home
   *
   * @param player The player we are teleporting
   * @param island The island we are teleporting them to
   * @param delay  How long the player should stand still for before teleporting
   */
  public boolean enterIsland(@NotNull Player player, @NotNull Island island) {
    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    if (isBannedOnIsland(island, user)) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().youHaveBeenBanned
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
          .replace("%owner%", island.getOwner().getName())
          .replace("%name%", island.getName())));
      return false;
    }
    boolean trusted = getIslandTrusted(island, user).isPresent();
    boolean inIsland = island.getMembership(user).getIslandRank() != IslandRank.VISITOR;
    if (!island.isVisitable() && !inIsland && !trusted && !user.isBypassing()) {
      player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().islandIsPrivate
          .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
      return false;
    }
    return true;
  }

  /**
   * Teleports a player to the Island's home
   *
   * @param player The player we are teleporting
   * @param island The island we are teleporting them to
   */
  private void teleportHome(@NotNull Player player, @NotNull Island island) {
    player.setFallDistance(0);
    IridiumSkyblock.getInstance().getTrack().track(player, island);
    PaperLib.teleportAsync(player, LocationUtils.getSafeLocation(island.getHome(), island),
        PlayerTeleportEvent.TeleportCause.PLUGIN);

  }

  /**
   * Teleports a player to an Island Warp
   *
   * @param player     The player we are teleporting
   * @param islandWarp The warp we are teleporting them to
   * @param delay      How long the player should stand still for before
   *                   teleporting
   */
  public void teleportWarp(@NotNull Player player, @NotNull IslandWarp islandWarp, int delay) {
    player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().teleportingWarp
        .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix))
        .replace("%name%", islandWarp.getName()));
    if (delay < 1) {
      teleportWarp(player, islandWarp);
      return;
    }
    BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> {
      teleportWarp(player, islandWarp);
      IridiumSkyblock.getInstance().getUserManager().getUser(player).setTeleportingTask(null);
    }, 20L * delay);
    IridiumSkyblock.getInstance().getUserManager().getUser(player).setTeleportingTask(bukkitTask);
  }

  /**
   * Teleports a player to an Island Warp
   *
   * @param player     The player we are teleporting
   * @param islandWarp The warp we are teleporting them to
   */
  private void teleportWarp(@NotNull Player player, @NotNull IslandWarp islandWarp) {
    player.setFallDistance(0);
    PaperLib.teleportAsync(player,
        LocationUtils.getSafeLocation(islandWarp.getLocation(), islandWarp.getIsland().orElse(null)),
        PlayerTeleportEvent.TeleportCause.PLUGIN);
  }

  /**
   * Creates an Island for the specified player with the provided name.
   *
   * @param player    The owner of the Island
   * @param name      The name of the Island
   * @param schematic The schematic of the Island
   * @return The island being created
   */
  public @NotNull CompletableFuture<Island> createIsland(@NotNull Player player, String name,
      @NotNull Schematics.SchematicConfig schematic) {
    clearIslandCache();
    CompletableFuture<Island> completableFuture = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(IridiumSkyblock.getInstance(), () -> {
      User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
      Island island = new Island(name, schematic);

      IridiumSkyblock.getInstance().getDatabaseManager().registerIsland(island).join();

      user.setIsland(island);// Set it in the current profile
      // user.setIslandRank(IslandRank.OWNER);
      IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().save(user);

      IslandMember membership = new IslandMember(island, user, IslandRank.OWNER);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager().save(membership);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager().addEntry(membership);

      Bukkit.getScheduler().runTask(IridiumSkyblock.getInstance(),
          () -> pasteSchematic(island, schematic).thenRun(() -> completableFuture.complete(island)));
    });
    return completableFuture;
  }

  /**
   * Deletes all blocks in the island and re-pastes the schematic.
   *
   * @param island          The specified Island
   * @param schematicConfig The schematic we are pasting
   */
  public void regenerateIsland(@NotNull Island island, User user,
      @NotNull Schematics.SchematicConfig schematicConfig) {
    IslandRegenEvent islandRegenEvent = new IslandRegenEvent(island, user, schematicConfig);
    Bukkit.getPluginManager().callEvent(islandRegenEvent);
    if (islandRegenEvent.isCancelled())
      return;

    CompletableFuture<Void> toComplete = new CompletableFuture<>();
    if (IridiumSkyblock.getInstance().getChunkGenerator() instanceof OceanGenerator) {
      OceanGenerator oceanGenerator = (OceanGenerator) IridiumSkyblock.getInstance().getChunkGenerator();
      for (int x = island.getPos1(getWorld()).getBlockX(); x <= island.getPos2(getWorld()).getBlockX(); x++) {
        for (int z = island.getPos1(getWorld()).getBlockZ(); z <= island.getPos2(getWorld()).getBlockZ(); z++) {
          oceanGenerator.generateWater(getWorld(), x, z);
          oceanGenerator.generateWater(getNetherWorld(), x, z);
          oceanGenerator.generateWater(getEndWorld(), x, z);
        }
      }
      toComplete.complete(null);
    } else {
      toComplete = CompletableFuture.allOf(
          deleteIslandBlocks(island, getWorld(), 0),
          deleteIslandBlocks(island, getNetherWorld(), 0),
          deleteIslandBlocks(island, getEndWorld(), 0));
    }
    toComplete.thenAccept((Void) -> {
      IslandRegenSettings regenSettings = IridiumSkyblock.getInstance().getConfiguration().regenSettings;
      getIslandMembers(island).stream().map(member -> member.getUser()).forEach(targetUser -> {
        Player player = user.getPlayer();
        if (player != null) {
          if (regenSettings.clearInventories)
            player.getInventory().clear();
          if (regenSettings.clearEnderChests)
            player.getEnderChest().clear();
          if (regenSettings.resetVaultBalances)
            IridiumSkyblock.getInstance().getEconomy().withdrawPlayer(player,
                IridiumSkyblock.getInstance().getEconomy().getBalance(player));
          if (regenSettings.kickMembers) {
            player.sendMessage(
                StringUtils.color(IridiumSkyblock.getInstance().getMessages().youHaveBeenKicked
                    .replace("%player%", user.getName())
                    .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
          }
          PlayerUtils.teleportSpawn(player);
        }
        IslandMember membership = island.getMembership(targetUser);
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager()
            .delete(membership);
        targetUser.getCurrentIslandMembership().ifPresent(
            current_membership -> {
              if (current_membership.getIslandId() == island.getId()) {
                targetUser.setIsland(null);
                IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().save(targetUser);
              }
            });
      });

      if (regenSettings.resetIslandBank) {
        getIslandBank(island, IridiumSkyblock.getInstance().getBankItems().moneyBankItem).setNumber(0);
        getIslandBank(island, IridiumSkyblock.getInstance().getBankItems().crystalsBankItem).setNumber(0);
        getIslandBank(island, IridiumSkyblock.getInstance().getBankItems().experienceBankItem).setNumber(0);
      }

      if (regenSettings.resetBoosters) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandBoosterTableManager().getEntries(island)
            .forEach(islandBooster -> islandBooster.setTime(LocalDateTime.now()));
      }

      if (regenSettings.resetMissions) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandMissionTableManager().getEntries(island)
            .forEach(islandMission -> islandMission.setProgress(0));
      }

      if (regenSettings.resetUpgrades) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandUpgradeTableManager().getEntries(island)
            .forEach(islandUpgrade -> islandUpgrade.setLevel(1));
      }

      if (regenSettings.clearWarps) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandWarpTableManager().getEntries(island)
            .forEach(
                IridiumSkyblock.getInstance().getDatabaseManager().getIslandWarpTableManager()::delete);
      }

      if (regenSettings.resetPermissions) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandPermissionTableManager().getEntries(island)
            .forEach(IridiumSkyblock.getInstance().getDatabaseManager()
                .getIslandPermissionTableManager()::delete);
      }

      if (regenSettings.unbanAll) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandBanTableManager().getEntries(island)
            .forEach(IridiumSkyblock.getInstance().getDatabaseManager().getIslandBanTableManager()::delete);
      }

      if (regenSettings.giveUpInvites) {
        IridiumSkyblock.getInstance().getDatabaseManager().getIslandInviteTableManager().getEntries(island)
            .forEach(IridiumSkyblock.getInstance().getDatabaseManager()
                .getIslandInviteTableManager()::delete);
      }

      if (regenSettings.resetBorderColour) {
        island.setColor(IridiumSkyblock.getInstance().getBorder().defaultColor);
      }

      if (regenSettings.makeIslandPrivate) {
        island.setVisitable(false);
      }

      pasteSchematic(island, schematicConfig).thenRun(() -> {

        Location islandHome = island.getCenter(IridiumSkyblock.getInstance().getIslandManager().getWorld())
            .add(schematicConfig.xHome, schematicConfig.yHome, schematicConfig.zHome);
        islandHome.setYaw(schematicConfig.yawHome);
        island.setHome(islandHome);

        Player player = user.getPlayer();
        if (player != null) {
          teleportHome(player, island, 0);
        }

        getEntities(island, getWorld(), getNetherWorld(), getEndWorld())
            .thenAccept(entities -> Bukkit.getScheduler().runTask(IridiumSkyblock.getInstance(), () -> {
              for (Entity entity : entities) {
                if (entity instanceof Player) {
                  teleportHome((Player) entity, island, 0);
                } else {
                  entity.remove();
                }
              }
            }));
      });
    });

  }

  private CompletableFuture<Void> pasteSchematic(@NotNull Island island,
      @NotNull Schematics.SchematicConfig schematicConfig) {
    setIslandBiome(island, schematicConfig.overworld.biome);
    setIslandBiome(island, schematicConfig.nether.biome);
    setIslandBiome(island, schematicConfig.end.biome);
    HashMap<World, Schematics.SchematicWorld> map = new HashMap<>();
    map.put(getWorld(), schematicConfig.overworld);
    if (nether != null)
      map.put(getNetherWorld(), schematicConfig.nether);
    if (the_end != null)
      map.put(getEndWorld(), schematicConfig.end);
    return IridiumSkyblock.getInstance().getSchematicManager().pasteSchematic(island,
        map);
  }

  /**
   * Deletes all blocks in an island.
   *
   * @param island The specified Island
   * @param world  The world we are deleting
   * @param delay  The delay between deleting each layer
   * @return A completableFuture for when its finished deleting the blocks
   */
  public CompletableFuture<Void> deleteIslandBlocks(@NotNull Island island, @NotNull World world, int delay) {

    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    if (world != null) {
      deleteIslandBlocks(island, world, world.getMaxHeight() - 1, completableFuture, delay);
    } else {
      completableFuture.complete(null);
    }
    return completableFuture;
  }

  /**
   * Gets all chunks the island is in.
   *
   * @param island The specified Island
   * @param worlds The worlds
   * @return A list of Chunks the island is in
   */
  public CompletableFuture<List<Chunk>> getIslandChunks(@NotNull Island island, @NotNull World... worlds) {
    CompletableFuture<List<Chunk>> returnFuture = new CompletableFuture<>();

    try {
      List<CompletableFuture<Chunk>> chunks = new ArrayList<>();
      for (World world : worlds) {
        if (world == null)
          continue;
        Location pos1 = island.getPos1(world);
        Location pos2 = island.getPos2(world);

        int minX = pos1.getBlockX() >> 4;
        int minZ = pos1.getBlockZ() >> 4;
        int maxX = pos2.getBlockX() >> 4;
        int maxZ = pos2.getBlockZ() >> 4;

        for (int x = minX; x <= maxX; x++) {
          for (int z = minZ; z <= maxZ; z++) {
            chunks.add(IridiumSkyblock.getInstance().getMultiVersion().getChunkAt(world, x, z));
          }
        }
      }
      List<Chunk> returnValue = new ArrayList<>(chunks.size());
      AtomicInteger numberOfChunk = new AtomicInteger(chunks.size());
      if (numberOfChunk.get() == 0) {
        returnFuture.complete(Collections.emptyList());
        return returnFuture;
      }

      chunks.forEach(futureChunk -> {
        futureChunk.thenAccept(chunk -> {
          returnValue.add(chunk);
          if (numberOfChunk.decrementAndGet() <= 0)
            returnFuture.complete(returnValue);
        });
      });
    } catch (Throwable t) {
      t.printStackTrace();
      returnFuture.complete(Collections.emptyList());
    }
    return returnFuture;

  }

  /**
   * Gets a list of Users from an island.
   *
   * @param island The specified Island
   * @return A list of users
   */
  public @NotNull List<IslandMember> getIslandMembers(@NotNull Island island) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandMemberTableManager().getEntries(island);
  }

  /**
   * Gets a list of all Members on an island
   *
   * @param island The Specified Island
   * @return A list of all members on this island
   */
  public @NotNull List<User> getPlayersOnIsland(@NotNull Island island) {
    return Bukkit.getOnlinePlayers().stream()
        .filter(player -> island.isInIsland(player.getLocation()))
        .map(IridiumSkyblock.getInstance().getUserManager()::getUser)
        .collect(Collectors.toList());
  }

  /**
   * Finds an Island by its id.
   *
   * @param id The id of the island
   * @return An Optional with the Island, empty if there is none
   */
  public Optional<Island> getIslandById(int id) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandTableManager().getIsland(id);
  }

  /**
   * Finds an Island by its name.
   *
   * @param name The name of the island
   * @return An Optional with the Island, empty if there is none
   */
  public Optional<Island> getIslandByName(String name) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandTableManager().getEntries().stream()
        .filter(island -> island.getName().equalsIgnoreCase(name)).findFirst();
  }

  /**
   * Finds an island by the player's location with cache
   *
   * @param player The specified Player
   * @return An optional of the island the player is in
   */
  public @NotNull Optional<Island> getIslandViaPlayerLocation(Player player) {
    if (!IridiumSkyblockAPI.getInstance().isIslandWorld(player.getWorld())) {
      return Optional.empty();
    }

    User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
    if (user.getCurrentIslandVisiting() != null) {
      if (user.getCurrentIslandVisiting().isInIsland(player.getLocation())) {
        return Optional.of(user.getCurrentIslandVisiting());
      }
    }

    Optional<Island> island = getIslandViaLocation(player.getLocation());
    island.ifPresent(user::setCurrentIslandVisiting);
    return island;
  }

  /**
   * Gets an {@link Island} from locations.
   *
   * @param location The locations the island is in
   * @return Optional of the island at the locations, empty if there is none
   */
  public @NotNull Optional<Island> getIslandViaLocation(@NotNull Location location) {
    if (!IridiumSkyblockAPI.getInstance().isIslandWorld(location.getWorld()))
      return Optional.empty();
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandTableManager().getEntries().stream()
        .filter(island -> island.isInIsland(location)).findFirst();
  }

  /**
   * Gets an IslandTrusted object from island and user
   *
   * @param island The specified island
   * @param user   The user who is trusted
   * @return An optional IslandTrusted Object
   */
  public Optional<IslandTrusted> getIslandTrusted(Island island, User user) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandTrustedTableManager()
        .getEntry(new IslandTrusted(island, user, user));
  }

  /**
   * Gets an IslandBan object from island and user
   *
   * @param island The specified island
   * @param user   The banned user
   * @return an optional IslandBan object
   */
  public Optional<IslandBan> getIslandBan(Island island, User user) {
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandBanTableManager()
        .getEntry(new IslandBan(island, user, user));
  }

  /**
   * Gets whether an IslandRank has the permission on the provided island.
   *
   * @param island     The specified Island
   * @param islandRank The specified Rank
   * @param permission The specified Permission
   * @return If the permission is allowed
   */
  public boolean getIslandPermission(@NotNull Island island, @NotNull IslandRank islandRank,
      @NotNull Permission permission, @NotNull String key) {
    Optional<IslandPermission> islandPermission = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandPermissionTableManager().getEntry(new IslandPermission(island, key, islandRank, true));
    return islandPermission.map(IslandPermission::isAllowed)
        .orElseGet(() -> islandRank.getLevel() >= permission.getDefaultRank().getLevel());
  }

  /**
   * Gets weather a permission is allowed or denied.
   *
   * @param island     The specified Island
   * @param user       The Specified User
   * @param permission The Specified permission
   * @param key        The permission Key
   * @return The the permission is allowed
   */
  public boolean getIslandPermission(@NotNull Island island, @NotNull User user, @NotNull Permission permission,
      @NotNull String key) {
    IslandRank islandRank = island.getMembership(user).getIslandRank();

    if (getIslandTrusted(island, user).isPresent()) {
      islandRank = IslandRank.MEMBER;
    }
    return getIslandPermission(island, islandRank, permission, key) || user.isBypassing();
  }

  /**
   * Gets weather a permission is allowed or denied.
   *
   * @param island         The specified Island
   * @param user           The Specified User
   * @param permissionType The Specified permission type
   * @return The the permission is allowed
   */
  public boolean getIslandPermission(@NotNull Island island, @NotNull User user,
      @NotNull PermissionType permissionType) {
    return getIslandPermission(island, user,
        IridiumSkyblock.getInstance().getPermissionList().get(permissionType.getPermissionKey()),
        permissionType.getPermissionKey());
  }

  /**
   * Gets an Island's bank from BankItem.
   *
   * @param island   The specified Island
   * @param bankItem The BankItem we are getting
   * @return the IslandBank
   */
  public synchronized IslandBank getIslandBank(@NotNull Island island, @NotNull BankItem bankItem) {
    Optional<IslandBank> optionalIslandBank = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandBankTableManager().getEntry(new IslandBank(island, bankItem.getName(), 0));
    if (optionalIslandBank.isPresent()) {
      return optionalIslandBank.get();
    } else {
      IslandBank islandBank = new IslandBank(island, bankItem.getName(), 0);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandBankTableManager().addEntry(islandBank);
      return islandBank;
    }
  }

  /**
   * Gets the IslandBlock for a specific island and material.
   *
   * @param island   The specified Island
   * @param material The specified Material
   * @return The IslandBlock
   */
  public synchronized IslandBlocks getIslandBlock(@NotNull Island island, @NotNull ObsidianMaterial material) {
    Optional<IslandBlocks> islandBlocksOptional = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandBlocksTableManager().getEntry(new IslandBlocks(island, material));
    if (islandBlocksOptional.isPresent()) {
      return islandBlocksOptional.get();
    }
    IslandBlocks islandBlocks = new IslandBlocks(island, material);
    IridiumSkyblock.getInstance().getDatabaseManager().getIslandBlocksTableManager().addEntry(islandBlocks);
    return islandBlocks;
  }

  /**
   * Gets the IslandBlock for a specific island and material.
   *
   * @param island      The specified Island
   * @param spawnerType The specified spawner type
   * @return The IslandBlock
   */
  public synchronized IslandSpawners getIslandSpawners(@NotNull Island island, @NotNull EntityType spawnerType) {
    Optional<IslandSpawners> islandSpawnersOptional = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandSpawnersTableManager().getEntry(new IslandSpawners(island, spawnerType));
    if (islandSpawnersOptional.isPresent()) {
      return islandSpawnersOptional.get();
    }
    IslandSpawners islandSpawners = new IslandSpawners(island, spawnerType);
    IridiumSkyblock.getInstance().getDatabaseManager().getIslandSpawnersTableManager().addEntry(islandSpawners);
    return islandSpawners;
  }

  /**
   * Sets whether a permission is allowed or denied for the specified IslandRank.
   *
   * @param island     The specified Island
   * @param islandRank The specified Rank
   * @param allowed    If the permission is allowed
   */
  public synchronized void setIslandPermission(@NotNull Island island, @NotNull IslandRank islandRank,
      @NotNull String key, boolean allowed) {
    Optional<IslandPermission> islandPermission = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandPermissionTableManager().getEntry(new IslandPermission(island, key, islandRank, true));
    if (islandPermission.isPresent()) {
      islandPermission.get().setAllowed(allowed);
    } else {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandPermissionTableManager()
          .addEntry(new IslandPermission(island, key, islandRank, allowed));
    }
  }

  /**
   * Gets an IslandSetting from a specific Island
   *
   * @param island       The specified Island
   * @param settingName  The Setting Name
   * @param defaultValue The default value for this setting
   * @return The IslandSetting object
   */
  public synchronized IslandSetting getIslandSetting(@NotNull Island island, @NotNull String settingName,
      @NotNull String defaultValue) {
    IslandSetting islandSetting = new IslandSetting(island, settingName, defaultValue);
    Optional<IslandSetting> islandSettingOptional = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandSettingTableManager().getEntry(islandSetting);
    if (islandSettingOptional.isPresent()) {
      return islandSettingOptional.get();
    } else {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandSettingTableManager().addEntry(islandSetting);
      return islandSetting;
    }
  }

  /**
   * Gets an IslandSetting from a specific Island
   *
   * @param island      The specified Island
   * @param settingType The specified Setting Type
   * @return The IslandSetting object
   */
  public synchronized IslandSetting getIslandSetting(@NotNull Island island, @NotNull SettingType settingType) {
    return getIslandSetting(island, settingType.getSettingName(), settingType.getDefaultValue());
  }

  /**
   * Deletes all blocks in an Island.
   * Start at the top and work your way down to the lowest layer.
   *
   * @param island            The specified Island
   * @param world             The specified World
   * @param y                 The current y level
   * @param completableFuture The completable future to be completed when task is
   *                          finished
   * @param delay             The delay in ticks between each layer
   */
  private void deleteIslandBlocks(@NotNull Island island, @NotNull World world, int y,
      CompletableFuture<Void> completableFuture, int delay) {
    if (world == null) {
      completableFuture.complete(null);
      return;
    }

    Location pos1 = island.getPos1(world);
    Location pos2 = island.getPos2(world);

    for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
      for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() != Material.AIR) {
          if (block.getState() instanceof InventoryHolder) {
            ((InventoryHolder) block.getState()).getInventory().clear();
          }
          block.setType(Material.AIR, false);
        }
      }
    }

    if (y == LocationUtils.getMinHeight(world)) {
      completableFuture.complete(null);
    } else {
      if (delay < 1) {
        deleteIslandBlocks(island, world, y - 1, completableFuture, delay);
      } else {
        Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(),
            () -> deleteIslandBlocks(island, world, y - 1, completableFuture, delay), delay);
      }
    }
  }

  /**
   * Deletes the specified Island.
   *
   * @param island The Island which should be deleted
   * @param user   The user who deleted the island
   */
  public void deleteIsland(@NotNull Island island, @Nullable User user) {
    IslandDeleteEvent islandDeleteEvent = new IslandDeleteEvent(island, user);
    Bukkit.getPluginManager().callEvent(islandDeleteEvent);
    if (islandDeleteEvent.isCancelled())
      return;
    clearIslandCache();
    deleteIslandBlocks(island, getWorld(), 3);
    deleteIslandBlocks(island, getNetherWorld(), 3);
    deleteIslandBlocks(island, getEndWorld(), 3);

    getIslandMembers(island).stream().map(member -> member.getUser().getPlayer()).forEach(player -> {
      if (player != null) {
        if (IridiumSkyblock.getInstance().getConfiguration().deleteSettings.clearInventories) {
          player.getInventory().clear();
        }
        if (IridiumSkyblock.getInstance().getConfiguration().deleteSettings.clearEnderChests) {
          player.getEnderChest().clear();
        }
        if (IridiumSkyblock.getInstance().getConfiguration().deleteSettings.resetVaultBalances) {
          IridiumSkyblock.getInstance().getEconomy().withdrawPlayer(player,
              IridiumSkyblock.getInstance().getEconomy().getBalance(player));
        }
        player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().islandDeleted
            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
      }
    });
    deleteIslandDatabaseEntries(island);

    getEntities(island, getWorld(), getEndWorld(), getNetherWorld()).thenAccept(entities -> Bukkit.getScheduler()
        .runTask(IridiumSkyblock.getInstance(), () -> entities.stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .forEach(PlayerUtils::teleportSpawn)));
  }

  /**
   * Delete all database entries of specific island
   *
   * @param island The specified Island
   */
  private void deleteIslandDatabaseEntries(@NotNull Island island) {
    DatabaseManager databaseManager = IridiumSkyblock.getInstance().getDatabaseManager();
    Bukkit.getScheduler().runTaskAsynchronously(IridiumSkyblock.getInstance(), () -> {
      databaseManager.getIslandTableManager().delete(island);
      databaseManager.getIslandBanTableManager().getEntries(island)
          .forEach(databaseManager.getIslandBanTableManager()::delete);
      databaseManager.getIslandBankTableManager().getEntries(island)
          .forEach(databaseManager.getIslandBankTableManager()::delete);
      databaseManager.getIslandBlocksTableManager().getEntries(island)
          .forEach(databaseManager.getIslandBlocksTableManager()::delete);
      databaseManager.getIslandBoosterTableManager().getEntries(island)
          .forEach(databaseManager.getIslandBoosterTableManager()::delete);
      databaseManager.getIslandInviteTableManager().getEntries(island)
          .forEach(databaseManager.getIslandInviteTableManager()::delete);
      databaseManager.getIslandLogTableManager().getEntries(island)
          .forEach(databaseManager.getIslandLogTableManager()::delete);
      databaseManager.getIslandMissionTableManager().getEntries(island)
          .forEach(databaseManager.getIslandMissionTableManager()::delete);
      databaseManager.getIslandRewardTableManager().getEntries(island)
          .forEach(databaseManager.getIslandRewardTableManager()::delete);
      databaseManager.getIslandSpawnersTableManager().getEntries(island)
          .forEach(databaseManager.getIslandSpawnersTableManager()::delete);
      databaseManager.getIslandTrustedTableManager().getEntries(island)
          .forEach(databaseManager.getIslandTrustedTableManager()::delete);
      databaseManager.getIslandUpgradeTableManager().getEntries(island)
          .forEach(databaseManager.getIslandUpgradeTableManager()::delete);
      databaseManager.getIslandWarpTableManager().getEntries(island)
          .forEach(databaseManager.getIslandWarpTableManager()::delete);
      databaseManager.getIslandSettingTableManager().getEntries(island)
          .forEach(databaseManager.getIslandSettingTableManager()::delete);
      databaseManager.getIslandMemberTableManager().getEntries(island)
          .forEach(databaseManager.getIslandMemberTableManager()::delete);
    });
  }

  /**
   * Gets an Island upgrade
   *
   * @param island The specified Island
   * @param user   The specified User
   * @return The a boolean the user is banned on this island
   */
  public boolean isBannedOnIsland(@NotNull Island island, User user) {
    return getIslandBan(island, user).isPresent() && !user.isBypassing();
  }

  /**
   * Gets an Island upgrade
   *
   * @param island  The specified Island
   * @param upgrade The specified Upgrade's name
   * @return The island Upgrade
   */
  public synchronized IslandUpgrade getIslandUpgrade(@NotNull Island island, @NotNull String upgrade) {
    Optional<IslandUpgrade> islandUpgrade = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandUpgradeTableManager().getEntry(new IslandUpgrade(island, upgrade));
    if (islandUpgrade.isPresent()) {
      return islandUpgrade.get();
    } else {
      IslandUpgrade isUpgrade = new IslandUpgrade(island, upgrade);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandUpgradeTableManager().addEntry(isUpgrade);
      return isUpgrade;
    }
  }

  /**
   * Gets all island missions and creates them if they don't exist.
   *
   * @param island The specified Island
   * @return A list of Island Missions
   */
  public synchronized IslandMission getIslandMission(@NotNull Island island, @NotNull Mission mission,
      @NotNull String missionKey, int missionIndex) {
    Optional<IslandMission> islandMissionOptional = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandMissionTableManager().getEntry(new IslandMission(island, mission, missionKey, missionIndex));
    if (islandMissionOptional.isPresent()) {
      return islandMissionOptional.get();
    } else {
      IslandMission islandMission = new IslandMission(island, mission, missionKey, missionIndex);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandMissionTableManager().addEntry(islandMission);
      return islandMission;
    }
  }

  private synchronized String getDailyIslandMission(@NotNull Island island, int index) {
    List<String> islandMissions = IridiumSkyblock.getInstance().getDatabaseManager().getIslandMissionTableManager()
        .getEntries(island).stream()
        .filter(islandMission -> islandMission.getType() == Mission.MissionType.DAILY)
        .filter(e -> IridiumSkyblock.getInstance().getMissionManager().canComplete(island, e.getMissionName()))
        .map(IslandMission::getMissionName)
        .distinct()
        .collect(Collectors.toList());

    if (islandMissions.size() > index) {
      return islandMissions.get(index);
    }

    ThreadLocalRandom random = ThreadLocalRandom.current();
    List<String> availableMissions = IridiumSkyblock.getInstance().getMissionsList().keySet().stream()
        .filter(mission -> IridiumSkyblock.getInstance().getMissionsList().get(mission)
            .getMissionType() == Mission.MissionType.DAILY)
        .filter(e -> IridiumSkyblock.getInstance().getMissionManager().canComplete(island, e))
        .filter(mission -> islandMissions.stream().noneMatch(m -> m.equals(mission)))
        .collect(Collectors.toList());

    String key = availableMissions.get(random.nextInt(availableMissions.size()));
    Mission mission = IridiumSkyblock.getInstance().getMissionsList().get(key);

    for (int i = 0; i < mission.getMissions().size(); i++) {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandMissionTableManager()
          .addEntry(new IslandMission(island, mission, key, i));
    }

    return key;
  }

  /**
   * Gets the Islands daily missions.
   *
   * @param island The specified Island
   * @return The daily missions
   */
  public synchronized Map<String, Mission> getDailyIslandMissions(@NotNull Island island) {
    Map<String, Mission> missions = new LinkedHashMap<>();

    IntStream.range(0, IridiumSkyblock.getInstance().getMissions().dailySlots.size())
        .boxed()
        .map(i -> getDailyIslandMission(island, i))
        .sorted(Comparator.comparingInt(mission -> {
          Integer slot = IridiumSkyblock.getInstance().getMissionsList().get(mission).getItem().slot;
          return slot == null ? 0 : slot;
        }))
        .forEachOrdered(
            mission -> missions.put(mission, IridiumSkyblock.getInstance().getMissionsList().get(mission)));

    return missions;
  }

  public int getIslandBlockAmount(Island island, ObsidianMaterial material) {
    int extraAmount = getIslandBlock(island, material).getExtraAmount();
    for (StackerSupport stackerSupport : IridiumSkyblock.getInstance().getStackerSupport()) {
      extraAmount += stackerSupport.getExtraBlocks(island, material);
    }
    return getIslandBlock(island, material).getAmount() + extraAmount;
  }

  public int getIslandSpawnerAmount(Island island, EntityType entityType) {
    int extraAmount = getIslandSpawners(island, entityType).getExtraAmount();
    for (StackerSupport stackerSupport : IridiumSkyblock.getInstance().getStackerSupport()) {
      extraAmount += stackerSupport.getExtraSpawners(island, entityType);
    }
    return getIslandSpawners(island, entityType).getAmount() + extraAmount;
  }

  /**
   * Recalculates the island value of the specified island.
   *
   * @param island The specified Island
   */
  public void recalculateIsland(@NotNull Island island) {
    getIslandChunks(island, getWorld(), getNetherWorld(), getEndWorld()).thenAcceptAsync(chunks -> {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandBlocksTableManager().getEntries(island)
          .forEach(islandBlocks -> islandBlocks.setAmount(0));
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandSpawnersTableManager().getEntries(island)
          .forEach(islandSpawners -> islandSpawners.setAmount(0));

      recalculateIsland(island, chunks);
    });
  }

  /**
   * Recalculates the island value of the specified island.
   *
   * @param island The specified Island
   */
  public CompletableFuture<Void> recalculateIslandAsync(@NotNull Island island) {
    CompletableFuture<Void> ret = new CompletableFuture<>();
    getIslandChunks(island, getWorld(), getNetherWorld(), getEndWorld()).thenAcceptAsync(chunks -> {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandBlocksTableManager().getEntries(island)
          .forEach(islandBlocks -> islandBlocks.setAmount(0));
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandSpawnersTableManager().getEntries(island)
          .forEach(islandSpawners -> islandSpawners.setAmount(0));

      recalculateIsland(island, chunks, ret);
    });
    return ret;
  }

  /**
   * Recalculates the island async with specified ChunkSnapshots.
   *
   * @param island The specified Island
   * @param chunks The Island's Chunks
   */
  private void recalculateIsland(@NotNull Island island, @NotNull List<Chunk> chunks) {
    chunks.stream().forEach(chunk_ -> {
      ChunkSnapshot chunk = chunk_.getChunkSnapshot(true, false, false);
      World world = Bukkit.getWorld(chunk.getWorldName());
      boolean ignoreMainMaterial = Optional.ofNullable(IridiumSkyblock.getInstance().getChunkGenerator()).map(x->x.ignoreMainMaterial()).orElse(true);
      int maxHeight = world == null ? 255 : world.getMaxHeight() - 1;

      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          if (island.isInIsland(x + (chunk.getX() * 16), z + (chunk.getZ() * 16), world)) {
            final int maxy = Math.min(maxHeight, chunk.getHighestBlockYAt(x, z));
            for (int y = LocationUtils.getMinHeight(world); y <= maxy; y++) {
              ObsidianMaterial material = ObsidianMaterial.valueOf(chunk.getBlockType(x, y, z));
              if (material == ObsidianMaterial.valueOf("AIR"))
                continue;
              if (!ignoreMainMaterial && material == Optional.ofNullable(IridiumSkyblock.getInstance().getChunkGenerator()).map(maybeChunk->maybeChunk.getMainMaterial(world)).orElse(ObsidianMaterial.valueOf("AIR")))
                continue;

              IslandBlocks islandBlock = IridiumSkyblock.getInstance().getIslandManager()
                  .getIslandBlock(island, material);
              islandBlock.setAmount(islandBlock.getAmount() + 1);
            }
          }
        }
      }
    });

    if (Bukkit.isPrimaryThread()) {
      getAllTileInIsland(island, chunks);
    } else {
      Bukkit.getScheduler().runTask(IridiumSkyblock.getInstance(), () -> getAllTileInIsland(island, chunks));
    }
  }

  /**
   * Recalculates the island async with specified ChunkSnapshots.
   *
   * @param island The specified Island
   * @param chunks The Island's Chunks
   */
  private void recalculateIsland(@NotNull Island island, @NotNull List<Chunk> chunks, CompletableFuture<Void> ret) {
    ListIterator<Chunk> iterator = new ArrayList<>(chunks).listIterator();
    int delay = IridiumSkyblock.getInstance().getConfiguration().tickPerRecalculationStep;
    ObsidianMaterial air = ObsidianMaterial.wrap(Material.AIR);

    new BukkitRunnable() {
      @Override
      public void run() {
        for (int i = 0; i < IridiumSkyblock.getInstance().getConfiguration().chunkPerTickRecalculation; i++)
          if (!iterator.hasNext()) {
            if (Bukkit.isPrimaryThread()) {
              getAllTileInIsland(island, chunks);
            } else {
              Bukkit.getScheduler().runTask(IridiumSkyblock.getInstance(),
                  () -> getAllTileInIsland(island, chunks));
            }
            ret.complete(null);
            this.cancel();
            return;
          } else {
            Chunk chk = iterator.next();
            ChunkSnapshot chunk = chk.getChunkSnapshot(true, false, false);
            World world = Bukkit.getWorld(chunk.getWorldName());
            boolean ignoreMainMaterial =Optional.ofNullable(IridiumSkyblock.getInstance().getChunkGenerator()).map(x->x.ignoreMainMaterial()).orElse(true);
            int maxHeight = world == null ? 255 : world.getMaxHeight() - 1;
            for (int x = 0; x < 16; x++) {
              for (int z = 0; z < 16; z++) {
                if (island.isInIsland(x + (chunk.getX() * 16), z + (chunk.getZ() * 16), world)) {
                  final int maxy = Math.min(maxHeight, chunk.getHighestBlockYAt(x, z));
                  for (int y = LocationUtils.getMinHeight(world); y <= maxy; y++) {
                    
                    ObsidianMaterial material = ObsidianMaterial
                        .match(chk.getBlock(x,y,z));
                    if (material == air)
                      continue;
                     if (!ignoreMainMaterial && material == Optional.ofNullable(IridiumSkyblock.getInstance().getChunkGenerator()).map(maybeChunk->maybeChunk.getMainMaterial(world)).orElse(ObsidianMaterial.valueOf("AIR")))
                continue;

                    IslandBlocks islandBlock = IridiumSkyblock.getInstance().getIslandManager()
                        .getIslandBlock(island, material);
                    islandBlock.setAmount(islandBlock.getAmount() + 1);
                  }
                }
              }
            }
          }
      }
    }.runTaskTimer(IridiumSkyblock.getInstance(), delay, delay);
  }

  private void getAllTileInIsland(Island island, List<Chunk> chunks) {
    chunks.forEach(chunk -> {
      for (BlockState blockState : chunk.getTileEntities()) {
        if (!(blockState instanceof CreatureSpawner))
          continue;
        if (!island.isInIsland(blockState.getLocation()))
          continue;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
        try {
          IslandSpawners islandSpawners = IridiumSkyblock.getInstance().getIslandManager()
              .getIslandSpawners(island, creatureSpawner.getSpawnedType());
          islandSpawners.setAmount(islandSpawners.getAmount() + 1);
        } catch (Throwable t) {

        }
      }
    });
  }

  /**
   * Gets time remaining on an island booster
   *
   * @param island  The specified Island
   * @param booster The booster name
   * @return The time remaining
   */
  public synchronized IslandBooster getIslandBooster(@NotNull Island island, @NotNull String booster) {
    Optional<IslandBooster> islandBooster = IridiumSkyblock.getInstance().getDatabaseManager()
        .getIslandBoosterTableManager().getEntry(new IslandBooster(island, booster));
    if (islandBooster.isPresent()) {
      return islandBooster.get();
    } else {
      IslandBooster newBooster = new IslandBooster(island, booster);
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandBoosterTableManager().addEntry(newBooster);
      return newBooster;
    }
  }

  /**
   * Gets all entities on an island
   *
   * @param island The specified Island
   * @return A list of all entities on that island
   */
  public CompletableFuture<List<Entity>> getEntities(@NotNull Island island, @NotNull World... worlds) {
    CompletableFuture<List<Entity>> completableFuture = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(IridiumSkyblock.getInstance(), () -> {
      List<Chunk> chunks = new ArrayList<>();
      for (World world : worlds) {
        if (world != null)
          chunks.addAll(getIslandChunks(island, world).join());
      }
      Bukkit.getScheduler().runTask(IridiumSkyblock.getInstance(), () -> {
        List<Entity> entities = new ArrayList<>();
        for (Chunk chunk : chunks) {
          for (Entity entity : chunk.getEntities()) {
            if (island.isInIsland(entity.getLocation())) {
              entities.add(entity);
            }
          }
        }
        completableFuture.complete(entities);
      });
    });
    return completableFuture;
  }

  public synchronized void islandLevelUp(Island island, int newLevel) {

    for (IslandMember user : getIslandMembers(island)) {
      Player player = Bukkit.getPlayer(user.getUserId());
      if (player != null) {
        IridiumSkyblock.getInstance().getConfiguration().islandLevelUpSound.play(player);
        player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().islandLevelUp
            .replace("%level%", String.valueOf(newLevel))
            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
      }
    }

    Reward reward = null;
    List<Map.Entry<Integer, Reward>> entries = IridiumSkyblock.getInstance().getConfiguration().islandLevelRewards
        .entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
    for (Map.Entry<Integer, Reward> entry : entries) {
      if (newLevel % entry.getKey() == 0) {
        reward = entry.getValue();
      }
    }
    if (reward != null) {
      IridiumSkyblock.getInstance().getDatabaseManager().getIslandRewardTableManager()
          .addEntry(new IslandReward(island, reward));
    }
  }

  /**
   * Sends the island border to all players on the island
   *
   * @param island The specified Island
   */
  public void sendIslandBorder(@NotNull Island island) {
    getEntities(island, getWorld(), getNetherWorld(), getEndWorld()).thenAccept(entities -> {
      for (Entity entity : entities) {
        if (entity instanceof Player) {
          PlayerUtils.sendBorder((Player) entity, island);
        }
      }
    });
  }

  /**
   * Gets a list of islands sorted by SortType
   *
   * @param sortType How we are sorting the islands
   * @return The sorted list of islands
   */
  public List<Island> getIslands(SortType sortType) {
    if (sortType == SortType.VALUE) {
      return islandValueSortCache.getCache(() -> IridiumSkyblock.getInstance().getDatabaseManager()
          .getIslandTableManager().getEntries().stream()
          .sorted(Comparator.comparing(Island::getValue).reversed()).collect(Collectors.toList()));
    }
    if (sortType == SortType.LEVEL) {
      return islandLevelSortCache.getCache(() -> IridiumSkyblock.getInstance().getDatabaseManager()
          .getIslandTableManager().getEntries().stream()
          .sorted(Comparator.comparing(Island::getExperience).reversed()).collect(Collectors.toList()));
    }
    return IridiumSkyblock.getInstance().getDatabaseManager().getIslandTableManager().getEntries();
  }

  /**
   * Represents a way of ordering Islands.
   */
  public enum SortType {
    VALUE, LEVEL
  }

  /**
   * Returns the overworld.
   *
   * @return The main skyblock {@link World}, might be null if some third-party
   *         plugin deleted it
   * @since 3.0.0
   */
  public World getWorld() {
    return overworld;
    // return
    // Bukkit.getWorld(IridiumSkyblock.getInstance().getConfiguration().worldName);
  }

  /**
   * Returns the NetherWorld
   *
   * @return The nether skyblock {@link World}, might be null if some third-party
   *         plugin deleted it
   * @since 3.0.0
   */
  public World getNetherWorld() {
    return nether;
    // return
    // Bukkit.getWorld(IridiumSkyblock.getInstance().getConfiguration().worldName +
    // "_nether");
  }

  /**
   * Returns the EndWorld
   *
   * @return The end skyblock {@link World}, might be null if some third-party
   *         plugin deleted it
   * @since 3.0.0
   */
  public World getEndWorld() {
    return the_end;
    // return
    // Bukkit.getWorld(IridiumSkyblock.getInstance().getConfiguration().worldName +
    // "_the_end");
  }

  public boolean isIslandOverWorld(World world) {
    return world.equals(getWorld());
  }

  public boolean isIslandNether(World world) {
    return world.equals(getNetherWorld());
  }

  public boolean isIslandEnd(World world) {
    return world.equals(getEndWorld());
  }

  public ItemStack getIslandCrystal(double amount) {
    ItemStack itemStack = ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getConfiguration().islandCrystal,
        Collections.singletonList(
            new Placeholder("amount", String.valueOf(amount))));
    ItemMeta meta = itemStack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(IridiumSkyblock.getInstance(), "amount");
    pdc.set(key, PersistentDataType.DOUBLE, amount);
    itemStack.setItemMeta(meta);
    return itemStack;
  }

  public double getIslandCrystals(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR)
      return 0;
    try {
      ItemMeta meta = itemStack.getItemMeta();
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      NamespacedKey key = new NamespacedKey(IridiumSkyblock.getInstance(), "amount");
      if (pdc.has(key, PersistentDataType.DOUBLE)) {
        double amount = pdc.get(key, PersistentDataType.DOUBLE);
        return amount;
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return 0;
  }

  public void reload() {
    Configuration configuration = IridiumSkyblock.getInstance().getConfiguration();
    this.createWorld(World.Environment.NORMAL, configuration.worldName);
    if (configuration.netherIslands)
      this.createWorld(World.Environment.NETHER, configuration.worldName + "_nether");
    if (configuration.endIslands)
      this.createWorld(World.Environment.THE_END, configuration.worldName + "_the_end");

    // Register worlds with multiverse
    Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> {
      if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
        IridiumSkyblock.getInstance().registerMultiverse(getWorld());
        if (configuration.netherIslands)
          IridiumSkyblock.getInstance().registerMultiverse(getNetherWorld());
        if (configuration.endIslands)
          IridiumSkyblock.getInstance().registerMultiverse(getEndWorld());
      }
    }, 1);
  }

}
