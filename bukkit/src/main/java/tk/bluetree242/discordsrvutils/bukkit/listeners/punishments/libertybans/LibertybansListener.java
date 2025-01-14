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

package tk.bluetree242.discordsrvutils.bukkit.listeners.punishments.libertybans;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.*;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.PlayerVictim;
import space.arim.libertybans.api.Victim;
import space.arim.libertybans.api.event.PostPardonEvent;
import space.arim.libertybans.api.event.PostPunishEvent;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.EventConsumer;
import space.arim.omnibus.events.ListenerPriorities;
import space.arim.omnibus.events.RegisteredListener;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObject;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObjectList;

public class LibertybansListener {
    private final LibertyBans plugin;
    private final DiscordSRVUtils core;
    private final RegisteredListener pListener;
    private final RegisteredListener pardonListener;

    public LibertybansListener(DiscordSRVUtils core) {
        this.core = core;
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        plugin = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();
        pListener = omnibus.getEventBus().registerListener(PostPunishEvent.class, ListenerPriorities.NORMAL, new PunishmentListener());
        pardonListener = omnibus.getEventBus().registerListener(PostPardonEvent.class, ListenerPriorities.NORMAL, new PardonListener());
    }

    public void unregister() {
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        if (pListener != null)
            omnibus.getEventBus().unregisterListener(pListener);
        if (pardonListener != null)
            omnibus.getEventBus().unregisterListener(pardonListener);
    }

    private void syncPunishment(Punishment punishment, boolean un) {
        if (punishment.getVictim().getType() == Victim.VictimType.ADDRESS) return;
        PlayerVictim victim = (PlayerVictim) punishment.getVictim();
        String id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(victim.getUUID());
        if (id == null) return;
        User discordUser = core.getJDA().retrieveUserById(id).complete();
        if (!un) {
            Member discordMember = core.getPlatform().getDiscordSRV().getMainGuild().retrieveMember(discordUser).complete();
            if (discordMember == null) return;
            if (!core.getPlatform().getDiscordSRV().getMainGuild().getSelfMember().canInteract(discordMember)) return;
            if (!core.getBansConfig().isSyncPunishmentsWithDiscord()) return;
            switch (punishment.getType()) {
                case BAN:
                    Role bannedRole = core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(core.getBansConfig().bannedRole());
                    if (bannedRole == null)
                        core.getPlatform().getDiscordSRV().getMainGuild().ban(discordUser, 0, "Minecraft Synced Ban").queue();
                    else if (core.getPlatform().getDiscordSRV().getMainGuild().getSelfMember().canInteract(bannedRole))
                        core.getPlatform().getDiscordSRV().getMainGuild().addRoleToMember(discordMember, bannedRole).reason("Minecraft Synced Ban").queue();
                    else {
                        core.severe("Could not add Banned role to " + discordUser.getName() + ". Please make sure the bot's role is higher than the banned role");
                    }
                    break;
                case MUTE:
                    Role role = core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(core.getBansConfig().mutedRole());
                    if (role == null) {
                        if (core.getBansConfig().mutedRole() != 0)
                            core.severe("No Role was found with id " + core.getBansConfig().mutedRole());
                        return;
                    }
                    core.getPlatform().getDiscordSRV().getMainGuild().addRoleToMember(discordUser.getIdLong(), role).reason("Mute Synced with Minecraft").queue();
                    break;
                default:
                    break;
            }
        } else {
            if (!core.getBansConfig().isSyncUnpunishmentsWithDiscord()) return;
            switch (punishment.getType()) {
                case BAN:
                    Role bannedRole = core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(core.getBansConfig().bannedRole());
                    if (bannedRole == null)
                        core.getPlatform().getDiscordSRV().getMainGuild().unban(discordUser).reason("Minecraft Synced UnBan").queue();
                    else if (core.getPlatform().getDiscordSRV().getMainGuild().getSelfMember().canInteract(bannedRole))
                        core.getPlatform().getDiscordSRV().getMainGuild().removeRoleFromMember(discordUser.getIdLong(), bannedRole).reason("Minecraft Synced UnBan").queue();
                    else {
                        core.severe("Could not remove Banned role from " + discordUser.getName() + ". Please make sure the bot's role is higher than the banned role");
                    }
                    break;
                case MUTE:
                    Role role = core.getPlatform().getDiscordSRV().getMainGuild().getRoleById(core.getBansConfig().mutedRole());
                    if (role == null) {
                        if (core.getBansConfig().mutedRole() != 0)
                            core.severe("No Role was found with id " + core.getBansConfig().mutedRole());
                        return;
                    }
                    core.getPlatform().getDiscordSRV().getMainGuild().removeRoleFromMember(discordUser.getIdLong(), role).reason("Unmute Synced with Minecraft").queue();
                default:
                    break;
            }
        }
    }

