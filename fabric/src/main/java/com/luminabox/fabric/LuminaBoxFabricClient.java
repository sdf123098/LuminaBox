package com.luminabox.fabric;

import com.luminabox.LuminaBoxClient;
import com.luminabox.client.KeyBindings;
import com.luminabox.network.PlayMusicPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class LuminaBoxFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LuminaBoxClient.init();

        // Register keybinding in Fabric
        KeyBindingHelper.registerKeyBinding(KeyBindings.openMusicMenuKey);

        // Register client receiver for play music payload
        ClientPlayNetworking.registerGlobalReceiver(com.luminabox.network.SyncPlaylistPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                com.luminabox.network.CommonNetworkLogic.handleSyncPlaylistOnClient(payload);
            });
        });
        // Register client tick event to update music engine
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (LuminaBoxClient.getAmbientMusicEngine() != null) {
                LuminaBoxClient.getAmbientMusicEngine().tick(client);
            }
            // Check key press
            while (KeyBindings.openMusicMenuKey.consumeClick()) {
                client.setScreen(new com.luminabox.client.gui.MusicPlayerScreen());
            }
        });

        LuminaBoxClient.LOGGER.info("Dynamic BGM Fabric client initialized.");
    }
}
