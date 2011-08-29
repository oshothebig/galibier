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

import org.openflow.protocol.OFType;

import java.util.EnumSet;

public class Constants {
    public static final int MAXIMUM_PACKET_LENGTH = 65535;
    public static final int LENGTH_FIELD_OFFSET = 2;
    public static final int LENGTH_FIELD_LENGTH = 2;
    public static final int LENGTH_FIELD_MODIFICATION = -4;

    public static final int CONTROLLER_DEFAULT_PORT = 6633;

    public static final EnumSet<OFType> REQUEST_TYPE =
            EnumSet.of(
                    OFType.ECHO_REQUEST,
                    OFType.FEATURES_REQUEST,
                    OFType.GET_CONFIG_REQUEST,
                    OFType.STATS_REQUEST,
                    OFType.BARRIER_REQUEST
            );
    public static final EnumSet<OFType> REPLY_TYPE =
            EnumSet.of(
                    OFType.ECHO_REPLY, 
                    OFType.FEATURES_REPLY,
                    OFType.GET_CONFIG_REPLY,
                    OFType.STATS_REPLY,
                    OFType.BARRIER_REPLY
            );
}
