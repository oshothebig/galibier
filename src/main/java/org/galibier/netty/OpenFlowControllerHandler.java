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

package org.galibier.netty;

import org.galibier.controller.core.Controller;
import org.galibier.controller.core.Switch;
import org.jboss.netty.channel.*;
import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenFlowControllerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger log = LoggerFactory.getLogger(OpenFlowControllerHandler.class);
    private static final OFMessageFactory factory = new BasicFactory();
    private Switch client;
    private Controller controller;

    public OpenFlowControllerHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        OFMessage request = (OFMessage) e.getMessage();
        OFMessage response;
        switch (request.getType()) {
            case HELLO:
                handleHello(ctx, (OFHello)request);
                break;
            case FEATURES_REPLY:
                handleFeaturesReply(ctx, (OFFeaturesReply)request);
                break;
            case ECHO_REQUEST:
                handleEchoRequest(ctx, (OFEchoRequest)request);
                break;
            case ECHO_REPLY:
                handleEchoReply(ctx, (OFEchoReply)request);
                break;
            case ERROR:
                handleError(ctx, (OFError)request);
                return;
            case PACKET_IN:
                handlePacketIn(ctx, (OFPacketIn)request);
                break;
            case PACKET_OUT:
                handlePacketOut(ctx, (OFPacketOut)request);
                break;
            default:
                controller.invokeMessageListener(client, request);
                return;
        }
    }

    //  Method for handling OpenFlow protocol
    //  Symmetric message
    private void handleHello(ChannelHandlerContext ctx, OFHello in) {
        log.info("HELLO received from {}", ctx.getChannel().getRemoteAddress());

        OFFeaturesRequest out = (OFFeaturesRequest)factory.getMessage(OFType.FEATURES_REQUEST);
        log.info("FEATURES REQUEST sent tot {}", ctx.getChannel().getRemoteAddress());
        ctx.getChannel().write(out);
    }

    private void handleError(ChannelHandlerContext ctx, OFError error) {
        //  TODO: implement more solid codes
        log.info("OpenFlow error occurred, error type: {}, error code: {}, ",
                error.getErrorType(), error.getErrorCode());
    }

    private void handleEchoRequest(ChannelHandlerContext ctx, OFEchoRequest in) {
        log.debug("ECHO REQUEST received from {}", ctx.getChannel().getRemoteAddress());

        OFEchoReply out = (OFEchoReply)factory.getMessage(OFType.ECHO_REPLY);
        out.setXid(in.getXid());
        ctx.getChannel().write(out);
        log.debug("ECHO REPLY sent to {}", ctx.getChannel().getRemoteAddress());
    }

    private void handleEchoReply(ChannelHandlerContext ctx, OFEchoReply in) {
        //  ignore
    }

    private void handleVendor(ChannelHandlerContext ctx, OFVendor in) {
        
    }

    //  Controller/switch message
    private void handleFeaturesRequest(ChannelHandlerContext ctx, OFFeaturesRequest in) {
        //  ignore
    }

    private void handleFeaturesReply(ChannelHandlerContext ctx, OFFeaturesReply in) {
        log.info("FEATURE REPLY received from {}", ctx.getChannel().getRemoteAddress());
        client.setFeatures((OFFeaturesReply) in);
        controller.addSwitch(client);
    }

    //  Asynchronous message
    private void handlePacketIn(ChannelHandlerContext ctx, OFPacketIn in) {
        controller.invokeMessageListener(client, in);
    }

    private void handlePacketOut(ChannelHandlerContext ctx, OFPacketOut in) {
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.info("Exception occurred from {}:{}", ctx.getChannel().getRemoteAddress(), e.getCause());

        e.getChannel().close();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Connected from {}", ctx.getChannel().getRemoteAddress());
        client = new Switch(ctx.getChannel());
        e.getChannel().write(factory.getMessage(OFType.HELLO));
        //  TODO: start sending echo request periodically
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Disconnected from {}", ctx.getChannel().getRemoteAddress());
        controller.removeSwitch(client);
        e.getChannel().close();
    }
}
