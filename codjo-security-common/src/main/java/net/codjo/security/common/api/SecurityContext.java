/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.io.Serializable;
/**
 * Represente un contexte de gestion de profile, pour un utilisateur particulier.
 */
public interface SecurityContext extends Serializable {
    public boolean isCallerInRole(String roleId);
}
