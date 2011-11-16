package net.codjo.security.server.storage;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.Storage;
/**
 *
 */
public abstract class AbstractStorage implements Storage {
    protected static final String BAD_DATA = "Paramétrage de la couche sécurité incohérente. ";


    protected ModelManager fromXml(String xml) {
        return XmlCodec.createFromXml(xml);
    }


    protected String toXml(ModelManager manager) {
        return XmlCodec.toXml(manager);
    }


    public static class BadConfigurationException extends RuntimeException {
        public BadConfigurationException(String message) {
            super(message);
        }
    }
}
