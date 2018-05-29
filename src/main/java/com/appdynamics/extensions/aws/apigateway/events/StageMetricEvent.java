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

package com.appdynamics.extensions.aws.apigateway.events;

import java.util.Date;

/**
 * Created by venkata.konala on 5/28/18.
 */
public class StageMetricEvent {

    private String restApiName;
    private String region;
    private String stageName ;
    private String deploymentId ;
    private Boolean cacheClusterEnabled ;
    private String cacheClusterSize ;
    private String cacheClusterStatus ;
    private String lastUpdatedDate ;
    private String createdDate ;

    public String getRestApiName() {
        return restApiName;
    }

    public void setRestApiName(String restApiName) {
        this.restApiName = restApiName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Boolean getCacheClusterEnabled() {
        return cacheClusterEnabled;
    }

    public void setCacheClusterEnabled(Boolean cacheClusterEnabled) {
        this.cacheClusterEnabled = cacheClusterEnabled;
    }

    public String getCacheClusterSize() {
        return cacheClusterSize;
    }

    public void setCacheClusterSize(String cacheClusterSize) {
        this.cacheClusterSize = cacheClusterSize;
    }

    public String getCacheClusterStatus() {
        return cacheClusterStatus;
    }

    public void setCacheClusterStatus(String cacheClusterStatus) {
        this.cacheClusterStatus = cacheClusterStatus;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
