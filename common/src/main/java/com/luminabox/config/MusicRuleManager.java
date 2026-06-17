package com.luminabox.config;

import com.luminabox.LuminaBox;
import com.luminabox.audio.MusicTrack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MusicRuleManager {
    private static MusicRuleManager instance;

    private Map<String, String> dimensions = new HashMap<>();
    private Map<String, String> biomes = new HashMap<>();
    private Map<String, String> bosses = new HashMap<>();
    private String combatMusic = "";
    private String bossMusic = "";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MusicRuleManager getInstance() {
        if (instance == null) {
            instance = new MusicRuleManager();
            instance.load();
        }
        return instance;
    }

    private File getConfigFile() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File configDir = new File(gameDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "luminabox_rules.json");
    }

    public void load() {
        File file = getConfigFile();
        if (!file.exists()) {
            // Write default values
            dimensions.put("minecraft:overworld", "overworld_theme.mp3");
            dimensions.put("minecraft:the_nether", "nether_theme.mp3");
            dimensions.put("minecraft:the_end", "end_theme.mp3");
            biomes.put("minecraft:desert", "desert_theme.mp3");
            biomes.put("minecraft:plains", "plains_theme.mp3");
            bosses.put("ender_dragon", "ender_dragon_theme.mp3");
            bosses.put("wither", "wither_theme.mp3");
            combatMusic = "combat_theme.mp3";
            bossMusic = "boss_theme.mp3";
            save();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            MusicRuleManager loaded = GSON.fromJson(reader, MusicRuleManager.class);
            if (loaded != null) {
                this.dimensions = loaded.dimensions != null ? loaded.dimensions : new HashMap<>();
                this.biomes = loaded.biomes != null ? loaded.biomes : new HashMap<>();
                this.bosses = loaded.bosses != null ? loaded.bosses : new HashMap<>();
                this.combatMusic = loaded.combatMusic != null ? loaded.combatMusic : "";
                this.bossMusic = loaded.bossMusic != null ? loaded.bossMusic : "";
            }
        } catch (IOException e) {
            LuminaBox.LOGGER.error("Failed to load music rules config", e);
        }
    }

    public void save() {
        File file = getConfigFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LuminaBox.LOGGER.error("Failed to save music rules config", e);
        }
    }

    public MusicTrack getMatchTrack(String type, String id) {
        String filename = "";
        if ("dimension".equals(type)) {
            filename = dimensions.get(id);
        } else if ("biome".equals(type)) {
            filename = biomes.get(id);
        } else if ("combat".equals(type)) {
            filename = combatMusic;
        } else if ("boss".equals(type)) {
            filename = bosses.get(id);
            if (filename == null || filename.isEmpty()) {
                filename = bossMusic;
            }
        }

        if (filename == null || filename.isEmpty()) {
            return null;
        }

        return new MusicTrack(
            type + "_" + id.replace(":", "_"),
            filename,
            "RulesConfig",
            filename,
            MusicTrack.SourceType.LOCAL,
            180
        );
    }

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public Map<String, String> getBiomes() {
        return biomes;
    }

    public Map<String, String> getBosses() {
        return bosses;
    }

    public String getCombatMusic() {
        return combatMusic;
    }

    public void setCombatMusic(String combatMusic) {
        this.combatMusic = combatMusic;
    }

    public String getBossMusic() {
        return bossMusic;
    }

    public void setBossMusic(String bossMusic) {
        this.bossMusic = bossMusic;
    }
}
