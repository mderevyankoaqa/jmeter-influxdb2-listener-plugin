package io.github.mderevyankoaqa.influxdb2.visualizer.result;

import org.apache.jmeter.samplers.SampleResult;

/**
 * The data object with parameters to create Result {@link com.influxdb.client.write.Point}.
 * @author Michael Derevyanko
 */
public class SampleResultPointContext {

    private long timeToSet;
    private SampleResult sampleResult;
    private String nodeName;
    private String runId;
    private String testName;
    boolean errorBodyToBeSaved;
    private int ResponseBodyLength;
    private String samplerType;

    /**
     * Checks whether the body of the failed repose is going to be saved.
     * @return true to save body; otherwise false to skip saving.
     */
    public boolean isErrorBodyToBeSaved() {
        return errorBodyToBeSaved ;
    }

    /**
     * Sets the option to save failed repose body.
     * @param flag represents the option.
     */
    public void setErrorBodyToBeSaved(boolean flag) {
        this.errorBodyToBeSaved = flag;
    }

    /**
     * Gets the run id, set in jmeter option.
     * @return returns run id.
     */
    public String getRunId() {
        return this.runId;
    }

    /**
     * Sets run id.
     * @param runId represents the run id.
     */
    public void setRunId(String runId) {
        this.runId = runId;
    }

    /**
     * Gets the node name to sort out the PC from what test has been started.
     * @return the node name.
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * Sets the node name.
     * @param nodeName the node name.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Gets the {@link SampleResult}.
     * @return sample result.
     */
    public SampleResult getSampleResult() {
        return this.sampleResult;
    }

    /**
     * Sets {@link SampleResult}.
     * @param sampleResult {@link SampleResult}
     */
    public void setSampleResult(SampleResult sampleResult) {
        this.sampleResult = sampleResult;
    }

    /**
     * Gets the time frame of the further {@link com.influxdb.client.write.Point}.
     * @return time represented in number.
     */
    public long getTimeToSet() {
        return this.timeToSet;
    }

    /**
     * Sets time of the further {@link com.influxdb.client.write.Point}.
     * @param timeToSet the time represented in number.
     */
    public void setTimeToSet(long timeToSet) {
        this.timeToSet = timeToSet;
    }

    /**
     * Gets the test name.
     * @return the test name.
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Sets the test name.
     * @param testName the test name.
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * Gets the response body length.
     * @return tge response body length.
     */
    public int getResponseBodyLength() {
        return ResponseBodyLength;
    }

    /**
     * Sets the response body length.
     * @param responseBodyLength response body length.
     */
    public void setResponseBodyLength(int responseBodyLength) {
        this.ResponseBodyLength = responseBodyLength;
    }

    /**
     * Sets samplerType.
     * @param samplerType represents the type of sample, whether it is a request or a transaction controller.
     */
    public void setSamplerType(String samplerType) {
        this.samplerType = samplerType;
    }

    /**
     * Gets the SamplerType.
     * @return returns SamplerType.
     */
    public String getSamplerType() {
        return this.samplerType;
    }
}
