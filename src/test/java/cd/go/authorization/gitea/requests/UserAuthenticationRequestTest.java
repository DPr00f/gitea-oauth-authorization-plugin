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

package cd.go.authorization.gitea.requests;

import cd.go.authorization.gitea.executors.UserAuthenticationRequestExecutor;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.TokenInfo;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserAuthenticationRequestTest {
    @Mock
    private GoPluginApiRequest request;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToUserAuthenticationRequest() throws Exception {
        String responseBody = "{\n" +
                "  \"credentials\": {\n" +
                "    \"access_token\": \"access-token\",\n" +
                "    \"token_type\": \"token\",\n" +
                "    \"refresh_token\": \"refresh-token\",\n" +
                "    \"expires_in\": \"7200\"\n" +
                "  },\n" +
                "  \"auth_configs\": [\n" +
                "    {\n" +
                "      \"id\": \"gitea-config\",\n" +
                "      \"configuration\": {\n" +
                "        \"ApplicationId\": \"client-id\",\n" +
                "        \"ClientSecret\": \"client-secret\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]" +
                "}";

        when(request.requestBody()).thenReturn(responseBody);

        final UserAuthenticationRequest request = UserAuthenticationRequest.from(this.request);

        assertThat(request.authConfigs(), hasSize(1));
        assertThat(request.executor(), instanceOf(UserAuthenticationRequestExecutor.class));

        assertAuthConfig(request.authConfigs().get(0));
        assertTokenInfo(request.tokenInfo());
    }

    private void assertTokenInfo(TokenInfo tokenInfo) {
        assertThat(tokenInfo.accessToken(), is("access-token"));
        assertThat(tokenInfo.tokenType(), is("token"));
        assertThat(tokenInfo.expiresIn(), is(7200));
        assertThat(tokenInfo.refreshToken(), is("refresh-token"));
    }

    private void assertAuthConfig(AuthConfig authConfig) {
        assertThat(authConfig.getId(), is("gitea-config"));
        assertThat(authConfig.giteaConfiguration().applicationId(), is("client-id"));
        assertThat(authConfig.giteaConfiguration().clientSecret(), is("client-secret"));
    }
}
