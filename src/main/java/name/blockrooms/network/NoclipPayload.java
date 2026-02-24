package name.blockrooms.network;

import io.netty.buffer.ByteBuf;
import name.blockrooms.Blockrooms;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NoclipPayload() implements CustomPacketPayload {
    public static final Type<NoclipPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Blockrooms.MODID, "noclip"));

    public static final StreamCodec<ByteBuf, NoclipPayload> STREAM_CODEC =
            StreamCodec.unit(new NoclipPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
