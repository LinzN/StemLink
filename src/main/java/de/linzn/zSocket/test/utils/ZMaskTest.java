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

package de.linzn.zSocket.test.utils;

import de.linzn.zSocket.components.IZMask;

import java.util.concurrent.Executors;

public class ZMaskTest implements IZMask {

    @Override
    public void runThread(Runnable run) {
        Executors.newSingleThreadExecutor().submit(run);
    }

    @Override
    public boolean isDebugging() {
        return true;
    }
}
