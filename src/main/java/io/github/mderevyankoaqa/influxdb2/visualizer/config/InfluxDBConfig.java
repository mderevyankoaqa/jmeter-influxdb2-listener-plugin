package io.github.mderevyankoaqa.influxdb2.visualizer.config;


import com.influxdb.utils.Arguments;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

/**
 * Configuration for influxDB.
 *
 * @author Alexander Wert
 * @author Michael Derevyanko (minor changes and improvements)
 */
public class InfluxDBConfig {

    /**
     * Default bucket name.
     */
    public static final String DEFAULT_BUCKET = "jmeter";

    /**
     * Default influxdb url.
     */
    public static final String DEFAULT_INFLUXDB_URL = "http://localhost:8086/";

    /**
     * Default threshold error.
     */
    public static final int DEFAULT_THRESHOLD_ERROR = 5;

    /**
     * Default token.
     */
    public static final String DEFAULT_INFLUX_DB_TOKEN = "put token here";

    /**
     * Default organization.
     */
    public static final String DEFAULT_INFLUX_DB_ORG = "performance_testing";

    /**
     * Default batch size.
     */
    public static final int DEFAULT_INFLUX_DB_MAX_BATCH_SIZE = 2000;

    /**
     * Default flush interval.
     */
    public static final int DEFAULT_INFLUX_DB_FLUSH_INTERVAL = 4000;

    /**
     * Default response body length.
     */
    public static final int DEFAULT_RESPONSE_BODY_LENGTH = 2000;

    /**
     * Config key for influxdb url.
     */
    public static final String KEY_INFLUX_DB_URL = "influxDBURL";

    /**
     * Config key for bucket.
     */
    public static final String KEY_INFLUX_DB_BUCKET = "influxDBBucket";

    /**
     * Config key for organization.
     */
    public static final String KEY_INFLUX_DB_ORG = "influxDBOrganization";

    /**
     * Config key for token.
     */
    public static final String KEY_INFLUX_DB_TOKEN = "influxDBToken";

    /**
     * Config key for batch size.
     */
    public static final String KEY_INFLUX_DB_MAX_BATCH_SIZE = "influxDBMaxBatchSize";

    /**
     * Config key for flush interval.
     */
    public static final String KEY_INFLUX_DB_FLUSH_INTERVAL = "influxDBFlushInterval";

    /**
     * Config key error threshold.
     */
    public static final String KEY_INFLUX_DB_THRESHOLD_ERROR = "influxDBThresholdError";

    /**
     * Config key to manage the response body length.
     */
    public static final String KEY_RESPONSE_BODY_LENGTH = "responseBodyLength";

    /**
     * InfluxDB URL.
     */
    private String influxDBURL;

    /**
     * InfluxDB Token.
     */
    private String influxToken;

    /**
     * InfluxDB Organization.
     */
    private String influxOrganization;

    /**
     * InfluxDB database Bucket.
     */
    private String influxBucket;

    /**
     * InfluxDB database batch size.
     */
    private int influxdbBatchSize;

    /**
     * InfluxDB database flush interval.
     */
    private int influxdbFlushInterval;

    /**
     * Default protection to avoid OOM error.
     */
    private int influxdbThresholdError;

    /**
     * Response body length.
     */
    private int responseBodyLength;

    /**
     * Creates the new instance of {@link InfluxDBConfig}
     *
     * @param context the {@link BackendListenerContext}
     */
    public InfluxDBConfig(BackendListenerContext context) {
        String influxDBURL = context.getParameter(KEY_INFLUX_DB_URL);
        Arguments.checkNonEmpty(influxDBURL, KEY_INFLUX_DB_URL);
        String[] influxHTTPScheme =  influxDBURL.split("://",2);
        ArgsValidator.checkHTTPScheme(influxHTTPScheme[0]);
        this.setInfluxDBURL(influxDBURL);

        String influxToken = context.getParameter(KEY_INFLUX_DB_TOKEN);
        Arguments.checkNonEmpty(influxToken, KEY_INFLUX_DB_TOKEN);
        this.setInfluxToken(influxToken);

        String influxOrg = context.getParameter(KEY_INFLUX_DB_ORG);
        Arguments.checkNonEmpty(influxOrg, KEY_INFLUX_DB_ORG);
        this.setInfluxOrganization(influxOrg);

        String influxBucket = context.getParameter(KEY_INFLUX_DB_BUCKET);
        Arguments.checkNonEmpty(influxBucket, KEY_INFLUX_DB_BUCKET);
        this.setInfluxBucket(influxBucket);

        int influxdbBatchSize = context.getIntParameter(KEY_INFLUX_DB_MAX_BATCH_SIZE);
        Arguments.checkNotNegativeNumber(influxdbBatchSize, KEY_INFLUX_DB_MAX_BATCH_SIZE);
        this.setInfluxdbBatchSize(influxdbBatchSize);

        int influxdbFlushInterval = context.getIntParameter(KEY_INFLUX_DB_FLUSH_INTERVAL);
        Arguments.checkNotNegativeNumber(influxdbFlushInterval, KEY_INFLUX_DB_FLUSH_INTERVAL);
        this.setInfluxdbFlushInterval(influxdbFlushInterval);

        int influxdbThresholdError = context.getIntParameter(KEY_INFLUX_DB_THRESHOLD_ERROR, InfluxDBConfig.DEFAULT_THRESHOLD_ERROR);
        Arguments.checkNotNegativeNumber(influxdbThresholdError, KEY_INFLUX_DB_THRESHOLD_ERROR);
        this.setInfluxdbThresholdError(influxdbThresholdError);

        int responseBodyLength= context.getIntParameter(KEY_RESPONSE_BODY_LENGTH);
        Arguments.checkNotNegativeNumber(influxdbFlushInterval, KEY_RESPONSE_BODY_LENGTH);
        this.setResponseBodyLength(responseBodyLength);
    }

