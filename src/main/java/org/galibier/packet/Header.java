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

public interface Header {
    /**
     * Returns the length in bytes of this header.
     * @return the length in bytes of this header
     */
    int headerLength();

    /**
     * Returns the length of the entire packet starting at this header.
     * @return the length of the entire packet
     */
    int totalLength();

    /**
     * Returns the parent header encapsulating this header or null
     * if this is the first header in the chain.
     * @return
     */
    Header parentHeader();

    /**
     * Returns the child header encapsulated by this header or null
     * if this is the last header in the chain.
     * @return
     */
    Header childHeader();

    /**
     * Return the protocol identifier value of the header encapsulated by
     * this header or -1 if there is no such value.
     * @return
     */
    int encapsulatedProtocol();

    /**
     * Returns the byte array packed this header.
     * @return
     */
    byte[] pack();

    Header unpack(ByteBuffer data);
    Header unpack(byte[] data);

    void setParentHeader(Header parent);
    void setChildHeader(Header child);
}
