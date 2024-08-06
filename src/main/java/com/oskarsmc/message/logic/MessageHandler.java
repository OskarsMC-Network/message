package com.oskarsmc.message.logic;

import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.event.MessageEvent;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The handler that handles all messages sent via the plugin.
 */
public final class MessageHandler {
    private final MessageSettings messageSettings;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ConcurrentHashMap<Player, Player> conversations = new ConcurrentHashMap<>();

    /**
     * Conversation Watchers
     */
    public final List<CommandSource> conversationWatchers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Construct the Message Handler.
     *
     * @param messageSettings Message Settings
     */
    public MessageHandler(MessageSettings messageSettings) {
        this.messageSettings = messageSettings;
    }

    /**
     * Handle the message event once it's been fired.
     *
     * @param event The Message Event instance.
     */
    public void handleMessageEvent(@NotNull MessageEvent event) {
        if (!messageSettings.selfMessageSending() && event.sender() == event.recipient()) {
            event.sender().sendMessage(Component.translatable("oskarsmc.message.command.common.self-sending-error", NamedTextColor.RED));
            return;
        }

        if (!event.getResult().isAllowed()) {
            return;
        }

        Component senderName = event.sender() instanceof Player player ? Component.text(player.getUsername()) : Component.text("UNKNOWN");
        Component receiverName = Component.text(event.recipient().getUsername());

        String senderServer = event.sender() instanceof Player player
                ? player.getCurrentServer().map(sv -> sv.getServerInfo().getName()).orElse("UNKNOWN")
                : "UNKNOWN";
        String receiverServer = event.recipient().getCurrentServer()
                .map(sv -> sv.getServerInfo().getName()).orElse("UNKNOWN");

        TagResolver.Builder builder = TagResolver.builder()
                .resolver(Placeholder.component("sender", senderName))
                .resolver(Placeholder.component("receiver", receiverName))
                .resolver(Placeholder.unparsed("sender_server", senderServer))
                .resolver(Placeholder.unparsed("receiver_server", receiverServer))
                .resolver(Placeholder.unparsed("message", event.getResult().string() != null
                        ? Objects.requireNonNull(event.getResult().string())
                        : event.originalMessage()));

        if (messageSettings.luckpermsIntegration()) {
            LuckPerms luckPerms = LuckPermsProvider.get();

            PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);

            CachedMetaData senderMetaData = event.sender() instanceof Player player
                    ? playerAdapter.getUser(player).getCachedData().getMetaData()
                    : null;

            CachedMetaData recipientMetaData = playerAdapter.getUser(event.recipient()).getCachedData().getMetaData();

            builder.resolver(craftLuckpermsPlaceholders("sender", senderMetaData))
                    .resolver(craftLuckpermsPlaceholders("receiver", recipientMetaData));
        } else {
            builder.resolver(craftPlaceholders());
        }

        if (messageSettings.miniPlaceholdersIntegration()) {
            builder.resolver(MiniPlaceholders.getRelationalGlobalPlaceholders(event.sender(), event.recipient()));
        }

        TagResolver placeholders = builder.resolver(event.extraPlaceholders()).build();

        Component senderMessage = miniMessage.deserialize(messageSettings.messageSentMiniMessage(), placeholders);
        Component receiverMessage = miniMessage.deserialize(messageSettings.messageReceivedMiniMessage(), placeholders);

        event.sender().sendMessage(senderMessage);
        event.recipient().sendMessage(receiverMessage);

        if (event.sender() instanceof Player player) {
            conversations.remove(event.recipient());
            conversations.put(event.recipient(), player);
        }

        Component socialSpyComponent = miniMessage.deserialize(messageSettings.messageSocialSpyMiniMessage(), placeholders);
        for (CommandSource watcher : conversationWatchers) {
            watcher.sendMessage(socialSpyComponent);
        }
    }

    private @NotNull TagResolver craftLuckpermsPlaceholders(String role, CachedMetaData cachedMetaData) {
        return cachedMetaData == null
                ? TagResolver.resolver(
                Placeholder.component(role + "_prefix", Component.empty()),
                Placeholder.component(role + "_suffix", Component.empty()),
                Placeholder.component(role + "_group", Component.empty()))
                : TagResolver.resolver(
                Placeholder.component(role + "_prefix", LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNullElse(cachedMetaData.getPrefix(), ""))),
                Placeholder.component(role + "_suffix", LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNullElse(cachedMetaData.getSuffix(), ""))),
                Placeholder.component(role + "_group", Component.text(Objects.requireNonNullElse(cachedMetaData.getPrimaryGroup(), "")))
        );
    }

    private @NotNull TagResolver craftPlaceholders() {
        return TagResolver.resolver(
                Placeholder.component("sender_prefix", Component.empty()),
                Placeholder.component("sender_suffix", Component.empty()),
                Placeholder.component("sender_group", Component.empty()),
                Placeholder.component("receiver_prefix", Component.empty()),
                Placeholder.component("receiver_suffix", Component.empty()),
                Placeholder.component("receiver_group", Component.empty())
        );
    }

    /**
     * Get all current conversations.
     *
     * @return An unmodifiable map with all conversations.
     */
    public @NotNull @UnmodifiableView Map<Player, Player> conversations() {
        return Collections.unmodifiableMap(conversations);
    }
}
