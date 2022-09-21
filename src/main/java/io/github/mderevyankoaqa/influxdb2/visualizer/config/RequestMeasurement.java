package io.github.mderevyankoaqa.influxdb2.visualizer.config;

/**
 * Constants (Tag, Field, Measurement) names for the requests measurement. *
 * @author Alexander Wert
 * @author Michael Derevyanko (minor changes and improvements)
 */
public interface RequestMeasurement {

	/**
	 * Measurement name.
	 */
	String MEASUREMENT_NAME = "requestsRaw";

	/**
	 * Tags.
	 *
	 * @author Alexander Wert
	 * @author Michael Derevyanko (minor changes and improvements)
	 */
	interface Tags {
		/**
		 * Request name tag.
		 */
		String REQUEST_NAME = "requestName";

		/**
		 * Influx DB tag for a unique identifier for each execution(aka 'run') of a load test.
		 */
		String RUN_ID = "runId";

		/**
		 * Test name field.
		 */
		String TEST_NAME = "testName";

		/**
		 * Node name field.
		 */
		String NODE_NAME = "nodeName";

		/**
		 * Response code field.
		 */
		String RESULT_CODE = "responseCode";

		/**
		 * Error message.
		 */
		String ERROR_MSG = "errorMessage";

		/**
		 * Error response body.
		 */
		String ERROR_RESPONSE_BODY = "errorResponseBody";

		/**
		 * The result, can be pass or fail.
		 */
		String RESULT = "result";

		/**
		 * sampleType represents the type of sample, whether it is a request or a transaction controller.
		 */
		String SAMPLE_TYPE = "sampleType";
	}

	/**
	 * Fields.
	 *
	 * @author Alexander Wert
	 */
	interface Fields {
		/**
		 * Response time field.
		 */
		String RESPONSE_TIME = "responseTime";

		/**
		 * Error count field.
		 */
		String ERROR_COUNT = "errorCount";

		/**
		 * Error count field.
		 */
		String REQUEST_COUNT = "count";

		/**
		 * Sent Bytes field.
		 */
		String SENT_BYTES = "sentBytes";

		/**
		 * Received Bytes field.
		 */
		String RECEIVED_BYTES = "receivedBytes";

		/**
		 * Latency field.
		 */
		String LATENCY = "latency";

		/**
		 * Connect Time field.
		 */
		String CONNECT_TIME = "connectTime";

		/**
		 * Processing Time field.
		 */
		String PROCESSING_TIME = "processingTime";
	}
}
