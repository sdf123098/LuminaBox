package com.luminabox.platform;

import com.luminabox.audio.MusicTrack;
import java.util.ArrayList;
import java.util.List;

public class PlatformMusicApi {
    
    /**
     * Mocks searching for online music on platforms (Netease/QQ)
     */
    public static List<MusicTrack> searchSongs(String query, String platform) {
        List<MusicTrack> results = new ArrayList<>();
        // Mock results based on platform API query
        results.add(new MusicTrack(
            platform + "_1",
            "Online Track: " + query + " (Hit 1)",
            "Online Artist",
            "https://music-stream-url-placeholder.com/stream/1.mp3",
            MusicTrack.SourceType.PLATFORM,
            210
        ));
        results.add(new MusicTrack(
            platform + "_2",
            "Online Track: " + query + " (Hit 2)",
            "Online Artist 2",
            "https://music-stream-url-placeholder.com/stream/2.mp3",
            MusicTrack.SourceType.PLATFORM,
            180
        ));
        return results;
    }

    /**
     * Mocks generating a QR code URL for scan login
     */
    public static String getLoginQrCodeUrl(String platform) {
        return "https://api-placeholder.com/" + platform + "/login/qrcode/url";
    }
}
