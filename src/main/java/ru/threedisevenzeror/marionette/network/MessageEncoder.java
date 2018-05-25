package ru.threedisevenzeror.marionette.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;

public class MessageEncoder extends MessageToByteEncoder<NetworkObject> {

    private MessageMapping protocol;

    public MessageEncoder(MessageMapping protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NetworkObject msg, ByteBuf out) throws Exception {
        int id = protocol.getPacketId(msg.getClass());
        out.writeByte(id);
        msg.write(out);
    }
}
