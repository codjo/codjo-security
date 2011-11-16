package net.codjo.security.gui.model;
/**
 *
 */
public class ObjectEvent<T> {
    public enum TYPE {
        ADDED,
        REMOVED
    }
    private ObjectEvent.TYPE type;
    private T object;


    public ObjectEvent(ObjectEvent.TYPE type, T object) {
        this.type = type;
        this.object = object;
    }


    public ObjectEvent.TYPE getType() {
        return type;
    }


    public T getObject() {
        return object;
    }


    @Override
    public String toString() {
        return "ObjectEvent(" + type + "," + object + ")";
    }
}
