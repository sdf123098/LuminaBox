package com.luminabox.fabric;

import com.luminabox.LuminaBox;
import com.luminabox.network.CommonNetworkLogic;
import com.luminabox.network.FileUploadPayload;
import com.luminabox.network.PlayMusicPayload;
import com.luminabox.network.SyncPlaylistPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class LuminaBoxFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LuminaBox.init();

        // Register custom packet payload
        PayloadTypeRegistry.playS2C().register(PlayMusicPayload.TYPE, PlayMusicPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FileUploadPayload.TYPE, FileUploadPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPlaylistPayload.TYPE, SyncPlaylistPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FileUploadPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                CommonNetworkLogic.handleFileUploadOnServer(payload, context.player(), context.server());
            });
        });

        LuminaBox.LOGGER.info("Dynamic BGM Fabric initialized.");
    }
}
