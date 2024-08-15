package com.oskarsmc.message.configuration;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * User Data class
 */
public final class UserData {
    private final Path dataFolder;
    private final Path file;

    /**
     * Construct userdata.
     *
     * @param dataFolder Data Folder
     * @param logger Logger
     */
    @Inject
    public UserData(@DataDirectory @NotNull Path dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.file = this.dataFolder.resolve("userdata.yml");

        createUserDataFile();
    }

    /**
     * Save the user message state.
     *
     * @param playerUUID Player UUID
     * @param canBeMessaged Can be messaged
     */
    public void saveUserMessageState(UUID playerUUID, boolean canBeMessaged) {
        final ConfigurationNode yaml = loadConfig();
        yaml.getNode("users", playerUUID.toString(), "canBeMessaged").setValue(canBeMessaged);
        saveUserData(yaml);
    }

    /**
     * Get the user message state.
     *
     * @param playerUUID Player UUID
     * @return Can be messaged
     */
    public boolean getUserMessageState(UUID playerUUID) {
        final ConfigurationNode yaml = loadConfig();
        return yaml.getNode("users", playerUUID.toString(), "canBeMessaged").getBoolean(true);
    }

    private void createUserDataFile() {
        if (!Files.exists(dataFolder)) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!Files.exists(file)) {
            try (InputStream in = MessageSettings.class.getResourceAsStream("/userdata.yml")) {
                assert in != null;
                Files.copy(in, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private @NotNull Path userDataFile() {
        return this.file;
    }

    private ConfigurationNode loadConfig() {
        try {
            return YAMLConfigurationLoader.builder().setPath(this.file).build().load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserData(ConfigurationNode yaml) {
        try {
            YAMLConfigurationLoader.builder().setPath(this.file).build().save(yaml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}