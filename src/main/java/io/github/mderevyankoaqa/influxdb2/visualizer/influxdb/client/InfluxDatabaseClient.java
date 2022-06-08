package io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client;

import com.influxdb.client.*;
import com.influxdb.client.write.Point;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.InfluxDBConfig;
import org.slf4j.Logger;

import java.util.*;

/**
 * The client to work with Influx DB 2.0 *
 *
 * @author Michael Derevyanko
 */
public class InfluxDatabaseClient {

    private final org.slf4j.Logger LOGGER;
    private final InfluxDBConfig influxDBConfig;
    private final List<Point> points;
    private InfluxDBClient influxDB;

    private WriteApiBlocking writeApi;
    private final List<Integer> errorsAmount;

    private static volatile InfluxDatabaseClient instance;


    /**
     * Creates a new instance of the @link InfluxDatabaseClient.
     *
     * @param config {@link InfluxDBConfig}
     * @param logger {@link Logger}
     */
    private InfluxDatabaseClient(InfluxDBConfig config, Logger logger) {

        this.points = Collections.synchronizedList(new ArrayList<>());
        this.errorsAmount = Collections.synchronizedList(new ArrayList<>());
        this.influxDBConfig = config;
        this.LOGGER = logger;

        this.LOGGER.info("New instance of InfluxDatabaseClient has been created!");
    }

    /**
     * Creates the singleton instance of the {@link InfluxDatabaseClient}.
     * @param config {@link InfluxDBConfig}
     * @param logger {@link Logger}
     * @return the created instance of {@link InfluxDatabaseClient}
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
     *
     * @param point the Influxdb {@link Point}.
     */
    public synchronized void collectData(Point point) {

        this.LOGGER.debug("Sending to write");
        this.points.add(point);

        this.checkBatchSize();
    }

    /**
     * Closes Influx DB client, cancels timers to write {@link Point}, cleaning {@link List<Point>} collection; writes Points before closing.
     */
    public synchronized void close() {

        this.LOGGER.info("The final step ---> importing before closing.");
        this.importData();

        this.influxDB.close();
        this.points.clear();

        if (instance != null) {

            instance = null;
            this.LOGGER.info("Instance of InfluxDatabaseClient has been refreshed!");
        }
    }

    /**
     * Imports {@link Point} from {@link List<Point>} collection if items exists. Cleans {@link List<Point>} after writing.
     * Manages Error Amount counter. The limit of the errors occurred one by one is 5. After 5 such errors, import is stopping till and of the tes.
     * If less 5 error has occurred and next import attempt was successful - errors counter will be refreshed.
     */
    public synchronized void importData() {

        if (this.errorsAmount.size() >= this.influxDBConfig.getInfluxdbThresholdError())
        {
            this.points.clear();
            this.LOGGER.warn("Importing of the results to Influx DB is skipping since 5 errors, has occurred!");
        }

        if (this.points.size() != 0 && this.errorsAmount.size() <= this.influxDBConfig.getInfluxdbThresholdError()) {
            try {

                long start = System.currentTimeMillis();
                this.writeApi.writePoints(this.points);
                long end = System.currentTimeMillis();

                if (this.errorsAmount.size() != 0)
                {
                    this.errorsAmount.clear();
                    this.LOGGER.warn("Counter of the errors refreshed since import was done successfully.");
                 }

                this.LOGGER.info("Data has been imported successfully, batch with size is --> " + this.points.size() + ", elapsed time is --> " + (end - start) + " ms");

                this.points.clear();
                this.LOGGER.debug("Points have been cleaned");

            } catch (Exception e) {

                this.LOGGER.error("Error has occurred, batch with size " + this.points.size() + " was not imported, see the details --> " + e.getMessage());
                this.errorsAmount.add(1);
            }
        }
    }

    /**
     * Creates the Influx DB client instance.
     */
    public synchronized void setupInfluxClient() {

        this.LOGGER.info("InfluxDBClientFactory is going to use the following properties:");
        this.LOGGER.info("URL --> " + this.influxDBConfig.getInfluxDBURL());
        this.LOGGER.info("Token --> " + this.influxDBConfig.getInfluxToken());
        this.LOGGER.info("Organization --> " + this.influxDBConfig.getInfluxOrganization());
        this.LOGGER.info("Bucket --> " + this.influxDBConfig.getInfluxBucket());

        try {
            this.influxDB = InfluxDBClientFactory.
                    create(this.influxDBConfig.getInfluxDBURL(),
                            this.influxDBConfig.getInfluxToken().toCharArray(),
                            this.influxDBConfig.getInfluxOrganization(),
                            this.influxDBConfig.getInfluxBucket());

            this.influxDB.enableGzip();
            this.writeApi = this.influxDB.getWriteApiBlocking();

        } catch (Exception e) {
            this.LOGGER.error("Failed to create client", e);
        }
    }

    /**
     * Checks batch size, makes import when limit is reached.
     */
    private synchronized void checkBatchSize()
    {
        if (this.points.size() >= this.influxDBConfig.getInfluxdbBatchSize()) {

            this.LOGGER.info("Batch size protection has occurred.");
            this.importData();
        }
    }
}
