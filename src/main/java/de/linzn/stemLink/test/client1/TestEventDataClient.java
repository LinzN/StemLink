/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.stemLink.test.client1;

import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.linzn.stemLink.test.ServerTest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TestEventDataClient {

    @EventHandler(channel = "test_echo_1")
    public void onReceiveDataEvent(ReceiveDataEvent event) {
        byte[] bytes = event.getDataInBytes();

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            String hexCode = dataInputStream.readUTF();
            int intCode = dataInputStream.readInt();
            System.out.println("Echo 1 HexCode pass: " + hexCode.equalsIgnoreCase(ServerTest.hexCode));
            System.out.println("Echo 1 IntCode pass: " + (intCode == ServerTest.intCode));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
