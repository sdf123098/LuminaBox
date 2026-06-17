package com.luminabox.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayMusicPayload(String trackId, String sourceUrl, boolean isPlay) implements CustomPacketPayload {
    public static final Type<PlayMusicPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("luminabox", "play_music"));

    public static final StreamCodec<FriendlyByteBuf, PlayMusicPayload> CODEC = StreamCodec.of(
        (buf, val) -> {
            buf.writeUtf(val.trackId);
            buf.writeUtf(val.sourceUrl);
            buf.writeBoolean(val.isPlay);
        },
        buf -> new PlayMusicPayload(buf.readUtf(), buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
