import re

file_path = 'common/src/main/java/com/dynamicbgm/audio/CustomMusicManager.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add PlaybackMode enum and state
mode_enum = '''
    public enum PlaybackMode {
        SEQUENTIAL,
        LOOP_ALL,
        LOOP_ONE,
        SHUFFLE
    }

    private PlaybackMode playbackMode = PlaybackMode.SEQUENTIAL;
    private final java.util.Random random = new java.util.Random();

    public PlaybackMode getPlaybackMode() { return playbackMode; }
    public void setPlaybackMode(PlaybackMode mode) { this.playbackMode = mode; }
    public void cyclePlaybackMode() {
        PlaybackMode[] modes = PlaybackMode.values();
        this.playbackMode = modes[(this.playbackMode.ordinal() + 1) % modes.length];
    }
'''
content = content.replace('    public enum PlaybackState {', mode_enum + '\n    public enum PlaybackState {')

# 2. Modify next()
next_method_old = '''    public synchronized void next() {
        if (currentPlaylist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % currentPlaylist.size();
        play(currentPlaylist.get(currentTrackIndex));
    }'''

next_method_new = '''    public synchronized void next() {
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
    }'''
content = content.replace(next_method_old, next_method_new)

# 3. Modify onPlayerFinished()
on_finished_old = '''                if (manualPlaybackActive && !currentPlaylist.isEmpty()) {
                    next();
                } else {
                    state = PlaybackState.STOPPED;
                    currentTrack = null;
                }'''

on_finished_new = '''                if (manualPlaybackActive && !currentPlaylist.isEmpty()) {
                    if (playbackMode == PlaybackMode.LOOP_ONE) {
                        play(currentPlaylist.get(currentTrackIndex));
                    } else if (playbackMode == PlaybackMode.SEQUENTIAL) {
                        if (currentTrackIndex < currentPlaylist.size() - 1) {
                            next();
                        } else {
                            state = PlaybackState.STOPPED;
                            currentTrack = null;
                        }
                    } else {
                        next();
                    }
                } else {
                    state = PlaybackState.STOPPED;
                    currentTrack = null;
                }'''
content = content.replace(on_finished_old, on_finished_new)

# 4. Add WatchService 
watch_code = '''
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
                        DynamicBgm.LOGGER.info("Hot reload triggered for local music directory.");
                        scanLocalMusic();
                        if (!valid) break;
                    } catch (InterruptedException x) {
                        return;
                    } catch (Exception e) {
                        DynamicBgm.LOGGER.error("Error in WatchService", e);
                    }
                }
            }, "DynamicBGM-WatchService");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (Exception e) {
            DynamicBgm.LOGGER.error("Failed to start watch service", e);
        }
    }
'''
content = content.replace('    private CustomMusicManager() {', watch_code + '\n    private CustomMusicManager() {')
content = content.replace('    private CustomMusicManager() {\n        // Private constructor for singleton\n    }', '    private CustomMusicManager() {\n        // Private constructor for singleton\n        startWatcher();\n    }')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("CustomMusicManager modified.")
