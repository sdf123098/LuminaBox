package com.luminabox.audio;

import com.luminabox.LuminaBox;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.luminabox.network.NetworkHandler;
import com.luminabox.network.FileUploadPayload;

public class CustomMusicManager {
    private static final CustomMusicManager INSTANCE = new CustomMusicManager();

    private MusicTrack currentTrack;
    private float masterVolume = 0.3f; // Default volume reduced to 30%
    private PlaybackState state = PlaybackState.STOPPED;
    private final List<MusicTrack> playlist = new ArrayList<>();
    private final List<MusicTrack> onlinePlaylist = new ArrayList<>();
    private final List<MusicTrack> serverPlaylist = new ArrayList<>();
    private String searchPrefix = "bsearch:";
    private int currentTrackIndex = -1;
    private String lastStatus = "ready";

    // Manual playback queue & status flags
    private List<MusicTrack> currentPlaylist = new ArrayList<>();
    private boolean manualPlaybackActive = false;

    private AudioStreamPlayer activePlayer;
    private AudioStreamPlayer fadingOutPlayer;


    public enum PlaybackMode {
        SEQUENTIAL,
        LOOP_ALL,
        LOOP_ONE,
        SHUFFLE
    }

    private PlaybackMode playbackMode = PlaybackMode.SEQUENTIAL;
    private final java.util.Random random = new java.util.Random();

    public PlaybackMode getPlaybackMode() { return playbackMode; }
    public void setPlaybackMode(PlaybackMode mode) {
        this.playbackMode = mode;
        com.luminabox.config.ModConfig.getInstance().setPlaybackMode(mode.name());
    }
    public void cyclePlaybackMode() {
        PlaybackMode[] modes = PlaybackMode.values();
        this.playbackMode = modes[(this.playbackMode.ordinal() + 1) % modes.length];
        com.luminabox.config.ModConfig.getInstance().setPlaybackMode(this.playbackMode.name());
    }

    public enum PlaybackState {
        PLAYING,
        PAUSED,
        STOPPED
    }


    private java.nio.file.WatchService watchService;
    private Thread watchThread;

