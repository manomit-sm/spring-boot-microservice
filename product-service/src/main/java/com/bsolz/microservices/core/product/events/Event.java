package com.bsolz.microservices.core.product.events;

import java.time.ZonedDateTime;

public class Event<K, T> {
    public enum Type {CREATE, DELETE}
    private Event.Type eventType;
    private K key;
    private T data;

    private ZonedDateTime eventCreatedAt;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public Event(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = ZonedDateTime.now();
    }

    public Type getEventType() {
        return eventType;
    }
    public K getKey() {
        return key;
    }
    public T getData() {
        return data;
    }
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}
