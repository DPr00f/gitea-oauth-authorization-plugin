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

package cd.go.authorization.gitea;

import cd.go.authorization.gitea.client.GiteaClient;
import cd.go.authorization.gitea.client.models.GiteaUser;
import cd.go.authorization.gitea.models.AuthConfig;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import cd.go.authorization.gitea.models.TokenInfo;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GiteaAuthenticatorTest {

    private GiteaAuthenticator authenticator;
    private AuthConfig authConfig;
    private GiteaConfiguration giteaConfiguration;
    private TokenInfo tokenInfo;
    private GiteaClient giteaClient;

    @Before
    public void setUp() throws Exception {
        authConfig = mock(AuthConfig.class);
        giteaConfiguration = mock(GiteaConfiguration.class);
        tokenInfo = mock(TokenInfo.class);
        giteaClient = mock(GiteaClient.class);

        when(authConfig.giteaConfiguration()).thenReturn(giteaConfiguration);
        when(giteaConfiguration.giteaClient()).thenReturn(giteaClient);

        authenticator = new GiteaAuthenticator();
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        when(giteaClient.user(tokenInfo)).thenReturn(new GiteaUser("username", "DisplayName", "email"));

        final GiteaUser user = authenticator.authenticate(tokenInfo, authConfig);

        assertThat(user, is(new GiteaUser("username", "DisplayName", "email")));
    }
}
