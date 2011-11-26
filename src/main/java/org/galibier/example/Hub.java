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

package org.galibier.example;

import org.galibier.core.Constants;
import org.galibier.core.Controller;
import org.galibier.core.SwitchEventListener;
import org.galibier.core.Switch;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;

import java.util.Collections;

public class Hub implements SwitchEventListener {
    private Controller controller;
    private final OFMessageFactory factory = new BasicFactory();

    public Hub() {
        this.controller = new Controller();
    }

    public void start(int port) {
        controller.addEventListener(this);
        controller.start(port);
    }

    public static void main(String[] args) {
        Hub hub = new Hub();
        hub.start(Constants.CONTROLLER_DEFAULT_PORT);
    }

    @Override
    public void switchConnected(Switch sw) {
        //  ignore
    }

    @Override
    public void switchDisconnected(Switch sw) {
        //  ignore
    }

    @Override
    public void handlePacketIn(Switch sw, OFPacketIn msg) {
        //  act as a repeater hub
        OFPacketOut out = (OFPacketOut)factory.getMessage(OFType.PACKET_OUT);
        out.setBufferId(msg.getBufferId());
        out.setInPort(msg.getInPort());

        //  all incoming packets are flooded
        OFActionOutput action = new OFActionOutput();
        action.setPort(OFPort.OFPP_FLOOD.getValue());
        out.setActions(Collections.singletonList((OFAction) action));
        out.setActionsLength((short)OFActionOutput.MINIMUM_LENGTH);

        //  packet data is set in the PACKET_OUT message when it is not buffered
        if (msg.getBufferId() == 0xffffffff) {
            byte[] packetData = msg.getPacketData();
            out.setLength((short)(OFPacketOut.MINIMUM_LENGTH + out.getActionsLength() + packetData.length));
            out.setPacketData(packetData);
        //  packet data does not have to be set in the PACKET_OUT message when it is buffered at the switch
        } else {
            out.setLength((short)(OFPacketOut.MINIMUM_LENGTH + out.getActionsLength()));
        }

        sw.send(out);
    }

    @Override
    public void handleFlowRemoved(Switch sw, OFFlowRemoved msg) {
        //  ignore
    }

    @Override
    public void handlePortStatus(Switch sw, OFPortStatus msg) {
        //  ignore
    }
}
