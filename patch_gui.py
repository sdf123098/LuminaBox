# coding: utf-8
import re

file_path = 'common/src/main/java/com/dynamicbgm/client/gui/MusicPlayerScreen.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add field for modeButton
content = content.replace('private Button stopButton;', 'private Button stopButton;\n    private net.minecraft.client.gui.components.Button modeButton;')

# 2. Add mode helper method
helper = '''
    private String getModeIcon(com.dynamicbgm.audio.CustomMusicManager.PlaybackMode mode) {
        switch (mode) {
            case LOOP_ALL: return "\uD83D\uDD01"; // 🔁
            case LOOP_ONE: return "\uD83D\uDD02"; // 🔂
            case SHUFFLE: return "\uD83D\uDD00"; // 🔀
            case SEQUENTIAL: default: return "\u2B07"; // ⬇
        }
    }
'''
content = content.replace('    private void toggleSelectMode() {', helper + '\n    private void toggleSelectMode() {')

# 3. Add mode button instantiation
mode_btn_code = '''
        modeButton = this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
            net.minecraft.network.chat.Component.literal(getModeIcon(com.dynamicbgm.audio.CustomMusicManager.getInstance().getPlaybackMode())),
            button -> {
                com.dynamicbgm.audio.CustomMusicManager.getInstance().cyclePlaybackMode();
                button.setMessage(net.minecraft.network.chat.Component.literal(getModeIcon(com.dynamicbgm.audio.CustomMusicManager.getInstance().getPlaybackMode())));
            }
        ).bounds(cardX + 365, cardY + 270, 20, 20).build());
'''
content = content.replace('        // --- TAB 0: LOCAL TAB WIDGETS ---', mode_btn_code + '\n        // --- TAB 0: LOCAL TAB WIDGETS ---')

# 4. Patch importButton
import_btn_old_regex = r'importButton = this\.addRenderableWidget\(Button\.builder\(\s*Component\.literal\(".*?"\),\s*button -> \{.*?\}\s*\)\.bounds\(cardX \+ 120, cardY \+ 270, 20, 20\)\.build\(\)\);'

import_btn_new = '''importButton = this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
            net.minecraft.network.chat.Component.literal("\uD83D\uDCC1"),
            button -> {
                boolean isAndroid = System.getProperty("os.name").toLowerCase().contains("android") ||
                                    System.getProperty("java.vm.name").toLowerCase().contains("dalvik") ||
                                    System.getProperty("java.vm.name").toLowerCase().contains("art");
                if (isAndroid) {
                    String path = com.dynamicbgm.audio.CustomMusicManager.getInstance().getLocalMusicFolder().getAbsolutePath();
                    com.dynamicbgm.audio.CustomMusicManager.showToast("Android: Please put music in " + path);
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
                            String[] files = selectedFiles.split("\\\\|");
                            java.io.File destDir = com.dynamicbgm.audio.CustomMusicManager.getInstance().getLocalMusicFolder();
                            for (String fPath : files) {
                                java.io.File src = new java.io.File(fPath);
                                if (src.exists() && src.isFile()) {
                                    java.io.File dest = new java.io.File(destDir, src.getName());
                                    java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    } catch (Exception e) {
                        com.dynamicbgm.DynamicBgm.LOGGER.error("Failed to import files", e);
                    } finally {
                        stack.close();
                    }
                }
            }
        ).bounds(cardX + 120, cardY + 270, 20, 20).build());'''

content = re.sub(import_btn_old_regex, import_btn_new, content, flags=re.DOTALL)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("MusicPlayerScreen modified.")
