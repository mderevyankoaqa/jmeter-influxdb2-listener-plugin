# JMeter InfluxDB v2.0 listener plugin
![](img/logo2.png) <img src="https://badges.frapsoft.com/os/v3/open-source.svg?v=103" width="500">


<p align="left">
<img src="https://img.shields.io/github/license/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<img src ="https://img.shields.io/github/v/release/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<img src="https://img.shields.io/maven-central/v/io.github.mderevyankoaqa/jmeter-plugins-influxdb2-listener?style=plastic">
<img src="https://img.shields.io/github/downloads/mderevyankoaqa/jmeter-influxdb2-listener-plugin/total?style=plastic">
<img src="https://img.shields.io/github/stars/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic&color=gold">
<img src="https://img.shields.io/github/forks/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<br>
<img src="https://img.shields.io/github/repo-size/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<img src="https://img.shields.io/github/issues/detail/age/mderevyankoaqa/jmeter-influxdb2-listener-plugin/1?style=plastic&color=purple">
<img src="https://img.shields.io/github/commit-activity/m/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic&color=red">
<img src="https://img.shields.io/github/contributors/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<img src="https://img.shields.io/github/last-commit/mderevyankoaqa/jmeter-influxdb2-listener-plugin?style=plastic">
<img src="https://badgen.net/github/closed-issues/mderevyankoaqa/jmeter-influxdb2-listener-plugin">
</p>

<h3> 

<summary>Support project ❤️ </summary>
 <details>
 <a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H8L62WL5HVD32">
  <img src="https://raw.githubusercontent.com/stefan-niedermann/paypal-donate-button/master/paypal-donate-button.png" alt="Donate with PayPal" />
</a>
</details>
</h3> 

<h3>
<summary>Like what you see? 😍 </summary>
<br>
   <a href="https://www.buymeacoffee.com/mvderevyanko">
   <img alt="Coffee2" src="https://raw.githubusercontent.com/mderevyankoaqa/jmeter-elk/main/img/Coffee2.png"  width="150">
</a>
</h3>

## Description
The goal of the project is to make a quite similar online dashboard in the same way as JMeter generates. Supported the latest InfluxDB v2.0 and created appropriate dashboard ("Flux" language has been used to create the queries - now there are a lot fo capacities to build amazing charts, tables with a lot of math function).
So that it would be possible to build the monitor hardware solution on the latest InfluxDB v2.0 and telegraf (agent to send the hardware metrics to InfluxDB) as well.

The plugin sends metrics to InfluxDB and provides the possibility to visualize the charts in Grafana, have the Aggregate report as JMeter creates. Added the possibly to save the following extra fields in the database:
* Response code;
* Error message;
* Response body of the failed requests (can be configured);
* Connect time;
* Latency;
* The response time (uses from the SampleResult.class, needs to make aggregate report).

