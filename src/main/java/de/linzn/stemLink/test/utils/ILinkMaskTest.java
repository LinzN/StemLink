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

package de.linzn.stemLink.test.utils;

import de.linzn.stemLink.components.ILinkMask;

import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ILinkMaskTest implements ILinkMask {

    @Override
    public void runThread(Runnable run) {
        Executors.newSingleThreadExecutor().submit(run);
    }

    @Override
    public void log(String logdata, Level loglevel) {
        System.out.println(loglevel.getName() + "::" + logdata);
    }
}
