package com.luminabox.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyMapping openMusicMenuKey;

    public static void init() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("luminabox", "main")
        );
        openMusicMenuKey = new KeyMapping(
            "key.luminabox.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            category
        );
    }
}
