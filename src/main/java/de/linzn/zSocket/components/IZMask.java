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

package de.linzn.zSocket.components;

public interface IZMask {
    /**
     * Template for SingleThreadExecutor
     *
     * @param run Runnable to execute
     */
    void runThread(Runnable run);

    /**
     * Get debugging mode
     *
     * @return boolean value if debugging is enabled
     */
    boolean isDebugging();

}
