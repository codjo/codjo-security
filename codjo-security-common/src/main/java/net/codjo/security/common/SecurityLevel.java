package net.codjo.security.common;
/**
 *
 */
public enum SecurityLevel {
    USER(1),
    SERVICE(0);

    private int value;


    SecurityLevel(int value) {
        this.value = value;
    }


    public int getValue() {
        return value;
    }


    public static SecurityLevel toEnum(int value) {
        for (SecurityLevel level : SecurityLevel.values()) {
            if (level.getValue() == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Pas de correspondance pour le security level: '" + value + "'");
    }


    public static SecurityLevel toEnum(String strValue) {
        int value;
        try {
            value = Integer.valueOf(strValue);
            return toEnum(value);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                  "Pas de correspondance pour le security level: '" + strValue + "'");
        }
    }
}
