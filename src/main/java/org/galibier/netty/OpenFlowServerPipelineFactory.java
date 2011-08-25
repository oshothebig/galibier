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

package org.galibier.netty;

import org.galibier.controller.core.Controller;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import java.util.concurrent.ScheduledExecutorService;

import static org.galibier.openflow.Constants.*;

public class OpenFlowServerPipelineFactory implements ChannelPipelineFactory {
    private final Controller controller;
    private final ScheduledExecutorService timer;

    public OpenFlowServerPipelineFactory(Controller controller, ScheduledExecutorService timer) {
        this.controller = controller;
        this.timer = timer;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        //  add the binary codec combination first
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(
                MAXIMUM_PACKET_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, -4, 0));
        pipeline.addLast("decoder", new OpenFlowDecoder());
        pipeline.addLast("encoder", new OpenFlowEncoder());

        //  add then the business logic
        pipeline.addLast("handler", new OpenFlowControllerHandler(controller, timer));

        return pipeline;
    }
}
