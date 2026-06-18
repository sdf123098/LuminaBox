package com.luminabox.network;

import com.luminabox.audio.CustomMusicManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CommonNetworkLogic {

    // temporary storage for incoming file chunks on the server
    private static final Map<String, byte[]> fileUploadBuffers = new HashMap<>();
    
    public static void handleFileUploadOnServer(FileUploadPayload payload, ServerPlayer player, net.minecraft.server.MinecraftServer server) {
        String fileName = payload.fileName();
        byte[] data = payload.data();
        int chunkIndex = payload.chunkIndex();
        int totalChunks = payload.totalChunks();

        // In a real scenario, this should be scoped per-player or use a unique ID to prevent overlap
        String key = player.getUUID().toString() + "_" + fileName;

        try {
            if (chunkIndex == 0) {
                fileUploadBuffers.put(key, new byte[0]);
            }

            byte[] existing = fileUploadBuffers.getOrDefault(key, new byte[0]);
            byte[] combined = new byte[existing.length + data.length];
            System.arraycopy(existing, 0, combined, 0, existing.length);
            System.arraycopy(data, 0, combined, existing.length, data.length);
            fileUploadBuffers.put(key, combined);

            if (chunkIndex == totalChunks - 1) {
                // Final chunk received, save file
                Path serverMusicDir = server.getServerDirectory().resolve("config").resolve("luminabox").resolve("music");
                if (!Files.exists(serverMusicDir)) {
                    Files.createDirectories(serverMusicDir);
                }

                File outFile = new File(serverMusicDir.toFile(), fileName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(fileUploadBuffers.get(key));
                }
                
                fileUploadBuffers.remove(key);
                player.sendSystemMessage(Component.literal("File uploaded successfully: " + fileName));
                
                // Trigger sync playlist back to all clients
                // For now, let's keep it simple.
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileUploadBuffers.remove(key);
        }
    }

    public static void handleSyncPlaylistOnClient(SyncPlaylistPayload payload) {
        // Here we parse JSON and update the UI's server tab
        // CustomMusicManager.getInstance().updateServerPlaylist(payload.jsonPlaylist());
    }
}
