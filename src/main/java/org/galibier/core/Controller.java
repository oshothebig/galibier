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

package org.galibier.core;

import org.galibier.netty.OpenFlowServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final int DEFAULT_SWITCHES = 64;

    private Channel channel;
    private ChannelFactory factory;
    private ServerBootstrap bootstrap;

    private final ConcurrentMap<Long, Switch> handshakedSwitches =
            new ConcurrentHashMap<Long, Switch>(DEFAULT_SWITCHES);

    private final CopyOnWriteArrayList<EventListener> eventListeners =
            new CopyOnWriteArrayList<EventListener>();

    public void start(int port) {
        factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        bootstrap = new ServerBootstrap(factory);

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        bootstrap.setPipelineFactory(new OpenFlowServerPipelineFactory(this, timer));
        bootstrap.setOption("reuseAddress", true);

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        channel = bootstrap.bind(new InetSocketAddress(port));
        log.info("Controller started: {}", channel.getLocalAddress());
    }

    public void switchHandshaked(Switch sw) {
        handshakedSwitches.put(sw.dataPathId(), sw);

        //  TODO: is ordering of invocation of listeners needed ?
        //  TODO: is concurrent invocation of listeners needed ?
        for (EventListener listener: eventListeners) {
            listener.switchConnected(sw);
        }
    }

    public synchronized void switchDisconnected(Switch sw) {
        handshakedSwitches.remove(sw.dataPathId());

        //  TODO: is ordering of invocation of listeners needed ?
        //  TODO: is concurrent invocation of listeners needed ?
        for (EventListener listener: eventListeners) {
            listener.switchDisconnected(sw);
        }
    }

    public void handlePacketIn(Switch sw, OFPacketIn in) {
        for (EventListener listener: eventListeners) {
            listener.handlePacketIn(sw, in);
        }
    }

    public void handleFlowRemoved(Switch sw, OFFlowRemoved in) {
        for (EventListener listener: eventListeners) {
            listener.handleFlowRemoved(sw, in);
        }
    }

    public void handlePortStatus(Switch sw, OFPortStatus in) {
        for (EventListener listener: eventListeners) {
            listener.handlePortStatus(sw, in);
        }
    }

    public void addEventListener(EventListener listener) {
        eventListeners.addIfAbsent(listener);
    }

    public void removeEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    //  TODO: Do callback or Future have to be supported to notify when the reply is received?
    //  Some kinds of messages do not introduce the reply.
    public void sendMessage(Switch sw, OFMessage msg) {
        long datapathId = sw.dataPathId();
        //  TODO: have to write codes to send packets
    }
}
