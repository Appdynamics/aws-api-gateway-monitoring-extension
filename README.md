# AWS API Gateway Monitoring Extension

## Use Case
Captures statistics for APIs in the API Gateway from Amazon CloudWatch and displays them in the AppDynamics Metric Browser.

## Prerequisites
1. Please give the following permissions to the account being used to with the extension.<br/>
   1. **cloudwatch:ListMetrics**
   2. **cloudwatch:GetMetricStatistics**
2. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.
3. The extension needs to be able to connect to AWS Cloudwatch in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.

<p>** Note: Running the extension with Machine Agent version 4.5.13 or later.</p>
<ol>
<li>
<p>If you are seeing warning messages while starting the Machine Agent, update the http-client and http-core JARs in <code>{MACHINE_AGENT_HOME}/monitorsLibs</code> to <code>httpclient-4.5.9</code> and <code>httpcore-4.4.12</code> to make this warning go away.</p>
</li>
<li>
<p>To make this extension work on Machine Agent &lt; 4.5.13, the http-client and http-core JARs in <code>{MACHINE_AGENT_HOME}/monitorsLibs</code> need to be updated to <code>httpclient-4.5.9</code> and <code>httpcore-4.4.12</code>.</p>
</li>
</ol>

## Installation
1. Run 'mvn clean install' from aws-api-gateway-monitoring-extension
2. Copy and unzip AWSAPIGatewayMonitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yml file in AWSAPIGatewayMonitor and provide the required configuration (see Configuration section).
4. Restart the Machine Agent.

Please place the extension in the "**monitors**" directory of your Machine Agent installation directory. Do not place the extension in the "**extensions**" directory of your Machine Agent installation directory.

## Configuration
In order to use the extension, you need to update the config.yml file that is present in the extension folder. The following is a step-by-step explanation of the configurable fields that are present in the config.yml file.

1. If SIM is enabled, then use the following metricPrefix
        ```
        metricPrefix: "Custom Metrics|AWS APIGateway"
        ```
   Else, configure the "COMPONENT_ID" under which the metrics need to be reported. This can be done by changing the value of `<COMPONENT_ID>` in
        metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|AWS APIGateway|".

        For example,
        ```
        metricPrefix: "Server|Component:100|Custom Metrics|AWS APIGateway|"
        ```
More details around metric prefix can be found [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695).

2. Provide accessKey(required) and secretKey(required) of our account(s), also provide displayAccountName(any name that represents your account) and
   regions(required). If you are running this extension inside an EC2 instance which has IAM profile configured then you don't have to configure these values,
   extension will use IAM profile to authenticate.
   ~~~
   accounts:
     - awsAccessKey: "XXXXXXXX1"
       awsSecretKey: "XXXXXXXXXX1"
       displayAccountName: "TestAccount_1"
       regions: ["us-east-1","us-west-1","us-west-2"]

     - awsAccessKey: "XXXXXXXX2"
       awsSecretKey: "XXXXXXXXXX2"
       displayAccountName: "TestAccount_2"
       regions: ["eu-central-1","eu-west-1"]
   ~~~

3. Provide the list of Api names that needs to be monitored. This list accepts regular expressions.
   
   ~~~
   apiNames: ["api1", "api2"]
   ~~~   

## Metrics

   1. 4XXError            -        The number of client-side errors captured in a specified period.
   2. 5XXError            -        The number of server-side errors captured in a given period.
   3. CacheHitCount       -        The number of requests served from the API cache in a given period.
   4. CacheMissCount      -        The number of requests served from the back end in a given period, when API caching is enabled.
   5. Count               -        The total number API requests in a given period.
   6. IntegrationLatency  -        The time between when API Gateway relays a request to the back end and when it receives a response from the back end.
   7. Latency             -        The time between when API Gateway receives a request from a client and when it returns a response to the client. The latency includes the integration latency and other API Gateway overhead.

   Apart from the above metric, we also have a metric called "API calls", that gives out the number of cloudwatch API calls from the extension.
   
## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench

Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting

Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension.

## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/aws-api-gateway-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.0.6       |
|Last Update               |28/05/2021|
|Changes list              |[ChangeLog](https://github.com/Appdynamics/aws-api-gateway-monitoring-extension/blob/master/CHANGELOG.md)|

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
