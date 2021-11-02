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
     * Writes the {@link Point}.
     *
     * @param point the Influxdb {@link Point}.
     */
    public synchronized void collectData(Point point) {

        LOGGER.debug("Sending to write");
        points.add(point);

        if (points.size() == influxDBConfig.getInfluxdbBatchSize()) {

            LOGGER.debug("Batch size protection has occurred");
            this.writeData();
        }
    }

    public synchronized void close() {
        if (points.size() != 0) {

            this.writeApi.writePoints(points);
            LOGGER.debug("The final step --->" + points.size() + " points have been written");
        }

        this.influxDB.close();
        instance = null;
        this.timer.cancel();
        points.clear();
    }

    public synchronized void writeData() {
        if (points.size() != 0) {
            try {
                this.writeApi.writePoints(points);
                LOGGER.debug("The ---> " + points.size() + " points have been written");

                points.clear();
                LOGGER.debug("Points have been cleaned");
            }
            catch (Exception e)
            {
                LOGGER.error("Error has occurred while points writing, see the details --> " + e.getMessage());
            }
        }
    }

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
