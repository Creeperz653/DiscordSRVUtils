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

package tk.bluetree242.discordsrvutils.systems.leveling.listeners.jda;


import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.member.GuildMemberJoinEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import lombok.RequiredArgsConstructor;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.events.DiscordLevelupEvent;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObject;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObjectList;
import tk.bluetree242.discordsrvutils.systems.leveling.MessageType;
import tk.bluetree242.discordsrvutils.systems.leveling.PlayerStats;

import java.security.SecureRandom;

@RequiredArgsConstructor
public class DiscordLevelingListener extends ListenerAdapter {
    private final DiscordSRVUtils core;

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if (core.getMainConfig().bungee_mode()) return;
        core.getAsyncManager().executeAsync(() -> {
            if (e.getMessage().isWebhookMessage()) return;
            if (e.getAuthor().isBot()) return;
            if (core.getPlatform().getDiscordSRV().getMainGuild().getIdLong() == core.getPlatform().getDiscordSRV().getMainGuild().getIdLong()) {
                if (core.getLevelingConfig().enabled()) {
                    core.getAsyncManager().handleCF(core.getLevelingManager().getPlayerStats(e.getMember().getIdLong()), stats -> {
                        if (stats == null) {
                            return;
                        }
                        if (core.getLevelingConfig().antispam_messages()) {
                            Long val = core.getLevelingManager().antispamMap.get(stats.getUuid());
                            if (val == null) {
                                core.getLevelingManager().antispamMap.put(stats.getUuid(), System.nanoTime());
                            } else {
                                if (!(System.nanoTime() - val >= core.getLevelingManager().MAP_EXPIRATION_NANOS))
                                    return;
                                core.getLevelingManager().antispamMap.remove(stats.getUuid());
                                core.getLevelingManager().antispamMap.put(stats.getUuid(), System.nanoTime());
                            }
                        }
                        int toAdd = new SecureRandom().nextInt(50);
                        boolean leveledUp = core.getAsyncManager().handleCFOnAnother(stats.setXP(stats.getXp() + toAdd, new DiscordLevelupEvent(stats, e.getChannel(), e.getAuthor())));
                        core.getAsyncManager().handleCFOnAnother(stats.addMessage(MessageType.DISCORD));
                        if (leveledUp) {
                            core.queueMsg(core.getMessageManager().getMessage(core.getLevelingConfig().discord_message(), PlaceholdObjectList.ofArray(core,
                                    new PlaceholdObject(core, stats, "stats"),
                                    new PlaceholdObject(core, e.getAuthor(), "user"),
                                    new PlaceholdObject(core, e.getMember(), "member"),
                                    new PlaceholdObject(core, core.getPlatform().getDiscordSRV().getMainGuild(), "guild")
                            ), null).build(), core.getJdaManager().getChannel(core.getLevelingConfig().discord_channel(), e.getChannel())).queue();
                        }
                    }, null);
                }
            }
        });
    }


    //give leveling roles when they rejoin the discord server
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        core.getAsyncManager().executeAsync(() -> {
            if (core.getDiscordSRV().getUuid(e.getUser().getId()) != null) {
                PlayerStats stats = core.getAsyncManager().handleCFOnAnother(core.getLevelingManager().getPlayerStats(e.getUser().getIdLong()));
                if (stats == null) return;
                Role role = core.getLevelingManager().getRoleForLevel(stats.getLevel());
                if (role != null) {
                    core.getPlatform().getDiscordSRV().getMainGuild().addRoleToMember(e.getMember(), role).reason("User ReJoined").queue();
                }
            }
        });
    }
}