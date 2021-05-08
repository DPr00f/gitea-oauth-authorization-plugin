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

package cd.go.authorization.gitea.requests;

import cd.go.authorization.gitea.executors.GetRolesExecutor;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetRolesRequestTest {
    private GoPluginApiRequest apiRequest;

    @Before
    public void setUp() {
        apiRequest = mock(GoPluginApiRequest.class);

        when(apiRequest.requestBody()).thenReturn("{\n" +
                "  \"auth_config\": {\n" +
                "    \"configuration\": {\n" +
                "       \"ApplicationId\": \"client-id\",\n" +
                "       \"ClientSecret\": \"client-secret\",\n" +
                "       \"GiteaUrl\": \"my-enterprise-url\",\n" +
                "       \"PersonalAccessToken\": \"Baz\"\n" +
                "    },\n" +
                "    \"id\": \"Gitea\"\n" +
                "  },\n" +
                "   \"role_configs\": [],\n" +
                "  \"username\": \"bob\"\n" +
                "}");
    }

    @Test
    public void shouldParseRequest() {
        GetRolesRequest request = (GetRolesRequest) GetRolesRequest.from(apiRequest);

        assertThat(request.getUsername(), is("bob"));
        assertThat(request.getAuthConfig().getId(), is("Gitea"));
        assertThat(request.getRoles(), hasSize(0));
    }

    @Test
    public void shouldReturnValidExecutor() {
        Request request = GetRolesRequest.from(apiRequest);

        assertThat(request.executor() instanceof GetRolesExecutor, is(true));
    }
}
