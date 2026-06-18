package com.luminabox.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FileUploadPayload(String fileName, int chunkIndex, int totalChunks, byte[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileUploadPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("luminabox", "file_upload"));

    public static final StreamCodec<FriendlyByteBuf, FileUploadPayload> STREAM_CODEC = StreamCodec.ofMember(
        FileUploadPayload::write,
        FileUploadPayload::new
    );

    public FileUploadPayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readByteArray());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.fileName);
        buf.writeInt(this.chunkIndex);
        buf.writeInt(this.totalChunks);
        buf.writeByteArray(this.data);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
