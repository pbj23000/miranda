/*
 * Copyright 2017 Long Term Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ltsllc.miranda.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;

import javax.net.ssl.SSLSession;

/**
 * Created by Clark on 5/23/2017.
 */
public class NewMinaIncomingHandler extends IoHandlerAdapter {
    private NewMinaNetworkListener networkListener;

    public NewMinaNetworkListener getNetworkListener() {
        return networkListener;
    }

    public void setNetworkListener(NewMinaNetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    public NewMinaIncomingHandler (NewMinaNetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("got connection");
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        System.out.println ("got message");
        System.out.println(message);
        session.write(message);
    }
}
