package com.oskarsmc.message.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.oskarsmc.message.command.MessageCommand;
import com.oskarsmc.message.command.MessageToggleCommand;
import com.oskarsmc.message.command.ReplyCommand;
import com.oskarsmc.message.command.SocialSpyCommand;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.logic.MessageHandler;
import com.oskarsmc.message.logic.MessageMetrics;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The message guice module.
 */
public final class MessageModule extends AbstractModule {
    private final MessageSettings messageSettings;

    /**
     * Construct the message guice module
     *
     * @param messageSettings The message settings to use when providing other values.
     */
    public MessageModule(MessageSettings messageSettings) {
        this.messageSettings = messageSettings;
    }

    @Override
    protected void configure() {
        bind(MessageSettings.class).toInstance(messageSettings);
        bind(MessageCommand.class).in(Singleton.class);
        bind(MessageToggleCommand.class).in(Singleton.class);
        bind(ReplyCommand.class).in(Singleton.class);
        bind(SocialSpyCommand.class).in(Singleton.class);
        bind(MessageMetrics.class).in(Singleton.class);
    }

    @Contract(" -> new")
    @Singleton
    @Provides
    private @NotNull MessageHandler provideHandler() {
        return new MessageHandler(messageSettings);
    }
}
