package com.luminabox;

import com.luminabox.audio.AmbientMusicEngine;
import com.luminabox.client.KeyBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuminaBoxClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("LuminaBoxClient");
    private static AmbientMusicEngine ambientMusicEngine;

    public static void init() {
        LOGGER.info("Initializing Dynamic BGM Mod client-side...");
        
        // Initialize keybindings
        KeyBindings.init();

        // Initialize BGM Engine
        ambientMusicEngine = new AmbientMusicEngine();
    }

    public static AmbientMusicEngine getAmbientMusicEngine() {
        return ambientMusicEngine;
    }
}
