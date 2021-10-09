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

package tk.bluetree242.discordsrvutils.embeds;



import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Embed {

    public static MessageEmbed error(String msg) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setDescription("❌ **" + msg + "**");
        return embed.build();
    }

    public static MessageEmbed error(String msg, String footer) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setFooter(footer);
        embed.setDescription("❌ **" + msg + "**");
        return embed.build();
    }

    public static MessageEmbed success(String msg) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setDescription("✅ **" + msg + "**");
        return embed.build();
    }
}
