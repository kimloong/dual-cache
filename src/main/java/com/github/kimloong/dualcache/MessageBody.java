package com.github.kimloong.dualcache;

/**
 * @author kimloong
 */
public class MessageBody {

    private String operation;

    private Object key;

    private Object value;

    public MessageBody() {
    }

    public MessageBody(String operation) {
        this(operation, null);
    }

    public MessageBody(String operation, Object key) {
        this(operation, key, null);
    }

    public MessageBody(String operation, Object key, Object value) {
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
