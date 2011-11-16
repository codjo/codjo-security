/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.service.ldap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
/**
 * Mock d'un itinialContext.<p>Pour utiliser il suffit de faire :
 * <pre> Properties prop = new Properties();
 * prop.put(Context.INITIAL_CONTEXT_FACTORY, "net.codjo.mock.naming.InitialContextMock");
 * prop.put(Context.PROVIDER_URL,
 * getUrl()); </pre></p>
 * <p>Dans les tests il suffit de faire ceci pour remplir l'environement :</p>
 *
 * <pre>
 * System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
 * "net.codjo.test.mock.naming.InitialContextMock");
 * new InitialContext().addToEnvironment("ejb/param/Bobo", "bobo");
 * new InitialContext().addToEnvironment("ejb/param/Bobi", "bobi");
 * </pre>
 *
 * @author $Author: crego $
 * @version $Revision: 1.10 $
 */
@SuppressWarnings({"unchecked", "StaticNonFinalField"})
public class InitialContextMock implements InitialContextFactory, Context {
    private static Map mockEnvs = Collections.synchronizedMap(new HashMap());


    public InitialContextMock() {
    }


    public Hashtable getEnvironment() throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode getEnvironment() n'est pas encore implémentée.");
    }


    public Context getInitialContext(Hashtable<?, ?> environment)
          throws NamingException {
        mockEnvs.putAll(environment);
        return this;
    }


    public String getNameInNamespace() throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode getNameInNamespace() n'est pas encore implémentée.");
    }


    public NameParser getNameParser(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode getNameParser() n'est pas encore implémentée.");
    }


    public NameParser getNameParser(String name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode getNameParser() n'est pas encore implémentée.");
    }


    public Object addToEnvironment(String propName, Object propVal)
          throws NamingException {
        return put(propName, propVal);
    }


    public void bind(Name name, Object obj) throws NamingException {
        throw new java.lang.UnsupportedOperationException("La méthode bind() n'est pas encore implémentée.");
    }


    public void bind(String name, Object obj) throws NamingException {
        throw new java.lang.UnsupportedOperationException("La méthode bind() n'est pas encore implémentée.");
    }


    public void close() throws NamingException {
        mockEnvs.clear();
    }


    public Name composeName(Name name, Name prefix)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode composeName() n'est pas encore implémentée.");
    }


    public String composeName(String name, String prefix)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode composeName() n'est pas encore implémentée.");
    }


    public Context createSubcontext(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode createSubcontext() n'est pas encore implémentée.");
    }


    public Context createSubcontext(String name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode createSubcontext() n'est pas encore implémentée.");
    }


    public void destroySubcontext(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode destroySubcontext() n'est pas encore implémentée.");
    }


    public void destroySubcontext(String name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode destroySubcontext() n'est pas encore implémentée.");
    }


    public NamingEnumeration list(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException("La méthode list() n'est pas encore implémentée.");
    }


    public NamingEnumeration<NameClassPair> list(String name)
          throws NamingException {
//        MockNamingEnumeration<NameClassPair> enumeration = new MockNamingEnumeration<NameClassPair>(mockEnvs, name);
//        return enumeration;
        throw new java.lang.UnsupportedOperationException("La méthode list() n'est pas encore implémentée.");
    }


    public NamingEnumeration<Binding> listBindings(Name name)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode listBindings() n'est pas encore implémentée.");
    }


    public NamingEnumeration<Binding> listBindings(String name)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode listBindings() n'est pas encore implémentée.");
    }


    public Object lookup(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode lookup() n'est pas encore implémentée.");
    }


    public Object lookup(String name) throws NamingException {
        Object obj = mockEnvs.get(name);
        if (obj instanceof NamingException) {
            throw (NamingException)obj;
        }

        return obj;
    }


    public Object lookupLink(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode lookupLink() n'est pas encore implémentée.");
    }


    public Object lookupLink(String name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode lookupLink() n'est pas encore implémentée.");
    }


    public Object put(String name, Object val) {
        return mockEnvs.put(name, val);
    }


    public void rebind(Name name, Object obj) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode rebind() n'est pas encore implémentée.");
    }


    public void rebind(String name, Object obj) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode rebind() n'est pas encore implémentée.");
    }


    public Object removeFromEnvironment(String propName)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode removeFromEnvironment() n'est pas encore implémentée.");
    }


    public void rename(Name oldName, Name newName)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode rename() n'est pas encore implémentée.");
    }


    public void rename(String oldName, String newName)
          throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode rename() n'est pas encore implémentée.");
    }


    public void unbind(Name name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode unbind() n'est pas encore implémentée.");
    }


    public void unbind(String name) throws NamingException {
        throw new java.lang.UnsupportedOperationException(
              "La méthode unbind() n'est pas encore implémentée.");
    }
}
