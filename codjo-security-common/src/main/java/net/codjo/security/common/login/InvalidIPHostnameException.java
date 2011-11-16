package net.codjo.security.common.login;
/**
 *
 */
public class InvalidIPHostnameException extends Exception {
    private final String ip;
    private final String hostname;


    public InvalidIPHostnameException(String ip, String hostname) {
        super("Le nom d'hôte (" + hostname + ") et l'IP (" + ip + ") associée ne sont pas corrects.");
        this.ip = ip;
        this.hostname = hostname;
    }


    public String getIp() {
        return ip;
    }


    public String getHostname() {
        return hostname;
    }
}
