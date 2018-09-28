package com.opc.client.model;

public class OpcEntity {
    private String timestamp;
    private FieldAndItem fieldAndItem;
    private Object value;

    public OpcEntity(String timestamp, FieldAndItem fieldAndItem, Object value) {
        this.timestamp = timestamp;
        this.fieldAndItem = fieldAndItem;
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public FieldAndItem getFieldAndItem() {
        return fieldAndItem;
    }

    public void setFieldAndItem(FieldAndItem fieldAndItem) {
        this.fieldAndItem = fieldAndItem;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
