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

package de.linzn.stemLink.test.server;

import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.IListener;
import de.linzn.stemLink.components.events.handler.EventHandler;

public class TestEventConnectionServer implements IListener {

    @EventHandler
    public void onConnectEvent(ConnectEvent event) {
        System.out.println("New client connected: " + event.getClientConnection().getUUID());
    }

    @EventHandler
    public void onDisconnectEvent(DisconnectEvent event) {
        System.out.println("Old client disconnected: " + event.getClientConnection().getUUID());
    }
}
