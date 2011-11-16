package net.codjo.security.common.api;
import java.io.Serializable;
import java.util.regex.Matcher;
/**
 *
 */
public class Pattern implements Serializable {
    private java.util.regex.Pattern pattern;


    public Pattern(String pattern) {
        try {

            this.pattern = java.util.regex.Pattern.compile(encode(pattern));
        }
        catch (Throwable ex) {
            throw new IllegalArgumentException("pattern invalide " + pattern, ex);
        }
    }


    public boolean match(String function) {
        Matcher matcher = pattern.matcher(function);
        return matcher.matches();
    }


    private static String encode(String function) {
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < function.length(); i++) {
            char ch = function.charAt(i);
            if (ch == '*') {
                sb.append('.');
            }
            sb.append(ch);
        }
        sb.append('$');
        return sb.toString();
    }
}
