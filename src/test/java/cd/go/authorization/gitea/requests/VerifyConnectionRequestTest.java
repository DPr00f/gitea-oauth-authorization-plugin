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

import cd.go.authorization.gitea.executors.VerifyConnectionRequestExecutor;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerifyConnectionRequestTest {
    @Mock
    private GoPluginApiRequest apiRequest;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToVerifyConnectionRequest() throws Exception {
        String responseBody = "{\n" +
                "  \"ApplicationId\": \"client-id\",\n" +
                "  \"ClientSecret\": \"client-secret\",\n" +
                "  \"GiteaUrl\": \"my-enterprise-url\"" +
                "}";

        when(apiRequest.requestBody()).thenReturn(responseBody);

        final VerifyConnectionRequest request = VerifyConnectionRequest.from(apiRequest);
        final GiteaConfiguration giteaConfiguration = request.giteaConfiguration();

        assertThat(request.executor(), instanceOf(VerifyConnectionRequestExecutor.class));

        assertThat(giteaConfiguration.applicationId(), is("client-id"));
        assertThat(giteaConfiguration.clientSecret(), is("client-secret"));
        assertThat(giteaConfiguration.giteaUrl(), is("my-enterprise-url"));
    }

}
