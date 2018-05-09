package ru.threedisevenzeror.marionette.network;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import ru.threedisevenzeror.marionette.model.packets.generic.ConnectionClosedMessage;
import ru.threedisevenzeror.marionette.model.packets.generic.PingPongMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private Map<Class, OnMessageReceived> receiverMap;
    private boolean isDisconnected;

    public MessageHandler() {
        receiverMap = new HashMap<>();

        addMessageHandler(PingPongMessage.class, (c, p) -> {
            if(p.isPingPacket) {
                c.channel().writeAndFlush(new PingPongMessage(false));
            }
        });
        addMessageHandler(ConnectionClosedMessage.class, (c, p) ->
                closeConnection(c, "Client " + c.channel().remoteAddress() + " is disconnected: " + p.reason));
    }

    public <T> MessageHandler addMessageHandler(Class<T> clazz, OnMessageReceived<T> handler) {
        receiverMap.put(clazz, handler);
        return this;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeConnection(ctx, "Channel is closed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        OnMessageReceived received = receiverMap.get(msg.getClass());
        if(received != null) {
            received.onMessageReceived(ctx, msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        closeConnection(ctx, "Unahdled exception: " + cause.toString());
    }

    private void closeConnection(ChannelHandlerContext context, String reason) {
        if(!isDisconnected) {
            context.channel().writeAndFlush(new ConnectionClosedMessage(reason))
                    .addListener((ChannelFutureListener) future -> future.channel().close());
            System.out.println("Client disconnected: " + context.channel().remoteAddress().toString() + ": " + reason);
            isDisconnected = true;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            closeConnection(ctx, "Channel is inactive");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
