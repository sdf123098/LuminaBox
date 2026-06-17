package com.luminabox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuminaBox {
    public static final String MOD_ID = "luminabox";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Initializing Dynamic BGM Mod common resources...");
        java.io.File configDir = new java.io.File(System.getProperty("user.dir"), "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        com.luminabox.config.ModConfig.init(configDir);
        com.luminabox.audio.CustomMusicManager.getInstance().reloadConfig();
        // Here we initialize configuration, network packets, etc.
    }
}
