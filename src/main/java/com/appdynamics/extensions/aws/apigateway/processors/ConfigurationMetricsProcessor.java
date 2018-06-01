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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.apigateway.model.*;
import com.appdynamics.extensions.aws.apigateway.EventsServiceMetricsWriter;
import com.appdynamics.extensions.aws.apigateway.configuration.APIGatewayConfiguration;
import com.appdynamics.extensions.aws.apigateway.configuration.EventsService;
import com.appdynamics.extensions.aws.apigateway.events.APIMetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.ResourceMetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.StageMetricEvent;
import com.appdynamics.extensions.aws.config.Account;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 5/28/18.
 */
public class ConfigurationMetricsProcessor {

    private APIGatewayConfiguration apiGatewayConfiguration;
    private Map<String, ?> eventsService;
    private EventsServiceMetricsWriter eventsServiceMetricsWriter;

    public ConfigurationMetricsProcessor(APIGatewayConfiguration apiGatewayConfiguration, EventsServiceMetricsWriter eventsServiceMetricsWriter){
        this.apiGatewayConfiguration = apiGatewayConfiguration;
        this.eventsService = apiGatewayConfiguration.getEventsService();
        this.eventsServiceMetricsWriter = eventsServiceMetricsWriter;
    }

    public void uploadConfigurationMetrics(){
        for(Account account : apiGatewayConfiguration.getAccounts()){
            AmazonApiGatewayClient amazonApiGatewayClient;
            String awsAccessKey = account.getAwsAccessKey();
            String awsSecretKey = account.getAwsSecretKey();
            if(!Strings.isNullOrEmpty(awsAccessKey) && !Strings.isNullOrEmpty(awsSecretKey)) {
                AWSCredentials awsCredentials = new BasicAWSCredentials(account.getAwsAccessKey(), account.getAwsSecretKey());
                amazonApiGatewayClient = new AmazonApiGatewayClient(awsCredentials);
            }
            else {
                amazonApiGatewayClient = new AmazonApiGatewayClient();
            }

            for(String region : account.getRegions()){
                amazonApiGatewayClient.setRegion(Region.getRegion(Regions.fromName(region)));
                uploadRestApisData(amazonApiGatewayClient, region);

            }
        }
    }

    private List<APIMetricEvent> uploadRestApisData(final AmazonApiGatewayClient amazonApiGatewayClient, final String region){
        List<APIMetricEvent> apiMetricEventList = Lists.newArrayList();
        List<ResourceMetricEvent> resourceMetricEventList = Lists.newArrayList();
        List<StageMetricEvent> stageMetricEventList = Lists.newArrayList();

        GetRestApisRequest restApisRequest = new GetRestApisRequest();
        GetRestApisResult restApisResult = amazonApiGatewayClient.getRestApis(restApisRequest);
        List<RestApi> resultList = restApisResult.getItems();
        for(RestApi restApi : resultList){
            APIMetricEvent apiMetricEvent = new APIMetricEvent();
            String id = restApi.getId();
            String name = restApi.getName();
            String description = restApi.getDescription();
            Date date = restApi.getCreatedDate();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
            String dateAsString = df.format(date);


            apiMetricEvent.setId(id);
            apiMetricEvent.setApiName(name);
            apiMetricEvent.setRegion(region);
            apiMetricEvent.setDescription(description);
            apiMetricEvent.setDate(dateAsString);

            apiMetricEventList.add(apiMetricEvent);

            if (eventsService.get("enableResourceMetrics") != null && (Boolean)eventsService.get("enableResourceMetrics")) {
                List<ResourceMetricEvent> restApiResourceMetricEventList = getResourcesData(amazonApiGatewayClient, region, id, name);
                resourceMetricEventList.addAll(restApiResourceMetricEventList);
            }
            if (eventsService.get("enableStageMetrics") != null && (Boolean)eventsService.get("enableStageMetrics")) {
                List<StageMetricEvent> restApiStageMetricEventList = getStagesData(amazonApiGatewayClient, region, id, name);
                stageMetricEventList.addAll(restApiStageMetricEventList);
            }
        }
        if(eventsService.get("enableResourceMetrics") != null && (Boolean)eventsService.get("enableResourceMetrics")) {
            eventsServiceMetricsWriter.uploadResourceMetrics(resourceMetricEventList);
        }
        if(eventsService.get("enableStageMetrics") != null && (Boolean)eventsService.get("enableStageMetrics")) {
            eventsServiceMetricsWriter.uploadStageMetrics(stageMetricEventList);
        }
        if(eventsService.get("enableApiMetrics") != null && (Boolean)eventsService.get("enableApiMetrics")) {
            eventsServiceMetricsWriter.uploadAPIMetrics(apiMetricEventList);
        }
        return apiMetricEventList;
    }


