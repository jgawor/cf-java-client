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

package org.cloudfoundry.client.v2;

import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.CreateApplicationRequest;
import org.cloudfoundry.client.v2.applications.CreateApplicationResponse;
import org.cloudfoundry.client.v2.servicebindings.AbstractServiceBindingResource;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.DeleteServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.DeleteServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.GetServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.GetServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingResource;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.CreateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.CreateUserProvidedServiceInstanceResponse;
import org.cloudfoundry.util.JobUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.util.Optional;

import static org.cloudfoundry.util.OperationUtils.thenKeep;
import static org.cloudfoundry.util.tuple.TupleUtils.consumer;
import static org.cloudfoundry.util.tuple.TupleUtils.function;
import static org.junit.Assert.assertEquals;

public final class ServiceBindingsTest extends AbstractIntegrationTest {

    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    private Mono<String> spaceId;

    @Test
    public void create() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    requestCreateServiceBinding(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .subscribe(this.<Tuple3<String, String, CreateServiceBindingResponse>>testSubscriber()
                .expectThat(consumer(ServiceBindingsTest::testServiceBinding)));
    }

    @Test
    public void delete() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    createServiceBindingId(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .as(thenKeep(function((serviceInstanceId, applicationId, serviceBindingId) -> deleteServiceBinding(this.cloudFoundryClient, serviceBindingId))))
            .then(function((serviceInstanceId, applicationId, serviceBindingId) -> requestGetServiceBinding(this.cloudFoundryClient, serviceBindingId)))
            .subscribe(this.testSubscriber()
                .expectErrorMatch(CloudFoundryException.class, "CF-ServiceBindingNotFound\\([0-9]+\\): The service binding could not be found: .*"));
    }

    @Test
    public void get() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    createServiceBindingId(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .then(function((serviceInstanceId, applicationId, serviceBindingId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    requestGetServiceBinding(this.cloudFoundryClient, serviceBindingId)
                )))
            .subscribe(this.<Tuple3<String, String, GetServiceBindingResponse>>testSubscriber()
                .expectThat(consumer(ServiceBindingsTest::testServiceBinding)));
    }

    @Test
    public void list() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    createServiceBindingId(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .then(function((serviceInstanceId, applicationId, serviceBindingId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    requestListServiceBindings(this.cloudFoundryClient, null, null)
                        .filter(resource -> serviceBindingId.equals(ResourceUtils.getId(resource)))
                        .single()
                )))
            .subscribe(this.<Tuple3<String, String, ServiceBindingResource>>testSubscriber()
                .expectThat(consumer(ServiceBindingsTest::testServiceBinding)));
    }

    @Test
    public void listFilterByApplicationId() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    createServiceBindingId(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .then(function((serviceInstanceId, applicationId, serviceBindingId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    requestListServiceBindings(this.cloudFoundryClient, applicationId, null)
                        .filter(resource -> serviceBindingId.equals(ResourceUtils.getId(resource)))
                        .single()
                )))
            .subscribe(this.<Tuple3<String, String, ServiceBindingResource>>testSubscriber()
                .expectThat(consumer(ServiceBindingsTest::testServiceBinding)));
    }

