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

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.components.events.handler.EventBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StemLinkServer implements Runnable {
    private final IStemLinkWrapper stemLinkWrapper;
    private final String host;
    private final int port;
    private final CryptContainer cryptContainer;
    ServerSocket server;
    Map<UUID, ServerConnection> stemLinks;
    EventBus eventBus;

    /**
     * Constructor for the StemLinkServer class
     *
     * @param host            hostname to bind the server
     * @param port            port the bind the server
     * @param stemLinkWrapper the ILinkMask mask class
     * @param cryptContainer  the CryptContainer for encryption in the client
     */
    public StemLinkServer(String host, int port, IStemLinkWrapper stemLinkWrapper, CryptContainer cryptContainer) {
        this.host = host;
        this.port = port;
        this.stemLinkWrapper = stemLinkWrapper;
        this.stemLinks = new HashMap<>();
        this.cryptContainer = cryptContainer;
        this.eventBus = new EventBus(stemLinkWrapper);
        stemLinkWrapper.log("Initializing StemLinkServer on " + this.host + ":" + this.port, Level.INFO);
    }

    /**
     * Enable this StemLinkServer instance
     */
    public void openServer() {
        try {
            this.server = new ServerSocket();
            this.server.bind(new InetSocketAddress(this.host, this.port));
            this.stemLinkWrapper.runThread(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable this StemLinkServer instance
     */
    public void closeServer() {
        try {
            this.server.close();
            ArrayList<UUID> uuidList = new ArrayList<>(this.stemLinks.keySet());
            for (UUID uuid : uuidList) {
                this.stemLinks.get(uuid).setDisable();
            }
            this.stemLinks.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("StemLink");
        do {
            try {
                Socket socket = this.server.accept();
                socket.setTcpNoDelay(true);
                ServerConnection serverConnection = new ServerConnection(socket, this, this.stemLinkWrapper, this.cryptContainer);
                serverConnection.setEnable();
            } catch (IOException e) {
                stemLinkWrapper.log("Connection already closed!", Level.SEVERE);
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
        return this.stemLinks.get(uuid);
    }

    /**
     * Get all clientConnections
     *
     * @return Returns HashMap of all client connections
     */
    public Map<UUID, ServerConnection> getClients() {
        return this.stemLinks;
    }

    /**
     * Register a new Eventlistener
     *
     * @param classInstance Event listener instance to register
     */
    public void registerEvents(Object classInstance) {
        this.eventBus.register(classInstance);
    }

    /**
     * Unregister an existing Event Listener
     *
     * @param classInstance Event Listener to unregister
     */
    public void unregisterEvents(Object classInstance) {
        this.eventBus.unregister(classInstance);
    }

}