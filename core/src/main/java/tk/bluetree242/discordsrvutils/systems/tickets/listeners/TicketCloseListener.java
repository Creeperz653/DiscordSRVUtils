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

package tk.bluetree242.discordsrvutils.systems.tickets.listeners;


import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import lombok.RequiredArgsConstructor;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;

@RequiredArgsConstructor
public class TicketCloseListener extends ListenerAdapter {

    private final DiscordSRVUtils core;

    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
        if (core.getMainConfig().bungee_mode()) return;
        core.getAsyncManager().handleCF(core.getTicketManager().getTicketByMessageId(e.getMessageIdLong()), ticket -> {
            if (ticket != null) {
                if (e.getUser().isBot()) return;
                if (e.getReactionEmote().getName().equals("\uD83D\uDD12")) {
                    e.getReaction().removeReaction(e.getUser()).queue();
                    if (!ticket.isClosed())
                        ticket.close(e.getUser());
                } else if (e.getReactionEmote().getName().equals("\uD83D\uDDD1️")) {
                    e.getReaction().removeReaction(e.getUser()).queue();
                    if (ticket.isClosed()) {
                        ticket.delete();
                    }
                }
                if (e.getReactionEmote().getName().equals("\uD83D\uDD13")) {
                    e.getReaction().removeReaction(e.getUser()).queue();
                    if (ticket.isClosed()) {
                        core.getAsyncManager().handleCF(ticket.reopen(e.getUser()), null, ex -> {
                            core.getErrorHandler().defaultHandle(ex);
                        });
                    }
                }
            }
        }, null);
    }

    public void onButtonClick(ButtonClickEvent e) {
        if (core.getMainConfig().bungee_mode()) return;
        core.getAsyncManager().handleCF(core.getTicketManager().getTicketByMessageId(e.getMessageIdLong()), ticket -> {
            if (ticket != null) {
                if (e.getUser().isBot()) return;
                if (e.getButton().getId().equals("close_ticket")) {
                    e.deferEdit().queue();
                    if (!ticket.isClosed())
                        ticket.close(e.getUser());
                } else if (e.getButton().getId().equals("delete_ticket")) {
                    e.deferEdit().queue();
                    if (ticket.isClosed()) {
                        ticket.delete();
                    }
                }
                if (e.getButton().getId().equals("reopen_ticket")) {
                    e.deferEdit().queue();
                    if (ticket.isClosed()) {
                        core.getAsyncManager().handleCF(ticket.reopen(e.getUser()), null, ex -> {
                            core.getErrorHandler().defaultHandle(ex);
                        });
                    }
                } else return;
            }
        }, null);
    }
}
