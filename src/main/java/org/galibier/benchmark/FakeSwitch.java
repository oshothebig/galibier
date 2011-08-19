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
    private final int dataPathId;

    private final AtomicInteger nextTransactionId = new AtomicInteger(0);
    private final AtomicInteger receivedMessages = new AtomicInteger(0);
    private final AtomicInteger sentPacketOuts = new AtomicInteger(0);

    private final OFMessageFactory factory = new BasicFactory();

    private final ByteBuffer fakeFeatureReply;

    private final byte[] packetInPayload;

    public FakeSwitch(int dataPathId, int messageLength) {
        this.dataPathId = dataPathId;
        this.fakeFeatureReply = makeFeatureReplyData();
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
        buf.flip();

        return buf;
    }

    public OFMessage packetInData() {
        OFPacketIn packet = (OFPacketIn)factory.getMessage(OFType.PACKET_IN);
        packet.setBufferId(0);
        packet.setInPort((short)0);
        packet.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        packet.setPacketData(packetInPayload);
        packet.setXid(nextTransactionId.get());

        return packet;
    }

    public OFMessage featureReplyData(OFFeaturesRequest request) {
        OFFeaturesReply reply = (OFFeaturesReply)factory.parseMessages(fakeFeatureReply).get(0);
        reply.setDatapathId(dataPathId);
        reply.setXid(request.getXid());

        return reply;
    }

    public OFMessage echoReplyData(OFEchoRequest request) {
        OFEchoReply reply = (OFEchoReply)factory.getMessage(OFType.ECHO_REPLY);
        reply.setXid(request.getXid());

        return reply;
    }

    public OFMessage helloData() {
        return factory.getMessage(OFType.HELLO);
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

    public void sendingPacketInCompleted() {
        sentPacketOuts.incrementAndGet();
    }

    public OFMessage getConfigReplyData(OFGetConfigRequest request) {
        OFGetConfigReply reply = (OFGetConfigReply)factory.getMessage(OFType.GET_CONFIG_REPLY);
        reply.setXid(request.getXid());
        reply.setMissSendLength((short)0xFFFF);
        reply.setFlags((short)0);

        return reply;
    }

    public OFMessage vendorReplyData(OFVendor request) {
        OFError reply = (OFError)factory.getMessage(OFType.ERROR);
        reply.setXid(request.getXid());
        reply.setErrorType(OFError.OFErrorType.OFPET_BAD_REQUEST);
        reply.setErrorCode(OFError.OFBadActionCode.OFPBAC_BAD_VENDOR);

        return reply;
    }
}
