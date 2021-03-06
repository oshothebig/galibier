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

import org.galibier.core.*;
import org.jboss.netty.channel.*;
import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.galibier.core.Constants.*;

public class OpenFlowControllerHandler extends SimpleChannelUpstreamHandler implements MessageDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OpenFlowControllerHandler.class);
    private static final OFMessageFactory factory = new BasicFactory();
    private static final long ECHO_REQUEST_INTERVAL = 5000; // milli sec
    private static final long FEATURES_REQUEST_INTERVAL = 5000; //  milli sec
    private static final long ECHO_REQUEST_TIMEOUT = 10000;

    private final ScheduledExecutorService timer;

    private final Controller controller;
    private Switch client;
    private Channel channel;

    private final AtomicLong lastEchoRequestedTimeMillis = new AtomicLong();

    private final AtomicInteger nextTransactionId = new AtomicInteger(0);
    private final ConcurrentMap<Integer, OFMessageFuture> pendingOperations =
            new ConcurrentHashMap<Integer, OFMessageFuture>();
    private ScheduledFuture<?> featuresRequestTask;
    private ScheduledFuture<?> echoRequestTask;
    private ScheduledFuture<?> heartbeatCheckTask;

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
        Object[] args = {in.getType(), in.getXid(), client};
        log.debug("{} (xid={}) received from {}", args);
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
        send(factory.getMessage(OFType.FEATURES_REQUEST));
    }

    private void handleError(OFError in) {
        //  TODO: implement more solid codes
        log.info("OpenFlow error occurred, error type: {}, error code: {}, ",
                in.getErrorType(), in.getErrorCode());
    }

    private void handleEchoRequest(OFEchoRequest in) {
        OFMessage out = factory.getMessage(OFType.ECHO_REPLY);
        out.setXid(in.getXid());
        send(out, false);
    }

    private void handleEchoReply(OFEchoReply in) {
        terminateRequest(in);
    }

    private void handleVendor(OFVendor in) {
        controller.handleVendorExtension(client, in);
    }

    private void handleFeaturesRequest(OFFeaturesRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleFeaturesReply(OFFeaturesReply in) {
        terminateRequest(in);
        client.setFeatures(in);

        controller.switchHandshaken(client);
    }

    private void handleGetConfigRequest(OFGetConfigRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleGetConfigReply(OFGetConfigReply in) {
        terminateRequest(in);

        //  if this GET_CONFIG_REPLY is corresponds to the startSendFeatureRequestPeriodically,
        //  stop the task
        if (!featuresRequestTask.isCancelled()) {
            stopSendFeaturesRequestPeriodically();
        }
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
        terminateRequest(in);
    }

    private void handleBarrierRequest(OFBarrierRequest in) {
        handleUnsupportedMessage(in);
    }

    private void handleBarrierReply(OFBarrierReply in) {
        terminateRequest(in);
    }

    private void handleUnsupportedMessage(OFMessage in) {
        log.warn("Unsupported message ({}) received from {}", in.getType(), client);
    }

    private void switchConnected(Channel channel) {
        this.channel = channel;
        this.client = new Switch(this);
        log.info("Connected from {}", client);

        send(factory.getMessage(OFType.HELLO));

        //  sending echo request periodically
        echoRequestTask = timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                send(factory.getMessage(OFType.ECHO_REQUEST));
                lastEchoRequestedTimeMillis.set(System.currentTimeMillis());
            }
        }, ECHO_REQUEST_INTERVAL, ECHO_REQUEST_INTERVAL, TimeUnit.MILLISECONDS);

        //  start the heartbeat check scheduled task
        heartbeatCheckTask = timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                if (current - lastEchoRequestedTimeMillis.get() > ECHO_REQUEST_TIMEOUT) {
                    log.warn("Disconnect due to echo reply timeout");
                    stop();
                }
            }
        }, ECHO_REQUEST_TIMEOUT, ECHO_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

        //  sending FEATURES REQUEST periodically until FEATURES REPLY is received
        featuresRequestTask = timer.scheduleAtFixedRate(new Runnable() {
            @Override
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

    private void stopHeartbeatCheckTask() {
        if (heartbeatCheckTask != null) {
            heartbeatCheckTask.cancel(false);
        }
    }

    private void switchDisconnected() {
        log.info("Disconnected from {}", client);
        stopSendEchoRequestPeriodically();
        stopSendFeaturesRequestPeriodically();
        stopHeartbeatCheckTask();

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

    /**
     * Asynchronously sends a OpenFlow protocol message to the switch corresponding to this handler.
     * @param out the OpenFlow protocol message to send to the switch
     * @param autoXid true when XID of the message is automatically created, false when it is not changed
     * @return the OFMessageFuture which will be notified if the message is a kind of request messages
     */
    private OFMessageFuture send(OFMessage out, boolean autoXid) {
        if (autoXid) {
            out.setXid(nextTransactionId.incrementAndGet());
        }
        
        if (channel != null && channel.isConnected()) {
            ChannelFuture future = channel.write(out);
            OFMessageFuture messageFuture = new OFMessageFuture(out, future);
            if (REQUEST_TYPE.contains(out.getType())) {
                pendingOperations.putIfAbsent(out.getXid(), messageFuture);
            }
            Object[] args = {out.getType(), out.getXid(), client};
            log.debug("{} (xid={}) sent to {}", args);
            return messageFuture;
        } else {
            return new OFMessageFuture(out, null);
        }
    }

    @Override
    public OFMessageFuture send(OFMessage msg) {
        return send(msg, true);
    }

    /**
     * Stops the scheduled tasks associated with this handler and close the channel to the switch
     */
    public void stop() {
        stopSendEchoRequestPeriodically();
        stopSendFeaturesRequestPeriodically();
        stopHeartbeatCheckTask();
        channel.getCloseFuture().awaitUninterruptibly();
    }

    private boolean terminateRequest(OFMessage reply) {
        int xid = reply.getXid();
        OFMessageFuture future = pendingOperations.remove(xid);
        if (future != null) {
            //  tell that the operation is completed
            future.setReply(reply);
        } else {
            //  already removed from the map
            log.warn("The request corresponding to {} (Xid={}) was already processed",
                    reply.getType(), reply.getXid());
        }

        return future != null;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        //  current OpenFlow protocol does not support UDP/IP
        return (InetSocketAddress)channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        //  current OpenFlow protocol does not support UDP/IP
        return (InetSocketAddress)channel.getLocalAddress();
    }
}
