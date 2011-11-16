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

package org.galibier.util;

import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFeaturesReply.OFCapabilities;
import org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import org.openflow.protocol.OFPhysicalPort.OFPortFeatures;
import org.openflow.protocol.OFPhysicalPort.OFPortState;
import org.openflow.protocol.action.OFActionType;

import java.util.EnumSet;

public class EnumUtil {
    private EnumUtil() {}

    public static EnumSet<OFCapabilities> parseCapabilities(int bitCapabilities) {
        EnumSet<OFFeaturesReply.OFCapabilities> capabilities = EnumSet.noneOf(OFFeaturesReply.OFCapabilities.class);
        
        if ((bitCapabilities & OFCapabilities.OFPC_FLOW_STATS.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_FLOW_STATS);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_TABLE_STATS.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_TABLE_STATS);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_PORT_STATS.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_PORT_STATS);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_STP.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_STP);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_RESERVED.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_RESERVED);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_IP_REASM.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_IP_REASM);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_QUEUE_STATS.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_QUEUE_STATS);
        }
        if ((bitCapabilities & OFCapabilities.OFPC_ARP_MATCH_IP.getValue()) != 0) {
            capabilities.add(OFFeaturesReply.OFCapabilities.OFPC_ARP_MATCH_IP);
        }

        return capabilities;
    }

    public static int packCapabilities(EnumSet<OFCapabilities> capabilities) {
        int result = 0;
        for (OFCapabilities capability: capabilities) {
            result = result | capability.getValue();
        }
        return result;
    }

    public static EnumSet<OFActionType> parseActions(int bitActions) {
        EnumSet<OFActionType> actions = EnumSet.noneOf(OFActionType.class);
        
        if ((bitActions & (1 << OFActionType.OUTPUT.getTypeValue())) != 0) {
            actions.add(OFActionType.OUTPUT);
        }
        if ((bitActions & (1 << OFActionType.SET_VLAN_VID.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_VLAN_VID);
        }
        if ((bitActions & (1 << OFActionType.SET_VLAN_PCP.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_VLAN_PCP);
        }
        if ((bitActions & (1 << OFActionType.STRIP_VLAN.getTypeValue())) != 0) {
            actions.add(OFActionType.STRIP_VLAN);
        }
        if ((bitActions & (1 << OFActionType.SET_DL_SRC.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_DL_SRC);
        }
        if ((bitActions & (1 << OFActionType.SET_DL_DST.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_DL_DST);
        }
        if ((bitActions & (1 << OFActionType.SET_NW_SRC.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_NW_SRC);
        }
        if ((bitActions & (1 << OFActionType.SET_NW_DST.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_NW_DST);
        }
        if ((bitActions & (1 << OFActionType.SET_NW_TOS.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_NW_TOS);
        }
        if ((bitActions & (1 << OFActionType.SET_TP_SRC.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_TP_SRC);
        }
        if ((bitActions & (1 << OFActionType.SET_TP_DST.getTypeValue())) != 0) {
            actions.add(OFActionType.SET_TP_DST);
        }
        if ((bitActions & (1 << OFActionType.OPAQUE_ENQUEUE.getTypeValue())) != 0) {
            actions.add(OFActionType.OPAQUE_ENQUEUE);
        }

        return actions;
    }

    public static int packActions(EnumSet<OFActionType> actions) {
        int result = 0;
        for (OFActionType action: actions) {
            result = result | action.getTypeValue();
        }
        return result;
    }

    public static EnumSet<OFPortConfig> parsePortConfigs(int bitConfigs) {
        EnumSet<OFPortConfig> configs = EnumSet.noneOf(OFPortConfig.class);
        if ((bitConfigs & OFPortConfig.OFPPC_PORT_DOWN.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_PORT_DOWN);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_STP.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_STP);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_RECV.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_RECV);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_RECV_STP.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_RECV_STP);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_FLOOD.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_FLOOD);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_FWD.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_FWD);
        }
        if ((bitConfigs & OFPortConfig.OFPPC_NO_PACKET_IN.getValue()) != 0) {
            configs.add(OFPortConfig.OFPPC_NO_PACKET_IN);
        }
        return configs;
    }

    public static int packPortConfigs(EnumSet<OFPortConfig> configs) {
        int result = 0;
        for (OFPortConfig config: configs) {
            result = result | config.getValue();
        }
        return result;
    }

    public static EnumSet<OFPortState> parsePortStates(int bitStates) {
        EnumSet<OFPortState> states = EnumSet.noneOf(OFPortState.class);
        if ((bitStates & OFPortState.OFPPS_LINK_DOWN.getValue()) != 0) {
            states.add(OFPortState.OFPPS_LINK_DOWN);
        }
        if ((bitStates & OFPortState.OFPPS_STP_LISTEN.getValue()) != 0) {
            states.add(OFPortState.OFPPS_STP_LISTEN);
        }
        if ((bitStates & OFPortState.OFPPS_STP_LEARN.getValue()) != 0) {
            states.add(OFPortState.OFPPS_STP_LEARN);
        }
        if ((bitStates & OFPortState.OFPPS_STP_FORWARD.getValue()) != 0) {
            states.add(OFPortState.OFPPS_STP_FORWARD);
        }
        if ((bitStates & OFPortState.OFPPS_STP_BLOCK.getValue()) != 0) {
            states.add(OFPortState.OFPPS_STP_BLOCK);
        }
        if ((bitStates & OFPortState.OFPPS_STP_MASK.getValue()) != 0) {
            states.add(OFPortState.OFPPS_STP_MASK);
        }
        return states;
    }

    public static int packPortStates(EnumSet<OFPortState> states) {
        int result = 0;
        for (OFPortState state: states) {
            result = result | state.getValue();
        }
        return result;
    }

    public static EnumSet<OFPortFeatures> parsePortFeatures(int bitFeatures) {
        EnumSet<OFPortFeatures> features = EnumSet.noneOf(OFPortFeatures.class);
        if ((bitFeatures & OFPortFeatures.OFPPF_10MB_HD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_10MB_HD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_10MB_FD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_10MB_FD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_100MB_HD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_100MB_HD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_100MB_FD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_100MB_FD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_1GB_HD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_1GB_HD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_1GB_FD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_1GB_FD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_10GB_FD.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_10GB_FD);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_COPPER.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_COPPER);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_FIBER.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_FIBER);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_AUTONEG.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_AUTONEG);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_PAUSE.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_PAUSE);
        }
        if ((bitFeatures & OFPortFeatures.OFPPF_PAUSE_ASYM.getValue()) != 0) {
            features.add(OFPortFeatures.OFPPF_PAUSE_ASYM);
        }
        return features;
    }

    public static int packPortFeatures(EnumSet<OFPortFeatures> features) {
        int result = 0;
        for (OFPortFeatures feature: features) {
            result = result | feature.getValue();
        }
        return result;
    }
}
