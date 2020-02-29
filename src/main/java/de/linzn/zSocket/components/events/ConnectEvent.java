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

package de.linzn.zSocket.components.events;

import de.linzn.zSocket.connections.AbstractConnection;

import java.util.UUID;

public class ConnectEvent implements IEvent {
    private UUID uuid;
    private AbstractConnection abstractConnection;

    /**
     * Constructor for connect event
     *
     * @param uuid Client uuid for event
     */
    public ConnectEvent(UUID uuid, AbstractConnection abstractConnection) {
        this.uuid = uuid;
        this.abstractConnection = abstractConnection;
    }

    /**
     * Get the client uuid of this event
     *
     * @return UUID uf the connected client
     */
    public UUID getClientUUID() {
        return uuid;
    }

    /**
     * Get the client connection
     *
     * @return The client connection
     */
    public AbstractConnection getClientConnection() {
        return this.abstractConnection;
    }
}
