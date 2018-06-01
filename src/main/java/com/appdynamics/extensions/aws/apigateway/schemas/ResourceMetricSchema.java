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

package com.appdynamics.extensions.aws.apigateway.schemas;

/**
 * Created by venkata.konala on 5/28/18.
 */
public class ResourceMetricSchema {

    private String restApiId;
    private String restApiName;
    private String region;
    private String id ;
    private String parentId ;
    private String path ;
    private String pathPart ;
    private String methods;

    public String getRestApiId() {
        return restApiId;
    }

    public void setRestApiId(String restApiId) {
        this.restApiId = restApiId;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathPart() {
        return pathPart;
    }

    public void setPathPart(String pathPart) {
        this.pathPart = pathPart;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }
}
