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
import de.linzn.zSocket.components.events.IListener;
import de.linzn.zSocket.components.events.handler.EventBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZServer implements Runnable {
    ServerSocket server;
    Map<UUID, ServerConnection> jServerConnections;
    EventBus eventBus;
    private final IZMask zMask;
    private final String host;
    private final int port;
    private final CryptContainer cryptContainer;

    /**
     * Constructor for the ZServer class
     *
     * @param host           hostname to bind the server
     * @param port           port the bidn the server
     * @param zMask          the ZMask mask class
     * @param cryptContainer the CryptContainer for encryption in the client
     */
    public ZServer(String host, int port, IZMask zMask, CryptContainer cryptContainer) {
        this.host = host;
        this.port = port;
        this.zMask = zMask;
        this.jServerConnections = new HashMap<>();
        this.cryptContainer = cryptContainer;
        this.eventBus = new EventBus();
        if (zMask.isDebugging())
            zMask.log("[" + Thread.currentThread().getName() + "] " + "Initializing server on " + this.host + ":" + this.port);
    }

    /**
     * Enable this zServer instance
     */
    public void openServer() {
        try {
            this.server = new ServerSocket();
            this.server.bind(new InetSocketAddress(this.host, this.port));
            this.zMask.runThread(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable this zServer instance
     */
    public void closeServer() {
        try {
            this.server.close();
            ArrayList<UUID> uuidList = new ArrayList<>(this.jServerConnections.keySet());
            for (UUID uuid : uuidList) {
                this.jServerConnections.get(uuid).setDisable();
            }
            this.jServerConnections.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("zServer");
        do {
            try {
                Socket socket = this.server.accept();
                socket.setTcpNoDelay(true);
                ServerConnection serverConnection = new ServerConnection(socket, this, this.zMask, this.cryptContainer);
                serverConnection.setEnable();
                this.jServerConnections.put(serverConnection.getUUID(), serverConnection);
            } catch (IOException e) {
                if (zMask.isDebugging())
                    zMask.log("[" + Thread.currentThread().getName() + "] " + "Connection already closed!");
            }
        } while (!this.server.isClosed());
    }

    /**
     * Get client with a uuid
     *
     * @param uuid UUID of the wanted client
     * @return Returns the client if exist otherwise null
     */
    public ServerConnection getClient(UUID uuid) {
        return this.jServerConnections.get(uuid);
    }

    /**
     * Get all clientConnections
     *
     * @return Returns HashMap of all client connections
     */
    public Map<UUID, ServerConnection> getClients() {
        return this.jServerConnections;
    }

    /**
     * Register a new IListener
     *
     * @param iListener IListener to register
     */
    public void registerEvents(IListener iListener) {
        this.eventBus.register(iListener);
    }

    /**
     * Unregister an existing IListener
     *
     * @param iListener IListener to unregister
     */
    public void unregisterEvents(IListener iListener) {
        this.eventBus.unregister(iListener);
    }

}