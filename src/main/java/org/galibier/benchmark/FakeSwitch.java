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

package org.galibier.benchmark;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeSwitch {
    private static final Logger log = LoggerFactory.getLogger(FakeSwitch.class);
    private static final int connectionTimeOut = 500;

    private final int dataPathId;

    private final AtomicInteger nextTransactionId = new AtomicInteger(0);
    private final AtomicInteger receivedMessages = new AtomicInteger(0);
    private final AtomicInteger sentPacketOuts = new AtomicInteger(0);
    private final AtomicBoolean readyToStart = new AtomicBoolean(false);

    private final OFMessageFactory factory = new BasicFactory();

    private Channel channel;

    private final ByteBuffer fakeFeatureReply;
    private final ByteBuffer fakePacketIn;

    private final byte[] packetInPayload;

    public FakeSwitch(int dataPathId, int messageLength) {
        this.dataPathId = dataPathId;
        this.fakeFeatureReply = makeFeatureReplyData();
        this.fakePacketIn = makePacketInData();
        this.packetInPayload = new byte[messageLength];
        Arrays.fill(packetInPayload, (byte)0x00);
    }

    private static byte[] toUnsignedByteArray(char[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (byte)(in[i] & 0xFF);
        }
        return out;
    }

    public void close() {
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }
    }

    private ByteBuffer makeFeatureReplyData() {
        final char[] fakeData = {
                0x97,0x06,0x00,0xe0,0x04,0x01,0x00,0x00,0x00,0x00,0x76,0xa9,
                0xd4,0x0d,0x25,0x48,0x00,0x00,0x01,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x1f,
                0x00,0x00,0x03,0xff,0x00,0x00,0x1a,0xc1,0x51,0xff,0xef,0x8a,0x76,0x65,0x74,0x68,
                0x31,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x01,0xce,0x2f,0xa2,0x87,0xf6,0x70,0x76,0x65,0x74,0x68,
                0x33,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x02,0xca,0x8a,0x1e,0xf3,0x77,0xef,0x76,0x65,0x74,0x68,
                0x35,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x03,0xfa,0xbc,0x77,0x8d,0x7e,0x0b,0x76,0x65,0x74,0x68,
                0x37,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00
        };

        final byte[] fakeDataBytes = toUnsignedByteArray(fakeData);

        ByteBuffer buf = ByteBuffer.allocate(fakeDataBytes.length);
        buf.put(fakeDataBytes);

        return buf;
    }

    private ByteBuffer makePacketInData() {
        final char[] fakeData = {
                0x97,0x0a,0x00,0x52,0x00,0x00,0x00,0x00,0x00,0x00,0x01,
                0x01,0x00,0x40,0x00,0x00,0x00,0x00,0x80,0x00,0x00,0x00,
                0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x02,0x08,0x00,0x45,
                0x00,0x00,0x32,0x00,0x00,0x00,0x00,0x40,0xff,0xf7,0x2c,
                0xc0,0xa8,0x00,0x28,0xc0,0xa8,0x01,0x28,0x7a,0x18,0x58,
                0x6b,0x11,0x08,0x97,0xf5,0x19,0xe2,0x65,0x7e,0x07,0xcc,
                0x31,0xc3,0x11,0xc7,0xc4,0x0c,0x8b,0x95,0x51,0x51,0x33,
                0x54,0x51,0xd5,0x00,0x36
        };

        final byte[] fakeDataBytes = toUnsignedByteArray(fakeData);

        ByteBuffer buf = ByteBuffer.allocate(fakeDataBytes.length);
        buf.put(fakeDataBytes);

        return buf;
    }

    public ChannelFuture sendPacketIn() {
        if (!channel.isWritable()) {
            return null;
        }

        OFPacketIn packet = (OFPacketIn)factory.getMessage(OFType.PACKET_IN);
        packet.setBufferId(0);
        packet.setInPort((short) 0);
        packet.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        packet.setPacketData(packetInPayload);
        packet.setXid(nextTransactionId.get());

        ChannelFuture future = channel.write(packet);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                sentPacketOuts.incrementAndGet();
            }
        });
        return future;
//        fakePacketIn.flip();
//        OFPacketIn out = (OFPacketIn)factory.parseMessages(fakePacketIn).get(0);
//        out.setXid(nextTransactionId.get());
//        out.setBufferId(0);
//
//        byte[] data = out.getPacketData();
//        //  Set source MAC address;
//        data[1] = ((byte)(nextTransactionId.get() >> 24 & 0xFF));
//        data[2] = ((byte)(nextTransactionId.get() >> 16 & 0xFF));
//        data[3] = ((byte)(nextTransactionId.get() >> 8  & 0xFF));
//        data[4] = ((byte)(nextTransactionId.get() & 0xFF));
//        data[5] = ((byte)(dataPathId & 0xFF));
//
//        //  Set destination MAC address;
//        data[11] = ((byte)(dataPathId & 0xFF));
//
//        out.setPacketData(data);
//
//        ChannelFuture future =  channel.write(out);
//        future.awaitUninterruptibly();
//        sentPacketOuts.incrementAndGet();
//        return future;
    }

    public ChannelFuture sendFeatureReply(OFFeaturesRequest in) {
        fakeFeatureReply.flip();
        OFFeaturesReply out = (OFFeaturesReply)factory.parseMessages(fakeFeatureReply).get(0);
        out.setDatapathId(dataPathId);
        out.setXid(in.getXid());

        log.info("Feature reply sent from {}", channel.getLocalAddress());

        ChannelFuture future = channel.write(out);
        future.awaitUninterruptibly();

        readyToStart.set(true);
        return future;
    }

    public ChannelFuture sendHello() {
        log.info("Hello sent from {}", channel.getLocalAddress());
        return channel.write(factory.getMessage(OFType.HELLO));
    }

    public ChannelFuture sendEchoReply(OFEchoRequest in) {
        OFEchoReply out = (OFEchoReply)factory.getMessage(OFType.ECHO_REPLY);
        out.setXid(in.getXid());
        log.info("Echo reply sent from {}", channel.getLocalAddress());
        return channel.write(out);
    }

    public void setChannel(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException();
        }

        this.channel = channel;
    }

    public boolean isReadyToStart() {
        return readyToStart.get();
    }

    public void receiveMessages() {
        nextTransactionId.incrementAndGet();
        receivedMessages.incrementAndGet();
    }

    public int getReceivedMessages() {
        return receivedMessages.get();
    }

    public int getSentPacketIns() {
        return sentPacketOuts.get();
    }
}
