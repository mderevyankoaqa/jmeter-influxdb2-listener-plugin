package io.github.mderevyankoaqa.influxdb2.visualizer;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.InfluxDBConfig;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.TestStartEndMeasurement;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.VirtualUsersMeasurement;
import io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client.InfluxDatabaseClient;
import io.github.mderevyankoaqa.influxdb2.visualizer.result.SampleResultPointContext;
import io.github.mderevyankoaqa.influxdb2.visualizer.result.SampleResultPointProvider;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Backend listener that writes JMeter metrics to influxDB directly.
 *
 * @author Alexander Wert
 * @author Michael Derevyanko (minor changes and improvements)
 */

public class InfluxDatabaseBackendListenerClient extends AbstractBackendListenerClient implements Runnable {

    /**
     * Constructs a new instance of {@link InfluxDatabaseBackendListenerClient}.
     * This constructor is currently empty, but can be used to initialize any necessary
     * resources or configurations in future extensions.
     */
    public InfluxDatabaseBackendListenerClient() {

    }

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
    private SecureRandom randomNumberGenerator;

    /**
     * Indicates whether to record Sub samples.
     */
    private boolean recordSubSamples;

    /**
     * The Influx DB Config.
     */
    private InfluxDBConfig influxDBConfig;

    private Timer timer;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * Processes sampler results.
     */
    
    /* --Removed RHILL 23/01/2026
    
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
            // Determine the type of sample, whether it is a request or a transaction controller
            String samplerType = "transaction";
            if (sampleResult instanceof HTTPSampleResult){
                samplerType = "request";
            }

            if ((null != regexForSamplerList && sampleResult.getSampleLabel().matches(regexForSamplerList))
                    || samplersToFilter.contains(sampleResult.getSampleLabel())) {

                SampleResultPointContext sampleResultContext = new SampleResultPointContext();
                sampleResultContext.setRunId(this.runId);
                sampleResultContext.setTestName(this.testName);
                sampleResultContext.setNodeName(this.nodeName);
                sampleResultContext.setSampleResult(sampleResult);
                sampleResultContext.setSamplerType(samplerType);
                sampleResultContext.setTimeToSet(System.currentTimeMillis() * ONE_MS_IN_NANOSECONDS + this.getUniqueNumberForTheSamplerThread());
                sampleResultContext.setErrorBodyToBeSaved(context.getBooleanParameter(KEY_INCLUDE_BODY_OF_FAILURES, false));
                sampleResultContext.setResponseBodyLength(this.influxDBConfig.getResponseBodyLength());
                var sampleResultPointProvider = new SampleResultPointProvider(sampleResultContext);

                Point resultPoint = sampleResultPointProvider.getPoint();
                InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).collectData(resultPoint);
            }
        }
    }
    */

   //START Replaced with RHILL 23/01/2026
private void addSampleAndAllSubSamples(SampleResult sampleResult, List<SampleResult> allSamples) {
    allSamples.add(sampleResult);
    for (SampleResult subResult : sampleResult.getSubResults()) {
        addSampleAndAllSubSamples(subResult, allSamples);
    }
}

@Override
public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
    List<SampleResult> allSampleResults = new ArrayList<>();

    for (SampleResult sampleResult : sampleResults) {
        if (recordSubSamples) {
            addSampleAndAllSubSamples(sampleResult, allSampleResults);
        } else {
            allSampleResults.add(sampleResult);
        }
    }

