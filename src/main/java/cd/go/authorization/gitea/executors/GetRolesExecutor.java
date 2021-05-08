/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.gitea.executors;

import cd.go.authorization.gitea.GiteaAuthorizer;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.requests.GetRolesRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.List;

import static cd.go.authorization.gitea.GiteaPlugin.LOG;
import static cd.go.authorization.gitea.utils.Util.GSON;
import static java.lang.String.format;

public class GetRolesExecutor implements RequestExecutor {
    private final GetRolesRequest request;
    private final GiteaAuthorizer giteaAuthorizer;

    public GetRolesExecutor(GetRolesRequest request) {
        this(request, new GiteaAuthorizer());
    }

    GetRolesExecutor(GetRolesRequest request, GiteaAuthorizer giteaAuthorizer) {
        this.request = request;
        this.giteaAuthorizer = giteaAuthorizer;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        if (request.getRoles().isEmpty()) {
            LOG.debug("[Get User Roles] Server sent empty roles config. Nothing to do!.");
            return DefaultGoPluginApiResponse.success("[]");
        }

        GiteaUser user = request.getAuthConfig().giteaConfiguration().giteaClient().user(request.getAuthConfig().giteaConfiguration().personalAccessToken());

        if (user == null) {
            LOG.error(format("[Get User Roles] User %s does not exist in Gitea.", request.getUsername()));
            return DefaultGoPluginApiResponse.error("");
        }

        List<String> roles = giteaAuthorizer.authorize(user, request.getAuthConfig(), request.getRoles());

        LOG.debug(format("[Get User Roles] User %s has %s roles.", request.getUsername(), roles));
        return DefaultGoPluginApiResponse.success(GSON.toJson(roles));
    }
}
