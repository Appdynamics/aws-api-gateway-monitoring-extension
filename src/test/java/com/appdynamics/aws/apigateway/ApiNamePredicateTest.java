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

package com.appdynamics.aws.apigateway;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Metric;
import static org.mockito.Mockito.when;

import com.appdynamics.extensions.aws.apigateway.ApiNamePredicate;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by venkata.konala on 5/22/18.
 */
@RunWith(PowerMockRunner.class)
public class ApiNamePredicateTest {

    @Mock
    private Metric metric;

    @Mock
    private Dimension dimension;

    @Test
    public void matchedApiNameMetricShouldPass(){
        String apiName = "sampleName";
        ApiNamePredicate apiNamePredicate = new ApiNamePredicate(apiName);
        when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.getValue()).thenReturn("sampleName");
        Assert.assertTrue(apiNamePredicate.apply(metric));

    }
}
