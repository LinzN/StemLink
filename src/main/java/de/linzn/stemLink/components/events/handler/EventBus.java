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

package de.linzn.stemLink.components.events.handler;

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stemLink.components.events.IEvent;
import de.linzn.stemLink.components.events.ReceiveDataEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EventBus {
    private final Map<Object, Map<Method, Class<IEvent>>> listenerSetMap;

    private final IStemLinkWrapper stemLinkWrapper;

    public EventBus(IStemLinkWrapper stemLinkWrapper) {
        this.listenerSetMap = new HashMap<>();
        this.stemLinkWrapper = stemLinkWrapper;
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return Map with event class and methods for this listener
     */
    private Map<Method, Class<IEvent>> findHandlers(Object listener) {
        Map<Method, Class<IEvent>> methods = new HashMap<>();

        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = m.getAnnotation(EventHandler.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    stemLinkWrapper.log("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument", Level.SEVERE);
                    continue;
                }
                Class<IEvent> iEvent = (Class<IEvent>) params[0];
                methods.put(m, iEvent);
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
            Map<Method, Class<IEvent>> handler = this.listenerSetMap.get(classInstance);
            for (Method method : handler.keySet()) {
                Class<IEvent> iClass = handler.get(method);
                if (iClass.equals(event.getClass())) {
                    EventHandler annotation = method.getAnnotation(EventHandler.class);
                    String annotationChannel = annotation.channel();
                    if (annotationChannel.isEmpty() || ((ReceiveDataEvent) event).getChannel().equalsIgnoreCase(annotationChannel)) {
                        callMethod(event, method, classInstance);
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
        Map<Method, Class<IEvent>> handler = findHandlers(classInstance);
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
