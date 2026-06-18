package com.luminabox.client.gui;

import com.luminabox.audio.CustomMusicManager;
import com.luminabox.audio.MusicTrack;
import com.luminabox.config.ModConfig;
import com.luminabox.config.MusicRuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import net.minecraft.client.gui.components.EditBox;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicPlayerScreen extends Screen {
    private int activeTab = com.luminabox.config.ModConfig.getInstance().getActiveTab(); // 0 = Local, 1 = Online, 2 = Server, 3 = Rules, 4 = Settings

    // Playlist Selection / Scroll state
    private boolean isSelectionMode = false;
    private final Set<Integer> selectedIndices = new HashSet<>();
    private int scrollIndex = 0;

    // Main Player Buttons
    private Button previousButton;
    private Button playPauseButton;
    private Button nextButton;
    private Button stopButton;
    private Button modeButton;
    private Button rpcToggleButton;
    private Button closeButton;

    // Tab 0: Local Tab Buttons
    private Button selectModeButton; // "选择" / "取消"
    private Button selectAllButton;   // "全选" (only visible in select mode)
    private Button pinButton;         // "置顶" (only visible in select mode)
    private Button deleteButton;      // "删除" (only visible in select mode)
    private Button scrollUpButton;
    private Button scrollDownButton;
    private Button refreshButton;
    private Button importButton;
    private Button uploadButton;

    // Tab 1: Online Search Tab
    private EditBox searchBox;
    private Button searchButton;
    private Button clearSearchButton; // New button to clear search and show playlist
    private List<MusicTrack> searchResults = new ArrayList<>();
    private int searchScrollIndex = 0;
    private Button searchScrollUpButton;
    private Button searchScrollDownButton;
    private Button searchSourceButton;
    private boolean showingSearchResults = false;

    // Tab 2: Server Tab Buttons
    private Button syncServerButton;
    private Button downloadAllServerButton;
    private int serverScrollIndex = 0;
    private Button serverScrollUpButton;
    private Button serverScrollDownButton;

    // Tab 3: Settings Tab Buttons
    private Button replaceVanillaButton;
    private Button debugLogButton;

    // Proxy Settings
    private EditBox proxyHostBox;
    private EditBox proxyPortBox;
    private Button saveProxyButton;

    // Tab navigation buttons
    // Removed because we use custom drawn sidebar elements instead


    private float getScale() {
        if (this.width < 480 || this.height < 300) {
            return Math.min((float)this.width / 480f, (float)this.height / 300f);
        }
        return 1.0f;
    }

    public MusicPlayerScreen() {
        super(Component.translatable("gui.luminabox.title"));
    }

    @Override
    protected void init() {
        super.init();

        // Scan local files when opening menu
        CustomMusicManager.getInstance().scanLocalMusic();

        float scale = getScale();
        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;

        // --- TAB SELECTION BUTTONS ---
        // Removed standard buttons; click logic is handled manually in mouseClicked

        // --- PLAYER CONTROLS (BOTTOM BAR) ---
        previousButton = this.addRenderableWidget(Button.builder(
            Component.literal("⏮"),
            button -> {
                if (ModConfig.getInstance().isUseRpcControl()) {
                    CustomMusicManager.getInstance().rpcPrevious();
                } else {
                    CustomMusicManager.getInstance().previous();
                }
            }
        ).bounds(cardX + 265, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("上一首 (Previous)"))).build());

        playPauseButton = this.addRenderableWidget(Button.builder(
            Component.literal("▶"),
            button -> {
                if (ModConfig.getInstance().isUseRpcControl()) {
                    CustomMusicManager.getInstance().rpcPlayPause();
                } else {
                    CustomMusicManager manager = CustomMusicManager.getInstance();
                    if (manager.getState() == CustomMusicManager.PlaybackState.PLAYING) {
                        manager.pause();
                    } else if (manager.getState() == CustomMusicManager.PlaybackState.PAUSED) {
                        manager.resume();
                    } else if (manager.getState() == CustomMusicManager.PlaybackState.STOPPED && manager.getPlaylist() != null && !manager.getPlaylist().isEmpty()) {
                        manager.play(manager.getPlaylist(), manager.getCurrentTrackIndex());
                    }
                }
            }
        ).bounds(cardX + 290, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("播放/暂停 (Play/Pause)"))).build());

        nextButton = this.addRenderableWidget(Button.builder(
            Component.literal("⏭"),
            button -> {
                if (ModConfig.getInstance().isUseRpcControl()) {
                    CustomMusicManager.getInstance().rpcNext();
                } else {
                    CustomMusicManager.getInstance().next();
                }
            }
        ).bounds(cardX + 315, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("下一首 (Next)"))).build());

        stopButton = this.addRenderableWidget(Button.builder(
            Component.literal("⏹"),
            button -> CustomMusicManager.getInstance().stop()
        ).bounds(cardX + 340, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("停止 (Stop)"))).build());

        modeButton = this.addRenderableWidget(Button.builder(
            Component.literal(getModeIcon(CustomMusicManager.getInstance().getPlaybackMode())),
            button -> {
                CustomMusicManager.getInstance().cyclePlaybackMode();
                button.setMessage(Component.literal(getModeIcon(CustomMusicManager.getInstance().getPlaybackMode())));
            }
        ).bounds(cardX + 365, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("切换播放模式 (Toggle Mode)"))).build());

        rpcToggleButton = this.addRenderableWidget(Button.builder(
            Component.literal(ModConfig.getInstance().isUseRpcControl() ? "🖥" : "🎮"),
            button -> {
                ModConfig config = ModConfig.getInstance();
                config.setUseRpcControl(!config.isUseRpcControl());
                button.setMessage(Component.literal(config.isUseRpcControl() ? "🖥" : "🎮"));
                if (config.isUseRpcControl()) {
                    CustomMusicManager.getInstance().stop(); // Stop internal when switching to RPC
                }
            }
        ).bounds(cardX + 390, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("模式: 内部播放(🎮) / 外部遥控(🖥)"))).build());

        // rpcToggleButton and modeButton are now visible across tabs by default

        // --- TAB 0: LOCAL TAB WIDGETS ---
        selectModeButton = this.addRenderableWidget(Button.builder(
            Component.literal("☑"),
            button -> toggleSelectMode()
        ).bounds(cardX + 145, cardY + 270, 20, 20).build());

        selectAllButton = this.addRenderableWidget(Button.builder(
            Component.literal("📝"),
            button -> toggleSelectAll()
        ).bounds(cardX + 170, cardY + 270, 20, 20).build());

        pinButton = this.addRenderableWidget(Button.builder(
            Component.literal("📌"),
            button -> pinSelected()
        ).bounds(cardX + 195, cardY + 270, 20, 20).build());

        deleteButton = this.addRenderableWidget(Button.builder(
            Component.literal("🗑"),
            button -> deleteSelected()
        ).bounds(cardX + 220, cardY + 270, 20, 20).build());

        scrollUpButton = this.addRenderableWidget(Button.builder(
            Component.literal("▲"),
            button -> scrollUp()
        ).bounds(cardX + 450, cardY + 40, 15, 15).build());

        scrollDownButton = this.addRenderableWidget(Button.builder(
            Component.literal("▼"),
            button -> scrollDown()
        ).bounds(cardX + 450, cardY + 205, 15, 15).build());

        refreshButton = this.addRenderableWidget(Button.builder(
            Component.literal("⟳"),
            button -> {
                CustomMusicManager.getInstance().scanLocalMusic();
                selectedIndices.clear();
                scrollIndex = 0;
            }
        ).bounds(cardX + 95, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("刷新曲库 (Refresh)"))).build());

        importButton = this.addRenderableWidget(Button.builder(
            Component.literal("📥"),
            button -> {
                boolean isAndroid = System.getProperty("os.name").toLowerCase().contains("android") ||
                                    System.getProperty("java.vm.name").toLowerCase().contains("dalvik") ||
                                    System.getProperty("java.vm.name").toLowerCase().contains("art");

                if (isAndroid) {
                    String path = CustomMusicManager.getInstance().getLocalMusicFolder().getAbsolutePath();
                    String msg = "检测到安卓系统, 请将音乐文件放入: " + path;
                    CustomMusicManager.showToast(msg);
                } else {
                    org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush();
                    try {
                        org.lwjgl.PointerBuffer filters = stack.mallocPointer(4);
                        filters.put(stack.UTF8("*.mp3"));
                        filters.put(stack.UTF8("*.wav"));
                        filters.put(stack.UTF8("*.ogg"));
                        filters.put(stack.UTF8("*.flac"));
                        filters.flip();

                        String selectedFiles = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                            "Import Music Files",
                            System.getProperty("user.home") + java.io.File.separator,
                            filters,
                            "Audio Files",
                            true
                        );

                        if (selectedFiles != null && !selectedFiles.isEmpty()) {
                            String[] files = selectedFiles.split("\\|");
                            File destDir = CustomMusicManager.getInstance().getLocalMusicFolder();
                            for (String fPath : files) {
                                File src = new File(fPath);
                                if (src.exists() && src.isFile()) {
                                    File dest = new File(destDir, src.getName());
                                    java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    } catch (Exception e) {
                        com.luminabox.LuminaBox.LOGGER.error("Failed to import files", e);
                    } finally {
                        stack.close();
                    }
                }
            }
        ).bounds(cardX + 120, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("导入本地音乐 (Import Music)"))).build());

        uploadButton = this.addRenderableWidget(Button.builder(
            Component.literal("⬆"),
            button -> {
                for (int index : selectedIndices) {
                    if (index >= 0 && index < CustomMusicManager.getInstance().getPlaylist().size()) {
                        CustomMusicManager.getInstance().uploadToServer(CustomMusicManager.getInstance().getPlaylist().get(index));
                    }
                }
            }
        ).bounds(cardX + 145, cardY + 270, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("上传至服务器 (Upload)"))).build());

        searchBox = new EditBox(this.font, cardX + 90, cardY + 15, 215, 20, Component.translatable("gui.luminabox.search_placeholder"));
        this.addRenderableWidget(searchBox);

        searchSourceButton = this.addRenderableWidget(Button.builder(
            Component.literal("YTM"),
            button -> {
                String current = CustomMusicManager.getInstance().getSearchPrefix();
                if (current.equals("ytmsearch:")) {
                    CustomMusicManager.getInstance().setSearchPrefix("ytsearch:");
                    button.setMessage(Component.literal("YT"));
                } else if (current.equals("ytsearch:")) {
                    CustomMusicManager.getInstance().setSearchPrefix("scsearch:");
                    button.setMessage(Component.literal("SC"));
                } else if (current.equals("scsearch:")) {
                    CustomMusicManager.getInstance().setSearchPrefix("https://www.bilibili.com/video/");
                    button.setMessage(Component.literal("Bili"));
                } else {
                    CustomMusicManager.getInstance().setSearchPrefix("ytmsearch:");
                    button.setMessage(Component.literal("YTM"));
                }
            }
        ).bounds(cardX + 310, cardY + 15, 30, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("切换搜索源(YTM/YT/SC)"))).build());

        searchButton = this.addRenderableWidget(Button.builder(
            Component.literal("🔍"),
            button -> {
                String query = searchBox.getValue();
                if (!query.trim().isEmpty()) {
                    CustomMusicManager.getInstance().searchPlatform(query, results -> {
                        this.searchResults = results;
                        this.searchScrollIndex = 0;
                        this.showingSearchResults = true;
                        updateButtonVisibility();
                    });
                }
            }
        ).bounds(cardX + 345, cardY + 15, 20, 20).build());

        clearSearchButton = this.addRenderableWidget(Button.builder(
            Component.literal("❌"),
            button -> {
                this.showingSearchResults = false;
                this.searchBox.setValue("");
                this.searchScrollIndex = 0;
                updateButtonVisibility();
            }
        ).bounds(cardX + 370, cardY + 15, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("返回网络播放列表"))).build());

        searchScrollUpButton = this.addRenderableWidget(Button.builder(
            Component.literal("▲"),
            button -> { if (searchScrollIndex > 0) searchScrollIndex--; }
        ).bounds(cardX + 450, cardY + 40, 15, 15).build());

        searchScrollDownButton = this.addRenderableWidget(Button.builder(
            Component.literal("▼"),
            button -> {
                int size = showingSearchResults ? searchResults.size() : CustomMusicManager.getInstance().getOnlinePlaylist().size();
                if (searchScrollIndex < size - 9) searchScrollIndex++;
            }
        ).bounds(cardX + 450, cardY + 205, 15, 15).build());

        // --- TAB 2: SERVER TAB WIDGETS ---
        syncServerButton = this.addRenderableWidget(Button.builder(
            Component.literal("⟳"),
            button -> CustomMusicManager.getInstance().scanLocalMusic()
        ).bounds(cardX + 400, cardY + 15, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.luminabox.server.sync"))).build());

        downloadAllServerButton = this.addRenderableWidget(Button.builder(
            Component.literal("📥"),
            button -> {
                for (MusicTrack t : CustomMusicManager.getInstance().getServerPlaylist()) {
                    CustomMusicManager.getInstance().downloadServerTrack(t);
                }
            }
        ).bounds(cardX + 425, cardY + 15, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.luminabox.server.download_all"))).build());

        serverScrollUpButton = this.addRenderableWidget(Button.builder(
            Component.literal("▲"),
            button -> { if (serverScrollIndex > 0) serverScrollIndex--; }
        ).bounds(cardX + 450, cardY + 40, 15, 15).build());

        serverScrollDownButton = this.addRenderableWidget(Button.builder(
            Component.literal("▼"),
            button -> {
                if (serverScrollIndex < CustomMusicManager.getInstance().getServerPlaylist().size() - 9) serverScrollIndex++;
            }
        ).bounds(cardX + 450, cardY + 205, 15, 15).build());

        // --- TAB 4: SETTINGS TAB WIDGETS ---
        replaceVanillaButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.luminabox.settings.replace_vanilla"),
            button -> {
                ModConfig config = ModConfig.getInstance();
                config.setReplaceVanillaMusic(!config.isReplaceVanillaMusic());
                updateButtonVisibility();
            }
        ).bounds(cardX + 90, cardY + 35, 180, 20).build());

        debugLogButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.luminabox.settings.debug_log"),
            button -> {
                boolean nextState = !com.luminabox.config.ModConfig.getInstance().isEnableDebugLog();
                com.luminabox.config.ModConfig.getInstance().setEnableDebugLog(nextState);
                button.setMessage(Component.translatable("gui.luminabox.settings.debug_log").append(": ").append(
                    nextState ? Component.translatable("gui.luminabox.generic.on") : Component.translatable("gui.luminabox.generic.off")
                ));
            }
        ).bounds(cardX + 90, cardY + 60, 200, 20).build());

        // Proxy UI
        proxyHostBox = new EditBox(this.font, cardX + 90, cardY + 115, 120, 20, Component.translatable("gui.luminabox.settings.proxy_host"));
        proxyHostBox.setValue(com.luminabox.config.ModConfig.getInstance().getProxyHost());
        this.addRenderableWidget(proxyHostBox);

        proxyPortBox = new EditBox(this.font, cardX + 220, cardY + 115, 50, 20, Component.translatable("gui.luminabox.settings.proxy_port"));
        int pPort = com.luminabox.config.ModConfig.getInstance().getProxyPort();
        proxyPortBox.setValue(pPort > 0 ? String.valueOf(pPort) : "");
        this.addRenderableWidget(proxyPortBox);

        saveProxyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.luminabox.settings.save_proxy"),
            button -> {
                com.luminabox.config.ModConfig.getInstance().setProxyHost(proxyHostBox.getValue().trim());
                try {
                    int port = Integer.parseInt(proxyPortBox.getValue().trim());
                    com.luminabox.config.ModConfig.getInstance().setProxyPort(port);
                } catch (NumberFormatException e) {
                    com.luminabox.config.ModConfig.getInstance().setProxyPort(0);
                }
                CustomMusicManager.showToast(Component.translatable("gui.luminabox.settings.proxy_saved").getString());
            }
        ).bounds(cardX + 280, cardY + 115, 80, 20).build());

        // Close (X) Button
        closeButton = this.addRenderableWidget(Button.builder(
            Component.literal("X"),
            button -> this.onClose()
        ).bounds(cardX + 450, cardY + 10, 20, 20).build());

        updateButtonVisibility();
    }

    private String getModeIcon(CustomMusicManager.PlaybackMode mode) {
        switch (mode) {
            case LOOP_ALL: return "🔁";
            case LOOP_ONE: return "🔂";
            case SHUFFLE: return "🔀";
            case SEQUENTIAL: default: return "⬇";
        }
    }

    private void toggleSelectMode() {
        isSelectionMode = !isSelectionMode;
        if (!isSelectionMode) {
            selectedIndices.clear();
        }
        if (selectModeButton != null) {
            selectModeButton.setMessage(Component.literal(
                isSelectionMode ? "☒" : "☑"
            ));
        }
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        // Tab 0 (Local)
        boolean isLocal = (activeTab == 0);
        if (refreshButton != null) refreshButton.visible = true;
        if (importButton != null) importButton.visible = true;
        if (uploadButton != null) uploadButton.visible = true;
        if (selectModeButton != null) selectModeButton.visible = isLocal;
        if (scrollUpButton != null) scrollUpButton.visible = isLocal;
        if (scrollDownButton != null) scrollDownButton.visible = isLocal;

        // Sub-options of "Select" button: only visible when in select mode
        boolean showSubOptions = isLocal && isSelectionMode;
        if (selectAllButton != null) selectAllButton.visible = showSubOptions;
        if (pinButton != null) pinButton.visible = showSubOptions;
        if (deleteButton != null) deleteButton.visible = showSubOptions;

        // Tab 1 (Online)
        boolean isOnline = (activeTab == 1);
        if (searchBox != null) {
            searchBox.visible = isOnline;
            searchBox.setFocused(isOnline);
        }
        if (searchButton != null) searchButton.visible = isOnline;
        if (clearSearchButton != null) clearSearchButton.visible = isOnline && showingSearchResults;
        if (searchScrollUpButton != null) searchScrollUpButton.visible = isOnline;
        if (searchScrollDownButton != null) searchScrollDownButton.visible = isOnline;
        if (searchSourceButton != null) searchSourceButton.visible = isOnline;

        // Player Controls (Global)
        if (modeButton != null) modeButton.visible = true;
        if (rpcToggleButton != null) rpcToggleButton.visible = true;

        // Tab 2 (Server)
        boolean isServer = (activeTab == 2);
        if (syncServerButton != null) syncServerButton.visible = isServer;
        if (downloadAllServerButton != null) downloadAllServerButton.visible = isServer;
        if (serverScrollUpButton != null) serverScrollUpButton.visible = isServer;
        if (serverScrollDownButton != null) serverScrollDownButton.visible = isServer;

        // Tab 4 (Settings)
        boolean isSettings = (activeTab == 4);
        replaceVanillaButton.visible = isSettings;
        debugLogButton.visible = isSettings;
        proxyHostBox.visible = isSettings;
        proxyPortBox.visible = isSettings;
        saveProxyButton.visible = isSettings;
        if (replaceVanillaButton != null) {
            replaceVanillaButton.setMessage(Component.translatable("gui.luminabox.settings.replace_vanilla")
                .append(ModConfig.getInstance().isReplaceVanillaMusic() ?
                    Component.translatable("gui.luminabox.generic.on") :
                    Component.translatable("gui.luminabox.generic.off")));
        }
        if (debugLogButton != null) {
            debugLogButton.visible = isSettings;
            debugLogButton.setMessage(Component.translatable("gui.luminabox.settings.debug_log")
                .append(ModConfig.getInstance().isEnableDebugLog() ? "ON" : "OFF"));
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float scale = getScale();
        context.pose().pushPose();
        context.pose().scale(scale, scale, 1.0f);

        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);

        super.renderBackground(context, scaledMouseX, scaledMouseY, delta);

        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;

        // Main glass container
        context.fillGradient(cardX, cardY, cardX + cardWidth, cardY + cardHeight, 0xDD2A2A2A, 0xDD131313);
        context.fill(cardX, cardY, cardX + cardWidth, cardY + 1, 0x44FFFFFF);
        context.fill(cardX, cardY, cardX + 1, cardY + cardHeight, 0x44FFFFFF);
        context.fill(cardX, cardY + cardHeight - 1, cardX + cardWidth, cardY + cardHeight, 0x44000000);
        context.fill(cardX + cardWidth - 1, cardY, cardX + cardWidth, cardY + cardHeight, 0x44000000);

        // Sidebar splitter
        context.fill(cardX, cardY, cardX + 80, cardY + cardHeight, 0x22000000);
        context.pose().popPose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float scale = getScale();
        context.pose().pushPose();
        context.pose().scale(scale, scale, 1.0f);

        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);

        super.render(context, scaledMouseX, scaledMouseY, delta);

        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;

        // 1. Sidebar Tabs
        for (int i = 0; i < 5; i++) {
            Component label = Component.empty();
            switch (i) {
                case 0: label = Component.translatable("gui.luminabox.tab.local"); break;
                case 1: label = Component.translatable("gui.luminabox.platform.online_mode"); break;
                case 2: label = Component.translatable("gui.luminabox.tab.server"); break;
                case 3: label = Component.translatable("gui.luminabox.tab.rules"); break;
                case 4: label = Component.translatable("gui.luminabox.tab.settings"); break;
            }
            int color = (activeTab == i) ? 0xFF00FFCC : 0xFFAAAAAA;
            context.drawCenteredString(this.font, label, cardX + 40, cardY + 20 + i * 35, color);
        }

        // 2. Main View Contents
        CustomMusicManager manager = CustomMusicManager.getInstance();

        if (activeTab == 0) {
            context.drawString(this.font, Component.translatable("gui.luminabox.local.title"), cardX + 90, cardY + 15, 0xFFFFFFFF, false);

            List<MusicTrack> playlist = manager.getPlaylist();

            // Limit scrollIndex
            if (scrollIndex < 0) scrollIndex = 0;
            if (scrollIndex > Math.max(0, playlist.size() - 9)) {
                scrollIndex = Math.max(0, playlist.size() - 9);
            }

            for (int i = 0; i < 9 && (scrollIndex + i) < playlist.size(); i++) {
                int actualIndex = scrollIndex + i;
                MusicTrack track = playlist.get(actualIndex);
                int color = (manager.getCurrentTrack() != null && manager.getCurrentTrack().getId().equals(track.getId()) && manager.getState() == CustomMusicManager.PlaybackState.PLAYING)
                    ? 0xFF00FFCC : 0xFFDDDDDD;

                int textX = cardX + 95;
                if (isSelectionMode) {
                    // Draw Checkbox in selection mode
                    int boxX = cardX + 95;
                    int boxY = cardY + 40 + i * 20;
                    context.fill(boxX, boxY, boxX + 10, boxY + 10, 0x44FFFFFF);
                    if (selectedIndices.contains(actualIndex)) {
                        context.fill(boxX + 2, boxY + 2, boxX + 8, boxY + 8, 0xFF00FFCC);
                    }
                    textX = cardX + 110;
                }

                String trackLine = (actualIndex + 1) + ". " + track.getTitle();
                if (trackLine.length() > (isSelectionMode ? 40 : 43)) {
                    trackLine = trackLine.substring(0, isSelectionMode ? 37 : 40) + "...";
                }
                context.drawString(this.font, Component.literal(trackLine), textX, cardY + 41 + i * 20, color, false);
            }

            if (playlist.isEmpty()) {
                context.drawString(this.font, Component.translatable("gui.luminabox.local.no_music"), cardX + 95, cardY + 45, 0xFF888888, false);
                context.drawString(this.font, Component.translatable("gui.luminabox.local.instructions"), cardX + 95, cardY + 65, 0xFF888888, false);
                context.drawString(this.font, Component.literal("luminabox/local/"), cardX + 95, cardY + 85, 0xFF00FFCC, false);
            }

            String status = manager.getLastStatus();
            String statusIcon = "✓"; // ready / success
            int statusColor = 0xFF55FF55;
            if (status.equals("download_failed")) {
                statusIcon = "❌";
                statusColor = 0xFFFF5555;
            } else if (status.equals("downloading")) {
                statusIcon = "⏳";
                statusColor = 0xFFFFFF55;
            } else if (status.equals("playing")) {
                statusIcon = "🔊";
            } else if (status.startsWith("Error")) {
                statusIcon = "⚠️ " + status;
                statusColor = 0xFFFF5555;
            }

            // Draw status icon at the bottom right
            context.drawString(this.font, Component.literal(statusIcon), cardX + 300, cardY + 276, statusColor, false);

        } else if (activeTab == 1) {
            // Online Search View / Online Playlist
            List<MusicTrack> displayList = showingSearchResults ? searchResults : manager.getOnlinePlaylist();

            if (showingSearchResults) {
                context.drawString(this.font, Component.translatable("gui.luminabox.platform.search_results"), cardX + 90, cardY + 38, 0xFF00FFCC, false);
            } else {
                context.drawString(this.font, Component.translatable("gui.luminabox.platform.online_playlist"), cardX + 90, cardY + 38, 0xFF00FFCC, false);
            }

            if (searchScrollIndex < 0) searchScrollIndex = 0;
            if (searchScrollIndex > Math.max(0, displayList.size() - 8)) {
                searchScrollIndex = Math.max(0, displayList.size() - 8);
            }

            for (int i = 0; i < 8 && (searchScrollIndex + i) < displayList.size(); i++) {
                int actualIndex = searchScrollIndex + i;
                MusicTrack track = displayList.get(actualIndex);
                int color = (manager.getCurrentTrack() != null && manager.getCurrentTrack().getId().equals(track.getId()) && manager.getState() == CustomMusicManager.PlaybackState.PLAYING)
                    ? 0xFF00FFCC : 0xFFDDDDDD;

                String trackLine = (actualIndex + 1) + ". " + track.getTitle() + " - " + track.getArtist();
                if (trackLine.length() > 38) {
                    trackLine = trackLine.substring(0, 35) + "...";
                }
                context.drawString(this.font, Component.literal(trackLine), cardX + 95, cardY + 52 + i * 20, color, false);

                if (showingSearchResults) {
                    int addBtnX = cardX + 420;
                    int addBtnY = cardY + 52 + i * 20;
                    context.fill(addBtnX - 2, addBtnY - 2, addBtnX + 15, addBtnY + 12, 0x5500FFCC);
                    context.drawString(this.font, Component.literal("+"), addBtnX + 4, addBtnY, 0xFFFFFFFF, false);
                }
            }
            if (displayList.isEmpty()) {
                if (showingSearchResults) {
                    context.drawString(this.font, Component.translatable("gui.luminabox.platform.no_results"), cardX + 95, cardY + 65, 0xFF888888, false);
                } else {
                    context.drawString(this.font, Component.translatable("gui.luminabox.platform.empty_list"), cardX + 95, cardY + 65, 0xFF888888, false);
                }
            }

        } else if (activeTab == 2) {
            context.drawString(this.font, Component.translatable("gui.luminabox.server.title"), cardX + 90, cardY + 18, 0xFFFFFFFF, false);

            List<MusicTrack> serverList = manager.getServerPlaylist();
            if (serverScrollIndex < 0) serverScrollIndex = 0;
            if (serverScrollIndex > Math.max(0, serverList.size() - 9)) {
                serverScrollIndex = Math.max(0, serverList.size() - 9);
            }

            for (int i = 0; i < 9 && (serverScrollIndex + i) < serverList.size(); i++) {
                int actualIndex = serverScrollIndex + i;
                MusicTrack track = serverList.get(actualIndex);
                File file = new File(manager.getLocalMusicFolder(), track.getSourcePathOrUrl());
                boolean downloaded = file.exists();

                int color = (manager.getCurrentTrack() != null && manager.getCurrentTrack().getId().equals(track.getId()) && manager.getState() == CustomMusicManager.PlaybackState.PLAYING)
                    ? 0xFF00FFCC : (downloaded ? 0xFFDDDDDD : 0xFF888888);

                String trackLine = (actualIndex + 1) + ". " + track.getTitle() + (downloaded ? " [本地]" : " [云端]");
                if (trackLine.length() > 40) {
                    trackLine = trackLine.substring(0, 37) + "...";
                }
                context.drawString(this.font, Component.literal(trackLine), cardX + 95, cardY + 40 + i * 20, color, false);
            }
            if (serverList.isEmpty()) {
                context.drawString(this.font, Component.translatable("gui.luminabox.server.empty_list"), cardX + 95, cardY + 65, 0xFF888888, false);
            }

        } else if (activeTab == 3) {
            context.drawString(this.font, Component.translatable("gui.luminabox.rules.title"), cardX + 90, cardY + 15, 0xFFFFFFFF, false);

            MusicRuleManager ruleMgr = MusicRuleManager.getInstance();
            context.drawString(this.font, Component.translatable("gui.luminabox.rules.overworld")
                .append(getRuleSongDisplayName(ruleMgr.getDimensions().get("minecraft:overworld"))), cardX + 95, cardY + 40, 0xFFDDDDDD, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.rules.nether")
                .append(getRuleSongDisplayName(ruleMgr.getDimensions().get("minecraft:the_nether"))), cardX + 95, cardY + 65, 0xFFDDDDDD, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.rules.combat")
                .append(getRuleSongDisplayName(ruleMgr.getCombatMusic())), cardX + 95, cardY + 90, 0xFFDDDDDD, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.rules.boss")
                .append(getRuleSongDisplayName(ruleMgr.getBossMusic())), cardX + 95, cardY + 115, 0xFFDDDDDD, false);

            context.drawString(this.font, Component.translatable("gui.luminabox.rules.hint"), cardX + 95, cardY + 145, 0xFF888888, false);

        } else if (activeTab == 4) {
            context.drawString(this.font, Component.translatable("gui.luminabox.settings.title"), cardX + 90, cardY + 15, 0xFFFFFFFF, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.settings.proxy_title"), cardX + 90, cardY + 100, 0xFFFFFFFF, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.settings.proxy_host"), cardX + 90, cardY + 140, 0xFF888888, false);
            context.drawString(this.font, Component.translatable("gui.luminabox.settings.proxy_port"), cardX + 220, cardY + 140, 0xFF888888, false);
        }

        // 3. Bottom Player Bar Rendering
        String trackTitle;
        if (ModConfig.getInstance().isUseRpcControl()) {
            trackTitle = "🖥 " + CustomMusicManager.getInstance().getRpcCurrentlyPlaying();
        } else {
            MusicTrack current = manager.getCurrentTrack();
            trackTitle = current != null ? current.getTitle() : Component.translatable("gui.luminabox.player.no_track").getString();
        }

        if (trackTitle.length() > 30) {
            trackTitle = trackTitle.substring(0, 27) + "...";
        }
        context.drawString(this.font, Component.literal(trackTitle), cardX + 95, cardY + 235, ModConfig.getInstance().isUseRpcControl() ? 0xFFFF00FF : 0xFFFFFFFF, false);

        // Render playback position and duration
        String timeStr = "00:00 / 00:00";
        double progressRatio = 0.0;

        if (!ModConfig.getInstance().isUseRpcControl()) {
            double currentSecs = manager.getPlaybackPosition();
            double durationSecs = manager.getDuration();
            timeStr = formatTime(currentSecs) + " / " + formatTime(durationSecs);
            progressRatio = durationSecs > 0 ? (currentSecs / durationSecs) : 0.0;
            progressRatio = Math.max(0.0, Math.min(1.0, progressRatio));
        }

        context.drawString(this.font, Component.literal(timeStr), cardX + 375, cardY + 235, 0xFFCCCCCC, false);

        // Progress Bar
        int progX = cardX + 95;
        int progY = cardY + 247;
        int progWidth = 360;
        int progHeight = 3;

        context.fill(progX, progY, progX + progWidth, progY + progHeight, 0x55555555);
        int progFillWidth = (int) (progWidth * progressRatio);
        context.fill(progX, progY, progX + progFillWidth, progY + progHeight, 0xFF00FFCC);
        if (progFillWidth > 0) {
            context.fill(progX + progFillWidth - 1, progY - 1, progX + progFillWidth + 1, progY + progHeight + 1, 0xFFFFFFFF);
        }

        // Volume slider
        context.drawString(this.font, Component.translatable("gui.luminabox.player.volume"), cardX + 95, cardY + 256, 0xFF888888, false);

        int sliderX = cardX + 130;
        int sliderY = cardY + 259;
        int sliderWidth = 80;
        int sliderHeight = 4;

        context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, 0x55555555);
        int fillWidth = (int) (sliderWidth * manager.getVolume());
        context.fill(sliderX, sliderY, sliderX + fillWidth, sliderY + sliderHeight, 0xFF00FFCC);
        context.fill(sliderX + fillWidth - 2, sliderY - 2, sliderX + fillWidth + 2, sliderY + sliderHeight + 2, 0xFFFFFFFF);

        String percentageStr = (int) (manager.getVolume() * 100) + "%";
        context.drawString(this.font, Component.literal(percentageStr), cardX + 220, cardY + 256, 0xFFCCCCCC, false);

        if (playPauseButton != null) {
            playPauseButton.setMessage(Component.literal(
                manager.getState() == CustomMusicManager.PlaybackState.PLAYING ? "⏸" : "▶"
            ));
        }

        context.pose().popPose();
    }

    private String formatTime(double seconds) {
        int totalSecs = (int) seconds;
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private String getRuleSongDisplayName(String val) {
        if (val == null || val.isEmpty()) return "None";
        return val;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = getScale();
        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;

        // 1. Sidebar tab switching click area
        if (mouseX >= cardX && mouseX <= cardX + 80) {
            if (mouseY >= cardY + 15 && mouseY <= cardY + 190) {
                int clickedTab = (int) ((mouseY - (cardY + 15)) / 35);
                if (clickedTab >= 0 && clickedTab <= 4) {
                    activeTab = clickedTab;
                    com.luminabox.config.ModConfig.getInstance().setActiveTab(activeTab);
                    updateButtonVisibility();
                    return true;
                }
            }
        }

        // 2. Local playlist item click
        if (activeTab == 0 && mouseX >= cardX + 90 && mouseX <= cardX + 450) {
            if (mouseY >= cardY + 40 && mouseY <= cardY + 220) {
                int clickedSong = (int) ((mouseY - (cardY + 40)) / 20);
                List<MusicTrack> playlist = CustomMusicManager.getInstance().getPlaylist();
                int actualIndex = scrollIndex + clickedSong;
                if (actualIndex >= 0 && actualIndex < playlist.size()) {
                    if (isSelectionMode) {
                        // In selection mode: clicking anywhere on the item toggles selection
                        if (selectedIndices.contains(actualIndex)) {
                            selectedIndices.remove(actualIndex);
                        } else {
                            selectedIndices.add(actualIndex);
                        }
                    } else {
                        // In normal mode: clicking plays the song
                        CustomMusicManager.getInstance().play(playlist, actualIndex);
                    }
                    return true;
                }
            }
        }

        // 3. Online Search playlist item click
        if (activeTab == 1 && mouseX >= cardX + 90 && mouseX <= cardX + 450) {
            if (mouseY >= cardY + 50 && mouseY <= cardY + 220) {
                int clickedSong = (int) ((mouseY - (cardY + 50)) / 20);
                int actualIndex = searchScrollIndex + clickedSong;

                if (showingSearchResults) {
                    if (actualIndex >= 0 && actualIndex < searchResults.size()) {
                        int addBtnX = cardX + 420;
                        if (mouseX >= addBtnX - 5 && mouseX <= addBtnX + 20) {
                            // Clicked the ADD button
                            MusicTrack track = searchResults.get(actualIndex);
                            CustomMusicManager.getInstance().addToOnlinePlaylist(track);
                            CustomMusicManager.showToast("已添加: " + track.getTitle());
                        } else {
                            // Clicked the song to play
                            CustomMusicManager.getInstance().play(searchResults, actualIndex);
                        }
                        return true;
                    }
                } else {
                    List<MusicTrack> onlinePlaylist = CustomMusicManager.getInstance().getOnlinePlaylist();
                    if (actualIndex >= 0 && actualIndex < onlinePlaylist.size()) {
                        CustomMusicManager.getInstance().play(onlinePlaylist, actualIndex);
                        return true;
                    }
                }
            }
        }

        // 4. Server playlist item click
        if (activeTab == 2 && mouseX >= cardX + 90 && mouseX <= cardX + 450) {
            if (mouseY >= cardY + 40 && mouseY <= cardY + 220) {
                int clickedSong = (int) ((mouseY - (cardY + 40)) / 20);
                List<MusicTrack> serverPlaylist = CustomMusicManager.getInstance().getServerPlaylist();
                int actualIndex = serverScrollIndex + clickedSong;
                if (actualIndex >= 0 && actualIndex < serverPlaylist.size()) {
                    CustomMusicManager.getInstance().play(serverPlaylist, actualIndex);
                    return true;
                }
            }
        }

        // 5. Ambient rules click area (cycles tracks)
        if (activeTab == 3 && mouseX >= cardX + 90 && mouseX <= cardX + 450) {
            if (mouseY >= cardY + 40 && mouseY <= cardY + 130) {
                if (mouseY >= cardY + 40 && mouseY < cardY + 60) {
                    cycleRule("dimension", "minecraft:overworld");
                    return true;
                } else if (mouseY >= cardY + 65 && mouseY < cardY + 85) {
                    cycleRule("dimension", "minecraft:the_nether");
                    return true;
                } else if (mouseY >= cardY + 90 && mouseY < cardY + 110) {
                    cycleRule("combat", "default");
                    return true;
                } else if (mouseY >= cardY + 115 && mouseY < cardY + 135) {
                    cycleRule("boss", "default");
                    return true;
                }
            }
        }

        // 5. Volume slider click
        if (handleVolumeAction(mouseX, mouseY)) {
            return true;
        }

        if (handleProgressAction(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void scrollUp() {
        if (scrollIndex > 0) {
            scrollIndex--;
        }
    }

    private void scrollDown() {
        List<MusicTrack> playlist = CustomMusicManager.getInstance().getPlaylist();
        if (scrollIndex < playlist.size() - 9) {
            scrollIndex++;
        }
    }

    private void toggleSelectAll() {
        List<MusicTrack> playlist = CustomMusicManager.getInstance().getPlaylist();
        if (selectedIndices.size() == playlist.size()) {
            selectedIndices.clear();
        } else {
            selectedIndices.clear();
            for (int i = 0; i < playlist.size(); i++) {
                selectedIndices.add(i);
            }
        }
    }

    private void pinSelected() {
        List<MusicTrack> localPlaylist = CustomMusicManager.getInstance().getPlaylist();
        if (localPlaylist.isEmpty() || selectedIndices.isEmpty()) return;

        List<MusicTrack> selectedItems = new ArrayList<>();
        List<MusicTrack> remainingItems = new ArrayList<>();
        for (int i = 0; i < localPlaylist.size(); i++) {
            if (selectedIndices.contains(i)) {
                selectedItems.add(localPlaylist.get(i));
            } else {
                remainingItems.add(localPlaylist.get(i));
            }
        }
        localPlaylist.clear();
        localPlaylist.addAll(selectedItems);
        localPlaylist.addAll(remainingItems);
        selectedIndices.clear();
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedIndices.add(i);
        }
        scrollIndex = 0;
    }

    private void deleteSelected() {
        List<MusicTrack> localPlaylist = CustomMusicManager.getInstance().getPlaylist();
        if (localPlaylist.isEmpty() || selectedIndices.isEmpty()) return;

        List<Integer> sortedIndices = new ArrayList<>(selectedIndices);
        sortedIndices.sort((a, b) -> b - a); // Descending order to avoid index shifts

        for (int idx : sortedIndices) {
            if (idx >= 0 && idx < localPlaylist.size()) {
                MusicTrack track = localPlaylist.get(idx);
                // Physically delete from disk to prevent it from reappearing on scanning/importing
                File file = new File(CustomMusicManager.getInstance().getLocalMusicFolder(), track.getSourcePathOrUrl());
                if (file.exists() && file.isFile()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                localPlaylist.remove(idx);
            }
        }
        selectedIndices.clear();
        CustomMusicManager.getInstance().setCurrentTrackIndex(-1);
        scrollIndex = 0;
    }

    private void cycleRule(String type, String id) {
        MusicRuleManager ruleMgr = MusicRuleManager.getInstance();
        List<MusicTrack> localTracks = CustomMusicManager.getInstance().getPlaylist();

        String currentVal = "";
        if ("dimension".equals(type)) {
            currentVal = ruleMgr.getDimensions().get(id);
        } else if ("combat".equals(type)) {
            currentVal = ruleMgr.getCombatMusic();
        } else if ("boss".equals(type)) {
            currentVal = ruleMgr.getBossMusic();
        }

        if (currentVal == null) currentVal = "";

        String nextVal = "";
        if (currentVal.isEmpty()) {
            if (!localTracks.isEmpty()) {
                nextVal = localTracks.get(0).getSourcePathOrUrl();
            }
        } else {
            int currentIdx = -1;
            for (int i = 0; i < localTracks.size(); i++) {
                if (localTracks.get(i).getSourcePathOrUrl().equals(currentVal)) {
                    currentIdx = i;
                    break;
                }
            }
            if (currentIdx != -1 && currentIdx < localTracks.size() - 1) {
                nextVal = localTracks.get(currentIdx + 1).getSourcePathOrUrl();
            } else {
                nextVal = ""; // Cycle back to None
            }
        }

        if ("dimension".equals(type)) {
            if (nextVal.isEmpty()) {
                ruleMgr.getDimensions().remove(id);
            } else {
                ruleMgr.getDimensions().put(id, nextVal);
            }
        } else if ("combat".equals(type)) {
            ruleMgr.setCombatMusic(nextVal);
        } else if ("boss".equals(type)) {
            ruleMgr.setBossMusic(nextVal);
        }

        ruleMgr.save();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (handleVolumeAction(mouseX, mouseY)) {
            return true;
        }
        if (handleProgressAction(mouseX, mouseY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private long lastSeekTime = 0;

    private boolean handleProgressAction(double mouseX, double mouseY) {
        float scale = getScale();
        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;
        int progX = cardX + 95;
        int progY = cardY + 247;
        int progWidth = 360;

        if (mouseX >= progX && mouseX <= progX + progWidth && mouseY >= progY - 4 && mouseY <= progY + 6) {
            double progressRatio = (mouseX - progX) / (double) progWidth;
            progressRatio = Math.max(0.0, Math.min(1.0, progressRatio));

            long now = System.currentTimeMillis();
            if (now - lastSeekTime > 200) {
                CustomMusicManager.getInstance().seekToProgress(progressRatio);
                lastSeekTime = now;
            }
            return true;
        }
        return false;
    }

    private boolean handleVolumeAction(double mouseX, double mouseY) {
        float scale = getScale();
        int logicalWidth = (int)(this.width / scale);
        int logicalHeight = (int)(this.height / scale);
        int cardWidth = 480;
        int cardHeight = 300;
        int cardX = (logicalWidth - cardWidth) / 2;
        int cardY = (logicalHeight - cardHeight) / 2;

        int sliderX = cardX + 130;
        int sliderWidth = 80;

        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= cardY + 252 && mouseY <= cardY + 272) {
            float volume = (float) ((mouseX - sliderX) / (double) sliderWidth);
            volume = Math.max(0.0f, Math.min(1.0f, volume));
            CustomMusicManager.getInstance().setVolume(volume);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
