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

package cd.go.authorization.gitea.client;

import cd.go.authorization.gitea.CallbackURL;
import cd.go.authorization.gitea.client.models.*;
import cd.go.authorization.gitea.models.GiteaConfiguration;
import cd.go.authorization.gitea.models.TokenInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.List;

import static cd.go.authorization.gitea.utils.Util.GSON;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class GiteaClientTest {

    @Mock
    private GiteaConfiguration giteaConfiguration;
    private MockWebServer server;
    private GiteaClient giteaClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        server = new MockWebServer();
        server.start();

        when(giteaConfiguration.applicationId()).thenReturn("client-id");
        when(giteaConfiguration.clientSecret()).thenReturn("client-secret");
        when(giteaConfiguration.giteaBaseURL()).thenReturn("https://gitea.com");

        CallbackURL.instance().updateRedirectURL("callback-url");

        giteaClient = new GiteaClient(giteaConfiguration);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldReturnAuthorizationServerUrlForGitea() throws Exception {
        final String authorizationServerUrl = giteaClient.authorizationServerUrl("call-back-url");

        assertThat(authorizationServerUrl, startsWith("https://gitea.com/login/oauth/authorize?client_id=client-id&redirect_uri=call-back-url&response_type=code&scope=api&state="));
    }

    @Test
    public void shouldReturnAuthorizationServerUrlForGiteaEnterprise() throws Exception {
        when(giteaConfiguration.giteaBaseURL()).thenReturn("http://enterprise.url");

        final String authorizationServerUrl = giteaClient.authorizationServerUrl("call-back-url");

        assertThat(authorizationServerUrl, startsWith("http://enterprise.url/login/oauth/authorize?client_id=client-id&redirect_uri=call-back-url&response_type=code&scope=api&state="));
    }

    @Test
    public void shouldFetchTokenInfoUsingAuthorizationCode() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new TokenInfo("token-444248275346-5758603453985735", "bearer", 7200, "refresh-token").toJSON()));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final TokenInfo tokenInfo = giteaClient.fetchAccessToken("code");


        assertThat(tokenInfo.accessToken(), is("token-444248275346-5758603453985735"));

        RecordedRequest request = server.takeRequest();
        assertEquals("POST /login/oauth/access_token HTTP/1.1", request.getRequestLine());
        assertEquals("application/x-www-form-urlencoded", request.getHeader("Content-Type"));
        assertEquals("client_id=client-id&client_secret=client-secret&code=code&grant_type=authorization_code&redirect_uri=callback-url", request.getBody().readUtf8());
    }

    @Test
    public void shouldFetchUserProfile() throws Exception {
        final TokenInfo tokenInfo = new TokenInfo("token-444248275346-5758603453985735", "bearer", 7200, "refresh-token");
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new GiteaUser("username", "Display Name", "email").toJSON()));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final GiteaUser giteaUser = giteaClient.user(tokenInfo);

        assertThat(giteaUser.getUsername(), is("username"));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET /api/v1/user?access_token=token-444248275346-5758603453985735 HTTP/1.1", request.getRequestLine());
    }

    @Test
    public void shouldFetchGroupsForAUser() throws Exception {
        final String personalAccessToken = "some-random-token";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(GSON.toJson(asList(new GiteaGroup(1L, "foo-group")))));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final List<GiteaGroup> giteaGroups = giteaClient.groups(personalAccessToken);

        assertThat(giteaGroups, hasSize(1));
        assertThat(giteaGroups.get(0).getName(), is("foo-group"));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET /api/v1/groups HTTP/1.1", request.getRequestLine());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("token " + personalAccessToken, request.getHeaders().get("Authorization"));
    }

    @Test
    public void shouldFetchProjectsForAUser() throws Exception {
        final String personalAccessToken = "some-random-token";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(GSON.toJson(asList(new GiteaProject(1L, "foo-project")))));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final List<GiteaProject> giteaProjects = giteaClient.projects(personalAccessToken);

        assertThat(giteaProjects, hasSize(1));
        assertThat(giteaProjects.get(0).getName(), is("foo-project"));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET /api/v1/projects HTTP/1.1", request.getRequestLine());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("token " + personalAccessToken, request.getHeaders().get("Authorization"));
    }

    @Test
    public void shouldFetchGroupMembershipForAUser() throws Exception {
        final String personalAccessToken = "some-random-token";

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new MembershipInfo(1L, "foo-user", AccessLevel.DEVELOPER).toJSON()));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final MembershipInfo membershipInfo = giteaClient.groupMembershipInfo(personalAccessToken, 1L, 1L);

        assertThat(membershipInfo.getUsername(), is("foo-user"));
        assertThat(membershipInfo.getAccessLevel(), is(AccessLevel.DEVELOPER));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET /api/v1/groups/1/members/1 HTTP/1.1", request.getRequestLine());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("token " + personalAccessToken, request.getHeaders().get("Authorization"));
    }

    @Test
    public void shouldFetchProjectMembershipForAUser() throws Exception {
        final String personalAccessToken = "some-random-token";

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new MembershipInfo(1L, "foo-user", AccessLevel.DEVELOPER).toJSON()));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        final MembershipInfo membershipInfo = giteaClient.projectMembershipInfo(personalAccessToken, 1L, 1L);

        assertThat(membershipInfo.getUsername(), is("foo-user"));
        assertThat(membershipInfo.getAccessLevel(), is(AccessLevel.DEVELOPER));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET /api/v1/projects/1/members/1 HTTP/1.1", request.getRequestLine());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("token " + personalAccessToken, request.getHeaders().get("Authorization"));
    }

    @Test
    public void shouldErrorOutWhenAPIRequestFails() throws Exception {
        final TokenInfo tokenInfo = new TokenInfo("token-444248275346-5758603453985735", "bearer", 7200, "refresh-token");

        server.enqueue(new MockResponse().setResponseCode(403).setBody("Unauthorized"));

        when(giteaConfiguration.giteaBaseURL()).thenReturn(server.url("/").toString());

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Api call to `/api/v1/user` failed with error: `Unauthorized`");

        giteaClient.user(tokenInfo);
    }
}
