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

package de.linzn.stemLink.connections.server;

import de.linzn.stemLink.components.ILinkMask;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.connections.AbstractConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;

public class ServerConnection extends AbstractConnection {
    private final StemLinkServer stemLinkServer;

    /**
     * Constructor for the ClientConnection class
     *
     * @param socket         active connected socket
     * @param stemLinkServer StemLinkServer of the instance
     * @param iLinkMask      the iLinkMask mask class
     * @param cryptContainer the CryptContainer for encryption in the client
     */
    ServerConnection(Socket socket, StemLinkServer stemLinkServer, ILinkMask iLinkMask, CryptContainer cryptContainer) {
        super(socket, iLinkMask, cryptContainer, UUID.randomUUID(), stemLinkServer.eventBus);
        this.stemLinkServer = stemLinkServer;
        iLinkMask.log("Initializing new server connection from " + socket.getRemoteSocketAddress(), Level.INFO);
    }


    @Override
    public void run() {
        this.triggerNewConnect();
        try {
            while (!this.stemLinkServer.server.isClosed() && this.isValidConnection()) {
                this.readInput();
            }
        } catch (IOException e2) {
            this.closeConnection();
        }
    }

    /**
     * Close this connection
     */
    @Override
    public synchronized void closeConnection() {
        if (!this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException ignored) {
            }
            this.triggerDisconnect();
            this.stemLinkServer.jServerConnections.remove(this.uuid);
        }
    }
}
