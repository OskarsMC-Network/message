package com.oskarsmc.message.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.oskarsmc.message.configuration.MessageSettings;
import com.velocitypowered.api.command.CommandSource;

public class ReplyCommand {
    @Inject
    public MessageSettings settings;

    @Inject
    public ReplyCommand(VelocityCommandManager<CommandSource> commandManager) {
        Command.Builder<CommandSource> builder = commandManager.commandBuilder("reply", settings.getReplyAlias().toArray(new String[0]));

        commandManager.command(builder.argument(StringArgument.greedy("message"), ArgumentDescription.of("The message to send to the last player you contacted.")));
    }
}
