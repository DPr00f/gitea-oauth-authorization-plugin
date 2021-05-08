/*
 * Copyright 2017 ThoughtWorks, Inc.
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

import cd.go.authorization.gitea.GiteaAuthenticator;
import cd.go.authorization.gitea.GiteaAuthorizer;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.User;
import cd.go.authorization.gitea.requests.UserAuthenticationRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

import static cd.go.authorization.gitea.utils.Util.GSON;
import static com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;

public class UserAuthenticationRequestExecutor implements RequestExecutor {
    private final UserAuthenticationRequest request;
    private final GiteaAuthenticator giteaAuthenticator;
    private final GiteaAuthorizer giteaAuthorizer;

    public UserAuthenticationRequestExecutor(UserAuthenticationRequest request) {
        this(request, new GiteaAuthenticator(), new GiteaAuthorizer());
    }

    UserAuthenticationRequestExecutor(UserAuthenticationRequest request, GiteaAuthenticator giteaAuthenticator, GiteaAuthorizer giteaAuthorizer) {
        this.request = request;
        this.giteaAuthenticator = giteaAuthenticator;
        this.giteaAuthorizer = giteaAuthorizer;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Authenticate] No authorization configuration found.");
        }

        final AuthConfig authConfig = request.authConfigs().get(0);
        final GiteaUser giteaUser = giteaAuthenticator.authenticate(request.tokenInfo(), authConfig);

        Map<String, Object> userMap = new HashMap<>();
        if (giteaUser != null) {
            userMap.put("user", new User(giteaUser));
            userMap.put("roles", giteaAuthorizer.authorize(giteaUser, authConfig, request.roles()));
        }

        DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, GSON.toJson(userMap));
        return response;
    }
}
