package ru.threedisevenzeror.marionette.network;

import io.netty.channel.ChannelHandlerContext;

public interface OnMessageReceived<T> {

    void onMessageReceived(ChannelHandlerContext context, T packet);
}
