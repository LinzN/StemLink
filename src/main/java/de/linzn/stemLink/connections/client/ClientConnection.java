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

package de.linzn.stemLink.connections.client;

import de.linzn.stemLink.components.ILinkMask;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.events.handler.EventBus;
import de.linzn.stemLink.connections.AbstractConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;

public class ClientConnection extends AbstractConnection {
    private final String host;
    private final int port;
    private boolean keepAlive;

    /**
     * Constructor for the ClientConnection class
     *
     * @param host           the host address for server to connect
     * @param port           the port for the server to connect
     * @param iLinkMask      the ILinkMask mask class
     * @param cryptContainer the CryptContainer for encryption in the client
     */
    public ClientConnection(String host, int port, ILinkMask iLinkMask, CryptContainer cryptContainer) {
        super(new Socket(), iLinkMask, cryptContainer, new UUID(0L, 0L), new EventBus(iLinkMask));
        this.host = host;
        this.port = port;
        this.keepAlive = true;
        iLinkMask.log("Initializing new client connection to /" + host + ":" + port, Level.INFO);
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
                this.triggerNewConnect();

                while (this.isValidConnection()) {
                    this.readInput();
                }
            } catch (IOException e2) {
                this.closeConnection();
            }
            /* For reduce cpu usage in idl */
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
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
                this.triggerDisconnect();
            }
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
