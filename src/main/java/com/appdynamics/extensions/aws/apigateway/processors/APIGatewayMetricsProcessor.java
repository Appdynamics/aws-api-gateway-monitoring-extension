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
import com.appdynamics.extensions.aws.apigateway.ApiNamePredicate;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.*;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by venkata.konala on 4/23/18.
 */
public class APIGatewayMetricsProcessor implements MetricsProcessor {

    private static final Logger logger = Logger.getLogger(APIGatewayMetricsProcessor.class);
    private static final String NAMESPACE = "AWS/ApiGateway";
    private static final String APINAME = "ApiName";
    private List<IncludeMetric> includeMetrics;
    private String apiName;

    public APIGatewayMetricsProcessor(List<IncludeMetric> includeMetrics, String apiName){
        this.includeMetrics = includeMetrics;
        this.apiName = apiName;
    }

    @Override
    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        /*TODO Not required*/
        ApiNamePredicate apiNamePredicate = new ApiNamePredicate(apiName);
        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter, NAMESPACE, includeMetrics, dimensionFilters);
    }

    private List<DimensionFilter> getDimensionFilters(){
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();
        DimensionFilter apiNameDimensionFilter = new DimensionFilter();
        apiNameDimensionFilter.withName(APINAME);
        dimensionFilters.add(apiNameDimensionFilter);
        return dimensionFilters;
    }

    @Override
    public StatisticType getStatisticType(AWSMetric metric) {
        return MetricsProcessorHelper.getStatisticType(metric.getIncludeMetric(), includeMetrics);
    }

    @Override
    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        List<Metric> stats = Lists.newArrayList();
        if(namespaceMetricStats != null){
            for(AccountMetricStatistics accountMetricStatistics : namespaceMetricStats.getAccountMetricStatisticsList()){
                for(RegionMetricStatistics regionMetricStatistics : accountMetricStatistics.getRegionMetricStatisticsList()){
                    for (MetricStatistic metricStatistic : regionMetricStatistics.getMetricStatisticsList()){
                        String metricPath = createMetricPath(accountMetricStatistics.getAccountName(), regionMetricStatistics.getRegion(), metricStatistic);
                        if(metricStatistic.getValue() != null){
                            Map<String, Object> metricProperties = Maps.newHashMap();
                            AWSMetric awsMetric = metricStatistic.getMetric();
                            IncludeMetric includeMetric = awsMetric.getIncludeMetric();
                            metricProperties.put("alias", includeMetric.getAlias());
                            metricProperties.put("multiplier", includeMetric.getMultiplier());
                            metricProperties.put("aggregationType", includeMetric.getAggregationType());
                            metricProperties.put("timeRollUpType", includeMetric.getTimeRollUpType());
                            metricProperties.put("clusterRollUpType ", includeMetric.getClusterRollUpType());
                            metricProperties.put("delta", includeMetric.isDelta());
                            Metric metric = new Metric(includeMetric.getName(), Double.toString(metricStatistic.getValue()), metricStatistic.getMetricPrefix() + metricPath, metricProperties);
                            stats.add(metric);
                        }
                        else{
                            logger.debug(String.format("Ignoring metric [ %s ] which has null value", metricPath));
                        }
                    }
                }
            }
        }
        return stats;
        /*Map<String, String> dimensionToMetricPathNameDictionary = Maps.newHashMap();
        dimensionToMetricPathNameDictionary.put(APINAME, "API Name");
        return MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats, dimensionToMetricPathNameDictionary, false);*/
    }

    private String createMetricPath(String accountName, String region, MetricStatistic metricStatistic){
        AWSMetric awsMetric = metricStatistic.getMetric();
        IncludeMetric includeMetric = awsMetric.getIncludeMetric();
        com.amazonaws.services.cloudwatch.model.Metric metric = awsMetric.getMetric();
        String apiName = metric.getDimensions().get(0).getValue();
        StringBuilder stringBuilder = new StringBuilder(accountName)
                .append("|")
                .append(region)
                .append("|")
                .append("API")
                .append("|")
                .append(apiName)
                .append(includeMetric.getName());
        return stringBuilder.toString();

    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }


}
