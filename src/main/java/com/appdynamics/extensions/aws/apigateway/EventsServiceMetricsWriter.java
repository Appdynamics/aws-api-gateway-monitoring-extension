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

    private EventsService eventsService;

    private String host;
    private  Integer port;
    private Boolean useSsl;
    private String accountName;
    private String apiKey;

    private CloseableHttpClient httpClient;
    private HttpHost httpHost;

    public EventsServiceMetricsWriter(EventsService eventsService){
        this.eventsService = eventsService;
        initialize();
    }

    private void initialize(){
        Map<String, ?> credentials = eventsService.getCredentials();
        host = credentials.get("ControllerEventsServiceHost") == null ? null : credentials.get("ControllerEventsServiceHost").toString();
        AssertUtils.assertNotNull(host, "The ControllerEventsServiceHost field is null or empty");
        port = credentials.get("ControllerEventsServicePort") == null ? null : (Integer)credentials.get("ControllerEventsServicePort");
        AssertUtils.assertNotNull(port, "The ControllerEventsServicePort field is null or empty");
        useSsl = credentials.get("SSLEnabled") == null ? null : (Boolean)credentials.get("SSLEnabled") ;
        AssertUtils.assertNotNull(useSsl, "The SSLEnabled field is null or empty");
        accountName = credentials.get("ControllerGlobalAccountName") == null ? null : credentials.get("ControllerGlobalAccountName").toString();
        AssertUtils.assertNotNull(accountName, "The ControllerGlobalAccountName field is null or empty");
        apiKey = credentials.get("EventsAPIKey") == null ? null : credentials.get("EventsAPIKey").toString();
        AssertUtils.assertNotNull(apiKey, "The EventsAPIKey field is null or empty");

        httpClient = HttpClients.createDefault();
        httpHost = new HttpHost(host, port, useSsl ? "https" : "http");
    }

    public void uploadTraditionalMetrics(final List<Metric> metricList){
        if(eventsSchemaExists("traditionalMetrics")){
            deleteEventsSchema("traditionalMetrics");
        }
        TraditionalMetricSchema traditionalMetricSchema = new TraditionalMetricSchema();
        traditionalMetricSchema.setMetricName("string");
        traditionalMetricSchema.setMetricPath("string");
        traditionalMetricSchema.setMetricValue("string");
        Schema schema = new Schema();
        schema.setSchema(traditionalMetricSchema);
        createEventsSchema("traditionalMetrics", schema);

        List<TraditonalMetricEvent> traditonalMetricEventList = Lists.newArrayList();
        for(Metric metric : metricList){
            TraditonalMetricEvent traditonalMetricEvent = new TraditonalMetricEvent();
            traditonalMetricEvent.setMetricName(metric.getMetricName());
            traditonalMetricEvent.setMetricPath(metric.getMetricPath());
            traditonalMetricEvent.setMetricValue(metric.getMetricValue());
            traditonalMetricEventList.add(traditonalMetricEvent);
        }
        publishEvents("traditionalMetrics", traditonalMetricEventList);

    }

    public void uploadAPIMetrics(final List<APIMetricEvent> apiMetricEventList){
        if(eventsSchemaExists("APIMetrics")){
            deleteEventsSchema("APIMetrics");
        }
        Schema schema = new Schema();
        APIMetricSchema apiMetricSchema = new APIMetricSchema();
        apiMetricSchema.setId("string");
        apiMetricSchema.setApiName("string");
        apiMetricSchema.setRegion("string");
        apiMetricSchema.setDescription("string");
        apiMetricSchema.setDate("date");
        schema.setSchema(apiMetricSchema);
        createEventsSchema("APIMetrics", schema);
        publishEvents("APIMetrics", apiMetricEventList);

    }

    public void uploadResourceMetrics(final List<ResourceMetricEvent> resourceMetricEventList){
        if(eventsSchemaExists("ResourceMetrics")){
            deleteEventsSchema("ResourceMetrics");
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
        createEventsSchema("ResourceMetrics", schema);
        publishEvents("ResourceMetrics", resourceMetricEventList);

    }

    public void uploadStageMetrics(final List<StageMetricEvent> stageMetricEventList){
        if(eventsSchemaExists("StageMetrics")){
            deleteEventsSchema("StageMetrics");
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
        createEventsSchema("StageMetrics", schema);

        publishEvents("StageMetrics", stageMetricEventList);

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
            e.printStackTrace();
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
                logger.debug("Successfully deleted {} schema" + schema);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
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
                logger.debug("{} created !!" +  schema);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
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
                logger.debug("Events published for {} !!" +  schema);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String args[]){

        APIGatewayConfiguration apiGatewayConfiguration = new APIGatewayConfiguration();
        Account account = new Account();
        account.setAwsAccessKey("");
        account.setAwsSecretKey("");
        account.setRegions(Sets.newHashSet("us-west-2"));
        apiGatewayConfiguration.setAccounts(Lists.newArrayList(account));

        EventsService eventsService = new EventsService();
        eventsService.setEnable(true);
        Map<String, Object> credentials = Maps.newHashMap();
        credentials.put("ControllerEventsServiceHost", "localhost");
        credentials.put("ControllerEventsServicePort", 9080);
        credentials.put("SSLEnabled", false);
        credentials.put("ControllerGlobalAccountName", "customer1_7a47c220-1e00-403b-a955-ac18296a1409");
        credentials.put("EventsAPIKey", "302c8c2b-5be8-4cb5-a5c4-30e0e57a49e1");
        eventsService.setCredentials(credentials);
        eventsService.setTraditonalMetricsEnable(true);
        eventsService.setApiMetricsEnable(true);
        eventsService.setResourceMetricsEnable(true);
        eventsService.setStageMetricsEnable(true);

        apiGatewayConfiguration.setEventsService(eventsService);
        EventsServiceMetricsWriter eventsServiceMetricsWriter = new EventsServiceMetricsWriter(eventsService);

        ConfigurationMetricsProcessor configurationMetricsProcessor = new ConfigurationMetricsProcessor(apiGatewayConfiguration, eventsServiceMetricsWriter);
        configurationMetricsProcessor.uploadConfigurationMetrics();
    }




}
