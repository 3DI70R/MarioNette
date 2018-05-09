package ru.threedisevenzeror.marionette.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Packet decoder
 * Decodes input byte array to packets that defined in MessageMapping using kryo
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private Kryo kryo;
    private MessageMapping protocol;

    public MessageDecoder(MessageMapping protocol) {
        kryo = new Kryo();
        kryo.setReferences(false);

        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int id = in.readUnsignedByte();
        Class<?> clazz = protocol.getPacketClass(id);
        ByteBufferInput input = new ByteBufferInput(new ByteBufInputStream(in));
        Object result = kryo.readObject(input, clazz);
        out.add(result);
    }
}
