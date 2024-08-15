package com.oskarsmc.message.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.*;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * The logger class. This class is responsible for logging messages to the console and to a file.
 */
public class Logger {

    private static org.slf4j.Logger logger = null;
    private static org.apache.logging.log4j.Logger fileLogger = LogManager.getLogger(Logger.class);
    private static Path dataFolder = null;

    /**
     * Set the log context.
     *
     * @param dataFolder Data Folder
     * @param logger     Logger
     */
    public static void setLogContext(Path dataFolder, org.slf4j.Logger logger) {
        Logger.logger = logger;
        Logger.dataFolder = dataFolder;
        intializeLogger();
    }

    private static void intializeLogger(){
        if (!dataFolder.resolve("logs").toFile().exists()) {
            dataFolder.resolve("logs").toFile().mkdir();
        }

        Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern("%d{MM-dd-yy HH:mm:ss.SSS} %m%n")
                .build();
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.newRootLogger(org.apache.logging.log4j.Level.INFO);
        builder.setConfigurationName("ConversationsLogger");
        Configuration configuration = builder.build();
        IfLastModified ifLastModified = IfLastModified.createAgeCondition(Duration.parse("P14D"));
        IfFileName ifFileName = IfFileName.createNameCondition("*/conversations-*.log.gz", null);
        DeleteAction deleteAction = DeleteAction.createDeleteAction(
                dataFolder.toString() + "/logs/", false, 1, false, null,
                new PathCondition[]{ifLastModified,ifFileName}, null, configuration);
        Action[] actions = new Action[]{deleteAction};
        RollingFileAppender rollingAppender = RollingFileAppender.newBuilder()
                .setName("ConversationFile")
                .withFileName(dataFolder.toString() + "/logs/conversations.log")
                .withFilePattern(dataFolder.toString() + "/logs/conversations-%d{MM-dd-yyyy}.log.gz")
                .withAppend(true)
                .setLayout(layout)
                .withStrategy(DefaultRolloverStrategy.newBuilder()
                        .withMax("20")
                        .withMin("1")
                        .withCompressionLevelStr("1")
                        .withFileIndex("min")
                        .withCustomActions(actions)
                        .withStopCustomActionsOnError(true)
                        .build())
                .withPolicy(TimeBasedTriggeringPolicy.newBuilder().withInterval(1).build())
                .setConfiguration(configuration)
                .build();
        rollingAppender.start();
        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) fileLogger;
        coreLogger.addAppender(rollingAppender);
        coreLogger.info("Conversation Logger initialized");
    }

    /**
     * Add a log to the console and file.
     *
     * @param log Component to log
     */
    public static synchronized void addLog(Component log) {
        if (logger == null || dataFolder == null) {
            return;
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(log);
        logger.info(plain);
        fileLogger.info(plain);
    }
}
