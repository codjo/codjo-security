/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.io.Serializable;
import net.codjo.agent.UserId;
/**
 * Represente les droits d'un utilisateur.
 */
public interface User extends Serializable {
    public UserId getId();


    public boolean isAllowedTo(String function);


    public boolean isInRole(String roleId);
}
