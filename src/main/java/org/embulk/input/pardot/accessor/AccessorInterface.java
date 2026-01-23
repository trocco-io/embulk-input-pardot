package org.embulk.input.pardot.accessor;

public interface AccessorInterface
{
    String get(String name);

    /**
     * Convert lower_underscore to UpperCamel (e.g., "visitor_id" -> "VisitorId")
     */
    default String lowerUnderscoreToUpperCamel(String input)
    {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            }
            else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            }
            else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
