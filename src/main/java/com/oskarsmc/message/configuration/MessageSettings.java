package com.oskarsmc.message.configuration;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.oskarsmc.message.util.VersionUtils;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The settings container class for the message plugin.
 */
public final class MessageSettings {
    private final Path dataFolder;
    private final Path file;

    private String messageSentMiniMessage;
    private String messageReceivedMiniMessage;
    private String messageSocialSpyMiniMessage;
    private String messageLogFormat;

    private HashMap<Class<? extends Exception>, String> customErrorHandlers;

    private List<String> messageAlias;
    private List<String> replyAlias;
    private List<String> socialSpyAlias;
    private List<String> msgToggleAlias;

    private boolean luckpermsIntegration;
    private boolean miniPlaceholdersIntegration;
    private boolean selfMessageSending;
    private boolean loggingEnabled;

    private final double configVersion;
    private boolean enabled;

    /**
     * Construct message settings.
     *
     * @param dataFolder Data Folder
     * @param logger     Logger
     */
    @Inject
    public MessageSettings(@DataDirectory @NotNull Path dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.file = this.dataFolder.resolve("config.toml");

        saveDefaultConfig();
        Toml toml = loadConfig();

        this.enabled = toml.getBoolean("plugin.enabled");

        // Version
        this.configVersion = toml.getDouble("developer-info.config-version");

        if (!VersionUtils.isLatestConfigVersion(this)) {
            logger.warn("Your Config is out of date (Latest: {}, Config Version: {})!", VersionUtils.CONFIG_VERSION, this.configVersion());
            logger.warn("Please backup your current config.toml, and delete the current one. A new config will then be created on the next proxy launch.");
            logger.warn("The plugin's functionality will not be enabled until the config is updated.");
            this.enabled(false);
            return;
        }

        // Plugin Features
        this.luckpermsIntegration = toml.getBoolean("plugin.luckperms-integration");
        this.selfMessageSending = toml.getBoolean("plugin.allow-self-message-sending");
        this.miniPlaceholdersIntegration = toml.getBoolean("plugin.miniplaceholders-integration");
        this.loggingEnabled = toml.getBoolean("plugin.enable-logging");

        // Messages - Message
        this.messageSentMiniMessage = toml.getString("messages.message-sent");
        this.messageReceivedMiniMessage = toml.getString("messages.message-received");
        this.messageSocialSpyMiniMessage = toml.getString("messages.message-socialspy");
        this.messageLogFormat = toml.getString("messages.message-log-format");
        // Aliases
        this.messageAlias = toml.getList("aliases.message");
        this.replyAlias = toml.getList("aliases.reply");
        this.socialSpyAlias = toml.getList("aliases.socialspy");
        this.msgToggleAlias = toml.getList("aliases.toggle");

        // Exceptions
        this.customErrorHandlers = new HashMap<>();
        Toml handlers = toml.getTable("error-handlers");
        for (Map.Entry<String, Object> entry : handlers.entrySet()) {
            // TODO: Fix this jankyness, but works for now
            String classPath = entry.getKey().replace("\"", "");
            // classPath = classPath.replace("cloud.commandframework", "com.oskarsmc.message.relocated.cloud"); // cant do this because the string gets relocated
            if (classPath.startsWith("loud.commandframework", 1)) { // TODO: make this less janky
                classPath = "com.oskarsmc.message.relocated.cloud" + classPath.substring(22);
            }

            try {
                @SuppressWarnings("unchecked")
                Class<? extends Exception> exceptionClass = (Class<? extends Exception>) Class.forName(classPath);

                this.customErrorHandlers.put(exceptionClass, entry.getValue().toString());
            } catch (ClassCastException e) {
                logger.error("Class \"{}\" is not of type exception!", classPath);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                logger.error("Could not find class \"{}\" as defined in custom exception handlers.", classPath);
            }
        }
    }

