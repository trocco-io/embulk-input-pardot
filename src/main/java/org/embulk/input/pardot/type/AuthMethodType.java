package org.embulk.input.pardot.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthMethodType {
    oauth,
    user_password;

    @JsonCreator
    public static AuthMethodType fromString(String value)
    {
        if (value == null) {
            return user_password; // デフォルト値
        }
        try {
            return AuthMethodType.valueOf(value);
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
