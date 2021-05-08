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

import cd.go.authorization.gitea.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import cd.go.authorization.gitea.models.TokenInfo;
import cd.go.authorization.gitea.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.OkHttpClient;

public class FetchAccessTokenRequestExecutor implements RequestExecutor {
    private final FetchAccessTokenRequest request;
    private final OkHttpClient httpClient;

    public FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request) {
        this(request, new OkHttpClient());
    }

    FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request, OkHttpClient httpClient) {
        this.request = request;
        this.httpClient = httpClient;
    }

    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Get Access Token] No authorization configuration found.");
        }

        if (!request.requestParameters().containsKey("code")) {
            throw new IllegalArgumentException("Get Access Token] Expecting `code` in request params, but not received.");
        }

        final AuthConfig authConfig = request.authConfigs().get(0);
        final GiteaConfiguration giteaConfiguration = authConfig.giteaConfiguration();

        final TokenInfo tokenInfo = giteaConfiguration.giteaClient().fetchAccessToken(request.requestParameters().get("code"));

        return DefaultGoPluginApiResponse.success(tokenInfo.toJSON());
    }
}
