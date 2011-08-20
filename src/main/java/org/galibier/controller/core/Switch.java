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

package org.galibier.controller.core;

import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Switch {
    private OFFeaturesReply features;
    private final Date connectedSince;
    private final Channel channel;
    private final Controller controller;
    private final AtomicInteger transactionId = new AtomicInteger(0);

    public Switch(Controller controller, Channel channel) {
        this.controller = controller;
        this.channel = channel;
        this.connectedSince = new Date();
    }

    public Date connectedSince() {
        return connectedSince;
    }

    public synchronized void setFeatures(OFFeaturesReply features) {
        this.features = features;
    }

    public long dataPathId() {
        return features.getDatapathId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Switch) {
            Switch other = (Switch) o;
            return this.dataPathId() == other.dataPathId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(dataPathId()).hashCode();
    }

    public void receive(OFMessage message) {
        channel.write(message);
    }
}
