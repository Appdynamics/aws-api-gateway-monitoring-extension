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

import com.appdynamics.extensions.aws.apigateway.configuration.APIGatewayConfiguration;
import com.appdynamics.extensions.aws.apigateway.configuration.EventsService;
import com.appdynamics.extensions.aws.apigateway.events.APIMetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.ResourceMetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.StageMetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.TraditonalMetricEvent;
import com.appdynamics.extensions.aws.apigateway.processors.ConfigurationMetricsProcessor;
import com.appdynamics.extensions.aws.apigateway.schemas.*;
import com.appdynamics.extensions.aws.config.Account;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 5/27/18.
 */
public class EventsServiceMetricsWriter {

    private static final Logger logger = Logger.getLogger(EventsServiceMetricsWriter.class);

    private Map<String, ?> eventsService;

    private String host;
    private Integer port;
    private Boolean useSsl;
    private String accountName;
    private String apiKey;

    private CloseableHttpClient httpClient;
    private HttpHost httpHost;

    public EventsServiceMetricsWriter(Map<String, ?> eventsService){
        this.eventsService = eventsService;
        initialize();
    }

    private void initialize(){
        Map<String, ?> credentials = (Map<String, ?>)eventsService.get("credentials");
        host = credentials.get("controllerEventsServiceHost") == null ? null : (String)credentials.get("controllerEventsServiceHost");
        AssertUtils.assertNotNull(host, "The controllerEventsServiceHost field is null or empty");
        port = credentials.get("controllerEventsServicePort") == null ? null : (Integer)credentials.get("controllerEventsServicePort");
        AssertUtils.assertNotNull(port, "The controllerEventsServicePort field is null or empty");
        useSsl = credentials.get("enableSSL") == null ? null : (Boolean)credentials.get("enableSSL") ;
        AssertUtils.assertNotNull(useSsl, "The enableSSL field is null or empty");
        accountName = credentials.get("controllerGlobalAccountName") == null ? null : (String)credentials.get("controllerGlobalAccountName");
        AssertUtils.assertNotNull(accountName, "The controllerGlobalAccountName field is null or empty");
        apiKey = credentials.get("eventsAPIKey") == null ? null : (String)credentials.get("eventsAPIKey");
        AssertUtils.assertNotNull(apiKey, "The eventsAPIKey field is null or empty");

        httpClient = HttpClients.createDefault();
        httpHost = new HttpHost(host, port, useSsl ? "https" : "http");
    }

    public void uploadTraditionalMetrics(final List<Metric> metricList){
        if(eventsSchemaExists("AWSAPIGatewayMonitor_traditionalMetrics")){
            deleteEventsSchema("AWSAPIGatewayMonitor_traditionalMetrics");
        }
        TraditionalMetricSchema traditionalMetricSchema = new TraditionalMetricSchema();
        traditionalMetricSchema.setMetricName("string");
        traditionalMetricSchema.setMetricPath("string");
        traditionalMetricSchema.setMetricValue("string");
        Schema schema = new Schema();
        schema.setSchema(traditionalMetricSchema);
        createEventsSchema("AWSAPIGatewayMonitor_traditionalMetrics", schema);

        List<TraditonalMetricEvent> traditonalMetricEventList = Lists.newArrayList();
        for(Metric metric : metricList){
            TraditonalMetricEvent traditonalMetricEvent = new TraditonalMetricEvent();
            traditonalMetricEvent.setMetricName(metric.getMetricName());
            traditonalMetricEvent.setMetricPath(metric.getMetricPath());
            traditonalMetricEvent.setMetricValue(metric.getMetricValue());
            traditonalMetricEventList.add(traditonalMetricEvent);
        }
        publishEvents("AWSAPIGatewayMonitor_traditionalMetrics", traditonalMetricEventList);

    }

    public void uploadAPIMetrics(final List<APIMetricEvent> apiMetricEventList){
        if(eventsSchemaExists("AWSAPIGatewayMonitor_APIMetrics")){
            deleteEventsSchema("AWSAPIGatewayMonitor_APIMetrics");
        }
        Schema schema = new Schema();
        APIMetricSchema apiMetricSchema = new APIMetricSchema();
        apiMetricSchema.setId("string");
        apiMetricSchema.setApiName("string");
        apiMetricSchema.setRegion("string");
        apiMetricSchema.setDescription("string");
        apiMetricSchema.setDate("date");
        schema.setSchema(apiMetricSchema);
        createEventsSchema("AWSAPIGatewayMonitor_APIMetrics", schema);
        publishEvents("AWSAPIGatewayMonitor_APIMetrics", apiMetricEventList);

    }

