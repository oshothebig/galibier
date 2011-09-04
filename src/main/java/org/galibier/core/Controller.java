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

import com.google.common.base.Preconditions;
import org.galibier.netty.OpenFlowServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.openflow.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final int DEFAULT_SWITCHES = 64;

    private ChannelFactory factory;
    private ServerBootstrap bootstrap;

    private final ConcurrentMap<Long, Switch> handshakedSwitches =
            new ConcurrentHashMap<Long, Switch>(DEFAULT_SWITCHES);

    private final CopyOnWriteArrayList<EventListener> eventListeners =
            new CopyOnWriteArrayList<EventListener>();
    private final CopyOnWriteArrayList<VendorListener> vendorListeners =
            new CopyOnWriteArrayList<VendorListener>();
    private final ScheduledExecutorService timer =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Starts the controller. The controller waits the connection from the switch on the port.
     * @param port The port number on which the controller listens
     */
    public void start(int port) {
        factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new OpenFlowServerPipelineFactory(this, timer));
        bootstrap.setOption("reuseAddress", true);

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        log.info("Controller started: {}", channel.getLocalAddress());
    }

    /**
     * Stops the controller. All connections to the switches are closed and all threads
     * related to the controller are released.
     */
    public void stop() {
        for (Switch sw: handshakedSwitches.values()) {
            sw.stop();
        }
        timer.shutdown();
        factory.releaseExternalResources();
    }

    /**
     * For internal use. Invokes the event listeners for the event that a switch is connected.
     * It is called internally when a switch is connected to the switch.
     * @param sw The switch connected to the switch
     */
    public void switchHandshaken(Switch sw) {
        handshakedSwitches.put(sw.dataPathId(), sw);

        //  TODO: is ordering of invocation of listeners needed ?
        //  TODO: is concurrent invocation of listeners needed ?
        for (EventListener listener: eventListeners) {
            listener.switchConnected(sw);
        }
    }

    /**
     * For internal use. Invokes the event listeners for the event that a switch is disconnected.
     * It is called internally when a switch is disconnected to the switch.
     * @param sw The switch disconnected from the switch
     */
    public synchronized void switchDisconnected(Switch sw) {
        handshakedSwitches.remove(sw.dataPathId());

        //  TODO: is ordering of invocation of listeners needed ?
        //  TODO: is concurrent invocation of listeners needed ?
        for (EventListener listener: eventListeners) {
            listener.switchDisconnected(sw);
        }
    }

    /**
     * For internal use. Invokes the event listeners for the event that the controller receives a PACKET_IN message.
     * @param sw The switch that sent the PACKET_IN message
     * @param in The PACKET_IN message
     */
    public void handlePacketIn(Switch sw, OFPacketIn in) {
        for (EventListener listener: eventListeners) {
            listener.handlePacketIn(sw, in);
        }
    }

    /**
     * For internal use. Invokes the event listeners for the event that the controller receives a FLOW_REMOVED message.
     * @param sw The switch that sent the FLOW_REMOVED message
     * @param in The FLOW_REMOVED message
     */
    public void handleFlowRemoved(Switch sw, OFFlowRemoved in) {
        for (EventListener listener: eventListeners) {
            listener.handleFlowRemoved(sw, in);
        }
    }

    /**
     * For internal use. Invokes the event listener for the event that the controller receives a PORT_STATUS message.
     * @param sw The switch that sent the PORT_STATUS message
     * @param in The PORT_STATUS message
     */
    public void handlePortStatus(Switch sw, OFPortStatus in) {
        for (EventListener listener: eventListeners) {
            listener.handlePortStatus(sw, in);
        }
    }

    /**
     * For internal use. Invokes the vendor listeners when the controller receives a VENDOR message.
     * @param sw The switch that sent the VENDOR message
     * @param in The VENDOR message
     */
    public void handleVendorExtension(Switch sw, OFVendor in) {
        for (VendorListener listener: vendorListeners) {
            listener.handleVendorExtension(sw, in);
        }
    }

    /**
     * Registers the event listener to the controller.
     * @param listener The event listener to be registered
     */
    public void addEventListener(EventListener listener) {
        Preconditions.checkNotNull(listener);

        eventListeners.addIfAbsent(listener);
    }

    /**
     * Unregisters the event listener from the controller.
     * @param listener The event listener to be unregisteed
     */
    public void removeEventListener(EventListener listener) {
        Preconditions.checkNotNull(listener);

        eventListeners.remove(listener);
    }

    /**
     * Registers the vendor listener to the controller.
     * @param listener The vendor listener to be registered
     */
    public void addVendorListener(VendorListener listener) {
        Preconditions.checkNotNull(listener);

        vendorListeners.addIfAbsent(listener);
    }

    /**
     * Unregisters the vendor listener from the controller.
     * @param listener The vendor listener to be unregistered
     */
    public void removeVendorListener(VendorListener listener) {
        Preconditions.checkNotNull(listener);

        vendorListeners.remove(listener);
    }

    //  TODO: Do callback or Future have to be supported to notify when the reply is received?
    //  Some kinds of messages do not introduce the reply.
    public void send(Switch sw, OFMessage msg) {
        Preconditions.checkNotNull(sw);
        Preconditions.checkNotNull(msg);

        long datapathId = sw.dataPathId();
        //  TODO: have to write codes to send packets
        Switch lookup = handshakedSwitches.get(datapathId);
        if (lookup == null) {
            log.warn("Switch (DPID={}) is already disconnected from the controller", sw.dataPathId());
        } else {
            lookup.send(msg);
        }
    }
}
