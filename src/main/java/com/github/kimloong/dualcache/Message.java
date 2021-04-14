package com.github.kimloong.dualcache;

/**
 * @author kimloong
 */
public class Message {

    private String group;

    private byte operation;

    private Object key;

    private Object value;

    public Message() {
    }

    public Message(String group, byte operation) {
        this(group, operation, null);
    }

    public Message(String group, byte operation, Object key) {
        this(group,operation, key, null);
    }

    public Message(String group, byte operation, Object key, Object value) {
        this.group = group;
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    public byte getOperation() {
        return operation;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getGroup() {
        return group;
    }
}
