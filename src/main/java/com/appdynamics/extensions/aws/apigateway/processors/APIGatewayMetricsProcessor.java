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
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.aws.apigateway.ApiNamesPredicate;
import com.appdynamics.extensions.aws.apigateway.EventsServiceMetricsWriter;
import com.appdynamics.extensions.aws.apigateway.configuration.APIGatewayConfiguration;
import com.appdynamics.extensions.aws.apigateway.configuration.EventsService;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.*;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;


import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;


/**
 * Created by venkata.konala on 4/23/18.
 */
public class APIGatewayMetricsProcessor implements MetricsProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(APIGatewayMetricsProcessor.class);
    private static final String NAMESPACE = "AWS/ApiGateway";
    private static final String APINAME = "ApiName";
    private List<IncludeMetric> includeMetrics;
    private List<String> apiNamesList;
    private APIGatewayConfiguration apiGatewayConfiguration;
    private Map<String, ?> eventsService;

    public APIGatewayMetricsProcessor(APIGatewayConfiguration apiGatewayConfiguration){
        this.apiGatewayConfiguration = apiGatewayConfiguration;
        this.includeMetrics = apiGatewayConfiguration.getMetricsConfig().getIncludeMetrics();
        this.apiNamesList = apiGatewayConfiguration.getApiNames();
        this.eventsService = apiGatewayConfiguration.getEventsService();
    }

    @Override
    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        /*The dimension being used here for filtering is "ApiName".
        * Another available dimension is "ApiName" and "Stage".
        * The "ApiName" dimension filter will retrieve metrics with just the
        * "ApiName" dimension as well as the metrics with "ApiName" and "Stage"
        * So there might be redundant metrics. To avoid descrepancy in data,
        * the aggregationType is made "AVERAGE" and it is not exposed to the
        * customers.
        * */
        List<DimensionFilter> dimensionFilters = getDimensionFilters();

        /*The Predicates are used for filtering with the Dimension values.
        * Since the dimension used for filtering is "ApiName", we can filter
        * further with the ApiName values.
        * */
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);

        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter, NAMESPACE, includeMetrics, dimensionFilters, apiNamesPredicate);
    }

    private List<DimensionFilter> getDimensionFilters(){
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();

        DimensionFilter apiNameDimensionFilter = new DimensionFilter();
        apiNameDimensionFilter.withName(APINAME);
        dimensionFilters.add(apiNameDimensionFilter);

        /*DimensionFilter stageDimensionFilter = new DimensionFilter();
        stageDimensionFilter.withName("Stage");
        dimensionFilters.add(stageDimensionFilter);*/

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
        uploadToEventsServiceIfEnabled(stats);
        return stats;
    }

    private void uploadToEventsServiceIfEnabled(List<Metric> metricList){
        if(eventsService != null){
            EventsServiceMetricsWriter eventsServiceMetricsWriter = EventsServiceMetricsWriter.getEventsServiceMetricsWriter(eventsService);
            ConfigurationMetricsProcessor configurationMetricsProcessor = new ConfigurationMetricsProcessor(apiGatewayConfiguration, eventsServiceMetricsWriter);
            if(eventsService.get("enableTraditionalMetrics") != null && (Boolean) eventsService.get("enableTraditionalMetrics")) {
                eventsServiceMetricsWriter.uploadTraditionalMetrics(metricList);
            }
            configurationMetricsProcessor.uploadConfigurationMetrics();
        }
        else{
            logger.debug("EventsService metrics are not enabled!!!!");
        }
    }

    private String createMetricPath(String accountName, String region, MetricStatistic metricStatistic){
        AWSMetric awsMetric = metricStatistic.getMetric();
        IncludeMetric includeMetric = awsMetric.getIncludeMetric();
        com.amazonaws.services.cloudwatch.model.Metric metric = awsMetric.getMetric();
        String apiName = null;
        String stageName = null;

        for(Dimension dimension : metric.getDimensions()) {
            if(dimension.getName().equalsIgnoreCase("ApiName")) {
                apiName = dimension.getValue();
            }
            if(dimension.getName().equalsIgnoreCase("Stage")) {
                stageName = dimension.getValue();
            }
        }
        //apiName will never be null
        StringBuilder stringBuilder = new StringBuilder(accountName)
                .append("|")
                .append(region)
                .append("|")
                .append(apiName)
                .append("|");
        if(stageName != null) {
            stringBuilder.append(stageName)
                    .append("|");
        }
        stringBuilder.append(includeMetric.getName());
        return stringBuilder.toString();

    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
}
