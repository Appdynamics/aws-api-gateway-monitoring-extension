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

import java.util.Map;

/**
 * Created by venkata.konala on 5/27/18.
 */
public class EventsService {

    private boolean enable;
    private Map<String, ?> credentials;
    private boolean traditonalMetricsEnable;
    private boolean apiMetricsEnable;
    private boolean resourceMetricsEnable;
    private boolean stageMetricsEnable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Map<String, ?> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, ?> credentials) {
        this.credentials = credentials;
    }

    public boolean isTraditonalMetricsEnable() {
        return traditonalMetricsEnable;
    }

    public void setTraditonalMetricsEnable(boolean traditonalMetricsEnable) {
        this.traditonalMetricsEnable = traditonalMetricsEnable;
    }

    public boolean isApiMetricsEnable() {
        return apiMetricsEnable;
    }

    public void setApiMetricsEnable(boolean apiMetricsEnable) {
        this.apiMetricsEnable = apiMetricsEnable;
    }

    public boolean isResourceMetricsEnable() {
        return resourceMetricsEnable;
    }

    public void setResourceMetricsEnable(boolean resourceMetricsEnable) {
        this.resourceMetricsEnable = resourceMetricsEnable;
    }

    public boolean isStageMetricsEnable() {
        return stageMetricsEnable;
    }

    public void setStageMetricsEnable(boolean stageMetricsEnable) {
        this.stageMetricsEnable = stageMetricsEnable;
    }
}
