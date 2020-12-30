package org.md.jmeter.influxdb2.visualizer.influxdb.client;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import org.md.jmeter.influxdb2.visualizer.config.InfluxDBConfig;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;

/**
 * The client to work with Influx DB 2.0 *
 * @author Michael Derevyanko
 */
public class InfluxDatabaseClient {

    private static org.slf4j.Logger LOGGER;
    private final InfluxDBConfig influxDBConfig;
    private InfluxDBClient influxDB;

    /**
     * Creates a new instance of the @link InfluxDatabaseClient.
     *
     * @param context {@link BackendListenerContext}
     * @param logger  {@link Logger}
     */
    public InfluxDatabaseClient(BackendListenerContext context, Logger logger) {

        LOGGER = logger;
        this.influxDBConfig = new InfluxDBConfig(context);
    }

    /**
     * Creates the Influx DB client instance.
     */
    public void setupInfluxClient() {

        this.influxDB = InfluxDBClientFactory.
                create(this.influxDBConfig.getInfluxDBURL(),
                        this.influxDBConfig.getInfluxToken().toCharArray(),
                        this.influxDBConfig.getInfluxOrganization(),
                        this.influxDBConfig.getInfluxBucket());
    }

    /**
     * Writes the {@link Point}.     *
     * @param point the Influxdb {@link Point}.
     */
    public void write(Point point) {

        // Write by Data Point
        try (WriteApi writeApi = this.influxDB.getWriteApi()) {

            writeApi.writePoint(point);
        }

        catch (Exception e)
        {
            LOGGER.error("Failed writing to influx", e);
        }
    }

    public void close()
    {
        this.influxDB.close();
    }
}
