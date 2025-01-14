/*
 *  LICENSE
 *  DiscordSRVUtils
 *  -------------
 *  Copyright (C) 2020 - 2021 BlueTree242
 *  -------------
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program.  If not, see
 *  <http://www.gnu.org/licenses/gpl-3.0.html>.
 *  END
 */

package tk.bluetree242.discordsrvutils.systems.leveling;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.exceptions.UnCheckedSQLException;
import tk.bluetree242.discordsrvutils.utils.FileWriter;
import tk.bluetree242.discordsrvutils.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LevelingManager {
    public final Long MAP_EXPIRATION_NANOS = Duration.ofSeconds(60L).toNanos();
    public final Map<UUID, Long> antispamMap = new HashMap<>();
    private final DiscordSRVUtils core;
    private boolean adding = false;
    public LoadingCache<UUID, PlayerStats> cachedUUIDS = Caffeine.newBuilder()
            .maximumSize(120)
            .expireAfterWrite(Duration.ofMinutes(1))
            .refreshAfterWrite(Duration.ofSeconds(30))
            .build(key -> {
                adding = true;
                PlayerStats stats = getPlayerStats(key).get();
                adding = false;
                return stats;
            });
    //leveling roles jsonobject, Initialized on startup
    @Getter
    private JSONObject levelingRolesRaw;


    public CompletableFuture<PlayerStats> getPlayerStats(UUID uuid) {
        return core.getAsyncManager().completableFuture(() -> {
            try (Connection conn = core.getDatabaseManager().getConnection()) {
                return getPlayerStats(conn, uuid);
            } catch (SQLException e) {
                throw new UnCheckedSQLException(e);
            }
        });
    }

    public void reloadLevelingRoles() {
        try {
            File levelingRoles = new File(core.getPlatform().getDataFolder(), core.fileseparator + "leveling-roles.json");
            if (!levelingRoles.exists()) {
                levelingRoles.createNewFile();
                FileWriter writer = new FileWriter(levelingRoles);
                writer.write("{\n\n}");
                writer.close();
                levelingRolesRaw = new JSONObject();
            } else {
                levelingRolesRaw = new JSONObject(Utils.readFile(levelingRoles));
            }
        } catch (FileNotFoundException e) {
            core.getLogger().severe("Error creating leveling-roles.json");
            levelingRolesRaw = new JSONObject();
        } catch (IOException e) {
            core.getLogger().severe("Error creating leveling-roles.json: " + e.getMessage());
        } catch (JSONException e) {
            core.getLogger().severe("Error loading leveling-roles.json: " + e.getMessage());
        }
    }

    public PlayerStats getCachedStats(UUID uuid) {
        return cachedUUIDS.get(uuid);
    }

    public PlayerStats getCachedStats(long discordID) {
        UUID uuid = core.getDiscordSRV().getUuid(discordID + "");
        if (uuid == null) return null;
        return cachedUUIDS.get(uuid);
    }

    public boolean isLinked(UUID uuid) {
        String discord = core.getDiscordSRV().getDiscordId(uuid);
        if (discord == null) return false;
        return true;
    }

    public CompletableFuture<PlayerStats> getPlayerStats(long discordID) {
        return core.getAsyncManager().completableFuture(() -> {
            UUID uuid = core.getDiscordSRV().getUuid(discordID + "");
            if (uuid == null) return null;
            return core.getAsyncManager().handleCFOnAnother(getPlayerStats(uuid));
        });
    }

    public CompletableFuture<PlayerStats> getPlayerStats(String name) {
        return core.getAsyncManager().completableFuture(() -> {
            try (Connection conn = core.getDatabaseManager().getConnection()) {
                return getPlayerStats(conn, name);
            } catch (SQLException e) {
                throw new UnCheckedSQLException(e);
            }
        });
    }


    public PlayerStats getPlayerStats(Connection conn, UUID uuid) throws SQLException {
        PreparedStatement p1 = conn.prepareStatement("SELECT * FROM leveling ORDER BY Level DESC");
        ResultSet r1 = p1.executeQuery();
        int num = 0;
        while (r1.next()) {
            num++;
            if (r1.getString("UUID").equals(uuid.toString())) {
                return getPlayerStats(r1, num);
            }
        }
        return null;
    }

    public PlayerStats getPlayerStats(Connection conn, String name) throws SQLException {
        PreparedStatement p1 = conn.prepareStatement("SELECT * FROM leveling ORDER BY Level DESC ");
        ResultSet r1 = p1.executeQuery();
        int num = 0;
        while (r1.next()) {
            num++;
            if (r1.getString("Name").equalsIgnoreCase(name)) {
                return getPlayerStats(r1, num);
            }
        }
        return null;
    }

    public PlayerStats getPlayerStats(ResultSet r, int rank) throws SQLException {
        PlayerStats stats = new PlayerStats(core, UUID.fromString(r.getString("UUID")), r.getString("Name"), r.getInt("level"), r.getInt("xp"), r.getInt("MinecraftMessages"), r.getInt("DiscordMessages"), rank);
        if (!adding)
            cachedUUIDS.put(stats.getUuid(), stats);
        return stats;
    }

    public CompletableFuture<List<PlayerStats>> getLeaderboard(int max) {
        return core.getAsyncManager().completableFuture(() -> {
            try (Connection conn = core.getDatabaseManager().getConnection()) {
                PreparedStatement p1 = conn.prepareStatement("SELECT * FROM leveling ORDER BY Level DESC ");
                List<PlayerStats> leaderboard = new ArrayList<>();
                ResultSet r1 = p1.executeQuery();
                int num = 0;
                while (r1.next()) {
                    num++;
                    if (num <= max) {
                        leaderboard.add(getPlayerStats(r1, num));
                    }
                }
                return leaderboard;
            } catch (SQLException e) {
                throw new UnCheckedSQLException(e);
            }
        });
    }

    public Role getRoleForLevel(int level) {
        Map<String, Object> map = levelingRolesRaw.toMap();
        List<String> keys = new ArrayList<>(map.keySet());
        if (keys.isEmpty()) return null;
        keys = keys.stream().filter(num -> Integer.parseInt(num) <= level).collect(Collectors.toList());
        keys.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o2) - Integer.parseInt(o1);
            }
        });
        Long id = (Long) map.get(keys.get(0));
        if (id != null) {
            return core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(id);
        }
        return null;
    }

    public List<Role> getRolesToRemove(Integer level) {
        List<Role> roles = new ArrayList<>();
        Map<String, Object> map = levelingRolesRaw.toMap();
        List<Object> values = new ArrayList<>(map.values());
        for (Object value : values) {
            Long id = (Long) value;
            roles.add(core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(id));
        }
        if (level != null)
            roles.remove(getRoleForLevel(level));
        return roles;
    }
}