    public class PunishmentListener implements EventConsumer<PostPunishEvent> {

        @Override
        public void accept(PostPunishEvent e) {
            core.getAsyncManager().executeAsync(() -> {
                LibertyBansPunishment punishment = new LibertyBansPunishment(e.getPunishment(), e.getPunishment().getOperator());

                Message msg = null;
                switch (e.getPunishment().getType()) {
                    case BAN:
                        msg = core.getMessageManager().getMessage(core.getBansConfig().bannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        if (e.getPunishment().isTemporary()) {
                            msg = core.getMessageManager().getMessage(core.getBansConfig().tempBannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        }
                        if (e.getPunishment().getVictim().getType() == Victim.VictimType.ADDRESS) {
                            if (e.getPunishment().isTemporary())
                                msg = core.getMessageManager().getMessage(core.getBansConfig().TempIPBannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                            else {
                                msg = core.getMessageManager().getMessage(core.getBansConfig().IPBannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                            }
                        }

                        break;

                    case MUTE:
                        msg = core.getMessageManager().getMessage(core.getBansConfig().MutedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        if (e.getPunishment().isTemporary()) {
                            msg = core.getMessageManager().getMessage(core.getBansConfig().TempMutedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        }
                        break;
                    default:
                        break;
                }
                if (msg != null) {
                    if (core.getBansConfig().isSendPunishmentmsgesToDiscord()) {
                        TextChannel channel = core.getJdaManager().getChannel(core.getBansConfig().channel_id());
                        if (channel == null) {
                            core.severe("No channel was found with id " + core.getBansConfig().channel_id() + " For Punishment message");
                            return;
                        } else
                            core.queueMsg(msg, channel).queue();
                    }
                }
                syncPunishment(e.getPunishment(), false);
            });
        }
    }

    public class PardonListener implements EventConsumer<PostPardonEvent> {

        @Override
        public void accept(PostPardonEvent e) {
            core.getAsyncManager().executeAsync(() -> {

                LibertyBansPunishment punishment = new LibertyBansPunishment(e.getPunishment(), e.getOperator());

                Message msg = null;
                switch (e.getPunishment().getType()) {
                    case BAN:
                        msg = core.getMessageManager().getMessage(core.getBansConfig().unbannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        if (e.getPunishment().getVictim().getType() == Victim.VictimType.ADDRESS) {
                            msg = core.getMessageManager().getMessage(core.getBansConfig().unipbannedMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        }
                        break;
                    case MUTE:
                        msg = core.getMessageManager().getMessage(core.getBansConfig().unmuteMessage(), PlaceholdObjectList.ofArray(core, new PlaceholdObject(core, punishment, "punishment")), null).build();
                        break;
                    default:
                        break;
                }
                if (msg != null) {
                    if (core.getBansConfig().isSyncUnpunishmentsmsgWithDiscord()) {
                        TextChannel channel = core.getJdaManager().getChannel(core.getBansConfig().channel_id());
                        if (channel == null) {
                            core.severe("No channel was found with id " + core.getBansConfig().channel_id() + " For UnPunishment message");
                            return;
                        }
                        core.queueMsg(msg, channel).queue();
                    }
                }
                syncPunishment(e.getPunishment(), true);
            });
        }
    }
}
