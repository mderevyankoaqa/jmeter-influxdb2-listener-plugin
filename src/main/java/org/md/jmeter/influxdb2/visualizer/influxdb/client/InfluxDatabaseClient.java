package org.md.jmeter.influxdb2.visualizer.influxdb.client;

import com.influxdb.client.*;
import com.influxdb.client.write.Point;
import org.md.jmeter.influxdb2.visualizer.config.InfluxDBConfig;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;

import java.util.*;

/**
 * The client to work with Influx DB 2.0 *
 *
 * @author Michael Derevyanko
 */
public class InfluxDatabaseClient {

    private static org.slf4j.Logger LOGGER;
    private static InfluxDBConfig influxDBConfig;
    private final List<Point> points;
    private InfluxDBClient influxDB;

    private WriteApiBlocking writeApi;

    private static volatile InfluxDatabaseClient instance;
    private static final int criticalBatchSize = 500;


    /**
     * Creates a new instance of the @link InfluxDatabaseClient.
     *
     * @param config {@link InfluxDBConfig}
     * @param logger  {@link Logger}
     */
    private InfluxDatabaseClient(InfluxDBConfig config, Logger logger) {

        this.points = Collections.synchronizedList(new ArrayList<>());
        influxDBConfig = config;
        LOGGER = logger;
    }

    /**
     * Creates the singleton instance of the {@link InfluxDatabaseClient}.
     * Creates the Influx DB client instance.
     * Executes the Influx DB points by schedule.
     * @param config {@link InfluxDBConfig}
     * @param logger  {@link Logger}
     */
    public static InfluxDatabaseClient getInstance(InfluxDBConfig config, Logger logger) {

        InfluxDatabaseClient result = instance;
        if (result != null) {
            return result;
        }
        synchronized (InfluxDatabaseClient.class) {
            if (instance == null) {
                instance = new InfluxDatabaseClient(config, logger);
            }
            return instance;
        }
    }

    /**
     * Collects the {@link Point} in {@link List<Point>}
     * Writes {@link List<Point>} when Batch size equals to size of the {@link List<Point>}; cleans {@link List<Point>} after writing.
     * @param point the Influxdb {@link Point}.
     */
    public synchronized void collectData(Point point) {

        LOGGER.debug("Sending to write");
        this.points.add(point);

        if (this.points.size() >= influxDBConfig.getInfluxdbBatchSize()) {

            LOGGER.info("Batch size protection has occurred.");
            this.writeData();
        }
    }

    /**
     * Closes Influx DB client, cancels timers to write {@link Point}, cleaning {@link List<Point>} collection; writes Points before closing.
     */
    public synchronized void close() {

        LOGGER.info("The final step ---> importing before closing.");
        this.writeData();
        this.influxDB.close();
        this.points.clear();

        if (instance != null)
        {
            instance = null;
        }
    }

    /**
     * Writes {@link Point} from {@link List<Point>} collection if items exists.
     */
    public synchronized void writeData() {

        if (this.points.size() != 0) {
            try {
                long start = System.currentTimeMillis();
                this.writeApi.writePoints(this.points);
                long end = System.currentTimeMillis();
                LOGGER.info("Data has been imported successfully, batch with size is --> " + this.points.size() + ", elapsed time is --> " + (end - start)+ " ms");

                this.points.clear();
                LOGGER.debug("Points have been cleaned");
            }
            catch (Exception e)
            {
                LOGGER.error("Error has occurred, batch with size " + this.points.size() + " was not imported, see the details --> " + e.getMessage());


                if (this.points.size() >= criticalBatchSize)
                {
                    LOGGER.warn("Critical size protection has occurred the --> " + this.points.size() + "; batch data has bee removed from que and will not be imported, data completely lost to avoid OOM!!!");
                    this.points.clear();
                }
            }
        }
    }

    /**
     * Creates the Influx DB client instance.
     */
    public synchronized void setupInfluxClient() {

        LOGGER.info("InfluxDBClientFactory is going to use the following properties:");
        LOGGER.info("URL --> " + influxDBConfig.getInfluxDBURL());
        LOGGER.info("Token --> " + influxDBConfig.getInfluxToken());
        LOGGER.info("Organization --> " + influxDBConfig.getInfluxOrganization());
        LOGGER.info("Bucket --> " + influxDBConfig.getInfluxBucket());

        try {
            this.influxDB = InfluxDBClientFactory.
                    create(influxDBConfig.getInfluxDBURL(),
                            influxDBConfig.getInfluxToken().toCharArray(),
                            influxDBConfig.getInfluxOrganization(),
                            influxDBConfig.getInfluxBucket());

            this.influxDB.enableGzip();
            this.writeApi = this.influxDB.getWriteApiBlocking();

        } catch (Exception e) {
            LOGGER.error("Failed to create client", e);
        }
    }
}
