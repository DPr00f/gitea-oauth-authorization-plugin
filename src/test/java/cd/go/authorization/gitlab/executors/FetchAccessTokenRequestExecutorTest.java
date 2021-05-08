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

import cd.go.authorization.gitea.client.GitLabClient;
import cd.go.authorization.gitea.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.GitLabConfiguration;
import cd.go.authorization.gitea.models.TokenInfo;
import cd.go.authorization.gitea.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchAccessTokenRequestExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FetchAccessTokenRequest fetchAccessTokenRequest;
    private AuthConfig authConfig;
    private GitLabConfiguration gitLabConfiguration;
    private FetchAccessTokenRequestExecutor executor;

    @Before
    public void setUp() throws Exception {
        fetchAccessTokenRequest = mock(FetchAccessTokenRequest.class);
        authConfig = mock(AuthConfig.class);
        gitLabConfiguration = mock(GitLabConfiguration.class);

        when(authConfig.gitLabConfiguration()).thenReturn(gitLabConfiguration);

        executor = new FetchAccessTokenRequestExecutor(fetchAccessTokenRequest);
    }

    @Test(expected = NoAuthorizationConfigurationException.class)
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn("{\"auth_configs\":[]}");

        FetchAccessTokenRequestExecutor executor = new FetchAccessTokenRequestExecutor(FetchAccessTokenRequest.from(request));

        executor.execute();
    }

    @Test
    public void shouldFetchAccessToken() throws Exception {
        final GitLabClient gitLabClient = mock(GitLabClient.class);

        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));
        when(authConfig.gitLabConfiguration()).thenReturn(gitLabConfiguration);
        when(gitLabConfiguration.gitLabClient()).thenReturn(gitLabClient);
        when(gitLabClient.fetchAccessToken("code-received-in-previous-step")).thenReturn(new TokenInfo("token-444248275346-5758603453985735", "bearer", 7200, "refresh-token"));

        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"access_token\": \"token-444248275346-5758603453985735\",\n" +
                "  \"token_type\": \"bearer\",\n" +
                "  \"expires_in\": 7200,\n" +
                "  \"refresh_token\": \"refresh-token\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
