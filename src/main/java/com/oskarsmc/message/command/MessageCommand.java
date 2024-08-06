package com.oskarsmc.message.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.minecraft.extras.RichDescription;
import cloud.commandframework.velocity.VelocityCommandManager;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.google.inject.Inject;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.configuration.UserData;
import com.oskarsmc.message.event.MessageEvent;
import com.oskarsmc.message.logic.MessageHandler;
import com.oskarsmc.message.util.DefaultPermission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The message command class.
 */
public final class MessageCommand {
    /**
     * Construct the message command.
     * @param messageSettings Message Settings
     * @param commandManager Command Manager
     * @param proxyServer Proxy Server
     * @param messageHandler Message Handler
     * @param userData User Data
     */
    @Inject
    public MessageCommand(@NotNull MessageSettings messageSettings, @NotNull VelocityCommandManager<CommandSource> commandManager, ProxyServer proxyServer, MessageHandler messageHandler, UserData userData) {
        Command.Builder<CommandSource> builder = commandManager.commandBuilder("message", messageSettings.messageAliases().toArray(new String[0]));

        commandManager.command(builder
                .argument(PlayerArgument.of("player"), RichDescription.translatable("oskarsmc.message.command.message.argument.player-argument"))
                .argument(StringArgument.of("message", StringArgument.StringMode.GREEDY), RichDescription.translatable("oskarsmc.message.command.common.argument.message-description"))
                .permission(new DefaultPermission("osmc.message.send"))
                .handler(context -> {
                    Player receiver = context.get("player");
                    if (!userData.getUserMessageState(receiver.getUniqueId()) && !context.getSender().hasPermission("osmc.message.bypass")) {
                        context.getSender().sendMessage(Component.translatable("oskarsmc.messages.disabled"));
                    } else {
                        proxyServer.getEventManager().fire(new MessageEvent(
                                context.getSender(),
                                receiver,
                                context.get("message")
                        )).thenAccept(messageHandler::handleMessageEvent);
                    }
                })
        );
    }
}
