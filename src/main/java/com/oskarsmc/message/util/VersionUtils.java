package com.oskarsmc.message.util;

import com.oskarsmc.message.configuration.MessageSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities relating to versioning.
 */
public final class VersionUtils {

    /**
     * The latest config version.
     */
    public static final double CONFIG_VERSION = 1.3;

    /**
     * Check if the Message Settings object is up-to-date.
     * @param messageSettings The message settings object to check.
     * @return If the message settings object it up-to-date.
     */
    public static boolean isLatestConfigVersion(@NotNull MessageSettings messageSettings) {
        if (messageSettings.configVersion() == null) {
            return false;
        }
        return messageSettings.configVersion() == CONFIG_VERSION;
    }
}
