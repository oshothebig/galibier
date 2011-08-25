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

import org.galibier.core.Switch;
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;

public interface EventListener {
    /**
     * It is called when the switch is connected to the controller and the handshake is completed.
     * @param sw The switch connected to the controller.
     */
    public void switchConnected(Switch sw);

    /**
     * It is called when the switch is disconnected from the controller.
     * @param sw The switch disconnected from the controller.
     */
    public void switchDisconnected(Switch sw);

    /**
     * It is called when the controller receives a PACKET IN message from a switch.
     * @param sw The switch that send the PACKET IN message to the controller.
     * @param msg The PACKET IN message that the controller receives.
     */
    public void handlePacketIn(Switch sw, OFPacketIn msg);

    /**
     * It is called when the controller receives a FLOW REMOVED message from a switch.
     * @param sw The switch that send the FLOW REMOVED message to the controller.
     * @param msg The FLOW REMOVED message that the controller receives.
     */
    public void handleFlowRemoved(Switch sw, OFFlowRemoved msg);

    /**
     * It is called when the controller receives a PORT STATUS message from a switch.
     * @param sw The switch that send the PORT STATUS message to the controller.
     * @param msg THE PORT STATUS message that the controller receives.
     */
    public void handlePortStatus(Switch sw, OFPortStatus msg);
}
