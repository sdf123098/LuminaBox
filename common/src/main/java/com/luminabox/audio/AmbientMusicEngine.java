package com.luminabox.audio;

import com.luminabox.config.MusicRuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.BossEvent;

import java.lang.reflect.Field;
import java.util.Map;

public class AmbientMusicEngine {
    private String lastPlayedTrackId = "";
    private long cooldownExpirationTime = 0;
    private final java.util.Set<String> missingTracksCache = new java.util.HashSet<>();
    private static Field eventsFieldCache = null;
    private static boolean reflectionFailed = false;

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        CustomMusicManager manager = CustomMusicManager.getInstance();
        if (manager.isManualPlaybackActive()) {
            return;
        }

        // 1. Detect Dimension
        String currentDimension = client.level.dimension().identifier().toString();

        // 2. Detect Biome
        Holder<Biome> biomeEntry = client.level.getBiome(client.player.blockPosition());
        String currentBiome = biomeEntry.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");

        // 3. Detect Boss Fight
        String bossName = detectBossFight(client);

        // 4. Detect Combat
        boolean combat = detectCombat(client);

        // 5. Determine target track based on priority rules
        MusicTrack targetTrack = determineTargetTrack(client, currentDimension, currentBiome, bossName, combat);

        MusicTrack currentPlaying = manager.getCurrentTrack();

        String targetId = targetTrack != null ? targetTrack.getId() : "";
        String currentId = currentPlaying != null ? currentPlaying.getId() : "";

        boolean fileExists = true;
        if (targetTrack != null) {
            if (missingTracksCache.contains(targetId)) {
                fileExists = false;
            } else if (!targetId.equals(lastPlayedTrackId)) {
                java.io.File file = null;
                if (targetTrack.getSourceType() == MusicTrack.SourceType.LOCAL || targetTrack.getSourceType() == MusicTrack.SourceType.SERVER) {
                    file = new java.io.File(manager.getLocalMusicFolder(), targetTrack.getSourcePathOrUrl());
                } else if (targetTrack.getSourceType() == MusicTrack.SourceType.PLATFORM) {
                    file = new java.io.File(manager.getLocalMusicFolder(), "platform_" + targetTrack.getId() + ".wav");
                }
                if (file != null && (!file.exists() || !file.isFile())) {
                    fileExists = false;
                    missingTracksCache.add(targetId);
                }
            }
        }

        if (!targetId.isEmpty()) {
            if (!fileExists) {
                if (!targetId.equals(lastPlayedTrackId)) {
                    com.luminabox.LuminaBox.LOGGER.warn("Target track file is missing: {}. Disabling playback for this rule.", targetTrack.getSourcePathOrUrl());
                    lastPlayedTrackId = targetId;
                    cooldownExpirationTime = 0;
                    if (currentPlaying != null) {
                        manager.stop();
                    }
                }
            } else {
                if (currentPlaying == null) {
                    // If it finished naturally, set a cooldown so it doesn't loop instantly
                    if (targetId.equals(lastPlayedTrackId)) {
                        if (cooldownExpirationTime == 0) {
                            // Just finished, set 30s cooldown
                            cooldownExpirationTime = System.currentTimeMillis() + 30000;
                        } else if (System.currentTimeMillis() >= cooldownExpirationTime) {
                            // Cooldown expired, play again
                            manager.play(targetTrack);
                            cooldownExpirationTime = 0;
                        }
                    } else {
                        // Playing a new/different track
                        manager.play(targetTrack);
                        lastPlayedTrackId = targetId;
                        cooldownExpirationTime = 0;
                    }
                } else if (!currentId.equals(targetId)) {
                    // State changed, override immediately
                    manager.play(targetTrack);
                    lastPlayedTrackId = targetId;
                    cooldownExpirationTime = 0;
                }
            }
        } else {
            // No custom music matches the current state
            if (currentPlaying != null) {
                manager.stop();
            }
            lastPlayedTrackId = "";
            cooldownExpirationTime = 0;
        }
    }

    private MusicTrack determineTargetTrack(Minecraft client, String dimension, String biome, String bossName, boolean combat) {
        MusicRuleManager rules = MusicRuleManager.getInstance();
        MusicTrack track = null;

        if (bossName != null) {
            // Specific boss track, e.g. boss_wither
            track = rules.getMatchTrack("boss", bossName.toLowerCase().replace(" ", "_"));
            if (track == null) {
                track = rules.getMatchTrack("boss", "default");
            }
        }

        if (track == null && combat) {
            track = rules.getMatchTrack("combat", "default");
        }

        if (track == null) {
            track = rules.getMatchTrack("biome", biome);
        }

        if (track == null) {
            track = rules.getMatchTrack("dimension", dimension);
        }

        return track;
    }

    private String detectBossFight(Minecraft client) {
        if (reflectionFailed) return null;
        try {
            var bossOverlay = client.gui.getBossOverlay();
            if (bossOverlay == null) return null;

            if (eventsFieldCache == null) {
                try {
                    eventsFieldCache = bossOverlay.getClass().getDeclaredField("events");
                } catch (NoSuchFieldException e) {
                    // Try alternate names if needed, or fail gracefully
                    reflectionFailed = true;
                    return null;
                }
                eventsFieldCache.setAccessible(true);
            }

            Map<?, ?> events = (Map<?, ?>) eventsFieldCache.get(bossOverlay);
            if (events != null && !events.isEmpty()) {
                for (Object value : events.values()) {
                    if (value instanceof BossEvent bossEvent) {
                        return bossEvent.getName().getString();
                    }
                }
                return "default";
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean detectCombat(Minecraft client) {
        if (client.player == null) return false;
        int currentTicks = client.player.tickCount;
        int lastHurtByMob = client.player.getLastHurtByMobTimestamp();
        int lastHurtMob = client.player.getLastHurtMobTimestamp();

        // 100 ticks = 5 seconds
        boolean recentlyHurtByMob = (lastHurtByMob > 0) && (currentTicks - lastHurtByMob > 0) && (currentTicks - lastHurtByMob < 100);
        boolean recentlyHurtMob = (lastHurtMob > 0) && (currentTicks - lastHurtMob > 0) && (currentTicks - lastHurtMob < 100);

        return recentlyHurtByMob || recentlyHurtMob;
    }
}
