package com.oskarsmc.message.command;

import cloud.commandframework.Command;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.logic.MessageHandler;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Message Toggle command
 */
public final class MessageToggleCommand {
    @Inject
    private MessageHandler messageHandler;

    /**
     * Construct the social spy command.
     *
     * @param messageSettings Message Settings
     * @param commandManager  Command Manager
     */
    @Inject
    public MessageToggleCommand(@NotNull MessageSettings messageSettings, @NotNull VelocityCommandManager<CommandSource> commandManager) {
        Command.Builder<CommandSource> builder = commandManager.commandBuilder("msgtoggle").permission("osmc.message.toggle");

        commandManager.command(builder
                .senderType(Player.class)
                .literal("on")
                .handler(context -> {
                    messageHandler.canBeMessaged.put(context.getSender(), true);
                    context.getSender().sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle.on"));
                })
        );

        commandManager.command(builder
                .senderType(Player.class)
                .literal("off")
                .handler(context -> {
                    messageHandler.canBeMessaged.put(context.getSender(), false);
                    context.getSender().sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle.off"));
                })
        );

        commandManager.command(builder
                .senderType(Player.class)
                .handler(context -> {
                    boolean canBeMessaged = messageHandler.canBeMessaged.getOrDefault(context.getSender(), true);
                    messageHandler.canBeMessaged.put(context.getSender(), !canBeMessaged);
                    context.getSender().sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle." + (canBeMessaged ? "off" : "on")));

                })
        );

    }
}
