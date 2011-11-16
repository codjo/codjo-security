package net.codjo.security.common;
/**
 *
 */
public class AccountLockedException extends Exception {
    private String urlToUnlock;


    public AccountLockedException(String message) {
        super(message);
    }


    public AccountLockedException(String message, String urlToUnlock) {
        super(message);
        this.urlToUnlock = urlToUnlock;
    }


    public String getUrlToUnlock() {
        return urlToUnlock;
    }
}
