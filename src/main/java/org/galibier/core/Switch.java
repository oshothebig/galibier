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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.galibier.util.EnumUtil;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.action.OFActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.openflow.protocol.OFFeaturesReply.OFCapabilities;
import static org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import static org.openflow.protocol.OFPhysicalPort.OFPortState;

public class Switch {
    private final static Logger log = LoggerFactory.getLogger(Switch.class);

    private volatile OFFeaturesReply features;
    private final ConcurrentMap<Short, OFPhysicalPort> ports =
            new ConcurrentHashMap<Short, OFPhysicalPort>();
    private final Date connectedSince;
    private final MessageDispatcher dispatcher;

    public Switch(MessageDispatcher dispatcher) {
        Preconditions.checkNotNull(dispatcher);

        this.dispatcher = dispatcher;
        this.connectedSince = new Date();
    }

    public Date connectedSince() {
        return connectedSince;
    }

    public synchronized void setFeatures(OFFeaturesReply features) {
        this.features = features;

        for (OFPhysicalPort port: features.getPorts()) {
            ports.put(port.getPortNumber(), port);
        }
    }

    public long dataPathId() {
        Preconditions.checkState(isHandshaken(), "Handshaking is not completed");

        return features.getDatapathId();
    }

    public int bufferCount() {
        Preconditions.checkState(isHandshaken(), "Handshaking is not completed");

        return features.getBuffers();
    }

    public byte tableCount() {
        Preconditions.checkState(isHandshaken(), "Handshaking is not completed");

        return features.getTables();
    }

    public EnumSet<OFCapabilities> capabilities() {
        Preconditions.checkState(isHandshaken(), "Handshaking is not completed");

        int bitCapabilities = features.getCapabilities();
        return EnumUtil.parseCapabilities(bitCapabilities);
    }

    public EnumSet<OFActionType> supportedActions() {
        Preconditions.checkState(isHandshaken());

        int bitActions = features.getActions();
        return EnumUtil.parseActions(bitActions);
    }

    public int portCount() {
        return ports.keySet().size();
    }

    public int enabledPortCount() {
        return getEnabledPorts().size();
    }

    public List<OFPhysicalPort> getEnabledPorts() {
        List<OFPhysicalPort> result = new ArrayList<OFPhysicalPort>();
        for (OFPhysicalPort port: ports.values()) {
            if (portEnabled(port)) {
                result.add(port);
            }
        }
        return result;
    }

    private boolean portEnabled(short portNumber) {
        return portEnabled(ports.get(portNumber));
    }

    private boolean portEnabled(OFPhysicalPort port) {
        if (port == null) {
            return false;
        }
        if ((port.getConfig() & OFPortConfig.OFPPC_PORT_DOWN.getValue()) > 0) {
            return false;
        }
        if ((port.getState() & OFPortState.OFPPS_LINK_DOWN.getValue()) > 0) {
            return false;
        }
        if ((port.getState() & OFPortState.OFPPS_STP_MASK.getValue()) == OFPortState.OFPPS_STP_BLOCK.getValue()) {
            return false;
        }
        return true;
    }

    public boolean isHandshaken() {
        return features != null;
    }

    public OFMessageFuture send(OFMessage out) {
        Preconditions.checkNotNull(out);

        if (features == null) {
            log.warn("FEATURE_REPLY is not received, but a message will be sent");
        }
        return dispatcher.send(out);
    }

    public void stop() {
        dispatcher.stop();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Switch) {
            Switch other = (Switch)o;
            return Objects.equal(this.dataPathId(), other.dataPathId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataPathId());
    }

    @Override
    public String toString() {
        String dpid = "NOT_HANDSHAKED";
        if (features != null) {
            dpid = String.format("%016x", dataPathId());
        }
        return Objects.toStringHelper(this)
                .add("address", dispatcher.remoteAddress())
                .add("dpid", dpid)
                .toString();
    }
}
