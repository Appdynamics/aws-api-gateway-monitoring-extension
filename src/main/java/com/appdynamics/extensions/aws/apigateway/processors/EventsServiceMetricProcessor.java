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

import com.appdynamics.extensions.aws.apigateway.APIGatewayMonitor;
import com.appdynamics.extensions.aws.apigateway.configuration.EventsService;
import com.appdynamics.extensions.aws.apigateway.events.MetricEvent;
import com.appdynamics.extensions.aws.apigateway.events.TraditionalMetricEvents;
import com.appdynamics.extensions.aws.apigateway.schemas.Schema;
import com.appdynamics.extensions.aws.apigateway.schemas.TraditionalMetricSchema;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
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
public class EventsServiceMetricProcessor {

    private static final Logger logger = Logger.getLogger(EventsServiceMetricProcessor.class);

    private EventsService eventsService;

    private String host;
    private  Integer port;
    private Boolean useSsl;
    private String accountName;
    private String apiKey;

    private CloseableHttpClient httpClient;
    private HttpHost httpHost;

    public EventsServiceMetricProcessor(EventsService eventsService){
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

    public void uploadTraditionalMetrics(List<Metric> metricList){
        if(! eventsSchemaExists("standardMetrics")){

            TraditionalMetricSchema traditionalMetricSchema = new TraditionalMetricSchema();
            traditionalMetricSchema.setMetricName("string");
            traditionalMetricSchema.setMetricPath("string");
            traditionalMetricSchema.setMetricValue("string");
            Schema schema = new Schema<>();
            schema.setSchema(traditionalMetricSchema);
            createEventsSchema("standardMetrics", schema);

            TraditionalMetricEvents traditionalMetricEvents = new TraditionalMetricEvents();
            List<MetricEvent> metricEventList = Lists.newArrayList();
            for(Metric metric : metricList){
                MetricEvent metricEvent = new MetricEvent();
                metricEvent.setMetricName(metric.getMetricName());
                metricEvent.setMetricPath(metric.getMetricPath());
                metricEvent.setMetricValue(metric.getMetricValue());
                metricEventList.add(metricEvent);
            }
            traditionalMetricEvents.setEventsList(metricEventList);
            publishEvents("standardMetrics", traditionalMetricEvents);
        }

    }

    private boolean eventsSchemaExists(String schema){
        HttpGet httpGet = new HttpGet(httpHost.getAddress() + "events/schema/" + schema);
        httpGet.setHeader("X-Events-API-AccountName", accountName);
        httpGet.setHeader("X-Events-API-Key", apiKey);
        httpGet.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        StatusLine statusLine;

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 200 ){
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createEventsSchema(String schema, Object object){
        try {
            Gson gson = new Gson();
            HttpPost httpPost = new HttpPost(httpHost.getAddress() + "events/publish/" + schema);
            httpPost.setHeader("X-Events-API-AccountName", accountName);
            httpPost.setHeader("X-Events-API-Key", apiKey);
            httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");


            httpPost.setEntity(new StringEntity(gson.toJson(object)));
            StatusLine statusLine;
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 201 ){
                logger.debug("{} created !!" +  schema);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publishEvents(String schema, Object object){

        try {
            Gson gson = new Gson();
            HttpPost httpPost = new HttpPost(httpHost.getAddress() + "events/publish/" + schema);
            httpPost.setHeader("X-Events-API-AccountName", accountName);
            httpPost.setHeader("X-Events-API-Key", apiKey);
            httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");


            httpPost.setEntity(new StringEntity(gson.toJson(object)));
            StatusLine statusLine;
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if (response != null && (statusLine = response.getStatusLine()) != null && statusLine.getStatusCode() == 202 ){
                logger.debug("Events published for {} !!" +  schema);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
