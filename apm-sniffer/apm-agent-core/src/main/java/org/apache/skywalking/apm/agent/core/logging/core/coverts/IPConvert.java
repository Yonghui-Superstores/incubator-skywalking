/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.core.logging.core.coverts;

import org.apache.skywalking.apm.agent.core.logging.core.Converter;
import org.apache.skywalking.apm.agent.core.logging.core.LogEvent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

public class IPConvert implements Converter {

    public static String IP_AND_ADDRESS;

    @Override
    public String convert(LogEvent logEvent) {
        return IP_AND_ADDRESS;
    }

    public static String normalizeHostAddress(InetAddress localHost) {
        return localHost.getHostAddress();
    }

    static {
        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList ipv4Result = new ArrayList();

            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
                Enumeration en = networkInterface.getInetAddresses();

                while (en.hasMoreElements()) {
                    InetAddress address = (InetAddress) en.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        ipv4Result.add(normalizeHostAddress(address));
                    }
                }
            }

            if (!ipv4Result.isEmpty()) {
                IP_AND_ADDRESS = (String) ipv4Result.get(0);
            } else {
                InetAddress localHost = InetAddress.getLocalHost();
                IP_AND_ADDRESS = normalizeHostAddress(localHost);
            }
        } catch (SocketException | UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
}
