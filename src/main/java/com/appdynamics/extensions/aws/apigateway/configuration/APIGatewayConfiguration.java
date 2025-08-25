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

package com.appdynamics.extensions.aws.apigateway.configuration;

import com.appdynamics.extensions.alerts.customevents.Event;
import com.appdynamics.extensions.aws.config.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 4/23/18.
 */
public class APIGatewayConfiguration extends Configuration {

    private Map<String, ?> eventsService;
    private List<String> apiId;


    public void setEventsService(Map<String, ?> eventsService) {
        this.eventsService = eventsService;
    }

    public Map<String, ?> getEventsService() {
        return eventsService;
    }

    public void setApiId(List<String> apiId) {
        this.apiId = apiId;
    }

    public List<String> getApiId() {
        return apiId;
    }
}
