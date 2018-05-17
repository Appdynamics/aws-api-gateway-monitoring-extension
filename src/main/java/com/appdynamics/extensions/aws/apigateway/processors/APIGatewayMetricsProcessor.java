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

package com.appdynamics.extensions.aws.apigateway.processors;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by venkata.konala on 4/23/18.
 */
public class APIGatewayMetricsProcessor implements MetricsProcessor {

    private static final Logger logger = Logger.getLogger(APIGatewayMetricsProcessor.class);
    private static final String NAMESPACE = "AWS/ApiGateway";
    private List<IncludeMetric> includeMetrics;
    private String apiName;

    public APIGatewayMetricsProcessor(List<IncludeMetric> includeMetrics, String apiName){
        this.includeMetrics = includeMetrics;
        this.apiName = apiName;
    }

    @Override
    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {

        List<DimensionFilter> dimensionFilters = getDimensionFilters();

        return MetricsProcessorHelper.getFilteredMetrics();
    }

    private List<DimensionFilter> getDimensionFilters(){
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();

        DimensionFilter apiNameDimensionFilter = new DimensionFilter();
        apiNameDimensionFilter.withName("ApiName");
        if(!Strings.isNullOrEmpty(apiName)){
            apiNameDimensionFilter.withName(apiName);
        }
        dimensionFilters.add(apiNameDimensionFilter);

        return dimensionFilters;
    }

    @Override
    public StatisticType getStatisticType(AWSMetric metric) {
        return null;
    }

    @Override
    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        return null;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }


}
