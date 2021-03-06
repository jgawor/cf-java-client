/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.client.v2.serviceinstances;

import org.junit.Test;


public final class BindServiceInstanceToRouteRequestTest {

    @Test(expected = IllegalStateException.class)
    public void noRouteId() {
        BindServiceInstanceToRouteRequest.builder()
            .serviceInstanceId("test-service-instance-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noServiceInstanceId() {
        BindServiceInstanceToRouteRequest.builder()
            .routeId("test-route-id")
            .build();
    }

    @Test
    public void valid() {
        BindServiceInstanceToRouteRequest.builder()
            .serviceInstanceId("test-service-instance-id")
            .routeId("test-route-id")
            .build();
    }

}
