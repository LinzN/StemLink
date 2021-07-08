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

package de.linzn.stemLink.components.events.handler;

import de.linzn.stemLink.components.StemLinkWrapper;
import de.linzn.stemLink.components.events.IEvent;
import de.linzn.stemLink.components.events.ReceiveDataEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EventBus {
    private final Map<Object, Map<Class<IEvent>, Method>> listenerSetMap;

    private final StemLinkWrapper stemLinkWrapper;

    public EventBus(StemLinkWrapper stemLinkWrapper) {
        this.listenerSetMap = new HashMap<>();
        this.stemLinkWrapper = stemLinkWrapper;
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return Map with event class and methods for this listener
     */
    private Map<Class<IEvent>, Method> findHandlers(Object listener) {
        Map<Class<IEvent>, Method> methods = new HashMap<>();

        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = m.getAnnotation(EventHandler.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    stemLinkWrapper.log("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument", Level.SEVERE);
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
     * @param event         IEvent to call
     * @param method        Method in listener
     * @param classInstance classInstance object which contains the method to call
     */
    private void callMethod(IEvent event, Method method, Object classInstance) {
        try {
            method.invoke(classInstance, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call all listener with the IEvent
     *
     * @param event IEvent to call in classInstance
     */
    public void callEventHandler(IEvent event) {
        for (Object classInstance : this.listenerSetMap.keySet()) {
            Map<Class<IEvent>, Method> handler = this.listenerSetMap.get(classInstance);
            for (Class<IEvent> iClass : handler.keySet()) {
                if (iClass.equals(event.getClass())) {
                    EventHandler annotation = handler.get(iClass).getAnnotation(EventHandler.class);
                    String annotationChannel = annotation.channel();
                    if (annotationChannel.isEmpty() || ((ReceiveDataEvent) event).getChannel().equalsIgnoreCase(annotationChannel)) {
                        callMethod(event, handler.get(iClass), classInstance);
                    }
                }
            }
        }
    }

    /**
     * Register a new Event listener classInstance
     *
     * @param classInstance Event listener classInstance to register
     */
    public void register(Object classInstance) {
        Map<Class<IEvent>, Method> handler = findHandlers(classInstance);
        this.listenerSetMap.put(classInstance, handler);
    }


    /**
     * Unregister a event listener classInstance
     *
     * @param classInstance Event listener classInstance to unregister
     */
    public void unregister(Object classInstance) {
        listenerSetMap.remove(classInstance);
    }
}