    for (SampleResult sampleResult : allSampleResults) {
        getUserMetrics().add(sampleResult);

        // Logic: If it's an HTTP request, it's a "request" otherwise, it's a "transaction".
        String samplerType = (sampleResult instanceof HTTPSampleResult) ? "request" : "transaction";

        // ✅ Apply regex-based filtering (e.g., to exclude "bzm - Parallel Controller")
        if ((regexForSamplerList != null && sampleResult.getSampleLabel().matches(regexForSamplerList))
                || samplersToFilter.contains(sampleResult.getSampleLabel())) {

            SampleResultPointContext sampleResultContext = new SampleResultPointContext();
            sampleResultContext.setRunId(this.runId);
            sampleResultContext.setTestName(this.testName);
            sampleResultContext.setNodeName(this.nodeName);
            sampleResultContext.setSampleResult(sampleResult);
            sampleResultContext.setSamplerType(samplerType);
            sampleResultContext.setTimeToSet(System.currentTimeMillis() * ONE_MS_IN_NANOSECONDS + this.getUniqueNumberForTheSamplerThread());
            sampleResultContext.setErrorBodyToBeSaved(context.getBooleanParameter(KEY_INCLUDE_BODY_OF_FAILURES, false));
            sampleResultContext.setResponseBodyLength(this.influxDBConfig.getResponseBodyLength());

            SampleResultPointProvider sampleResultPointProvider = new SampleResultPointProvider(sampleResultContext);
            Point resultPoint = sampleResultPointProvider.getPoint();

            InfluxDatabaseClient.getInstance(this.influxDBConfig, LOGGER).collectData(resultPoint);
        }
    }
}

    //END Replaced with RHILL 23/01/2026

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument(KEY_TEST_NAME, "Test");
        arguments.addArgument(KEY_NODE_NAME, "Test-Node");
        arguments.addArgument(KEY_RUN_ID, "R001");
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_URL, InfluxDBConfig.DEFAULT_INFLUXDB_URL);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_TOKEN, InfluxDBConfig.DEFAULT_INFLUX_DB_TOKEN);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_ORG, InfluxDBConfig.DEFAULT_INFLUX_DB_ORG);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_BUCKET, InfluxDBConfig.DEFAULT_BUCKET);
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_FLUSH_INTERVAL, Integer.toString(InfluxDBConfig.DEFAULT_INFLUX_DB_FLUSH_INTERVAL));
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_MAX_BATCH_SIZE, Integer.toString(InfluxDBConfig.DEFAULT_INFLUX_DB_MAX_BATCH_SIZE));
        arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_THRESHOLD_ERROR, Integer.toString(InfluxDBConfig.DEFAULT_THRESHOLD_ERROR));
        arguments.addArgument(KEY_SAMPLERS_LIST, ".*");
        arguments.addArgument(KEY_USE_REGEX_FOR_SAMPLER_LIST, "true");
        arguments.addArgument(KEY_RECORD_SUB_SAMPLES, "true");
        arguments.addArgument(KEY_INCLUDE_BODY_OF_FAILURES, "true");
        arguments.addArgument(InfluxDBConfig.KEY_RESPONSE_BODY_LENGTH, Integer.toString(InfluxDBConfig.DEFAULT_RESPONSE_BODY_LENGTH));

        return arguments;
    }

    @Override
    public void setupTest(BackendListenerContext context) {
        this.testName = context.getParameter(KEY_TEST_NAME, "Test");
        this.runId = context.getParameter(KEY_RUN_ID, "R001"); //Will be used to compare performance of R001, R002, etc of 'Test'
        this.randomNumberGenerator = new SecureRandom ();
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

        this.scheduledFuture = this.scheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);

        // Indicates whether to write sub sample records to the database
        this.recordSubSamples = Boolean.parseBoolean(context.getParameter(KEY_RECORD_SUB_SAMPLES, "false"));
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        LOGGER.info("Shutting down influxDB scheduler...");

        // Stop importing data by timer.
        this.timer.cancel();

        this.scheduledFuture.cancel(false);
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
            LOGGER.info("influxDB scheduler terminated!");
            if(!this.scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.info("Threads didn't finish in 30 seconds!");
            }
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

                LOGGER.debug("Running the timer: " + LocalDate.now());
                InfluxDatabaseClient.getInstance(conf, logger).importData();

            }
        }, 0, conf.getInfluxdbFlushInterval());
    }
}
