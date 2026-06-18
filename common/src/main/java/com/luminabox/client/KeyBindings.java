package com.luminabox.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String CATEGORY = "key.categories.luminabox.main";
    public static KeyMapping openMusicMenuKey;

    public static void init() {
        openMusicMenuKey = new KeyMapping(
            "key.luminabox.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
        );
    }
}
