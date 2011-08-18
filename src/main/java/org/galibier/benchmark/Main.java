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

package org.galibier.benchmark;

import org.galibier.netty.OpenFlowDecoder;
import org.galibier.netty.OpenFlowEncoder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.galibier.openflow.Constants.LENGTH_FIELD_LENGTH;
import static org.galibier.openflow.Constants.LENGTH_FIELD_OFFSET;
import static org.galibier.openflow.Constants.MAXIMUM_PACKET_LENGTH;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Option(name = "-s", aliases = "--switch", usage = "Number of switches")
    private int switches = 16;

    @Option(name = "-p", aliases = "--port", usage = "Port number of the controller")
    private int port = 6633;

    @Option(name = "-l", aliases = "--loops", usage = "Number of loops")
    private int loops = 10;

    @Option(name = "-d", aliases = "--duration", usage = "Duration of a loop in milli sec")
    private int duration = 1000;

    @Option(name = "-m", aliases = "--message", usage = "Bytes of the payload of a packet in")
    private int messageLength = 128;

    @Argument(index = 0, metaVar = "host", required = true, usage = "Host name of the controller")
    private String host;

    private long previousReceivedMessages = 0;
    private long previousSentPacketIns = 0;
    private List<FakeSwitch> fakeSwitches;
    private List<ClientBootstrap> bootstraps;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ChannelFactory factory = new NioClientSocketChannelFactory(executor, executor);
    private final ChannelGroup channels = new DefaultChannelGroup("emulated-channels");

    public void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("java Main [options] host");
            parser.printUsage(System.err);
            System.exit(0);
        }

        fakeSwitches = new ArrayList<FakeSwitch>(switches);
        bootstraps = new ArrayList<ClientBootstrap>(switches);

        initialize();

        long connectionStartTime = System.nanoTime();

        List<ChannelFuture> futures = new ArrayList<ChannelFuture>(switches);
        for (ClientBootstrap bootstrap: bootstraps) {
            futures.add(bootstrap.connect(new InetSocketAddress(host, port)));
        }

        for (ChannelFuture f: futures) {
            f.awaitUninterruptibly();
            if (!f.isSuccess()) {
                f.getCause().printStackTrace();
                System.exit(0);
            }

            channels.add(f.getChannel());
        }
        long connectionEndTime = System.nanoTime();

        //  wait until all features reply is received
        boolean ready = waitForReady(1000);
        if (!ready) {
            System.err.println("Could not receive features replies");
            close();
            System.exit(1);
        }

        long benchmarkStartTime = System.nanoTime();
        for (Channel channel: channels) {
            OpenFlowBenchmarkHandler handler = (OpenFlowBenchmarkHandler)channel.getPipeline().getLast();
            handler.start(benchmarkStartTime, duration * loops);
        }

        for (int i = 0; i < loops; i++) {
            try {
                Thread.sleep(duration);
                outputReport();
            } catch (InterruptedException e) {
                //  ignore
            }
        }

        long benchmarkEndTime = System.nanoTime();

        close();
    }

    private void close() {
        ChannelGroupFuture closeFutures = channels.close();
        closeFutures.awaitUninterruptibly();
        for (ClientBootstrap bootstrap: bootstraps) {
            bootstrap.releaseExternalResources();
        }
    }

    private void initialize() {
        for (int i = 0; i < switches; i++) {
            final FakeSwitch fakeSwitch = new FakeSwitch(i, messageLength);
            fakeSwitches.add(fakeSwitch);

            ClientBootstrap bootstrap = new ClientBootstrap(factory);
            bootstraps.add(bootstrap);

            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);
            bootstrap.setOption("reuseAddress", true);
            bootstrap.setOption("connectTimeoutMillis", 1000);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(
                            MAXIMUM_PACKET_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, -4, 0));
                    pipeline.addLast("decoder", new OpenFlowDecoder());
                    pipeline.addLast("encoder", new OpenFlowEncoder());
                    pipeline.addLast("handler", new OpenFlowBenchmarkHandler(fakeSwitch));

                    return pipeline;
                }
            });
        }
    }

    private boolean waitForReady(int timeout) {
        long start = System.currentTimeMillis();
        outside: while (true) {
            long checked = System.currentTimeMillis();
            if (checked - start > timeout) {
                return false;
            }

            for (FakeSwitch sw: fakeSwitches) {
                if (!sw.isReadyToStart()) {
                    continue outside;
                }
            }
            return true;
        }
    }

    private long totalReceivedMessages() {
        long total = 0;
        for (FakeSwitch fakeSwitch: fakeSwitches) {
            total += fakeSwitch.getReceivedMessages();
        }

        return total;
    }

    private void outputReport() {
        long receivedMessages = totalReceivedMessages();
        long sentPacketIns = totalSentPacketIns();
        long receivedMessageDifference = receivedMessages - previousReceivedMessages;
        long sentPacketInDifference = sentPacketIns - previousSentPacketIns;
        previousReceivedMessages = receivedMessages;
        previousSentPacketIns = sentPacketIns;

        System.out.println(String.format("Received: %d, Sent: %d",
                receivedMessageDifference, sentPacketInDifference));
    }

    private long totalSentPacketIns() {
        long total = 0;
        for (FakeSwitch sw: fakeSwitches) {
            total += sw.getSentPacketIns();
        }

        return total;
    }

    public static void main(String[] args) {
        new Main().doMain(args);
    }
}