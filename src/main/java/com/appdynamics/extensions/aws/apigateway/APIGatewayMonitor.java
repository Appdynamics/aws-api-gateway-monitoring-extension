/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.aws.apigateway;

import com.appdynamics.extensions.aws.SingleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.apigateway.configuration.APIGatewayConfiguration;
import com.appdynamics.extensions.aws.apigateway.processors.APIGatewayMetricsProcessor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import org.apache.log4j.Logger;


/**
 * Created by venkata.konala on 4/23/18.
 */
public class APIGatewayMonitor extends SingleNamespaceCloudwatchMonitor<APIGatewayConfiguration>{

    private static final Logger logger = Logger.getLogger(APIGatewayMonitor.class);

    public APIGatewayMonitor(){
        super(APIGatewayConfiguration.class);
    }

    @Override
    protected NamespaceMetricStatisticsCollector getNamespaceMetricsCollector(APIGatewayConfiguration apiGatewayConfiguration) {

        MetricsProcessor metricsProcessor = createMetricsProcessor(apiGatewayConfiguration);
        return new NamespaceMetricStatisticsCollector.Builder(apiGatewayConfiguration.getAccounts(),
                apiGatewayConfiguration.getConcurrencyConfig(),
                apiGatewayConfiguration.getMetricsConfig(),
                metricsProcessor,
                getContextConfiguration().getMetricPrefix())
                .withCredentialsDecryptionConfig(apiGatewayConfiguration.getCredentialsDecryptionConfig())
                .withProxyConfig(apiGatewayConfiguration.getProxyConfig())
                .build();
    }

    private MetricsProcessor createMetricsProcessor(APIGatewayConfiguration apiGatewayConfiguration){
        return new APIGatewayMetricsProcessor(apiGatewayConfiguration.getMetricsConfig().getIncludeMetrics(), apiGatewayConfiguration.getApiName());
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return "Custom Metrics|AWS API Gateway";
    }

    @Override
    public String getMonitorName() {
        return "APIGatewayMonitor";
    }

    @Override
    protected int getTaskCount() {
        return 3;
    }
}
