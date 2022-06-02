package io.github.mderevyankoaqa.influxdb2.visualizer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client.InfluxDatabaseClient;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.InfluxDBConfig;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.TestStartEndMeasurement;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.VirtualUsersMeasurement;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import io.github.mderevyankoaqa.influxdb2.visualizer.result.SampleResultPointContext;
import io.github.mderevyankoaqa.influxdb2.visualizer.result.SampleResultPointProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Backend listener that writes JMeter metrics to influxDB directly.
 *
 * @author Alexander Wert
 * @author Michael Derevyanko (minor changes and improvements)
 */

public class InfluxDatabaseBackendListenerClient extends AbstractBackendListenerClient implements Runnable {
    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InfluxDatabaseBackendListenerClient.class);

    /**
     * Parameter Keys.
     */
    private static final String KEY_USE_REGEX_FOR_SAMPLER_LIST = "useRegexForSamplerList";
    private static final String KEY_TEST_NAME = "testName";
    private static final String KEY_RUN_ID = "runId";
    private static final String KEY_INCLUDE_BODY_OF_FAILURES = "saveResponseBodyOfFailures";
    private static final String KEY_NODE_NAME = "nodeName";
    private static final String KEY_SAMPLERS_LIST = "samplersList";
    private static final String KEY_RECORD_SUB_SAMPLES = "recordSubSamples";

    private final WritePrecision writePrecision = WritePrecision.MS;

    /**
     * Constants.
     */
    private static final String SEPARATOR = ";";
    private static final int ONE_MS_IN_NANOSECONDS = 1000000;

    /**
     * Scheduler for periodic metric aggregation.
     */
    private ScheduledThreadPoolExecutor scheduler;

    /**
     * Name of the test.
     */
    private String testName;

    /**
     * A unique identifier for a single execution (aka 'run') of a load test.
     * In a CI/CD automated performance test, a Jenkins or Bamboo build id would be a good value for this.
     */
    private String runId;

    /**
     * Name of the name.
     */
    private String nodeName;

    /**
     * Regex if samplers are defined through regular expression.
     */
    private String regexForSamplerList;

    /**
     * Set of samplers to record.
     */
    private Set<String> samplersToFilter;

    /**
     * Random number generator.
     */
    private Random randomNumberGenerator;

    /**
     * Indicates whether to record Sub samples.
     */
    private boolean recordSubSamples;


    private InfluxDBConfig influxDBConfig;

    private Timer timer;

    /**
     * Processes sampler results.
     */
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        // Gather all the listeners
        List<SampleResult> allSampleResults = new ArrayList<>();
        for (SampleResult sampleResult : sampleResults) {
            allSampleResults.add(sampleResult);

            if (recordSubSamples) {
                Collections.addAll(allSampleResults, sampleResult.getSubResults());
            }
        }

        for (SampleResult sampleResult : allSampleResults) {
            getUserMetrics().add(sampleResult);

            if ((null != regexForSamplerList && sampleResult.getSampleLabel().matches(regexForSamplerList))
                    || samplersToFilter.contains(sampleResult.getSampleLabel())) {

                SampleResultPointContext sampleResultContext = new SampleResultPointContext();
                sampleResultContext.setRunId(this.runId);
                sampleResultContext.setTestName(this.testName);
                sampleResultContext.setNodeName(this.nodeName);
                sampleResultContext.setSampleResult(sampleResult);
                sampleResultContext.setTimeToSet(System.currentTimeMillis() * ONE_MS_IN_NANOSECONDS + this.getUniqueNumberForTheSamplerThread());
                sampleResultContext.setErrorBodyToBeSaved(context.getBooleanParameter(KEY_INCLUDE_BODY_OF_FAILURES, false));

                var sampleResultPointProvider = new SampleResultPointProvider(sampleResultContext);

                Point resultPoint = sampleResultPointProvider.getPoint();
                InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).collectData(resultPoint);

            }
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument(KEY_TEST_NAME, "Test");
        arguments.addArgument(KEY_NODE_NAME, "Test-Node");
        arguments.addArgument(KEY_RUN_ID, "R001");
        arguments.addArgument(InfluxDBConfig.KEY_HTTP_SCHEME, InfluxDBConfig.DEFAULT_HTTP_SCHEME);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_HOST, "localhost");
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_PORT, Integer.toString(InfluxDBConfig.DEFAULT_PORT));
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_TOKEN, InfluxDBConfig.DEFAULT_INFLUX_DB_TOKEN);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_ORG, InfluxDBConfig.DEFAULT_INFLUX_DB_ORG);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_BUCKET, InfluxDBConfig.DEFAULT_BUCKET);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_FLUSH_INTERVAL, String.valueOf(InfluxDBConfig.DEFAULT_INFLUX_DB_FLUSH_INTERVAL));
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_MAX_BATCH_SIZE, String.valueOf(InfluxDBConfig.DEFAULT_INFLUX_DB_MAX_BATCH_SIZE));
        arguments.addArgument(KEY_SAMPLERS_LIST, ".*");
        arguments.addArgument(KEY_USE_REGEX_FOR_SAMPLER_LIST, "true");
        arguments.addArgument(KEY_RECORD_SUB_SAMPLES, "true");
        arguments.addArgument(KEY_INCLUDE_BODY_OF_FAILURES, "true");

        return arguments;
    }

    @Override
    public void setupTest(BackendListenerContext context) {
        this.testName = context.getParameter(KEY_TEST_NAME, "Test");
        this.runId = context.getParameter(KEY_RUN_ID, "R001"); //Will be used to compare performance of R001, R002, etc of 'Test'
        this.randomNumberGenerator = new Random();
        this.nodeName = context.getParameter(KEY_NODE_NAME, "Test-Node");

        this.setupInfluxClient(context);

        Point setupPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME).time(System.currentTimeMillis(), writePrecision)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.STARTED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, this.nodeName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, this.runId)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, this.testName)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");

        InfluxDatabaseClient.getInstance(influxDBConfig, LOGGER).collectData(setupPoint);

        this.parseSamplers(context);
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);
        this.scheduler.setRemoveOnCancelPolicy(true);

        this.scheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);

        // Indicates whether to write sub sample records to the database
        this.recordSubSamples = Boolean.parseBoolean(context.getParameter(KEY_RECORD_SUB_SAMPLES, "false"));
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        LOGGER.info("Shutting down influxDB scheduler...");


        this.timer.cancel();
        this.scheduler.shutdown();

        this.addVirtualUsersMetrics(0, 0, 0, 0, JMeterContextService.getThreadCounts().finishedThreads);

        Point teardownPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME).time(System.currentTimeMillis(), writePrecision)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.FINISHED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, this.nodeName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, this.runId)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, this.testName)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");

        InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).collectData(teardownPoint);

        try {
            this.scheduler.awaitTermination(30, TimeUnit.SECONDS);
            LOGGER.info("influxDB scheduler terminated!");
        } catch (InterruptedException e) {
            LOGGER.error("Error waiting for end of scheduler " + e);
        }

        InfluxDatabaseClient.getInstance(influxDBConfig, LOGGER).close();
        this.samplersToFilter.clear();
        super.teardownTest(context);
    }

    /**
     * Periodically writes virtual users metrics to influxDB.
     */
    public void run() {
        ThreadCounts tc = JMeterContextService.getThreadCounts();

        this.addVirtualUsersMetrics(getUserMetrics().getMinActiveThreads(),
                getUserMetrics().getMeanActiveThreads(),
                getUserMetrics().getMaxActiveThreads(),
                tc.startedThreads,
                tc.finishedThreads);
    }

    /**
     * Setups influxDB client.
     *
     * @param context {@link BackendListenerContext}.
     */
    private void setupInfluxClient(BackendListenerContext context) {

        this.influxDBConfig = new InfluxDBConfig(context);


        InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).setupInfluxClient();

        this.importDataByTimer(this.influxDBConfig, LOGGER);

    }

    /**
     * Parses list of samplers.
     *
     * @param context {@link BackendListenerContext}.
     */
    private void parseSamplers(BackendListenerContext context) {

        //List of samplers to record.
        String samplersList = context.getParameter(KEY_SAMPLERS_LIST, "");
        this.samplersToFilter = new HashSet<>();

        if (context.getBooleanParameter(KEY_USE_REGEX_FOR_SAMPLER_LIST, false)) {
            this.regexForSamplerList = samplersList;
        } else {
            this.regexForSamplerList = null;
            String[] samplers = samplersList.split(SEPARATOR);

            this.samplersToFilter = new HashSet<>();
            Collections.addAll(this.samplersToFilter, samplers);
        }
    }

    /**
     * Writes thread metrics.
     */
    private void addVirtualUsersMetrics(int minActiveThreads, int meanActiveThreads, int maxActiveThreads, int startedThreads, int finishedThreads) {
        Point virtualUsersMetricsPoint = Point.measurement(VirtualUsersMeasurement.MEASUREMENT_NAME).time(System.currentTimeMillis(), writePrecision)
                .addField(VirtualUsersMeasurement.Fields.MIN_ACTIVE_THREADS, minActiveThreads)
                .addField(VirtualUsersMeasurement.Fields.MAX_ACTIVE_THREADS, maxActiveThreads)
                .addField(VirtualUsersMeasurement.Fields.MEAN_ACTIVE_THREADS, meanActiveThreads)
                .addField(VirtualUsersMeasurement.Fields.STARTED_THREADS, startedThreads)
                .addField(VirtualUsersMeasurement.Fields.FINISHED_THREADS, finishedThreads)
                .addTag(VirtualUsersMeasurement.Tags.NODE_NAME, this.nodeName)
                .addTag(VirtualUsersMeasurement.Tags.TEST_NAME, this.testName)
                .addTag(VirtualUsersMeasurement.Tags.RUN_ID, this.runId);

        InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).collectData(virtualUsersMetricsPoint);
    }

    /**
     * Try to get a unique number for the sampler thread.
     */
    private int getUniqueNumberForTheSamplerThread() {

        return randomNumberGenerator.nextInt(ONE_MS_IN_NANOSECONDS);
    }

    /**
     * Imports data by timer set in the settings.
     * @param conf uses {@link InfluxDBConfig}
     * @param logger uses {@link Logger}
     */
    public void importDataByTimer(InfluxDBConfig conf, Logger logger) {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {

                LOGGER.debug("Running the timer: " + new java.util.Date());
                InfluxDatabaseClient.getInstance(conf, logger).importData();

            }
        }, 0, conf.getInfluxdbFlushInterval());
    }
}