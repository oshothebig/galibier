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

package org.galibier.packet;

import com.google.common.base.Objects;

import java.nio.ByteBuffer;

public class Ethernet implements Header {
    private static final int MINIMUM_HEADER_LENGTH = 14;
    private static final int TAG_VLAN_HEADER_LENGTH = 18;

    private Header parentHeader;
    private Header childHeader;

    private MACAddress source;
    private MACAddress destination;
    private short tpid;
    private short tci;
    private short type;
    private boolean isTaggedVlan;

    private static final MACAddress nullAddress = MACAddress.valueOf(new byte[] {0, 0, 0, 0, 0, 0});

    public Ethernet() {}

    public Ethernet(Header childHeader) {
        this.childHeader = childHeader;
    }

    public MACAddress sourceAddress() {
        return source;
    }

    public MACAddress destinationAddress() {
        return destination;
    }

    public int tpid() {
        return tpid & 0xFFFF;
    }

    public int priority() {
        return (tci >> 13) & 0x7;
    }

    public int vlanId() {
        return tci & 0xFFF;
    }

    @Override
    public int headerLength() {
        if (isTaggedVlan) {
            return TAG_VLAN_HEADER_LENGTH;
        } else {
            return MINIMUM_HEADER_LENGTH;
        }
    }

    @Override
    public int totalLength() {
        if (childHeader != null) {
            return headerLength() + childHeader.totalLength();
        } else {
            return headerLength();
        }
    }

    @Override
    public Header parentHeader() {
        return null;
    }

    @Override
    public Header childHeader() {
        return childHeader;
    }

    private int minimumHeaderLength() {
        return MINIMUM_HEADER_LENGTH;
    }

    @Override
    public int encapsulatedProtocol() {
        return type & 0xFFFF;
    }

    @Override
    public void setParentHeader(Header parent) {
        parentHeader = parent;
    }

    @Override
    public void setChildHeader(Header child) {
        childHeader = child;
    }

    @Override
    public byte[] pack() {
        byte[] bytes;
        byte[] headerBytes = packHeader();

        if (childHeader != null) {
            byte[] childHeaderBytes = childHeader.pack();
            ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + childHeaderBytes.length);
            buffer.put(headerBytes);
            buffer.put(childHeaderBytes);
            bytes = buffer.array();
        } else {
            bytes = headerBytes;
        }

        return bytes;
    }

    @Override
    public Header unpack(byte[] data) {
        return unpack(ByteBuffer.wrap(data));
    }

    @Override
    public Header unpack(ByteBuffer data) {
        if (data.position() != 0) {
            throw new IllegalArgumentException("unpack() must be called with data.position() == 0");
        }

        if (data.remaining() < minimumHeaderLength()) {
            return null;
        }

        ByteBuffer headerBuffer = data;
        unpackHeader(headerBuffer);

        ByteBuffer payloadBuffer = headerBuffer.slice();
        if (headerBuffer.hasRemaining()) {
            childHeader = new Payload(payloadBuffer);
        }

        return this;
    }

    private byte[] packHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(headerLength());
        buffer.put(source.toBytes());
        buffer.put(destination.toBytes());
        if (isTaggedVlan) {
            buffer.putShort(tpid);
            buffer.putShort(tci);
        }
        buffer.putShort(type);

        return buffer.array();
    }

    private void unpackHeader(ByteBuffer header) {
        source = MACAddress.valueOf(unpackBytes(header, MACAddress.MAC_ADDRESS_LENGTH));
        destination = MACAddress.valueOf(unpackBytes(header, MACAddress.MAC_ADDRESS_LENGTH));

        short value = header.getShort();
        if (isTaggedVlan(value)) {
            isTaggedVlan = true;
            tpid = value;
            tci = header.getShort();
            type = header.getShort();
        } else {
            isTaggedVlan = false;
            type = value;
        }
    }

    private boolean isTaggedVlan(short value) {
        return (value & 0xFFFF) == EthernetType.TAG_VLAN;
    }

    private byte[] unpackBytes(ByteBuffer data, int length) {
        byte[] bytes = new byte[length];
        data.get(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("source", source)
                .add("destination", destination)
                .add("type", String.format("%x", type & 0xFFFF))
                .toString();

    }
}
