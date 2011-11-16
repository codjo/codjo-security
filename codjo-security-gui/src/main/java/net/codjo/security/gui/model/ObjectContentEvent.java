package net.codjo.security.gui.model;
import net.codjo.security.common.message.Role;
/**
 *
 */
public class ObjectContentEvent<T> {
    public enum TYPE {
        ROLE_ADDED,
        ROLE_REMOVED
    }
    private TYPE type;
    private T object;
    private Role role;


    public ObjectContentEvent(TYPE type, T object, Role role) {
        this.type = type;
        this.object = object;
        this.role = role;
    }


    public TYPE getType() {
        return type;
    }


    public T getObject() {
        return object;
    }


    public Role getRole() {
        return role;
    }


    @Override
    public String toString() {
        return "ObjectContentEvent(" + type + "," + object + "," + role + ")";
    }
}
