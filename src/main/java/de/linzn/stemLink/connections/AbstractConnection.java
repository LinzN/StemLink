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

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.encryption.CryptManager;
import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.IEvent;
import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventBus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;

public abstract class AbstractConnection implements Runnable {

    protected final CryptManager cryptManager;
    protected final IStemLinkWrapper stemLinkWrapper;
    protected Socket socket;
    protected UUID uuid;
    protected EventBus eventBus;
    protected ClientType clientType;

    /**
     * Constructor for the AbstractConnection class
     *
     * @param socket          active connection
     * @param stemLinkWrapper the iLinkMask mask class
     * @param cryptContainer  the CryptContainer for encryption in the client
     * @param uuid            the uuid for this client
     * @param eventBus        the eventBus for the connection
     */
    public AbstractConnection(Socket socket, IStemLinkWrapper stemLinkWrapper, CryptContainer cryptContainer, UUID uuid, ClientType clientType, EventBus eventBus) {
        this.socket = socket;
        this.stemLinkWrapper = stemLinkWrapper;
        this.cryptManager = new CryptManager(cryptContainer);
        this.uuid = uuid;
        this.clientType = clientType;
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
        this.stemLinkWrapper.runThread(this);
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
    private void call_data_event(String channel, byte[] bytes) {
        this.stemLinkWrapper.runThread(() -> {
            IEvent iEvent = new ReceiveDataEvent(channel, this.uuid, bytes, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Trigger a new connect event
     */
    protected void call_connect() {
        stemLinkWrapper.log("Connected to Socket", Level.FINE);
        this.stemLinkWrapper.runThread(() -> {
            IEvent iEvent = new ConnectEvent(this.uuid, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Trigger a disconnect event
     */
    protected void call_disconnect() {
        stemLinkWrapper.log("Disconnected from Socket", Level.FINE);
        this.stemLinkWrapper.runThread(() -> {
            IEvent iEvent = new DisconnectEvent(this.uuid, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Get the uuid uf this client
     *
     * @return uuid of the client
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Get ClientType of this client
     *
     * @return ClientType of the client
     */
    public ClientType getClientType() {
        return clientType;
    }

    /**
     * Update uuid of this connection
     * Only available in pre-handshake
     *
     * @param uuid new UUID to update
     */
    public void updateUUID(UUID uuid) {
        this.stemLinkWrapper.log("Update UUID of connection: " + uuid, Level.INFO);
        this.uuid = uuid;
    }

    /**
     * Update client type of the connection
     *
     * @param clientType new client type to update
     */
    public void updateClientType(ClientType clientType) {
        this.stemLinkWrapper.log("Update ClientType of connection: " + clientType, Level.INFO);
        this.clientType = clientType;
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
    protected boolean readInput() throws IOException, IllegalBlockSizeException, BadPaddingException {
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
            stemLinkWrapper.log("No channel in header", Level.SEVERE);
            return false;
        } else {
            this.call_data_event(headerChannel, fullData);
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

                BufferedOutputStream bOutStream = new BufferedOutputStream(this.socket.getOutputStream());
                DataOutputStream dataOut = new DataOutputStream(bOutStream);

                dataOut.writeUTF(new String(this.cryptManager.encryptFinal(headerChannel.getBytes())));
                dataOut.writeInt(fullData.length);

                for (byte aFullData : fullData) {
                    dataOut.writeByte(aFullData);
                }
                bOutStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stemLinkWrapper.log("The connection is closed. No output possible!", Level.SEVERE);
        }
    }

    /**
     * Read handshake data from server/client
     *
     * @throws IOException Exception if something failed
     */
    protected abstract void read_handshake() throws IOException, IllegalBlockSizeException, BadPaddingException;


    /**
     * Write handshake data for server/client
     *
     * @param step current handshake step defined in function itself
     */
    protected abstract void write_handshake(String step);

}
