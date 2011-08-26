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
import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Switch {
    private final static Logger log = LoggerFactory.getLogger(Switch.class);

    private OFFeaturesReply features;
    private final Date connectedSince;
    private final Channel channel;

    public Switch(Channel channel) {
        Preconditions.checkNotNull(channel);

        this.channel = channel;
        this.connectedSince = new Date();
    }

    public Date connectedSince() {
        return connectedSince;
    }

    public synchronized void setFeatures(OFFeaturesReply features) {
        this.features = features;
    }

    public synchronized long dataPathId() {
        return features.getDatapathId();
    }

    public boolean isHandshaked() {
        return features != null;
    }

    public void sendMessage(OFMessage out) {
        Preconditions.checkNotNull(out);

        channel.write(out);
    }

    public void stop() {
        //  stop scheduled tasks
        channel.getCloseFuture().awaitUninterruptibly();
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
}
