/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.gateway.ethernet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.mycontroller.standalone.gateway.model.GatewayEthernet;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class EthernetGatewayListener implements Runnable {
    private Socket socket = null;
    private boolean terminate = false;
    private boolean terminated = false;
    private GatewayEthernet gateway = null;

    public EthernetGatewayListener(Socket socket, GatewayEthernet gateway) {
        this.socket = socket;
        this.gateway = gateway;
    }

    @Override
    public void run() {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
        while (!isTerminate()) {
            try {
                if (buf.ready()) {
                    String message = buf.readLine();
                    _logger.debug("Message Received: {}", message);
                    RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                            .gatewayId(gateway.getId())
                            .data(message)
                            .networkType(gateway.getNetworkType())
                            .build());
                }
                Thread.sleep(100);
            } catch (IOException | InterruptedException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        _logger.debug("EthernetGatewayListener Terminated...");
        this.terminated = true;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public synchronized void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