Notes: if you need to save the errors you got wile the test to csv like file and then share to the dev team you can use the [jmeter-csv-listener-plugin](https://github.com/mderevyankoaqa/jmeter-csv-listener-plugin).
This plugin can be used while the functional testing and load tests as well.

## Important notes
🚨 The plugin allows 5 errors happened one by one, then plugin will stop importing the results after that! See details in the logs.
Counter will be refreshed at least you have 4 fails. This is protection to avoid OOM error.
The option can be configured in the settings (the key name is `influxDBThresholdError` see the [Plugin configuration](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/tree/main#plugin-configuration) for the details). You need to be careful with that option and know the hardware resources to store data in the memory.

Pleas monitor the elapsed time of the data importing (see logs) to avoid issues with requests sending from JMeter.
Pay attention on "Sending metrics performance tuning" chapter, see below.


## Compatibility
The supported versions:
* 🚨 Java 11 - make sure that you have it (its minimum version); otherwise errors will occur while plugin loading.
* InfluxDB v2.x, see release notes: https://docs.influxdata.com/influxdb/v2.0/reference/release-notes/influxdb/  (1.8 is not supported)
* JMeter 5.6.3 only.

* The current board and plugin were tested on Grafana 11.1.3 and InfluxDB 2.7.8, JAVA 21.0.1.

## Maven Support
Please see the latest release here https://search.maven.org/artifact/io.github.mderevyankoaqa/jmeter-plugins-influxdb2-listener.

## CI/CD Support
The CI/CD can be done using [jmeter-maven-plugin](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin)
The example of the adding plugin to the project with the jmeter-maven:

          <configuration>
                <jmeterExtensions>
                  <artifact>io.github.mderevyankoaqa:jmeter-plugins-influxdb2-listener:2.8</artifact>
                </jmeterExtensions>
            </configuration>
Some notes cab be found in the article [Adding jar's to the /lib/ext directory](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki/Adding-Excluding-libraries-to-from-the-classpath).

## Development and branching strategy
Hello all contributors! Welcome to the project, I'm happy to see you here. Just to avoid the mess and have a nice history, let's keep the simple rules:
1. Code should pass standard static code analysis in IntelliJ IDEA.
2. Comments should be for the new code to have clear java docs.
3. All new development lets - in the "development" branch. So the steps:
   a. make a branch for the feature you want to develop from "development" (source branch -> "main"). There will be a mirror of the latest release + can be a merge of the new features.
   b. all pull requests from your feature branch -> "development" branch only.
   c. once we decide to make the new release after testing, we will merge "development" -> "main" with push to maven central.

While the first pull request please add your self to the [build.gradle](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/blob/main/build.gradle) the section 'developers'

    developers {
        developer {
        id = 'your github if'
        name = 'your name'
        email = 'email'
        }

## Deployment
* Put '`jmeter-plugin-influxdb2-listener-<version>.jar`' file from [Releases](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/releases) to `~<JmeterPath<\lib\ext`;

Note: "fatJar" gradle task should be used to create the package for the JMeter.

![](img/deploy1.png)

* Run JMeter and select the test plan, Add-> Listener -> Backend Listener.

  ![](img/deploy2.png)

* Go to Backend Listener

  ![](img/testPlan.png)

* Select from the dropdown item with the name '`io.github.mderevyankoaqa.influxdb2.visualizer.InfluxDatabaseBackendListenerClient`'.

  ![](img/deploy3.png)

## Upgrade
* Close JMeter if its started.
* Remove old jar.
* Put '`jmeter-plugin-influxdb2-listener-<version>.jar`' file from [Releases](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/releases) to `~<JmeterPath<\lib\ext`;
* Run JMeter again and got Listener.
* Select from the dropdown item with the name '`io.github.mderevyankoaqa.influxdb2.visualizer.InfluxDatabaseBackendListenerClient`'.

  ![](img/deploy3.png)

* Click 'Clear all' button

  ![](img/deploy6.png)
* Set right settings (update all properties).

## InfluxDB configuration
* Create Bucket to store the further testing results.

  ![](img/influx1.png)

* Create the token with read and write rights.
  ![](img/influx2.png)

## Plugin configuration
Let’s explain the plugin fields:
* `testName` - the name of the test.
* `nodeName` - the name of the server.
* `runId` - the identification number of hte test run, can be dynamic.
* `influxDBURL` - InfluxDB server URL [protocol://][host][:port], protocol (can be http or https) and the default port is 8086.
* `influxDBToken` - the influxdb bucket token, the default value should be updated, copy it from InfluxDB site.

  ![](img/influx3.png)

* `influxDBOrganization` - the influxdb bucket organization, the default value should be updated, copy it from InfluxDB site.

  ![](img/influx4.png)

* `influxDBFlushInterval` - its interval to send data to InfluxDB, the default value is 4000 (4 seconds).
* `influxDBMaxBatchSize` - the max size of the batch with metrics, the default 2000 (2000 items of JMeter results).
* `influxDBThresholdError` - the error threshold before stopping the import, the default value is 5. (see [Important notes](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/tree/main#important-notes) for more detail.)
* `influxDBBucket` - the InfluxDB bucket name to store the test results.
* `samplersList` - the regex value to sort out the JMeter samplers results; the default is _`.*`_. For example if you have the pattern of JMeter test plan development like this - create the 'Transaction controller', add inside of the 'Transaction controller' the Sampler with request, and the name pattern '`GET->Something`', like on the see screen below.
  The regex `^(Home Page|Login|Search)(-success|-failure)?$` can be used to save only samplers names. The regex can be generated from JMeter menu.

  ![](img/deploy4.png)

You can modify the generated string in terms of your purposes.

![](img/deploy5.png)

* `useRegexForSamplerList` - allows to use the regexps if set to 'true'.
* `recordSubSamples` - allows to save the JMeter sub samples if set to 'true'.
* `saveResponseBodyOfFailures` - allows to save the response body of the failures.
* `responseBodyLength` - allows saving the response body, not more than the set specified length.

## Sending metrics performance tuning
The plugin imports batch with JMeter results each 4 seconds (by default settings). In the logs you will see records like this:
`INFO o.m.j.i.v.InfluxDatabaseBackendListenerClient: Data has been imported successfully, batch size --> 68, elapsed time is --> 14 ms` (the elapsed time is the response time of the batch importing.)
So you can control the flush interval depends on the load you have and adjust `influxDBFlushInterval` setting. Is not recommended having less 1 second.  
Max batch protection -> send data when batch max size is occurred. For example, when batch size is 2000 items (it's the default setting of `influxDBMaxBatchSize`) plugin imports that batch, even when flush interval was not occurred.
Using both options you can tune data importing and have optimal performance.

Make sure you have enough ram to aggregate huge batch and optimal flush period.

Notes: when test has been interrupted from UI; the processes may not be finished properly, restart JMeter.


## Grafana dashboard capabilities
See deployment instructions here https://grafana.com/grafana/dashboards/13644

Dashboard helps:
* Filter the results by Run Id or Type (can be requests or transactions).

![](img/filterById.png)  
![](img/filterByType.png)

* Monitor throughput with active users.
  ![](img/grafana1.png)

* Overview and analise the response time, distribution as well. Added the filters to see requests with status "pass", "fail".
  ![](img/grafana2.png)
  ![](img/grafana3.png)


* See aggregate report.
  The table rendering may take an extra time. The table has hardware resources consuming queries from Influxdb side. If you have low hardware on the Influxdb server - recommended make the clone of the original dashboard and remove aggregate report.
  So the idea - it's to have one 'fast' dashboard for the online monitoring (has no aggregate report) to see the results while the test, the second (original with aggregate report) to see the final results.
  ![](img/grafana4.0.png)

  Now added the possibility to see the aggregate report for requests with status "pass" only.
  ![](img/grafana4.1.png)


* Investigate errors. The table is interactive, it's possible to filter data in the columns and see details for the specific error. Added the paging.
  ![](img/grafana5.png)


* See network statistics, latency, processing time.
  ![](img/grafana6.0.png)
  ![](img/grafana6.1.png)
  ![](img/grafana6.2.png)


* Check individual request details.
  ![](img/grafana7.png)