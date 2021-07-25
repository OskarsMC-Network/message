package com.oskarsmc.message.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.velocity.VelocityCommandManager;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.google.inject.Inject;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.event.MessageEvent;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageCommand {
    @Inject
    private ProxyServer proxyServer;

    @Inject
    private MessageSettings settings;

    @Inject
    public MessageCommand(VelocityCommandManager<CommandSource> commandManager) {
        Command.Builder<CommandSource> builder = commandManager.commandBuilder("message", settings.getMessageAlias().toArray(new String[0]));

        commandManager.command(builder.argument(PlayerArgument.of("player"), ArgumentDescription.of("The player to send the message to."))
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("The message to send."))
                .handler(context -> {
                    proxyServer.getEventManager().fire(new MessageEvent(context.getSender(), context.get("player"), context.get("message"))).thenAccept(this::messageLogic);
                })
        );
    }

    public void messageLogic(MessageEvent event) {
        if (event.getResult().isAllowed()) {
            String senderName;
            String receiverName;

            if (event.sender() instanceof Player) {
                Player sender = (Player) event.sender();
                senderName = sender.getUsername();
            } else {
                senderName = "UNKNOWN";
            }


            receiverName = event.recipient().getUsername();

            MiniMessage miniMessage = MiniMessage.get();

            Map<String, String> map = new HashMap<String, String>();

            map.put("sender", senderName);
            map.put("receiver", receiverName);
            map.put("message", event.message().replace("<", "").replace(">", "")); //TODO: Remove this when minimessage releases the parser fix!

            Component senderMessage = miniMessage.parse(settings.getMessageSentMiniMessage(), map);
            Component receiverMessage = miniMessage.parse(settings.getMessageReceivedMiniMessage(), map);

            event.sender().sendMessage(senderMessage);
            event.recipient().sendMessage(receiverMessage);
        }
    }
}
