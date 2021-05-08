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
import cd.go.authorization.gitea.client.GiteaClient;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import cd.go.authorization.gitea.models.GiteaRole;
import cd.go.authorization.gitea.requests.GetRolesRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GetRolesExecutorTest {
    private GetRolesRequest request;
    private GetRolesExecutor executor;

    private GiteaAuthorizer authorizer;
    private GiteaClient giteaClient;
    private GiteaUser giteaUser;
    private final String PERSONAL_ACCESS_TOKEN = "some-random-token";

    @Before
    public void setUp() throws Exception {
        request = mock(GetRolesRequest.class);
        authorizer = mock(GiteaAuthorizer.class);

        AuthConfig authConfig = mock(AuthConfig.class);
        giteaClient = mock(GiteaClient.class);
        giteaUser = mock(GiteaUser.class);
        when(request.getAuthConfig()).thenReturn(authConfig);
        when(request.getUsername()).thenReturn("bob");
        GiteaConfiguration giteaConfiguration = mock(GiteaConfiguration.class);
        when(authConfig.giteaConfiguration()).thenReturn(giteaConfiguration);
        when(giteaConfiguration.giteaClient()).thenReturn(giteaClient);
        when(giteaConfiguration.personalAccessToken()).thenReturn(PERSONAL_ACCESS_TOKEN);

        executor = new GetRolesExecutor(request, authorizer);
    }

    @Test
    public void shouldReturnEmptyResponseIfThereAreNoRolesProvidedFromRequest() throws Exception {

        when(giteaClient.user(anyString())).thenReturn(giteaUser);

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
        verifyZeroInteractions(authorizer);
    }

    @Test
    public void shouldReturnSuccessResponseWithRoles() throws Exception {
        when(giteaClient.user(PERSONAL_ACCESS_TOKEN)).thenReturn(giteaUser);
        when(request.getRoles()).thenReturn(rolesWithName("blackbird", "super-admin", "view"));
        when(authorizer.authorize(giteaUser, request.getAuthConfig(), request.getRoles())).thenReturn(Arrays.asList("blackbird", "super-admin"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\"blackbird\",\"super-admin\"]", response.responseBody(), true);

        verify(giteaClient).user(request.getAuthConfig().giteaConfiguration().personalAccessToken());
        verify(authorizer).authorize(giteaUser, request.getAuthConfig(), request.getRoles());
    }

    @Test
    public void shouldReturnErrorResponseWhenUserWithProvidedUsernameNotFound() throws Exception {
        when(request.getRoles()).thenReturn(rolesWithName("blackbird", "super-admin", "view"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(500));
        verify(giteaClient).user(request.getAuthConfig().giteaConfiguration().personalAccessToken());
        verifyZeroInteractions(authorizer);
    }

    private GiteaRole roleWithName(String name) {
        return GiteaRole.fromJSON("{\"name\":\"" + name + "\"}");
    }

    private List<GiteaRole> rolesWithName(String... names) {
        return Arrays.stream(names).map(this::roleWithName).collect(Collectors.toList());
    }
}
