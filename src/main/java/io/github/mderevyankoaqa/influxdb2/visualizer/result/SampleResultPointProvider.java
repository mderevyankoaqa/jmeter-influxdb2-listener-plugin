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
    private static final String NO_DATA = "noData";

    /**
     * Creates the new instance of the {@link SampleResultPointProvider}.
     * @param sampleResultContext the {@link SampleResultPointContext}.
     */
    public SampleResultPointProvider(SampleResultPointContext sampleResultContext) {
        this.sampleResultContext = sampleResultContext;
    }

    /**
     * Gets {@link Point}, returns the OK or KO jmeter point, depends from the sample result.
     * @return {@link Point} to save.
     */
    public Point getPoint() {
        Point point = this.getDefaultPoint();

        if (this.sampleResultContext.getSampleResult().getErrorCount() == 0) {
            // Marks the result point as passed.
            point.addTag(RequestMeasurement.Tags.RESULT, "pass" );
        } else {
            // Marks the result point as failed.
            point.addTag(RequestMeasurement.Tags.RESULT, "fail" );
        }
        return point;
    }

    /**
     * Gets the error body to be saved in the point.
     * @param isToBeSaved set to true if body need to be saved; otherwise false.
     * @return the normalized string if parameter @param isToBeSaved set to true; 'noData' string otherwise.
     */
     private String getErrorBodyToBeSaved(boolean isToBeSaved, boolean isSuccessful)
     {
         String errorResponseBody;

         if (isToBeSaved && isSuccessful)
         {
             errorResponseBody = this.getErrorBody();
         }
         else
         {
             errorResponseBody = NO_DATA;
         }

         return errorResponseBody;
     }

    /**
     * Gets the assertion failure message.
     * @return  first non null assertion failure message if assertionResults is not null, 'noData' string otherwise.
     */
     private String getAssertionFailure()
     {
         String assertionMsg = this.sampleResultContext.getSampleResult().getFirstAssertionFailureMessage();

         if(assertionMsg == null)
         {
             assertionMsg = NO_DATA;
         }
         return assertionMsg;
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
            return  InfluxDatabaseUtility.getSubstring(InfluxDatabaseUtility.getEscapedString(errorBody),
                    this.sampleResultContext.getResponseBodyLength());
        }

        return NO_DATA;
    }



    /**
     * Builds the OK jmeter {@link Point}.
     * @return OK jmeter {@link Point}.
     */
    private Point getDefaultPoint() {

        return Point.measurement(RequestMeasurement.MEASUREMENT_NAME).time(this.sampleResultContext.getTimeToSet(), WritePrecision.NS)
                .addTag(RequestMeasurement.Tags.REQUEST_NAME, this.sampleResultContext.getSampleResult().getSampleLabel())
                .addTag(RequestMeasurement.Tags.RUN_ID, this.sampleResultContext.getRunId())
                .addTag(RequestMeasurement.Tags.TEST_NAME, this.sampleResultContext.getTestName())
                .addTag(RequestMeasurement.Tags.NODE_NAME, this.sampleResultContext.getNodeName())
                .addTag(RequestMeasurement.Tags.RESULT_CODE, this.sampleResultContext.getSampleResult().getResponseCode())
                .addTag(RequestMeasurement.Tags.ERROR_MSG, this.getAssertionFailure())
                .addTag(RequestMeasurement.Tags.SAMPLE_TYPE, this.sampleResultContext.getSamplerType())
                .addTag(RequestMeasurement.Tags.ERROR_RESPONSE_BODY, this.getErrorBodyToBeSaved(this.sampleResultContext.isErrorBodyToBeSaved(), !this.sampleResultContext.getSampleResult().isSuccessful()))
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
