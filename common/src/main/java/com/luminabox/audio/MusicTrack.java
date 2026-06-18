package com.luminabox.audio;

public class MusicTrack {
    private final String id;
    private final String title;
    private final String artist;
    private final String sourcePathOrUrl;
    private final SourceType sourceType;
    private final int durationSeconds;

    public enum SourceType {
        LOCAL,
        SERVER,
        PLATFORM
    }

    public MusicTrack(String id, String title, String artist, String sourcePathOrUrl, SourceType sourceType, int durationSeconds) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.sourcePathOrUrl = sourcePathOrUrl;
        this.sourceType = sourceType;
        this.durationSeconds = durationSeconds;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSourcePathOrUrl() {
        return sourcePathOrUrl;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public String toString() {
        return artist + " - " + title + " (" + sourceType + ")";
    }
}
