package com.oskarsmc.message.util;

import com.google.inject.AbstractModule;
import com.oskarsmc.message.Message;
import com.oskarsmc.message.configuration.MessageSettings;

public class MessageModule extends AbstractModule {
    private final MessageSettings messageSettings;
    private final Message message;

    public MessageModule(MessageSettings messageSettings, Message message) {
        this.messageSettings = messageSettings;
        this.message = message;
    }

    @Override
    protected void configure() {
        bind(MessageSettings.class)
                .toInstance(messageSettings);

        bind(Message.class)
                .toInstance(message);
    }
}
