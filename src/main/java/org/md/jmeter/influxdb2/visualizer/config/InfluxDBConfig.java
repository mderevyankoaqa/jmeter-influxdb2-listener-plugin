package org.md.jmeter.influxdb2.visualizer.config;

import com.influxdb.Arguments;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

/**
 * Configuration for influxDB.
 * @author Alexander Wert
 * @author Michael Derevyanko (minor changes and improvements)
 */
public class InfluxDBConfig {

	/**
	 * Default bucket name.
	 */
	public static final String DEFAULT_BUCKET = "jmeter";

	/**
	 * Default http scheme name.
	 */
	public static final String DEFAULT_HTTP_SCHEME = "http";

	/**
	 * Default port.
	 */
	public static final int DEFAULT_PORT = 8086;

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
	 * Config key for port.
	 */
	public static final String KEY_INFLUX_DB_PORT = "influxDBPort";

	/**
	 * Config key for host.
	 */
	public static final String KEY_INFLUX_DB_HOST = "influxDBHost";


	/**
	 * Config key for http scheme.
	 */
	public static final String KEY_HTTP_SCHEME = "influxDBHttpScheme";

	/**
	 * Config key for batch size.
	 */
	public static final String KEY_INFLUX_DB_MAX_BATCH_SIZE = "influxDBMaxBatchSize";

	/**
	 * Config key for retry interval.
	 */
	public static final String KEY_INFLUX_DB_FLUSH_INTERVAL = "influxDBFlushInterval";

	/**
	 * InfluxDB Host.
	 */
	private String influxDBHost;

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
	 * InfluxDB Port.
	 */
	private int influxDBPort;

	/**
	 * InfluxDB database retention policy.
	 */
	private String influxHTTPScheme;

	/**
	 * InfluxDB database batch size.
	 */
	private int influxdbBatchSize;

	/**
	 * InfluxDB database flush interval.
	 */
	private int influxdbFlushInterval;


	/**
	 * Creates the new instance of {@link InfluxDBConfig}
	 * @param context the {@link BackendListenerContext}
	 */
	public InfluxDBConfig(BackendListenerContext context) {
		String influxDBHost = context.getParameter(KEY_INFLUX_DB_HOST);
		Arguments.checkNonEmpty(KEY_INFLUX_DB_BUCKET, influxDBHost);
		this.setInfluxDBHost(influxDBHost);

		int influxDBPort = context.getIntParameter(KEY_INFLUX_DB_PORT, InfluxDBConfig.DEFAULT_PORT);
		this.setInfluxDBPort(influxDBPort);

		String influxToken = context.getParameter(KEY_INFLUX_DB_TOKEN);
		Arguments.checkNonEmpty(influxToken, KEY_INFLUX_DB_TOKEN);
		this.setInfluxToken(influxToken);

		String influxOrg = context.getParameter(KEY_INFLUX_DB_ORG);
		Arguments.checkNonEmpty(influxOrg, KEY_INFLUX_DB_ORG);
		this.setInfluxOrganization(influxOrg);

		String influxBucket = context.getParameter(KEY_INFLUX_DB_BUCKET);
		Arguments.checkNonEmpty(influxBucket, KEY_INFLUX_DB_BUCKET);
		this.setInfluxBucket(influxBucket);

		String influxHTTPScheme = ArgsValidator.checkHTTPScheme(context.getParameter(KEY_HTTP_SCHEME, DEFAULT_HTTP_SCHEME));
		this.setInfluxHTTPScheme(influxHTTPScheme);

		int influxdbBatchSize = context.getIntParameter(KEY_INFLUX_DB_MAX_BATCH_SIZE);
		Arguments.checkNotNegativeNumber(influxdbBatchSize, KEY_INFLUX_DB_MAX_BATCH_SIZE);
		this.setInfluxdbBatchSize(influxdbBatchSize);

		int influxdbFlushInterval = context.getIntParameter(KEY_INFLUX_DB_FLUSH_INTERVAL);
		Arguments.checkNotNegativeNumber(influxdbFlushInterval, KEY_INFLUX_DB_FLUSH_INTERVAL);
		this.setInfluxdbFlushInterval(influxdbFlushInterval);
	}

	/**
	 * Builds URL to influxDB.
	 *
	 * @return influxDB URL.
	 */
	public String getInfluxDBURL() {
		return this.influxHTTPScheme + "://" + this.getInfluxDBHost()  + ":" + this.getInfluxDBPort();
	}

	/**
	 * @return the influxDBHost.
	 */
	public String getInfluxDBHost() {
		return this.influxDBHost;
	}

	/**
	 * @param influxDBHost
	 *            the influxDBHost to set.
	 */
	public void setInfluxDBHost(String influxDBHost) {
		this.influxDBHost = influxDBHost;
	}

	/**
	 * @return the influxDatabase Bucket.
	 */
	public String getInfluxBucket() {
		return this.influxBucket;
	}

	/**
	 * @param influxBucket
	 *            the influxDatabase to set.
	 */
	public void setInfluxBucket(String influxBucket) {
		this.influxBucket = influxBucket;
	}

	/**
	 * @param influxHTTPScheme
	 *            the influxHTTPScheme to set.
	 */
	public void setInfluxHTTPScheme(String influxHTTPScheme) {
		this.influxHTTPScheme = influxHTTPScheme;
	}

	/**
	 * @return the influxDBPort.
	 */
	public int getInfluxDBPort() {
		return this.influxDBPort;
	}

	/**
	 * @param influxDBPort
	 *            the influxDBPort to set.
	 */
	public void setInfluxDBPort(int influxDBPort) {
		this.influxDBPort = influxDBPort;
	}

	/**
	 * Gets the InfluxDB organization.
	 * @return the InfluxDB organization.
	 */
	public String getInfluxOrganization() {
		return influxOrganization;
	}

	/**
	 * Sets the InfluxDB organization name.
	 * @param influxOrganization is InfluxDB organization name..
	 */
	public void setInfluxOrganization(String influxOrganization) {
		this.influxOrganization = influxOrganization;
	}

	/**
	 * Gets the InfluxDB token.
	 * @return InfluxDB token represented in the string.
	 */
	public String getInfluxToken() {
		return this.influxToken;
	}

	/**
	 * Sets the InfluxDB token.
	 * @param influxToken is InfluxDB token represented in the string.
	 */
	public void setInfluxToken(String influxToken) {
		this.influxToken = influxToken;
	}

	/**
	 * Sets the InfluxDB batch size.
	 * @param influxdbBatchSize is InfluxDB batch size represented in the int.
	 */
	public void setInfluxdbBatchSize(int influxdbBatchSize) {
		this.influxdbBatchSize = influxdbBatchSize;
	}

	/**
	 * Returns InfluxDB batch size.
	 * @return InfluxDB batch size represented in the int.
	 */
	public int getInfluxdbBatchSize() {
		return influxdbBatchSize;
	}

	/**
	 * Gets InfluxDB flush interval.
	 * @return InfluxDB flush interval represented in the int.
	 */
	public int getInfluxdbFlushInterval() {
		return influxdbFlushInterval;
	}

	/**
	 * Sets flush interval.
	 * @param influxdbFlushInterval is flush interval represented in the int.
	 */
	public void setInfluxdbFlushInterval(int influxdbFlushInterval) {
		this.influxdbFlushInterval = influxdbFlushInterval;
	}
}
