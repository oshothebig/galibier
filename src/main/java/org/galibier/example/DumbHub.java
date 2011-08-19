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

import org.galibier.controller.core.Controller;
import org.galibier.controller.core.Switch;
import org.galibier.controller.event.MessageListener;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFMessageFactory;
import org.openflow.util.U16;

import java.util.ArrayList;
import java.util.List;

public class DumbHub implements MessageListener {
    private Controller controller;
    private OFMessageFactory factory;

    public Command receive(Switch client, OFMessage message) {
        OFPacketIn packetIn = (OFPacketIn) message;
        OFPacketOut out = (OFPacketOut) factory.getMessage(OFType.PACKET_OUT);
        out.setBufferId(packetIn.getBufferId());
        out.setInPort(packetIn.getInPort());

        // set actions
        OFActionOutput action = new OFActionOutput();
        action.setMaxLength((short) 0);
        action.setPort((short) OFPort.OFPP_FLOOD.getValue());
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(action);
        out.setActions(actions);
        out.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
        out.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + out.getActionsLength()));
        client.receive(out);

        return Command.CONTINUE;
    }

    public DumbHub() {
        controller = new Controller();
        factory = new BasicFactory();
        controller.addMessageListener(OFType.PACKET_IN, this);
    }

    public void start() {
        controller.bind();
    }

    public static void main(String[] args) {
        DumbHub hub = new DumbHub();
        hub.start();
    }
}
