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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMACAddress {
    @Test
    public void getByAddress() {
        MACAddress sa1 = MACAddress.valueOf("00:01:02:03:04:05");
        MACAddress ba1 = MACAddress.valueOf(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05});
        assertEquals(sa1, ba1);
        assertEquals("00:01:02:03:04:05", sa1.toString());

        MACAddress sa2 = MACAddress.valueOf("FF:FE:FD:10:20:30");
        MACAddress ba2 = MACAddress.valueOf(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD, 0x10, 0x20, 0x30});
        assertEquals(sa2,ba2);
        assertEquals("FF:FE:FD:10:20:30", sa2.toString());
    }

    @Test(expected = NumberFormatException.class)
    public void illegalFormat() {
        MACAddress addr = MACAddress.valueOf("0T:00:01:02:03:04");
    }

    @Test(expected = IllegalArgumentException.class)
    public void longStringFields() {
        MACAddress addr = MACAddress.valueOf("00:01:02:03:04:05:06");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortStringFields() {
        MACAddress addr = MACAddress.valueOf("00:01:02:03:04");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAddressArgumentCount2() {
        MACAddress addr = MACAddress.valueOf(new byte[]{0x01, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
    }
}
