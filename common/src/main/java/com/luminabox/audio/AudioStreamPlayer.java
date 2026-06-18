package com.luminabox.audio;

import com.luminabox.LuminaBox;
import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner;

public class AudioStreamPlayer implements Runnable {
    // Shared LavaPlayer manager for the entire application
    private static final AudioPlayerManager MANAGER;
    static {
        MANAGER = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(MANAGER);
        
        try {
            MANAGER.registerSourceManager(new dev.lavalink.bilibili.BilibiliAudioSourceManager());
        } catch (Exception e) {
            LuminaBox.LOGGER.error("Failed to register BilibiliAudioSourceManager", e);
        }
        
        AudioSourceManagers.registerRemoteSources(MANAGER);
        
        try {
            // "内置模型仅在在线搜歌那里": Initialize the Youtube Rotator module
            // We use an empty block list, which still helps bypass some limitations or allows it to be injected later
            NanoIpRoutePlanner planner = new NanoIpRoutePlanner(Collections.emptyList(), true);
            new YoutubeIpRotatorSetup(planner)
                .forManager(MANAGER)
                .withRetryLimit(3)
                .setup();
        } catch (Exception e) {
            LuminaBox.LOGGER.warn("Failed to setup YouTube IP Rotator", e);
        }

        MANAGER.setHttpRequestConfigurator((config) -> {
            String proxyHost = com.luminabox.config.ModConfig.getInstance().getProxyHost();
            int proxyPort = com.luminabox.config.ModConfig.getInstance().getProxyPort();
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                return org.apache.http.client.config.RequestConfig.copy(config)
                        .setProxy(new org.apache.http.HttpHost(proxyHost, proxyPort))
                        .build();
            }
            return config;
        });

