package com.luminabox.neoforge;

import com.luminabox.LuminaBox;
import com.luminabox.LuminaBoxClient;
import com.luminabox.client.KeyBindings;
import com.luminabox.network.PlayMusicPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(LuminaBox.MOD_ID)
public class LuminaBoxNeoForge {

    public LuminaBoxNeoForge(IEventBus modEventBus) {
        LuminaBox.init();

        modEventBus.addListener(this::onRegisterKeymappings);
        modEventBus.addListener(this::registerPayloads);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init();
        }

        LuminaBox.LOGGER.info("Dynamic BGM NeoForge initialized.");
    }

    private void onRegisterKeymappings(RegisterKeyMappingsEvent event) {
        LuminaBoxClient.init();
        event.register(KeyBindings.openMusicMenuKey);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        registrar.playToClient(
            PlayMusicPayload.TYPE,
            PlayMusicPayload.CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    String trackId = payload.trackId();
                    String sourceUrl = payload.sourceUrl();
                    if (payload.isPlay()) {
                        com.luminabox.audio.MusicTrack track = new com.luminabox.audio.MusicTrack(
                            trackId,
                            trackId,
                            "Server-Synced",
                            sourceUrl,
                            com.luminabox.audio.MusicTrack.SourceType.SERVER,
                            180
                        );
                        com.luminabox.audio.CustomMusicManager.getInstance().play(track);
                    } else {
                        com.luminabox.audio.CustomMusicManager.getInstance().stop();
                    }
                });
            }
        );

        registrar.playToServer(
            com.luminabox.network.FileUploadPayload.TYPE,
            com.luminabox.network.FileUploadPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    com.luminabox.network.CommonNetworkLogic.handleFileUploadOnServer(
                        (com.luminabox.network.FileUploadPayload) payload, 
                        (net.minecraft.server.level.ServerPlayer) context.player(),
                        net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer()
                    );
                });
            }
        );

        registrar.playToClient(
            com.luminabox.network.SyncPlaylistPayload.TYPE,
            com.luminabox.network.SyncPlaylistPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    com.luminabox.network.CommonNetworkLogic.handleSyncPlaylistOnClient((com.luminabox.network.SyncPlaylistPayload) payload);
                });
            }
        );
    }

    public static class ClientSetup {
        public static void init() {
            NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event) -> {
                net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                if (LuminaBoxClient.getAmbientMusicEngine() != null) {
                    LuminaBoxClient.getAmbientMusicEngine().tick(client);
                }

                // Key press check in tick
                while (KeyBindings.openMusicMenuKey.consumeClick()) {
                    client.setScreen(new com.luminabox.client.gui.MusicPlayerScreen());
                }
            });

            NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ScreenEvent.Init.Post event) -> {
                if (event.getScreen() instanceof net.minecraft.client.gui.screens.PauseScreen) {
                    event.addListener(
                        net.minecraft.client.gui.components.Button.builder(
                            net.minecraft.network.chat.Component.translatable("gui.luminabox.pause_menu_btn"),
                            button -> net.minecraft.client.Minecraft.getInstance().setScreen(new com.luminabox.client.gui.MusicPlayerScreen())
                        ).bounds(10, 10, 90, 20).build()
                    );
                }
            });
        }
    }
}
