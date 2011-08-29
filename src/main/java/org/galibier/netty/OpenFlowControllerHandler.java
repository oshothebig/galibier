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
import org.galibier.core.MessageDispatcher;
import org.galibier.core.Operation;
import org.galibier.core.Switch;
import org.jboss.netty.channel.*;
import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.galibier.core.Constants.*;

public class OpenFlowControllerHandler extends SimpleChannelUpstreamHandler implements MessageDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OpenFlowControllerHandler.class);
    private static final OFMessageFactory factory = new BasicFactory();
    private static final long ECHO_REQUEST_INTERVAL = 5000; // milli sec
    private static final long FEATURES_REQUEST_INTERVAL = 5000; //  milli sec

    private final ScheduledExecutorService timer;

    private final Controller controller;
    private Switch client;
    private Channel channel;

    private final AtomicInteger nextTransactionId = new AtomicInteger(0);
    private final ConcurrentMap<Integer, Operation> pendingOperations =
            new ConcurrentHashMap<Integer, Operation>();
    private ScheduledFuture<?> featuresRequestTask;
    private ScheduledFuture<?> echoRequestTask;

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
        log.debug("{} received from {}", in.getType(), channel.getRemoteAddress());
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
        sendFeaturesRequest();
    }

    private void handleError(OFError in) {
        //  TODO: implement more solid codes
        log.info("OpenFlow error occurred, error type: {}, error code: {}, ",
                in.getErrorType(), in.getErrorCode());
    }

    private void handleEchoRequest(OFEchoRequest in) {
        sendEchoReply(in.getXid());
    }

    private void handleEchoReply(OFEchoReply in) {
        //  TODO
        //  disconnect when continuous ECHO_REPLY can not be received
    }

    private void handleVendor(OFVendor in) {
        //  TODO: have to implement
    }

    private void handleFeaturesRequest(OFFeaturesRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleFeaturesReply(OFFeaturesReply in) {
        //  
        client.setFeatures(in);

        controller.switchHandshaked(client);
    }

    private void handleGetConfigRequest(OFGetConfigRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleGetConfigReply(OFGetConfigReply in) {
        //  TODO
        //  if this GET_CONFIG_REPLY is corresponds to the startSendFeatureRequestPeriodically,
        //  stop the task.
        stopSendFeaturesRequestPeriodically();
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

    private void switchConnected(Channel channel) {
        log.info("Connected from {}", channel.getRemoteAddress());
        this.channel = channel;
        this.client = new Switch(this);

        sendHello();

        //  sending echo request periodically
        echoRequestTask = timer.scheduleAtFixedRate(new Runnable() {
            public void run() {
                send(factory.getMessage(OFType.ECHO_REQUEST));
            }
        }, ECHO_REQUEST_INTERVAL, ECHO_REQUEST_INTERVAL, TimeUnit.MILLISECONDS);

        //  sending FEATURES REQUEST periodically until FEATURES REPLY is received
        featuresRequestTask = timer.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (client.isHandshaken()) {
                    OFSetConfig config = (OFSetConfig) factory.getMessage(OFType.SET_CONFIG);
                    config.setMissSendLength((short)0xffff).setLengthU(OFSetConfig.MINIMUM_LENGTH);
                    send(config);
                    send(factory.getMessage(OFType.GET_CONFIG_REQUEST));
                } else {
                    send(factory.getMessage(OFType.FEATURES_REQUEST));
                }
            }
        }, FEATURES_REQUEST_INTERVAL, FEATURES_REQUEST_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void stopSendFeaturesRequestPeriodically() {
        if (featuresRequestTask != null) {
            featuresRequestTask.cancel(false);
        }
    }

    private void stopSendEchoRequestPeriodically() {
        if (echoRequestTask != null) {
            echoRequestTask.cancel(false);
        }
    }

    private void switchDisconnected() {
        log.info("Disconnected from {}", channel.getRemoteAddress());
        stopSendEchoRequestPeriodically();
        stopSendFeaturesRequestPeriodically();

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

    public void send(OFMessage out) {
        if (channel != null && channel.isConnected()) {
            out.setXid(nextTransactionId.incrementAndGet());

            ChannelFuture future = channel.write(out);
            if (REQUEST_TYPE.contains(out.getType())) {
                final Operation op = new Operation(out);
                pendingOperations.putIfAbsent(out.getXid(), op);
                future.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        op.sentCompleted();
                    }
                });
            }
            log.debug("{} sent to {}", out.getType(), channel.getRemoteAddress());
        }
    }

    public void stop() {
        stopSendEchoRequestPeriodically();
        stopSendFeaturesRequestPeriodically();
        channel.getCloseFuture().awaitUninterruptibly();
    }

    private Operation terminateRequest(OFMessage reply) {
        int xid = reply.getXid();
        Operation op = pendingOperations.remove(xid);
        if (op != null) {
            //  tell that the operation is completed
        } else {
            //  already removed from the map
        }
        return op;
    }

    private void sendHello() {
        OFMessage hello = factory.getMessage(OFType.HELLO);
        send(hello);
        log.info("{} sent to {}", hello.getType(), channel.getRemoteAddress());
    }

    private void sendFeaturesRequest() {
        OFMessage request = factory.getMessage(OFType.FEATURES_REQUEST);
        send(request);
        log.info("{} sent to {}", request.getType(), channel.getRemoteAddress());
    }

    private void sendEchoReply(int xid) {
        OFEchoReply reply = (OFEchoReply)factory.getMessage(OFType.ECHO_REPLY);
        reply.setXid(xid);
        channel.write(reply);
        log.debug("{} sent to {}", reply.getType(), channel.getRemoteAddress());
    }

}
