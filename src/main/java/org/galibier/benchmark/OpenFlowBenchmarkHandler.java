/*
 * Copyright (c) 2011, Sho SHIMIZU
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.galibier.benchmark;

import org.jboss.netty.channel.*;
import org.openflow.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowBenchmarkHandler extends SimpleChannelUpstreamHandler{
    private static final Logger log = LoggerFactory.getLogger(OpenFlowBenchmarkHandler.class);

    private FakeSwitch fakeSwitch;
    private volatile long benchmarkEndTime;
    private Channel channel;

    public OpenFlowBenchmarkHandler(FakeSwitch fakeSwitch) {
        this.fakeSwitch = fakeSwitch;
    }

    public void start(long start, int duration) {
        benchmarkEndTime = start + (long)duration * 1000 * 1000;
        OFMessage packetIn = fakeSwitch.packetInData();
        channel.write(packetIn);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        OFMessage in = (OFMessage) e.getMessage();
        OFMessage out;
        switch (in.getType()) {
            case HELLO:
                break;
            case ECHO_REQUEST:
                out = fakeSwitch.echoReplyData((OFEchoRequest)in);
                ctx.getChannel().write(out);
                break;
            case FEATURES_REQUEST:
                out = fakeSwitch.featureReplyData((OFFeaturesRequest)in);
                ctx.getChannel().write(out);
                break;
            case GET_CONFIG_REQUEST:
                out = fakeSwitch.getConfigReplyData((OFGetConfigRequest)in);
                ctx.getChannel().write(out);
                break;
            case VENDOR:
                out = fakeSwitch.vendorReplyData((OFVendor)in);
                ctx.getChannel().write(out);
            case PACKET_OUT:
            case FLOW_MOD:
                long currentTime = System.nanoTime();
                if (currentTime > benchmarkEndTime) {
                    break;
                }
                
                fakeSwitch.receiveMessages();
                out = fakeSwitch.packetInData();
                ChannelFuture future = ctx.getChannel().write(out);
                future.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        fakeSwitch.sendingPacketInCompleted();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ctx.getChannel().close();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = ctx.getChannel();
        OFMessage hello = fakeSwitch.helloData();
        ctx.getChannel().write(hello);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.getChannel().close();
    }
}
