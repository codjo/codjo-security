package net.codjo.security.gui.model;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
/**
 *
 */
public class CycleException extends RuntimeException {
    public CycleException(Role father, RoleComposite son) {
        super("Cycle détecté."
              + " Le rôle '" + father.getName() + "'"
              + " est un rôle père de '" + son.getName() + "'");
    }
}