    private List<ResourceMetricEvent> getResourcesData(final AmazonApiGatewayClient amazonApiGatewayClient, final String region, final String restApiId, final String restApiName){
        List<ResourceMetricEvent> resourceMetricEvents = Lists.newArrayList();

        GetResourcesRequest resourcesRequest = new GetResourcesRequest();
        resourcesRequest.setRestApiId(restApiId);
        GetResourcesResult resourcesResult = amazonApiGatewayClient.getResources(resourcesRequest);
        List<Resource> resultList = resourcesResult.getItems();
        for(Resource resource : resultList){
            ResourceMetricEvent resourceMetricEvent = new ResourceMetricEvent();
            String id = resource.getId();
            String parentId = resource.getParentId();
            String path = resource.getPath();
            String pathPart = resource.getPathPart();
            StringBuilder methodsBuilder = new StringBuilder();
            Map<String, Method> resourceMethods = resource.getResourceMethods();
            if(resourceMethods != null) {
                for (Map.Entry<String, Method> entry : resourceMethods.entrySet()) {
                    String resourceMethodName = entry.getKey();
                    Method method = entry.getValue();
                    methodsBuilder.append(resourceMethodName + " ");
                }
            }

            resourceMetricEvent.setRestApiId(restApiId);
            resourceMetricEvent.setRestApiName(restApiName);
            resourceMetricEvent.setRegion(region);
            resourceMetricEvent.setId(id);
            resourceMetricEvent.setParentId(parentId);
            resourceMetricEvent.setPath(path);
            resourceMetricEvent.setPathPart(pathPart);
            resourceMetricEvent.setMethods(methodsBuilder.toString());
            resourceMetricEvents.add(resourceMetricEvent);
        }
        return resourceMetricEvents;
    }

    private List<StageMetricEvent> getStagesData(final AmazonApiGatewayClient amazonApiGatewayClient, final String region, final String restApiId, final String restApiName){
        List<StageMetricEvent> stageMetricEvents = Lists.newArrayList();

        GetStagesRequest stagesRequest = new GetStagesRequest();
        stagesRequest.setRestApiId(restApiId);
        GetStagesResult stagesResult = amazonApiGatewayClient.getStages(stagesRequest);
        List<Stage> stageList = stagesResult.getItem();
        for(Stage stage : stageList){
            StageMetricEvent stageMetricEvent = new StageMetricEvent();
            String stageName = stage.getStageName();
            String deploymentId = stage.getDeploymentId();
            Boolean cacheClusterEnabled = stage.getCacheClusterEnabled();
            String cacheClusterSize = stage.getCacheClusterSize();
            String cacheClusterStatus = stage.getCacheClusterStatus();
            Date lastUpdatedDate = stage.getLastUpdatedDate();
            Date createdDate = stage.getCreatedDate();

            stageMetricEvent.setRestApiName(restApiName);
            stageMetricEvent.setRegion(region);
            stageMetricEvent.setStageName(stageName);
            stageMetricEvent.setDeploymentId(deploymentId);
            stageMetricEvent.setCacheClusterEnabled(cacheClusterEnabled);
            stageMetricEvent.setCacheClusterSize(cacheClusterSize);
            stageMetricEvent.setCacheClusterStatus(cacheClusterStatus);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
            String dateAsString = df.format(lastUpdatedDate);
            String dateAsString2 = df.format(createdDate);

            stageMetricEvent.setLastUpdatedDate(dateAsString);
            stageMetricEvent.setCreatedDate(dateAsString2);

            stageMetricEvents.add(stageMetricEvent);
        }
        return stageMetricEvents;
    }



}
