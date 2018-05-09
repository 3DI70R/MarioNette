package ru.threedisevenzeror.marionette.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Object> {

    private Kryo kryo;
    private MessageMapping protocol;

    public MessageEncoder(MessageMapping protocol) {
        this.protocol = protocol;

        kryo = new Kryo();
        kryo.setReferences(false);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        int id = protocol.getPacketId(msg.getClass());
        ByteBufferOutput output = new ByteBufferOutput(new ByteBufOutputStream(out));
        output.write(id);
        kryo.writeObject(output, msg);
        output.flush();
    }
}
