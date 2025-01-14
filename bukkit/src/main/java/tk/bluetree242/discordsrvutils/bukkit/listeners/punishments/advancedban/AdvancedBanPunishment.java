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

package tk.bluetree242.discordsrvutils.bukkit.listeners.punishments.advancedban;

import tk.bluetree242.discordsrvutils.interfaces.Punishment;
import tk.bluetree242.discordsrvutils.utils.Utils;


public class AdvancedBanPunishment implements Punishment {
    private final me.leoko.advancedban.utils.Punishment punishment;

    public AdvancedBanPunishment(me.leoko.advancedban.utils.Punishment punishment) {
        this.punishment = punishment;
    }

    @Override
    public String getDuration() {
        if (punishment.getEnd() == -1)
            return "Permanent";
        return Utils.getDuration((punishment.getEnd() - punishment.getStart()) + 1);
    }

    @Override
    public String getOperator() {
        return punishment.getOperator();
    }

    @Override
    public String getName() {
        return punishment.getName();
    }

    @Override
    public String getReason() {
        return punishment.getReason();
    }
}
