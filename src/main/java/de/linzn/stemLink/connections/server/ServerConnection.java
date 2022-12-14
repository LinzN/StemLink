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

package de.linzn.stemLink.connections.server;

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.connections.AbstractConnection;
import de.linzn.stemLink.connections.ClientType;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class ServerConnection extends AbstractConnection {
    private final StemLinkServer stemLinkServer;
    private boolean handshakeConfirmed;

    /**
     * Constructor for the ClientConnection class
     *
     * @param socket          active connected socket
     * @param stemLinkServer  StemLinkServer of the instance
     * @param stemLinkWrapper the iLinkMask mask class
     * @param cryptContainer  the CryptContainer for encryption in the client
     */
    ServerConnection(Socket socket, StemLinkServer stemLinkServer, IStemLinkWrapper stemLinkWrapper, CryptContainer cryptContainer) {
        super(socket, stemLinkWrapper, cryptContainer, new UUID(0, 0), ClientType.NONE, stemLinkServer.eventBus);
        this.stemLinkServer = stemLinkServer;
        stemLinkWrapper.log("Initializing stemLink to client " + socket.getRemoteSocketAddress(), Level.INFO);
    }


    @Override
    public void run() {
        try {
            this.write_handshake("STEP-1");

            while (!this.stemLinkServer.server.isClosed() && this.isValidConnection() && !this.handshakeConfirmed) {
                this.read_handshake();
            }

            if (this.handshakeConfirmed) {
                this.call_connect();
                this.stemLinkWrapper.runThread(this::requestKeepALiveHeartbeat);
                while (!this.stemLinkServer.server.isClosed() && this.isValidConnection()) {
                    this.readInput();
                }
            }
        } catch (IOException e2) {
            this.closeConnection();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            stemLinkWrapper.log("Encryption Error! Closing connection", Level.SEVERE);
            stemLinkWrapper.log(e, Level.SEVERE);
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
            if (this.handshakeConfirmed) {
                this.call_disconnect();
            }
            this.stemLinkServer.stemLinks.remove(this.uuid);
        }
    }

    @Override
    protected void read_handshake() throws IOException, IllegalBlockSizeException, BadPaddingException {
        BufferedInputStream bInStream = new BufferedInputStream(this.socket.getInputStream());
        DataInputStream dataInput = new DataInputStream(bInStream);
        String value = new String(this.cryptManager.decryptFinal(dataInput.readUTF().getBytes()));

        if (value.split("_")[0].equalsIgnoreCase("CLIENT-HANDSHAKE-2")) {
            this.handshakeConfirmed = false;
            UUID clientUUID = UUID.fromString(value.split("_")[1]);
            ClientType clientType = ClientType.valueOf(value.split("_")[2]);
            this.updateUUID(clientUUID);
            this.updateClientType(clientType);
            this.write_handshake("STEP-CONFIRM");
        }else if (value.split("_")[0].equalsIgnoreCase("CLIENT-HANDSHAKE-COMPLETE-CONFIRM")) {
            this.handshakeConfirmed = true;
            this.stemLinkServer.stemLinks.put(this.uuid, this);
            this.stemLinkWrapper.log("Server::Handshake complete", Level.FINE);
        } else if (value.split("_")[0].equalsIgnoreCase("CLIENT-HANDSHAKE-CANCEL")) {
            this.handshakeConfirmed = false;
            this.stemLinkWrapper.log("Server::Cancel handshake process", Level.WARNING);
            this.closeConnection();
        }
    }

    @Override
    protected void write_handshake(String step) {
        long randomValue = new Date().getTime();
        String value;

        if (step.equalsIgnoreCase("STEP-1")) {
            value = "SERVER-HANDSHAKE-1_" + randomValue;
            this.stemLinkWrapper.log("Server::Start handshake process", Level.FINE);

        }else if (step.equalsIgnoreCase("STEP-2")) {
            value = "SERVER-HANDSHAKE-2_" + randomValue;
            this.stemLinkWrapper.log("Server::Set new encryption level", Level.FINE);

        } else if (step.equalsIgnoreCase("STEP-CONFIRM")) {
            value = "SERVER-HANDSHAKE-COMPLETE_" + randomValue;
            this.stemLinkWrapper.log("Server::Finishing handshake process to client", Level.FINE);

        } else {
            this.handshakeConfirmed = false;
            value = "SERVER-HANDSHAKE-CANCEL_" + randomValue;
            this.stemLinkWrapper.log("Server::Cancel handshake process", Level.FINE);
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

}
