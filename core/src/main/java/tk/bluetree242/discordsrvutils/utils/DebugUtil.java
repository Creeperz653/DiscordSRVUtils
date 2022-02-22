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

package tk.bluetree242.discordsrvutils.utils;

import github.scarsz.discordsrv.dependencies.okhttp3.OkHttpClient;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;

import java.security.SecureRandom;

// This idea is taken from discordsrv, and i do not own the bin
// I Copied some of the original discordsrv code for some reason. This code isn't 100% mine
public class DebugUtil {
    public static String run() throws Exception {
        return run(null);
    }

    public static String run(String stacktrace) throws Exception {
        return DiscordSRVUtils.get().getServer().getDebugger().run(stacktrace);
    }
}