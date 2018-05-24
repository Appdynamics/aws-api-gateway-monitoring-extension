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


import com.amazonaws.services.cloudwatch.model.Metric;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import java.util.List;

/**
 * Created by venkata.konala on 5/21/18.
 */
public class ApiNamesPredicate implements Predicate<Metric> {

    private List<String> apiNamesList;
    private Predicate<CharSequence> patternPredicate;

    public ApiNamesPredicate(List<String> apiNamesList){
        this.apiNamesList = apiNamesList;
        buildPattern();
    }

    private void buildPattern(){
        if(apiNamesList != null && !apiNamesList.isEmpty()){

            for(String apiPattern : apiNamesList){
                if(!Strings.isNullOrEmpty(apiPattern)) {
                    Predicate<CharSequence> apiPatternPredicate = Predicates.containsPattern(apiPattern);
                    if (patternPredicate == null) {
                        patternPredicate = apiPatternPredicate;
                    } else {
                        patternPredicate = Predicates.or(patternPredicate, apiPatternPredicate);
                    }
                }
            }
        }
    }

    @Override
    public boolean apply(Metric metric) {
        if(patternPredicate == null){
            return true;
        }
        else{
            String apiName = metric.getDimensions().get(0).getValue();
            return patternPredicate.apply(apiName);
        }
    }
}
