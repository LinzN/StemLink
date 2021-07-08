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

import de.linzn.stemLink.components.IStemLinkWrapper;

import java.util.concurrent.Executors;
import java.util.logging.Level;

public class StemLinkWrapperTest implements IStemLinkWrapper {

    @Override
    public void runThread(Runnable run) {
        Executors.newSingleThreadExecutor().submit(run);
    }

    @Override
    public void log(Object logdata, Level loglevel) {
        System.out.println(loglevel.getName() + "::" + logdata.toString());
    }
}