    @Test
    public void listFilterByServiceInstanceId() {
        String applicationName = this.nameFactory.getApplicationName();
        String serviceInstanceName = this.nameFactory.getServiceInstanceName();

        createServiceInstanceAndApplicationIds(this.spaceId, this.cloudFoundryClient, serviceInstanceName, applicationName)
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    createServiceBindingId(this.cloudFoundryClient, applicationId, serviceInstanceId)
                )))
            .then(function((serviceInstanceId, applicationId, serviceBindingId) -> Mono
                .when(
                    Mono.just(serviceInstanceId),
                    Mono.just(applicationId),
                    requestListServiceBindings(this.cloudFoundryClient, null, serviceInstanceId)
                        .filter(resource -> serviceBindingId.equals(ResourceUtils.getId(resource)))
                        .single()
                )))
            .subscribe(this.<Tuple3<String, String, ServiceBindingResource>>testSubscriber()
                .expectThat(consumer(ServiceBindingsTest::testServiceBinding)));
    }

    private static Mono<String> createApplicationId(CloudFoundryClient cloudFoundryClient, String spaceId, String applicationName) {
        return requestCreateApplication(cloudFoundryClient, spaceId, applicationName, null, null, null, null)
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createServiceBindingId(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId) {
        return requestCreateServiceBinding(cloudFoundryClient, applicationId, serviceInstanceId)
            .map(ResourceUtils::getId);
    }

    private static Mono<Tuple2<String, String>> createServiceInstanceAndApplicationIds(Mono<String> spaceId, CloudFoundryClient cloudFoundryClient, String serviceInstance, String application) {
        return spaceId
            .then(spaceId1 -> Mono.when(
                createUserServiceInstanceId(cloudFoundryClient, spaceId1, serviceInstance),
                createApplicationId(cloudFoundryClient, spaceId1, application)
            ));
    }

    private static Mono<String> createUserServiceInstanceId(CloudFoundryClient cloudFoundryClient, String spaceId, String serviceInstanceName) {
        return requestCreateUserServiceInstance(cloudFoundryClient, spaceId, serviceInstanceName)
            .map(ResourceUtils::getId);
    }

    private static Mono<Void> deleteServiceBinding(CloudFoundryClient cloudFoundryClient, String serviceBindingId) {
        return requestDeleteServiceBinding(cloudFoundryClient, serviceBindingId)
            .then(job -> JobUtils.waitForCompletion(cloudFoundryClient, job));
    }

    private static Mono<CreateApplicationResponse> requestCreateApplication(CloudFoundryClient cloudFoundryClient, String spaceId, String applicationName, String buildpack, Boolean diego, Integer
        diskQuota, Integer memory) {
        return cloudFoundryClient.applicationsV2()
            .create(CreateApplicationRequest.builder()
                .buildpack(buildpack)
                .diego(diego)
                .diskQuota(diskQuota)
                .memory(memory)
                .name(applicationName)
                .spaceId(spaceId)
                .build());
    }

    private static Mono<CreateServiceBindingResponse> requestCreateServiceBinding(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId) {
        return cloudFoundryClient.serviceBindingsV2()
            .create(CreateServiceBindingRequest.builder()
                .applicationId(applicationId)
                .serviceInstanceId(serviceInstanceId)
                .build());
    }

    private static Mono<CreateUserProvidedServiceInstanceResponse> requestCreateUserServiceInstance(CloudFoundryClient cloudFoundryClient, String spaceId, String name) {
        return cloudFoundryClient.userProvidedServiceInstances()
            .create(CreateUserProvidedServiceInstanceRequest.builder()
                .name(name)
                .spaceId(spaceId)
                .build());
    }

    private static Mono<DeleteServiceBindingResponse> requestDeleteServiceBinding(CloudFoundryClient cloudFoundryClient, String serviceBindingId) {
        return cloudFoundryClient.serviceBindingsV2()
            .delete(DeleteServiceBindingRequest.builder()
                .serviceBindingId(serviceBindingId)
                .build());
    }

    private static Mono<GetServiceBindingResponse> requestGetServiceBinding(CloudFoundryClient cloudFoundryClient, String serviceBindingId) {
        return cloudFoundryClient.serviceBindingsV2()
            .get(GetServiceBindingRequest.builder()
                .serviceBindingId(serviceBindingId)
                .build());
    }

    private static Flux<ServiceBindingResource> requestListServiceBindings(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId) {
        ListServiceBindingsRequest.Builder builder = ListServiceBindingsRequest.builder();

        Optional.ofNullable(applicationId).map(builder::applicationId);
        Optional.ofNullable(serviceInstanceId).map(builder::serviceInstanceId);

        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.serviceBindingsV2()
                .list(builder
                    .page(page)
                    .build()));
    }

    private static <R extends AbstractServiceBindingResource> void testServiceBinding(String serviceInstanceId, String applicationId, R serviceBindingResource) {
        assertEquals(serviceInstanceId, ResourceUtils.getEntity(serviceBindingResource).getServiceInstanceId());
        assertEquals(applicationId, ResourceUtils.getEntity(serviceBindingResource).getApplicationId());
    }

}
