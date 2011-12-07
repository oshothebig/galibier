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

import java.nio.ByteBuffer;

public class Payload implements Header {
    private Header parentHeader;
    private ByteBuffer payloadBuffer;

    public Payload(ByteBuffer buffer) {
        this.payloadBuffer = buffer;
    }

    @Override
    public int headerLength() {
        return payloadBuffer.limit();
    }

    @Override
    public int totalLength() {
        return payloadBuffer.limit();
    }

    @Override
    public Header parentHeader() {
        return parentHeader;
    }

    @Override
    public Header childHeader() {
        return null;
    }

    @Override
    public int encapsulatedProtocol() {
        return -1;
    }

    @Override
    public byte[] pack() {
        payloadBuffer.rewind();
        return payloadBuffer.array();
    }

    @Override
    public Header unpack(ByteBuffer data) {
        payloadBuffer = data;
        return this;
    }

    @Override
    public Header unpack(byte[] data) {
        return unpack(ByteBuffer.wrap(data));
    }

    @Override
    public void setParentHeader(Header parent) {
        parentHeader = parent;
    }

    @Override
    public void setChildHeader(Header child) {
        throw new IllegalArgumentException("Cannot call setChildHeader() on PacketPayload");
    }
}
