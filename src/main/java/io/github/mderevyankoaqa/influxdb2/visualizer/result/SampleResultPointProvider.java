package io.github.mderevyankoaqa.influxdb2.visualizer.result;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.github.mderevyankoaqa.influxdb2.visualizer.config.RequestMeasurement;
import io.github.mderevyankoaqa.influxdb2.visualizer.influxdb.client.InfluxDatabaseUtility;

/**
 * The provider of the Influxdb {@link Point} based on the sample result.
 * @author Michael Derevyanko
 */
public class SampleResultPointProvider {

    private final SampleResultPointContext sampleResultContext;
    private final String assertionFailureMessage;
    private Point errorPoint;

    /**
     * Creates the new instance of the {@link SampleResultPointProvider}.
     * @param sampleResultContext the {@link SampleResultPointContext}.
     */
    public SampleResultPointProvider(SampleResultPointContext sampleResultContext) {
        this.sampleResultContext = sampleResultContext;

        this.assertionFailureMessage = this.sampleResultContext.getSampleResult().getFirstAssertionFailureMessage();
    }

    /**
     * Gets {@link Point}, returns the OK or KO jmeter point, depends from the sample result.
     * @return {@link Point} to save.
     */
    public Point getPoint() {

        if (this.assertionFailureMessage == null) {
            return this.getOKPoint();
        } else {
            return this.getErrorPoint();
        }
    }

    /**
     * Gets KO jmeter {@link Point}, saves the assertion message and response error body - depends from the settings.
     * @return KO jmeter {@link Point}.
     */
    private Point getErrorPoint() {

        if (this.sampleResultContext.isErrorBodyToBeSaved()) {
            this.errorPoint = this.getOKPoint()
                    .addTag(RequestMeasurement.Tags.ERROR_MSG, this.assertionFailureMessage)
                    .addTag(RequestMeasurement.Tags.ERROR_RESPONSE_BODY, this.getErrorBody());

        }

        if (!this.sampleResultContext.isErrorBodyToBeSaved()) {
            this.errorPoint = this.getOKPoint()
                    .addTag(RequestMeasurement.Tags.ERROR_MSG, this.assertionFailureMessage);


        }

        return this.errorPoint;
    }

    /**
     * Gets error body.
     * @return returns body of the failed response.
     */
    private String getErrorBody()
    {
        String errorBody = this.sampleResultContext.getSampleResult().getResponseDataAsString();
        if(errorBody != null && !errorBody.isEmpty())
        {
            return  InfluxDatabaseUtility.getEscapedString(errorBody);
        }

        return "ErrorBodyIsEmpty.";
    }

    /**
     * Builds the OK jmeter {@link Point}.
     * @return OK jmeter {@link Point}.
     */
    private Point getOKPoint() {

        return Point.measurement(RequestMeasurement.MEASUREMENT_NAME).time(this.sampleResultContext.getTimeToSet(), WritePrecision.NS)
                .addTag(RequestMeasurement.Tags.REQUEST_NAME, this.sampleResultContext.getSampleResult().getSampleLabel())
                .addTag(RequestMeasurement.Tags.RUN_ID, this.sampleResultContext.getRunId())
                .addTag(RequestMeasurement.Tags.TEST_NAME, this.sampleResultContext.getTestName())
                .addTag(RequestMeasurement.Tags.NODE_NAME, this.sampleResultContext.getNodeName())
                .addTag(RequestMeasurement.Tags.RESULT_CODE, this.sampleResultContext.getSampleResult().getResponseCode())
                .addField(RequestMeasurement.Fields.ERROR_COUNT, this.sampleResultContext.getSampleResult().getErrorCount())
                .addField(RequestMeasurement.Fields.REQUEST_COUNT, this.sampleResultContext.getSampleResult().getSampleCount())
                .addField(RequestMeasurement.Fields.RECEIVED_BYTES, this.sampleResultContext.getSampleResult().getBytesAsLong())
                .addField(RequestMeasurement.Fields.SENT_BYTES, this.sampleResultContext.getSampleResult().getSentBytes())
                .addField(RequestMeasurement.Fields.RESPONSE_TIME, this.sampleResultContext.getSampleResult().getTime())
                .addField(RequestMeasurement.Fields.LATENCY, this.sampleResultContext.getSampleResult().getLatency())
                .addField(RequestMeasurement.Fields.CONNECT_TIME, this.sampleResultContext.getSampleResult().getConnectTime())
                .addField(RequestMeasurement.Fields.PROCESSING_TIME, this.sampleResultContext.getSampleResult().getLatency() - this.sampleResultContext.getSampleResult().getConnectTime());
    }
}