    private void saveDefaultConfig() {
        if (!Files.exists(dataFolder)) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!Files.exists(file)) {
            try (InputStream in = MessageSettings.class.getResourceAsStream("/config.toml")) {
                assert in != null;
                Files.copy(in, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private @NotNull Path configFile() {
        return this.file;
    }

    private Toml loadConfig() {
        try {
            return new Toml().read(Files.newInputStream(this.file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Get the MiniMessage markup of the senders sent message.
     *
     * @return The MiniMessage markup of the senders sent message.
     */
    public String messageSentMiniMessage() {
        return this.messageSentMiniMessage;
    }

    /**
     * Get the MiniMessage markup of the recipients received message.
     *
     * @return The MiniMessage markup of the recipients received message.
     */
    public String messageReceivedMiniMessage() {
        return this.messageReceivedMiniMessage;
    }

    /**
     * Get if the plugin is enabled.
     *
     * @return If the plugin is enabled.
     */
    @Pure
    public boolean enabled() {
        return this.enabled;
    }

    /**
     * Set if the plugin is enabled.
     *
     * @param enabled If the plugin is enabled.
     */
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get luckperms integration.
     *
     * @return Luckperms integration.
     */
    @Pure
    public boolean luckpermsIntegration() {
        return this.luckpermsIntegration;
    }

    /**
     * Set luckperms integration.
     *
     * @param luckpermsIntegration Luckperms integration
     */
    public void luckpermsIntegration(boolean luckpermsIntegration) {
        this.luckpermsIntegration = luckpermsIntegration;
    }

    /**
     * Get miniplaceholders integration.
     *
     * @return MiniPlaceholders integration.
     */
    @Pure
    public boolean miniPlaceholdersIntegration() {
        return this.miniPlaceholdersIntegration;
    }

    /**
     * Set miniplaceholders integration.
     *
     * @param miniPlaceholdersIntegration MiniPlaceholders integration
     */
    public void miniPlaceholdersIntegration(boolean miniPlaceholdersIntegration) {
        this.miniPlaceholdersIntegration = miniPlaceholdersIntegration;
    }

    /**
     * Get if the config allows players sending messages to themselves.
     *
     * @return If the config allows players sending messages to themselves.
     */
    @Pure
    public boolean selfMessageSending() {
        return selfMessageSending;
    }

    /**
     * Get if the config enables the logging of messages.
     *
     * @return If the config enables the logging of messages.
     */
    @Pure
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Get the configuration version.
     *
     * @return The configuration version.
     */
    @Pure
    public Double configVersion() {
        return configVersion;
    }

    /**
     * Get the MiniMessage markup of the socialspy message.
     *
     * @return The MiniMessage markup of the socialspy message.
     */
    @Pure
    public String messageSocialSpyMiniMessage() {
        return messageSocialSpyMiniMessage;
    }

    /**
     * Get the MiniMessage markup of the log entry.
     *
     * @return The MiniMessage markup of the log entry.
     */
    @Pure
    public String messageLogFormat() {
        return messageLogFormat;
    }

    /**
     * Get the aliases of the message command.
     *
     * @return The aliases of the message command.
     */
    @Pure
    public List<String> messageAliases() {
        return messageAlias;
    }

    /**
     * Get the aliases of the socialspy command.
     *
     * @return The aliases of the socialspy command.
     */
    @Pure
    public List<String> socialSpyAliases() {
        return socialSpyAlias;
    }

    /**
     * Get the aliases of the msgtoggle command.
     *
     * @return The aliases of the msgtoggle command.
     */
    @Pure
    public List<String> msgToggleAliases() {
        return msgToggleAlias;
    }

    /**
     * Get the aliases of the reply command.
     *
     * @return The aliases of the reply command.
     */
    @Pure
    public List<String> replyAliases() {
        return replyAlias;
    }

    /**
     * Get all defined custom error handlers
     *
     * @return a map of all defined error handlers
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView Map<Class<? extends Exception>, String> getCustomErrorHandlers() {
        return Collections.unmodifiableMap(customErrorHandlers);
    }
}
