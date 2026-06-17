package com.luminabox.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luminabox.LuminaBox;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static ModConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    private boolean replaceVanillaMusic = true;
    private float defaultVolume = 0.5f;
    private boolean enableDebugLog = false;
    private boolean useRpcControl = false;
    private List<String> localDirectories = new ArrayList<>();
    private List<String> serverSourceUrls = new ArrayList<>();
    private String platformCookie = "";
    private String proxyHost = "";
    private int proxyPort = 0;
    private int activeTab = 0;
    private String searchPrefix = "ytmsearch:";
    private String playbackMode = "SEQUENTIAL";
    private List<com.luminabox.audio.MusicTrack> onlinePlaylist = new ArrayList<>();

    public static void init(File configDir) {
        configFile = new File(configDir, "luminabox.json");
        if (configFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                instance = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                LuminaBox.LOGGER.error("Failed to load LuminaBox config", e);
            }
        }
        if (instance == null) {
            instance = new ModConfig();
        }
        instance.save();
    }

    public void save() {
        if (configFile == null) return;
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            LuminaBox.LOGGER.error("Failed to save LuminaBox config", e);
        }
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }

    public boolean isReplaceVanillaMusic() { return replaceVanillaMusic; }
    public void setReplaceVanillaMusic(boolean replaceVanillaMusic) { this.replaceVanillaMusic = replaceVanillaMusic; save(); }

    public float getDefaultVolume() { return defaultVolume; }
    public void setDefaultVolume(float defaultVolume) { this.defaultVolume = defaultVolume; save(); }

    public boolean isEnableDebugLog() { return enableDebugLog; }
    public void setEnableDebugLog(boolean enableDebugLog) { this.enableDebugLog = enableDebugLog; save(); }

    public boolean isUseRpcControl() { return useRpcControl; }
    public void setUseRpcControl(boolean useRpcControl) { this.useRpcControl = useRpcControl; save(); }

    public List<String> getLocalDirectories() { return localDirectories; }
    public List<String> getServerSourceUrls() { return serverSourceUrls; }

    public String getPlatformCookie() { return platformCookie; }
    public void setPlatformCookie(String platformCookie) { this.platformCookie = platformCookie; save(); }

    public String getProxyHost() { return proxyHost; }
    public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; save(); }

    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; save(); }

    public int getActiveTab() { return activeTab; }
    public void setActiveTab(int activeTab) { this.activeTab = activeTab; save(); }

    public String getSearchPrefix() { return searchPrefix; }
    public void setSearchPrefix(String searchPrefix) { this.searchPrefix = searchPrefix; save(); }

    public String getPlaybackMode() { return playbackMode; }
    public void setPlaybackMode(String playbackMode) { this.playbackMode = playbackMode; save(); }

    public List<com.luminabox.audio.MusicTrack> getOnlinePlaylist() { return onlinePlaylist; }
    public void setOnlinePlaylist(List<com.luminabox.audio.MusicTrack> onlinePlaylist) { 
        this.onlinePlaylist = onlinePlaylist; 
        save(); 
    }
}