    /**
     * Gets the InfluxDB URL.
     *
     * @return the InfluxDB URL.
     */
    public String getInfluxDBURL() {
        return influxDBURL;
    }

    /**
     * Sets influxDB URL.
     *
     * @param influxDBURL the influxdb host url.
     */
    public void setInfluxDBURL(String influxDBURL) {
        this.influxDBURL = influxDBURL;
    }

    /**
     * Gets influxDatabase Bucket.
     *
     * @return the influxDatabase Bucket.
     */
    public String getInfluxBucket() {
        return this.influxBucket;
    }

    /**
     * Sets influxDatabase.
     *
     * @param influxBucket the influxDatabase to set.
     */
    public void setInfluxBucket(String influxBucket) {
        this.influxBucket = influxBucket;
    }

    /**
     * Gets the InfluxDB organization.
     *
     * @return the InfluxDB organization.
     */
    public String getInfluxOrganization() {
        return influxOrganization;
    }

    /**
     * Sets the InfluxDB organization name.
     *
     * @param influxOrganization is InfluxDB organization name..
     */
    public void setInfluxOrganization(String influxOrganization) {
        this.influxOrganization = influxOrganization;
    }

    /**
     * Gets the InfluxDB token.
     *
     * @return InfluxDB token represented in the string.
     */
    public String getInfluxToken() {
        return this.influxToken;
    }

    /**
     * Sets the InfluxDB token.
     *
     * @param influxToken is InfluxDB token represented in the string.
     */
    public void setInfluxToken(String influxToken) {
        this.influxToken = influxToken;
    }

    /**
     * Sets the InfluxDB batch size.
     *
     * @param influxdbBatchSize is InfluxDB batch size represented in the int.
     */
    public void setInfluxdbBatchSize(int influxdbBatchSize) {
        this.influxdbBatchSize = influxdbBatchSize;
    }

    /**
     * Returns InfluxDB batch size.
     *
     * @return InfluxDB batch size represented in the int.
     */
    public int getInfluxdbBatchSize() {
        return influxdbBatchSize;
    }

    /**
     * Gets InfluxDB flush interval.
     *
     * @return InfluxDB flush interval represented in the int.
     */
    public int getInfluxdbFlushInterval() {
        return influxdbFlushInterval;
    }

    /**
     * Sets flush interval.
     *
     * @param influxdbFlushInterval is flush interval represented in the int.
     */
    public void setInfluxdbFlushInterval(int influxdbFlushInterval) {
        this.influxdbFlushInterval = influxdbFlushInterval;
    }

    /**
     * Sets error threshold.
     *
     * @param influxdbThresholdError is the threshold of error to stop sending data to influx db to avoid OOM error.
     */
    public void setInfluxdbThresholdError(int influxdbThresholdError) {
        this.influxdbThresholdError = influxdbThresholdError;
    }

    /**
     * Gets threshold error to stop InfluxDB import.
     *
     * @return Threshold of error represented in the int.
     */
    public int getInfluxdbThresholdError() {
        return influxdbThresholdError;
    }

    /**
     * Gets the response body length.
     *
     * @return the response body length.
     */
    public int getResponseBodyLength() {
        return responseBodyLength;
    }

    /**
     * Sets the response body length.
     *
     * @param responseBodyLength the response body length.
     */
    public void setResponseBodyLength(int responseBodyLength) {
        this.responseBodyLength = responseBodyLength;
    }
}
