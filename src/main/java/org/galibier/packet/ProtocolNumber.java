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

/**
 * Protocol number assignment is listed in
 * http://www.iana.org/assignments/protocol-numbers/protocol-numbers.xml
 */
public class ProtocolNumber {
    public static final byte ICMP       = (byte)1;
    public static final byte IGMP       = (byte)2;
    public static final byte IPv4       = (byte)4;
    public static final byte TCP        = (byte)6;
    public static final byte UDP        = (byte)17;
    public static final byte IPv6       = (byte)41;
    public static final byte RSVP       = (byte)46;
    public static final byte GRE        = (byte)47;
    public static final byte OSPF       = (byte)89;
    public static final byte L2TP       = (byte)115;
    public static final byte STP        = (byte)118;
    public static final byte SCTP       = (byte)132;
}
