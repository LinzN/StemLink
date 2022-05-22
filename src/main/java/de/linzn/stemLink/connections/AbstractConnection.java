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

package de.linzn.stemLink.connections;

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.encryption.CryptManager;
import de.linzn.stemLink.components.encryption.DataHead;
import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.IEvent;
import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventBus;
import de.linzn.stemLink.connections.client.ClientConnection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public abstract class AbstractConnection implements Runnable {

    protected final CryptManager cryptManager;
    protected final IStemLinkWrapper stemLinkWrapper;
    protected Socket socket;
    protected UUID uuid;
    protected EventBus eventBus;
    protected ClientType clientType;
    protected int so_timeout = 120 * 1000;

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
        try {
            this.socket.setSoTimeout(so_timeout);
        } catch (SocketException e) {
            e.printStackTrace();
            stemLinkWrapper.log(e, Level.SEVERE);
        }
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
        stemLinkWrapper.log("Stemlink is now connected to remote " + this.socket.getRemoteSocketAddress(), Level.FINE);
        this.stemLinkWrapper.runThread(() -> {
            IEvent iEvent = new ConnectEvent(this.uuid, this);
            this.eventBus.callEventHandler(iEvent);
        });
    }

    /**
     * Trigger a disconnect event
     */
    protected void call_disconnect() {
        stemLinkWrapper.log("Stemlink is disconnected from remote " + this.socket.getRemoteSocketAddress(), Level.FINE);
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

        DataHead dataHead = DataHead.fromString(new String(this.cryptManager.decryptFinal(dataInput.readUTF().getBytes())));
        String headerChannel = dataHead.getHeader();
        int dataSize = dataHead.getDataSize();

        byte[] encryptedDataPackage = new byte[dataSize];
        byte[] decryptedDataPackage;

        for (int i = 0; i < dataSize; i++) {
            encryptedDataPackage[i] = dataInput.readByte();
        }
        decryptedDataPackage = this.cryptManager.decryptFinal(encryptedDataPackage);

        /* Default input read*/
        if (headerChannel.isEmpty()) {
            stemLinkWrapper.log("No channel in header", Level.SEVERE);
            return false;
        } else if (headerChannel.equalsIgnoreCase("keep_alive_heartbeat")) {
            this.answerKeepALiveHeartbeat(decryptedDataPackage);
            return true;
        } else {
            this.call_data_event(headerChannel, decryptedDataPackage);
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
                byte[] encryptedDataPackage = this.cryptManager.encryptFinal(bytes);
                DataHead dataHead = new DataHead(headerChannel, encryptedDataPackage.length);

                BufferedOutputStream bOutStream = new BufferedOutputStream(this.socket.getOutputStream());
                DataOutputStream dataOut = new DataOutputStream(bOutStream);

                dataOut.writeUTF(new String(this.cryptManager.encryptFinal(dataHead.toString().getBytes())));

                for (byte dataByte : encryptedDataPackage) {
                    dataOut.writeByte(dataByte);
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

    protected void requestKeepALiveHeartbeat() {
        while (this.isValidConnection()) {
            stemLinkWrapper.log("Heartbeat request send to stemLink!", Level.FINE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                outputStream.writeLong(new Date().getTime());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.writeOutput("keep_alive_heartbeat", byteArrayOutputStream.toByteArray());
            try {
                Thread.sleep(1000 * 30);
            } catch (InterruptedException ignored) {
            }
        }
    }

    protected void answerKeepALiveHeartbeat(byte[] bytes) {
        stemLinkWrapper.log("Heartbeat received from stemLink!", Level.FINE);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
        Long dateTime = null;
        try {
            dateTime = dataInputStream.readLong();
            stemLinkWrapper.log("Data: " + dateTime, Level.FINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this instanceof ClientConnection) {
            stemLinkWrapper.log("Writing back to server!", Level.FINE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                outputStream.writeLong(dateTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.writeOutput("keep_alive_heartbeat", byteArrayOutputStream.toByteArray());
        }
    }

}
