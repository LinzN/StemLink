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

package de.linzn.stemLink.test;

import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.connections.ClientType;
import de.linzn.stemLink.connections.client.ClientConnection;
import de.linzn.stemLink.connections.server.StemLinkServer;
import de.linzn.stemLink.test.utils.StemLinkWrapperTest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ServerTest {
    public static ServerTest serverTest;
    public static String hexCode = "4e8a6fe71aa98ee8e46e4c4e759432b6";
    public static int intCode = 40001;
    private final CryptContainer cryptContainer;
    public ClientConnection clientConnection1;
    public StemLinkServer stemLinkServer;

    public ServerTest() {
        this.cryptContainer = new CryptContainer("28482B4D6251655468566D597133743677397A24432646294A404E635266556A", new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        server();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client1();
    }

    public static void main(String[] args) {
        serverTest = new ServerTest();
    }

    private void client1() {
        UUID uuid = UUID.randomUUID();
        clientConnection1 = new ClientConnection("127.0.0.1", 9090, uuid, ClientType.DEFAULT, new StemLinkWrapperTest(), this.cryptContainer);
        clientConnection1.registerEvents(new de.linzn.stemLink.test.client1.TestEventDataClient());
        clientConnection1.registerEvents(new de.linzn.stemLink.test.client1.TestEventConnectionClient());
        clientConnection1.setEnable();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            outputStream.writeUTF(hexCode);
            outputStream.writeInt(intCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clientConnection1.writeOutput("test_echo_1", byteArrayOutputStream.toByteArray());
    }

    private void server() {
        stemLinkServer = new StemLinkServer("127.0.0.1", 9090, new StemLinkWrapperTest(), cryptContainer);
        stemLinkServer.registerEvents(new de.linzn.stemLink.test.server.TestEventDataServer());
        stemLinkServer.registerEvents(new de.linzn.stemLink.test.server.TestEventConnectionServer());
        stemLinkServer.openServer();
    }
}
