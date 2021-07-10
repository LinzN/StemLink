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

package de.linzn.stemLink.components.events;

import de.linzn.stemLink.connections.AbstractConnection;

import java.util.UUID;

public class ReceiveDataEvent implements IEvent {

    private final String channel;
    private final UUID clientUUID;
    private final byte[] dataInBytes;
    private final AbstractConnection abstractConnection;

    /**
     * Constructor for data receive event
     *
     * @param channel     Data channel
     * @param clientUUID  client uuid
     * @param dataInBytes raw data as byte array to send
     */
    public ReceiveDataEvent(String channel, UUID clientUUID, byte[] dataInBytes, AbstractConnection abstractConnection) {
        this.channel = channel;
        this.clientUUID = clientUUID;
        this.dataInBytes = dataInBytes;
        this.abstractConnection = abstractConnection;
    }

    /**
     * Get the channel of this event
     *
     * @return channel for this event
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Get the uuid uf the client
     *
     * @return uuid uf the client
     */
    public UUID getClientUUID() {
        return clientUUID;
    }

    /**
     * Get the raw received data as byte array of the client
     *
     * @return raw data as byte array
     */
    public byte[] getDataInBytes() {
        return dataInBytes;
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