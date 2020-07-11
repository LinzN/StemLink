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

package de.linzn.zSocket.connections.server;

import de.linzn.zSocket.components.IZMask;
import de.linzn.zSocket.components.encryption.CryptContainer;
import de.linzn.zSocket.connections.AbstractConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class ServerConnection extends AbstractConnection {
    private final ZServer zServer;

    /**
     * Constructor for the ClientConnection class
     *
     * @param socket         active connected socket
     * @param zServer        ZServer of the instance
     * @param zMask          the ZMask mask class
     * @param cryptContainer the CryptContainer for encryption in the client
     */
    ServerConnection(Socket socket, ZServer zServer, IZMask zMask, CryptContainer cryptContainer) {
        super(socket, zMask, cryptContainer, UUID.randomUUID(), zServer.eventBus);
        this.zServer = zServer;
        if (zMask.isDebugging())
            zMask.log("[" + Thread.currentThread().getName() + "] " + "Initializing new server connection from " + socket.getRemoteSocketAddress());
    }


    @Override
    public void run() {
        this.triggerNewConnect();
        try {
            while (!this.zServer.server.isClosed() && this.isValidConnection()) {
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
            this.zServer.jServerConnections.remove(this.uuid);
        }
    }
}
