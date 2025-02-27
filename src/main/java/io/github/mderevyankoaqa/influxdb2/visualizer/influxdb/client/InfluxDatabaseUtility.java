package io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client;

/**
 * The utility to escape values before inserting.
 * @author Michael Derevyanko
 */
public class InfluxDatabaseUtility {

    // Private constructor to prevent instantiation
    private InfluxDatabaseUtility() {
        // This constructor is intentionally left empty to prevent instantiation.
    }

    /**
     * Updates not supported values.
     * @param value the string which is going to be updated.
     * @return the escaped string.
     */
    public static String getEscapedString(String value) {

          return value.replace("\n", "")
                      .replace("\r", "")
                      .replace(",", "\\,")
                      .trim();

    }

    /**
     * Returns a substring of the specified length from the given string.
     * <p>
     * If the length of the provided string is greater than or equal to the
     * expected length, this method returns the substring starting from the
     * beginning of the string up to the expected length. If the length of the
     * string is less than the expected length, the entire string is returned
     * without any truncation.
     * </p>
     *
     * @param value the original string from which the substring will be extracted.
     * @param expectedLength the desired length of the substring.
     * @return the substring of the specified length if the original string is
     *         longer than the expected length; otherwise, the original string.
     */
    public static String getSubstring(String value, int expectedLength)
    {
        String newValue;
        if(value.length() >= expectedLength)
        {
            newValue = value.substring(0,expectedLength);
        }else {
            newValue = value;
        }

        return newValue;
    }
}
