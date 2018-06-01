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

    private boolean enableTraditionalMetrics;
    private boolean enableApiMetrics;
    private boolean enableResourceMetrics;
    private boolean enableStageMetrics;
    private Map<String, ?> credentials;

    public boolean isEnableTraditonalMetrics() {
        return enableTraditionalMetrics;
    }

    public void setEnableTraditonalMetrics(boolean enableTraditionalMetrics) {
        this.enableTraditionalMetrics = enableTraditionalMetrics;
    }

    public boolean isEnableApiMetrics() {
        return enableApiMetrics;
    }

    public void setEnableApiMetrics(boolean enableApiMetrics) {
        this.enableApiMetrics = enableApiMetrics;
    }

    public boolean isEnableResourceMetrics() {
        return enableResourceMetrics;
    }

    public void setEnableResourceMetrics(boolean enableResourceMetrics) {
        this.enableResourceMetrics = enableResourceMetrics;
    }

    public boolean isEnableStageMetrics() {
        return enableStageMetrics;
    }

    public void setEnableStageMetrics(boolean enableStageMetrics) {
        this.enableStageMetrics = enableStageMetrics;
    }

    public Map<String, ?> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, ?> credentials) {
        this.credentials = credentials;
    }
}
