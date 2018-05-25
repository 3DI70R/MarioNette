package ru.threedisevenzeror.marionette.network;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import ru.threedisevenzeror.marionette.model.message.generic.ClientInfoMessage;
import ru.threedisevenzeror.marionette.model.message.generic.ConnectionClosedMessage;
import ru.threedisevenzeror.marionette.model.message.generic.PingPongMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private static ScheduledExecutorService pingExecutor = Executors.newSingleThreadScheduledExecutor();

    private ClientInfoMessage clientInfo;
    private Map<Class, OnMessageReceived> receiverMap;
    private ScheduledFuture<?> pingPongFuture;
    private boolean isDisconnected;
    private boolean isHandshakeFinished;

    public MessageHandler() {
        receiverMap = new HashMap<>();

        addMessageHandler(ClientInfoMessage.class, (c, p) -> {
            clientInfo = p;
        });
        addMessageHandler(PingPongMessage.class, (c, p) -> {
            if(p.isPingPacket) {
                c.channel().writeAndFlush(new PingPongMessage(false));
            }
        });
        addMessageHandler(ConnectionClosedMessage.class, (c, p) ->
                closeConnection(c, p.reason));
    }

    public ClientInfoMessage getClientInfo() {
        return clientInfo;
    }

    public <T> MessageHandler addMessageHandler(Class<T> clazz, OnMessageReceived<T> handler) {
        receiverMap.put(clazz, handler);
        return this;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        pingPongFuture = pingExecutor.scheduleAtFixedRate(() -> ctx.channel().writeAndFlush(
                new PingPongMessage(true)), 0, 5, TimeUnit.SECONDS
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeConnection(ctx, "Channel is closed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(!isHandshakeFinished) {
            if(msg instanceof ClientInfoMessage) {
                clientInfo = (ClientInfoMessage) msg;
                isHandshakeFinished = true;
            } else {
                closeConnection(ctx, "Invalid packet");
                return;
            }
        }

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

            if(pingPongFuture != null) {
                pingPongFuture.cancel(false);
            }
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