        MANAGER.getConfiguration().setOutputFormat(StandardAudioDataFormats.COMMON_PCM_S16_LE);
    }

    public static void search(String identifier, java.util.function.Consumer<List<AudioTrack>> callback) {
        if (com.luminabox.config.ModConfig.getInstance().isEnableDebugLog()) {
            LuminaBox.LOGGER.info("[NetworkSearchDebug] Initiating search for: " + identifier);
        }
        MANAGER.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (com.luminabox.config.ModConfig.getInstance().isEnableDebugLog()) {
                    LuminaBox.LOGGER.info("[NetworkSearchDebug] Single track loaded successfully: " + track.getInfo().title);
                }
                List<AudioTrack> res = new ArrayList<>();
                res.add(track);
                callback.accept(res);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (com.luminabox.config.ModConfig.getInstance().isEnableDebugLog()) {
                    LuminaBox.LOGGER.info("[NetworkSearchDebug] Playlist loaded (" + playlist.getTracks().size() + " tracks) successfully: " + playlist.getName());
                }
                callback.accept(playlist.getTracks());
            }
            @Override
            public void noMatches() {
                if (com.luminabox.config.ModConfig.getInstance().isEnableDebugLog()) {
                    LuminaBox.LOGGER.info("[NetworkSearchDebug] No matches found for: " + identifier);
                }
                callback.accept(new ArrayList<>());
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                LuminaBox.LOGGER.error("[NetworkSearchDebug] Search failed with exception for: " + identifier, exception);
                callback.accept(new ArrayList<>());
            }
        });
    }

    private final String identifier;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private volatile boolean failed = false;
    private volatile float volumeMultiplier = 0.0f;

    private SourceDataLine line;
    private final Object pauseLock = new Object();
    private AudioPlayer player;
    private AudioTrack currentTrack;

    public AudioStreamPlayer(String identifier, float initialVolume) {
        this.identifier = identifier;
        this.volumeMultiplier = Math.max(0.0f, Math.min(1.0f, initialVolume));
    }

    public boolean hasFailed() { return failed; }

    public double getPlaybackPositionSeconds() {
        if (currentTrack != null) {
            return currentTrack.getPosition() / 1000.0;
        }
        return 0.0;
    }

    public double getDurationSeconds() {
        if (currentTrack != null) {
            return currentTrack.getDuration() / 1000.0;
        }
        return 0.0;
    }

    public void setVolume(float volume) {
        this.volumeMultiplier = Math.max(0.0f, Math.min(1.0f, volume));
        if (player != null) {
            player.setVolume((int)(this.volumeMultiplier * 100));
        }
    }

    public void seekTo(double seconds) {
        if (currentTrack != null) {
            currentTrack.setPosition((long)(seconds * 1000));
        }
    }

    public void pausePlayback() {
        this.paused = true;
        if (player != null) player.setPaused(true);
        if (line != null) line.stop();
    }

    public void resumePlayback() {
        synchronized (pauseLock) {
            this.paused = false;
            if (player != null) player.setPaused(false);
            if (line != null) line.start();
            pauseLock.notifyAll();
        }
    }

    public void stopPlayback() {
        this.running = false;
        if (player != null) player.stopTrack();
        resumePlayback();
    }

    @Override
    public void run() {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(AudioStreamPlayer.class.getClassLoader());
            playAudio();
        } catch (Throwable e) {
            this.failed = true;
            LuminaBox.LOGGER.error("Error during music playback: ", e);
            try { CustomMusicManager.getInstance().setLastStatus("Error: " + e.getClass().getSimpleName()); } catch (Exception ignored) {}
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            cleanup();
            try { CustomMusicManager.getInstance().onPlayerFinished(this); } catch (Exception ignored) {}
        }
    }

    private void debugLog(String message) {
        if (com.luminabox.config.ModConfig.getInstance().isEnableDebugLog()) {
            LuminaBox.LOGGER.info("[AudioDebug] " + message);
        }
    }

    private void checkPause() throws InterruptedException {
        if (paused && running) {
            synchronized (pauseLock) {
                while (paused && running) pauseLock.wait(100);
            }
        }
    }

    private void playAudio() throws Exception {
        debugLog("Attempting to play audio using LavaPlayer: " + identifier);
        
        CountDownLatch latch = new CountDownLatch(1);
        final AudioTrack[] loadedTrack = new AudioTrack[1];
        final Exception[] loadError = new Exception[1];
        
        MANAGER.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                loadedTrack[0] = track;
                latch.countDown();
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!playlist.getTracks().isEmpty()) {
                    loadedTrack[0] = playlist.getTracks().get(0);
                } else {
                    loadError[0] = new Exception("LavaPlayer found empty playlist.");
                }
                latch.countDown();
            }
            @Override
            public void noMatches() {
                loadError[0] = new Exception("LavaPlayer found no matches for file.");
                latch.countDown();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                loadError[0] = exception;
                latch.countDown();
            }
        });
        
        latch.await();
        
        if (loadError[0] != null) {
            throw loadError[0];
        }
        if (loadedTrack[0] == null) {
            throw new Exception("LavaPlayer failed to load the track (unknown reason).");
        }
        
        this.currentTrack = loadedTrack[0];
        this.player = MANAGER.createPlayer();
        this.player.setVolume((int)(this.volumeMultiplier * 100));
        this.player.playTrack(this.currentTrack);
        
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        
        while (running) {
            checkPause();
            if (!running) break;
            
            AudioTrackState state = currentTrack.getState();
            if (state == AudioTrackState.FINISHED || state == AudioTrackState.STOPPING || state == AudioTrackState.INACTIVE) {
                break; // Track ended
            }
            
            try {
                AudioFrame frame = player.provide(10L, TimeUnit.MILLISECONDS);
                if (frame != null) {
                    byte[] data = frame.getData();
                    if (data != null && data.length > 0) {
                        line.write(data, 0, data.length);
                    }
                }
            } catch (TimeoutException | InterruptedException e) {
                // Ignore timeout, loop will re-check track state at the top
            } catch (Exception e) {
                LuminaBox.LOGGER.error("Playback error: ", e);
                this.failed = true;
                break;
            }
        }
    }

    private void cleanup() {
        try {
            if (player != null) {
                player.destroy();
            }
        } catch (Exception ignored) {}
        
        try {
            if (line != null) {
                line.flush();
                line.close();
            }
        } catch (Exception ignored) {}
    }
}
