package org.embulk.input.pardot.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AuthMethodType {
    OAUTH,
    USER_PASSWORD;

    @JsonCreator
    public static AuthMethodType fromString(String value)
    {
        if (value == null) {
            return USER_PASSWORD; // デフォルト値
        }
        try {
            return AuthMethodType.valueOf(value.toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Unknown authentication method type '%s'. Supported types are: oauth, user_password", value));
        }
    }

    @JsonValue
    public String toString()
    {
        return name();
    }
}
