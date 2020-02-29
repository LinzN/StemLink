/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.zSocket.test.client1;

import de.linzn.zSocket.components.events.IListener;
import de.linzn.zSocket.components.events.ReceiveDataEvent;
import de.linzn.zSocket.components.events.handler.EventHandler;
import de.linzn.zSocket.test.ServerTest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TestEventDataClient implements IListener {

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
