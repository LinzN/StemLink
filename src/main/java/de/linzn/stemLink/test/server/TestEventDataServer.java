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

package de.linzn.stemLink.test.server;

import de.linzn.stemLink.components.events.IListener;
import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.linzn.stemLink.test.ServerTest;

import java.io.*;

public class TestEventDataServer implements IListener {

    @EventHandler(channel = "test_echo_1")
    public void onReceiveDataEvent(ReceiveDataEvent event) {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(event.getDataInBytes()));
        try {
            String hexCode = dataInputStream.readUTF();
            int intCode = dataInputStream.readInt();

            System.out.println("HexCode 1 pass: " + hexCode.equalsIgnoreCase(ServerTest.hexCode));
            System.out.println("IntCode 1 pass: " + (intCode == ServerTest.intCode));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(hexCode);
            dataOutputStream.writeInt(intCode);
            event.getConnection().writeOutput("test_echo_1", byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
