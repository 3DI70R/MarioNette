package ru.threedisevenzeror.marionette.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

import java.util.List;

/**
 * Packet decoder
 * Decodes input byte array to message that defined in MessageMapping using kryo
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private MessageMapping protocol;

    public MessageDecoder(MessageMapping protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int id = in.readUnsignedByte();
        NetworkObject obj = protocol.createObjectForPacket(id);
        obj.read(in);
        out.add(obj);
    }
}
