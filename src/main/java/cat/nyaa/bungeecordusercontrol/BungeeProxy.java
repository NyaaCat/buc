/*
 * Copyright (c) 2016 Minelink Incorporated
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * https://github.com/MinelinkNetwork/BungeeProxy/blob/master/src/main/java/net/minelink/bungeeproxy/BungeeProxy.java
 */
package cat.nyaa.bungeecordusercontrol;

import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.logging.Level;


public class BungeeProxy {
    public BungeeProxy(BUC plugin) {
        try {
            Field remoteAddressField = AbstractChannel.class.getDeclaredField("remoteAddress");
            remoteAddressField.setAccessible(true);
            Class pipelineUtilsClazz = Class.forName("net.md_5.bungee.netty.PipelineUtils");
            Field serverChild = pipelineUtilsClazz.getDeclaredField("SERVER_CHILD");
            serverChild.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(serverChild, serverChild.getModifiers() & ~Modifier.FINAL);

            ChannelInitializer<Channel> bungeeChannelInitializer = (ChannelInitializer<Channel>) serverChild.get(pipelineUtilsClazz);

            Method initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannelMethod.setAccessible(true);

            serverChild.set(null, new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    initChannelMethod.invoke(bungeeChannelInitializer, channel);
                    if (plugin.config.haproxy_enable &&
                            plugin.config.haproxy_address.contains(((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress())) {
                        channel.pipeline().addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder() {
                        });
                        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof HAProxyMessage) {
                                    HAProxyMessage message = (HAProxyMessage) msg;
                                    remoteAddressField.set(channel, new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                                } else {
                                    super.channelRead(ctx, msg);
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
            plugin.getProxy().stop();
        }
    }
}
