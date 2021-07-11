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

package de.linzn.stemLink.components.encryption;

import java.util.Arrays;
import java.util.Date;

public class DataHead {
    private final String header;
    private final int dataSize;
    private final long timeStamp;


    public DataHead(String header, int dataSize) {
        this.header = header;
        this.dataSize = dataSize;
        this.timeStamp = new Date().getTime();
    }

    private DataHead(String header, int dataSize, long timeStamp) {
        this.header = header;
        this.dataSize = dataSize;
        this.timeStamp = timeStamp;
    }

    public static DataHead fromString(String arrayString) {
        String[] headArray = arrayString.replace("[", "").replace("]", "").split(", ");
        String header = headArray[0];
        int dataSize = Integer.parseInt(headArray[1]);
        long timeStamp = Long.parseLong(headArray[2]);
        return new DataHead(header, dataSize, timeStamp);
    }

    @Override
    public String toString() {
        Object[] headArray = new Object[3];
        headArray[0] = this.header;
        headArray[1] = this.dataSize;
        headArray[2] = this.timeStamp;
        return Arrays.toString(headArray);
    }

    public int getDataSize() {
        return dataSize;
    }

    public String getHeader() {
        return header;
    }
}
