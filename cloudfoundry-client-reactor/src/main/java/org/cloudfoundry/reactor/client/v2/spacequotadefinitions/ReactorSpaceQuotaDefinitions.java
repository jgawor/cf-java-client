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

package org.cloudfoundry.reactor.client.v2.spacequotadefinitions;

import org.cloudfoundry.client.v2.spacequotadefinitions.AssociateSpaceQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.AssociateSpaceQuotaDefinitionResponse;
import org.cloudfoundry.client.v2.spacequotadefinitions.CreateSpaceQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.CreateSpaceQuotaDefinitionResponse;
import org.cloudfoundry.client.v2.spacequotadefinitions.DeleteSpaceQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.DeleteSpaceQuotaDefinitionResponse;
import org.cloudfoundry.client.v2.spacequotadefinitions.GetSpaceQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.GetSpaceQuotaDefinitionResponse;
import org.cloudfoundry.client.v2.spacequotadefinitions.ListSpaceQuotaDefinitionsRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.ListSpaceQuotaDefinitionsResponse;
import org.cloudfoundry.client.v2.spacequotadefinitions.RemoveSpaceQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.spacequotadefinitions.SpaceQuotaDefinitions;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.v2.AbstractClientV2Operations;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link SpaceQuotaDefinitions}
 */
public final class ReactorSpaceQuotaDefinitions extends AbstractClientV2Operations implements SpaceQuotaDefinitions {

    /**
     * Creates an instance
     *
     * @param connectionContext the {@link ConnectionContext} to use when communicating with the server
     * @param root              the root URI of the server.  Typically something like {@code https://api.run.pivotal.io}.
     * @param tokenProvider     the {@link TokenProvider} to use when communicating with the server
     */
    public ReactorSpaceQuotaDefinitions(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    @Override
    public Mono<AssociateSpaceQuotaDefinitionResponse> associateSpace(AssociateSpaceQuotaDefinitionRequest request) {
        return put(request, AssociateSpaceQuotaDefinitionResponse.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions", request.getSpaceQuotaDefinitionId(), "spaces", request.getSpaceId()));
    }

    @Override
    public Mono<CreateSpaceQuotaDefinitionResponse> create(CreateSpaceQuotaDefinitionRequest request) {
        return post(request, CreateSpaceQuotaDefinitionResponse.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions"));
    }

    @Override
    public Mono<DeleteSpaceQuotaDefinitionResponse> delete(DeleteSpaceQuotaDefinitionRequest request) {
        return delete(request, DeleteSpaceQuotaDefinitionResponse.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions", request.getSpaceQuotaDefinitionId()));
    }

    @Override
    public Mono<GetSpaceQuotaDefinitionResponse> get(GetSpaceQuotaDefinitionRequest request) {
        return get(request, GetSpaceQuotaDefinitionResponse.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions", request.getSpaceQuotaDefinitionId()));
    }

    @Override
    public Mono<ListSpaceQuotaDefinitionsResponse> list(ListSpaceQuotaDefinitionsRequest request) {
        return get(request, ListSpaceQuotaDefinitionsResponse.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions"));
    }

    @Override
    public Mono<Void> removeSpace(RemoveSpaceQuotaDefinitionRequest request) {
        return delete(request, Void.class,
            builder -> builder.pathSegment("v2", "space_quota_definitions", request.getSpaceQuotaDefinitionId(), "spaces", request.getSpaceId()));
    }

}
