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

import org.openflow.protocol.OFMessage;

import java.util.Date;

public class Operation {
    private final OFMessage message;
    private final Date createdAt;
    private Date sentAt;

    public Operation(OFMessage msg) {
        this.message = msg;
        this.createdAt = new Date();
    }

    public OFMessage getMessage() {
        return message;
    }

    public Date sentAt() {
        return sentAt;
    }

    public Date createdAt() {
        return createdAt;
    }

    public void sentCompleted() {
        sentAt = new Date();
    }

    public int transactionId() {
        return message.getXid();
    }
}
