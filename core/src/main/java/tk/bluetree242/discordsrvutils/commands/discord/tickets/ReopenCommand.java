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

package tk.bluetree242.discordsrvutils.commands.discord.tickets;

import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.systems.commandmanagement.Command;
import tk.bluetree242.discordsrvutils.systems.commandmanagement.CommandCategory;
import tk.bluetree242.discordsrvutils.systems.commandmanagement.CommandEvent;
import tk.bluetree242.discordsrvutils.systems.tickets.TicketManager;

public class ReopenCommand extends Command {
    public ReopenCommand() {
        super("reopen", "Reopen the ticket command executed on", "[P]reopen", null, CommandCategory.TICKETS, "reopenticket");
    }

    @Override
    public void run(CommandEvent e) throws Exception {
        DiscordSRVUtils.get().getAsyncManager().handleCF(core.getTicketManager().getTicketByChannel(e.getChannel().getIdLong()), ticket -> {
            if (ticket == null) {
                e.replyErr("You are not in a ticket").queue();
                return;
            }
            if (!ticket.isClosed()) {
                e.replyErr("Ticket is already opened").queue();
            } else {
                e.reply("Reopening Ticket...").setEphemeral(true).queue();
                DiscordSRVUtils.get().getAsyncManager().handleCF(ticket.reopen(e.getAuthor()), null, err -> {
                    DiscordSRVUtils.get().getErrorHandler().defaultHandle(err);
                });
            }
        }, err -> {
            DiscordSRVUtils.get().getErrorHandler().defaultHandle(err);
        });
    }
}