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

import org.galibier.core.Controller;
import org.galibier.core.Switch;
import org.jboss.netty.channel.*;
import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenFlowControllerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger log = LoggerFactory.getLogger(OpenFlowControllerHandler.class);
    private static final OFMessageFactory factory = new BasicFactory();

    private final ScheduledExecutorService timer;

    private final Controller controller;
    private Switch client;
    private Channel channel;

    private final AtomicInteger nextTransactionId = new AtomicInteger(0);
    private final ConcurrentMap<Integer, OFMessage> pendingOperations =
            new ConcurrentHashMap<Integer, OFMessage>();

    public OpenFlowControllerHandler(Controller controller, ScheduledExecutorService timer) {
        this.controller = controller;
        this.timer = timer;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof OFMessage) {
            OFMessage in = (OFMessage)e.getMessage();
            handleMessage(in);
        }
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        switchConnected(ctx.getChannel());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        switchDisconnected();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.warn("Exception occurred", e.getCause());

        e.getChannel().close();
    }

    public void handleMessage(OFMessage in) {
        switch(in.getType()) {
            case HELLO:
                handleHello((OFHello)in);
                break;
            case ERROR:
                handleError((OFError)in);
                break;
            case ECHO_REQUEST:
                handleEchoRequest((OFEchoRequest)in);
                break;
            case ECHO_REPLY:
                handleEchoReply((OFEchoReply)in);
                break;
            case VENDOR:
                handleVendor((OFVendor)in);
                break;
            case FEATURES_REQUEST:
                handleFeaturesRequest((OFFeaturesRequest)in);
                break;
            case FEATURES_REPLY:
                handleFeaturesReply((OFFeaturesReply)in);
                break;
            case GET_CONFIG_REQUEST:
                handleGetConfigRequest((OFGetConfigRequest)in);
                break;
            case GET_CONFIG_REPLY:
                handleGetConfigReply((OFGetConfigReply)in);
                break;
            case SET_CONFIG:
                handleSetConfig((OFSetConfig)in);
                break;
            case PACKET_IN:
                handlePacketIn((OFPacketIn)in);
                break;
            case FLOW_REMOVED:
                handleFlowRemoved((OFFlowRemoved)in);
                break;
            case PORT_STATUS:
                handlePortStatus((OFPortStatus)in);
                break;
            case PACKET_OUT:
                handlePacketOut((OFPacketOut)in);
                break;
            case FLOW_MOD:
                handleFlowModification((OFFlowMod)in);
                break;
            case PORT_MOD:
                handlePortModification((OFPortMod)in);
                break;
            case STATS_REQUEST:
                handleStatisticsRequest((OFStatisticsRequest)in);
                break;
            case STATS_REPLY:
                handleStatisticsReply((OFStatisticsReply)in);
                break;
            case BARRIER_REQUEST:
                handleBarrierRequest((OFBarrierRequest)in);
                break;
            case BARRIER_REPLY:
                handleBarrierReply((OFBarrierReply)in);
                break;
        }
    }

    private void handleHello(OFHello in) {
        log.info("HELLO received from {}", channel.getRemoteAddress());
        sendFeaturesRequest();
    }

    private void handleError(OFError in) {
        //  TODO: implement more solid codes
        log.info("OpenFlow error occurred, error type: {}, error code: {}, ",
                in.getErrorType(), in.getErrorCode());
    }

    private void handleEchoRequest(OFEchoRequest in) {
        log.debug("ECHO REQUEST received from {}", channel.getRemoteAddress());
        sendEchoReply(in.getXid());
    }

    private void handleEchoReply(OFEchoReply in) {
        log.debug("ECHO REPLY received from {}", channel.getRemoteAddress());
    }

    private void handleVendor(OFVendor in) {
        //  TODO: have to implement
    }

    private void handleFeaturesRequest(OFFeaturesRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleFeaturesReply(OFFeaturesReply in) {
        log.info("FEATURE REPLY received from {}", channel.getRemoteAddress());
        client.setFeatures(in);

        controller.switchHandshaked(client);
    }

    private void handleGetConfigRequest(OFGetConfigRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleGetConfigReply(OFGetConfigReply in) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void handleSetConfig(OFSetConfig in) {
        handleUnsupportedMessage(in);
    }

    private void handlePacketIn(OFPacketIn in) {
        controller.handlePacketIn(client, in);
    }

    private void handleFlowRemoved(OFFlowRemoved in) {
        controller.handleFlowRemoved(client, in);
    }

    private void handlePortStatus(OFPortStatus in) {
        controller.handlePortStatus(client, in);
    }

    private void handlePacketOut(OFPacketOut in) {
        handleUnsupportedMessage(in);
    }

    private void handleFlowModification(OFFlowMod in) {
        handleUnsupportedMessage(in);
    }

    private void handlePortModification(OFPortMod in) {
        handleUnsupportedMessage(in);
    }

    private void handleStatisticsRequest(OFStatisticsRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleStatisticsReply(OFStatisticsReply in) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void handleBarrierRequest(OFBarrierRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleBarrierReply(OFBarrierReply in) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void handleUnsupportedMessage(OFMessage in) {
        log.warn("Unsupported message ({}) received from {}", in.getType(), channel.getRemoteAddress());
    }

    public void switchConnected(Channel channel) {
        log.info("Connected from {}", this.channel.getRemoteAddress());
        this.channel = channel;
        this.client = new Switch(channel);

        sendHello();
        //  TODO: start sending echo request periodically
    }

    public void switchDisconnected() {
        log.info("Disconnected from {}", channel.getRemoteAddress());

        //  tell the parent that the connection to a switch is released
        controller.switchDisconnected(client);

        //  close the channel
        ChannelFuture future = channel.getCloseFuture();
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                client = null;
            }
        });
    }

    public void sendMessage(OFMessage out) {
        out.setXid(nextTransactionId.incrementAndGet());
        channel.write(out);
    }

    public void sendHello() {
        OFMessage hello = factory.getMessage(OFType.HELLO);
        sendMessage(hello);
        log.info("HELLO sent to {}", channel.getRemoteAddress());
    }

    public void sendFeaturesRequest() {
        OFFeaturesRequest request = (OFFeaturesRequest)factory.getMessage(OFType.FEATURES_REQUEST);
        sendMessage(request);
        log.info("FEATURES REQUEST sent to {}", channel.getRemoteAddress());
    }

    private void sendEchoReply(int xid) {
        OFEchoReply reply = (OFEchoReply)factory.getMessage(OFType.ECHO_REPLY);
        reply.setXid(xid);
        channel.write(reply);
        log.debug("ECHO REPLY sent to {}", channel.getRemoteAddress());
    }

}