    public void uploadResourceMetrics(final List<ResourceMetricEvent> resourceMetricEventList){
        if(eventsSchemaExists("AWSAPIGatewayMonitor_ResourceMetrics")){
            deleteEventsSchema("AWSAPIGatewayMonitor_ResourceMetrics");
        }
        Schema schema = new Schema();
        ResourceMetricSchema resourceMetricSchema = new ResourceMetricSchema();
        resourceMetricSchema.setRestApiId("string");
        resourceMetricSchema.setRestApiName("string");
        resourceMetricSchema.setRegion("string");
        resourceMetricSchema.setId("string");
        resourceMetricSchema.setParentId("string");
        resourceMetricSchema.setPath("string");
        resourceMetricSchema.setPathPart("string");
        resourceMetricSchema.setMethods("string");
        schema.setSchema(resourceMetricSchema);
        createEventsSchema("AWSAPIGatewayMonitor_ResourceMetrics", schema);
        publishEvents("AWSAPIGatewayMonitor_ResourceMetrics", resourceMetricEventList);

    }

    public void uploadStageMetrics(final List<StageMetricEvent> stageMetricEventList){
        if(eventsSchemaExists("AWSAPIGatewayMonitor_StageMetrics")){
            deleteEventsSchema("AWSAPIGatewayMonitor_StageMetrics");
        }
        Schema schema = new Schema();
        StageMetricSchema stageMetricSchema = new StageMetricSchema();
        stageMetricSchema.setRestApiName("string");
        stageMetricSchema.setRegion("string");
        stageMetricSchema.setStageName("string");
        stageMetricSchema.setDeploymentId("string");
        stageMetricSchema.setCacheClusterEnabled("boolean");
        stageMetricSchema.setCacheClusterSize("string");
        stageMetricSchema.setCacheClusterStatus("string");
        stageMetricSchema.setLastUpdatedDate("date");
        stageMetricSchema.setCreatedDate("date");
        schema.setSchema(stageMetricSchema);
        createEventsSchema("AWSAPIGatewayMonitor_StageMetrics", schema);

        publishEvents("AWSAPIGatewayMonitor_StageMetrics", stageMetricEventList);

    }

    private boolean eventsSchemaExists(final String schema){
        HttpGet httpGet = new HttpGet(httpHost.toURI() + "/events/schema/" + schema);
        httpGet.setHeader("X-Events-API-AccountName", accountName);
        httpGet.setHeader("X-Events-API-Key", apiKey);
        httpGet.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        StatusLine statusLine;
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 200 ){
                return true;
            }
        }
        catch (IOException e) {
            logger.error(String.format("Error while checking if the %s schema exists",schema) + e);
        }
        finally {
            if(response != null){
                try {
                    response.close();
                }
                catch(Exception e){
                    logger.error("Error while closing the response" + e.getMessage());

                }
            }
        }
        return false;
    }

    private void deleteEventsSchema(final String schema){
        HttpDelete httpDelete = new HttpDelete(httpHost.toURI() + "/events/schema/" + schema);
        httpDelete.setHeader("X-Events-API-AccountName", accountName);
        httpDelete.setHeader("X-Events-API-Key", apiKey);
        httpDelete.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        StatusLine statusLine;
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpDelete);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 200 ){
                logger.debug(String.format("Successfully deleted %s schema", schema) );
            }
        }
        catch (IOException e) {
            logger.error(String.format("Error while deleting the eventSchema %s",schema) + e);
        }
        finally {
            if(response != null){
                try {
                    response.close();
                }
                catch(Exception e){
                    logger.error("Error while closing the response" + e.getMessage());

                }
            }
        }
    }

    private void createEventsSchema(final String schema, final Object object){
        CloseableHttpResponse response = null;
        try {
            Gson gson = new Gson();
            HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/schema/" + schema);
            httpPost.setHeader("X-Events-API-AccountName", accountName);
            httpPost.setHeader("X-Events-API-Key", apiKey);
            httpPost.setHeader("Content-Type", "application/vnd.appd.events+json;v=2");

            String entity = gson.toJson(object);
            httpPost.setEntity(new StringEntity(entity));
            StatusLine statusLine;
            response = httpClient.execute(httpPost);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 201 ){
                logger.debug(String.format("%s created !!", schema));
            }

        }
        catch (IOException e) {
            logger.error(String.format("Error while creating the eventSchema for %s",schema) + e);
        }
        finally {
            if(response != null){
                try {
                    response.close();
                }
                catch(Exception e){
                    logger.error("Error while closing the response" + e.getMessage());

                }
            }
        }

    }

    private void publishEvents(final String schema, final Object object){
        CloseableHttpResponse response = null;
        try {
            Gson gson = new Gson();
            HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/publish/" + schema);
            httpPost.setHeader("X-Events-API-AccountName", accountName);
            httpPost.setHeader("X-Events-API-Key", apiKey);
            httpPost.setHeader("Content-Type", "application/vnd.appd.events+json;v=2");

            String entity = gson.toJson(object);
            httpPost.setEntity(new StringEntity(entity));
            StatusLine statusLine;
            response = httpClient.execute(httpPost);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 200 ){
                logger.debug(String.format("Events published for %s !!", schema));
            }

        }
        catch (IOException e) {
            logger.error(String.format("Error while publishing the events for %s",schema) + e);
        }
        finally {
            if(response != null){
                try {
                    response.close();
                }
                catch(Exception e){
                    logger.error("Error while closing the response" + e.getMessage());

                }
            }
        }

    }
}
