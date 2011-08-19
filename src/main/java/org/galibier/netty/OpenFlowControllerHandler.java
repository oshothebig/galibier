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
                log.info("HELLO received from {}", ctx.getChannel().getRemoteAddress());
                response = factory.getMessage(OFType.FEATURES_REQUEST);
                e.getChannel().write(response);
                log.info("FEATURE REQUEST sent to {}", ctx.getChannel().getRemoteAddress());
                break;
            case FEATURES_REPLY:
                log.info("FEATURE REPLY received from {}", ctx.getChannel().getRemoteAddress());
                client.setFeatures((OFFeaturesReply) request);
                controller.addSwitch(client);
                return;
            case ECHO_REQUEST:
                response = factory.getMessage(OFType.ECHO_REPLY);
                response.setXid(request.getXid());
                e.getChannel().write(response);
                log.info("ECHO REPLY sent to {}", ctx.getChannel().getRemoteAddress());
                break;
            case ERROR:
                OFError error = (OFError) request;
                logError(client, error);
                return;
            default:
                controller.invokeMessageListener(client, request);
                return;
        }
    }
    
    private void logError(Switch client, OFError error) {
        //  TODO: implement more solid codes
        log.info("OpenFlow error occurred, error type: {}, error code: {}, ",
                error.getErrorType(), error.getErrorCode());
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
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Disconnected from {}", ctx.getChannel().getRemoteAddress());
        controller.removeSwitch(client);
        e.getChannel().close();
    }
}
