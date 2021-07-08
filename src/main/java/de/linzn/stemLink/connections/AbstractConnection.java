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

package de.linzn.stemLink.connections;

import de.linzn.stemLink.components.ILinkMask;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.encryption.CryptManager;
import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.IEvent;
import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventBus;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public abstract class AbstractConnection implements Runnable {

    protected Socket socket;
    protected UUID uuid;
    protected EventBus eventBus;
    private final ILinkMask iLinkMask;
    private final CryptManager cryptManager;

    /**
     * Constructor for the AbstractConnection class
     *
     * @param socket         active connection
     * @param iLinkMask      the iLinkMask mask class
     * @param cryptContainer the CryptContainer for encryption in the client
     * @param uuid           the uuid for this client
     * @param eventBus       the eventBus for the connection
     */
    public AbstractConnection(Socket socket, ILinkMask iLinkMask, CryptContainer cryptContainer, UUID uuid, EventBus eventBus) {
        this.socket = socket;
        this.iLinkMask = iLinkMask;
        this.cryptManager = new CryptManager(cryptContainer);
        this.uuid = uuid;
        this.eventBus = eventBus;
    }

    public abstract void run();

    /**
     * Close this connection
     */
    public abstract void closeConnection();

    /**
     * Enable this connection
     */
    public synchronized void setEnable() {
        this.iLinkMask.runThread(this);
    }

    /**
     * Disable this connection
     */
    public synchronized void setDisable() {
        this.closeConnection();
    }

    /**
     * Trigger the eventBus for data input event
     *
     * @param channel channel of the receive event
     * @param bytes   raw data for the event
     */
    private void triggerDataInput(String channel, byte[] bytes) {
        //if (zMask.isDebugging())
        //    zMask.log("[" + Thread.currentThread().getName() + "] " + "IncomingData from Socket");
        this.iLinkMask.runThread(() -> {
            IEvent iEvent = new ReceiveDataEvent(channel, this.uuid, bytes, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Trigger a new connect event
     */
    protected void triggerNewConnect() {
        if (iLinkMask.isDebugging())
            iLinkMask.log("[" + Thread.currentThread().getName() + "] " + "Connected to Socket");
        this.iLinkMask.runThread(() -> {
            IEvent iEvent = new ConnectEvent(this.uuid, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Trigger a disconnect event
     */
    protected void triggerDisconnect() {
        if (iLinkMask.isDebugging())
            iLinkMask.log("[" + Thread.currentThread().getName() + "] " + "Disconnected from Socket");
        this.iLinkMask.runThread(() -> {
            IEvent iEvent = new DisconnectEvent(this.uuid, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Get the uuid uf this client
     *
     * @return the uuid uf thid client
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Check if the connection is valid and up
     *
     * @return boolean value if the connection is valid and still up
     */
    public boolean isValidConnection() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    /**
     * Read the input stream from the socket connection
     *
     * @return boolean value if read was successful
     * @throws IOException Fired a exception if something went wrong
     */
    protected boolean readInput() throws IOException {
        BufferedInputStream bInStream = new BufferedInputStream(this.socket.getInputStream());
        DataInputStream dataInput = new DataInputStream(bInStream);
        String headerChannel = new String(this.cryptManager.decryptFinal(dataInput.readUTF().getBytes()));
        int dataSize = dataInput.readInt();
        byte[] fullDataEncrypted = new byte[dataSize];
        byte[] fullData;

        for (int i = 0; i < dataSize; i++) {
            fullDataEncrypted[i] = dataInput.readByte();
        }
        fullData = this.cryptManager.decryptFinal(fullDataEncrypted);

        /* Default input read*/
        if (headerChannel.isEmpty()) {
            if (this.iLinkMask.isDebugging())
                iLinkMask.log("[" + Thread.currentThread().getName() + "] " + "No channel in header");
            return false;
        } else {
            //if (this.iLinkMask.isDebugging())
            //    iLinkMask.log("[" + Thread.currentThread().getName() + "] " + "Data amount: " + fullData.length);
            this.triggerDataInput(headerChannel, fullData);
            return true;
        }
    }

    /**
     * Write into the output stream
     *
     * @param headerChannel Channel header for this packet
     * @param bytes         Bytes to send to the connected side
     */
    public void writeOutput(String headerChannel, byte[] bytes) {
        if (this.isValidConnection()) {
            try {
                byte[] fullData = this.cryptManager.encryptFinal(bytes);

                BufferedOutputStream bOutSream = new BufferedOutputStream(this.socket.getOutputStream());
                DataOutputStream dataOut = new DataOutputStream(bOutSream);

                dataOut.writeUTF(new String(this.cryptManager.encryptFinal(headerChannel.getBytes())));
                dataOut.writeInt(fullData.length);

                for (byte aFullData : fullData) {
                    dataOut.writeByte(aFullData);
                }
                bOutSream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (iLinkMask.isDebugging())
                iLinkMask.log("[" + Thread.currentThread().getName() + "] " + "The connection is closed. No output possible!");
        }
    }

}
