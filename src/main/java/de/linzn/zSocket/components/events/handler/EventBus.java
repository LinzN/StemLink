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

package de.linzn.zSocket.components.events.handler;

import de.linzn.zSocket.components.events.IEvent;
import de.linzn.zSocket.components.events.IListener;
import de.linzn.zSocket.components.events.ReceiveDataEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventBus {
    private Map<IListener, Map<Class<IEvent>, Method>> listenerSetMap;

    public EventBus() {
        this.listenerSetMap = new HashMap<>();
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return Map with event class and methods for this listener
     */
    private Map<Class<IEvent>, Method> findHandlers(IListener listener) {
        Map<Class<IEvent>, Method> methods = new HashMap<>();

        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = m.getAnnotation(EventHandler.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    System.out.println("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument");
                    continue;
                }
                Class<IEvent> iEvent = (Class<IEvent>) params[0];
                methods.put(iEvent, m);
            }
        }
        return methods;
    }


    /**
     * Call a listener method
     *
     * @param event     IEvent to call
     * @param method    Method in listener
     * @param iListener IListener object which contains the method to call
     */
    private void callMethod(IEvent event, Method method, IListener iListener) {
        try {
            method.invoke(iListener, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call all listener with the IEvent
     *
     * @param event IEvent to call in IListener
     */
    public void callEventHandler(IEvent event) {
        for (IListener iListener : this.listenerSetMap.keySet()) {
            Map<Class<IEvent>, Method> handler = this.listenerSetMap.get(iListener);
            for (Class<IEvent> iClass : handler.keySet()) {
                if (iClass.equals(event.getClass())) {
                    if (event instanceof ReceiveDataEvent) {
                        EventHandler annotation = handler.get(iClass).getAnnotation(EventHandler.class);
                        String annotationChannel = annotation.channel();
                        if (annotationChannel.isEmpty() || ((ReceiveDataEvent) event).getChannel().equalsIgnoreCase(annotationChannel)) {
                            callMethod(event, handler.get(iClass), iListener);
                        }
                    } else {
                        callMethod(event, handler.get(iClass), iListener);
                    }
                }
            }
        }
    }

    /**
     * Register a new IListener
     *
     * @param listener IListener to register
     */
    public void register(IListener listener) {
        Map<Class<IEvent>, Method> handler = findHandlers(listener);
        this.listenerSetMap.put(listener, handler);
    }


    /**
     * Unregister a IListener
     *
     * @param listener IListener to unregister
     */
    public void unregister(IListener listener) {
        listenerSetMap.remove(listener);
    }
}
