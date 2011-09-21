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
public enum ProtocolNumber {
    HOPOPT          ((byte)0),
    ICMP            ((byte)1),
    IGMP            ((byte)2),
    GGP             ((byte)3),
    IPv4            ((byte)4),
    ST              ((byte)5),
    TCP             ((byte)6),
    CBT             ((byte)7),
    EGP             ((byte)8),
    IGP             ((byte)9),
    BBN_RCC_MON     ((byte)10),
    NVP_II          ((byte)11),
    PUP             ((byte)12),
    ARGUS           ((byte)13),
    EMCON           ((byte)14),
    XNET            ((byte)15),
    CHAOS           ((byte)16),
    UDP             ((byte)17),
    MUX             ((byte)18),
    DCN_MEAS        ((byte)19),
    HMP             ((byte)20),
    PRM             ((byte)21),
    XNS_IDP         ((byte)22),
    TRUNK_1         ((byte)23),
    TRUNK_2         ((byte)24),
    LEAF_1          ((byte)25),
    LEAF_2          ((byte)26),
    RDP             ((byte)27),
    IRTP            ((byte)28),
    ISO_TP4         ((byte)29),
    NETBLT          ((byte)30),
    MFE_NSP         ((byte)31),
    MERIT_INP       ((byte)32),
    DCCP            ((byte)33),
    THREE_PC        ((byte)34),
    IDPR            ((byte)35),
    XTP             ((byte)36),
    DDP             ((byte)37),
    IDPR_CMTP       ((byte)38),
    TP_PLUS_PLUS    ((byte)39),
    IL              ((byte)40),
    IPv6            ((byte)41),
    SDRP            ((byte)42),
    IPv6_ROUTE      ((byte)43),
    IPv6_FRAG       ((byte)44),
    IDRP            ((byte)45),
    RSVP            ((byte)46),
    GRE             ((byte)47),
    DSR             ((byte)48),
    BNA             ((byte)49),
    ESP             ((byte)50),
    AH              ((byte)51),
    I_NLSP          ((byte)52),
    SWIPE           ((byte)53),
    NARP            ((byte)54),
    MOBILE          ((byte)55),
    TLSP            ((byte)56),
    SKIP            ((byte)57),
    IPv6_ICMP       ((byte)58),
    IPv6_NONXT      ((byte)59),
    IPv6_OPTS       ((byte)60),
    // 61 is none
    CFTP            ((byte)62),
    // 63 is none
    SAT_EXPAK       ((byte)64),
    KRYPTOLAN       ((byte)65),
    RVD             ((byte)66),
    IPPC            ((byte)67),
    //  68 is none
    SAT_MON         ((byte)69),
    VISA            ((byte)70),
    IPCV            ((byte)71),
    CPNX            ((byte)72),
    CPHB            ((byte)73),
    WSN             ((byte)74),
    PVP             ((byte)75),
    BR_SAT_MON      ((byte)76),
    SUN_ND          ((byte)77),
    WB_MON          ((byte)78),
    WB_EXPAK        ((byte)79),
    ISO_IP          ((byte)80),
    VMTP            ((byte)81),
    SECURE_VMTP     ((byte)82),
    VINES           ((byte)83),
    TTP             ((byte)84),
    IPTM            ((byte)84),
    NSFNET_IGP      ((byte)85),
    DGP             ((byte)86),
    TCF             ((byte)87),
    EIGRP           ((byte)88),
    OSPFIGP         ((byte)89),
    SPRITE_RPC      ((byte)90),
    LARP            ((byte)91),
    MTP             ((byte)92),
    AX_25           ((byte)93),
    IPIP            ((byte)94),
    MICP            ((byte)95),
    SCC_SP          ((byte)96),
    ETHERIP         ((byte)97),
    ENCAP           ((byte)98),
    // 99 is none
    GMTP            ((byte)100),
    IFMP            ((byte)101),
    PNNI            ((byte)102),
    PIM             ((byte)103),
    ARIS            ((byte)104),
    SCPS            ((byte)105),
    QNX             ((byte)106),
    A_N             ((byte)107),
    IPCOMP          ((byte)108),
    SNP             ((byte)109),
    COMPAQ_PEER     ((byte)110),
    IPX_IN_IP       ((byte)111),
    VRRP            ((byte)112),
    PGM             ((byte)113),
    // 114 is none
    L2TP            ((byte)115),
    DDX             ((byte)116),
    IATP            ((byte)117),
    STP             ((byte)118),
    SRP             ((byte)119),
    UTI             ((byte)120),
    SMP             ((byte)121),
    SM              ((byte)122),
    PTP             ((byte)123),
    ISIS_OVER_IPv4  ((byte)124),
    FIRE            ((byte)125),
    CRTP            ((byte)126),
    CRUDP           ((byte)127),
    SSCOPMCE        ((byte)128),
    IPLT            ((byte)129),
    SPS             ((byte)130),
    PIPE            ((byte)131),
    SCTP            ((byte)132),
    FC              ((byte)133),
    RSVP_E2E_IGNORE ((byte)134),
    MOBILITY_HEADER ((byte)135),
    UDPLITE         ((byte)136),
    MPLS_IN_IP      ((byte)137),
    MANET           ((byte)138),
    HIP             ((byte)139),
    SHIM6           ((byte)140),
    WESP            ((byte)141),
    ROHC            ((byte)142);

    private byte value;

    private ProtocolNumber(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
