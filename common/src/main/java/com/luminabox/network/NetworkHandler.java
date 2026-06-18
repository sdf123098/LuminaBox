package com.luminabox.network;

import java.util.ServiceLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface NetworkHandler {
    void sendToServer(CustomPacketPayload payload);

    static NetworkHandler getInstance() {
        return ServiceLoader.load(NetworkHandler.class).findFirst().orElseThrow(() -> new IllegalStateException("No NetworkHandler implementation found"));
    }
}
