package com.oskarsmc.message.command;

import cloud.commandframework.Command;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.oskarsmc.message.configuration.UserData;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Message Toggle command
 */
public final class MessageToggleCommand {

    /**
     * Construct the message toggle command.
     *
     * @param commandManager Command Manager
     * @param userData User Data
     */
    @Inject
    public MessageToggleCommand(@NotNull VelocityCommandManager<CommandSource> commandManager, UserData userData) {
        Command.Builder<CommandSource> builder = commandManager.commandBuilder("msgtoggle").permission("osmc.message.toggle");

        commandManager.command(builder
                .senderType(Player.class)
                .literal("on")
                .handler(context -> {
                    Player player = (Player) context.getSender();
                    UUID playerUUID = player.getUniqueId();
                    userData.saveUserMessageState(playerUUID, true);
                    player.sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle.on"));
                })
        );

        commandManager.command(builder
                .senderType(Player.class)
                .literal("off")
                .handler(context -> {
                    Player player = (Player) context.getSender();
                    UUID playerUUID = player.getUniqueId();
                    userData.saveUserMessageState(playerUUID, false);
                    player.sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle.off"));
                })
        );

        commandManager.command(builder
                .senderType(Player.class)
                .handler(context -> {
                    Player player = (Player) context.getSender();
                    UUID playerId = player.getUniqueId();
                    boolean canBeMessaged = userData.getUserMessageState(playerId);
                    userData.saveUserMessageState(playerId, !canBeMessaged);
                    player.sendMessage(Component.translatable("oskarsmc.message.command.msgtoggle." + (canBeMessaged ? "off" : "on")));
                })
        );

    }
}
