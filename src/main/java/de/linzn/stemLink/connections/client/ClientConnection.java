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

package de.linzn.stemLink.connections.client;

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.events.handler.EventBus;
import de.linzn.stemLink.connections.AbstractConnection;
import de.linzn.stemLink.connections.ClientType;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class ClientConnection extends AbstractConnection {
    private final String host;
    private final int port;
    private boolean keepAlive;
    private boolean handshakeConfirmed;

    /**
     * Constructor for the ClientConnection class
     *
     * @param host            the host address for server to connect
     * @param port            the port for the server to connect
     * @param stemLinkWrapper the ILinkMask mask class
     * @param cryptContainer  the CryptContainer for encryption in the client
     */
    public ClientConnection(String host, int port, UUID clientUUID, ClientType clientType, IStemLinkWrapper stemLinkWrapper, CryptContainer cryptContainer) {
        super(new Socket(), stemLinkWrapper, cryptContainer, clientUUID, clientType, new EventBus(stemLinkWrapper));
        this.host = host;
        this.port = port;
        this.keepAlive = true;
        this.handshakeConfirmed = false;
        stemLinkWrapper.log("Initializing stemLink to server /" + host + ":" + port, Level.INFO);

    }

    /**
     * Enable this connection
     */
    @Override
    public synchronized void setEnable() {
        this.keepAlive = true;
        super.setEnable();
    }

    /**
     * Disable this connection
     */
    @Override
    public synchronized void setDisable() {
        this.keepAlive = false;
        super.setDisable();
    }

    @Override
    public void run() {
        while (this.keepAlive) {
            try {
                this.socket = new Socket(this.host, this.port);
                this.socket.setTcpNoDelay(true);
                this.handshakeConfirmed = false;

                while (this.isValidConnection() && !this.handshakeConfirmed) {
                    this.read_handshake();
                }

                if (this.handshakeConfirmed) {
                    this.call_connect();

                    while (this.isValidConnection()) {
                        this.readInput();
                    }
                }
            } catch (IOException e2) {
                this.closeConnection();
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                stemLinkWrapper.log("Encryption Error! Closing connection", Level.SEVERE);
                stemLinkWrapper.log(e, Level.SEVERE);
                this.closeConnection();
            }
            /* Reduce cpu usage while trying to reconnect to stemLink server */
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                this.stemLinkWrapper.log(exception, Level.SEVERE);
            }
        }
    }

    /**
     * Check if the stemLink is online
     *
     * @return boolean value if stemLink is online
     */
    public boolean isOnline() {
        return this.keepAlive && this.isValidConnection();
    }

    /**
     * Close this connection
     */
    @Override
    public synchronized void closeConnection() {
        if (!this.socket.isClosed() && this.socket.getRemoteSocketAddress() != null) {
            try {
                this.socket.close();
            } catch (IOException ignored) {
            }
            if (this.keepAlive) {
                if (this.handshakeConfirmed) {
                    this.call_disconnect();
                }
            }
        }
    }

    @Override
    public void read_handshake() throws IOException, IllegalBlockSizeException, BadPaddingException {
        BufferedInputStream bInStream = new BufferedInputStream(this.socket.getInputStream());
        DataInputStream dataInput = new DataInputStream(bInStream);
        String value = new String(this.cryptManager.decryptFinal(dataInput.readUTF().getBytes()));

        if (value.split("_")[0].equalsIgnoreCase("SERVER-HANDSHAKE-1")) {
            this.handshakeConfirmed = false;
            write_handshake("STEP-2");
        }else if (value.split("_")[0].equalsIgnoreCase("SERVER-HANDSHAKE-2")) {
            this.handshakeConfirmed = false;
            write_handshake("STEP-3");
        }  else if (value.split("_")[0].equalsIgnoreCase("SERVER-HANDSHAKE-COMPLETE")) {
            this.handshakeConfirmed = true;
            write_handshake("STEP-CONFIRM");

        } else if (value.split("_")[0].equalsIgnoreCase("SERVER-HANDSHAKE-CANCEL")) {
            this.handshakeConfirmed = false;
            this.stemLinkWrapper.log("Client::Cancel handshake process", Level.WARNING);
            this.closeConnection();
        }
    }

    @Override
    protected void write_handshake(String step) {
        long randomValue = new Date().getTime();
        String value;

        if (step.equalsIgnoreCase("STEP-2")) {
            value = "CLIENT-HANDSHAKE-2_" + this.getUUID() + "_" + this.getClientType().name() + "_" + randomValue;
            this.stemLinkWrapper.log("Client::Start handshake process", Level.FINE);
            this.stemLinkWrapper.log("Client::" + this.getUUID(), Level.FINE);
            this.stemLinkWrapper.log("Client::Send UUID for handshake", Level.FINE);
            this.stemLinkWrapper.log("Client::" + this.getClientType().name(), Level.FINE);
            this.stemLinkWrapper.log("Client::Send ClientType for handshake", Level.FINE);

        } else if (step.equalsIgnoreCase("STEP-CONFIRM")) {
            value = "CLIENT-HANDSHAKE-COMPLETE-CONFIRM_" + randomValue;
            this.stemLinkWrapper.log("Client::Confirming handshake process to server", Level.FINE);

        } else {
            this.handshakeConfirmed = false;
            value = "CLIENT-HANDSHAKE-CANCEL_" + randomValue;
            this.stemLinkWrapper.log("Client::Cancel handshake process", Level.FINE);
        }

        if (this.isValidConnection()) {
            try {
                BufferedOutputStream bOutStream = new BufferedOutputStream(this.socket.getOutputStream());
                DataOutputStream dataOut = new DataOutputStream(bOutStream);

                dataOut.writeUTF(new String(this.cryptManager.encryptFinal(value.getBytes())));

                bOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stemLinkWrapper.log("Handshake failed on STEP: " + step, Level.SEVERE);
        }
    }


    /**
     * Register a new classInstance
     *
     * @param classInstance Event listener classInstance to register
     */
    public void registerEvents(Object classInstance) {
        this.eventBus.register(classInstance);
    }

    /**
     * Unregister an existing IListener
     *
     * @param classInstance Event listener classInstance to unregister
     */
    public void unregisterEvents(Object classInstance) {
        this.eventBus.unregister(classInstance);
    }
}