    public void startWatcher() {
        if (watchThread != null) return;
        try {
            watchService = java.nio.file.FileSystems.getDefault().newWatchService();
            java.nio.file.Path path = getLocalMusicFolder().toPath();
            path.register(watchService, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE, java.nio.file.StandardWatchEventKinds.ENTRY_DELETE, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
            watchThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        java.nio.file.WatchKey key = watchService.take();
                        for (java.nio.file.WatchEvent<?> event : key.pollEvents()) { } // Clear events
                        boolean valid = key.reset();
                        LuminaBox.LOGGER.info("Hot reload triggered for local music directory.");
                        scanLocalMusic();
                        if (!valid) break;
                    } catch (InterruptedException x) {
                        return;
                    } catch (Exception e) {
                        LuminaBox.LOGGER.error("Error in WatchService", e);
                    }
                }
            }, "LuminaBox-WatchService");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (Exception e) {
            LuminaBox.LOGGER.error("Failed to start watch service", e);
        }
    }

    private CustomMusicManager() {
        // Private constructor for singleton
        startWatcher();
        try {
            this.playbackMode = PlaybackMode.valueOf(com.luminabox.config.ModConfig.getInstance().getPlaybackMode());
        } catch (Exception e) {
            this.playbackMode = PlaybackMode.SEQUENTIAL;
        }
        this.searchPrefix = com.luminabox.config.ModConfig.getInstance().getSearchPrefix();
        this.masterVolume = com.luminabox.config.ModConfig.getInstance().getDefaultVolume();
    }

    public static CustomMusicManager getInstance() {
        return INSTANCE;
    }

    public void reloadConfig() {
        try {
            this.playbackMode = PlaybackMode.valueOf(com.luminabox.config.ModConfig.getInstance().getPlaybackMode());
        } catch (Exception e) {
            this.playbackMode = PlaybackMode.SEQUENTIAL;
        }
        this.searchPrefix = com.luminabox.config.ModConfig.getInstance().getSearchPrefix();
        this.masterVolume = com.luminabox.config.ModConfig.getInstance().getDefaultVolume();

        List<MusicTrack> savedOnline = com.luminabox.config.ModConfig.getInstance().getOnlinePlaylist();
        if (savedOnline != null) {
            this.onlinePlaylist.clear();
            this.onlinePlaylist.addAll(savedOnline);
        }
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public synchronized List<MusicTrack> getServerPlaylist() {
        if (serverPlaylist.isEmpty()) {
            serverPlaylist.add(new MusicTrack("server_1", "Lobby Ambient", "Server Admin", "server_lobby.wav", MusicTrack.SourceType.SERVER, 120));
            serverPlaylist.add(new MusicTrack("server_2", "Dungeon Combat", "Server Admin", "server_combat.wav", MusicTrack.SourceType.SERVER, 90));
            serverPlaylist.add(new MusicTrack("server_3", "Town Peaceful", "Server Admin", "server_town.wav", MusicTrack.SourceType.SERVER, 150));
        }
        return serverPlaylist;
    }

    public List<MusicTrack> getOnlinePlaylist() {
        return onlinePlaylist;
    }

    public void addToOnlinePlaylist(MusicTrack track) {
        if (track != null) {
            onlinePlaylist.add(track);
            com.luminabox.config.ModConfig.getInstance().setOnlinePlaylist(new ArrayList<>(onlinePlaylist));
        }
    }

    public String getSearchPrefix() {
        return searchPrefix;
    }

    public void setSearchPrefix(String prefix) {
        this.searchPrefix = prefix;
        com.luminabox.config.ModConfig.getInstance().setSearchPrefix(prefix);
    }

    public void generateSampleWav() {
        this.lastStatus = "downloading";
        new Thread(() -> {
            try {
                // Simulate download delay
                Thread.sleep(1200);
                
                File folder = getLocalMusicFolder();
                File target = new File(folder, "sample_synth.wav");
                
                int sampleRate = 44100;
                double duration = 5.0; // 5 seconds
                int numSamples = (int) (sampleRate * duration);
                short[] buffer = new short[numSamples];
                for (int i = 0; i < numSamples; i++) {
                    double time = i / (double) sampleRate;
                    // Synthesize a nice C major arpeggio
                    double freq = 261.63; // C4
                    if (time > 1.25 && time <= 2.5) {
                        freq = 329.63; // E4
                    } else if (time > 2.5 && time <= 3.75) {
                        freq = 392.00; // G4
                    } else if (time > 3.75) {
                        freq = 523.25; // C5
                    }
                    buffer[i] = (short) (Math.sin(2 * Math.PI * freq * time) * 6000
                                       + Math.sin(2 * Math.PI * (freq * 2) * time) * 2000);
                }
                
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
                    out.write("RIFF".getBytes());
                    writeIntLE(out, 36 + numSamples * 2);
                    out.write("WAVE".getBytes());
                    out.write("fmt ".getBytes());
                    writeIntLE(out, 16); // subchunk1size
                    writeShortLE(out, (short) 1); // PCM
                    writeShortLE(out, (short) 1); // Mono
                    writeIntLE(out, sampleRate);
                    writeIntLE(out, sampleRate * 2);
                    writeShortLE(out, (short) 2); // blockalign
                    writeShortLE(out, (short) 16); // bitspersample
                    out.write("data".getBytes());
                    writeIntLE(out, numSamples * 2);
                    for (short s : buffer) {
                        out.write(s & 0xFF);
                        out.write((s >> 8) & 0xFF);
                    }
                }
                
                this.lastStatus = "success";
                scanLocalMusic();
            } catch (Exception e) {
                this.lastStatus = "fail";
                LuminaBox.LOGGER.error("Failed to generate sample WAV file", e);
            }
        }, "LuminaBox-SampleDownloader").start();
    }

    public void downloadServerTrack(MusicTrack track) {
        new Thread(() -> {
            try {
                File folder = getLocalMusicFolder();
                File target = new File(folder, track.getSourcePathOrUrl());
                if (target.exists()) return;
                
                int sampleRate = 44100;
                double duration = 4.0;
                int numSamples = (int) (sampleRate * duration);
                short[] buffer = new short[numSamples];
                
                double baseFreq = track.getId().equals("server_2") ? 150.0 : 330.0;
                for (int i = 0; i < numSamples; i++) {
                    double time = i / (double) sampleRate;
                    double tone = Math.sin(2 * Math.PI * baseFreq * time);
                    if (track.getId().equals("server_2")) {
                        double beat = Math.sin(2 * Math.PI * 2.5 * time);
                        if (beat > 0) tone *= 0.2;
                    }
                    buffer[i] = (short) (tone * 7000);
                }
                
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
                    out.write("RIFF".getBytes());
                    writeIntLE(out, 36 + numSamples * 2);
                    out.write("WAVE".getBytes());
                    out.write("fmt ".getBytes());
                    writeIntLE(out, 16);
                    writeShortLE(out, (short) 1); // PCM
                    writeShortLE(out, (short) 1); // Mono
                    writeIntLE(out, sampleRate);
                    writeIntLE(out, sampleRate * 2);
                    writeShortLE(out, (short) 2);
                    writeShortLE(out, (short) 16);
                    out.write("data".getBytes());
                    writeIntLE(out, numSamples * 2);
                    for (short s : buffer) {
                        out.write(s & 0xFF);
                        out.write((s >> 8) & 0xFF);
                    }
                }
                scanLocalMusic();
            } catch (Exception e) {
                LuminaBox.LOGGER.error("Failed to generate server track", e);
            }
        }).start();
    }

    public synchronized void scanLocalMusic() {
        playlist.clear();
        File folder = getLocalMusicFolder();
        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp3") || lower.endsWith(".ogg") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".m4a") || lower.endsWith(".aac");
        });

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                String name = f.getName();
                String title = name;
                String artist = "Local";
                int dash = name.indexOf(" - ");
                if (dash != -1) {
                    artist = name.substring(0, dash).trim();
                    title = name.substring(dash + 3, name.lastIndexOf('.')).trim();
                } else {
                    title = name.substring(0, name.lastIndexOf('.')).trim();
                }

                playlist.add(new MusicTrack(
                    "local_" + i,
                    title,
                    artist,
                    name,
                    MusicTrack.SourceType.LOCAL,
                    180
                ));
            }
        }

        if (!playlist.isEmpty()) {
            currentTrackIndex = 0;
        } else {
            currentTrackIndex = -1;
        }
        LuminaBox.LOGGER.info("Scanned {} local music tracks.", playlist.size());
    }

    public void searchPlatform(String query, java.util.function.Consumer<List<MusicTrack>> callback) {
        String identifier = query;
        if (!identifier.startsWith("ytsearch:") && !identifier.startsWith("ytmsearch:") && !identifier.startsWith("scsearch:") && !identifier.startsWith("http")) {
            identifier = searchPrefix + identifier;
        }
        AudioStreamPlayer.search(identifier, tracks -> {
            List<MusicTrack> results = new ArrayList<>();
            for (com.sedmelluq.discord.lavaplayer.track.AudioTrack t : tracks) {
                String title = t.getInfo().title;
                String author = t.getInfo().author;
                String uri = t.getInfo().uri;
                int durationSecs = (int)(t.getDuration() / 1000);
                results.add(new MusicTrack(
                    "platform_" + uri.hashCode(),
                    title,
                    author,
                    uri,
                    MusicTrack.SourceType.PLATFORM,
                    durationSecs
                ));
            }
            callback.accept(results);
        });
    }

    public List<MusicTrack> getPlaylist() {
        return playlist;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= -1 && index < playlist.size()) {
            this.currentTrackIndex = index;
        }
    }

    public File getLocalMusicFolder() {
        File gameDir = net.minecraft.client.Minecraft.getInstance().gameDirectory;
        File musicFolder = new File(gameDir, "luminabox/local");
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        return musicFolder;
    }

    public synchronized void play(MusicTrack track) {
        if (track == null) {
            stop();
            return;
        }

        // If the track is already playing, do nothing
        if (state == PlaybackState.PLAYING && currentTrack != null && currentTrack.getId().equals(track.getId())) {
            return;
        }

        LuminaBox.LOGGER.info("Starting music playback: {}", track);

        // Resolve identifier
        String identifier = null;
        if (track.getSourceType() == MusicTrack.SourceType.LOCAL) {
            File trackFile = new File(getLocalMusicFolder(), track.getSourcePathOrUrl());
            if (trackFile.exists()) {
                identifier = trackFile.getAbsolutePath();
            }
        } else if (track.getSourceType() == MusicTrack.SourceType.SERVER) {
            File trackFile = new File(getLocalMusicFolder(), track.getSourcePathOrUrl());
            if (!trackFile.exists()) {
                // Generate server file locally (Mock)
                try {
                    int sampleRate = 44100;
                    double duration = 4.0;
                    int numSamples = (int) (sampleRate * duration);
                    short[] buffer = new short[numSamples];
                    double baseFreq = track.getId().equals("server_2") ? 150.0 : 330.0;
                    for (int i = 0; i < numSamples; i++) {
                        double time = i / (double) sampleRate;
                        double tone = Math.sin(2 * Math.PI * baseFreq * time);
                        if (track.getId().equals("server_2")) {
                            double beat = Math.sin(2 * Math.PI * 2.5 * time);
                            if (beat > 0) tone *= 0.2;
                        }
                        buffer[i] = (short) (tone * 7000);
                    }
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(trackFile)) {
                        out.write("RIFF".getBytes());
                        writeIntLE(out, 36 + numSamples * 2);
                        out.write("WAVE".getBytes());
                        out.write("fmt ".getBytes());
                        writeIntLE(out, 16);
                        writeShortLE(out, (short) 1);
                        writeShortLE(out, (short) 1);
                        writeIntLE(out, sampleRate);
                        writeIntLE(out, sampleRate * 2);
                        writeShortLE(out, (short) 2);
                        writeShortLE(out, (short) 16);
                        out.write("data".getBytes());
                        writeIntLE(out, numSamples * 2);
                        for (short s : buffer) {
                            out.write(s & 0xFF);
                            out.write((s >> 8) & 0xFF);
                        }
                    }
                    scanLocalMusic();
                } catch (Exception e) {
                    LuminaBox.LOGGER.error("Failed to auto-generate server track file", e);
                }
            }
            if (trackFile.exists()) {
                identifier = trackFile.getAbsolutePath();
            }
        } else if (track.getSourceType() == MusicTrack.SourceType.PLATFORM) {
            identifier = track.getSourcePathOrUrl();
        }

        if (identifier == null) {
            LuminaBox.LOGGER.warn("Audio identifier is null or invalid for track: {}", track);
            return;
        }

        // 1. Instantly stop existing player if any to prevent overlap
        if (activePlayer != null) {
            activePlayer.stopPlayback();
            activePlayer = null;
        }

        this.currentTrack = track;
        this.state = PlaybackState.PLAYING;

        // 2. Start new player
        activePlayer = new AudioStreamPlayer(identifier, 0.0f);
        Thread playerThread = new Thread(activePlayer, "LuminaBox-Player-" + track.getId());
        playerThread.setDaemon(true);
        playerThread.start();

        // 3. Fade in new player
        fadeIn(activePlayer);
    }

    public synchronized void play(List<MusicTrack> tracks, int index) {
        if (tracks == null || tracks.isEmpty() || index < 0 || index >= tracks.size()) {
            return;
        }
        this.currentPlaylist = new ArrayList<>(tracks);
        this.currentTrackIndex = index;
        this.manualPlaybackActive = true;
        play(currentPlaylist.get(index));
    }

    public boolean isManualPlaybackActive() {
        return manualPlaybackActive;
    }

    public void setManualPlaybackActive(boolean active) {
        this.manualPlaybackActive = active;
    }

    public double getPlaybackPosition() {
        if (activePlayer != null) {
            return activePlayer.getPlaybackPositionSeconds();
        }
        return 0.0;
    }

    public double getDuration() {
        if (activePlayer != null) {
            double dur = activePlayer.getDurationSeconds();
            if (dur > 0) return dur;
        }
        if (currentTrack != null) {
            return currentTrack.getDurationSeconds();
        }
        return 0.0;
    }

    public synchronized void pause() {
        if (state == PlaybackState.PLAYING && activePlayer != null) {
            state = PlaybackState.PAUSED;
            activePlayer.pausePlayback();
            LuminaBox.LOGGER.info("Music playback paused.");
        }
    }

    public synchronized void resume() {
        if (state == PlaybackState.PAUSED && activePlayer != null) {
            state = PlaybackState.PLAYING;
            activePlayer.resumePlayback();
            LuminaBox.LOGGER.info("Music playback resumed.");
        }
    }

    public synchronized void stop() {
        state = PlaybackState.STOPPED;
        currentTrack = null;
        manualPlaybackActive = false;
        if (activePlayer != null) {
            activePlayer.stopPlayback();
            activePlayer = null;
        }
        if (fadingOutPlayer != null) {
            fadingOutPlayer.stopPlayback();
            fadingOutPlayer = null;
        }
        LuminaBox.LOGGER.info("Music playback stopped.");
    }

    public synchronized void next() {
        if (currentPlaylist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % currentPlaylist.size();
        play(currentPlaylist.get(currentTrackIndex));
    }

    public synchronized void nextAuto() {
        if (currentPlaylist.isEmpty()) return;
        if (playbackMode == PlaybackMode.SHUFFLE && currentPlaylist.size() > 1) {
            int nextIdx;
            do {
                nextIdx = random.nextInt(currentPlaylist.size());
            } while (nextIdx == currentTrackIndex);
            currentTrackIndex = nextIdx;
        } else {
            currentTrackIndex = (currentTrackIndex + 1) % currentPlaylist.size();
        }
        play(currentPlaylist.get(currentTrackIndex));
    }

    public synchronized void previous() {
        if (currentPlaylist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        play(currentPlaylist.get(currentTrackIndex));
    }

    public synchronized void setVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (activePlayer != null) {
            activePlayer.setVolume(masterVolume);
        }
        com.luminabox.config.ModConfig.getInstance().setDefaultVolume(masterVolume);
    }

    public void seekToProgress(double ratio) {
        if (activePlayer != null && currentTrack != null) {
            double duration = getDuration();
            if (duration > 0) {
                activePlayer.seekTo(ratio * duration);
            }
        }
    }

    public float getVolume() {
        return masterVolume;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public PlaybackState getState() {
        return state;
    }

    public synchronized void onPlayerFinished(AudioStreamPlayer player) {
        if (player == activePlayer) {
            LuminaBox.LOGGER.info("Track finished. Failed: {}", player.hasFailed());
            activePlayer = null;
            if (player.hasFailed()) {
                stop();
                this.lastStatus = "fail";
            } else {
                if (!currentPlaylist.isEmpty()) {
                    if (playbackMode == PlaybackMode.LOOP_ONE) {
                        play(currentPlaylist.get(currentTrackIndex));
                    } else if (playbackMode == PlaybackMode.SEQUENTIAL) {
                        if (currentTrackIndex < currentPlaylist.size() - 1) {
                            nextAuto();
                        } else {
                            state = PlaybackState.STOPPED;
                            currentTrack = null;
                        }
                    } else {
                        nextAuto();
                    }
                } else {
                    state = PlaybackState.STOPPED;
                    currentTrack = null;
                }
            }
        } else if (player == fadingOutPlayer) {
            fadingOutPlayer = null;
        }
    }

    private void fadeIn(AudioStreamPlayer player) {
        new Thread(() -> {
            try {
                float vol = 0.0f;
                while (vol < masterVolume && player == activePlayer && state == PlaybackState.PLAYING) {
                    vol = Math.min(masterVolume, vol + 0.05f);
                    player.setVolume(vol);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "LuminaBox-FadeIn").start();
    }



    private void writeIntLE(java.io.OutputStream out, int val) throws java.io.IOException {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }

    private void writeShortLE(java.io.OutputStream out, short val) throws java.io.IOException {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }

    public static void showToast(String message) {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            java.lang.reflect.Method currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");
            Object activityThread = currentActivityThreadMethod.invoke(null);
            java.lang.reflect.Method getApplicationMethod = activityThreadClass.getMethod("getApplication");
            Object context = getApplicationMethod.invoke(activityThread);

            Class<?> toastClass = Class.forName("android.widget.Toast");
            java.lang.reflect.Method makeTextMethod = toastClass.getMethod("makeText", Class.forName("android.content.Context"), CharSequence.class, int.class);
            Object toast = makeTextMethod.invoke(null, context, message, 1); // 1 = Toast.LENGTH_LONG
            java.lang.reflect.Method showMethod = toastClass.getMethod("show");
            showMethod.invoke(toast);
        } catch (Exception e) {
            System.out.println("[Android Toast Fallback] " + message);
        }
    }

    // --- RPC / Internal Player Control Methods ---
    // Controls LuminaBox's own player only. External system media keys are
    // intentionally not used to comply with CurseForge's security policy.
    public void rpcPlayPause() {
        if (state == PlaybackState.PLAYING) {
            pause();
        } else if (state == PlaybackState.PAUSED) {
            resume();
        }
    }

    public void rpcNext() {
        next();
    }

    public void rpcPrevious() {
        previous();
    }

    public String getRpcCurrentlyPlaying() {
        return currentTrack != null ? currentTrack.getTitle() : "";
    }

    // --- Server Upload Methods ---
    public void uploadToServer(MusicTrack track) {
        File file = new File(getLocalMusicFolder(), track.getSourcePathOrUrl());
        if (!file.exists() || !file.isFile()) {
            showToast("文件不存在，无法上传: " + track.getTitle());
            return;
        }

        new Thread(() -> {
            try {
                byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
                int chunkSize = 30000; // 30KB per chunk to stay well under Minecraft's packet limit
                int totalChunks = (int) Math.ceil((double) fileData.length / chunkSize);

                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int length = Math.min(chunkSize, fileData.length - start);
                    byte[] chunk = new byte[length];
                    System.arraycopy(fileData, start, chunk, 0, length);

                    FileUploadPayload payload = new FileUploadPayload(file.getName(), i, totalChunks, chunk);
                    NetworkHandler.getInstance().sendToServer(payload);
                    
                    // Small delay to prevent flooding the server
                    Thread.sleep(50);
                }
                showToast("上传完成: " + track.getTitle());
            } catch (Exception e) {
                LuminaBox.LOGGER.error("Failed to upload file to server", e);
                showToast("上传失败: " + track.getTitle());
            }
        }).start();
    }
}
