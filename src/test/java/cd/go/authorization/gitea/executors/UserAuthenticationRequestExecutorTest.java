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
import cd.go.authorization.gitea.models.TokenInfo;
import cd.go.authorization.gitea.requests.UserAuthenticationRequest;
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

public class UserAuthenticationRequestExecutorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private UserAuthenticationRequest request;
    private AuthConfig authConfig;

    private UserAuthenticationRequestExecutor executor;
    private GiteaAuthenticator authenticator;
    private GiteaAuthorizer authorizer;

    @Before
    public void setUp() throws Exception {
        request = mock(UserAuthenticationRequest.class);
        authConfig = mock(AuthConfig.class);
        authenticator = mock(GiteaAuthenticator.class);
        authorizer = mock(GiteaAuthorizer.class);

        executor = new UserAuthenticationRequestExecutor(request, authenticator, authorizer);
    }

    @Test
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        when(request.authConfigs()).thenReturn(Collections.emptyList());

        thrown.expect(NoAuthorizationConfigurationException.class);
        thrown.expectMessage("[Authenticate] No authorization configuration found.");

        executor.execute();
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        final GiteaUser giteaUser = mock(GiteaUser.class);
        final TokenInfo tokenInfo = new TokenInfo("access-token", "token-type", 7200, "refresh-token");

        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.tokenInfo()).thenReturn(tokenInfo);
        when(authenticator.authenticate(tokenInfo, authConfig)).thenReturn(giteaUser);
        when(giteaUser.getEmail()).thenReturn("bford@example.com");
        when(giteaUser.getName()).thenReturn("Bob");
        when(giteaUser.getUsername()).thenReturn("bford");

        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"roles\": [],\n" +
                "  \"user\": {\n" +
                "    \"username\": \"bford\",\n" +
                "    \"display_name\": \"Bob\",\n" +
                "    \"email\": \"bford@example.com\"\n" +
                "  }\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
