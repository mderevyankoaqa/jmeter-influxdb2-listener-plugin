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
}
