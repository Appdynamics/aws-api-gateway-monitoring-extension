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

import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.appdynamics.extensions.aws.apigateway.ApiNamesPredicate;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

/**
 * Created by venkata.konala on 5/22/18.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiNamesPredicateTest {

    private Metric metric;
    private Dimension dimension;

    @Test
    public void matchedApiNameMetricShouldReturnTrue(){
        metric = mock(Metric.class);
        dimension = mock(Dimension.class);
        List<String> apiNamesList = Lists.newArrayList("sampleName");
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        when(metric.dimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.value()).thenReturn("sampleName");
        Assert.assertTrue(apiNamesPredicate.apply(metric));

    }

    @Test
    public void unMatchedApiNameMetricShouldReturnFalse(){
        metric = mock(Metric.class);
        dimension = mock(Dimension.class);
        List<String> apiNamesList = Lists.newArrayList("sampleName1", "sampleName2");
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        when(metric.dimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.value()).thenReturn("sampleName");
        Assert.assertFalse(apiNamesPredicate.apply(metric));

    }

    @Test
    public void emptyPredicateShouldReturnTrue(){
        metric = mock(Metric.class);
        List<String> apiNamesList = Lists.newArrayList();
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        Assert.assertTrue(apiNamesPredicate.apply(metric));

    }

    @Test
    public void nullPredicateShouldReturnTrue(){
        metric = mock(Metric.class);
        List<String> apiNamesList = null;
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        Assert.assertTrue(apiNamesPredicate.apply(metric));

    }

    @Test
    public void emptyApiNamesInListShouldReturnTrue(){
        metric = mock(Metric.class);
        List<String> apiNamesList = Lists.newArrayList("", "");
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        Assert.assertTrue(apiNamesPredicate.apply(metric));

    }

    @Test
    public void emptyApiNamesAndNonEmtyApiNamesInListShouldReturnTrueIfMatched(){
        metric = mock(Metric.class);
        dimension = mock(Dimension.class);
        List<String> apiNamesList = Lists.newArrayList("sampleName", "");
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        when(metric.dimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.value()).thenReturn("sampleName");
        Assert.assertTrue(apiNamesPredicate.apply(metric));

    }

    @Test
    public void emptyApiNamesAndNonEmtyApiNamesInListShouldReturnFalseIfNotMatched(){
        metric = mock(Metric.class);
        dimension = mock(Dimension.class);
        List<String> apiNamesList = Lists.newArrayList("sampleName$", "");
        ApiNamesPredicate apiNamesPredicate = new ApiNamesPredicate(apiNamesList);
        when(metric.dimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.value()).thenReturn("sampleName1");
        Assert.assertFalse(apiNamesPredicate.apply(metric));

    }
}
