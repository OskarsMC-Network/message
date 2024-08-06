package com.oskarsmc.message;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.CloudInjectionModule;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.oskarsmc.message.command.MessageCommand;
import com.oskarsmc.message.command.MessageToggleCommand;
import com.oskarsmc.message.command.ReplyCommand;
import com.oskarsmc.message.command.SocialSpyCommand;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.configuration.UserData;
import com.oskarsmc.message.locale.CommandExceptionHandler;
import com.oskarsmc.message.locale.TranslationManager;
import com.oskarsmc.message.logic.MessageMetrics;
import com.oskarsmc.message.util.CloudSuggestionProcessor;
import com.oskarsmc.message.util.DependencyChecker;
import com.oskarsmc.message.util.MessageModule;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * The main class for the message plugin.
 */
public final class Message {
    @Inject
    private Injector injector;

    @Inject
    private Logger logger;

    @Inject
    private @DataDirectory
    @NotNull Path dataFolder;

    /**
     * Initialise the plugin.
     *
     * @param event Proxy Initialise Event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        MessageSettings messageSettings = new MessageSettings(dataFolder, logger);
        UserData userData = new UserData(dataFolder, logger);

        injector = injector.createChildInjector(
                new CloudInjectionModule<>(
                        CommandSource.class,
                        CommandExecutionCoordinator.simpleCoordinator(),
                        Function.identity(),
                        Function.identity()
                ),
                new MessageModule(messageSettings)
        );

        if (messageSettings.enabled()) {
            injector.getInstance(TranslationManager.class);

            if (messageSettings.luckpermsIntegration()) {
                if (DependencyChecker.luckperms()) {
                    logger.info("LuckPerms integration enabled. Targeted LuckPerms version 5.4, using {}", LuckPermsProvider.get().getPluginMetadata().getVersion());
                } else {
                    logger.warn("LuckPerms integration was enabled but LuckPerms was not detected on the proxy. Continuing without it.");
                    messageSettings.luckpermsIntegration(false);
                }
            }

            if (messageSettings.miniPlaceholdersIntegration()) {
                if (DependencyChecker.miniplaceholders()) {
                    logger.info("MiniPlaceholders integration enabled.");
                } else {
                    logger.warn("MiniPlaceholders integration was enabled but MiniPlaceholders was not detected on the proxy. Continuing without it.");
                    messageSettings.miniPlaceholdersIntegration(false);
                }
            }

            // Register custom exception handlers
            injector.getInstance(CommandExceptionHandler.class);

            // Allow autocompletion regardless of capitalisation
            injector.getInstance(Key.get(new TypeLiteral<VelocityCommandManager<CommandSource>>() {
            })).commandSuggestionProcessor(new CloudSuggestionProcessor());

            // Commands
            injector.getInstance(MessageCommand.class);
            injector.getInstance(MessageToggleCommand.class);
            injector.getInstance(SocialSpyCommand.class);
            injector.getInstance(ReplyCommand.class);

            // Metrics
            injector.getInstance(MessageMetrics.class);
        }

        logger.info("Loaded message {}", getClass().getPackage().getImplementationVersion());
    }
}
