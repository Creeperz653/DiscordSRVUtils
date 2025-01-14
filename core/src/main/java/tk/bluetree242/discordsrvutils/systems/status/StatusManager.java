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

package tk.bluetree242.discordsrvutils.systems.status;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.exceptions.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObject;
import tk.bluetree242.discordsrvutils.placeholder.PlaceholdObjectList;
import tk.bluetree242.discordsrvutils.utils.FileWriter;
import tk.bluetree242.discordsrvutils.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class StatusManager {
    private static StatusManager main;
    private final DiscordSRVUtils core;
    private StatusTimer timer = new StatusTimer(this);

    public Path getDataPath() {
        return Paths.get(core.getPlatform().getDataFolder() + core.fileseparator + "data" + core.fileseparator + "status-message.json");
    }


    public Message getStatusMessage(boolean online) {
        PlaceholdObjectList holders = new PlaceholdObjectList(core);
        holders.add(new PlaceholdObject(core, core.getServer().getOriginal(), "server"));
        return core.getMessageManager().parseMessageFromJson(core.getMessageManager().getMessageJSONByName("status-" + (online ? "online" : "offline")), holders, null).build();
    }

    public CompletableFuture<Message> newMessage(TextChannel channel) {
        return core.getAsyncManager().completableFuture(() -> {
            //path for some temp storage which should not be stored in database
            File file = getDataPath().toFile();
            JSONObject json = new JSONObject();
            json.put("channel", channel.getIdLong());
            //Its already async, complete() should be fine
            Message msg = channel.sendMessage(getStatusMessage(true)).complete();
            json.put("message", msg.getIdLong());
            try {
                if (!file.exists()) {
                    getDataPath().getParent().toFile().mkdirs();
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file);
                writer.write(json.toString());
                writer.close();
                //Should be written successfully
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return msg;
        });
    }

    public Long getMessageId() throws IOException {
        File file = getDataPath().toFile();
        if (!file.exists()) return null;
        JSONObject json = new JSONObject(Utils.readFile(file.getPath()));
        return json.getLong("message");
    }

    public Long getChannelId() throws IOException {
        File file = getDataPath().toFile();
        if (!file.exists()) return null;
        JSONObject json = new JSONObject(Utils.readFile(file.getPath()));
        return json.getLong("channel");
    }

    public CompletableFuture<Void> editMessage(boolean online) {
        return core.getAsyncManager().completableFutureRun(() -> {
            Message toSend = getStatusMessage(online);
            try {
                Long messageId = getMessageId();
                Long channelId = getChannelId();
                if (messageId == null || channelId == null) return;
                Message msg = Objects.requireNonNull(core.getPlatform().getDiscordSRV().getMainGuild().getTextChannelById(channelId)).retrieveMessageById(messageId).complete();
                if (msg == null) return;
                //Its async so it should be fine.. complete() to make sure it does it before discordsrv shuts down when doing offline message
                msg.editMessage(toSend).complete();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
                //Ignore the error for now
            } catch (ErrorResponseException ex) {
                //message does not exist, ok that is fine
            }

        });
    }

    public void registerTimer() {
        if (timer != null) {
            timer.cancel();
        }
        new Timer().schedule(timer = new StatusTimer(this), 1000, core.getStatusConfig().update_delay() * 1000);
    }

    public void unregisterTimer() {
        if (timer != null)
            timer.cancel();
        timer = null;
    }

    public void reloadTimer() {
        unregisterTimer();
        registerTimer();
    }
}
