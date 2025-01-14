package tk.bluetree242.discordsrvutils.commands.discord.other;

import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;
import tk.bluetree242.discordsrvutils.DiscordSRVUtils;
import tk.bluetree242.discordsrvutils.systems.commandmanagement.Command;
import tk.bluetree242.discordsrvutils.systems.commandmanagement.CommandEvent;

public class LinkAccountCommand extends Command {
    public LinkAccountCommand(DiscordSRVUtils core) {
        super(core, "linkaccount", "Link your Discord Account with InGame Account Using Code", "[P]linkaccount <code>", null,
                new OptionData(OptionType.INTEGER, "code", "LinkAccount Code", true));
    }

    @Override
    public void run(CommandEvent e) throws Exception {
        Integer code = (int) e.getOption("code").getAsLong();
        String response = core.getPlatform().getDiscordSRV().proccessMessage(code + "", e.getAuthor());
        if (response != null) e.reply(response).setEphemeral(true).queue();
    }
}
