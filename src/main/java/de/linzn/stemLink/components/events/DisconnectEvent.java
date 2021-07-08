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

package de.linzn.stemLink.components.events;

import de.linzn.stemLink.connections.AbstractConnection;

import java.util.UUID;

public class DisconnectEvent implements IEvent {
    private final UUID uuid;
    private final AbstractConnection abstractConnection;

    /**
     * Constructor for disconnect event
     *
     * @param uuid Client uuid for event
     */
    public DisconnectEvent(UUID uuid, AbstractConnection abstractConnection) {
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
    @Override
    public AbstractConnection getConnection() {
        return this.abstractConnection;
    }
}
