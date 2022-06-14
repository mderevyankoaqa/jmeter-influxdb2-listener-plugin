package io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client;

/**
 * The utility to escape values before inserting.
 * @author Michael Derevyanko
 */
public class InfluxDatabaseUtility {

    /**
     * Updates not supported values.
     * @param value the string which is going to be updated.
     * @return the escaped string.
     */
    public static String getEscapedString(String value) {

          return value.replace("\n", "")
                      .replace("\r", "")
                      .trim();

    }

    /**
     * Gets the substring from the value.
     * @param value the initial value.
     * @param expectedLength the expected length.
     * @return returns the the truncated string if the initial string length is less or equal to expected length; otherwise initial string.
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
