package com.luminabox.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPlaylistPayload(String jsonPlaylist) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPlaylistPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("luminabox", "sync_playlist"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlaylistPayload> STREAM_CODEC = StreamCodec.ofMember(
        SyncPlaylistPayload::write,
        SyncPlaylistPayload::new
    );

    public SyncPlaylistPayload(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.jsonPlaylist);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
