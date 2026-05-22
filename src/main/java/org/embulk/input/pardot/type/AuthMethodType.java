package org.embulk.input.pardot.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthMethodType
{
    OAUTH("oauth"),
    USER_PASSWORD("user_password");

    private final String value;

    AuthMethodType(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }
}
