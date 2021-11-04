package org.md.jmeter.influxdb2.visualizer.influxdb.client;

import com.influxdb.client.*;
import com.influxdb.client.write.Point;
import org.md.jmeter.influxdb2.visualizer.config.InfluxDBConfig;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The client to work with Influx DB 2.0 *
 *
 * @author Michael Derevyanko
 */
public class InfluxDatabaseClient {

    private static org.slf4j.Logger LOGGER;
    private static InfluxDBConfig influxDBConfig;
    private Timer timer;
    private static final List<Point> points = new ArrayList<>();
    private InfluxDBClient influxDB;

    private WriteApiBlocking writeApi;

    private static volatile InfluxDatabaseClient instance;

    /**
     * Creates a new instance of the @link InfluxDatabaseClient.
     *
     * @param context {@link BackendListenerContext}
     * @param logger  {@link Logger}
     */
    private InfluxDatabaseClient(BackendListenerContext context, Logger logger) {

        LOGGER = logger;
        influxDBConfig = new InfluxDBConfig(context);
    }

    /**
     * Creates the singleton instance of the {@link InfluxDatabaseClient}.
     * Creates the Influx DB client instance.
     * Executes the Influx DB points by schedule.
     * @param context {@link BackendListenerContext}
     * @param logger  {@link Logger}
     */
    public static InfluxDatabaseClient getInstance(BackendListenerContext context, Logger logger) {

        InfluxDatabaseClient result = instance;
        if (result != null) {
            return result;
        }
        synchronized (InfluxDatabaseClient.class) {
            if (instance == null) {
                instance = new InfluxDatabaseClient(context, logger);

                instance.setupInfluxClient();
                instance.writeDataByTimer();
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
        points.add(point);

        if (points.size() == influxDBConfig.getInfluxdbBatchSize()) {

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
        this.timer.cancel();
        points.clear();

        if(instance != null) {
            instance = null;
        }
    }

    /**
     * Writes {@link Point} from {@link List<Point>} collection if items exists.
     */
    public synchronized void writeData() {
        if (points.size() != 0) {
            try {
                long start = System.currentTimeMillis();
                this.writeApi.writePoints(points);
                long end = System.currentTimeMillis();
                LOGGER.info("Data has been imported successfully, batch with size --> " + points.size() + " ,elapsed time is -->" + (end - start)+ " ms");

                points.clear();
                LOGGER.debug("Points have been cleaned");
            }
            catch (Exception e)
            {

                LOGGER.error("Error has occurred, batch with size " + points.size() + " was not imported, see the details --> " + e.getMessage());

                this.timer.cancel();
                LOGGER.warn("Timer has stopped since error! You will have max batch size protection only.");
            }
        }
    }

    /**
     * Creates the Influx DB client instance.
     */
    private void setupInfluxClient() {

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

    /**
     * Writes data by timer.
     */
    private synchronized void writeDataByTimer() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {

            @Override
            public void run() {
                LOGGER.debug("Running the timer: " + new java.util.Date());
                if (instance != null) {
                    instance.writeData();
                }
            }
        }, 0, influxDBConfig.getInfluxdbFlushInterval());
    }
}
