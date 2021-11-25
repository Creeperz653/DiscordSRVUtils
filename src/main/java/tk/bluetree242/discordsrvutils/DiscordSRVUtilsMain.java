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

package tk.bluetree242.discordsrvutils;

import github.scarsz.discordsrv.DiscordSRV;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DiscordSRVUtilsMain extends JavaPlugin {
    private DiscordSRVUtils core;

    public void onEnable() {
        core.onEnable();
        //bstats stuff
        Metrics metrics = new Metrics(this, 9456);
        metrics.addCustomChart(new AdvancedPie("features", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            //Removed Tickets Because it caused lag on a few servers
                /*
                if (!TicketManager.get().getPanels().get().isEmpty())
                valueMap.put("Tickets", 1);
                 */
            if (core.getLevelingConfig().enabled()) valueMap.put("Leveling", 1);
            if (core.getSuggestionsConfig().enabled()) valueMap.put("Suggestions", 1);
            if (core.getMainConfig().welcomer_enabled()) valueMap.put("Welcomer", 1);
            if (core.getBansConfig().isSendPunishmentmsgesToDiscord() && isAnyPunishmentsPluginInstalled())
                valueMap.put("Punishment Messages", 1);
            if (getServer().getPluginManager().isPluginEnabled("Essentials") && core.getMainConfig().afk_message_enabled())
                valueMap.put("AFK Messages", 1);
            return valueMap;
        }));
        metrics.addCustomChart(new SimplePie("discordsrv_versions", () -> DiscordSRV.getPlugin().getDescription().getVersion()));
        metrics.addCustomChart(new SimplePie("admins", () -> core.getAdminIds().size() + ""));
    }

    public void onDisable() {

    }

    public void onLoad() {
        core = new DiscordSRVUtils(this);
    }

    public void disable() {
        setEnabled(false);
    }

    private boolean isAnyPunishmentsPluginInstalled() {
        if (getServer().getPluginManager().isPluginEnabled("AdvancedBan")) return true;
        if (getServer().getPluginManager().isPluginEnabled("Litebans")) return true;
        if (getServer().getPluginManager().isPluginEnabled("Libertybans")) return true;
        return false;
    }
}
