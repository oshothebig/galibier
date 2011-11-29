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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class MACAddress {
    public static final int MAC_ADDRESS_LENGTH = 6;
    private final byte[] address = new byte[MAC_ADDRESS_LENGTH];

    //  preventing to create a instance
    private MACAddress() {}

    public static MACAddress valueOf(String address) {
        String[] elements = address.split(":");
        Preconditions.checkArgument(elements.length == MAC_ADDRESS_LENGTH);

        MACAddress instance = new MACAddress();
        for (int i = 0; i < MAC_ADDRESS_LENGTH; i++) {
            String element = elements[i];
            instance.address[i] = (byte)Integer.parseInt(element, 16);
        }

        return instance;
    }

    public static MACAddress valueOf(byte[] address) {
        Preconditions.checkArgument(address.length == MAC_ADDRESS_LENGTH);

        MACAddress instance = new MACAddress();
        for (int i = 0; i < MAC_ADDRESS_LENGTH; i++) {
            instance.address[i] = address[i];
        }

        return instance;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MACAddress) {
            MACAddress other = (MACAddress)o;
            return Arrays.equals(this.address, other.address);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.address);
    }

    @Override
    public String toString() {
        List<String> elements = Lists.newArrayList();
        for (byte b: address) {
            elements.add(String.format("%02X", b & 0xFF));
        }
        return Joiner.on(":").join(elements);
    }
}
